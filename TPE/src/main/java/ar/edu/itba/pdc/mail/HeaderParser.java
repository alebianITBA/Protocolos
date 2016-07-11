package ar.edu.itba.pdc.mail;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.transformations.NoneTransformation;
import ar.edu.itba.pdc.transformations.Transformation;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;

public class HeaderParser implements Parser {
    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(HeaderParser.class);

    private ProxyHandler handler;
    private Connection connection;

    private HeaderStatus currentStatus;
    private String lastHeader;
    private String boundary;

    private AtomicBoolean transform;

    public HeaderParser(ProxyHandler handler, Connection connection) {
        this.handler = handler;
        this.connection = connection;
        reset();
    }

    @Override
    public String parse(String line) {
        if (line == null || line.isEmpty()) {
            currentStatus = HeaderStatus.ENDED;
            return ParserUtils.CRLF;
        }

        if (getTransform()) {
            int splitAt = line.indexOf(':');
            if (splitAt != -1) {
                // Es un header nuevo
                String header = line.substring(0, splitAt).trim();
                if (header.contains(" ")) {
                    return ParserUtils.completeLine(line);
                }
                String value = line.substring(splitAt + 1).trim();
                return ParserUtils.completeLine(parseNewHeader(header, value));
            } else if (line.matches(ParserUtils.WHITESPACE)) {
                // Es la continuacion del header anterior
                return ParserUtils.completeLine(parseHeaderContinuation(line));
            } else { // Si entra en este caso el mail está mal formado
                return ParserUtils.completeLine(line);
            }
        } else {
            return ParserUtils.completeLine(line);
        }
    }

    private String parseNewHeader(String header, String value) {
        String transformed = ParserUtils.EMPTY;

        switch (header.toLowerCase()) {
            case ParserUtils.SUBJECT:
                if (header.toLowerCase().equals(ParserUtils.SUBJECT)) {
                    String transformedSubject = CONFIGURATION.subjectTransformation().transform(value, null);
                    transformed = header + ": " + transformedSubject;
                }
                break;
            case ParserUtils.CONTENT_TYPE:
                Matcher boundaryMatcher = ParserUtils.boundaryAttribute.matcher(value);
                if (boundaryMatcher.find()) {
                    boundary = boundaryMatcher.group(1).trim();
                    connection.getBoundaryStack().push(boundary);
                    LOGGER.debug("Found boundary = " + boundary);
                }
            default:
                if (value.isEmpty()) {
                    transformed = header + ":";
                } else {
                    transformed = header + ": " + value;
                }
                break;
        }
        lastHeader = header.toLowerCase();
        return transformed;
    }

    private String parseHeaderContinuation(String line) {
        switch (lastHeader) {
            case ParserUtils.SUBJECT:
                return CONFIGURATION.subjectTransformation().transform(line, null);
            case ParserUtils.CONTENT_TYPE:
                Matcher boundaryMatcher = ParserUtils.boundaryAttribute.matcher(line);
                if (boundaryMatcher.find()) {
                    boundary = boundaryMatcher.group(1).trim();
                    connection.getBoundaryStack().push(boundary);
                    LOGGER.debug("Found boundary = " + boundary);
                }
            default:
                return line;
        }
    }

    @Override
    public void reset() {
        this.currentStatus = HeaderStatus.BEFORE_SUBJECT;
        this.transform = null;
    }

    @Override
    public boolean ended() {
        return currentStatus == HeaderStatus.ENDED;
    }

    private boolean getTransform() {
        if (this.transform == null) {
            Transformation transformation;
            transformation = connection.getSubjectTransformation();

            if (transformation == null || transformation == NoneTransformation.INSTANCE) {
                this.transform = new AtomicBoolean(false);
            } else {
                this.transform = new AtomicBoolean(true);
            }
        }
        return this.transform.get();
    }

}
