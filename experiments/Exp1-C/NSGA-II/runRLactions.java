import org.moeaframework.core.population.Population;
import org.moeaframework.core.variable.BinaryIntegerVariable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class runRLactions {

    public static void main(String[] args) {

        Population result;
        mySim sim = new mySim();
        sim.init();
        sim.reset(50, 1, 2, 450, 1, 14, 2, 1);


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




        System.out.println(sim.root.cashInHand);
        int day = 0;
        while (!sim.isDone() && day<40){
            System.out.println(actionList.get(day)[0]+"--"+actionList.get(day)[1]+"--"+actionList.get(day)[2]+"--"+actionList.get(day)[3]+"--"+actionList.get(day)[4]+"--"+actionList.get(day)[5]);
            sim.step(actionList.get(day));
            System.out.println("Cash: "+sim.root.cashInHand+"  Time: "+ sim.root.time()+"  Progress:  "+ sim.root.progress*100);
            day += 1;
        }

    }
}
