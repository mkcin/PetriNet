package alternator;

import petrinet.PetriNet;
import petrinet.Transition;

import java.util.*;

public class Alternator {

    private static final ArrayList<ArrayList<Transition<Place>>> transitions = new ArrayList<>();
    private static final int WAGE = 1;
    private static final int TOKEN = 1;

    private final Map<Place, Integer> marking = new HashMap<>();
    private PetriNet<Place> alternator;
    private boolean criticalSectionOccupied;

    private enum Place {
        A1, A2, A_BLOCKER,
        B1, B2, B_BLOCKER,
        C1, C2, C_BLOCKER,
        CENTRE
    }

    private static Place getPlace(int group, int type) {
        if (group == 1) {
            if (type == 1) return Place.A1;
            if (type == 2) return Place.A2;
            if (type == 3) return Place.A_BLOCKER;
        }
        if (group == 2) {
            if (type == 1) return Place.B1;
            if (type == 2) return Place.B2;
            if (type == 3) return Place.B_BLOCKER;
        }
        if (group == 3) {
            if (type == 1) return Place.C1;
            if (type == 2) return Place.C2;
            if (type == 3) return Place.C_BLOCKER;
        }
        return Place.CENTRE;
    }

    static {
        for (int i = 1; i <= 3; i++) {
            ArrayList<Transition<Place>> ithPart = new ArrayList<>();

            Map<Place, Integer> input1 = new HashMap<>();
            input1.put(getPlace(i, 1), WAGE);
            input1.put(Place.CENTRE, WAGE);
            Map<Place, Integer> output1 = Collections.singletonMap(getPlace(i, 2), WAGE);
            Set<Place> inhibitor1 = Collections.singleton(getPlace(i, 3));
            ithPart.add(new Transition<Place>(input1, Collections.emptySet(), inhibitor1, output1));

            Map<Place, Integer> input2 = Collections.singletonMap(getPlace(i, 2), WAGE);
            Map<Place, Integer> output2 = new HashMap<>();
            output2.put(getPlace(i, 1), WAGE);
            output2.put(getPlace(i, 3), WAGE);
            output2.put(Place.CENTRE, WAGE);
            Set<Place> reset2 = new HashSet<>();
            reset2.add(getPlace(i % 3 + 1, 3));
            reset2.add(getPlace((i + 1) % 3 + 1, 3));
            ithPart.add(new Transition<Place>(input2, reset2, Collections.emptySet(), output2));

            transitions.add(ithPart);
        }
    }

    private class Worker implements Runnable {

        private int number;

        public Worker(int number) {
            this.number = number;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    alternator.fire(Alternator.transitions.get(this.number - 1));
                    assert (!criticalSectionOccupied);
                    criticalSectionOccupied = true;

                    System.out.println("watek numer " + this.number);
                    System.out.println(".");

                    assert (criticalSectionOccupied);
                    criticalSectionOccupied = false;
                    alternator.fire(Alternator.transitions.get(this.number - 1));
                } catch (InterruptedException e) {
                    System.out.println("watek numer " + this.number + " przerwany");
                    return;
                }
            }
        }
    }

    public Alternator() {
        this.marking.put(Place.A1, TOKEN);
        this.marking.put(Place.B1, TOKEN);
        this.marking.put(Place.C1, TOKEN);
        this.marking.put(Place.CENTRE, TOKEN);
        this.alternator = new PetriNet<>(this.marking, true);
        this.criticalSectionOccupied = false;
    }

    public void start() throws InterruptedException {
        ArrayList<Transition<Place>> allTransitions = new ArrayList<>(Alternator.transitions.get(0));
        allTransitions.addAll(Alternator.transitions.get(1));
        allTransitions.addAll(Alternator.transitions.get(2));

        Set<Map<Place, Integer>> possibleMarkings = this.alternator.reachable(allTransitions);
        System.out.println("Liczba osiągalnych znakowań: " + possibleMarkings.size());

        for (Map<Place, Integer> m : possibleMarkings) {
            boolean ASection = m.containsKey(Place.A2);
            boolean BSection = m.containsKey(Place.B2);
            boolean CSection = m.containsKey(Place.C2);
            boolean safeRule = ASection && !BSection && !CSection ||
                    !ASection && BSection && !CSection ||
                    !ASection && !BSection && CSection ||
                    !ASection && !BSection && CSection;
            assert (safeRule);
        }
        // Kiedy wątek X znajduje się w sekcji krytycznej, czyli po pierwszym odpaleniu swoich przejść
        // przenosi żeton z miejsca X1 do miejsca X2. Kiedy opuszcza sekcję krytyczną oddaje żeton z X2 do X1
        // Gdyby 2 wątki znajdowały się w sekcjach krytycznych, oba musiałyby mieć żetony w miejscach nr 2
        System.out.println("Wszystkie znakowania są bezpieczne");
        System.out.println("W każdym możliwym znakowaniu w sekcji krytycznej znajduje się tylko jeden wątek");

        ArrayList<Thread> workers = new ArrayList<>();
        for (int i = 1; i <= 3; i++)
            workers.add(new Thread(new Worker(i)));

        for (Thread t : workers)
            t.start();

        Thread.sleep(30000);

        for (Thread t : workers)
            t.interrupt();
    }

}
