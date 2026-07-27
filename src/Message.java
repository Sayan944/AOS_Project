import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private MessageType type;

    private int senderId;

    private int receiverId;

    private int snapshotId;

    private String payload;

    public Message(MessageType type,
                   int senderId,
                   int receiverId,
                   String payload) {

        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.payload = payload;
        this.snapshotId = -1;
    }

    public Message(MessageType type,
                   int senderId,
                   int receiverId,
                   int snapshotId,
                   String payload) {

        this.type = type;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.snapshotId = snapshotId;
        this.payload = payload;
    }

    // -----------------------------
    // Getters
    // -----------------------------

    public MessageType getType() {
        return type;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public int getSnapshotId() {
        return snapshotId;
    }

    public String getPayload() {
        return payload;
    }

    /*
        setter methods 
    */

    public void setPayload(String payload) {
        this.payload = payload;
    }

   /*
   console display
   */

    @Override
    public String toString() {

        return "\n=============================="
                + "\nMessage Type : " + type
                + "\nFrom         : University " + senderId
                + "\nTo           : University " + receiverId
                + "\nSnapshot ID  : " + snapshotId
                + "\nPayload      : " + payload
                + "\n==============================";
    }
}