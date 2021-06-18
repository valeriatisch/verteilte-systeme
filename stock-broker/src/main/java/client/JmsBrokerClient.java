package client;

import common.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


public class JmsBrokerClient extends Thread {

    private final String clientName;

    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private final MessageConsumer msg_consumer;
    private final MessageProducer reg_producer;
    private HashMap<String, MessageConsumer> subscribers = new HashMap<>();

    // to run multiple clients
    public volatile boolean running;
    public volatile String command = "empty";

    public JmsBrokerClient(String clientName) throws JMSException {
        this.clientName = clientName;

        /* initialize connection, sessions, consumer, producer, etc. */
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue prod_queue = session.createQueue("server_incoming"+this.clientName);
        msg_producer = session.createProducer(prod_queue);
        Queue cons_queue = session.createQueue("server_outgoing"+this.clientName);
        msg_consumer = session.createConsumer(cons_queue);
        // send register message
        Queue reg_queue = session.createQueue("register");
        reg_producer = session.createProducer(reg_queue);
        reg_producer.send(session.createObjectMessage(new RegisterMessage(clientName)));
        System.out.println("Client requested to register themself as " + clientName);
        Message message = msg_consumer.receive();
        if (message instanceof TextMessage) {
            System.out.println(((TextMessage) message).getText());
        } else {
            System.out.println("Invalid message detected");
        }
    }

    public void requestList() throws JMSException {
        // send request list message
        ObjectMessage msg = session.createObjectMessage(new RequestListMessage());
        msg.setStringProperty("ClientName", this.clientName);
        msg_producer.send(msg);
        System.out.println(this.clientName + " requested to see all stocks.");
        // receive list of stocks
        Message message = msg_consumer.receive();
        if (message instanceof ObjectMessage) {
            ListMessage content = (ListMessage) ((ObjectMessage) message).getObject();
//            System.out.println(this.clientName + " Received reply ");
            System.out.println(this.clientName + " \t List of the stocks: \n" + content.getStocks());
        } else {
            System.out.println(this.clientName + " Invalid message detected");
        }
    }

    public void buy(String stockName, int amount) throws JMSException {
        // send buy message
        ObjectMessage msg = session.createObjectMessage(new BuyMessage(stockName, amount));
        msg.setStringProperty("ClientName", this.clientName);
        msg_producer.send(msg);
        System.out.println(this.clientName + " requested to buy " + stockName + " for " + amount);
        Message message = msg_consumer.receive();
        if (message instanceof TextMessage) {
            System.out.println(((TextMessage) message).getText());
        } else {
            System.out.println(this.clientName + " Invalid message detected");
        }
    }

    public void sell(String stockName, int amount) throws JMSException {
        // send sell message
        ObjectMessage msg = session.createObjectMessage(new SellMessage(stockName, amount));
        msg.setStringProperty("ClientName", this.clientName);
        msg_producer.send(msg);
        System.out.println(this.clientName + " requested to sell " + stockName + " for " + amount);
        Message message = msg_consumer.receive();
        if (message instanceof TextMessage) {
            System.out.println(((TextMessage) message).getText());
        } else {
            System.out.println(this.clientName + " Invalid message detected");
        }
    }

    public void watch(String stockName) throws JMSException {
        Topic topic = this.session.createTopic(stockName);
        MessageConsumer subscriber = this.session.createConsumer(topic);
        this.subscribers.put(stockName, subscriber);
        MessageListener listener = message -> {
            try {
                System.out.println(((TextMessage) message).getText());
            } catch (JMSException e) {
                e.printStackTrace();
            }
        };
        //subscriber.setMessageListener(listener);
        System.out.println(this.clientName + " You are now watching stock: "+stockName);
    }

    public void unwatch(String stockName) throws JMSException {
        MessageConsumer subscriber = this.subscribers.get(stockName);
        if(subscriber == null) {
            System.out.println(this.clientName + " You cannot unwatch not watched stock: "+stockName);
        }
        else {
            subscriber.close();
            this.subscribers.remove(stockName);
            System.out.println(this.clientName + " You are no longer watching stock: "+stockName);
        }
    }

    public void quit() throws JMSException {
        // send unregister message
        reg_producer.send(session.createObjectMessage(new UnregisterMessage(clientName)));
        // close connection
        msg_producer.close();
        msg_consumer.close();
        con.close();
        System.out.println(this.clientName + " quit");
    }

    @Override
    public void run(){
        running = true;
        try {
            while (running) {
                if (!this.command.equals("empty")) {
                    synchronized (this) {
                        String[] task = this.command.split(" ");
                        switch (task[0].toLowerCase()) {
                            case "quit":
                                this.quit();
                                System.out.println(this.clientName + ": Bye bye");
                                running = false;
                                break;
                            case "list":
                                this.requestList();
                                break;
                            case "buy":
                                if (task.length == 3) {
                                    this.buy(task[1], Integer.parseInt(task[2]));
                                } else {
                                    System.out.println(this.clientName + ": Correct usage: buy [stock] [amount]");
                                }
                                break;
                            case "sell":
                                if (task.length == 3) {
                                    this.sell(task[1], Integer.parseInt(task[2]));
                                } else {
                                    System.out.println(this.clientName + ": Correct usage: sell [stock] [amount]");
                                }
                                break;
                            case "watch":
                                if (task.length == 2) {
                                    this.watch(task[1]);
                                } else {
                                    System.out.println(this.clientName + ": Correct usage: watch [stock]");
                                }
                                break;
                            case "unwatch":
                                if (task.length == 2) {
                                    this.unwatch(task[1]);
                                } else {
                                    System.out.println(this.clientName + ": Correct usage: watch [stock]");
                                }
                                break;
                            default:
                                System.out.println(this.clientName + ": Unknown command. Try one of:");
                                System.out.println("quit, list, buy, sell, watch, unwatch");
                        }
                        this.command = "empty";
                    }
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(JmsBrokerClient.class.getName()).log(Level.SEVERE, null, ex);
        }
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
