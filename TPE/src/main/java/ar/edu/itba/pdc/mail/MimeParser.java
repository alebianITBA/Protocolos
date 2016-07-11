package ar.edu.itba.pdc.mail;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.handler.ProxyHandler;
import org.apache.log4j.Logger;

public class MimeParser implements Parser {
    private ProxyHandler handler;
    private Connection connection;

    private MailStatus currentStatus;

    private HeaderParser headerParser;
    private BodyParser bodyParser;

    public MimeParser(ProxyHandler handler, Connection connection) {
        this.handler = handler;
        this.connection = connection;
        reset();
    }

    @Override
    public String parse(String mailPart) {
        if (mailPart.isEmpty()) {
            return ParserUtils.CRLF;
        }

        String[] splitted = mailPart.split(ParserUtils.CRLF);
        int i = 0;
        while(i < splitted.length) {

            switch (currentStatus) {
                case HEADER:
                    splitted[i] = getHeaderParser().parse(splitted[i]);
                    if (getHeaderParser().ended()) {
                        currentStatus = MailStatus.BODY;
                        getHeaderParser().reset();
                    }
                    break;
                case BODY:
                    splitted[i] = getBodyParser().parse(splitted[i]);
                    if (getBodyParser().ended()) {
                        currentStatus = MailStatus.ENDED;
                        getBodyParser().reset();
                    }
                    break;
                case ENDED:
                    // If we are here it If we are here it means that we received the end of an email and the beginning of another one
                    currentStatus = MailStatus.HEADER;
                    splitted[i] = getHeaderParser().parse(splitted[i]);
                    break;
            }

            if (currentStatus == MailStatus.ENDED) {
                reset();
            }

            i++;

        }

        return ParserUtils.arrayToString(splitted);
    }

    private HeaderParser getHeaderParser() {
        if (headerParser == null) {
            headerParser = new HeaderParser(handler, connection);
        }
        return headerParser;
    }

    private BodyParser getBodyParser() {
        if (bodyParser == null) {
            bodyParser = new BodyParser(handler, connection);
        }
        return bodyParser;
    }

    @Override
    public void reset() {
        this.currentStatus = MailStatus.HEADER;
        getHeaderParser().reset();
        getBodyParser().reset();
    }

    @Override
    public boolean ended() {
        return false;
    }
}
