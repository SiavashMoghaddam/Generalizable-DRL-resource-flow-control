import com.anylogic.engine.Engine;
import singleproject9.*;
import java.io.IOException;
import java.util.Random;



public class mySim {
    Engine myEngine;
    Main root;
    double epsilon = 1e-8;

    int obsCapRebar, obsCapForm, obsCapConc, obsWaitingForMatRebar, obsWaitingForMatForm,obsWaitingForMatConc,
            obsWaitingForServRebar,obsWaitingForServForm,obsWaitingForServConc,obsStockRebar,
            obsStockForm, obsStockConc,obsAssedRebar,obsAssedForm,obsAssedConc,
            obsRebarOnWay,obsFormOnWay,obsWasteRebar,obsWasteForm, obsCashInHand, IobsRealOrderRebar, IobsRealOrderForm;
    double obsDoneRebar,obsDoneForm,obsDoneConc, obsProgress;


    public void init(){
        final CustomExperiment myExp = new CustomExperiment(null);
        myEngine = myExp.createEngine();

    }

    public void reset(int areaZone, int numZone, int numFloor, int payPerUnit,
                      int removeFormC, int removeFormBF, int storageCap, int seed){
        if(myEngine != null){
            myEngine.stop();}


        if(seed == -1) {
        }
        else if(seed == -2){
            Random r = new Random();
            myEngine.getDefaultRandomGenerator().setSeed((r.nextInt(Integer.MAX_VALUE)));
        }
        else {
            myEngine.getDefaultRandomGenerator().setSeed(seed);
        }



        root = new Main(myEngine,null,null);
        root.setParametersToDefaultValues();

        root.set_areaZone(areaZone);
        root.set_numZone(numZone);
        root.set_numFloor(numFloor);
        root.set_cashStart((int) (areaZone * numZone * payPerUnit * 1.2));
        root.set_payPerUnit(payPerUnit);
        root.set_storageCap(storageCap);
        root.set_removeFormC(removeFormC);
        root.set_removeFormBF(removeFormBF);

        myEngine.start(root);
    }

    public boolean isDone(){
        boolean done = false;
        if(root.isDone){
            done = true;
        }
        if(root.cashInHand < 0){
            done = true;
        }
        if(root.time() >= 40){
            done = true;
        }
//
        return done;
    }

    public boolean step(int[] action){
        int _actCapRebar = action[0];
        int _actCapForm = action[1];
        int _actCapConc = action[2];
        int _actMatRebar = action[3];
        int _actMatForm = action[4];
        int _actMatConc = action[5];

        root.takeAction(_actCapRebar,_actCapForm,_actCapConc,_actMatRebar,_actMatForm,_actMatConc);
        myEngine.runFast(root.time() + 1.0 + epsilon);

        return isDone();
    }

    public void close(){
        myEngine.stop();
    }

    public void showObs(){
        obsCapRebar = root.capRebar;
        obsCapForm = root.capForm;
        obsCapConc = root.capConc;
        obsWaitingForMatRebar = root.queRebarCUnit.size()+root.assRebarC.queueSize(1)
                + root.queRebarBFUnit.size()+root.assRebarBF.queueSize(1);
        obsWaitingForMatForm = root.queFormCUnit.size()+root.assFormC.queueSize(1)
                + root.queFormBFUnit.size()+root.assFormBF.queueSize(1);
        obsWaitingForMatConc = root.queConcCUnit.size()+root.assConcC.queueSize(1)
                + root.queConcBFUnit.size()+root.assConcBF.queueSize(1);
        obsWaitingForServRebar = root.serviceRebarC.queueSize()+root.serviceRebarC.delaySize()
                + root.serviceRebarBF.queueSize()+root.serviceRebarBF.delaySize();
        obsWaitingForServForm = root.serviceFormC.queueSize()+root.serviceFormC.delaySize()
                + root.serviceFormBF.queueSize()+root.serviceFormBF.delaySize();
        obsWaitingForServConc = root.serviceConcC.queueSize()+root.serviceConcC.delaySize()
                + root.serviceConcBF.queueSize()+root.serviceConcBF.delaySize();
        obsStockRebar = root.stockRebar;
        obsStockForm = root.stockForm;
        obsStockConc = root.stockConc;
        obsAssedRebar = (int) (root.assRebarC.out.count() + root.assRebarBF.out.count());
        obsAssedForm = (int) (root.assFormC.out.count()*10 + root.assFormBF.out.count()*10);
        obsAssedConc = (int) (root.assConcC.out.count()*10 + root.assConcBF.out.count()*10);
        obsDoneRebar = root.doneRebarC + root.doneRebarBF;
        obsDoneForm = root.doneFormC + root.doneFormBF;
        obsDoneConc = root.doneConcC + root.doneConcBF;
        obsRebarOnWay = root.rebarOnWay;
        obsFormOnWay = root.formOnWay;
        obsWasteRebar = root.wasteRebar;
        obsWasteForm = root.wasteForm;
        obsCashInHand = root.cashInHand;
        obsProgress = root.progress;
        IobsRealOrderRebar = root.RealOrderRebar;
        IobsRealOrderForm = root.RealOrderForm;

        System.out.println(
                "capRebar=" + obsCapRebar +
                        " | capForm=" + obsCapForm +
                        " | capConc=" + obsCapConc +
                        " | waitMatRebar=" + obsWaitingForMatRebar +
                        " | waitMatForm=" + obsWaitingForMatForm +
                        " | waitMatConc=" + obsWaitingForMatConc +
                        " | waitServRebar=" + obsWaitingForServRebar +
                        " | waitServForm=" + obsWaitingForServForm +
                        " | waitServConc=" + obsWaitingForServConc +
                        " | stockRebar=" + obsStockRebar +
                        " | stockForm=" + obsStockForm +
                        " | stockConc=" + obsStockConc +
                        " | assedRebar=" + obsAssedRebar +
                        " | assedForm=" + obsAssedForm +
                        " | assedConc=" + obsAssedConc +
                        " | doneRebar=" + obsDoneRebar +
                        " | doneForm=" + obsDoneForm +
                        " | doneConc=" + obsDoneConc +
                        " | rebarOnWay=" + obsRebarOnWay +
                        " | formOnWay=" + obsFormOnWay +
                        " | wasteRebar=" + obsWasteRebar +
                        " | wasteForm=" + obsWasteForm +
                        " | cashInHand=" + obsCashInHand +
                        " | progress=" + obsProgress +
                        " | realOrderRebar=" + IobsRealOrderRebar +
                        " | realOrderForm=" + IobsRealOrderForm
        );
    }

}