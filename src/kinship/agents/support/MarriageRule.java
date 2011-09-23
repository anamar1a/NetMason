package kinship.agents.support;

import java.util.Set;

import kinship.agents.Household;
import kinship.agents.Person;
import kinship.model.KinshipModel;
import sim.engine.SimState;
import sim.util.Int2D;

public class MarriageRule implements Cloneable {
	
	public String name;
	
	public double percentEliminated; 
	
	public KinshipModel model;
	
	public MarriageRule(KinshipModel model) {
		this.model = model;
	}

	public void apply(Set<Person> set, Person target) {
		
	}
	
	public double getMateFitness(Person p1, Person p2) {
		return 0;
	}
	
	public void applyToObservations(Set<Person> fesibleChoices) {
		
	}
	
	protected double findMin(double[] minArray) {
		double min = minArray[0];

		for (int i = 1; i < minArray.length; i++) {
			if (minArray[i] <= min) {
				min = minArray[i];
			}
		}

		return min;
	}
	

}
