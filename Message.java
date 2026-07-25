package common;

public class Message {

    // Message Types
    public static final String REQUEST = "REQUEST";
    public static final String TOKEN = "TOKEN";
    public static final String RELEASE = "RELEASE";
    public static final String MARKER = "MARKER";
    public static final String SNAPSHOT = "SNAPSHOT";
    public static final String ACK = "ACK";

    private String type;
    private int senderId;
    private String data;

    // Constructor without data
    public Message(String type, int senderId) {
        this.type = type;
        this.senderId = senderId;
        this.data = "";
    }

    // Constructor with data
    public Message(String type, int senderId, String data) {
        this.type = type;
        this.senderId = senderId;
        this.data = data;
    }

    // Getters
    public String getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public String getData() {
        return data;
    }

    // Convert Message object into String
    @Override
    public String toString() {
        return type + "|" + senderId + "|" + data;
    }

    // Convert received String into Message object
    public static Message fromString(String message) {

        String[] parts = message.split("\\|", 3);

        String type = parts[0];
        int senderId = Integer.parseInt(parts[1]);

        String data = "";

        if (parts.length == 3) {
            data = parts[2];
        }

        return new Message(type, senderId, data);
    }
}