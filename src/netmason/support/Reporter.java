package netmason.support;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import netmason.main.agents.Strategist;
import netmason.main.agents.behaviours.optimizer.Problem;
import netmason.main.agents.behaviours.optimizer.Strategy;
import netmason.model.NetMason;
import netmason.model.probes.StrategyProbe;


import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * @author  Maciek
 */
public class Reporter implements Steppable {

	private static final long serialVersionUID = 1L;

	BufferedWriter strategistWriter;

	BufferedWriter evalWriter;

	BufferedWriter runWriter;

	BufferedWriter fitnessWriter;

	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	NetMason myModel;

	public String prefix;

	public HashMap<Strategist, Integer> generationCounter = new HashMap<Strategist, Integer>();

	public Reporter(NetMason myModel) {
		this.myModel = myModel;
	}

	public void setup() {

		if (myModel.logging) {

			this.prefix = myModel.properties.get("prefix");

			try {

				strategistWriter = new BufferedWriter(new FileWriter(prefix + "_strategists.txt", true));
				runWriter = new BufferedWriter(new FileWriter(prefix + "_run.txt", true));
				evalWriter = new BufferedWriter(new FileWriter(prefix + "_evaluations.txt", true));
				fitnessWriter = new BufferedWriter(new FileWriter(prefix + "_fitness.txt", true));

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (myModel.runID == 1) {

				try {

					String pp = "prefix;runID;tick;";

					evalWriter.append(pp + "strategistID;generation;p1;p2;p3;predictedProfit");
					fitnessWriter.append(pp + "strategistID;p1;p2;p3;predictedProfit");
					strategistWriter.append(pp + "strategistID;p1;p2;p3;actionExecuted;actualProfit;predictedProfit;level;planningHorizon");
					runWriter.append("prefix;runID;numStrategists;scenarioName;beta;delta");

					runWriter.newLine();
					fitnessWriter.newLine();
					strategistWriter.newLine();
					evalWriter.newLine();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			String tempRun = "";
			tempRun = tempRun + this.prefix + ";";
			tempRun = tempRun + myModel.runID + ";";
			tempRun = tempRun + myModel.strategistList.size() + ";";
			tempRun = tempRun + myModel.runID + ";";
			tempRun = tempRun + myModel.properties.get("beta") + ";";
			tempRun = tempRun + myModel.properties.get("delta");

			try {
				runWriter.write(tempRun);
				runWriter.newLine();
				runWriter.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void recordStrategist(Strategist s) {

		if (myModel.logging) {

			String tempPrice = this.prefix + ";";
			tempPrice = tempPrice + myModel.runID + ";";
			tempPrice = tempPrice + myModel.schedule.getTime() + ";";
			tempPrice = tempPrice + s.agentID + ";";
			tempPrice = tempPrice + round(s.myStrategy.getDimension(0)) + ";";
			tempPrice = tempPrice + round(s.myStrategy.getDimension(1)) + ";";
			tempPrice = tempPrice + round(s.myStrategy.getDimension(2)) + ";";
			tempPrice = tempPrice + s.lastActionName + ";";
			tempPrice = tempPrice + round(s.actualProfit) + ";";
			tempPrice = tempPrice + round(s.predictedProfit) + ";";
			tempPrice = tempPrice + s.level + ";";
			tempPrice = tempPrice + s.planningHorizon + ";";

			try {
				strategistWriter.write(tempPrice);
				strategistWriter.newLine();
				strategistWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void record() throws IOException {

		/* Record market information */

		runWriter.flush();
		evalWriter.flush();
		fitnessWriter.flush();
	}

	public void flushAndCloseAll() {

		if (myModel.logging) {

			try {

				runWriter.flush();
				evalWriter.flush();
				strategistWriter.flush();
				evalWriter.flush();

				runWriter.close();
				evalWriter.close();
				strategistWriter.close();
				evalWriter.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static double round(double d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Double.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.doubleValue();
	}

	public static double round(double d) {
		return round(d, 2);
	}

	public void step(SimState state) {

		if (myModel.logging) {

			generationCounter = new HashMap<Strategist, Integer>();
		}
	}

	public void reportEvaluation(Strategist target, Strategy strategy, double fitness) {

		if (myModel.logging) {

			int generation = 0;

			if (this.generationCounter.containsKey(target)) {
				generation = this.generationCounter.get(target);
				generation++;
				this.generationCounter.put(target, generation);
			} else {
				this.generationCounter.put(target, generation);
			}

			String tempFit = "";
			tempFit = tempFit + this.prefix + ";";
			tempFit = tempFit + myModel.runID + ";";
			tempFit = tempFit + myModel.schedule.getTime() + ";";
			tempFit = tempFit + target.agentID + ";";
			tempFit = tempFit + generation + ";";
			tempFit = tempFit + strategy.getDimension(0) + ";";
			tempFit = tempFit + strategy.getDimension(1) + ";";
			tempFit = tempFit + strategy.getDimension(2) + ";";
			tempFit = tempFit + fitness;

			try {
				evalWriter.write(tempFit);
				evalWriter.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void reportFitnessLandscape(Strategist target) {

		if (myModel.logging) {

			Strategy tempStrategy = new Strategy(target);

			NetMason alternativeUniverse = target.myCloner.createModelFromBeliefs(target);

			StrategyProbe probe = new StrategyProbe(alternativeUniverse);
			alternativeUniverse.attachProbe(probe);

			alternativeUniverse.start();
			while (alternativeUniverse.schedule.getTime() < target.planningHorizon) {
				alternativeUniverse.schedule.step(alternativeUniverse);
			}

			HashMap<Integer, Strategy> subGameSolutions = probe.getStrategies();

			Problem problem = new Problem(myModel, target, null, subGameSolutions);

			for (int attempt = 0; attempt < 20000; attempt++) {

				tempStrategy.randomize();

				double fitness = problem.directFitness(tempStrategy, target);

				String tempFit = "";
				tempFit = tempFit + this.prefix + ";";
				tempFit = tempFit + myModel.runID + ";";
				tempFit = tempFit + myModel.schedule.getTime() + ";";
				tempFit = tempFit + target.agentID + ";";
				tempFit = tempFit + tempStrategy.getDimension(0) + ";";
				tempFit = tempFit + tempStrategy.getDimension(1) + ";";
				tempFit = tempFit + tempStrategy.getDimension(2) + ";";
				tempFit = tempFit + fitness;

				try {
					fitnessWriter.write(tempFit);
					fitnessWriter.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

}
