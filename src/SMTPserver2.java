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
import java.util.Locale;

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
    private static byte[] readyResp = "220 The SMTP server is ready to proceed.\r\n".getBytes(charset);
    private static byte[] quitResp = "221 quitting process.\r\n".getBytes(charset);
    private static byte[] ackResp = "250 Fantastic! Your email message was delivered, as expected.\r\n".getBytes(charset);
    private static byte[] inputResp = "354 The \"From\" and \"To\" information has been received.\r\n".getBytes(charset);
    private static byte[] unrecResp = "500 The SMTP server was unable to correct process the command(s) received. This is probably due to a syntax error.\r\n".getBytes(charset);
    private static byte[] helpResp = "214 Help message received. Our SMTP server supports the following commands:\nHELP\nHELO\nMAIL FROM: [address]\nRCPT TO: [address]\nDATA\nQUIT\r\n".getBytes(charset);
    private static byte[] closingResp = "221 The connection to the mail server is now ending.\r\n".getBytes(charset);
    private static byte[] invalidResp = "503 Bad sequence of commands.\r\n".getBytes(charset);
    private static byte[] syntaxResp = "501 Syntax error in parameters or arguments.\r\n".getBytes(charset);
    private static byte[] unknownResp = "500 Syntax error, command unrecognized.\r\n".getBytes(charset);
    private static byte[] internalResp = "451 Requested action aborted: error in processing.\r\n".getBytes(charset);

    protected final static int serviceReady = 0;
    protected final static int help = 1;
    protected final static int helo = 2;
    protected final static int mailFrom = 3;
    protected final static int rcptTo = 4;
    protected final static int data = 5;
    protected final static int msg = 6;
    protected final static int quit = 7;


    private final int port;
    private ServerSocketChannel ssc;
    private Selector selector;

    private static String[] supportedCmds = { "helo", "help\r\n", "mail from", "rcpt to", "quit\r\n", "data\r\n" };

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
        // Register the server socket channel, indicating an interest in accepting new
        // connections
        this.ssc.register(selector, SelectionKey.OP_ACCEPT);
    }

    private static void writeToFile(ClientState client) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "/mails/" + client.getReceiver());
        if (!Files.exists(path)) {
            System.out.println("Creating directory ..");
            try {
                Files.createDirectories(path);
                System.out.println("Directory created!");
            } catch (IOException e) {
                System.out.println("Error when creating directory" + e.getMessage());
            }
        }
        String file = path + "/" + client.getSender() + randomId() + ".txt";
        Path filePath = Paths.get(file);
        Files.writeString(filePath, client.getMessage(), charset);
    }

    private static void writeToChannel(byte[] msg, SelectionKey key) throws IOException {
        // Write a msg into a key's Channel
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
        buffer.put(msg);
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }


    private static int randomId() {
        return (int) (Math.random() * 9999);
    }

    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel sock = (ServerSocketChannel) key.channel();
            SocketChannel client = sock.accept();
            client.configureBlocking(false);
            client.register(this.selector, SelectionKey.OP_READ);
            ClientState state = new ClientState();
            key.attach(state);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean commandExists(String message) {
        // TODO adjust for all commands (helo not correct)
        if (message.length() < 6)
            return false;
        // extra check for "helo"
        if (message.substring(0, 4).equalsIgnoreCase("helo"))
            return true;
        boolean exists = false;
        for (int i = 1; i < supportedCmds.length; i++) {
            if (message.length() < 7) {
                exists = message.equalsIgnoreCase(supportedCmds[i]);
                if (exists)
                    break;
            } else {
                exists = message.toLowerCase().indexOf(supportedCmds[i]) == 0;
                if (exists)
                    break;
            }
        }
        return exists;
    }

    private void handleRead(SelectionKey key) {
        // this method handels readable keys, sets the coreespondinng clientState object and then calls  key.interestOps(SelectionKey.OP_WRITE);
        try {
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
            ClientState client = (ClientState) key.attachment();
            channel.read(buffer);
            buffer.flip();
            CharsetDecoder decoder = charset.newDecoder();
            CharBuffer charBuf = decoder.decode(buffer);
            String message = charBuf.toString();
            if (client.getState() == data) {
                if (message.endsWith("\r\n.\r\n")) {
                    client.setPrevState(client.getState());
                    client.setState(msg);
                    String wholeMessage = client.getMessage() + message.split("\r\n.\r\n")[0];
                    client.setMessage(wholeMessage);
                } else {
                    String wholeMessage = client.getMessage() + message;
                    client.setMessage(wholeMessage);
                }
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
            if (message.toLowerCase().startsWith("helo")) {
                client.setPrevState(client.getState());
                client.setState(helo);
            } else if (message.toLowerCase().startsWith("help")) {
                client.setPrevState(client.getState());
                client.setState(help);
            } else if (message.toLowerCase().startsWith("quit")) {
                client.setPrevState(client.getState());
                client.setState(quit);
                writeToFile(client);
            } else if (message.toLowerCase().startsWith("data")) {
                client.setPrevState(client.getState());
                client.setState(data);
            } else if (message.toLowerCase().startsWith("rcpt")) {
                client.setPrevState(client.getState());
                client.setState(rcptTo);
                client.setReceiver(message.substring(9, message.length() - 2));
            } else if (message.toLowerCase().startsWith("mail")) {
                client.setPrevState(client.getState());
                client.setState(mailFrom);
                client.setSender(message.substring(11, message.length() - 2));
            } else {
                client.setState(serviceReady);
            }
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleWrite(SelectionKey key) throws IOException{
        ClientState client = (ClientState) key.attachment();
        switch (client.getState()) {
            case serviceReady:
                if (client.getPrevState() == serviceReady) {
                    writeToChannel(readyResp, key);
                } else {
                    writeToChannel(syntaxResp, key);
                }
                break;
            case helo:
                if (client.getPrevState() == serviceReady || client.getPrevState() == help) {
                    writeToChannel(ackResp, key);
                } else {
                    writeToChannel(invalidResp, key);
                }
                break;
            case mailFrom:
                if (client.getPrevState() == helo || client.getPrevState() == help) {
                    writeToChannel(ackResp, key);
                } else {
                    writeToChannel(invalidResp, key);
                }
                break;
            case rcptTo:
                if ((client.getPrevState() == mailFrom || client.getPrevState() == help) && client.getSender() != "") {
                    writeToChannel(ackResp, key);
                } else {
                    writeToChannel(invalidResp, key);
                }
                break;
            case data:
                if (client.getPrevState() == rcptTo || client.getPrevState() == help && client.getReceiver() != "") {
                    writeToChannel(ackResp, key);
                } else {
                    writeToChannel(invalidResp, key);
                }
                break;
            case msg:
                if (client.getPrevState() == data || client.getPrevState() == msg || client.getPrevState() == help) {
                    writeToChannel(ackResp, key);
                } else {
                    writeToChannel(invalidResp, key);
                }
                break;
            case help:
                writeToChannel(helpResp, key);
                break;
            case quit:
                writeToChannel(quitResp, key);
                key.cancel();
                key.channel().close();
                return;
            default:
                writeToChannel(internalResp, key);
                break;

        }
        key.interestOps(SelectionKey.OP_READ);
    }

    public void startServer() {
        try {
            System.out.println("Server is starting on port " + port);
            while (ssc.isOpen()) {
                // Block until at least one channel has been selected
                if (selector.select() == 0)
                    continue;
                // Get an iterator over the set of selected keys
                Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                // Look at each key in the selected set
                while (iter.hasNext()) {
                    // Get current key
                    SelectionKey key = iter.next();
                    // Is a new connection coming in?
                    if (key.isAcceptable())
                        handleAccept(key);
                    // Is there data to read on this channel?
                    if (key.isReadable())
                        handleRead(key);
                    // Is there data to be written on this channel?
                    if (key.isWritable())
                        handleWrite(key);
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
