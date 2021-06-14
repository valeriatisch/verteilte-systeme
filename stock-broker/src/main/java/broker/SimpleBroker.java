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
                            System.out.println("Received buy request for: "+buy.getAmount()+" of stock "+buy.getStockName());
                            // TODO: buy stock
                        }
                        case STOCK_LIST -> {
                            // list stock
                            System.out.println("Received list request.");
                            List<Stock> stock = getStockList();
                            // TODO: send message to correct client
                        }
                        case STOCK_SELL -> {
                            SellMessage sell = (SellMessage) message;
                            System.out.println("Received sell request for: "+sell.getAmount()+" of stock "+sell.getStockName());
                            // TODO: sell stock
                        }
                        case SYSTEM_REGISTER -> {
                            RegisterMessage reg = (RegisterMessage) message;
                            String client = reg.getClientName();
                            if(!clients.containsKey(client)) {
                                clients.put(client, new HashMap<>());
                            }
                        }
                        case SYSTEM_UNREGISTER -> {
                            UnregisterMessage unreg = (UnregisterMessage) message;
                            String client = unreg.getClientName();
                            clients.remove(client);
                            // TODO: what happens to the clients stock?
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
                this.stocks.remove(stock);
            }
        }
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
        Stock stockToBuy = null;
        for (Stock stock: this.stocks) {
            if (stock.getName().equals(stockName)) {
                stockToBuy = stock;
                // remove stock from list to protect from concurrent write attempt
                this.stocks.remove(stock);
            }
        }
        if (stockToBuy == null) {
            return -1;
        }
        if (amount < 0) {
            this.stocks.add(stockToBuy);
            return -1;
        }
        if(this.clients.containsKey(clientName)) {
            // broker already knows the client
            HashMap<String, Integer> ownedStocks = this.clients.get(clientName);
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
        stockToBuy.setAvailableCount(stockToBuy.getAvailableCount() + amount);
        this.stocks.add(stockToBuy);
        return 1;
    }

    public synchronized List<Stock> getStockList() {
        List<Stock> stockList = new ArrayList<>();

        /* TODO: populate stockList */
        stockList.addAll(this.stocks);
        return stockList;
    }
}
