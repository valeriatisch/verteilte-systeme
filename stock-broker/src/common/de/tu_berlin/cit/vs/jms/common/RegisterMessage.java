package de.tu_berlin.cit.vs.jms.common;


public class RegisterMessage extends BrokerMessage {
    private String clientName;
    
    public RegisterMessage(String clientName) {
        super(Type.SYSTEM_REGISTER);
        
        this.clientName = clientName;
    }
    
    public String getClientName() {
        return clientName;
    }
}
