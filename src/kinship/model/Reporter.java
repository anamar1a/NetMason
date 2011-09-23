package kinship.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import kinship.agents.Clan;
import kinship.agents.Household;
import kinship.agents.Person;
import kinship.support.KinshipEdge;
import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class Reporter implements Steppable {

	private static final long serialVersionUID = 1L;

	public KinshipModel myWorld;

	BufferedWriter runWriter;

	BufferedWriter populationWriter;

	BufferedWriter agentWriter;

	BufferedWriter stateWriter;

	BufferedWriter linkWriter;

	BufferedWriter edgesWriter;

	BufferedWriter nodesWriter;

	public HashMap<Household, Integer> nodeMapping = new HashMap<Household, Integer>();

	public int maxNodeID = 0;

	String prefix;

	public double lastUpdate = 0;

	public Reporter(KinshipModel myWorld, String prefix) {
		this.myWorld = myWorld;
		this.prefix = prefix;
		this.setup();
		lastUpdate = System.currentTimeMillis();
	}

	public void step(SimState state) {

		if ((myWorld.schedule.getTime() == 1)) {

			String tempRun = "";
			tempRun = tempRun + this.prefix + "	";
			tempRun = tempRun + myWorld.runID;

			try {
				runWriter.write(tempRun);
				runWriter.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		try {

			int modulo = myWorld.parameterMap.get("reportingSkip").intValue();

			if (myWorld.schedule.getTime() % modulo == 0) {

				if (myWorld.random.nextDouble() < myWorld.parameterMap.get("proportionReported")) {
					for (Clan c : myWorld.society) {
						for (Household h : c.households) {
							reportHousehold(h);
						}
					}
				}

				double avgWealth = 0;
				double maxWealth = 0;
				double minWealth = 100000;
				int avgNumOffspring = 0;
				
				for (Clan c : myWorld.society) {

					double temp = 0;
					for (Household h : c.households) {
						temp += h.wealth;
						avgNumOffspring  += h.offspring.size();
					}

				

					avgWealth += temp;
					maxWealth = Math.max(maxWealth, temp);
					minWealth= Math.min(minWealth, temp);

				}

			

				if ((myWorld.schedule.getTime() > 200) || (myWorld.society.size() == 0)) {
					myWorld.finish();
					closeFiles();
					return;
				} else {

					String tempRun = "";
					tempRun = tempRun + this.prefix + "	";
					tempRun = tempRun + myWorld.runID + "	";
					tempRun = tempRun + (int) myWorld.schedule.getTime() + "	";
					tempRun = tempRun + (int) avgWealth / myWorld.society.size() + "	";
					tempRun = tempRun + (int) maxWealth + "	";
					tempRun = tempRun + (int) minWealth + "	";		
					tempRun = tempRun + myWorld.society.size() + "	";
					tempRun = tempRun + (int) ((System.currentTimeMillis() - this.lastUpdate)) + "	";
					tempRun = tempRun + (int) avgNumOffspring;

					populationWriter.write(tempRun);
					populationWriter.newLine();

				}

				this.lastUpdate = System.currentTimeMillis();

				populationWriter.flush();
				agentWriter.flush();

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void reportHousehold(Household h) {

		if ((h.stillExists) && (h.location != null)) {

			try {

				String tempH = "";
				tempH = tempH + this.prefix + "	";
				tempH = tempH + myWorld.runID + "	";
				tempH = tempH + (int) myWorld.schedule.getTime() + "	";
				tempH = tempH + h.id + "	";
				tempH = tempH + (int) h.wealth + "	";
				tempH = tempH + h.location.x + "	";
				tempH = tempH + h.location.y;

				agentWriter.write(tempH);
				agentWriter.newLine();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setup() {

		try {

			runWriter = new BufferedWriter(new FileWriter(prefix + "_run.txt", true));
			populationWriter = new BufferedWriter(new FileWriter(prefix + "_macro.txt", true));
			agentWriter = new BufferedWriter(new FileWriter(prefix + "_micro.txt", true));
			stateWriter = new BufferedWriter(new FileWriter(prefix + "_states.txt", true));
			linkWriter = new BufferedWriter(new FileWriter(prefix + "_links.txt", true));
			edgesWriter = new BufferedWriter(new FileWriter(prefix + "_edges.txt", true));
			nodesWriter = new BufferedWriter(new FileWriter(prefix + "_nodes.txt", true));

			if ((myWorld.runID == 1)) {

				runWriter.append("prefix	runID	landscape	eventSource	permutation	rule1	rule2	rule3	rule4	rule5	ruleEvolution	axis	order");
				stateWriter.append("idSource	time	x	y	size	clanID");
				linkWriter.append("idSource	idTarget	eventType");
				nodesWriter.append("prefix	runID	time	nodeID	nodeNum	color");
				edgesWriter.append("prefix	runID	time	aNum	bNum");

				String temp = "";
				temp = temp + "prefix" + "	";
				temp = temp + "runID" + "	";
				temp = temp + "time" + "	";
				temp = temp + "avgWealth" + "	";
				temp = temp + "maxWealth" + "	";
				temp = temp + "minWealth" + "	";
				temp = temp + "numOfClans" + "	";
				temp = temp + "simulationSpeed" + "	";
				temp = temp + "numOffspring";

				populationWriter.append(temp);

				temp = "";
				temp = temp + "prefix" + "	";
				temp = temp + "runID" + "	";
				temp = temp + "time" + "	";
				temp = temp + "myID" + "	";
				temp = temp + "myClanID" + "	";
				temp = temp + "myCampID" + "	";
				temp = temp + "herdSize" + "	";
				temp = temp + "x" + "	";
				temp = temp + "y" + "	";
				temp = temp + "age";

				agentWriter.append(temp);

				runWriter.newLine();
				populationWriter.newLine();
				agentWriter.newLine();
				stateWriter.newLine();
				linkWriter.newLine();
				edgesWriter.newLine();
				nodesWriter.newLine();

			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void closeFiles() {
		try {

			runWriter.flush();
			runWriter.close();

			populationWriter.flush();
			populationWriter.close();

			agentWriter.flush();
			agentWriter.close();

			stateWriter.flush();
			stateWriter.close();

			linkWriter.flush();
			linkWriter.close();

			nodesWriter.flush();
			nodesWriter.close();

			edgesWriter.flush();
			edgesWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void reportState(int nodeID, Household node) {

		if (myWorld.optionsMap.get("reportingDetail").equalsIgnoreCase("high")) {

			String tempH = "";
			tempH = tempH + nodeID + "	";
			tempH = tempH + myWorld.schedule.getTime() + "	";
			tempH = tempH + node.location.x + "	";
			tempH = tempH + node.location.y + "	";
			tempH = tempH + (int) node.wealth + "	";

			try {
				this.stateWriter.write(tempH);
				this.stateWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	public void reportLink(int sourceID, int targetID, String eventType) {

		if (myWorld.optionsMap.get("reportingDetail").equalsIgnoreCase("high")) {

			String tempH = "";
			tempH = tempH + sourceID + "	";
			tempH = tempH + targetID + "	";
			tempH = tempH + eventType;

			try {
				this.linkWriter.write(tempH);
				this.linkWriter.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public synchronized void reportHouseholdEvent(Household source, Household target, String event) {

		if (event.equalsIgnoreCase("gaveBirth")) {

			int oldSourceID = this.nodeMapping.get(source);
			int targetID = this.nodeMapping.get(target);

			int newSourceID = this.maxNodeID;
			this.nodeMapping.put(source, newSourceID);
			this.maxNodeID++;

			this.reportState(newSourceID, source);
			this.reportLink(oldSourceID, newSourceID, "was");

		} else if (event.equalsIgnoreCase("died")) {

			int oldSourceID = this.nodeMapping.get(source);
			int newSourceID = this.maxNodeID;
			this.nodeMapping.put(source, newSourceID);
			this.maxNodeID++;

			this.reportState(newSourceID, source);
			this.reportLink(oldSourceID, newSourceID, "die");

		} else if (event.equalsIgnoreCase("wasBorn")) {

			int newSourceID = this.maxNodeID;
			this.nodeMapping.put(source, this.maxNodeID);
			this.maxNodeID++;

			this.reportState(newSourceID, source);

		} else if (event.equalsIgnoreCase("married")) {

			int newSourceID = this.maxNodeID;
			int targetID = this.nodeMapping.get(target);
			this.nodeMapping.put(source, this.maxNodeID);
			this.maxNodeID++;

			this.reportState(newSourceID, source);
			this.reportLink(targetID, newSourceID, "mar");

		} else {

			int oldSourceID = this.nodeMapping.get(source);
			int newSourceID = this.maxNodeID;
			this.nodeMapping.put(source, newSourceID);
			this.maxNodeID++;

			this.reportState(newSourceID, source);
			this.reportLink(oldSourceID, newSourceID, "");

		}

	}

	public void flushAll() {
		try {

			runWriter.flush();

			populationWriter.flush();

			agentWriter.flush();

			stateWriter.flush();

			linkWriter.flush();

			edgesWriter.flush();

			nodesWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void saveGDFNetworkSnapshot() {

		try {
			BufferedWriter guessWriter = new BufferedWriter(new FileWriter(prefix + "_" + this.myWorld.runID + "_" + this.myWorld.schedule.getSteps()
					+ "_KIN.gdf", true));

			guessWriter.write("nodedef> name, width, height, color, strokecolor");
			guessWriter.newLine();

			for (Person h : myWorld.matchMaker.kinshipGraph.getVertices()) {

				if (h.stillExists) {
					guessWriter.write("v" + h.id + ",10,10,red,red");
					guessWriter.newLine();
				} else {
					guessWriter.write("v" + h.id + ",10,10,green,green");
					guessWriter.newLine();
				}
			}

			guessWriter.write("edgedef> node1,node2,color");
			guessWriter.newLine();

			UndirectedSparseGraph<Person, KinshipEdge> kinshipGraph = this.myWorld.matchMaker.kinshipGraph;

			for (KinshipEdge e : kinshipGraph.getEdges()) {

				Person h1 = kinshipGraph.getEndpoints(e).getFirst();
				Person h2 = kinshipGraph.getEndpoints(e).getSecond();

				/* Order relations such that resulting graph is directed. */

				if (h2.father == h1) {
					guessWriter.write("v" + h1.id + ",v" + h2.id + ",black");
					guessWriter.newLine();
				} else if (h1.father == h2) {
					guessWriter.write("v" + h2.id + ",v" + h1.id + ",black");
					guessWriter.newLine();
				} else if ((h1.spouses.contains(h2))) {
					guessWriter.write("v" + h1.id + ",v" + h2.id + ",black");
					guessWriter.newLine();
				} else {
					guessWriter.write("v" + h2.id + ",v" + h1.id + ",black");
					guessWriter.newLine();
				}

			}
			guessWriter.flush();
			guessWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
}
