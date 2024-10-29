import java.util.*;

public class NFAToDFALazyConverter {

    // NFA class with state transitions represented as a map
    static class NFA {
        Map<Integer, Map<Character, Set<Integer>>> transitions;
        Set<Integer> startStates;
        Set<Integer> acceptStates;

        NFA() {
            transitions = new HashMap<>();
            startStates = new HashSet<>();
            acceptStates = new HashSet<>();
        }

        void addTransition(int fromState, char symbol, int toState) {
            transitions.computeIfAbsent(fromState, k -> new HashMap<>())
                       .computeIfAbsent(symbol, k -> new HashSet<>()).add(toState);
        }

        Set<Integer> getTransitions(int state, char symbol) {
            return transitions.getOrDefault(state, Collections.emptyMap())
                              .getOrDefault(symbol, Collections.emptySet());
        }
    }

    // DFA class to store its states, transitions, and start/accept states
    static class DFA {
        Map<Set<Integer>, Map<Character, Set<Integer>>> transitions;
        Set<Set<Integer>> acceptStates;
        Set<Integer> startState;

        DFA(Set<Integer> startState) {
            this.transitions = new HashMap<>();
            this.startState = startState;
            this.acceptStates = new HashSet<>();
        }

        void addTransition(Set<Integer> fromState, char symbol, Set<Integer> toState) {
            transitions.computeIfAbsent(fromState, k -> new HashMap<>()).put(symbol, toState);
        }
    }

    // METHOD : converttoDFA()
    /*  description : 
     * This method implements the core of the subset construction algorithm. 
        By processing each state lazily, it only creates DFA states as they’re required, reducing unnecessary computation.
     */
    public static DFA convertToDFA(NFA nfa, Set<Character> alphabet) {
        Map<Set<Integer>, Set<Integer>> dfaStates = new HashMap<>();
        Queue<Set<Integer>> workQueue = new LinkedList<>();
        
        // Initialize DFA start state
        Set<Integer> dfaStartState = epsilonClosure(nfa.startStates, nfa);
        DFA dfa = new DFA(dfaStartState);
        workQueue.add(dfaStartState);
        dfaStates.put(dfaStartState, dfaStartState);

        // Lazy state exploration
        while (!workQueue.isEmpty()) {
            Set<Integer> currentDFAState = workQueue.poll();

            // Check if it's an accept state
            if (!Collections.disjoint(currentDFAState, nfa.acceptStates)) {
                dfa.acceptStates.add(currentDFAState);
            }

            // Explore transitions for each symbol in the alphabet
            for (char symbol : alphabet) {
                Set<Integer> nextState = new HashSet<>();
                for (int nfaState : currentDFAState) {
                    nextState.addAll(nfa.getTransitions(nfaState, symbol));
                }
                
                nextState = epsilonClosure(nextState, nfa);

                // If this DFA state is new, add it to the work queue
                if (!dfaStates.containsKey(nextState)) {
                    dfaStates.put(nextState, nextState);
                    workQueue.add(nextState);
                }
                
                // Add the transition to the DFA
                dfa.addTransition(currentDFAState, symbol, nextState);
            }
        }

        return dfa;
    }

    // METHOD : epsilonClosure()
    /* description : 
     * This is a helper function to find the epsilon closure of a set of states. 
     * It uses a stack to iteratively add all states reachable through epsilon transitions, ensuring that each state is processed only once.
     */
    private static Set<Integer> epsilonClosure(Set<Integer> states, NFA nfa) {
        Set<Integer> closure = new HashSet<>(states);
        Stack<Integer> stack = new Stack<>();
        stack.addAll(closure);


        while (!stack.isEmpty()) {
            int state = stack.pop();
            for (int nextState : nfa.getTransitions(state, 'ε')) { // assuming ε is the epsilon symbol
                if (closure.add(nextState)) {
                    stack.push(nextState);
                }
            }
        }

        return closure;
    }

    public static void main(String[] args) {
        // Example NFA setup
        NFA nfa = new NFA();
        nfa.startStates.add(0);
        nfa.acceptStates.add(2);
        nfa.addTransition(0, 'a', 0);
        nfa.addTransition(0, 'b', 0);
        nfa.addTransition(0, 'b', 1);
        nfa.addTransition(1, 'a', 2);

        // Alphabet for the language
        Set<Character> alphabet = new HashSet<>(Arrays.asList('a', 'b'));

        // Convert to DFA lazily
        DFA dfa = convertToDFA(nfa, alphabet);

        // Output DFA transitions (for testing purposes)
        System.out.println("DFA Transitions:");
        for (Map.Entry<Set<Integer>, Map<Character, Set<Integer>>> entry : dfa.transitions.entrySet()) {
            System.out.println("From state(s) " + entry.getKey() + ":");
            for (Map.Entry<Character, Set<Integer>> trans : entry.getValue().entrySet()) {
                System.out.println("  --" + trans.getKey() + "--> " + trans.getValue());
            }
        }
    }
}
