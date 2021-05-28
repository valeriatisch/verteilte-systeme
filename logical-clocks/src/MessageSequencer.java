import java.util.ArrayList;

/**
 * TODO:
 *
 * The message sequencer acts as a broadcasting service.
 *
 * It is the responsibility of the message sequencer to
 * forward each received internal message to every thread
 * in the system; including the sender-thread.
 *
 * Because there exists only one message sequencer in the
 * entire system, all internal messages should be received
 * in the same order at all threads. Note that the message
 * sequencer never receives external messages.
 *
 */

class MessageSequencer extends Thread{
    private static final String dirPath = System.getProperty("user.dir") + "/logFiles";

    protected ArrayList<Message> queue;
    private MessageGenerator[] generators;
    private volatile boolean running = true;
    private int numMsg = 0;


    public MessageSequencer(MessageGenerator[] generators) {
        this.queue = new ArrayList<>();
        this.generators = generators;
    }


    public void terminate() {
        // tell thread to terminate
        this.running = false;
    }

    private boolean checkForMessage() {
        // check if a new message arrived in queue
        return this.numMsg < this.queue.size();
    }

    public void receiveMsg(Message msg) {
        this.queue.add(msg);
    }

    private void handleMsg() {
        // broadcast internal messages accordingly
        Message msg = new Message(this.queue.get(this.numMsg));
        msg.setType(true);
        for (int i = 0; i < this.generators.length; i++) {
            synchronized (this.generators[i]) {
                this.generators[i].receiveMsg(msg);
                this.generators[i].notify();
            }
        }
        this.numMsg++;
    }

    @Override
    public void  run() {
        while(this.running) {
            // wait to be notified by a sender
            try{
                synchronized (this) {
                    this.wait();
                }
            } catch(InterruptedException ex){
                break;
            }
            while (this.checkForMessage()) { this.handleMsg(); }
        }
    }
}

