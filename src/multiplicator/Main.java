package multiplicator;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            int a = scanner.nextInt();
            int b = scanner.nextInt();

            Multiplicator multiplicator = new Multiplicator(a, b);
            multiplicator.multiply();
            System.out.println(multiplicator.getResult());
        } catch (InterruptedException e) {
            System.out.println("Wątek główny zatrzymany w trakcie działania");
            return;
        }


    }
}
