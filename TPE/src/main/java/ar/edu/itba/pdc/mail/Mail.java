package ar.edu.itba.pdc.mail;

import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.apache.log4j.Logger;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mail {

    private static final ProxyConfiguration CONFIGURATION = ProxyConfiguration.INSTANCE;
    private static final Logger LOGGER = Logger.getLogger(Mail.class);

    private static final Pattern boundaryAttribute = Pattern.compile("(?:boundary=\"?)([^\"]*)");
    private static final Pattern boundaryClose = Pattern.compile("^(?:--)(.*)(?:--)$");

    private static final String
            SUBJECT = "subject:",
            CONTENT_TYPE = "content-type: ",
            PIC = "content-type: image",
            HEADER = "header",
            BODY = "body";

    private String originalMail[];

    private StringBuffer answer;

    public Mail(String original) {
        this.originalMail = original.split("\r\n");
        this.answer = new StringBuffer();
        parse();
    }

    private void parse(){
        LOGGER.info("Parsing mail...");
        String line;

        Stack<String> boundaryStack = new Stack<>();
        String mailPart;
        String transformedLine = "";
        mailPart = HEADER;
        int i = 0;
        while(i < originalMail.length){
            line = originalMail[i];
            Matcher boundaryCloseMatcher = boundaryClose.matcher(line);

            switch (mailPart){
                case HEADER:
                    while (originalMail[i+1].matches("\\s.*")) {
                        i++;
                        line = line + originalMail[i];
                    }
                    if (line.toLowerCase().startsWith(SUBJECT)) {
                        transformedLine = line.split(":")[0] + ":" + CONFIGURATION.subjectTransformation()
                                .transform(line.split(":")[1], null);

                    } else if (line.toLowerCase().startsWith(CONTENT_TYPE)) {
                        pushIfContainsBoundary(line, boundaryStack);
                        transformedLine = line;
                    }  else if (line.isEmpty()){
                        mailPart = BODY;
                        transformedLine = line;
                    }
                    else {
                        transformedLine = line;
                    }
                    break;

                case BODY:
                    if (line.toLowerCase().startsWith(CONTENT_TYPE)) {
                        while (originalMail[i+1].matches("\\s.*")) {
                            i++;
                            line = line + originalMail[i];
                        }
                        pushIfContainsBoundary(line, boundaryStack);
                        if (line.toLowerCase().contains(PIC)){
                            String imageType = line.split("/")[1].split(";")[0];
                            StringBuilder imageBuilder = new StringBuilder();
                            while (!line.startsWith("--")){
                                if (line.contains(":") || line.matches("\\s.*") || line.isEmpty()){
                                    answer.append(line);
                                    answer.append("\r\n");
                                } else {
                                    imageBuilder.append(line);
                                    imageBuilder.append("\r\n");
                                }
                                i++;
                                line = originalMail[i];
                            }
                            i--;
                            if (imageBuilder.length() / (1024*1024) > CONFIGURATION.maxImageLengthMb()){
                                answer.append(imageBuilder.toString());
                            } else {
                                answer.append(CONFIGURATION.imageTransformation()
                                        .transform(imageBuilder.toString(), imageType));
                            }
                        } else {
                            transformedLine = line;
                        }
                    } else if (boundaryCloseMatcher.matches()) {
                        // Cierra un boundary
                        String boundary = boundaryCloseMatcher.group(1);
                        if (boundaryStack.size() > 0 && boundaryStack.peek().equals(boundary)) {
                            boundaryStack.pop();
                        }
                        transformedLine = line;
                    } else {
                        transformedLine = line;
                    }
                    break;

                default:
                    transformedLine = line;
                    break;
            }
            i++;
            answer.append(transformedLine);
            answer.append("\r\n");
        }
        originalMail = null; // So GC can save memory
        LOGGER.info("Size of boundary Stack is: " + boundaryStack.size());
    }

    @Override
    public String toString() {
        return answer.toString();
    }

    private void pushIfContainsBoundary(String line, Stack<String> stack){
        if (line.contains("boundary")) {
            Matcher boundaryAttributeMatcher = boundaryAttribute.matcher(line);
            if (boundaryAttributeMatcher.find()) {
                stack.push(boundaryAttributeMatcher.group(1));
            }
        }
    }

}
