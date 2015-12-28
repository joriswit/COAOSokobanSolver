package Sokoban;

public interface SolverProgress {
    void onProgressUpdate(int expanded, int inspected, int visited,
                          int filled, long start);
    boolean isCancelled();
}
