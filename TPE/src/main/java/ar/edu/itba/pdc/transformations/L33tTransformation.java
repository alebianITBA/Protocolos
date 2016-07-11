package ar.edu.itba.pdc.transformations;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.StringDecoder;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.codec.net.BCodec;
import org.apache.commons.codec.net.QCodec;

public enum L33tTransformation implements Transformation{
    INSTANCE;

    private String textToL33t(final String subject){
        StringBuffer transformBuffer = new StringBuffer();
        for(char c : subject.toCharArray()){
            transformBuffer.append(translate(c));
        }
        return transformBuffer.toString();
    }

    @Override
    public String transform(final String subject, final String type) {
        if(subject.length() > 1) {
            if(subject.substring(0, 2).equals("=?")) {
                return transformEncodedWord(subject);
            } else {
                return textToL33t(subject);
            }
        } else {
            return subject;
        }
    }

    private String transformEncodedWord(String subject) {
        String[] values = subject.split("\\?");
        StringDecoder stringDecoder;
        StringEncoder stringEncoder;
        if(values[2].charAt(0) == 'B') {
            BCodec bCodec = new BCodec(values[1]);
            stringDecoder = bCodec;
            stringEncoder = bCodec;
        } else if(values[2].charAt(0) == 'Q') {
            QCodec qCodec  = new QCodec(values[1]);
            stringDecoder = qCodec;
            stringEncoder = qCodec;
        } else {
            return null;
        }
        try {
            return stringEncoder.encode(textToL33t(stringDecoder.decode(subject)));
        } catch (EncoderException | DecoderException e) {
            return subject;
        }
    }

    public String toString() {
        return "LEET (1337) Transformation";
    }

    public char translate(final char c) {
        switch (c) {
            case 'a': return '4';
            case 'e': return '3';
            case 'i': return '1';
            case 'o': return '0';
            case 'c': return '<';
            default: return c;
        }
    }
}
