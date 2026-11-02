
public class simpleRun {

    public static void main(String[] args){

        for(int i =0; i<1; i++){
            mySim sim = new mySim();
            sim.init();
            sim.reset(100, 4, 5, 450,1,14,2, 2);

            System.out.println((int) sim.root.time());
            sim.showObs();
            System.out.println(sim.root.cashInHand);

            while(!sim.isDone()){

                int[] myActions = {4, 5, 6, 7, 8, 9};
                sim.step(myActions);
                System.out.println((int) sim.root.time());
                sim.showObs();
                System.out.println(sim.root.cashInHand);

            }
        }
    }
}
