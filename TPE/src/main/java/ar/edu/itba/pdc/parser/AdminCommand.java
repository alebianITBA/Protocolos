package ar.edu.itba.pdc.parser;

public enum AdminCommand {
    AUTH(            "+OK admin logged.\r\n",          "-ERR authentication failed.\r\n"),
    STOP(            "+OK Stopping server.\r\n",       "-ERR couldn't stop server.\r\n"),
    QUIT(            "+OK Goodbye.\r\n",               "-ERR couldn't close connection.\r\n"),
    STATS(           "+OK Collected stats:\r\n",       "-ERR can't show stats.\r\n"),
    LISTUSERS(       "+OK Users list:\r\n",            "-ERR couldn't show users list.\r\n"),
    SETSERVER(       "+OK user mapped to server.\r\n", "-ERR couldn't map user to server.\r\n"),
    REMOVESERVER(    "+OK user removed from map.\r\n", "-ERR couldn't remove user from map.\r\n"),
    LEET(            "+OK LEET\r\n",                   "-ERR couldn’t set LEET mode.\r\n"),
    ROTATION(        "+OK IMAGE ROTATION\r\n",         "-ERR couldn’t set IMAGE ROTATION mode.\r\n"),
    // This are not commands
    CONNECTED(       "+OK Authenticate now.\r\n",      "-ERR couldn't connect.\r\n"),
    UNKNOWN(         "+OK ?.\r\n",                     "-ERR Unknown command.\r\n"),
    AUTH_NEEDED(     "+OK ?.\r\n",                     "-ERR authentication needed.\n"),
    WRONG_PARAMETERS("+OK ?.\r\n",                     "-ERR incorrect parameters.\r\n");

    private final String success;
    private final String error;

    AdminCommand(final String success, final String error) {
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
