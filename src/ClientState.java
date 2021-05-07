public class ClientState {

    private int state;
    private int prevState;
    private String message;
    private String sender;
    private String receiver;

    public ClientState() {
        this.state = SMTPServer.serviceReady;
        this.prevState = SMTPServer.serviceReady;
        this.sender = "";
        this.receiver = "";
        this.message = "";
    }

    public int getState() {return this.state;}
    public void setState(int state) {this.state = state;}

    public int getPrevState() {return this.prevState;}
    public void setPrevState(int state) {this.prevState = state;}

    public String getMessage() {return this.message;}
    public void setMessage(String message) {this.message = message;}

    public String getSender() {return this.sender;}
    public void setSender(String sender) {this.sender = sender;}

    public String getReceiver() {return this.receiver;}
    public void setReceiver(String receiver) {this.receiver = receiver;}
}
