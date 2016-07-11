package ar.edu.itba.pdc.transformations;

public enum NoneTransformation implements Transformation {
    INSTANCE;

    @Override
    public String transform(final String input, final String type) {
        return input;
    }

    @Override
    public String toString() {
        return "None Transformation";
    }

}
