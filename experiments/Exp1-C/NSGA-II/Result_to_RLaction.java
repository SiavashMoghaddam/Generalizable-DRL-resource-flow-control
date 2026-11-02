import org.moeaframework.core.population.Population;
import org.moeaframework.core.variable.BinaryIntegerVariable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Result_to_RLaction {
    public static void main(String[] args) {

        Population result;
        try {
            result = Population.load(new File("Results/NSGAII_warm.res"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        result.display();




        int[][] actions = new int[40][6];
        for (int i = 0; i < 240; i++) {
            int day = i / 6;       // which of the 40 actions
            int var = i % 6;       // which variable within the action
            actions[day][var] = BinaryIntegerVariable.getInt(result.get(0).getVariable(i));
            //System.out.println(actions[day][var]);
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("Result_to_Action/NSGAII_warm.csv"))) {

            for (int day = 0; day < actions.length; day++) {

                StringBuilder line = new StringBuilder();

                for (int var = 0; var < actions[day].length; var++) {
                    line.append(actions[day][var]);

                    if (var < actions[day].length - 1)
                        line.append(",");
                }

                pw.println(line);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }




    }
}
