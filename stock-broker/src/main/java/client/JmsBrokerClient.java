package client;

import common.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JmsBrokerClient {

    private final String clientName;

    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private final MessageConsumer msg_consumer;
    private final MessageProducer reg_producer;

    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;

        /* initialize connection, sessions, consumer, producer, etc. */
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue prod_queue = session.createQueue("prod_queue");
        msg_producer = session.createProducer(prod_queue);
        Queue cons_queue = session.createQueue("cons_queue");
        msg_consumer = session.createConsumer(cons_queue);
        // send register message
        Queue reg_queue = session.createQueue("reg_queue");
        reg_producer = session.createProducer(reg_queue);
        reg_producer.send(session.createObjectMessage(new RegisterMessage(clientName)));
        System.out.println("Client requested to register themself as " + clientName);
    }

    public void requestList() throws JMSException {
        // send request list message
        msg_producer.send(session.createObjectMessage(new RequestListMessage()));
        System.out.println("Client requested to see all stocks.");
        // receive list of stocks
        Message msg = msg_consumer.receive();
        if (msg instanceof ObjectMessage) {
            ListMessage content = (ListMessage) ((ObjectMessage) msg).getObject();
            System.out.println("Received reply ");
            System.out.println("\t List of the stocks: \n" + content.getStocks());
        } else {
            System.out.println("Invalid message detected");
        }
    }

    public void buy(String stockName, int amount) throws JMSException {
        // send buy message
        msg_producer.send(session.createObjectMessage(new BuyMessage(stockName, amount)));
        System.out.println("Client requested to buy " + stockName + " for " + amount);
    }

    public void sell(String stockName, int amount) throws JMSException {
        // send sell message
        msg_producer.send(session.createObjectMessage(new SellMessage(stockName, amount)));
        System.out.println("Client requested to sell " + stockName + " for " + amount);
    }

    public void watch(String stockName) throws JMSException {
        //TODO
    }

    public void unwatch(String stockName) throws JMSException {
        //TODO
    }

    public void quit() throws JMSException {
        // send unregister message
        reg_producer.send(session.createObjectMessage(new UnregisterMessage(clientName)));
        // close connection
        msg_producer.close();
        msg_consumer.close();
        con.close();
        System.out.println("Client quit");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter the client name:");
            String clientName = reader.readLine();

            JmsBrokerClient client = new JmsBrokerClient(clientName);

            boolean running = true;
            while (running) {
                System.out.println("Enter command:");
                String[] task = reader.readLine().split(" ");

                synchronized (client) {
                    switch (task[0].toLowerCase()) {
                        case "quit":
                            client.quit();
                            System.out.println("Bye bye");
                            running = false;
                            break;
                        case "list":
                            client.requestList();
                            break;
                        case "buy":
                            if (task.length == 3) {
                                client.buy(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: buy [stock] [amount]");
                            }
                            break;
                        case "sell":
                            if (task.length == 3) {
                                client.sell(task[1], Integer.parseInt(task[2]));
                            } else {
                                System.out.println("Correct usage: sell [stock] [amount]");
                            }
                            break;
                        case "watch":
                            if (task.length == 2) {
                                client.watch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        case "unwatch":
                            if (task.length == 2) {
                                client.unwatch(task[1]);
                            } else {
                                System.out.println("Correct usage: watch [stock]");
                            }
                            break;
                        default:
                            System.out.println("Unknown command. Try one of:");
                            System.out.println("quit, list, buy, sell, watch, unwatch");
                    }
                }
            }

        } catch (JMSException | IOException ex) {
            Logger.getLogger(JmsBrokerClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
