package nl.joriswit.coaosokobansolver;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import Sokoban.*;

public class SolverActivity extends Activity {

    private SolverTask solverTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solver);

        Intent intent = getIntent();
        // Since this app only has one intent-filter, the following should always be true.
        if(intent.getAction().equals("nl.joriswit.sokosolver.SOLVE")) {

            String mapString = intent.getStringExtra("LEVEL");

            solverTask = new SolverTask();
            solverTask.execute(new Solver(mapString));
        }

        this.findViewById(R.id.close_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SolverActivity.this.finish();
            }
        });
    }

    @Override
    protected void onPause() {
        if(solverTask != null) {
            solverTask.cancel(true);
        }
        super.onPause();
    }

    private class SolverTask extends AsyncTask<Solver, Integer, State> implements SolverProgress {
        protected State doInBackground(Solver... solvers) {

            Solver solver = solvers[0];

            Heuristics.MultipleHeuristic heuristic =
                    new Heuristics.MultipleHeuristic();

            heuristic.add(new Heuristics.MinGoalDistance(), 3);

            long time = System.currentTimeMillis();
            long num = solver.search(heuristic, this, (int) (3.0 / 4 * Solver.searchLimit));
            time = System.currentTimeMillis() - time;

            if (solver.getEndState() == null) {
                heuristic = new Heuristics.MultipleHeuristic();
                heuristic.add(new Heuristics.MaxScore(), 3);
                time = System.currentTimeMillis();
                num += solver.search(heuristic, this, (int)(1.0/4 * Solver.searchLimit));
                time = System.currentTimeMillis() - time;
            }

            return solver.getEndState();
        }

        protected void onProgressUpdate(Integer... progress) {
            TextView expandedText = (TextView)SolverActivity.this.findViewById(R.id.expanded);
            TextView inspectedText = (TextView)SolverActivity.this.findViewById(R.id.inspected);
            TextView queueText = (TextView)SolverActivity.this.findViewById(R.id.queue);
            TextView filledText = (TextView)SolverActivity.this.findViewById(R.id.filled);
            expandedText.setText(progress[0].toString());
            inspectedText.setText(progress[1].toString());
            queueText.setText(progress[2].toString());
            filledText.setText(progress[3].toString());
        }

        protected void onPostExecute(State result) {
            TextView status = (TextView)SolverActivity.this.findViewById(R.id.status);
            if (result != null) {

                StringBuilder solution = new StringBuilder();
                for (Direction d : result.directionPath())
                    solution.append(d.toString());

                // This passes the solution back to Soko++.
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SOLUTION", solution.toString());
                setResult(RESULT_OK, resultIntent);

                status.setText(R.string.solution_found_text);
            } else {
                status.setText(R.string.solution_not_found_text);
            }
        }

        @Override
        public void onProgressUpdate(int expanded, int inspected, int visited, int filled, long start) {
            publishProgress(Integer.valueOf(expanded), Integer.valueOf(inspected), Integer.valueOf(visited), Integer.valueOf(filled));
        }
    }


}
