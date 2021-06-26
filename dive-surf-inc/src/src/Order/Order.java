package Order;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Order {

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
