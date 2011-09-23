package netmason.main.agents;

import netmason.main.agents.behaviours.Chatter;
import netmason.main.agents.behaviours.Planner;
import netmason.main.agents.behaviours.optimizer.Strategy;
import netmason.main.agents.behaviours.optimizer.StrategyOptimizer;
import netmason.main.messages.TakeTask;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xnode;
import netmason.main.templates.Xtask;
import netmason.model.NetMason;
import netmason.model.probes.PayoffProbe;
import netmason.support.DelegatedActivities;
import netmason.support.ModelCloner;
import netmason.support.TimedStack;
import sim.engine.SimState;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * @author  Maciek
 */
public class Strategist extends Operative {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property  name="myOptimizer"
	 * @uml.associationEnd  
	 */
	public StrategyOptimizer myOptimizer;

	/**
	 * @uml.property  name="myCloner"
	 * @uml.associationEnd  
	 */
	public ModelCloner myCloner;

	/**
	 * @uml.property  name="myStrategy"
	 * @uml.associationEnd  
	 */
	public Strategy myStrategy;

	/* levels of recursion. 2: best response, 3: best response with respect to expectations of opponent's best response, ad infinitum. */
	public int level = 2;

	public double timeComplexity;

	public double planningHorizon = 50;

	public double predictedProfit = 0;

	public double actualProfit = 0;

	/**
	 * @uml.property  name="nextTask"
	 * @uml.associationEnd  
	 */
	public Xtask nextTask;

	public String lastActionName = "";

	public int agentID;

	/**
	 * @uml.property  name="myPayoffProbe"
	 * @uml.associationEnd  
	 */
	public PayoffProbe myPayoffProbe;

	public int remainingOptimizations = 100;

	public double lastOptimization = -50;

	public Strategist(String type, String name) {
		super(type, name);
		this.subType = "Strategist";

	}

	public void step(SimState state) {

		this.myChatter.readMessages();
		this.myChatter.chat();

		/*
		 * If there is something on stack, just deal with it. Otherwise initiate
		 * a project yourself.
		 */

		if (this.projectStack.size() > 0) {
			this.myPlaner.runPlanner();
		} else {

			this.actualProfit = this.myPayoffProbe.getPayoffOfAgent(this);

			myModel.myReporter.recordStrategist(this);

			if ((myModel.properties.get("fitnessLandscape").equalsIgnoreCase("true")) && (this.level == 2) && (this.agentID == 0)
					&& (this.myModel.schedule.getTime() > 900)) {
				myModel.myReporter.reportFitnessLandscape(this);
			}

			this.myPayoffProbe.clearMemory();

			if ((myModel.optimizing) && (this.remainingOptimizations > 0) && (this.lastOptimization < this.myModel.schedule.getTime() - this.planningHorizon)) {
				setStrategy(myOptimizer.generateNewStrategy());
				this.remainingOptimizations--;
				this.lastOptimization = myModel.schedule.getTime();
			}

			nextTask = this.myModel.projectList.get(this.agentID).get(this.myStrategy.drawFromDistribution());
			this.lastActionName = nextTask.name;

			inbox.add(new TakeTask(myModel.myMaster, this, nextTask));

		}

		/*
		 * Connection operations have been finished. If it is still possible,
		 * agent will register itself on the schedule for next activation.
		 */

		// myModel.schedule.scheduleOnce(myModel.schedule.time() -
		// Math.log(myModel.random.nextDouble()), this);
	}

	public Strategy getStrategy() {
		return this.myStrategy;
	}

	public void setStrategy(Strategy strategy) {
		this.myStrategy = strategy;

	}

	public void reCreate() {
		/* TODO: should probably call super.reCreate() instead of redoing it all */

		/*
		 * Recreate method makes sure all fields are cleaned and initilized with
		 * proper information.
		 */

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

		System.out.println(myModel.masterGraph.getNeighbors(this));
		
		for (Xnode j : myModel.masterGraph.getNeighbors(this)) {

			this.beliefs.addVertex(j);

			Xedge newEdge = new Xedge();
			Xedge oldEdge = myModel.masterGraph.findEdge(this, j);

			for (String key : oldEdge.properties.keySet()) {
				newEdge.properties.put(key, oldEdge.properties.get(key));
			}

			this.beliefs.addEdge(newEdge, this, j);
		}
		
		System.out.println(this.beliefs.getNeighbors(this));
		

		this.myOptimizer = new StrategyOptimizer(this);
		this.myCloner = new ModelCloner();
		this.myStrategy = new Strategy(this);
		this.myStrategy.randomize();

		this.myPayoffProbe = new PayoffProbe(myModel);
		myModel.attachProbe(myPayoffProbe);

		for (Strategist s : myModel.strategistList.values()) {
			if (!this.beliefs.containsVertex(s)) {
				this.beliefs.addVertex(s);
			}
		}
		
		this.beliefsDirty = true;

	}

	public Xnode move(NetMason targetUniverse) {

		Strategist derivative = new Strategist(this.type, this.name);

		derivative.myModel = targetUniverse;
		derivative.myStrategy = this.myStrategy;
		derivative.agentID = this.agentID;
		derivative.setStrategy(this.getStrategy());

		return derivative;

	}

}
