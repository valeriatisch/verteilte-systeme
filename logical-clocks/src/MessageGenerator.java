import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private int numMsg = 0; // index of the last handled message in the queue
    private final MessageSequencer sequencer;
    private final boolean usingSequencer; // true for sequencer false for lamport
    private int lamportCounter = 0;

    protected MessageGenerator[] generators; // needs to know the other threads for use with lamport
    public int id; // id to identify thread (same as index in array)

    // constructor for use with message sequencer
    public MessageGenerator(int id, MessageSequencer sequencer) {
        this.id = id;
        this.sequencer = sequencer;
        this.queue = new ArrayList<>();
        this.usingSequencer = true;
    }

    // constructor for use with lamport
    public MessageGenerator(int id) {
        this.id = id;
        this.queue = new ArrayList<>();
        this.sequencer = new MessageSequencer(null);
        this.usingSequencer = false;
    }

    public void setGenerators(MessageGenerator[] generators) {
        this.generators = generators;
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
        System.out.println("Thread " + this.id + " writing logs");
        Path path;
        if (this.usingSequencer)
            path = Paths.get(dirPath + "/sequencer");
        else
            path = Paths.get(dirPath + "/lamport");
        // create directory
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
        synchronized (this) {
            if (this.usingSequencer) {
                for (Message message : this.queue) {
                    if (message.isInternal()) {
                        result += "" + message.getPayload() + "\n";
                    }
                }
            } else {
                PriorityQueue<Message> lamportQueue = new PriorityQueue<>();
                // sort message depending on their lamportCounter
                for (Message message : this.queue) {
                    if (message.isInternal()) {
                        lamportQueue.add(message);
                    }
                }
                Message msg = lamportQueue.poll();
                while (msg != null) {
                    result += "" + msg.getPayload() + "\n";
                    msg = lamportQueue.poll();
                }
            }
        }
        Files.writeString(filePath, result, charset);
    }

    public synchronized void receiveMsg(Message msg) {
        this.queue.add(msg);
    }

    // use with message sequencer
    private void handleMsg() {
        // handle external/internal messages accordingly
        Message msg = new Message(this.queue.get(this.numMsg));
        if (!msg.isInternal()) {
            Message response = new Message(msg);
            response.setType(true);
            response.setThreadId(this.id);
            synchronized (this.sequencer) {
                this.sequencer.receiveMsg(response);
                this.sequencer.notify();
            }
        }
        this.numMsg++;
    }

    // use with lamport
    private void handleMsgLamport() {
        // broadcast internal messages accordingly
        Message msg = this.queue.get(this.numMsg);
        // broadcast external messages
        if (!msg.isInternal()) {
            this.lamportCounter++;
            msg.setLamportCounter(this.lamportCounter);
            msg.setThreadId(this.id);
            Message msgToSend = new Message(msg);
            msgToSend.setType(true);
            for (int i = 0; i < this.generators.length; i++) {
                synchronized (this.generators[i]) {
                    this.generators[i].receiveMsg(msgToSend);
                    // no need to notify self
                    if( this.id != i)
                        this.generators[i].notify();
                }
            }
        } else {
            this.lamportCounter = Math.max(this.lamportCounter, msg.getLamportCounter()) + 1;
            msg.setLamportCounter(this.lamportCounter);
        }
        this.numMsg++;
    }

    @Override
    public void run() {
        while(this.running) {
            // wait to be notified by a sender
            try{
                synchronized (this) {
                    this.wait();
                }
            } catch(InterruptedException ex){
                break;
            }
            while (this.checkForMessage()) {
                if(this.usingSequencer)
                    this.handleMsg();
                else
                    this.handleMsgLamport();
            }
        }
    }
}
