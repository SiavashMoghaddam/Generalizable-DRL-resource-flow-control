import numpy as np
from alpyne.env import AlpyneEnv
from alpyne.sim import AnyLogicSim
from gymnasium import spaces
from alpyne.data import SimStatus, SimConfiguration
from gymnasium.core import ActType, ObsType
from typing import SupportsFloat
from alpyne.typing import EngineSettingKeys
import typing
from typing import Any, SupportsFloat, Callable
from stable_baselines3.common.env_util import make_vec_env
from stable_baselines3 import SAC, PPO, DQN
import os
from stable_baselines3.common.vec_env.subproc_vec_env import SubprocVecEnv
from stable_baselines3.common.vec_env.dummy_vec_env import DummyVecEnv
from stable_baselines3.common.vec_env import VecNormalize
from stable_baselines3.common.monitor import Monitor
from stable_baselines3.common.evaluation import evaluate_policy
from stable_baselines3.common.vec_env import VecMonitor
from sb3_contrib import MaskablePPO
from stable_baselines3.common.callbacks import BaseCallback
from collections import deque
import ast
import matplotlib.pyplot as plt
from alpyne.utils import next_num
import pandas as pd
import secrets

class MyEnvironment(AlpyneEnv):
    
    def __init__(self, sim:AnyLogicSim):
        super().__init__(sim)
        self.observation_space = spaces.Box(low = 0.0, high= 1.0, shape=(27,), dtype=np.float32)
        self.action_space = spaces.Box(low=np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0]), high=np.array([10.0, 10.0, 10.0, 1.0, 1.0, 10.0]), dtype=np.float64)
        self.lastAction = None|ActType
        self.previousStatus = None|SimStatus
        self.finalProgress = None
        self.finalWasteRebar= None
        self.finalWasteForm= None
        
        self.areaZoneList = [50]
        self.numZoneList = [1,2,3,4]
        self.numFloorList = [2,3,4,5]


        # self.confs = [
        #     (50, 1, 2),
        #     (50, 1, 3),
        #     (50, 1, 4),
        #     (50, 1, 5),
        #     (50, 2, 2),
        #     (50, 3, 2),
        #     (50, 4, 2),
        # ]


        self.areaZone = None
        self.numZone  = None
        self.numFloor = None
        self.cashStart = None
        self.storageCap = 2.0
        self.payPerUnit = 450
        self.removeFormC = 1
        self.removeFormBF = 14

        self.termination = None
        


        self.rng = np.random.RandomState() 


    # def _get_config(self)-> SimConfiguration|None:


    #     self.areaZone = self.rng.choice(self.areaZoneList)
    #     #self.areaZone = 50
    #     self.numZone = self.rng.choice(self.numZoneList)
    #     #self.numZone = 1
    #     self.numFloor = self.rng.choice(self.numFloorList)
    #     #self.numFloor = 3
    
    #     # idx = self.rng.randint(0, len(self.confs))
    #     # self.areaZone, self.numZone, self.numFloor = self.confs[idx]
        
        
        
    #     self.termination = self.areaZone*  self.numZone*  self.numFloor
    #     self.cashStart = int(self.areaZone * self.numZone * self.payPerUnit * 1.2)
        

    #     myConfig = SimConfiguration(cnfAreaZone = self.areaZone,
    #                                 cnfNumZone = self.numZone,
    #                                 cnfNumFloor = self.numFloor,
    #                                 cnfCashStart = self.cashStart,
    #                                 cnfPayPerUnit = self.payPerUnit,
    #                                 cnfStorageCapacity = self.storageCap,
    #                                 cnfRemoveFormC = self.removeFormC,
    #                                 cnfRemoveFormBF = self.removeFormBF)
    
    #     return myConfig
    

        
    def _get_obs(self, status:SimStatus) -> ObsType:


        obsCapRebar = status.observation['obsCapRebar']/10
        obsCapForm = status.observation['obsCapForm']/10
        obsCapConc = status.observation['obsCapConc']/10

        obsWaitingForMatRebar = status.observation['obsWaitingForMatRebar']/(self.areaZone*self.numZone)
        obsWaitingForMatForm = status.observation['obsWaitingForMatForm']/(self.areaZone*self.numZone)
        obsWaitingForMatConc = status.observation['obsWaitingForMatConc']/(self.areaZone*self.numZone)

        obsWaitingForServRebar = status.observation['obsWaitingForServRebar']/(self.areaZone*self.numZone)
        obsWaitingForServForm = status.observation['obsWaitingForServForm']/(self.areaZone*self.numZone)
        obsWaitingForServConc = status.observation['obsWaitingForServConc']/(self.areaZone*self.numZone)

        obsStockRebar = status.observation['obsStockRebar']/(self.storageCap*self.areaZone*self.numZone*45)
        obsStockForm = status.observation['obsStockForm']/(self.storageCap*self.areaZone*self.numZone*1.6)
        obsStockConc = status.observation['obsStockConc']/(1*self.areaZone*self.numZone*0.4)


        obsAssedRebar = status.observation['obsAssedRebar'] / (self.areaZone*self.numZone*self.numFloor)/2
        obsAssedForm = status.observation['obsAssedForm'] / (self.areaZone*self.numZone*self.numFloor)/2
        obsAssedConc = status.observation['obsAssedConc'] / (self.areaZone*self.numZone*self.numFloor)/2
        
        obsDoneRebar = status.observation['obsDoneRebar'] / (self.areaZone*self.numZone*self.numFloor)/2
        obsDoneForm = status.observation['obsDoneForm'] / (self.areaZone*self.numZone*self.numFloor)/2
        obsDoneConc = status.observation['obsDoneConc'] / (self.areaZone*self.numZone*self.numFloor)/2
    
        obsRebarOnWay = np.clip(status.observation['obsRebarOnWay'] / (2*5000), a_min = 0 , a_max = 1)
        obsFormOnWay = np.clip(status.observation['obsFormOnWay'] / (2*50), a_min = 0 , a_max = 1)
        
        WasteRebar = status.observation['obsWasteRebar'] / (self.areaZone*self.numZone*self.numFloor)
        obsWasteRebar = WasteRebar/(1+WasteRebar)
        
        WasteForm = status.observation['obsWasteForm'] / (self.areaZone*self.numZone*self.numFloor)
        obsWasteForm = WasteForm/(1+WasteForm)
        
        obsCashInHand = np.clip(status.observation['obsCashInHand'] / (self.cashStart*2), a_min=0.0, a_max=1.0)
        
        obsProgress = status.observation['obsProgress']

        obsCnfAreaZone = self.areaZone / 200
        obsCnfNumZone = self.numZone / 4
        obsCnfNumFloor = self.numFloor / 5

        
        
        return np.array([obsCapRebar,
                         obsCapForm,
                         obsCapConc,
                         obsWaitingForMatRebar,
                         obsWaitingForMatForm,
                         obsWaitingForMatConc,
                         obsWaitingForServRebar,
                         obsWaitingForServForm,
                         obsWaitingForServConc,
                         obsStockRebar,
                         obsStockForm,
                         obsStockConc,
                         obsAssedRebar,
                         obsAssedForm,
                         obsAssedConc,
                         obsDoneRebar,
                         obsDoneForm,
                         obsDoneConc,
                         obsRebarOnWay,
                         obsFormOnWay,
                         obsWasteRebar,
                         obsWasteForm,
                         obsCashInHand,
                         obsProgress,
                         obsCnfAreaZone,
                         obsCnfNumZone,
                         obsCnfNumFloor])
        
    def _to_action(self, action:ActType) -> dict:
        self.lastAction = action
        return dict(actCapRebar = int(round(action[0])), 
                    actCapForm = int(round(action[1])), 
                    actCapConc = int(round(action[2])), 
                    actMatRebar = int(round(action[3])), 
                    actMatForm = int(round(action[4])), 
                    actMatConc = int(round(action[5])))
    
    
    def reset(self, *, seed: int | None = None, options: dict[str, Any] | None = None) -> tuple[
        ObsType, dict[str, Any]]:
        super().reset(seed=seed, options=options)

        config = SimConfiguration()
        config.update(**(self._get_config() or dict()))

        if options:

            detected_keys = typing.get_args(EngineSettingKeys)
            for name, value in options.items():
                # redefine engine settings fields with the ones in the settings
                if name in detected_keys:
                    setattr(self._sim.engine_settings, name, value)

                # override the current planned config
                if name in config:
                    config[name] = value

        # return type based on sim constructor
        status: SimStatus | None = self._sim.reset(config)
        if status is None:  # handle if auto_wait == False; we want the status when it's ready
            status = self._sim.lock()
        self.previousStatus = status
        return self._get_obs(status), self._get_info(status)

    
    def _calc_reward(self, status:SimStatus) -> SupportsFloat:
        

        # ── PARAMETERS ───────────────────────────────
        rew_done    = 100.0
        rew_assed    = 100.0
        rew_progress = 200.0
        bonus_complete = 100.0
        pen_time = 400.0 
        pen_fail = 100.0

        pen_doubleOrder = 10
        


        pen_waste = 5.0
        
        factor_cash = 0.25 #0.1

        # ── EXTRACT OBS ───────────────────────────────
        obs = status.observation
        prev = self.previousStatus.observation


        assedRebarToday = (obs['obsAssedRebar'] - prev['obsAssedRebar']) / (self.areaZone*self.numZone*self.numFloor)/2
        assedFormToday = (obs['obsAssedForm'] - prev['obsAssedForm']) / (self.areaZone*self.numZone*self.numFloor)/2
        assedConcToday = (obs['obsAssedConc'] - prev['obsAssedConc']) / (self.areaZone*self.numZone*self.numFloor)/2

        doneRebarToday = (obs['obsDoneRebar'] - prev['obsDoneRebar']) / (self.areaZone*self.numZone*self.numFloor)/2
        doneFormToday = (obs['obsDoneForm'] - prev['obsDoneForm']) / (self.areaZone*self.numZone*self.numFloor)/2
        doneConcToday = (obs['obsDoneConc'] - prev['obsDoneConc']) / (self.areaZone*self.numZone*self.numFloor)/2


        wasteRebarToday = (obs['obsWasteRebar'] - prev['obsWasteRebar'])/(self.areaZone*self.numZone*self.numFloor)
        wasteRebarToday = wasteRebarToday / (1 + wasteRebarToday)

        wasteFormToday = (obs['obsWasteForm'] - prev['obsWasteForm'])/(self.areaZone*self.numZone*self.numFloor)
        wasteFormToday = wasteFormToday / (1 + wasteFormToday)
        
        progressToday = obs['obsProgress'] - prev['obsProgress']

        cashChange = (obs['obsCashInHand'] - prev['obsCashInHand']) / (self.areaZone*self.numZone*self.numFloor)


        reward = 0.0
         
        reward -= (pen_time / (self.areaZone*self.numZone*self.numFloor))
        

        reward += rew_assed*(assedRebarToday + assedFormToday +assedConcToday)
        reward += rew_done*(doneRebarToday + doneFormToday + doneConcToday)
        reward += progressToday*rew_progress


        reward -= pen_waste*(wasteRebarToday+wasteFormToday)

        reward += cashChange*factor_cash
        
        if(obs['obsRebarOnWay'] > 5000):
            reward -= pen_doubleOrder
        if(obs['obsFormOnWay'] > 50):
            reward -= pen_doubleOrder

        if obs['obsProgress'] >= 1.0:
            reward += bonus_complete
            print("Success")
        if obs['obsCashInHand'] < 0.0:
            reward -= pen_fail
            print("Fail1")
        if status.time >= self.termination:
            reward -= pen_fail
            print("Fail2")
        
        self.finalProgress = status.observation["obsProgress"]
        finalWasteRebar = status.observation["obsWasteRebar"]/(self.areaZone*self.numZone*self.numFloor)
        self.finalWasteRebar = finalWasteRebar/(1+finalWasteRebar)
        finalWasteForm = status.observation["obsWasteForm"]/(self.areaZone*self.numZone*self.numFloor)
        self.finalWasteForm = finalWasteForm/(1+finalWasteForm)

        self.previousStatus = status
        
        return reward
    
    def _is_terminal(self, status:SimStatus) -> bool:
        return (status.stop or status.observation['obsCashInHand'] < 0.0)
    
    def _is_truncated(self, status: SimStatus) -> bool:
        truncated = (status.time >= self.termination)
        return truncated
    
    def close(self):
        super().close()
        self._sim._quit_app()


