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
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.Serializable;

public class InventroySystem {

}
