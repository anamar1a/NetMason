package kinship.agents;

import java.util.HashMap;
import java.util.HashSet;

import kinship.agents.support.MarriageRule;
import kinship.agents.support.RuleAgent;
import kinship.model.KinshipModel;
import kinship.support.KinshipEdge;
import kinship.support.TruncatedShortestPath;
import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class MatchMaker extends RuleAgent implements Steppable {

	private static final long serialVersionUID = 1L;

	public UndirectedSparseGraph<Person, KinshipEdge> kinshipGraph;

	public kinship.support.TruncatedShortestPath<Person, KinshipEdge> distanceFinder;

	// Read from file and instansiate

	public HashSet<Person> availableFemales;

	public enum Caste {
		A, B, C
	};

	public HashMap<Caste, Integer> castePreferences;

	private int kinshipHorizon;

	private KinshipModel myWorld;

	// Figure out efficent storage for pool of available females
	// This might be a separate class, maybe it can have a method which returns
	// a potential (list of) match withing certain location physical radius or
	// with pribability depending on physical distance

	public MatchMaker(KinshipModel myWorld) {

		this.myWorld = myWorld;
		this.kinshipGraph = new UndirectedSparseGraph<Person, KinshipEdge>();
		this.distanceFinder = new TruncatedShortestPath<Person, KinshipEdge>(this.kinshipGraph, myWorld);
		this.kinshipHorizon = 3 * myWorld.parameterMap.get("splitRadius").intValue();
	}

	public void step(SimState state) {

		/*
		 * Clean up dead and old Households and recalculate shortest distances
		 * and common relatives intersection sets
		 */

		if (myWorld.random.nextDouble() < myWorld.parameterMap.get("clanActivity")) {
			this.cleanDeadHouseholds();
			this.distanceFinder.reset();
		}

	}

	public void cleanDeadHouseholds() {

		int multiplier = myWorld.parameterMap.get("ancestryDepth").intValue();

		double thresholdDate = myWorld.schedule.getTime() - multiplier * myWorld.parameterMap.get("meanAge");

		HashSet<Person> toRemove = new HashSet<Person>();

		for (Person h : this.myWorld.deadHouseholds) {

			if (h.birthDate < thresholdDate) {
				toRemove.add(h);
			}

		}

		for (Person h : toRemove) {
			myWorld.deadHouseholds.remove(h);
			kinshipGraph.removeVertex(h);
		}

	}

	public int getKinshipDistanceFromTo(Person source, Person target) {

		if (target.equals(source)) {
			return 0;
		} else {

			Number distance = distanceFinder.getDistance(source, target);
			if ((distance == null) || (distance.intValue() < 0)) {
				return this.kinshipHorizon;
			} else {
				return distance.intValue();
			}
		}
	}

	public Person findMateFor(Person male) {

		HashSet<Person> feasibleChoices = new HashSet<Person>();
		feasibleChoices.addAll(availableFemales);

		for (MarriageRule r : this.myRules) {
			r.apply(feasibleChoices, male);
		}

		if (feasibleChoices.size() > 0) {
			Person[] choices = new Person[feasibleChoices.size()];
			choices = feasibleChoices.toArray(choices);
			return choices[myWorld.random.nextInt(choices.length)];
		} else {
			return null;
		}

	}

}
