package kinship.agents;

import java.util.HashSet;

import kinship.agents.MatchMaker.Caste;
import kinship.model.KinshipModel;
import kinship.support.KinshipEdge;
import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.graph.util.EdgeType;

public class Person implements Steppable {

	public Household household;

	HashSet<Person> children;

	public Person father;

	public Person mother;

	public HashSet<Person> spouses;

	public KinshipModel model;

	public Caste caste;

	public Clan clan;

	public double birthDate;

	public double age;

	public boolean female;

	public boolean stillExists;

	public String id;

	public Person(KinshipModel model, Person father, Person mother) {

		this.model = model;

		// Subclass person into male female

		this.father = father;
		this.mother = mother;

		/*
		 * Centralize to make sure edges both direction are added, enumeration,
		 * check with JUNG which is the most efficient way for filtering (field
		 * / subclass).
		 */

		this.model.matchMaker.kinshipGraph.addEdge(new KinshipEdge("put_type_here"), father, this, EdgeType.UNDIRECTED);
		this.model.matchMaker.kinshipGraph.addEdge(new KinshipEdge("put_type_here"), mother, this, EdgeType.UNDIRECTED);

		// Inherit caste

		if (father != null) {
			this.caste = father.caste;
			this.household = father.household;
		} else {
			this.caste = Caste.values()[model.random.nextInt(Caste.values().length)];
			this.clan = new Clan(model, this);
			this.household = new Household(model, this);
		}

		this.birthDate = model.schedule.getTime();
		this.age = 0;
		this.female = model.random.nextBoolean();

	}

	public void completeDemographics() {

		/* If a female of age and is a spouse, give birth to child. */

		if ((this.female) && (this.age > 14) && (this.age < 30) && (this.household.spouses.contains(this))) {
			if (model.random.nextDouble() < 0.5) {
				this.household.offspring.add(new Person(model, this.household.headOfHousehold, this));
			}
		}

		// Age and die if too old

		this.age++;
		
		if (this.age > 30) {
			if (model.random.nextDouble() < 0.25) {
				this.household.removePerson(this);
				this.stillExists = false;
			}
		}
		
		// Put on market if female of age
		
		if ((this.age==12) && (this.female)) {
			model.matchMaker.availableFemales.add(this);
		}

	}

	public void step(SimState state) {

		this.completeDemographics();

	}

}
