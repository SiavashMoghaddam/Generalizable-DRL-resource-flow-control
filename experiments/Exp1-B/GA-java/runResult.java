import org.moeaframework.core.population.NondominatedPopulation;
import org.moeaframework.core.population.Population;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.core.variable.RealVariable;

import java.io.*;


public class runResult {

    public static void main(String[] args) {

        Population result;
        mySim sim = new mySim();
        sim.init();
        sim.reset(50, 1, 2, 450, 1, 14, 2, 10);
        try {
            result = Population.load(new File("Results/200k/200k_seed10.res"));
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

        System.out.println(sim.root.cashInHand);
        int day = 0;
        while (!sim.isDone() && day<40){
            System.out.println(actions[day][0]+"--"+actions[day][1]+"--"+actions[day][2]+"--"+actions[day][3]+"--"+actions[day][4]+"--"+actions[day][5]);
            sim.step(actions[day]);
            System.out.println("Cash: "+sim.root.cashInHand+"  Time: "+ sim.root.time()+"  Progress:  "+ sim.root.progress*100);
            day += 1;
        }

    }
}
