package messaging;

public class Message {
    private final String senderId;
    private final String type;
    private final Object content;
    
    public Message(String senderId, String type, Object content) {
        this.senderId = senderId;
        this.type = type;
        this.content = content;
    }
    
    public String getSenderId() { return senderId; }
    public String getType() { return type; }
    public Object getContent() { return content; }
    
    @Override
    public String toString() {
        return String.format("[%s → %s]", type, content);
    }
}
