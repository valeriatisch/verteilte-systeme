import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
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
    private static byte [] readyResp    = "220 The SMTP server is ready to proceed.".getBytes(charset);
    private static byte [] ackResp      = "250 Fantastic! Your email message was delivered, as expected.".getBytes(charset);
    private static byte [] inputResp    = "354 The \"From\" and \"To\" information has been received".getBytes(charset);
    private static byte [] unrecResp    = "500 The SMTP server was unable to correct process the command(s) received. This is probably due to a syntax error".getBytes(charset);
    private static byte [] helpResp     = "214 Help message received. Our SMTP server supports the following commands:".getBytes(charset);
    private static byte [] closingResp  = "221 The connection to the mail server is now ending.".getBytes(charset);
    private static byte [] invalidResp  = "503 Bad sequence of commands.".getBytes(charset);
    private static byte [] syntaxResp   = "501 Syntax error in parameters or arguments.".getBytes(charset);
    private static byte [] unknownResp  = "500 Syntax error, command unrecognized.".getBytes(charset);
    private static byte [] internalResp = "451 Requested action aborted: error in processing.".getBytes(charset);

    protected final static int serviceReady = 0;
    protected final static int help         = 1;
    protected final static int helo         = 2;
    protected final static int mailFrom     = 3;
    protected final static int rcptTo       = 4;
    protected final static int data         = 5;
    protected final static int msg          = 6;
    protected final static int quit         = 7;

    private final int port;
    private ServerSocketChannel ssc;
    private Selector selector;

    private static String [] supportedCmds = {"help", "helo", "mail from", "rcpt to", "quit", "data"};

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

    private static void writeToFile(ClientState client) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "/mails/" + client.getReceiver());
        if (!Files.exists(path)) {
            System.out.println("Creating directory ..");
            try {
                Files.createDirectories(path);
                System.out.println("Directory created!");
            } catch ( IOException e) {
                System.out.println("Error when creating directory" + e.getMessage());
            }
        }
        String file = path + "/" + client.getSender() + randomId() + ".txt";
        Path filePath = Paths.get(file);
        Files.writeString(filePath, client.getMessage(), charset);
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
        try {
            ServerSocketChannel sock = (ServerSocketChannel) key.channel();
            SocketChannel client = sock.accept();
            client.configureBlocking(false);
            SelectionKey chnnl = client.register(selector, SelectionKey.OP_READ |
                    SelectionKey.OP_WRITE);
            // Attach E-Mail
            ClientState clientMail = new ClientState();
            chnnl.attach(clientMail);
            // Send Service Ready
            writeToChannel(readyResp, chnnl);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private boolean commandExists(String message) {
        boolean exists = false;
        for(int i=0; i<supportedCmds.length; i++) {
            if(message.length() == 4){
                exists = message == supportedCmds[i];
                if (exists) break;
            }
            else {
                exists = message.indexOf(supportedCmds[i]) == 0;
                if (exists) break;
            }
        }
        return exists;
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
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            ClientState client = (ClientState) key.attachment();
            channel.read(buffer);
            buffer.flip();
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuf = decoder.decode(buffer);
            String message = charBuf.toString();
            // String message = new String(buffer.array(), charset);


            System.out.println(message);
            if (client.getState() != data && !commandExists(message.toLowerCase())) {
                writeToChannel(unknownResp, key);
                return;
            }
            // 'help' message can be send anytime
            if (message.toLowerCase() == "help" ) {
                writeToChannel(helpResp, key);
                return;
            }

            if (message.toLowerCase() == "helo" && client.getState() != serviceReady) {
                writeToChannel(syntaxResp, key);
                return;
            } 
            // check clients current state to determine which command is expected
            switch (client.getState()) {
                case serviceReady:
                    if (message.toLowerCase() != "helo") {
                        writeToChannel(invalidResp, key);
                        break;
                    }
                    writeToChannel(ackResp, key);
                    client.setState(helo);
                    break;

                case helo:
                    if (message.substring(0, 8).toLowerCase() != "mail from") {
                        writeToChannel(invalidResp, key);
                        break;
                    }
                    client.setSender(message.substring(9));
                    writeToChannel(ackResp, key);
                    client.setState(mailFrom);
                    break;

                case mailFrom:
                    if (message.substring(0, 6).toLowerCase() != "rcpt to") {
                        writeToChannel(invalidResp, key);
                        break;
                    }
                    client.setReceiver(message.substring(7));
                    writeToChannel(ackResp, key);
                    break;

                case rcptTo:
                    if (message.toLowerCase() != "data") {
                        writeToChannel(invalidResp, key);
                        break;
                    }
                    writeToChannel(inputResp, key);
                    client.setState(data);
                    break;

                case data:
                    if (message.indexOf("\r\n.\r\n") == message.length()-5) {
                        client.setState(msg);
                        writeToChannel(ackResp, key);
                    }
                    client.setMessage(client.getMessage() + message);
                    break;

                case msg:
                    if (message != "quit") {
                        writeToChannel(invalidResp, key);
                        break;
                    }
                    writeToChannel(closingResp, key);
                    client.setState(quit);
                    writeToFile(client);
                    channel.close();
                    break;

                default:
                    writeToChannel(internalResp, key);
                    break;
            }

        } catch (IOException e){
            try {
                writeToChannel(internalResp, key);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }        
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
                    if(key.isAcceptable()) {
                        System.out.println("Key is acceptable.");
                        handleAccept(key);
                        System.out.println("Key was accepted.");
                    }
                    // Is there data to read on this channel?
                    if(key.isReadable()) {
                        System.out.println("Key is readable.");
                        handleRead(key);
                        System.out.println("Key is read.");
                    } 
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
