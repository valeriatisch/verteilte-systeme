public class ClientState {

    private int state;
    private String message;
    private String sender;
    private String receiver;

    public ClientState() {
        this.state = SMTPServer.serviceReady;
    }

    public int getState() {return this.state;}
    public void setState(int state) {this.state = state;}

    public String getMessage() {return this.message;}
    public void setMessage(String message) {this.message = message;}

    public String getSender() {return this.sender;}
    public void setSender(String sender) {this.sender = sender;}

    public String getReceiver() {return this.receiver;}
    public void setReceiver(String receiver) {this.receiver = receiver;}
}
