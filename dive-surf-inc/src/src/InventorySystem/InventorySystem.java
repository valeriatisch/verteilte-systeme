/*
InventorySystem: the inventory system takes transformed 
order messages (see below) and simply tests whether the requested
items are available. Only one inventory exists, 
which means that the InventorySystems checks both types of items.
Therefore, the inventory system modifies the valid property of the
incoming messages and optional modifies the validationResult property
*/

package InventorySystem;

import Order.Order;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;


public class InventorySystem {
    private static int totalSurfboards = 100;
    private static int totalDivingSuits = 100;


    public static void setTotalSurfboards(int total) {
        totalSurfboards = total;
    }
    public static int getTotalSurfboards() {
        return totalSurfboards;
    }

    public static void setTotalDivingSuits(int total) {
        totalDivingSuits = total;
    }

    public static int getTotalDivingSuits() {
        return totalDivingSuits;
    }

    public static boolean checkInventory(int surfboards, int diveSuits) {
        // Illegal Input
        if (surfboards < 0 || diveSuits < 0) {
            return false;
        }
        // Check Availability
        boolean checkBoards = (getTotalSurfboards() - surfboards) > -1;
        boolean checkSuits = (getTotalDivingSuits() - diveSuits) > -1;

        return (checkBoards && checkSuits);
    }

    public static void updateInventory(int surfboards, int diveSuits) {
        // Set new inventory variables
        setTotalSurfboards(getTotalSurfboards() - surfboards);
        setTotalDivingSuits(getTotalDivingSuits() - diveSuits);
    }

    private static Processor InventoryCheck = new Processor() {
        public void process(Exchange exchange) throws Exception {
            Order order = (Order) exchange.getIn().getBody();

            int surfboards = Integer.parseInt(order.getNumberOfSurfboards());
            int diveSuits = Integer.parseInt(order.getNumberOfDivingSuits());
            // set 'Valid' property
            order.setValid(Boolean.toString(checkInventory(surfboards, diveSuits)));
            System.out.println("Order: "+order.getOrderID()+" is "+checkInventory(surfboards, diveSuits));
            // return Exchange
            exchange.getIn().setBody(order);
        }
    };

    private static Processor InventoryUpdate = new Processor() {
        public void process(Exchange exchange) throws Exception {
            Order order = (Order) exchange.getIn().getBody();
            // split body into different properties

            int surfboards = Integer.parseInt(order.getNumberOfSurfboards());
            int diveSuits = Integer.parseInt(order.getNumberOfDivingSuits());
            // update Inventory
            updateInventory(surfboards, diveSuits);
            // return Exchange
            order.setValid(Boolean.toString(checkInventory(surfboards, diveSuits)));
;
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
                        .process(InventoryCheck)
                        .to("activemq:queue:validated");
                from("activemq:topic:processed")
                        .process(InventoryUpdate)
                        .to("stream:out");
            }
        };

        context.addRoutes(route);
        context.start();
        System.in.read();
        context.stop();
    }
}
