package netmason.main.agents.behaviours.optimizer;

import java.util.HashMap;
import java.util.TreeMap;

import netmason.main.agents.Strategist;
import netmason.model.NetMason;
import netmason.model.probes.StrategyProbe;


import com.oat.AlgorithmExecutor;
import com.oat.domains.cfo.CFODomain;
import com.oat.domains.cfo.algorithms.evolution.RealValueGeneticAlgorithm;
import com.oat.probes.BestSolutionProbe;
import com.oat.stopcondition.EvaluationsStopCondition;

/**
 * @author  Maciek
 */
public class StrategyOptimizer {

	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	public NetMason myModel;

	/**
	 * @uml.property  name="domain"
	 * @uml.associationEnd  
	 */
	public CFODomain domain;

	public TreeMap<Double, Strategy> searchHistory;

	/**
	 * @uml.property  name="myAgent"
	 * @uml.associationEnd  
	 */
	public Strategist myAgent;

	public StrategyOptimizer(Strategist myAgent) {

		this.myAgent = myAgent;
		this.myModel = myAgent.myModel;
		domain = new CFODomain();

	}

	public Strategy generateNewStrategy() {

		if (myAgent.level == 1) {
			return myAgent.getStrategy();
		} else {

			double startTime = System.currentTimeMillis();

			/*
			 * Recover strategies of other strategic agents for which best
			 * response will be computed.
			 */

			NetMason alternativeUniverse = myAgent.myCloner.createModelFromBeliefs(myAgent);

			StrategyProbe probe = new StrategyProbe(alternativeUniverse);
			alternativeUniverse.attachProbe(probe);

			alternativeUniverse.start();
			while (alternativeUniverse.schedule.getTime() < myAgent.planningHorizon) {
				alternativeUniverse.schedule.step(alternativeUniverse);
			}

			HashMap<Integer, Strategy> subGameSolutions = probe.getStrategies();

			/*
			 * Instantiate and execute Problem using pre-calculated strategies
			 * of Strategists.
			 */

			Problem problem = new Problem(myModel, myAgent, this, subGameSolutions);

			searchHistory = new TreeMap<Double, Strategy>();
			EvaluationsStopCondition stopCondition = new EvaluationsStopCondition(new Integer(myModel.properties.get("maxNumEvaluations")));
			RealValueGeneticAlgorithm algorithm = new RealValueGeneticAlgorithm();

			algorithm.setSeed(myModel.random.nextLong());
			algorithm.setPopsize(100);
			algorithm.setMutation(0.05);

			AlgorithmExecutor executor = new AlgorithmExecutor(problem, algorithm, stopCondition);

			BestSolutionProbe probe1 = new BestSolutionProbe();
			executor.addRunProbe(probe1);

			try {
				executor.executeAndWait();
			} catch (Exception e) {
				e.printStackTrace();
			}

			myAgent.predictedProfit = searchHistory.lastKey();
			Strategy bestStrategy = searchHistory.get(searchHistory.lastKey());

			double endTime = System.currentTimeMillis();
			myAgent.timeComplexity = endTime - startTime;

			System.out.println(myAgent.level + " " + myAgent.agentID + " " + bestStrategy.toString() + " payoffs " + myAgent.predictedProfit);

			return bestStrategy;
		}
	}

}
