/* This is our agent class. It implements MASON's Steppable interface 
 * (which allow us to use MASON schedule to direct activations as well
 *  as extends JUNG's SparseVertex class, allowing for exploiting JUNG to 
 *  operate and display the connections network. */

package netmason.main.agents;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import netmason.main.agents.behaviours.Chatter;
import netmason.main.agents.behaviours.Planner;
import netmason.main.messages.Message;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xnode;
import netmason.model.NetMason;
import netmason.support.DelegatedActivities;
import netmason.support.TimedStack;

import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * @author Maciek
 */
public class Operative extends Xnode implements Steppable {

	private static final long serialVersionUID = 1L;

	// Number of activations left
	public String subType = "Operative";

	// Graph with beliefs

	public UndirectedSparseGraph<Xnode, Xedge> beliefs = new UndirectedSparseGraph<Xnode, Xedge>();

	private DijkstraShortestPath<Xnode, Xedge> pathFinder = new DijkstraShortestPath<Xnode, Xedge>(beliefs);

	/*
	 * TODO - Note: the beliefsDirty flag is there for the pathFinder to know if
	 * it should reset it's cache, and should be updated each time the belief
	 * graph is modified (which addBelief does). To be be safe, we should
	 * encapsulate the whole beliefs graph in a Beliefs class, but's that's a
	 * high impact change... still: to be considered...
	 */
	protected boolean beliefsDirty = false;

	// Project stack
	/**
	 * @uml.property name="projectStack"
	 * @uml.associationEnd
	 */
	public TimedStack projectStack;

	// Tasks that can not be dealt myself

	/**
	 * @uml.property name="chatterStack"
	 * @uml.associationEnd
	 */
	public TimedStack chatterStack;

	// Tasks that can been delegated outside

	/**
	 * @uml.property name="delegatedTo"
	 * @uml.associationEnd
	 */
	public DelegatedActivities delegatedTo;

	/**
	 * @uml.property name="delegatedFrom"
	 * @uml.associationEnd
	 */
	public DelegatedActivities delegatedFrom;

	// Chatter manager

	/**
	 * @uml.property name="myChatter"
	 * @uml.associationEnd
	 */
	protected Chatter myChatter;

	public Queue<Message> inbox = new LinkedList<Message>();

	public Queue<Message> outbox = new LinkedList<Message>();

	// Planner manager

	/**
	 * @uml.property name="myPlaner"
	 * @uml.associationEnd
	 */
	public Planner myPlaner;

	public HashMap<String, String> scriptBase = new HashMap<String, String>();

	public Operative(String type, String name) {

		super(type, name);

		this.projectStack = new TimedStack(this);
		this.chatterStack = new TimedStack(this);

		this.delegatedTo = new DelegatedActivities();
		this.delegatedFrom = new DelegatedActivities();

		this.myChatter = new Chatter(this);
		this.myPlaner = new Planner(this);

	}

	public void reCreate() {

		/*
		 * Recreate method makes sure all fields are cleaned and initilized with
		 * proper information.
		 */

		// TODO: should clear() data structures instead of re-instanciating them
		this.projectStack = new TimedStack(this);
		this.chatterStack = new TimedStack(this);

		this.delegatedTo = new DelegatedActivities();
		this.delegatedFrom = new DelegatedActivities();

		this.myChatter = new Chatter(this);
		this.myPlaner = new Planner(this);

		/*
		 * Initialization of beliefs according to masterGraph, that has been
		 * read directly from database / xml file.
		 */

		this.beliefs = new UndirectedSparseGraph<Xnode, Xedge>();
		beliefsDirty = true;
		this.beliefs.addVertex(this);

		for (Xnode j : myModel.masterGraph.getNeighbors(this)) {

			this.beliefs.addVertex(j);

			Xedge newEdge = new Xedge();
			Xedge oldEdge = myModel.masterGraph.findEdge(this, j);

			for (String key : oldEdge.properties.keySet()) {
				newEdge.properties.put(key, oldEdge.properties.get(key));
			}

			this.beliefs.addEdge(newEdge, this, j);
		}
		

		this.beliefsDirty = true;

	}

	public void step(SimState state) {

		this.myChatter.readMessages();
		this.myPlaner.runPlanner();
		this.myChatter.chat();

		/*
		 * Connection operations have been finished. If it is still possible,
		 * agent will register itself on the schedule for next activation.
		 */

		// myModel.schedule.scheduleOnce(myModel.schedule.time() -
		// Math.log(myModel.random.nextDouble()), this);
	}

	public Xnode move(NetMason targetUniverse) {

		Operative derivative = new Operative(this.type, this.name);
		derivative.myModel = targetUniverse;

		return derivative;

	}

	public void expandTask(Xnode activity) {

		double t = myModel.schedule.getTime();
		if (activity.isType("task")) {
			projectStack.push(activity, t);
			for (Xnode n : myModel.planGraph.getSuccessors(activity)) {
				expandTask(n);
			}
		} else if (activity.isType("knowledge")) {
			projectStack.push(activity, t);
		} else if (activity.isType("resource")) {
			projectStack.push(activity, t);
		}

	}

	/**
	 * Adds a belief to the operative's belief network. This should be the ONLY
	 * way we modify the belief graph because it makes sure that the
	 * beliefsDirty flag is updated.
	 */
	public void addBelief(Xnode sourceNode, Xnode targetNode) {
		/*
		 * Note: no need to add vertices prior to the edge or to check for
		 * preexistence: addEdge() takes care of all that
		 */
		beliefs.addVertex(sourceNode);
		beliefs.addVertex(targetNode);
		if (!beliefs.isNeighbor(sourceNode, targetNode))
			beliefs.addEdge(new Xedge(), sourceNode, targetNode);
		beliefsDirty = true;
	}

	public List<Xedge> getPath(Xnode sourceNode, Xnode targetNode) {
		if (beliefsDirty) {
			//pathFinder.reset();
			//TODO This is a temporary fix to make the code work. Find out what the real problem is with reset().
			pathFinder = new DijkstraShortestPath<Xnode, Xedge>(beliefs);
			beliefsDirty = false;
		}
		return pathFinder.getPath(sourceNode, targetNode);
	}
}
