package protocol.status;

public enum ResponseStatus {
    OK,
    ALREADY_SUBSCRIBED,
    NOT_SUBSCRIBED,
    SERVER_UNAVAILABLE,
    INTERNAL_ERROR,
    NO_MESSAGES,
    PROXY_DOES_NOT_KNOW_ANY_SERVER,
    TRANSFER_TOPIC_ERROR
}
