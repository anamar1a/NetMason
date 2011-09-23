package netmason.main.agents.behaviours.optimizer;

import java.util.HashMap;

import netmason.main.agents.Strategist;
import netmason.model.NetMason;
import netmason.model.probes.PayoffProbe;


import com.oat.SolutionGenerator;
import com.oat.domains.cfo.CFOProblem;
import com.oat.domains.cfo.CFOSolution;

/**
 * @author  Maciek
 */
public class Problem extends CFOProblem implements SolutionGenerator {

	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	NetMason myModel;

	/**
	 * @uml.property  name="initialStrategy"
	 * @uml.associationEnd  
	 */
	Strategy initialStrategy;

	/**
	 * @uml.property  name="myAgent"
	 * @uml.associationEnd  
	 */
	public Strategist myAgent;

	/**
	 * @uml.property  name="myOptimizer"
	 * @uml.associationEnd  
	 */
	public StrategyOptimizer myOptimizer;

	public HashMap<Integer, Strategy> subGameSolutions;

	public Problem(NetMason myModel, Strategist myAgent, StrategyOptimizer optimizer, HashMap<Integer, Strategy> subGameSolutions) {

		super(myModel.projectList.get(myAgent.agentID).size());
		this.myModel = myModel;
		this.myAgent = myAgent;
		this.myOptimizer = optimizer;
		this.initialStrategy = myAgent.getStrategy();
		this.subGameSolutions = subGameSolutions;
		this.setDimensions(myModel.projectList.get(myAgent.agentID).size());

	}

	public SUPPORTED_DIMENSIONS[] getSupportDimensionality() {
		return new SUPPORTED_DIMENSIONS[] { SUPPORTED_DIMENSIONS.ANY };
	}

	protected double[][] prepareMinMax() {

		double[][] d = new double[dimensions][2];
		for (int i = 0; i < dimensions; i++) {
			d[i][0] = 0;
			d[i][1] = 1;
		}
		return d;

	}

	protected double[][] prepareOptima() {

		double[][] d = new double[1][dimensions];
		for (int i = 0; i < d[0].length; i++) {
			d[0][i] = 0.5 * (initialStrategy.getDimension(i));
		}
		return d;
	}

	public double problemSpecificCost(double[] v) {

		Strategy evaluatedStrategy = new Strategy(v, myAgent);

		double totalPayoff = this.directFitness(evaluatedStrategy, myAgent);

		totalPayoff = totalPayoff - Double.valueOf(myModel.properties.get("delta")) * evaluatedStrategy.getDistanceTo(myAgent.getStrategy());

		myOptimizer.searchHistory.put(totalPayoff, evaluatedStrategy);

		myModel.myReporter.reportEvaluation(myAgent, evaluatedStrategy, totalPayoff);

		return totalPayoff;
	}

	public double directFitness(Strategy v, Strategist s) {

		/*
		 * Multiple simulations are required as we operate in mixed, not pure
		 * strategies. Agents would maximize therefore expected payoff.
		 */

		double totalPayoff = 0;

		for (int j = 0; j < Double.valueOf(myModel.properties.get("numSamples")); j++) {

			/*
			 * Unless roll back function is implemented, set up a temporary
			 * universe to evaluate consequence of adopting v.
			 */

			NetMason alternativeUniverse = s.myCloner.createModelFromBeliefs(s);

			alternativeUniverse.optimizing = false;

			PayoffProbe probe = new PayoffProbe(alternativeUniverse);
			alternativeUniverse.attachProbe(probe);

			for (Integer i : this.subGameSolutions.keySet()) {
				alternativeUniverse.strategistList.get(i).setStrategy(this.subGameSolutions.get(i));
			}

			alternativeUniverse.strategistList.get(s.agentID).setStrategy(v);

			/*
			 * After setting strategies for all agents, perform a single draw
			 * from Strategy and see how it performs over certain planning
			 * horizon.
			 */

			alternativeUniverse.start();
			while (alternativeUniverse.schedule.getTime() < s.planningHorizon) {
				alternativeUniverse.schedule.step(alternativeUniverse);
			}
			totalPayoff += probe.getPayoffOfAgent(alternativeUniverse.strategistList.get(s.agentID));

		}

		totalPayoff /= Double.valueOf(myModel.properties.get("numSamples")) * s.planningHorizon;

		return totalPayoff;
	}

	public boolean isMinimization() {
		return false;
	}

	public String getName() {
		return "Market problem";
	}

	public CFOSolution generateSolution() {
		return null;
	}

}
