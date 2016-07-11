package ar.edu.itba.pdc.mail;

import java.util.regex.Pattern;

public class ParserUtils {
    public static final Pattern boundaryAttribute = Pattern.compile("(?:boundary=\"?)([^\";]*)");
    public static final Pattern boundaryClose = Pattern.compile("^(?:--)(.*)(?:--)$");
    public static final Pattern boundary = Pattern.compile("^(?:--)(.*)$");
    public static final String CONTENT_TYPE = "content-type",
            PIC = "content-type: image",
            SUBJECT = "subject",
            END = ".",
            CRLF = "\r\n",
            BOUNDARY_START = "--",
            EMPTY = "",
            MAIL_END = "\r\n.\r\n",
            WHITESPACE = "\\s.*";

    public static String arrayToString(String[] array) {
        StringBuilder answer = new StringBuilder();
        for (String line : array) {
            answer.append(line);
        }
        return answer.toString();
    }

    public static String getImageType(String line) {
        return line.split("/")[1].split(";")[0];
    }

    public static int getImageSizeInMb(StringBuffer image) {
        int MB = 1024 * 1024;
        return image.length() / MB;
    }

    public static String completeLine(String line) {
        return line + CRLF;
    }
}
