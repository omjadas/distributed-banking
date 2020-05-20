/**
 * Thrown when an operation is requested on an unknown account.
 */
public class UnknownAccountException extends Exception {
    private static final long serialVersionUID = -1270599957289073750L;

    /**
     * Create an instance of UnknownAccountException.
     *
     * @param message message of the exception
     */
    public UnknownAccountException(String message) {
        super(message);
    }
}
