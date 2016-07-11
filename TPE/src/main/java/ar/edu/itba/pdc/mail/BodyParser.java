package ar.edu.itba.pdc.mail;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.transformations.NoneTransformation;
import ar.edu.itba.pdc.transformations.Transformation;
import ar.edu.itba.pdc.transformations.TransformationThread;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

public class BodyParser implements Parser {
    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(BodyParser.class);

    private ProxyHandler handler;
    private Connection connection;

    private BodyStatus currentStatus;

    private String imageType;
    private StringBuffer imageBuilder;
    private Queue<String> cache;

    private AtomicBoolean transform;

    public BodyParser(ProxyHandler handler, Connection connection) {
        this.handler = handler;
        this.connection = connection;
        reset();
    }

    @Override
    public String parse(String line) {
        final boolean transform = getTransform();
        if (transform && connection.getBoundaryStack().size() > 0) {

            if (line.equals(ParserUtils.END) && currentStatus != BodyStatus.PROCESSING_IMAGE) {
                currentStatus = BodyStatus.ENDED;
            }

            switch (currentStatus) {
                case DEFAULT:

                    Matcher boundaryCloseMatcher = ParserUtils.boundaryClose.matcher(line);
                    Matcher boundaryOpenMatcher = ParserUtils.boundary.matcher(line);

                    if (line.toLowerCase().contains(ParserUtils.PIC) && !connection.getInBody()) {
                        currentStatus = BodyStatus.GETTING_IMAGE_HEADER;
                        imageType = ParserUtils.getImageType(line);
                    } else if (line.toLowerCase().contains(ParserUtils.CONTENT_TYPE)) {
                        Matcher boundaryMatcher = ParserUtils.boundaryAttribute.matcher(line);
                        if (boundaryMatcher.find()) {
                            String boundary = boundaryMatcher.group(1).trim();
                            connection.getBoundaryStack().push(boundary);
                            LOGGER.debug("Found boundary = " + boundary);
                        }
                        currentStatus = BodyStatus.BODY_HEADER;
                    } else if (boundaryCloseMatcher.find()) {
                        // Cierra un boundary
                        String boundary = boundaryCloseMatcher.group(1);
                        if (connection.getBoundaryStack().size() > 0 && connection.getBoundaryStack().peek().equals(boundary)) {
                            connection.getBoundaryStack().pop();
                        }
                    } else if (boundaryOpenMatcher.find()) {
                        String boundary = boundaryOpenMatcher.group(1);
                        if (connection.getBoundaryStack().size() > 0 && connection.getBoundaryStack().peek().equals(boundary)) {
                            connection.setInBody(false);
                        }
                    }
                    break;

                case BODY_HEADER:
                    Matcher boundaryMatcher = ParserUtils.boundaryAttribute.matcher(line);
                    if (boundaryMatcher.find()) {
                        String boundary = boundaryMatcher.group(1).trim();
                        connection.getBoundaryStack().push(boundary);
                        LOGGER.debug("Found boundary = " + boundary);
                    } else if (line.isEmpty()) {
                        connection.setInBody(true);
                        currentStatus = BodyStatus.DEFAULT;
                    }
                    break;

                case GETTING_IMAGE_HEADER:
                    if (line.isEmpty()) {
                        connection.setInBody(true);
                        currentStatus = BodyStatus.GETTING_IMAGE;
                    }
                    break;

                case GETTING_IMAGE:
                    if (line.startsWith(ParserUtils.BOUNDARY_START)) {
                        connection.setInBody(false);
                        getCache().add(line);
                        CONFIGURATION.getThreadPool().execute(new TransformationThread(handler, connection, getImageBuilder(), imageType, cache));
                        currentStatus = BodyStatus.PROCESSING_IMAGE;
                    } else {
                        if (ParserUtils.getImageSizeInMb(getImageBuilder()) > CONFIGURATION.maxImageLengthMb()) {
                            currentStatus = BodyStatus.IMAGE_SIZE_EXCEEDED;
                            getImageBuilder().append(line).append(ParserUtils.CRLF);
                            String ans = getImageBuilder().toString();
                            resetImageBuilder();
                            return ans;
                        }
                        getImageBuilder().append(line);
                    }
                    // Return nothing by default, the other thread will answer
                    return ParserUtils.EMPTY;

                case PROCESSING_IMAGE:
                    getCache().add(line);
                    return ParserUtils.EMPTY;

                case IMAGE_SIZE_EXCEEDED:
                    if (line.startsWith(ParserUtils.BOUNDARY_START)) {
                        currentStatus = BodyStatus.DEFAULT;
                    }
                    break;
            }

        } else {
            if (line.equals(ParserUtils.END)) {
                currentStatus = BodyStatus.ENDED;
            }
        }
        return ParserUtils.completeLine(line);
    }

    @Override
    public void reset() {
        this.currentStatus = BodyStatus.DEFAULT;
        this.transform = null;
    }

    private boolean getTransform() {
        if (this.transform == null) {
            Transformation transformation;
            if (connection != null) {
                transformation = connection.getImageTransformation();
            } else {
                transformation = CONFIGURATION.imageTransformation();
            }

            if (transformation == null || transformation == NoneTransformation.INSTANCE) {
                this.transform = new AtomicBoolean(false);
            } else {
                this.transform = new AtomicBoolean(true);
            }
        }
        return this.transform.get();
    }

    @Override
    public boolean ended() {
        return this.currentStatus == BodyStatus.ENDED;
    }

    private Queue<String> getCache() {
        if (this.cache == null) {
            this.cache = new ConcurrentLinkedQueue<>();
        }
        return this.cache;
    }

    private void resetImageBuilder() {
        this.imageBuilder = null;
    }

    private StringBuffer getImageBuilder() {
        if (this.imageBuilder == null){
            this.imageBuilder = new StringBuffer();
        }
        return this.imageBuilder;
    }

}
