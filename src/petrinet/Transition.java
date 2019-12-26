package petrinet;

import java.util.Collection;
import java.util.Map;

public class Transition<T> {


    private final Map<T, Integer> input;
    private final Collection<T> reset;
    private final Collection<T> inhibitor;
    private final Map<T, Integer> output;

    public Transition(Map<T, Integer> input, Collection<T> reset, Collection<T> inhibitor, Map<T, Integer> output) {
        this.input = input;
        this.reset = reset;
        this.inhibitor = inhibitor;
        this.output = output;
    }

    public void fire(Map<T, Integer> marking) {
        for (Map.Entry<T, Integer> element : this.input.entrySet()) {
            T key = element.getKey();
            Integer value = element.getValue();
            if (!marking.containsKey(key))
                marking.put(key, 0);

            marking.replace(element.getKey(), marking.get(key) - value);

            if (marking.get(key) == 0)
                marking.remove(key);
        }
        for (Map.Entry<T, Integer> element : this.output.entrySet()) {
            T key = element.getKey();
            Integer value = element.getValue();
            if (marking.containsKey(key))
                marking.put(key, marking.get(key) + value);
            else
                marking.put(key, value);
        }
        for (T reset : this.reset)
            marking.remove(reset);
    }

    public boolean canBeFired(Map<T, Integer> marking) {
        for (T from : this.input.keySet()) {
            if (marking.containsKey(from)) {
                if (marking.get(from) < input.get(from))
                    return false;
            } else if (input.get(from) != 0)
                return false;
        }
        for (T blocked : this.inhibitor) {
            if (marking.containsKey(blocked))
                return false;
        }
        return true;
    }


    public Map<T, Integer> getInput() {
        return input;
    }

    public Collection<T> getReset() {
        return reset;
    }

    public Collection<T> getInhibitor() {
        return inhibitor;
    }

    public Map<T, Integer> getOutput() {
        return output;
    }

}