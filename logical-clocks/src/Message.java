
public class Message implements Comparable<Message>{

    private boolean type;
    private int payload;
    private int threadId;
    private int lamportCounter = -1;

    public Message(int payload, boolean type) {
        this.payload = payload;
        this.type = type;
    }

    public Message(int payload, boolean type, int lamportCounter) {
        this.payload = payload;
        this.type = type;
        this.lamportCounter = lamportCounter;
    }

    public int getThreadId() {return this.threadId; }
    public void setThreadId(int threadId) { this.threadId = threadId;}


    public Message(Message message){
        this(message.payload, message.type);
        this.setThreadId(message.getThreadId());
        this.setLamportCounter(message.getLamportCounter());
    }

    public int getLamportCounter() { return this.lamportCounter; }
    public void setLamportCounter(int lamportCounter) { this.lamportCounter = lamportCounter; }

    public int getPayload() { return this.payload; }
    public void setPayload(int payload) { this.payload = payload; }

    public boolean isInternal() { return this.type; }
    public void setType(boolean type) { this.type = type; }

    @Override
    public int compareTo(Message msg) {
        if (this == msg) return 0;
        if(this.getLamportCounter() == msg.getLamportCounter()) {
            return Integer.compare(this.getThreadId(), msg.getThreadId());
        } else if(this.getLamportCounter() > msg.getLamportCounter())
            return 1;
        else
            return -1;
    }
}
