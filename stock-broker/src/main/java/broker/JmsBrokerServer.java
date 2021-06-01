package broker;

import  common.Stock;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JmsBrokerServer {
    public static void main(String[] args) {
        try {
            List<Stock> stocks = new ArrayList<>();
            stocks.add(new Stock("ALDI", 200, 2.0));
            stocks.add(new Stock("LIDL", 300, 1.0));
            
            SimpleBroker broker = new SimpleBroker(stocks);
            System.in.read();
            broker.stop();
        } catch (JMSException ex) {
            Logger.getLogger(JmsBrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JmsBrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
