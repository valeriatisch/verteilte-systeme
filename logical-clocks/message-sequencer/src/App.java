public class App {
    public static void main(String[] args) {
        if (args.length == 2) {
            try {
                int NrThreads = Integer.parseInt(args[0]);
                int NrMsg = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Arguments must be integers.");
                System.exit(1);
            }
        } else {
            System.out.println("This program expects two arguments: 1. Number of Threads & 2. Number of messages to send");
        }

    }
}