myenv = MyEnvironment(AnyLogicSim("D:/PhD/Thesis_2/4_Paper/3_Experiments/Exp1/Exp1A/RL/Exported/model.jar", 
                                  engine_overrides=dict(seed=secrets.randbits(32))))
model = PPO.load("Models_old/Model13", env=myenv)
myData = []
totalOrderRebar = 0
totalOrderForm = 0
totalOrderConc = 0
totalRealOrderRebar = 0
totalRealOrderForm = 0
RealWasteRebar = 0
RealWasteForm = 0
areaZoneList = [50,100,150,200]
numZoneList = [1,2,3,4]
numFloorList = [2,3,4,5]
completed = 0
for i in areaZoneList:
    for j in numZoneList:
        for k in numFloorList:
            myenv.areaZone = i
            myenv.numZone = j
            myenv.numFloor = k
            myenv.payPerUnit = 450
            myenv.cashStart = int(i*j*450*1.2)
            myenv.storageCap = 2.0
            myenv.removeFormC = 1
            myenv.removeFormBF = 14
            myenv.termination = i*j*k

            conf = dict(cnfAreaZone = myenv.areaZone,
                        cnfNumZone = myenv.numZone,
                        cnfNumFloor = myenv.numFloor,
                        cnfCashStart = myenv.cashStart,
                        cnfPayPerUnit = myenv.payPerUnit,
                        cnfStorageCapacity = myenv.storageCap,
                        cnfRemoveFormC = myenv.removeFormC,
                        cnfRemoveFormBF = myenv.removeFormBF)
            
            obs, _ = myenv.reset(options=conf)
            myenv._sim.reset(conf)
            done1 = done2 = False
            rewards = 0
            time = 0
            totalOrderRebar = 0
            totalOrderForm = 0
            totalOrderConc = 0
            totalRealOrderRebar = 0
            totalRealOrderForm = 0
            RealWasteRebar = 0
            RealWasteForm = 0
            #print("time: ", time,"   ", myenv._sim.observation())
            #print(obs)
            while not done1 and not done2:
                action = model.predict(obs, deterministic=True)[0]
                obs, reward, done1, done2, _ = myenv.step(action)
                #print(np.round(action))
                rewards += reward
                time += 1
                totalOrderRebar += int(round(action[3]))*5000
                totalOrderForm += int(round(action[4]))*50
                totalOrderConc += int(round(action[5]))*1

                totalRealOrderRebar += myenv._sim.observation()['IobsRealOrderRebar']
                totalRealOrderForm += myenv._sim.observation()['IobsRealOrderForm']

                #print("time: ", time,"   ", myenv._sim.observation())
                #print(obs)
            if obs[23]>=1:
                completed += 1
            print("time: ", time, "Reward: ", rewards, "Progress: ", obs[23]*100,"%")
            myData.append({"areaZone":myenv.areaZone,"numZone":myenv.numZone,"numFloor":myenv.numFloor,"Time":time, "Reward":rewards, "Progress":obs[23]*100, "Stock Rebar":myenv._sim.observation()['obsStockRebar'], "Stock Form":myenv._sim.observation()['obsStockForm'], 
                           "waste Rebar":myenv._sim.observation()['obsWasteRebar'],"waste Form":myenv._sim.observation()['obsWasteForm'],
                           "ordere Rebar":totalOrderRebar, "ordere Form":totalOrderForm, "Order Conc": totalOrderConc,
                            "Real ordere Rebar":totalRealOrderRebar, "Real ordere Form":totalRealOrderForm,
                            "Real Waste Reabr":max(0, (totalRealOrderRebar - myenv.areaZone*myenv.numZone*myenv.numFloor*45)),
                            "Real Waste Form":max(0, (totalRealOrderForm - myenv.areaZone*myenv.numZone*myenv.numFloor*1.6)),
                           "Cost per m2":(myenv.cashStart - myenv._sim.observation()['obsCashInHand'] + myenv.numFloor*myenv.payPerUnit*(myenv.areaZone*myenv.numZone))/(myenv.areaZone*myenv.numZone*myenv.numFloor),
                           "Duration per m2": (time)/(myenv.areaZone*myenv.numZone*myenv.numFloor)})
print(completed)
myenv.close()
            
df = pd.DataFrame(myData)
output_file = "Tests_exp/20251118+next_num.csv"
df.to_csv(output_file)


# filter out all runs with Progress < 1%
bad_runs = [
    (d["areaZone"], d["numZone"], d["numFloor"])
    for d in myData
    if d["Progress"] < 100.0
]

print("Runs with progress below 100%:")
for triple in bad_runs:
    print(triple)