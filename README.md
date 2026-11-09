This repository contains the source code, simulation environment, and trained models for the manuscript under review.:
## 📌 Overview
<p align="justify">
Construction projects rely on coupled resource flows that evolve stochastically, making their real-time control challenging. Existing scheduling approaches rarely capture flow interactions, adopt oversimplified representations, and remain tailored to single project instances, limiting their ability to manage resource flow variability and mitigate time and cost overruns. This study aims to address these limitations by developing a generalizable framework for adaptive real-time control of resource flows under uncertainty. To this end, the framework integrates Deep Reinforcement Learning (DRL) with Discrete Event Simulation (DES) for adaptive control of labor, material, and cash flows, formulating the control problem as a Markov Decision Process (MDP) to enable policy learning under uncertainty. A curriculum learning strategy is adopted to facilitate learning across increasingly complex tasks and promote generalization across diverse project configurations. A parameterized reinforced concrete building model, adjustable to varying configurations, serves as the validation case. Experimental results confirm that the proposed framework outperforms evolutionary algorithms and alternative DRL baselines in achieving timely project completion with stable cash flow dynamics. Furthermore, the trained policy generalizes its learned behavior effectively across diverse project configurations, encompassing both previously encountered and unseen instances, without requiring retraining. The proposed framework provides construction practitioners with a scalable, adaptive decision-support tool that enables data-driven daily decisions on resource management under project uncertainties.
</p>

## ⚙️ Prerequisites & System Requirements
To replicate the experiments and run the simulation–learning pipeline, ensure the following core software environments are installed:
- **Python:** `3.12.0`
- **Java Development Kit (JDK):** `17` (required for running exported AnyLogic simulation engines via Alpyne)
- **AnyLogic:** `8.9.0` (required for inspecting or modifying the `.alp` source model)

## Repository Structure
```text
├── README.md                  # Project overview, installation, and replication guide
├── requirements.txt           # Python dependencies
├── LICENSE                    # MIT License
├── agent/                     # Python source code: Custom Gymnasium environment & Alpyne connector, DRL training and inference scripts (PPO, A2C, SAC)
├── environment/               # AnyLogic model files (.alp, exported models)
└── experiments/               # Trained DRL models and GA and NSGA-II optimizations.
