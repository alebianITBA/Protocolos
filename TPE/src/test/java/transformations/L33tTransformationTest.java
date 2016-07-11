package transformations;

import ar.edu.itba.pdc.transformations.L33tTransformation;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class L33tTransformationTest {

    private static final String subject_1 = "hola bar\r\n";
    private static final String subject_1_trans = "h0l4 b4r\r\n";
    private static final String subject_2 = "hola bar! (parte 2)\r\n";
    private static final String subject_2_trans = "h0l4 b4r! (p4rt3 2)\r\n";
    private static final String subject_3 = "hola mundo!\r\n";
    private static final String subject_3_trans = "h0l4 mund0!\r\n";
    private static final String subject_4 = "=?UTF-8?B?4p2BIGHDoWXDqWnDrW/Ds3XDug==?=\r\n";
    private static final String subject_4_trans = "=?UTF-8?B?4p2BIDTDoTPDqTHDrTDDs3XDug==?=\r\n";
    private static final String subject_5 = "=?ISO-8859-1?Q?a=61e=65i=69o=6f=75u?=\r\n";
    private static final String subject_5_trans = "=?ISO-8859-1?Q?4=E13=E91=ED0=F3u=FA?=";
    private static final String subject_6 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.\r\n";
    private static final String subject_6_trans = "L0r3m 1psum d0l0r s1t 4m3t, <0ns3<t3tur 4d1p1s1<1ng 3l1t, s3d d0 31usm0d t3mp0r 1n<1d1dunt ut l4b0r3 3t d0l0r3 m4gn4 4l1qu4.\r\n";
    private static final String subject_7 = "hola\r\n\r\naeiou\r\nLala: lala\r\n\r\n\r\n.\r\n";
    private static final String subject_7_trans = "h0l4\r\n\r\n4310u\r\nL4l4: l4l4\r\n\r\n\r\n.\r\n";

    private static final L33tTransformation TRANSFORMATION = L33tTransformation.INSTANCE;

    @Test
    public void transformTest() {
        assertEquals(subject_1_trans, TRANSFORMATION.transform(subject_1, null));
        assertEquals(subject_2_trans, TRANSFORMATION.transform(subject_2, null));
        assertEquals(subject_3_trans, TRANSFORMATION.transform(subject_3, null));
        //assertEquals(subject_4_trans, TRANSFORMATION.transform(subject_4, null));
        //assertEquals(subject_5_trans, TRANSFORMATION.transform(subject_5, null));
        assertEquals(subject_6_trans, TRANSFORMATION.transform(subject_6, null));
        assertEquals(subject_7_trans, TRANSFORMATION.transform(subject_7, null));
    }

}
