import org.moeaframework.core.Solution;
import org.moeaframework.core.initialization.Initialization;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.problem.Problem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class warmInitialization implements Initialization {


    protected final Problem problem;
    public warmInitialization(Problem problem){
        super();
        this.problem = problem;
    }


    @Override
    public Solution[] initialize(int populationSize) {

        List<int[]> actionList = new ArrayList<>();;

        String csvFile = "RLaction/Actions_(50-1-2)_(Seed1).csv";
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean skipHeader = true;

            while ((line = br.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue; // skip the CSV header line
                }
                String[] tokens = line.split(",");
                int[] row = new int[6];
                for (int var = 0; var < 6; var++) {
                    row[var] = Integer.parseInt(tokens[var + 1].trim());
                }
                actionList.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        Solution[] initialPopulation = new Solution[populationSize];


        for (int i = 0; i < populationSize; i++) {
            Solution solution = problem.newSolution();

            if (i < 1){
                for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                    solution.getVariable(j).randomize();


                    if (j < (actionList.size()*6)){
                        int day = j / 6;
                        int var = j % 6;
                        int action = actionList.get(day)[var];
                        ((BinaryIntegerVariable)solution.getVariable(j)).setValue(action);
                    }
                }
            }
            else {
                for (int j = 0; j < solution.getNumberOfVariables(); j++) {
                    solution.getVariable(j).randomize();}
            }


            initialPopulation[i] = solution;
        }

        return initialPopulation;
    }

}
