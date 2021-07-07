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

    // return the current CustomerID
    public String getCustomerID() {
        return this.CustomerID;
    }

    // return the current LastName
    public String getFirstName() {
        return this.FirstName;
    }

    // return the current LastName
    public String getLastName() {
        return this.LastName;
    }

    // return the current OverallItems
    public String getOverallItems() {
        return this.OverallItems;
    }

    // return the current NumberOfDivingSuits
    public String getNumberOfDivingSuits() {
        return this.NumberOfDivingSuits;
    }

    // return the current NumberOfSurfboards
    public String getNumberOfSurfboards() {
        return this.NumberOfSurfboards;
    }

    // return the current OrderID
    public String getOrderID() {
        return this.OrderID;
    }

    // return the current Valid
    public String getValid() {
        return this.Valid;
    }

    // return the current validationResult
    public String getValidationResult() {
        return this.validationResult;
    }

    // return the current CustomerID
    public void setCustomerID(String customerID) {
        this.CustomerID = customerID;
    }

    // set a new Value for FirstName
    public void setFirstName(String firstName) {
        this.FirstName = firstName;
    }

    // set a new Value for LastName
    public void setLastName(String lastName) {
        this.LastName = lastName;
    }

    // set a new Value for OverallItems
    public void setOverallItems(String overallItems) {
        this.OverallItems = overallItems;
    }

    // set a new Value for NumberOfDivingSuits
    public void setNumberOfDivingSuits(String numberOfDivingSuits) {
        this.NumberOfDivingSuits = numberOfDivingSuits;
    }

    // set a new Value for NumberOfSurfboards
    public void setNumberOfSurfSuits(String numberOfSurfSuits) {
        this.NumberOfSurfboards = numberOfSurfSuits;
    }

    // set a new Value for OrderID
    public void setOrderID(String orderID) {
        this.OrderID = orderID;
    }

    // set a new Value for setValid
    public void setValid(String valid) {
        this.Valid = valid;
    }

    // set a new Value for validationResult
    public void setValidationResult(String validationResult) {
        this.validationResult = validationResult;
    }

    public String generate_order(int format) {
        Random rand = new Random();
        String[] firstName = new String[] { "Adam", "Alexa", "Aaron", "Bella", "Carl", "Daria", "Dawson", "Ella",
                "Fred", "Fiona", "George", "Hella", "Hank", "Isa", "John", "Joanna", "Joe", "Lea", "Monte", "Marina",
                "Mark", "Nina", "Otto", "Paula", "Peter", "Rose", "Steve", "Tina", "Tim", "Victoria", "Walter" };
        String[] lastName = new String[] { "Anderson", "Ashwoon", "Aikin", "Bateman", "Bongard", "Bowers", "Boyd",
                "Cannon", "Cast", "Deitz", "Dewalt", "Ebner", "Frick", "Hancock", "Haworth", "Hesch", "Hoffman",
                "Kassing", "Knutson", "Lawless", "Lawicki", "Mccord", "McCormack", "Miller", "Myers", "Nugent", "Ortiz",
                "Orwig", "Ory", "Paiser", "Pak", "Pettigrew", "Quinn", "Quizoz", "Ramachandran", "Resnick", "Sagar",
                "Schickowski", "Schiebel", "Sellon", "Severson", "Shaffer", "Solberg", "Soloman" };
        String order_str = "";
        if (format == 1) {
            order_str = firstName[rand.nextInt(firstName.length)] + ", " + lastName[rand.nextInt(lastName.length)]
                    + ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000) + '\n';
        } else if (format == 2) {
            order_str = rand.nextInt(1000) + ", " + firstName[rand.nextInt(firstName.length)] + ' '
                    + lastName[rand.nextInt(lastName.length)] + ", " + rand.nextInt(1000) + ", " + rand.nextInt(1000)
                    + '\n';
        }
        return order_str;
    }

    public void generate_file(int nr) {
        Random rand = new Random();
        File order = new File("/Users/valeria.tisch/IdeaProjects/verteilte-systeme/dive-surf-inc/orders/order" + nr);
        try {
            FileWriter writer = new FileWriter(order);
            for (int i = 0; i <= rand.nextInt(10); i++)
                writer.write(generate_order(2));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
