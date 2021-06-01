package common;


public class RequestListMessage extends BrokerMessage {
    public RequestListMessage() {
        super(Type.STOCK_LIST);
    }
}
