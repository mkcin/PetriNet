package petrinet;

import java.util.*;
import java.util.concurrent.Semaphore;


public class PetriNet<T> {

    private final boolean fair;
    private Map<T, Integer> marking;
    private Queue<Pair<Semaphore, Collection<Transition<T>>>> stopped;
    Semaphore fireSemaphore;
    Semaphore releaseNext;
    Semaphore safeFireCopy;

    public PetriNet(Map<T, Integer> initial, boolean fair) {
        this.fireSemaphore = new Semaphore(1, true);
        this.releaseNext = new Semaphore(0, true);
        this.safeFireCopy = new Semaphore(1, true);
        this.marking = initial;
        this.fair = fair;
        this.stopped = new ArrayDeque<>();
    }

    private void findReachableStates(Collection<Transition<T>> transitions, Map<T, Integer> currentMarking, Set<Map<T, Integer>> reachedMarkings) {
        if (reachedMarkings.contains(currentMarking))
            return;

        reachedMarkings.add(currentMarking);

        for (Transition<T> transition : transitions) {
            if (transition.canBeFired(currentMarking)) {
                Map<T, Integer> newMarking = new HashMap<>(currentMarking);
                transition.fire(newMarking);
                this.findReachableStates(transitions, newMarking, reachedMarkings);
            }
        }
    }

    public Set<Map<T, Integer>> reachable(Collection<Transition<T>> transitions) {
        Set<Map<T, Integer>> states = new HashSet<>();
        try {
            this.safeFireCopy.acquire();
            Map<T, Integer> currentMarking = new HashMap<>(this.marking);
            this.safeFireCopy.release();

            this.findReachableStates(transitions, currentMarking, states);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return states;
    }

    public Transition<T> fire(Collection<Transition<T>> transitions) throws InterruptedException {
        this.fireSemaphore.acquire();
        Transition<T> toFire = null;
        try {
            for (Transition<T> transition : transitions) {
                if (transition.canBeFired(this.marking)) {
                    toFire = transition;
                    break;
                }
            }

            if (toFire == null) {
                Semaphore privateMutex = new Semaphore(0, true);
                Pair<Semaphore, Collection<Transition<T>>> myFire = new Pair<>(privateMutex, transitions);
                this.stopped.add(myFire);
                this.fireSemaphore.release();
                privateMutex.acquire();

                for (Transition<T> transition : transitions) {
                    if (transition.canBeFired(this.marking)) {
                        toFire = transition;
                        break;
                    }
                }
            }

            this.safeFireCopy.acquire();
            toFire.fire(this.marking);
            this.safeFireCopy.release();

        } catch (InterruptedException e) {
            this.fireSemaphore.release();
            throw e;
        }

        boolean fired = false;
        for (Pair<Semaphore, Collection<Transition<T>>> waiting : this.stopped) {
            for (Transition<T> transition : waiting.getSecond()) {
                if (transition.canBeFired(this.marking)) {
                    Pair<Semaphore, Collection<Transition<T>>> nextThread = waiting;
                    this.stopped.remove(nextThread);
                    nextThread.getFirst().release();
                    fired = true;
                    break;
                }
            }
            if (fired)
                break;
        }

        if (!fired)
            this.fireSemaphore.release();

        return toFire;
    }

    public Map<T, Integer> getMarking() {
        return this.marking;
    }


}