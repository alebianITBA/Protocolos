package ar.edu.itba.pdc.mail;

public interface Parser {

    void reset();

    String parse(String toParse);

    boolean ended();

}
