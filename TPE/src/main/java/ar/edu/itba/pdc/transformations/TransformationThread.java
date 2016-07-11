package ar.edu.itba.pdc.transformations;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.mail.BodyStatus;
import ar.edu.itba.pdc.mail.ParserUtils;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.util.Queue;
import java.util.regex.Matcher;

@SuppressWarnings("Duplicates")
public class TransformationThread extends Thread {
    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;

    private final Connection connection;
    private final ProxyHandler handler;
    private StringBuffer imageBuilder;
    private String imageType;
    private Queue<String> cache;

    private BodyStatus currentStatus;

    private static final Logger LOGGER = Logger.getLogger(TransformationThread.class);

    public TransformationThread(ProxyHandler handler, Connection connection, StringBuffer imageBuilder, String imageType, Queue<String> cache) {
        this.connection = connection;
        this.handler = handler;
        this.imageBuilder = imageBuilder;
        this.imageType = imageType;
        this.cache = cache;
        this.currentStatus = BodyStatus.DEFAULT;
    }

    @Override
    public void run() {
        try {
            handler.writeToClient(transformAndResetBuilder(), connection);
            boolean keepProcessing = true;

            String line;
            while (keepProcessing) {
                line = cache.poll();
                if (line != null) {
                    if (line.equals(ParserUtils.END)) {
                        handler.writeToClient(ParserUtils.MAIL_END, connection);
                        keepProcessing = false;
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
                            handler.writeToClient(ParserUtils.completeLine(line), connection);
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
                            handler.writeToClient(ParserUtils.completeLine(line), connection);
                            break;

                        case GETTING_IMAGE_HEADER:
                            if (line.isEmpty()) {
                                currentStatus = BodyStatus.GETTING_IMAGE;
                            }
                            handler.writeToClient(ParserUtils.completeLine(line), connection);
                            break;

                        case GETTING_IMAGE:
                            if (line.startsWith(ParserUtils.BOUNDARY_START)) {
                                handler.writeToClient(transformAndResetBuilder() + ParserUtils.completeLine(line), connection);
                                currentStatus = BodyStatus.DEFAULT;
                            } else {
                                if (line.isEmpty()) {
                                    break;
                                }
                                if (ParserUtils.getImageSizeInMb(imageBuilder) > CONFIGURATION.maxImageLengthMb()) {
                                    currentStatus = BodyStatus.IMAGE_SIZE_EXCEEDED;
                                    imageBuilder.append(ParserUtils.completeLine(line));
                                    String ans = imageBuilder.toString();
                                    imageBuilder.delete(0, ans.length());
                                    handler.writeToClient(ans, connection);
                                }
                                imageBuilder.append(line);
                            }
                            break;

                        case IMAGE_SIZE_EXCEEDED:
                            if (line.startsWith(ParserUtils.BOUNDARY_START)) {
                                currentStatus = BodyStatus.DEFAULT;
                            }
                            handler.writeToClient(ParserUtils.completeLine(line), connection);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            connection.close();
            return;
        }

        connection.prepareForNextMail();
    }

    private String transformAndResetBuilder() {
        String original = imageBuilder.toString();
        String image = connection.getImageTransformation().transform(original, imageType);
        imageBuilder.delete(0, original.length());
        return ParserUtils.completeLine(image);
    }
}
