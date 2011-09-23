package netmason.main.agents.behaviours.optimizer;

import netmason.main.agents.Strategist;

/**
 * @author  Maciek
 */
public class Strategy {

	public double[] probabilities;

	public double profit;

	/**
	 * @uml.property  name="myAgent"
	 * @uml.associationEnd  
	 */
	Strategist myAgent;

	public Strategy(double[] probabilities, Strategist myAgent) {

		this.myAgent = myAgent;
		this.probabilities = probabilities;

	}

	public Strategy(Strategist myAgent) {

		this.myAgent = myAgent;

		int dimensionality = myAgent.myModel.projectList.get(myAgent.agentID).size();
		probabilities = new double[dimensionality];

		for (int d = 0; d < dimensionality; d++) {
			probabilities[d] = (double) 1 / dimensionality;
		}

	}

	public double getDistanceTo(Strategy tempStrategy) {

		double distance = 0;

		for (int i = 0; i < probabilities.length; i++) {
			distance = distance + Math.abs(this.probabilities[i] - tempStrategy.probabilities[i]);
		}

		return distance;
	}

	public String toString() {

		String temp = "";
		for (int i = 0; i < probabilities.length; i++) {
			temp = temp + " p[" + i + "]: " + +this.probabilities[i];
		}

		return temp;
	}

	public double getDimension(int i) {

		if (i > this.probabilities.length - 1) {
			return 0;
		} else {
			return this.probabilities[i];
		}

	}

	public void normalize() {
		double length = 0;

		for (int i = 0; i < probabilities.length; i++) {
			length = length + this.probabilities[i];
		}

		for (int i = 0; i < probabilities.length; i++) {
			this.probabilities[i] = this.probabilities[i] / length;
		}

	}

	public void randomize() {

		this.probabilities = new double[myAgent.myModel.projectList.get(myAgent.agentID).size()];

		for (int i = 0; i < probabilities.length; i++) {
			this.probabilities[i] = myAgent.myModel.random.nextDouble();
		}

		this.normalize();
	}

	public int drawFromDistribution() {

		int index = 0;
		int coordinate = 0;

		this.normalize();

		double rand = myAgent.myModel.random.nextDouble();

		double totalSum = 0;
		double previousSum = 0;

		for (coordinate = 0; coordinate < probabilities.length; coordinate++) {

			previousSum = totalSum;
			totalSum = totalSum + probabilities[coordinate];

			if ((totalSum >= rand) && (previousSum <= rand)) {
				index = coordinate;
				break;
			}

		}

		return index;
	}

}
