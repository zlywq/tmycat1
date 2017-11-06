package g1.cmn;


public class MyBaseException extends RuntimeException {

    public boolean forTest;
    public int code;

    public MyBaseException(String message,int code,boolean forTest) {
        super(message);
        this.code = code;
        this.forTest = forTest;
    }

    public MyBaseException() {
        super();
    }
    public MyBaseException(String message) {
        super(message);
    }
    public MyBaseException(String message, Throwable cause) {
        super(message, cause);
    }
    public MyBaseException(Throwable cause) {
        super(cause);
    }
    protected MyBaseException(String message, Throwable cause,boolean enableSuppression,boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
