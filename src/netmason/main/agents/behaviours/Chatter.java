package netmason.main.agents.behaviours;

import java.util.Collection;

import netmason.main.agents.Operative;
import netmason.main.messages.AddFact;
import netmason.main.messages.InquireAbout;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xnode;

import ec.util.MersenneTwisterFast;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * @author Maciek
 */
public class Chatter {

	Operative agent;

	public Chatter(Operative myAgent) {
		this.agent = myAgent;
	}

	/** Passive communication: interpreting what others have send. */
	public void readMessages() {
		while (!agent.inbox.isEmpty())
			agent.inbox.remove().interpret();
	}

	/**
	 * Active communications: chatting about objects from chatterStack. This can
	 * include access to resources and people.
	 */
	public void chat() {
		if (agent.chatterStack.size() > 0) {
			inquireAboutNeededFact();
		} else {
			shareRandomFact();
		}
	}

	private void inquireAboutNeededFact() {

	
		/*
		 * Create InquireAbout message that will get broadcasted to all of the
		 * neighbors.
		 */
		for (Xnode n : agent.beliefs.getNeighbors(agent)) {
			if (n.isType("agent")) {
				Xnode objectOfInterest = agent.chatterStack.getRandomNode();
				agent.outbox.add(new InquireAbout(agent, (Operative) n, objectOfInterest));
			}
		}
	}

	private void shareRandomFact() {
		
		UndirectedSparseGraph<Xnode, Xedge> beliefs = agent.beliefs;

		Collection<Xnode> neighbors = beliefs.getNeighbors(agent);
		if (neighbors.size() <= 1) // TODO shouldn't it be '== 0'?
			return;

		MersenneTwisterFast random = agent.myModel.random;
		double socialActivity = Double.valueOf(agent.myModel.properties.get("socialActivity"));
		if (random.nextDouble() >= socialActivity)
			return;

		Xnode randomNeighbor = (Xnode) neighbors.toArray()[random.nextInt(neighbors.size())];
		if (!randomNeighbor.isType("agent"))
			return;

		Object[] edges = beliefs.getEdges().toArray();
		Xedge e = (Xedge) edges[random.nextInt(beliefs.getEdgeCount())];
		Pair<Xnode> endPoints = beliefs.getEndpoints(e);
		agent.outbox.add(new AddFact(agent, (Operative) randomNeighbor, endPoints.getFirst(), endPoints.getSecond()));
	}
}
