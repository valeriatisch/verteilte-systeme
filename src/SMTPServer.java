import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

/**
 * @author Moritz Luca Bostelmann
 * @author Samy Abdellah Hamdad
 * @author Alaa Mlaouhi
 * @author Valeria Tisch
 */

public class SMTPServer {

    public static final int DEFAULT_BUFFER_SIZE = 1234;

    private ByteBuffer buffy = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
    private static Charset charset = StandardCharsets.US_ASCII;
    // https://www.knownhost.com/wiki/email/troubleshooting/error-numbers
    private static byte [] readyResp    = new String("220 The SMTP server is ready to proceed.").getBytes(charset);
    private static byte [] ackResp      = new String("250 Fantastic! Your email message was delivered, as expected.").getBytes(charset);
    private static byte [] inputResp    = new String("354 The \"From\" and \"To\" information has been received").getBytes(charset);
    private static byte [] unrecResp    = new String("500 The SMTP server was unable to correct process the command(s) received. This is probably due to a syntax error").getBytes(charset);
    private static byte [] helpResp     = new String("214 Help message received. Our SMTP server supports the following commands:").getBytes(charset);
    private static byte [] closingResp  = new String("221 The connection to the mail server is now ending.").getBytes(charset);

    private final int port;
    private ServerSocketChannel ssc;
    private Selector selector;

    public SMTPServer(int port) throws Exception {
        this.port = port;
        // Initializing ...
        // Create a new non-blocking server socket channel
        this.ssc = ServerSocketChannel.open();
        this.ssc.configureBlocking(false);
        // Bind the server socket to the specified port
        this.ssc.socket().bind(new InetSocketAddress(port));
        // Create a selector
        this.selector = Selector.open();
        // Register the server socket channel, indicating an interest in accepting new connections
        this.ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    private static void writeToFile(byte [] msg, String sender, String receiver) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "/mails/" + receiver);
        if (!Files.exists(path)) {
            System.out.println("Creating directory ..");
            try {
                Files.createDirectories(path);
                System.out.println("Directory created!");
            } catch ( IOException e) {
                System.out.println("Error when creating directory" + e.getMessage());
            }
        }
        String file = path + "/" + sender + randomId() + ".txt";
        Path filePath = Paths.get(file);
        Files.write(filePath, msg);
    }

    private static void writeToChannel(byte [] msg, SelectionKey key) throws IOException {
        // Write a msg into a key's Channel
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        buffer.put(msg);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    private void readMessage(SelectionKey key) {
        //TODO
    }

    private static int randomId() {
        return (int) (Math.random() * 9999);
    }

    private void handleAccept(SelectionKey key) {
        ServerSocketChannel sock = (ServerSocketChannel) key.channel();
        SocketChannel client = sock.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ |
                SelectionKey.OP_WRITE);
        //TODO
        // Create a new socket
        // Accept the connection request
        // Non-blocking
        // Register
        // Attach E-Mail
        // Send Service Ready
    }

    private void handleRead(SelectionKey key) {
        //TODO
        // Received data into buffer
        /*
        Received e-mails are to be stored efficiently in a file following the naming convention <sender>_<message_id>
        under the directory <receiver>, where <sender> corresponds to the e-mail address of the sender and <receiver>
        to the e-mail address of the recipient. The values for <sender> and <receiver> can be extracted from the commands
        MAIL FROM and RCPT TO.
        Use randomId() for <message_id>.
         */
        // Handle HELO, MAIL FROM, RCPT TO, DATA, HELP and, QUIT
    }

    public void startServer() {
        try {
            System.out.println("Server is starting on port " + port);

            Iterator<SelectionKey> iter;
            SelectionKey key;
            while(ssc.isOpen()) {
                // Block until at least one channel has been selected
                if (selector.select() == 0) continue;
                // Get an iterator over the set of selected keys
                iter = selector.selectedKeys().iterator();
                // Look at each key in the selected set
                while(iter.hasNext()) {
                    // Get current key
                    key = iter.next();
                    // Is a new connection coming in?
                    if(key.isAcceptable()) handleAccept(key);
                    // Is there data to read on this channel?
                    if(key.isReadable()) handleRead(key);
                    // // Remove key from selected set; it's been handled
                    iter.remove();
                }
            }
        } catch (IOException e) {
            System.out.println("Server is terminating.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        final int port = 1234;
        new SMTPServer(port).startServer();
    }
}
