package de.tu_berlin.cit.vs.jms.broker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import de.tu_berlin.cit.vs.jms.common.BrokerMessage;
import de.tu_berlin.cit.vs.jms.common.RegisterMessage;
import de.tu_berlin.cit.vs.jms.common.Stock;
import de.tu_berlin.cit.vs.jms.common.UnregisterMessage;
import org.apache.activemq.ActiveMQConnectionFactory;


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
