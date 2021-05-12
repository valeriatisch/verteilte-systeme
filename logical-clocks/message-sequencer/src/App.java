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
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                int NrThreads = Integer.parseInt(args[0]);
                int NrMsg = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Arguments must be integers.");
                System.exit(1);
            }
        } else {
            System.out.println("This program expects two arguments: 1. Number of Threads & 2. Number of messages to send");
        }
    }

    MessageSequencer ms = new MessageSequencer();
}