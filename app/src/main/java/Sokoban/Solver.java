package Sokoban;

import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * A class used for solving Sokoban puzzles. This is the main class of the
 * project.
 */
public class Solver {
    /**
     * Tells whether a progress meter should be displayed.
     */
    static boolean printProgress = true;

    /**
     * The time limit for a search.
     */
    public static int searchLimit = 60000;

    /**
     * The interval to wait between check of time and prints.
     */
    static int interval = 200;

    /**
     * The map to solve.
     */
    Map map;

    /**
     * The start state of the map.
     */
    State startState;

    /**
     * The end state of the map. If no solution has been found, it is null.
     */
    State endState;

    /**
     * Create a new Solver.
     *
     * @param mapString A string representation of a map.
     */
    public Solver(String mapString) {
        map = Map.parse(mapString);
        startState = new State(map.getStart(), map.getBoxes(), map);
        endState = null;
    }

    /**
     * @return the map to solve.
     */
    public Map getMap() {
        return map;
    }

    /**
     * @return the start state of the map.
     */
    public State getStartState() {
        return startState;
    }

    /**
     * @return the end state of the map.
     */
    public State getEndState() {
        return endState;
    }

    /**
     * Do an A* search for a solution.
     *
     * @param heuristic The heuristic to use.
     * @param limit The time limit.
     * @return the number of expanded nodes.
     */
    public int search(Comparator<State> heuristic, SolverProgress progress, int limit) {
        int numExpanded = 0;
        int numInspected = 0;

        Queue<State> queue = new PriorityQueue<State>(1000, heuristic);
        Set<State> visited = new HashSet<State>();

        queue.add(startState);
        visited.add(startState);

        int i = 0;
        long start = System.currentTimeMillis();

        while (!queue.isEmpty()) {
            State curState = queue.poll();

            if (numExpanded == 1 || i == interval) {
                if (printProgress) {
                    progress.onProgressUpdate(numExpanded, numInspected, queue.size(),
                            curState.getNumBoxesInGoal(), start);
                }

                if (System.currentTimeMillis()-start >= limit)
                    break;

                if (progress.isCancelled())
                    break;

                i = 0;
            }
            i++;
            numExpanded++;

            for (Entry<Direction, Point> move : curState.getAvailableMoves()) {
                State nextState = State.getStateAfterMove(curState, move);
                numInspected++;

                if (!visited.contains(nextState)) {
                    if (nextState.isGoalReached()) {
                        endState = nextState;
                        if (printProgress) {
                            progress.onProgressUpdate(numExpanded, numInspected, queue.size(),
                                    endState.getNumBoxesInGoal(), start);
                        }
                        return numExpanded;
                    }
                    queue.add(nextState);
                    visited.add(nextState);
                }
            }
        }

        if (printProgress) {
            progress.onProgressUpdate(numExpanded, numInspected, queue.size(), 0, start);
        }
        return numExpanded;
    }
}
