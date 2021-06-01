package broker;

import common.Stock;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.ArrayList;
import java.util.List;


public class SimpleBroker {
    /* TODO: variables as needed */

    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if(msg instanceof ObjectMessage) {
                //TODO
            }
        }
    };

    public SimpleBroker(List<Stock> stockList) throws JMSException {
        /* TODO: initialize connection, sessions, etc. */

        for(Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
        }
    }

    public void stop() throws JMSException {
        //TODO
    }

    public synchronized int buy(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }

    public synchronized int sell(String stockName, int amount) throws JMSException {
        //TODO
        return -1;
    }

    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */

        return stockList;
    }
}