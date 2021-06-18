package client;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MultiClients {


    public static void main(String[] args) throws JMSException, InterruptedException {
        System.out.println("Enter number of Clients to run");
        HashMap<String, JmsBrokerClient> threads = new HashMap<>();
        Scanner input = new Scanner(System.in);
        int numberOfThreads = Integer.parseInt(input.nextLine());
        // create threads
        for (int i = 0; i < numberOfThreads; i++) {
            System.out.println("Enter ID for client Nr: " + (i + 1));
            String id = input.nextLine();
            JmsBrokerClient client = new JmsBrokerClient(id);
            threads.put(id, client);
            System.out.println("Client Nr: " + (i + 1) + " with clientName " + id + " created.");
            System.out.println("\n");
            // client.start();
        }
        String command;
        boolean start = true;
        while( true) {
            System.out.println("Do you want to execute a command on all clients (Y/N) ?");
            command = input.nextLine();
            // quit
            if (command.toLowerCase().equals("quit all")) break;
            // send commands to threads
            if (command.toLowerCase(Locale.ROOT).equals("y")) {
                // same command
                System.out.println("input command");
                command = input.nextLine();
                System.out.println(command);
                for (String key: threads.keySet()) {
                    //JmsBrokerClient thread = threads.get(key);

                        threads.get(key).command = command;
                    System.out.println(threads.get(key).command);

                }
            } else {
                // different commands
                HashMap<String, String > commands = new HashMap<>();
                for (String key : threads.keySet()) {
                    System.out.println("Command for Client " + key);
                    commands.put(key, input.nextLine());
                }
                for (String key : threads.keySet()) {

                        System.out.println(key + " " + commands.get(key));
                        threads.get(key).command = commands.get(key);
                    System.out.println(key + " " + threads.get(key).command);
                }
            }
            System.out.println("Starting threads!");
            // start threads
            if (start) {
                for (String key : threads.keySet()) {
                    JmsBrokerClient thread = threads.get(key);
                    thread.start();
                }
                start = false;
            }
            TimeUnit.SECONDS.sleep(5);
        }
        // kill threads
        for (String key: threads.keySet()) {
            JmsBrokerClient thread = threads.get(key);
            thread.interrupt();
        }
    }
}
