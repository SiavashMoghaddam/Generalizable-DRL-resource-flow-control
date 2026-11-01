# Generalizable-DRL-reource-flow-control
This repository contains the official source code, simulation environment, and trained models for the research paper:
> **Generalizable Multi-Objective Control of Construction Project Resource Flow Using Deep Reinforcement Learning**  
> Siavash Moghaddam, Amin Alvanchi, Masoud Zavari  
> *Engineering with Computers*  
## 📌 Overview
Construction projects rely on coupled resource flows that evolve stochastically, making their real-time control challenging. Existing scheduling approaches rarely capture flow interactions, adopt oversimplified representations, and remain tailored to single project instances, limiting their ability to manage resource flow variability and mitigate time and cost overruns. This study aims to address these limitations by developing a generalizable framework for adaptive real-time control of resource flows under uncertainty. To this end, the framework integrates Deep Reinforcement Learning (DRL) with Discrete Event Simulation (DES) for adaptive control of labor, material, and cash flows, formulating the control problem as a Markov Decision Process (MDP) to enable policy learning under uncertainty. A curriculum learning strategy is adopted to facilitate learning across increasingly complex tasks and promote generalization across diverse project configurations. A parameterized reinforced concrete building model, adjustable to varying configurations, serves as the validation case. Experimental results confirm that the proposed framework outperforms evolutionary algorithms and alternative DRL baselines in achieving timely project completion with stable cash flow dynamics. Furthermore, the trained policy generalizes its learned behavior effectively across diverse project configurations, encompassing both previously encountered and unseen instances, without requiring retraining. The proposed framework provides construction practitioners with a scalable, adaptive decision-support tool that enables data-driven daily decisions on resource management under project uncertainties.

## Repository Structure
```text
├── README.md                  # Project overview and instructions
├── requirements.txt           # Python dependencies
├── LICENSE                    # MIT License
├── agent/                     # Python source code (DRL agents, Gym environment)
├── environment/               # AnyLogic simulation models and interfaces
├── models/                    # Trained PPO model weights (.zip)
├── GA(java)/                  # GA and NSGA-II optimizations
└── results/                   # Experimental results used to create figures and tables
