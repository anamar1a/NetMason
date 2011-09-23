package kinship.agents.support;

import java.util.Set;
import java.util.TreeSet;

import kinship.agents.Household;
import kinship.agents.Person;
import kinship.model.KinshipModel;
import kinship.support.ValuedPerson;
import sim.engine.SimState;
import sim.util.Int2D;

public class RuleKinshipConstraint extends MarriageRule {

	public RuleKinshipConstraint(KinshipModel hb) {
		super(hb);
	}

	public void apply(Set<Person> set, Person target) {

		TreeSet<ValuedPerson> orderedPositions = new TreeSet<ValuedPerson>();

		for (Person p : set) {
			orderedPositions.add(new ValuedPerson(p, this.getMateFitness(p, target)));
		}

		int size = orderedPositions.size();
		ValuedPerson[] tempArray = new ValuedPerson[size];
		tempArray = orderedPositions.toArray(tempArray);

		for (int i = 0; i < this.percentEliminated * size; i++) {
			set.remove(tempArray[i].person);
		}

	}

	public double getMateFitness(Person p1, Person p2) {

		// TODO: Double check whether preference is supposed to be monotonic

		int distance = this.model.matchMaker.getKinshipDistanceFromTo(p1, p2);

		if (distance < 5) {
			return 0;
		} else {
			return distance;
		}
	}

}
