import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.algorithm.single.GeneticAlgorithm;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.TypedProperties;
import org.moeaframework.core.objective.Maximize;
import org.moeaframework.core.population.NondominatedPopulation;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.problem.Problem;
import org.moeaframework.util.format.TableFormat;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class opt_test extends AbstractProblem {

    int j = 0;
    static double[] myData;

    public opt_test() {
        super(240, 1, 0);
    }

    public static void main(String[] args){


        Problem pr = new opt_test();
        NSGAII alg = new NSGAII(pr);
        alg.setInitialPopulationSize(200);
        PRNG.setSeed(1);

        //TypedProperties p = new TypedProperties();
        //p.setString("operator","hux+bf");
        //p.setDouble("bf.rate", 0.05);
        //alg.applyConfiguration(p);
        alg.getConfiguration().display();

//        myData = new double[20_000];

//        alg.run(20_000);
//        NondominatedPopulation result = alg.getResult();
//        result.display();
//        pr.close();


//        try {
//            result.save(TableFormat.CSV, new File("Results/NSGA_20k.csv"));
//            result.save(new File("Results/NSGA_20k.res"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

//        writeData();

    }



    @Override
    public void evaluate(Solution solution) {
        mySim sim = new mySim();
        sim.init();
        sim.reset(50, 1, 2, 450,1,14,2,1);
        int[][] actions = new int[40][6];
        for (int i = 0; i < 240; i++) {
            int day = i / 6;       // which of the 40 actions
            int var = i % 6;       // which variable within the action
            actions[day][var] = BinaryIntegerVariable.getInt(solution.getVariable(i));
        }

        int day = 0;
        while (!sim.isDone()){
            sim.step(actions[day]);
            day += 1;
        }
        solution.setObjectiveValue(0, sim.root.progress);
//        solution.setObjectiveValue(0, sim.root.time()/37);
//        solution.setObjectiveValue(1, (double) sim.root.cashInHand /30500.0);



        //solution.setConstraintValue(0, sim.root.progress);
        //solution.setConstraintValue(1, sim.root.cashInHand);

        sim.close();


        myData[j] = sim.root.progress * 100;
        if(j%200 == 0){
            System.out.println("Ep. Count = "+j+" - Progress = "+ Arrays.stream(myData).max());
        }
        j += 1;


    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(240,1,0);

        for (int i = 0; i < 240; i++) {
            int posInDay = i % 6;
            switch (posInDay) {
                case 0: // Var1
                case 1: // Var2
                case 2: // Var3
                    solution.setVariable(i, new BinaryIntegerVariable(0, 10));
                    break;
                case 3: // Var4
                case 4: // Var5
                    solution.setVariable(i, new BinaryIntegerVariable(0, 1));
                    break;
                case 5: // Var6
                    solution.setVariable(i, new BinaryIntegerVariable(0, 10));
                    break;
            }
        }

        solution.setObjective(0, new Maximize());
        //solution.setObjective(1, new Maximize());
        //solution.setObjective(2, new Minimize());

        //solution.setConstraint(0, Equal.to(1.0));
        //solution.setConstraint(1, GreaterThan.value(0.0));



        return  solution;
    }

    private static void writeData() {

        int rows = 200;
        int cols = myData.length / rows; // = 500 for 100k elements

        File file = new File("Results/Data_NSGA_20k.csv");

        try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {

            for (int r = 0; r < rows; r++) {

                StringBuilder line = new StringBuilder();

                for (int c = 0; c < cols; c++) {

                    int index = c * rows + r;  // take 200 at a time per column
                    line.append(myData[index]);

                    if (c < cols - 1)
                        line.append(",");
                }

                pw.println(line.toString());
            }

            System.out.println("CSV written: " + file.getAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}