package responses;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Responses {
    private static final Charset charset = StandardCharsets.US_ASCII;
    
    public static byte [] crnlMsg = null;
    public static byte [] serviceRdy = null;
    public static byte [] ack = null;
    public static byte [] startMailInput = null;
    public static byte [] closingChnl = null;
    public static byte [] cmdUnrecognzd = null;
    public static byte [] cmdOutOfOrder = null;
    public static byte [] helpMsg = null;

    /*
    public static void initializeResponses() {
        // SMTP Response codes: https://www.knownhost.com/wiki/email/troubleshooting/error-numbers
        crnlMsg = new String("\r\n").getBytes(charset);
        serviceRdy = new String("220 service ready").getBytes(charset);
        ack = new String("250 OK").getBytes(charset);
        startMailInput = new String("354 start mail input").getBytes(charset);
        closingChnl = new String("221 closing channel").getBytes(charset);
        cmdUnrecognzd = new String("500 command unrecognized").getBytes(charset);
        cmdOutOfOrder = new String("503 command out of order").getBytes(charset);
        helpMsg = new String("214 Use following commands in given order.\nHELO -> MAIL FROM: -> RCPT TO: -> DATA -> actual message -> QUIT").getBytes(charset);
    }*/
}
