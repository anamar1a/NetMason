package kinship.graphing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JFrame;

import kinship.graphing.displays.ClanMapDisplay;
import kinship.model.KinshipModel;

import org.freehep.graphicsio.pdf.PDFExportFileType;

import sim.display.Console;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.HistogramGenerator;
import sim.util.media.chart.TimeSeriesChartGenerator;

public class KinshipModelWithUI extends GUIState {

	public ClanMapDisplay clanDisplay;

	public HistogramGenerator agesHist;

	public JFrame agesHistFrame;

	public TimeSeriesChartGenerator ageChart;

	public JFrame ageFrame;

	protected double nextUpdate = 1;

	protected boolean mapToLayout = false;

	public GUIReporter myReporter;

	public static void main(String[] args) {
		KinshipModelWithUI vid = new KinshipModelWithUI();
		Console c = new Console(vid);
		c.setVisible(true);
	}

	public KinshipModelWithUI() {
		super(new KinshipModel(System.currentTimeMillis()));
	}

	public KinshipModelWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "Computational Hierarchies";
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

		this.scheduleImmediateRepeat(true, myReporter);

		clanDisplay.reset();

		ageChart.repaint();

		agesHist.repaint();

	}

	public void init(Controller c) {
		super.init(c);

		myReporter = new GUIReporter(this);

		clanDisplay = new ClanMapDisplay(this, 1);
		clanDisplay.frame.setTitle("Clan Map");
		c.registerFrame(clanDisplay.frame);
		clanDisplay.frame.setVisible(false);

		ageChart = new TimeSeriesChartGenerator();
		ageChart.setTitle("Ages Plots");
		ageChart.addSeries(myReporter.avgAgeSeries, null);
		ageChart.addSeries(myReporter.maxAgeSeries, null);
		ageChart.setDomainAxisLabel("Time in ticks");
		ageChart.setRangeAxisLabel("Age in ticks");
		ageFrame = ageChart.createFrame(this);
		ageFrame.getContentPane().setLayout(new BorderLayout());
		ageFrame.getContentPane().add(ageChart, BorderLayout.CENTER);
		ageFrame.pack();
		c.registerFrame(ageFrame);

		agesHist = new HistogramGenerator();
		agesHist.setTitle("Ages Histogram");
		agesHist.update();
		agesHist.addSeries(myReporter.ages, 10, "Distribution of ages", null);
		agesHistFrame = agesHist.createFrame(this);
		agesHistFrame.getContentPane().setLayout(new BorderLayout());
		agesHistFrame.getContentPane().add(agesHist, BorderLayout.CENTER);
		agesHistFrame.pack();
		c.registerFrame(agesHistFrame);

		Steppable graphUpdater = new Steppable() {

			private static final long serialVersionUID = 6184761986120478954L;

			public void step(SimState state) {

				if (agesHist.isVisible()) {
					agesHist.updateSeries(0, myReporter.ages, false);
				}

			}
		};

		this.scheduleImmediateRepeat(true, graphUpdater);
	}

	public void quit() {
		super.quit();
	}

	public static PDFExportFileType exporter = new PDFExportFileType();

	public static void exportSnapshot(String name, Component viewer) {

		try {
			exporter.exportToFile(new File(name), viewer, viewer, new Properties(), "export");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
