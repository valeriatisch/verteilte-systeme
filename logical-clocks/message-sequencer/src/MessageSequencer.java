import java.util.LinkedList;

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

class MessageSequencer extends Thread {

    private LinkedList<String> queue;
    private volatile boolean running = true;
    private int numMsg = 0;
    private final String filePath = "";
    
    public MessageSequencer() {
        queue = new LinkedList<String>();
    }

    public void terminate() {
        // tell thread to terminate
        this.running = false;
    }

    private boolean checkForMessage() {
        // check if a new message arrived in queue
        return numMsg == queue.size();
    }

    public void receiveMsg(String msg) {
        queue.add(msg);
    }

    private void handleMsg() {
        // broadcast internal messages accordingly
    }

    public void run() {
        while(running) {
            if(checkForMessage()) {

            }
        }
    }
}

