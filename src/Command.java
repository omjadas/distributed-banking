/**
 * Commands that are sent between banks.
 */
public enum Command {
    REGISTER,
    REGISTER_RESPONSE,
    DEPOSIT,
    WITHDRAW,
    GET_BALANCE,
    GET_BALANCE_RESPONSE,
    TAKE_SNAPSHOT,
    ACKNOWLEDGEMENT,
    DUMMY,
    SNAPSHOT,
    WHITE_MESSAGE,
    CHANDY_LAMPORT_MARKER,
    CHANDY_LAMPORT_RESET
}
