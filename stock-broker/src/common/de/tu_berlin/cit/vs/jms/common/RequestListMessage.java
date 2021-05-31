package de.tu_berlin.cit.vs.jms.common;


public class RequestListMessage extends BrokerMessage {
    public RequestListMessage() {
        super(Type.STOCK_LIST);
    }
}
