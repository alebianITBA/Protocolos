package ar.edu.itba.pdc.transformations;

public interface Transformation {
    /**
     * @param parameter optional parameter for transformations that need one
     */
    public String transform(final String input, final String parameter);
    
}
