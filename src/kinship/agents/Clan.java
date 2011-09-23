package kinship.agents;

import java.awt.Paint;
import java.util.HashSet;

import kinship.model.KinshipModel;
import sim.engine.SimState;
import sim.engine.Steppable;

public class Clan implements Steppable {

	private static final long serialVersionUID = 5065000091778198862L;

	public HashSet<Household> households;

	public int id;

	public KinshipModel myWorld;

	public boolean stillExists = true;

	public int maxHouseholdId = 0;

	public double diameter;
	
	public Person founder;

	public Clan(KinshipModel kinshipModel, Person founder) {

		this.myWorld = kinshipModel;
		this.founder = founder;
		
		this.id = myWorld.maxClanId;
		myWorld.maxClanId++;

		myWorld.schedule.scheduleOnce(this, 2);
	
		households = new HashSet<Household>();

		this.diameter = 2 * myWorld.parameterMap.get("splitRadius");

	}

	public void step(SimState state) {



	}

	public void addHousehold(Household household) {
		this.households.add(household);
		household.clan = this;
	}


	public int getNumHouseholds() {
		return this.households.size();
	}

	public int myID() {
		return this.id;
	}

	public String toString() {
		String name = "Clan " + this.id;
		return name;
	}

	
	

	public double getAncestryDiameter() {

		if (households.size() <= 1) {
			return 0;
		} else {

			double sumOfDia = 0;
			int denominator = 0;

			for (Household h : households) {
				for (Household g : households) {
					if (!h.equals(g)) {
						sumOfDia += myWorld.matchMaker.getKinshipDistanceFromTo(h.headOfHousehold, g.headOfHousehold);
						denominator++;
					}
				}
			}

			return sumOfDia / denominator;
		}
	}

	public Paint getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
