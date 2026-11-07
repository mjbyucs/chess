package client;

public class ResponseException extends Exception {
    public enum Code {
        ClientError,
        ServerError
    }

    final private Code code;

    public ResponseException(int status, String message) {
        this(fromHttpStatusCode(status), message);
    }
    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code code() {
        return code;
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode / 100) {
            case 5 -> Code.ServerError;
            case 4 -> Code.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }

}
