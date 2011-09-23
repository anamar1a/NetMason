package kinship.agents;

import java.util.HashMap;
import java.util.HashSet;

import kinship.model.KinshipModel;
import kinship.support.MarriageOffer;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;

public class Household implements Steppable {

	private static final long serialVersionUID = -4464137454168076541L;

	public final KinshipModel myWorld;

	public HashMap<Household, Int2D> neighbours = new HashMap<Household, Int2D>();

	public Int2D location;

	public boolean stillExists = true;

	public int id;

	public HashSet<Person> spouses;
	
	public Person headOfHousehold;
	
	public HashSet<Person> offspring;
	
	public HashSet<Household> buds;

	String name = "";

	public double wealth;
	
	public Village myVillage;
	
	public Clan clan;

	public Household(KinshipModel householdWorld, Person headOfHousehold) {

		this.myWorld = householdWorld;
		this.headOfHousehold = headOfHousehold;
		
		headOfHousehold.household = this;
		
		
		// Switch caste for a founder (depends on local castes in the village?)
		
	}

	public void step(final SimState state) {
		
		if (spouses.isEmpty()) {
			// Search for mate
		}
		
		/* Remove children which have since moved out. */ 
		
		HashSet<Person> toRemoveSet = new HashSet<Person>();
		
		for (Person p: this.offspring) {
			if (p.household != this) {
				toRemoveSet.add(p);
			}
		}
		
		this.offspring.removeAll(toRemoveSet);
		
		// Wealth dynamics
		
		/* If wealth is sufficient, allow adult members to establish a new Household. */
		
		
		for (Person p: this.offspring) {
			if ((p.age > 18) && (this.wealth > 30)) {
				this.buds.add(new Household(myWorld, p));	
			}
		}
		
		

	}
	
	public boolean considerProposal(Person female, MarriageOffer offer) {
		return stillExists;
		// Accept offer or reject and wait for better
	}

	
	public int getKinshipDistanceTo(Household target) {
		return myWorld.matchMaker.getKinshipDistanceFromTo(this.headOfHousehold, target.headOfHousehold);
	}

	public void removePerson(Person person) {
		/* What happens to Household where head of household dies?. */
		
	}
	
	public void searchForMate(Person p) {
		
		Person match = this.myWorld.matchMaker.findMateFor(p);
		
		if (match != null) {
			p.household.spouses.add(match);
		}
		
		// Make sure to relink links for female 
		
	}


}
