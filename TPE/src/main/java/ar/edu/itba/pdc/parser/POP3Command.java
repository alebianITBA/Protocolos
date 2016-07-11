package ar.edu.itba.pdc.parser;

public enum POP3Command {
    USER("+OK User accepted.\r\n", "-ERR User not found.\r\n"),
    PASS("+OK Logged in.\r\n", "-ERR Incorrect password.\r\n"),
    LIST("+OK\r\n", "-ERR\r\n"),
    RETR("+OK\r\n", "-ERR .\r\n"),
    DELE("+OK msg deleted.\r\n", "-ERR couldnt'delete msg.\r\n"),
    APOP("+OK .\r\n", "-ERR .\r\n"),
    TOP("+OK .\r\n", "-ERR .\r\n"),
    UIDL("+OK .\r\n", "-ERR .\r\n"),
    QUIT("+OK logging out.\r\n", "-ERR couldn't log out.\r\n"),
    STAT("+OK .\r\n", "-ERR .\r\n"),
    NOOP("+OK .\r\n", "-ERR .\r\n"),
    RSET("+OK .\r\n", "-ERR .\r\n"),
    CAPA("+OK Capability list follows.\r\n", "-ERR ."),
    // These are not commands
    CONNECTION("+OK POP3 ready.\r\n", "-ERR connection error.\r\n"),
    UNKNOWN("+OK .\r\n", "-ERR invalid command.\r\n");

    private final String success;
    private final String error;
    public static final String CRLF = ".\r\n";

    POP3Command(final String success, final String error) {
        this.success = success;
        this.error = error;
    }

    public String success() {
        return success;
    }

    public String error() {
        return error;
    }
}
