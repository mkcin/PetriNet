package alternator;

public class Main {
    public static void main(String[] args) {
        try {
            Alternator alternator = new Alternator();
            alternator.start();
        } catch (InterruptedException e) {
            System.out.println("Wątek główny zatrzymany w trakcie działania");
            return;
        }
    }
}

