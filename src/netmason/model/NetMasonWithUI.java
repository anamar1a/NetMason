package netmason.model;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import netmason.support.graphics.BeliefDisplay;
import netmason.support.graphics.CommunicationsDisplay;
import netmason.support.graphics.GanttDisplay;
import netmason.support.graphics.PlanDisplay;
import netmason.support.graphics.ScenarioDisplay;

import org.jfree.data.xy.XYSeries;

import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * @author  Maciek
 */
public class NetMasonWithUI extends GUIState {

	/**
	 * @uml.property  name="jDisplay"
	 * @uml.associationEnd  
	 */
	public BeliefDisplay jDisplay;
		
	/**
	 * @uml.property  name="aDisplay"
	 * @uml.associationEnd  
	 */
	public CommunicationsDisplay aDisplay;
	
	/**
	 * @uml.property  name="sDisplay"
	 * @uml.associationEnd  
	 */
	public ScenarioDisplay sDisplay;
	
	/**
	 * @uml.property  name="pDisplay"
	 * @uml.associationEnd  
	 */
	public PlanDisplay pDisplay;
	
	/**
	 * @uml.property  name="gDisplay"
	 * @uml.associationEnd  
	 */
	public GanttDisplay gDisplay;

	public JTextArea textArea;

	private double nextUpdate = 0;

	/**
	 * @uml.property  name="commsChart"
	 * @uml.associationEnd  
	 */
	public TimeSeriesChartGenerator commsChart;

	public JFrame commsFrame;

	public double commsIntensity;

	public XYSeries commsSeries;

	public static void main(String[] args) {
		NetMasonWithUI vid = new NetMasonWithUI();
		Console c = new Console((GUIState) vid);
		c.setVisible(true);
	}

	public NetMasonWithUI() {
		super(new NetMason(System.currentTimeMillis()));
	}

	public NetMasonWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "NetMason Control";
	}

	public Object getSimulationInspectedObject() {
		return state;
	}

	public void start() {
		super.start();
		setupPortrayals();
	}

	public void load(SimState state) {
		super.load(state);
		setupPortrayals();
	}

	public void setupPortrayals() {

		// Reset displays.

		jDisplay.reset();
		aDisplay.reset();
		sDisplay.reset();
		// gDisplay.reset();
		pDisplay.reset();
		
		commsChart.repaint();
		
		this.scheduleImmediateRepeat(false, jDisplay);
		this.scheduleImmediateRepeat(false, aDisplay);
		this.scheduleImmediateRepeat(false, sDisplay);
		// this.scheduleImmediateRepeat(false, gDisplay);
		this.scheduleImmediateRepeat(false, pDisplay);
		
		textArea.removeAll();
		commsSeries.clear();

	}

	public void init(Controller c) {
		super.init(c);

		// Instantiate BeliefDisplay
		jDisplay = new BeliefDisplay(this);
		BeliefDisplay.frame.setTitle("Agent Beliefs Display");
		c.registerFrame(BeliefDisplay.frame);
		BeliefDisplay.frame.setVisible(true);
		
		// Instantiate BeliefDisplay
		aDisplay = new CommunicationsDisplay(this);
		CommunicationsDisplay.frame.setTitle("Communication Network Display");
		c.registerFrame(CommunicationsDisplay.frame);
		CommunicationsDisplay.frame.setVisible(false);

		sDisplay = new ScenarioDisplay(this);
		ScenarioDisplay.frame.setTitle("Scenario Display");
		c.registerFrame(ScenarioDisplay.frame);
		ScenarioDisplay.frame.setVisible(false);
		
		pDisplay = new PlanDisplay(this);
		PlanDisplay.frame.setTitle("Plan Display");
		c.registerFrame(PlanDisplay.frame);
		PlanDisplay.frame.setVisible(false);
		
		//gDisplay = new GanttDisplay("Project Progress display");
		//gDisplay.pack();
		//c.registerFrame(gDisplay);
		//gDisplay.setPreferredSize(new java.awt.Dimension(500, 500));
		//gDisplay.setVisible(false);
		
		
		
		// Instantiate logs
		JFrame logFrame = new JFrame();
		textArea = new JTextArea();
		JScrollPane scrollPane = new JScrollPane(textArea);
		logFrame.setBounds(300, 300, 800, 600);
		scrollPane.setVisible(true);
		logFrame.setTitle("Message Log");
		c.registerFrame(logFrame);
		logFrame.add(scrollPane);
		logFrame.setVisible(true);

		// Instantiate time series chart with communication statistics
		commsSeries = new XYSeries("Number of Messages per Tick");
		commsChart = new TimeSeriesChartGenerator();
		commsChart.setTitle("Communications Statistics");
		commsChart.addSeries(commsSeries, null);
		commsChart.setDomainAxisLabel("Time");
		commsChart.setRangeAxisLabel("Number of messages");
		commsFrame = commsChart.createFrame(this);
		commsFrame.getContentPane().setLayout(new BorderLayout());
		commsFrame.getContentPane().add(commsChart, BorderLayout.CENTER);
		commsFrame.pack();
		c.registerFrame(commsFrame);

		/*
		 * A simple agent will be created and registered on schedule. It will
		 * update information used for plotting the histogram. It is possible to
		 * have underlying JFreeChart data structure in the SimState thread, but
		 * it unnecessarily slows down simulation when run in the batch /
		 * command line only mode.
		 */

		Steppable graphUpdater = new Steppable() {

			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {

				double currentTime = state.schedule.time();
				NetMason world = (NetMason) state;

				/*
				 * Check if it is appropriate time to update, draw only if the
				 * display can be seen.
				 */

				if (currentTime > nextUpdate) {

					textArea.append(world.buffer);
					world.buffer = "";
					nextUpdate = currentTime + Double.valueOf(world.properties.get("updateInterval"));
					textArea.validate();
					

					commsSeries.add(currentTime, world.myMailMan.messageCounter);
					world.myMailMan.messageCounter = 0;

				}
				

			}
		};

		this.scheduleImmediateRepeat(true, graphUpdater);

	}

	public void quit() {
		super.quit();

		if (BeliefDisplay.frame != null)
			BeliefDisplay.frame.dispose();
		BeliefDisplay.frame = null;
	}

}
