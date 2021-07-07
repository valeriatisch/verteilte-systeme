/*
 * WebOrderSystem: the web order system generates a string for each
 * new incoming order. The string needs to be processed further by the
 * integration solution. The string consists of comma separated entries
 * and is formatted as follows: <First Name, Last Name, Number of
 * ordered surfboards, Number of ordered diving suits, Customer-ID> -
 * e.g.: Alice, Test, 2, 0, 1
 */

package WebOrderSystem;

import Order.Order;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
// import java.io.IOException;
import java.io.InputStreamReader;
// import java.util.Arrays;
// import java.util.List;

public class WebOrderSystem {

    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;
    private static final String[] firstName =  new String[] { "Adam", "Alexa", "Aaron", "Bella", "Carl", "Daria", "Dawson", "Ella",
            "Fred", "Fiona", "George", "Hella", "Hank", "Isa", "John", "Joanna", "Joe", "Lea", "Monte", "Marina",
            "Mark", "Nina", "Otto", "Paula", "Peter", "Rose", "Steve", "Tina", "Tim", "Victoria", "Walter"};
    private static final String[] lastName = new String[] { "Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd",
            "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman",
            "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent",
            "Ortiz", "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick",
            "Sagar", "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman" };

    public WebOrderSystem() throws JMSException {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic order_topic = session.createTopic("Orders");
        msg_producer = session.createProducer(order_topic);
    }

    public void stop() throws JMSException {
        msg_producer.close();
        session.close();
        con.close();
    }

    public static void main(String[] args) throws JMSException {

        WebOrderSystem webordersys = new WebOrderSystem();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        boolean True = true;
        int i = 0;
        while (i < 1) {

            // In case it should be generated be the user:
            /*
            System.out.println(" PLease enter a string with the following format: <First Name, Last Name, Number of" +
                    "ordered surfboards, Number of ordered diving suits, Customer-ID>");

            String order_str = null;
            try {
                order_str = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert order_str != null;
            List<String> items = Arrays.asList(order_str.split("\\s*,\\s*"));
            boolean b = true;
            if (items.size() != 5) {
                for (int i = 2; i < items.size(); i++) {
                    try {
                        Integer.parseInt(items.get(i));
                    } catch (NumberFormatException e) {
                        System.out.println(i + 1 + ". element must be an integer.");
                    }
                }
                b = false;
            }

            if (b) {
                webordersys.msg_producer.send(webordersys.session.createObjectMessage(order_str));
                System.out.println("Sent");
            }
            */

             Order order = new Order(String.valueOf(i), firstName[i], lastName[i], String.valueOf(2*i), String.valueOf(i), String.valueOf(i), String.valueOf(i), "false", "false");
             //String order_str = order.generate_order(1);
             //System.out.println(order_str);
             webordersys.msg_producer.send(webordersys.session.createObjectMessage(order));
             i++;
        }

        webordersys.stop();
    }

}