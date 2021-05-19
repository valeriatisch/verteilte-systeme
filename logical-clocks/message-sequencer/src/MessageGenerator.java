import java.util.LinkedList;

/**
 * TODO:
 *
 * External Messages: can be seen as messages from a client.
 *      - random integer value as payload
 *      - are sent to the inbox-queue of only one thread
 *      Once an external message is received, the message’s content must be
 *      broadcasted to the other threads over the message sequencer.
 */
public class MessageGenerator extends Thread {

    private LinkedList<String> queue;
    private volatile boolean running = true;
    private int numMsg = 0;
    private final String filePath = "";
    private MessageGenerator[] otherGenerators;
    public int id;

    public MessageGenerator(int id) {
        this.id = id;
        queue = new LinkedList<String>();
    }

    public void setGenerators(MessageGenerator[] generators) {
        this.otherGenerators = generators;
    }

    public void terminate() {
        // tell thread to terminate
        this.running = false;
    }

    private boolean checkForMessage() {
        // check if a new message arrived in queue
        return numMsg == queue.size();
    }

    private void writeToFile() {
        // write messages in queue to log file
    }

    public void receiveMsg(String msg) {
        queue.add(msg);
    }

    private void handleMsg() {
        // handle external/internal messages accordingly
    }

    public void run() {
        while(running) {
            if(checkForMessage()) {

            }
        }
        writeToFile();
    }
}
