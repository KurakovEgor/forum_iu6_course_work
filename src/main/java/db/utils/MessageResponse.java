package db.utils;

public class MessageResponse {
    private final String message;

    public MessageResponse(String description) {
        this.message = description;
    }

    public String getMessage() {
        return message;
    }
}
