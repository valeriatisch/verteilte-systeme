import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * TODO:
 * Multiple threads broadcast messages over a message sequencer.
 *
 * Each thread maintains an own queue which can be seen as inbox for new messages.
 * One thread continuously polls the inbox-queue for new messages.
 *
 * If a message is read, the next steps are evaluated based on the message type:
 * External Messages: can be seen as messages from a client.
 *      - random integer value as payload
 *      - are sent to the inbox-queue of only one thread
 *      Once an external message is received, the message’s content must be
 *      broadcasted to the other threads over the message sequencer.
 * Internal Messages: are sent from one thread to the inbox-queue of the message sequencer
 * or from the message sequencer to the inbox-queue of one thread.
 *      - additional information (thread-id, counters etc.).
 *
 * Requirements:
 * Each thread stores a history (e.g. an array) of received internal messages.
 *
 * When the program terminates, all threads must be stopped and the threads
 * write their history to a thread-specific log file on the file system.
 *
 * The message sequencer runs in an additional thread.
 *
 * Simulate clients by generating a customizable number of external messages
 * which are sent to random threads (except the message sequencer).
 */

public class App {

    
    public static void main(String[] args) throws InterruptedException {
        int nrThreads = 0;
        int nrMsg = 0;
        int counter = 0;
        if (args.length == 2) {
            try {
                nrThreads = Integer.parseInt(args[0]);
                nrMsg = Integer.parseInt(args[1]);
                
            } catch (NumberFormatException e) {
                System.err.println("Arguments must be integers.");
                System.exit(1);
            }
            
        } else {
            System.out.println("This program expects two arguments: 1. Number of Threads & 2. Number of messages to send");
            System.exit(1);
        }

        MessageGenerator[] threads = new MessageGenerator[nrThreads];
        MessageSequencer ms = new MessageSequencer(threads);
        for (int i=0; i<nrThreads; i++) {
//            System.out.println(i);
            threads[i] = new MessageGenerator(i, ms);
        }

        for (int i=0; i<nrThreads; i++) {
            threads[i].start();
        }
        ms.start();

        while (counter < nrMsg) {
            int randomThread = new Random().nextInt(nrThreads);
            int randomPayload = (int) (Math.random() * Integer.MAX_VALUE);
//            System.out.println("payload: " + counter);
            Message msg = new Message(randomPayload, false);
            synchronized (threads[randomThread]) {
                threads[randomThread].receiveMsg(msg);
                threads[randomThread].notify();
            }
            counter++;
        }
        TimeUnit.MILLISECONDS.sleep(20000);

        for (int i=0; i<nrThreads; i++) {
            threads[i].terminate();
            threads[i].interrupt();
        }
        ms.terminate();
        ms.interrupt();
        System.exit(0);
    }
    
}
