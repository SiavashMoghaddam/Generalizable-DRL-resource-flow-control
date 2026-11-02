import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.analysis.parameter.Enumeration;
import org.moeaframework.analysis.parameter.Parameter;
import org.moeaframework.analysis.parameter.ParameterSet;
import org.moeaframework.analysis.sample.SampledResults;
import org.moeaframework.analysis.sample.Samples;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Solution;
import org.moeaframework.core.constraint.Equal;
import org.moeaframework.core.objective.Maximize;
import org.moeaframework.core.objective.Minimize;
import org.moeaframework.core.population.NondominatedPopulation;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import org.moeaframework.parallel.DistributedProblem;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.problem.Problem;
import org.moeaframework.util.format.TableFormat;

import java.io.File;
import java.io.IOException;

public class opt_fixedSeed extends AbstractProblem {

    double j = 0;


    public opt_fixedSeed() {
        super(240, 2, 1);
    }

    public static void main(String[] args){


        Problem pr = new opt();
        Problem prDist = DistributedProblem.from(pr,6);
        NSGAII alg = new NSGAII(prDist);
        alg.setInitialPopulationSize(200);
        //Long a = PRNG.getRandom().nextLong();
        //System.out.println(a);
        PRNG.setSeed(5744905366068213525L);
        alg.setInitialization(new warmInitialization(prDist));



        alg.run(500_000);
        NondominatedPopulation result = alg.getResult();
        result.display();
        prDist.close();


        try {
            result.save(TableFormat.CSV, new File("Results/NSGAII_warm(500k)_FSeed.csv"));
            result.save(new File("Results/NSGAII_warm(500k)_FSeed.res"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


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
        //solution.setObjectiveValue(0, sim.root.progress);
        solution.setObjectiveValue(0, sim.root.time()/34);
        solution.setObjectiveValue(1, (double) sim.root.cashInHand /30900.0);



        solution.setConstraintValue(0, sim.root.progress);
        //solution.setConstraintValue(1, sim.root.cashInHand);

        sim.close();

        j += 1;
        if(j%1000 == 0)
        {
            System.out.println(j);
        }

    }

    @Override
    public Solution newSolution() {
        Solution solution = new Solution(240,2,1);

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

        solution.setObjective(0, new Minimize());
        solution.setObjective(1, new Maximize());
        //solution.setObjective(2, new Minimize());

        solution.setConstraint(0, Equal.to(1.0));
        //solution.setConstraint(1, GreaterThan.value(0.0));



        return  solution;
    }
}