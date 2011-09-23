package netmason.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import netmason.main.agents.Operative;
import netmason.main.agents.SimulationMaster;
import netmason.main.agents.Strategist;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xleaf;
import netmason.main.templates.Xnode;
import netmason.main.templates.Xtask;
import netmason.model.NetMason;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author  Maciek
 */
public class ModelCloner {

	/**
	 * @uml.property  name="myAgent"
	 * @uml.associationEnd  
	 */
	Operative myAgent;

	/**
	 * @uml.property  name="sourceUniverse"
	 * @uml.associationEnd  
	 */
	NetMason sourceUniverse;

	HashMap<Xnode, Xnode> forwardMap;

	public NetMason createModelFromBeliefs(Operative myAgent) {

		this.myAgent = myAgent;
		this.sourceUniverse = myAgent.myModel;

		NetMason targetUniverse = new NetMason(System.currentTimeMillis(), false);
		targetUniverse.logging = false;

		// Copy agents and connections between them from beliefs to target
		// masterGraph

		forwardMap = new HashMap<Xnode, Xnode>();

		for (Xnode sourceXnode : myAgent.beliefs.getVertices()) {
			Xnode targetXnode = sourceXnode.move(targetUniverse);
			targetUniverse.masterGraph.addVertex(targetXnode);
			forwardMap.put(sourceXnode, targetXnode);
		}

		for (Xedge sourceEdge : myAgent.beliefs.getEdges()) {
			Xedge targetXedge = new Xedge();
			targetUniverse.masterGraph.addEdge(targetXedge, forwardMap.get(myAgent.beliefs.getEndpoints(sourceEdge).getFirst()), forwardMap.get(myAgent.beliefs
					.getEndpoints(sourceEdge).getSecond()));
		}

		// Copy settings, plans

		copySettings(sourceUniverse, targetUniverse);

		// Create new SimulationMaster

		targetUniverse.myMaster = new SimulationMaster("master", "GameMaster");
		targetUniverse.myMaster.myModel = targetUniverse;

		/*
		 * Finalize construction by recreating beliefs of all new agents and
		 * adding agents to list for housekeeping.
		 */

		ArrayList<Operative> agentList = new ArrayList<Operative>();
		targetUniverse.nodeTable.put(Operative.class, agentList);

		for (Xnode tempXnode : targetUniverse.masterGraph.getVertices()) {
			if (tempXnode.type.equalsIgnoreCase("agent")) {
				((Operative) tempXnode).reCreate();
				agentList.add((Operative) tempXnode);
				if (((Operative) tempXnode).subType.equalsIgnoreCase("Strategist")) {
					targetUniverse.strategistList.put(((Strategist) tempXnode).agentID, (Strategist) tempXnode);
					((Strategist) tempXnode).level = Math.max(1, ((Strategist) myAgent).level - 1);
					((Strategist) tempXnode).remainingOptimizations = 1;
				}
			}
		}
		
		for (Strategist sourceStrategist: sourceUniverse.strategistList.values()) {
			
			Strategist targetStrategist = (Strategist) forwardMap.get(sourceStrategist);
			targetStrategist.setStrategy(sourceStrategist.getStrategy());
			
			
		}

		// Augment beliefs of all agents
		
		for (Xnode x : targetUniverse.masterGraph.getVertices()) {
			if (x.type.equalsIgnoreCase("agent")) {

				Operative tempOperative = (Operative) x;

				for (Xnode tempXnode : targetUniverse.masterGraph.getVertices()) {

					if (!tempOperative.beliefs.containsVertex(tempXnode)) {
						tempOperative.beliefs.addVertex(tempXnode);
					}
				}

				for (Xedge tempEdge : targetUniverse.masterGraph.getEdges()) {

					Xnode source = targetUniverse.masterGraph.getEndpoints(tempEdge).getFirst();
					Xnode target = targetUniverse.masterGraph.getEndpoints(tempEdge).getSecond();

					if (!tempOperative.beliefs.isNeighbor(source, target)) {
						tempOperative.beliefs.addEdge(new Xedge(), source, target);
					}

				}
			}
		}

		return targetUniverse;
	}

	private void copySettings(NetMason sourceUniverse, NetMason targetUniverse) {

		/*
		 * Due to superfluous prudency, we copy all possible objects and data
		 * structures. Most likely we could get away with copying just social
		 * network and beliefs.
		 */

		/* Copy plan library. */

		targetUniverse.planGraph = new DirectedSparseGraph<Xnode, Xedge>();

		for (Xnode x : sourceUniverse.planGraph.getVertices()) {

			if (!forwardMap.containsKey(x)) {
				Xnode targetX = x.move(targetUniverse);
				forwardMap.put(x, targetX);
			}

			targetUniverse.planGraph.addVertex(forwardMap.get(x));
		}

		for (Xedge sourceEdge : sourceUniverse.planGraph.getEdges()) {
			Xedge targetXedge = new Xedge();
			targetUniverse.planGraph.addEdge(targetXedge, forwardMap.get(sourceUniverse.planGraph.getSource(sourceEdge)), forwardMap
					.get(sourceUniverse.planGraph.getDest(sourceEdge)));
		}

		/* Copy properties. */

		targetUniverse.properties = sourceUniverse.properties;

		/* Copy project lists. */

		targetUniverse.projectList = new HashMap<Integer, ArrayList<Xtask>>();

		for (Strategist s : sourceUniverse.strategistList.values()) {
			ArrayList<Xtask> sourceTaskList = sourceUniverse.projectList.get(s.agentID);
			ArrayList<Xtask> targetTaskList = new ArrayList<Xtask>();

			targetUniverse.projectList.put(s.agentID, targetTaskList);
			for (Xtask sourceTask : sourceTaskList) {
				targetTaskList.add((Xtask) forwardMap.get(sourceTask));

			}
		}

		/* Copy payoff structure. */

		targetUniverse.payoffStructure = new HashMap<Vector<Xtask>, Xleaf>();

		for (Vector<Xtask> sourceKey : sourceUniverse.payoffStructure.keySet()) {

			Vector<Xtask> targetKey = new Vector<Xtask>();
			
			for (Xtask sourceTask: sourceKey) {
				targetKey.add((Xtask) forwardMap.get(sourceTask));
			}
			
			Xleaf targetLeaf = new Xleaf("leaf", "leaf");
			Xleaf sourceLeaf = sourceUniverse.payoffStructure.get(sourceKey);
			targetLeaf.payoffs = sourceLeaf.payoffs;
			

			targetUniverse.payoffStructure.put(targetKey, targetLeaf);

		}

	}
}
