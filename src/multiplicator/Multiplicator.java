package multiplicator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Multiplicator {

    private static final Collection<Transition<Place>> transitions = new ArrayList<>();
    private static final Collection<Transition<Place>> resultSignal = new ArrayList<>();
    private static final int WAGE = 1;
    private static final int TOKEN = 1;

    private final Map<Place, Integer> marking = new HashMap<>();
    private int A, B;
    private PetriNet<Place> multiplicator;

    private enum Place {
        P1, P2, P3, A, B, PRODUCT
    }

    static {
        Map<Place, Integer> input1 = Collections.singletonMap(Place.P1, WAGE);
        Collection<Place> inhibitor1 = Collections.singleton(Place.B);
        resultSignal.add(new Transition<Place>(input1, Collections.emptySet(), inhibitor1, Collections.emptyMap()));

        Map<Place, Integer> input2 = new HashMap<>();
        input2.put(Place.P3, WAGE);
        input2.put(Place.P1, WAGE);
        Map<Place, Integer> output2 = new HashMap<>();
        output2.put(Place.A, WAGE);
        output2.put(Place.P1, WAGE);
        transitions.add(new Transition<>(input2, Collections.emptySet(), Collections.emptySet(), output2));

        Map<Place, Integer> input3 = Collections.singletonMap(Place.P2, WAGE);
        Map<Place, Integer> output3 = Collections.singletonMap(Place.P1, WAGE);
        Set<Place> inhibitor3 = Collections.singleton(Place.A);
        transitions.add(new Transition<>(input3, Collections.emptySet(), inhibitor3, output3));

        Map<Place, Integer> input4 = new HashMap<>();
        input4.put(Place.B, WAGE);
        input4.put(Place.P1, WAGE);
        Map<Place, Integer> output4 = Collections.singletonMap(Place.P2, WAGE);
        Set<Place> inhibitor4 = Collections.singleton(Place.P3);
        transitions.add(new Transition<>(input4, Collections.emptySet(), inhibitor4, output4));

        Map<Place, Integer> input5 = new HashMap<>();
        input5.put(Place.A, WAGE);
        input5.put(Place.P2, WAGE);
        Map<Place, Integer> output5 = new HashMap<>();
        output5.put(Place.PRODUCT, WAGE);
        output5.put(Place.P3, WAGE);
        output5.put(Place.P2, WAGE);
        transitions.add(new Transition<>(input5, Collections.emptySet(), Collections.emptySet(), output5));
    }

    private class Worker implements Runnable {

        private int counter;
        private int number;

        public Worker(int number) {
            this.number = number;
            this.counter = 0;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    multiplicator.fire(Multiplicator.transitions);
                    this.counter++;
                } catch (InterruptedException e) {
                    System.out.println("watek numer " + this.number + " wykonal fire " + this.counter + " razy");
                    return;
                }
            }
        }
    }

    public Multiplicator(int A, int B) {
        this.A = A;
        this.B = B;
        this.marking.put(Place.A, this.A);
        this.marking.put(Place.B, this.B);
        this.marking.put(Place.P1, TOKEN);
        this.multiplicator = new PetriNet<>(this.marking, true);
    }

    public void multiply() throws InterruptedException {
        List<Thread> workers = new ArrayList<>();
        for (int i = 1; i <= 4; i++)
            workers.add(new Thread(new Worker(i)));
        for (Thread t : workers)
            t.start();
        multiplicator.fire(Multiplicator.resultSignal);
        for (Thread t : workers)
            t.interrupt();
    }

    public int getResult() {
        return this.marking.get(Place.PRODUCT);
    }
}
