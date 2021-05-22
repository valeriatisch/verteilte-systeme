public class Message {

    protected static final boolean INTERNAL = true;
    protected static final boolean EXTERNAL = false;

    private boolean type;
    private int payload;
    private int threadId;

    public Message(int payload, boolean type) {
        this.payload = payload;
        this.type = type;
    }

    public int getThreadId() {return this.threadId; }
    public void setThreadId(int threadId) { this.threadId = threadId;}


    public Message(Message message){
        this(message.payload, message.type);
        this.setThreadId(message.getThreadId());
    }

    public int getPayload() { return this.payload; }
    public void setPayload(int payload) { this.payload = payload; }

    public boolean isInternal() { return this.type; }
    public void setType(boolean type) { this.type = type; }
}
