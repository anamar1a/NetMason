package kinship.graphing;

import java.util.ArrayList;

import kinship.agents.Person;
import kinship.model.KinshipModel;

import org.jfree.data.xy.XYSeries;

import sim.engine.SimState;
import sim.engine.Steppable;

public class GUIReporter implements Steppable {

	public int nextUpdate = 0;

	public int lastUpdate = 0;

	public XYSeries avgAgeSeries;

	public XYSeries maxAgeSeries;

	public XYSeries minAgeSeries;

	double[] ages = { 1, 2, 3 };

	public KinshipModel myModel;

	public GUIReporter(KinshipModelWithUI modelWithUI) {

		myModel = (KinshipModel) modelWithUI.state;

		minAgeSeries = new XYSeries("minAge");
		avgAgeSeries = new XYSeries("avgAge");
		maxAgeSeries = new XYSeries("maxAge");

	}

	public void step(SimState state) {

		/*
		 * Check if it is appropriate time to update, draw only if the display
		 * can be seen.
		 */

		double currentTime = state.schedule.getTime();

		if (currentTime >= nextUpdate) {

			double minAge = 100;
			double maxAge = 0;
			double avgAge = 0;

			ArrayList<Double> agesList = new ArrayList<Double>();

			for (Person p : myModel.matchMaker.kinshipGraph.getVertices()) {

				minAge = Math.min(minAge, p.age);
				maxAge = Math.max(maxAge, p.age);
				avgAge += p.age;

			}

			avgAgeSeries.add(currentTime, avgAge / myModel.matchMaker.kinshipGraph.getVertexCount());
			maxAgeSeries.add(currentTime, maxAge);

			/* Update histograms */

			Double[] tempArray = new Double[agesList.size()];
			tempArray = agesList.toArray(tempArray);
			ages = new double[agesList.size()];
			for (int i = 0; i < ages.length; i++) {
				ages[i] = tempArray[i].doubleValue();
			}

			nextUpdate = nextUpdate + 1;

		}
	}

}
