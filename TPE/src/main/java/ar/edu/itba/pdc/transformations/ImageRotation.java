package ar.edu.itba.pdc.transformations;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public enum ImageRotation implements Transformation{
    INSTANCE;

    private static final Logger LOGGER = Logger.getLogger(ImageRotation.class);

    @Override
    public String transform(final String image, final String type){
        LOGGER.info("Rotating image");
        byte[] bytesImage = Base64.decodeBase64(image.getBytes());
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytesImage);

        ByteArrayOutputStream outputStream;
        try {
            final BufferedImage bufferedImageInput = ImageIO.read(inputStream);
            BufferedImage bufferedImageOutput = rotateImage(bufferedImageInput);
            outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImageOutput, type, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return image;
        }

        byte[] bytesImageRotated = outputStream.toByteArray();
        return new String(Base64.encodeBase64(bytesImageRotated));

    }

    private static BufferedImage rotateImage(final BufferedImage image) {
        final AffineTransform at = new AffineTransform();
        at.rotate(Math.PI, image.getWidth()/2, image.getHeight()/2);
        final AffineTransformOp atOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        return atOp.filter(image, null);
    }
}
