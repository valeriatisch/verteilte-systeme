/*
 * WebOrderSystem: the web order system generates a string for each
 * new incoming order. The string needs to be processed further by the
 * integration solution. The string consists of comma separated entries
 * and is formatted as follows: <First Name, Last Name, Number of
 * ordered surfboards, Number of ordered diving suits, Customer-ID> -
 * e.g.: Alice, Test, 2, 0, 1
 */

package WebOrderSystem;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class WebOrderSystem {

    private final Connection con;
    private final Session session;
    private final MessageProducer msg_producer;

    public WebOrderSystem() throws JMSException {
        ActiveMQConnectionFactory conFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        conFactory.setTrustAllPackages(true);
        con = conFactory.createConnection();
        con.start();
        session = con.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue orders_queue = session.createQueue("incoming_orders");
        msg_producer = session.createProducer(orders_queue);
    }

    public static void main(String[] args) throws JMSException {

        WebOrderSystem webordersys = new WebOrderSystem();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        boolean True = true;
        while (True) {

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
        }

        webordersys.msg_producer.close();
        webordersys.session.close();
        webordersys.con.close();

    }

}