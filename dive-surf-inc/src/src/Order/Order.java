package Order;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

public class Order implements Serializable {

    private String CustomerID;
    private String FirstName;
    private String LastName;
    private String OverallItems;
    private String NumberOfDivingSuits;
    private String NumberOfSurfboards;
    private String OrderID;
    private String Valid;
    private String validationResult;

    public Order() {
    }

    public Order(String CustomerID, String FirstName, String LastName, String OverallItems, String NumberOfDivingSuits,
                 String NumberOfSurfboards, String OrderID, String Valid, String validationResult) {
        this.CustomerID = CustomerID;
        this.FirstName = FirstName;
        this.LastName = LastName;
        this.OverallItems = OverallItems;
        this.NumberOfDivingSuits = NumberOfDivingSuits;
        this.NumberOfSurfboards = NumberOfSurfboards;
        this.OrderID = OrderID;
        this.Valid = Valid;
        this.validationResult = validationResult;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getLastName() {
        return LastName;
    }

    public String getOverallItems() {
        return OverallItems;
    }

    public String getNumberOfDivingSuits() {
        return NumberOfDivingSuits;
    }

    public String getNumberOfSurfboards() {
        return NumberOfSurfboards;
    }

    public String getOrderID() {
        return OrderID;
    }

    public String getValid() {
        return Valid;
    }

    public String getValidationResult() {
        return validationResult;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public void setLastName(String lastName) {
        LastName = lastName;
    }

    public void setOverallItems(String overallItems) {
        OverallItems = overallItems;
    }

    public void setNumberOfDivingSuits(String numberOfDivingSuits) {
        NumberOfDivingSuits = numberOfDivingSuits;
    }

    public void setNumberOfSurfSuits(String numberOfSurfSuits) {
        NumberOfSurfboards = numberOfSurfSuits;
    }

    public void setOrderID(String orderID) {
        OrderID = orderID;
    }

    public void setValid(String valid) {
        Valid = valid;
    }

    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public String generate_order(int format) {
        Random rand = new Random();
        String[] firstName =  new String[] { "Adam", "Alexa", "Aaron", "Bella", "Carl", "Daria", "Dawson", "Ella",
                "Fred", "Fiona", "George", "Hella", "Hank", "Isa", "John", "Joanna", "Joe", "Lea", "Monte", "Marina",
                "Mark", "Nina", "Otto", "Paula", "Peter", "Rose", "Steve", "Tina", "Tim", "Victoria", "Walter"};
        String[] lastName = new String[] { "Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd",
                "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman",
                "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent",
                "Ortiz", "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick",
                "Sagar", "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman" };
        String order_str = "";
        if (format == 1){
            order_str = firstName[rand.nextInt(firstName.length)] + ", " + lastName[rand.nextInt(lastName.length)] +
                    ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000) + ", "
                    + rand.nextInt(1000) + '\n';
        } else if (format == 2){
            order_str = rand.nextInt(1000) + ", " + firstName[rand.nextInt(firstName.length)] + ' ' +
                    lastName[rand.nextInt(lastName.length)] + ", " + rand.nextInt(1000) + ", "
                    + rand.nextInt(1000) + '\n';
        }
        return order_str;
    }

    public void generate_file(int nr){
        Random rand = new Random();
        File order = new File("/Users/valeria.tisch/IdeaProjects/verteilte-systeme/dive-surf-inc/orders/order" + nr);
        try {
            FileWriter writer = new FileWriter(order);
            for(int i=0; i <= rand.nextInt(10); i++) writer.write(generate_order(2));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
