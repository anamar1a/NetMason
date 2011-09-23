package netmason.model;

/* This demo shows how to use JUNG and MASON together. 
 * A number of agents will create and drop connections
 * between themselves, with this network of interactions 
 * described with JUNG graph. The agents will be actived
 * using Poisson activation scheme. */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import netmason.main.agents.MailMan;
import netmason.main.agents.Operative;
import netmason.main.agents.SimulationMaster;
import netmason.main.agents.Strategist;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xleaf;
import netmason.main.templates.Xnode;
import netmason.main.templates.Xtask;
import netmason.model.probes.ModelProbe;
import netmason.support.ModelFactory;
import netmason.support.Propertable;
import netmason.support.Reporter;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

/**
 * @author  Maciek
 */
public class NetMason extends SimState implements Propertable {

	public Hashtable<String, String> properties = new Hashtable<String, String>();

	public UndirectedSparseGraph<Xnode, Xedge> masterGraph = new UndirectedSparseGraph<Xnode, Xedge>();

	public DirectedSparseGraph<Xnode, Xedge> planGraph = new DirectedSparseGraph<Xnode, Xedge>();

	/**
	 * @uml.property  name="myMaster"
	 * @uml.associationEnd  
	 */
	public SimulationMaster myMaster;

	String buffer = "";

	public ArrayList<Steppable> futureActivations = new ArrayList<Steppable>();

	BufferedWriter logStream = null;

	public double socialActivity = 0.2;

	// TODO Question: Mapping from Integers is weird - why not just an ArrayList? (NP 2009-03-12)
	public HashMap<Integer, Strategist> strategistList = new HashMap<Integer, Strategist>();

	public HashMap<Integer, ArrayList<Xtask>> projectList = new HashMap<Integer, ArrayList<Xtask>>();

	public HashMap<Class, ArrayList> nodeTable = new HashMap<Class, ArrayList>();

	public boolean logging = true;

	public int runID = 1;

	public boolean optimizing = true;

	/**
	 * @uml.property  name="myReporter"
	 * @uml.associationEnd  
	 */
	public Reporter myReporter;

	/**
	 * @uml.property  name="myMailMan"
	 * @uml.associationEnd  
	 */
	public MailMan myMailMan;

	public HashMap<Vector<Xtask>, Xleaf> payoffStructure = new HashMap<Vector<Xtask>, Xleaf>();

	public NetMason(long seed) {
		super(seed);
		new ModelFactory(this);
		myReporter = new Reporter(this);
	}

	public NetMason(long seed, boolean b) {
		super(seed);
		myReporter = new Reporter(this);
	}

	public void start() {

		if (logging) {

			try {
				logStream = new BufferedWriter(new FileWriter("log.txt"));
				logStream.newLine();
				logStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			myReporter.setup();
		}

		super.start();

		schedule.reset();

		/*
		 * Create N agents, which will be added to the graph as nodes. The
		 * initial activations of out agents are scheduled here, all subsequent
		 * will be scheduled by agents themselves.
		 */

		Collection<Operative> agentList = this.nodeTable.get(Operative.class);

		for (Operative a : agentList) {
			this.schedule.scheduleRepeating(a, 1, 1.0);
		}

		/* Create MainMan agent */

		myMailMan = new MailMan(this);
		schedule.scheduleRepeating(1.0, (int) Schedule.MAXIMUM_INTEGER - 1, myMailMan);

		/* Create Final agent */

		final Steppable finalAgent = new Steppable() {
			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {
				myReporter.flushAndCloseAll();
				state.finish();
			}
		};

		schedule.scheduleOnce(Double.valueOf(this.properties.get("maxTime")), (int) Schedule.MAXIMUM_INTEGER, finalAgent);

		/* Create Master Agent */

		schedule.scheduleRepeating(1.0, (int) Schedule.MAXIMUM_INTEGER - 1, myMaster);

		schedule.scheduleRepeating(1.0, (int) Schedule.MAXIMUM_INTEGER - 2, myReporter);

	}

	public static void main(String[] args) {

		doLoop(NetMason.class, args);

		System.exit(0);
	}

	static final long serialVersionUID = -7164072518609011190L;

	public synchronized void print(String log) {

		if (logging) {

			try {
				logStream.write(log);
				logStream.newLine();
				logStream.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

			buffer = buffer + log + "\n";

		}

	}

	public void attachProbe(ModelProbe probe) {
		this.myMaster.probeList.add(probe);

	}

	public Hashtable<String, Xnode> getMyEntites() {
		// TODO Auto-generated method stub
		return null;
	}

	public Hashtable<String, String> getMyProperties() {
		// TODO Auto-generated method stub
		return null;
	}

}
