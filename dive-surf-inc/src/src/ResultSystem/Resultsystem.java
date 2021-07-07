/*
ResultSystem: the result system simply collects
 processed order and prints a message to the console once a new order is received.
 The result system must distinguish between valid and invalid orders
*/
/*
The result of the order application integration is a common/transformed format for orders.
 Each order consists of following properties (type String):
o CustomerID
o FirstName
o LastName
o OverallItems (Number of all items in order)
o NumberOfDivingSuits
o NumberOfSurfboards
o OrderID
o Valid
o validationResult
*/
package ResultSystem;

import Order.Order;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.AggregationStrategy;

public class Resultsystem {

    public static class BooleanAggregation implements AggregationStrategy {
        @Override
        public Exchange aggregate(Exchange exchange, Exchange exchange1) {

            Order order = (Order) exchange.getIn().getBody();
            Order order1 = (Order) exchange1.getIn().getBody();

            Boolean valid = Boolean.parseBoolean(order.getValid());
            Boolean valid1 = Boolean.parseBoolean(order1.getValid());

            order1.setValidationResult(Boolean.toString(valid && valid1));

            exchange1.getIn().setBody(order1);
            return exchange1;
        }
    }



    public static void main(String[] args) throws Exception{
        DefaultCamelContext ctxt = new DefaultCamelContext();
        ActiveMQComponent activeMQComponent = ActiveMQComponent.activeMQComponent();
        activeMQComponent.setTrustAllPackages(true);
        ctxt.addComponent("activemq", activeMQComponent);

        RouteBuilder route = new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("activemq:queue:validated")
                        .aggregate(constant(true), new BooleanAggregation()).completionInterval(5)
                        .choice()
                            .when(header("validationResult"))
                                .multicast()
                                .to("activemq:topic:processed", "stream:out");
            }
        };

        ctxt.addRoutes(route);

        ctxt.start();
        System.in.read();
        ctxt.stop();
    }
}
