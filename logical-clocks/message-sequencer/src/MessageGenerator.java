import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private static final Charset charset = StandardCharsets.US_ASCII;
    private static final String dirPath = System.getProperty("user.dir") + "/logFiles";

    private final ArrayList<Message> queue; // queue for incoming messages, maybe add a second list to store all messages for logging?
    private volatile boolean running = true;
    private int numMsg = 0;
    private final MessageSequencer sequencer;

    protected MessageGenerator[] otherGenerators; // needs to know the other threads
    public int id; // id to identify thread (same as index in array)

    public MessageGenerator(int id, MessageSequencer sequencer) {
        this.id = id;
        this.sequencer = sequencer;
        this.queue = new ArrayList<>();
    }

    public void setGenerators(MessageGenerator[] generators) {
        this.otherGenerators = generators;
    }

    public void terminate() {
        // tell thread to terminate
        this.running = false;
        try {
            this.writeToFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean checkForMessage() {
        // check if a new message arrived in queue
        return this.numMsg < this.queue.size();
    }

    private void writeToFile() throws IOException{
        // write messages in queue to log file
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            System.out.println("Creating directory ..");
            try {
                Files.createDirectories(path);
                System.out.println("Directory created!");
            } catch (IOException e) {
                System.out.println("Error when creating directory" + e.getMessage());
            }
        }
        String file = path + "/" + this.id + ".txt";
        Path filePath = Paths.get(file);
        String result = "";
        System.out.println("Thread queue size: " + this.queue.size());
        for (Message message: this.queue) {
//            System.out.println(message.getPayload());
//            System.out.println(message.isInternal());
            if (message.isInternal()) {
                result += "" + message.getPayload() + "\n";
            }
        }
        Files.writeString(filePath, result, charset);
    }

    public void receiveMsg(Message msg) {
        this.queue.add(msg);
    }

    private void handleMsg() {
        // handle external/internal messages accordingly
        Message msg = new Message(this.queue.get(this.numMsg));
        if (!msg.isInternal()) {
            Message response = new Message(msg);
            response.setType(true);
            response.setThreadId(this.id);
//            System.out.println("response: " + response.isInternal());
            synchronized (this.sequencer) {
                this.sequencer.receiveMsg(response);
                this.sequencer.notify();
            }
        }
        this.numMsg++;
    }

    @Override
    public void run() {
        while(this.running) {
            try{
                synchronized (this) {
                    //don't allow two threads at the same time to access this function
                    this.wait();
                }
            } catch(InterruptedException ex){
                break;
            }
            while (this.queue.size() != 0) {
                if (this.checkForMessage()) {
                    this.handleMsg();
                }
            }
        }
    }
}
