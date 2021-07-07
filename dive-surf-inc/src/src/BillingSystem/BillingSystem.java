package BillingSystem;

import Order.Order;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import java.util.Random;

public class BillingSystem {

    private static Random rand = new Random();

    private static Processor BillCheck = new Processor() {
        public void process(Exchange exchange) throws Exception {
            Order order = (Order) exchange.getIn().getBody();

            boolean valid = rand.nextBoolean();
            order.setValid(Boolean.toString(valid));
            // the billing system modifies modifies the validationResult property
            order.setValid(Boolean.toString(valid));
            System.out.println("Order: "+order.getOrderID()+" is "+valid);
            // return Exchange
            exchange.getIn().setBody(order);
        }
    };

    public static void main(String[] args) throws Exception {
        DefaultCamelContext context = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        context.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("activemq:topic:Orders")
                        .process(BillCheck)
                        .to("activemq:queue:validated");
            }
        };
        context.addRoutes(route);
        context.start();
        System.in.read();
        context.stop();

    }

}
