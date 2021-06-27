package BillingSystem;

import Order.Order;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;
import java.util.Random;

public class BillingSystem {

    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private final MessageConsumer msg_consumer;
    Random rand = new Random();

    public BillingSystem() throws JMSException {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue prod_queue = session.createQueue("incoming");
        msg_producer = session.createProducer(prod_queue);
        Queue cons_queue = session.createQueue("outgoing");
        msg_consumer = session.createConsumer(cons_queue);
        MessageListener listener = message -> {

            if (message instanceof ObjectMessage) {
                try {
                    Order order = (Order) ((ObjectMessage) message).getObject();
                    // test if customer is in good credit
                    // if (order.isValid()) { /*what to do here?*/ }
                    // the billing system modifies the valid property of the incoming messages
                    boolean valid = rand.nextBoolean();
                    order.setValid(Boolean.toString(valid));
                    // the billing system modifies modifies the validationResult property
                    if (valid) {
                        order.setValidationResult("Positive");
                    } else {
                        order.setValidationResult("Negative");
                    }
                    msg_producer.send(session.createObjectMessage((Serializable) order));
                } catch (JMSException e) {
                    e.printStackTrace();
                }

            }
        };
        msg_consumer.setMessageListener(listener);
    }

    public void stop() throws JMSException {
        this.msg_consumer.close();
        this.msg_producer.close();
        this.session.close();
        this.con.close();
        System.exit(0);
    }

    public static void main(String[] args) throws JMSException {
        BillingSystem billingsys = new BillingSystem();
        billingsys.stop();
    }

}
