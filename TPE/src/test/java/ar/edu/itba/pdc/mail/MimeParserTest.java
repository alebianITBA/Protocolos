package ar.edu.itba.pdc.mail;

import ar.edu.itba.pdc.connection.Connection;
import ar.edu.itba.pdc.handler.ProxyHandler;
import ar.edu.itba.pdc.utils.ProxyConfiguration;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;

import static org.junit.Assert.assertEquals;

public class MimeParserTest {
    private String two_subjects,
            two_subjects_trans,
            weird_case,
            weird_case_trans,
            base_64,
            base_64_trans,
            quoted_printable,
            quoted_printable_trans,
            mime,
            mime_trans,
            no_subject,
            no_subject_trans,
            subject_in_body,
            subject_in_body_trans,
            multiline_subject_mail,
            multiline_subject_mail_trans;

    private final Connection connection = mock(Connection.class);

    private final ProxyHandler handler = mock(ProxyHandler.class);

    private MimeParser mimeParser = new MimeParser(handler, connection);

    @Before
    public void setUp() throws IOException {
        when(connection.getBoundaryStack()).thenReturn(new Stack<>());

        two_subjects = readFile("src/test/resources/two_subjects.txt");
        two_subjects_trans = readFile("src/test/resources/two_subjects_transformed.txt");
        weird_case = readFile("src/test/resources/weird_case.txt");
        weird_case_trans = readFile("src/test/resources/weird_case_transformed.txt");
        base_64 = readFile("src/test/resources/base_64.txt");
        base_64_trans = readFile("src/test/resources/base_64_transformed.txt");
        quoted_printable = readFile("src/test/resources/quoted_printable.txt");
        quoted_printable_trans = readFile("src/test/resources/quoted_printable_transformed.txt");
        no_subject = readFile("src/test/resources/no_subject.txt");
        no_subject_trans = readFile("src/test/resources/no_subject_transformed.txt");
        subject_in_body = readFile("src/test/resources/subject_in_body.txt");
        subject_in_body_trans = readFile("src/test/resources/subject_in_body_transformed.txt");
        multiline_subject_mail = readFile("src/test/resources/multiline_subject_mail.txt");
        multiline_subject_mail_trans = readFile("src/test/resources/multiline_subject_mail_transformed.txt");
        mime = readFile("src/test/resources/mime.txt");
        mime_trans = readFile("src/test/resources/mime_transformed.txt");
    }

    @Test
    public void twoSubjectsTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(two_subjects, mimeParser.parse(two_subjects));

        // With transformations
        activateTransformations();
        assertEquals(two_subjects_trans, mimeParser.parse(two_subjects));
    }

    @Test
    public void weirdCaseTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(weird_case, mimeParser.parse(weird_case));

        // With transformations
        activateTransformations();
        assertEquals(weird_case_trans, mimeParser.parse(weird_case));
    }

    @Test
    public void base64Test() {
        // Without transformations
        deactivateTransformations();
        assertEquals(base_64, mimeParser.parse(base_64));

        // With transformations
        activateTransformations();
        assertEquals(base_64_trans, mimeParser.parse(base_64));
    }

    @Test
    public void quotedPrintableTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(quoted_printable, mimeParser.parse(quoted_printable));

        // With transformations
        activateTransformations();
        assertEquals(quoted_printable_trans, mimeParser.parse(quoted_printable));
    }

    @Test
    public void noSubjectTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(no_subject, mimeParser.parse(no_subject));

        // With transformations
        activateTransformations();
        assertEquals(no_subject_trans, mimeParser.parse(no_subject));
    }

    @Test
    public void subjectInBodyTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(subject_in_body, mimeParser.parse(subject_in_body));

        // With transformations
        activateTransformations();
        assertEquals(subject_in_body_trans, mimeParser.parse(subject_in_body));
    }

    @Test
    public void multilineSubjectMailTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(multiline_subject_mail, mimeParser.parse(multiline_subject_mail));

        // With transformations
        activateTransformations();
        assertEquals(multiline_subject_mail_trans, mimeParser.parse(multiline_subject_mail));
    }

    @Test
    public void mimeTest() {
        // Without transformations
        deactivateTransformations();
        assertEquals(mime, mimeParser.parse(mime));
        // With transformations
        activateTransformations();
        assertEquals(mime_trans, mimeParser.parse(mime));
    }

    private void deactivateTransformations() {
        ProxyConfiguration.INSTANCE.desactivateSubjectTransformation();
        ProxyConfiguration.INSTANCE.desactivateImageTransformation();
        when(connection.getImageTransformation()).thenReturn(ProxyConfiguration.INSTANCE.imageTransformation());
        when(connection.getSubjectTransformation()).thenReturn(ProxyConfiguration.INSTANCE.subjectTransformation());
    }

    private void activateTransformations() {
        ProxyConfiguration.INSTANCE.setLeetSubjectTransformation();
        ProxyConfiguration.INSTANCE.setImageRotationTransformation();
        when(connection.getImageTransformation()).thenReturn(ProxyConfiguration.INSTANCE.imageTransformation());
        when(connection.getSubjectTransformation()).thenReturn(ProxyConfiguration.INSTANCE.subjectTransformation());
    }

    private static String readFile(String path)
            throws IOException
    {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
}
