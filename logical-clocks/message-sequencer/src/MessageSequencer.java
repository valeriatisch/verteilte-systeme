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

    public void run() {
    }

}
