package kinship.agents.support;

import java.util.Set;
import java.util.TreeSet;

import kinship.agents.Person;
import kinship.model.KinshipModel;
import kinship.support.ValuedPerson;

public class RuleCasteConstraint extends MarriageRule {

	public RuleCasteConstraint(KinshipModel hb) {
		super(hb);
	}

	public void apply(Set<Person> set, Person target) {

		TreeSet<ValuedPerson> orderedPositions = new TreeSet<ValuedPerson>();

		for (Person p : set) {
			orderedPositions.add(new ValuedPerson(p, this.getMateFitness(p, target)));
		}

		while (orderedPositions.first().score < 1) {
			ValuedPerson vp = orderedPositions.pollFirst();
			set.remove(vp.person);
		}

	}

	public double getMateFitness(Person p1, Person p2) {

		// TODO: handle fuzziness in castes
		
		if (p1.caste == p2.caste) {
			return 1;
		} else {
			return 0;
		}
	}

}
