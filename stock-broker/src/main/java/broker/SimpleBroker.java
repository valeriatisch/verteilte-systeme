package broker;

import common.*;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.util.*;
import javax.jms.Queue;


public class SimpleBroker {
    /* TODO: variables as needed */
    private Connection connection;
    private Session session;
    private HashMap<String, MessageProducer> producers = new HashMap<>();
    private HashMap<String, MessageConsumer> consumers = new HashMap<>();
    private ArrayList<Stock> stocks = new ArrayList<>();
    private HashMap<String, HashMap<String, Integer>> clients = new HashMap<>();



    private final MessageListener listener = new MessageListener() {
        @Override
        public void onMessage(Message msg) {
            if(msg instanceof ObjectMessage) {
                try {
                    BrokerMessage message = (BrokerMessage) ((ObjectMessage) msg).getObject();
                    switch(message.getType()) {
                        case STOCK_BUY -> {
                            // buy stock
                            BuyMessage buy = (BuyMessage) message;
                            String stock = buy.getStockName();
                            int amount = buy.getAmount();
                            System.out.printf("Received buy request for %d of stock %s%n", amount, stock);
                            String client = msg.getStringProperty("ClientName");
                            if(!clients.containsKey(client)) {
                                session.createTextMessage("You are not registered. Did you send the message to a " +
                                        "wrong queue?");
                                break;
                            }
                            int succ = buy(client, buy.getStockName(), buy.getAmount());
                            MessageProducer producer = producers.get(client);
                            if(succ==1) {
                                producer.send(session.createTextMessage(String.format("You successfully bought %d of" +
                                        " stock %s.", amount, stock)));
                                MessageProducer topicProducer = producers.get(stock);
                                topicProducer.send(session.createTextMessage(String.format("%s bought %d of stock %s",
                                        client, amount, stock)));
                            }
                            else {
                                producer.send(session.createTextMessage(String.format("Could not buy %d of" +
                                        " stock %s.", amount, stock)));
                            }
                        }
                        case STOCK_LIST -> {
                            // list stock
                            String client = msg.getStringProperty("ClientName");
                            System.out.println("Received list request.");
                            List<Stock> stock = getStockList();
                            MessageProducer producer = producers.get(client);
                            producer.send(session.createObjectMessage(new ListMessage(stock)));
                        }
                        case STOCK_SELL -> {
                            SellMessage sell = (SellMessage) message;
                            String stock = sell.getStockName();
                            int amount = sell.getAmount();
                            System.out.println("Received sell request for: "+sell.getAmount()+" of stock "+
                                    sell.getStockName());
                            String client = msg.getStringProperty("ClientName");
                            if(!clients.containsKey(client)) {
                                session.createTextMessage("You are not registered. Did you send the message to a " +
                                        "wrong queue?");
                                break;
                            }
                            int succ = sell(client, stock, amount);
                            MessageProducer producer = producers.get(client);
                            if(succ==1) {
                                producer.send(session.createTextMessage(String.format("You successfully sold %d of" +
                                        " stock %s.", amount, stock)));
                                MessageProducer topicProducer = producers.get(stock);
                                topicProducer.send(session.createTextMessage(String.format("%s sold %d of stock %s",
                                        client, amount, stock)));
                            }
                            else {
                                producer.send(session.createTextMessage(String.format("Could not sell %d of" +
                                        " stock %s.", amount, stock)));
                            }
                        }
                        case SYSTEM_REGISTER -> {
                            RegisterMessage reg = (RegisterMessage) message;
                            String client = reg.getClientName();
                            System.out.printf("Received register from client: %s%n", client);
                            if(!clients.containsKey(client)) {
                                clients.put(client, new HashMap<>());
                                Queue incoming = session.createQueue("server_incoming"+client);
                                Queue outgoing = session.createQueue("server_outgoing"+client);
                                MessageConsumer consumer = session.createConsumer(incoming);
                                MessageProducer producer = session.createProducer(outgoing);
                                consumers.put(client, consumer);
                                producers.put(client, producer);
                                consumer.setMessageListener(this);
                                producer.send(session.createTextMessage("You were successfully registered at the broker!"));
                            }
                            else {
                                producers.get(client).send(session.createTextMessage("You already are registered."));
                            }
                        }
                        case SYSTEM_UNREGISTER -> {
                        }
                    }

                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public SimpleBroker(List<Stock> stockList) throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        connectionFactory.setTrustAllPackages(true);
        // start connection
        this.connection = connectionFactory.createConnection();
        this.connection.start();
        // create session
        this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        // create register queue
        Queue registerQueue = this.session.createQueue("register");
        // create consumer
        MessageConsumer consumer = this.session.createConsumer(registerQueue);
        for(Stock stock : stockList) {
            /* TODO: prepare stocks as topics */
            Topic topic = this.session.createTopic(stock.getName());
            // store producer into Hashmap
            this.producers.put(stock.getName(), this.session.createProducer(topic));
        }
        // store stocks
        this.stocks.addAll(stockList);
        consumer.setMessageListener(this.listener);
    }

    public void stop() throws JMSException {
        //TODO
        this.session.close();
        this.connection.close();
        System.exit(0);
    }

    public synchronized int buy(String clientName, String stockName, int amount) throws JMSException {
        Stock stockToBuy = null;
        for (Stock stock: this.stocks) {
            if (stock.getName().equals(stockName)) {
                stockToBuy = stock;
                // remove stock from list to protect from concurrent write attempt
            }
        }
        this.stocks.remove(stockToBuy);
        if (stockToBuy == null) {
            return -1;
        }
        if (stockToBuy.getAvailableCount() <= 0 || stockToBuy.getAvailableCount() - amount < 0) {
            this.stocks.add(stockToBuy);
            return -1;
        }
        if(this.clients.containsKey(clientName)) {
            HashMap<String, Integer> ownedStocks = this.clients.get(clientName);
            if (ownedStocks.containsKey(stockName)) {
                // broker already knows the client, they own the stock already
                int owned = ownedStocks.get(stockName);
                ownedStocks.remove(stockName);
                owned = owned + amount;
                ownedStocks.put(stockName, owned);
            } else {
                // client doesn't own this stock yet
                ownedStocks.put(stockName, amount);
            }
            this.clients.replace(clientName, ownedStocks);
        }
        else {
            // client is buying for the first time
            HashMap<String, Integer> ownedStocks = new HashMap<>();
            ownedStocks.put(stockName, amount);
            this.clients.put(clientName, ownedStocks);
        }
        stockToBuy.setAvailableCount(stockToBuy.getAvailableCount() - amount);
        this.stocks.add(stockToBuy);
        return 1;
    }

    public synchronized int sell(String clientName, String stockName, int amount) throws JMSException {
        Stock stockToSell = null;
        for (Stock stock: this.stocks) {
            if (stock.getName().equals(stockName)) {
                stockToSell = stock;
                // remove stock from list to protect from concurrent write attempt
            }
        }
        this.stocks.remove(stockToSell);
        if (stockToSell == null) {
            return -1;
        }
        if (amount < 0) {
            this.stocks.add(stockToSell);
            return -1;
        }
        if(this.clients.containsKey(clientName)) {
            // broker already knows the client
            HashMap<String, Integer> ownedStocks = this.clients.get(clientName);
            System.out.println(ownedStocks);
            if (ownedStocks.containsKey(stockName)) {
                // the client owns some of the stock already
                int owned = ownedStocks.get(stockName);
                if (owned >= amount) {
                    // amount to be sold is not more than client owns
                    ownedStocks.remove(stockName);
                    ownedStocks.put(stockName, owned - amount);
                    this.clients.replace(clientName, ownedStocks);
                } else {
                    // client is trying to sell more than they own
                    return -1;
                }
            } else {
                // client doesn't own this stock yet
                return -1;
            }
        } else {
            // client is trying to sell stock they don't own
            return -1;
        }
        stockToSell.setAvailableCount(stockToSell.getAvailableCount() + amount);
        this.stocks.add(stockToSell);
        return 1;
    }

    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */
        stockList.addAll(this.stocks);
        return stockList;
    }
}
