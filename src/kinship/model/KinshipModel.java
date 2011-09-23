package kinship.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import kinship.agents.Clan;
import kinship.agents.Household;
import kinship.agents.MatchMaker;
import kinship.agents.Person;
import kinship.graphing.KinshipModelWithUI;
import sim.display.Console;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Int2D;

public class KinshipModel extends SimState
{

	public HashMap<String, Double> parameterMap = new HashMap<String, Double>();

	public HashMap<String, String> optionsMap = new HashMap<String, String>();

	public int maxClanId = 1;

	public int maxHouseholdId = 1;

	public int runID = 1;

	public boolean evolution = true;
	
	public SparseGrid2D houseGrid;

	public Reporter myReporter;

	public MatchMaker matchMaker;
	
	public TreeSet<Person> deadHouseholds;

	public ArrayList<Clan> society;
	
	public KinshipModel(long seed) {
		super(seed);
		// TODO Auto-generated constructor stub
	}
	
	public void registerReporter(String fileName) {

		myReporter = new Reporter(this, fileName);
		this.schedule.scheduleRepeating(myReporter);

	}
	
	public Int2D getLocation(Household mate) {

		return houseGrid.getObjectLocation(mate);

	}

	public void setLocation(Household mate, Int2D location) {

		houseGrid.setObjectLocation(mate, location);

	}

	public HashMap<Household, Int2D> getNeighbors(Int2D location, int radius) {

		Bag neighbours;

		neighbours = this.houseGrid.getNeighborsHamiltonianDistance(location.x, location.y, radius, false, null, null, null);
		HashMap<Household, Int2D> finalMap = new HashMap<Household, Int2D>();

		for (int i = 0; i < neighbours.size(); i++) {
			finalMap.put((Household) neighbours.get(i), this.houseGrid.getObjectLocation(((Household) neighbours.get(i))));
		}

		return finalMap;

	}

	public void removeHousehold(Household household) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {

		if (args.length > 0) {

			if (args[0].equalsIgnoreCase("-b")) {
				doLoop(KinshipModel.class, args);
				System.exit(0);

			} else {

				KinshipModelWithUI householdWorld = new KinshipModelWithUI();
				Console c = new Console(householdWorld);
				c.setBounds(600, 0, 425, 470);
				c.setVisible(true);
			}

		} else {
			KinshipModelWithUI householdWorld = new KinshipModelWithUI();
			Console c = new Console(householdWorld);
			c.setBounds(600, 0, 425, 470);
			c.setVisible(true);

		}

	}

}
