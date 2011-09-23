package kinship.graphing.displays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;

import kinship.agents.Person;
import kinship.graphing.KinshipModelWithUI;
import kinship.model.KinshipModel;
import kinship.support.KinshipEdge;

import org.apache.commons.collections15.Transformer;

import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.gui.LabelledList;
import sim.util.gui.MovieMaker;
import sim.util.gui.NumberTextField;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.picking.ShapePickSupport;

/**
 * @author Maciek
 */
public class ClanMapDisplay extends JComponent implements Steppable {

	public class OptionPane extends JFrame {
		// buffer stuff
		public int buffering;

		public JRadioButton useNoBuffer = new JRadioButton("By Drawing Separate Rectangles");

		public JRadioButton useBuffer = new JRadioButton("Using a Stretched Image");

		public JRadioButton useDefault = new JRadioButton("Let the Program Decide How");

		public ButtonGroup usageGroup = new ButtonGroup();

		public JCheckBox antialias = new JCheckBox("Antialias Graphics");

		public JCheckBox antialiasText = new JCheckBox("Antialias Text");

		public JCheckBox alphaInterpolation = new JCheckBox("Better Transparency");

		public JCheckBox interpolation = new JCheckBox("Bilinear Interpolation of Images");

		public JCheckBox tooltips = new JCheckBox("Tool Tips");

		public NumberTextField xOffsetField = new NumberTextField(0, 1, 50) {
			public double newValue(final double val) {
				/*
				 * double scale = getScale(); insideDisplay.xOffset = val /
				 * scale; Display2D.this.repaint(); // redraw the inside display
				 * return insideDisplay.xOffset * scale;
				 */
				return 1.0;
			}
		};

		public NumberTextField yOffsetField = new NumberTextField(0, 1, 50) {
			public double newValue(final double val) {
				/*
				 * double scale = getScale(); insideDisplay.yOffset = val /
				 * scale; Display2D.this.repaint(); // redraw the inside display
				 * return insideDisplay.yOffset * scale;
				 */
				return 1.0;
			}
		};

		public OptionPane(String title) {
			super(title);
			useDefault.setSelected(true);
			useNoBuffer.setToolTipText("<html>When not using transparency on Windows/XWindows,<br>this method is often (but not always) faster</html>");
			usageGroup.add(useNoBuffer);
			usageGroup.add(useBuffer);
			useBuffer
					.setToolTipText("<html>When using transparency, <i>or</i> when on a Mac,<br>this method is usually faster, but may require more<br>memory (especially on Windows/XWindows) --<br>increasing heap size can help performance.</html>");
			usageGroup.add(useDefault);

			JPanel p2 = new JPanel();

			Box b = new Box(BoxLayout.Y_AXIS);
			b.add(useNoBuffer);
			b.add(useBuffer);
			b.add(useDefault);
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.setBorder(new javax.swing.border.TitledBorder("Draw Grids of Rectangles..."));
			p.add(b, BorderLayout.CENTER);
			p2.setLayout(new BorderLayout());
			p2.add(p, BorderLayout.NORTH);

			LabelledList l = new LabelledList("Offset in Pixels");
			l.addLabelled("X Offset", xOffsetField);
			l.addLabelled("Y Offset", yOffsetField);
			p2.add(l, BorderLayout.CENTER);
			getContentPane().add(p2, BorderLayout.NORTH);

			b = new Box(BoxLayout.Y_AXIS);
			b.add(antialias);
			b.add(antialiasText);
			b.add(interpolation);
			b.add(alphaInterpolation);
			b.add(tooltips);
			p = new JPanel();
			p.setLayout(new BorderLayout());
			p.setBorder(new javax.swing.border.TitledBorder("Graphics Features"));
			p.add(b, BorderLayout.CENTER);
			getContentPane().add(p, BorderLayout.CENTER);

			ActionListener listener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					/*
					 * useTooltips = tooltips.isSelected(); if
					 * (useDefault.isSelected()) buffering =
					 * FieldPortrayal2D.DEFAULT; else if
					 * (useBuffer.isSelected()) buffering =
					 * FieldPortrayal2D.USE_BUFFER; else buffering =
					 * FieldPortrayal2D.DONT_USE_BUFFER;
					 * insideDisplay.setupHints(antialias.isSelected(),
					 * antialiasText.isSelected(),
					 * alphaInterpolation.isSelected(),
					 * interpolation.isSelected()); Display2D.this.repaint(); //
					 * redraw the inside display
					 */
				}
			};
			useNoBuffer.addActionListener(listener);
			useBuffer.addActionListener(listener);
			useDefault.addActionListener(listener);
			antialias.addActionListener(listener);
			antialiasText.addActionListener(listener);
			alphaInterpolation.addActionListener(listener);
			interpolation.addActionListener(listener);
			tooltips.addActionListener(listener);
			pack();
		}
	}

	public static final ImageIcon LAYERS_ICON = iconFor("Layers.png");

	public static final ImageIcon LAYERS_ICON_P = iconFor("LayersPressed.png");

	public static final ImageIcon MOVIE_ON_ICON = iconFor("MovieOn.png");

	public static final ImageIcon MOVIE_ON_ICON_P = iconFor("MovieOnPressed.png");

	public static final ImageIcon MOVIE_OFF_ICON = iconFor("MovieOff.png");

	public static final ImageIcon MOVIE_OFF_ICON_P = iconFor("MovieOffPressed.png");

	public static final ImageIcon CAMERA_ICON = iconFor("Camera.png");

	public static final ImageIcon CAMERA_ICON_P = iconFor("CameraPressed.png");

	public static final ImageIcon OPTIONS_ICON = iconFor("Options.png");

	public static final ImageIcon OPTIONS_ICON_P = iconFor("OptionsPressed.png");

	private static final long serialVersionUID = 1L;

	public Layout layout;

	private double nextUpdate = 0;

	public VisualizationViewer viewer;

	public JFrame frame;

	public OptionPane optionPane = new OptionPane("");

	GUIState simulation;

	private long interval;

	JDialog dialog;

	Stoppable stopper;

	Box header;

	/** The popup layers menu */
	JPopupMenu popup;

	/** The button which pops up the layers menu */
	JToggleButton togglebutton; // for popup

	/** The button which starts or stops a movie */
	JButton movieButton;

	/** The button which snaps a screenshot */
	JButton snapshotButton;

	/** The button which pops up the option pane */
	JButton optionButton;

	/**
	 * The last steps for a frame that was painted to the screen. Keeping this
	 * variable around enables our movie maker to ensure that it doesn't write a
	 * frame twice to its movie stream.
	 */
	long lastEncodedSteps = 0;

	/** Set to true if we're running on a Mac */
	public static final boolean isMacOSX = isMacOSX();

	/** Set to true if we're running on Windows */
	public static final boolean isWindows = isWindows();

	/** Set to the version number */
	public static final String javaVersion = getVersion();

	Object intervalLock = new Object(); // interval lock

	static ImageIcon iconFor(String name) {
		return new ImageIcon(Display2D.class.getResource(name));
	}

	public void step(SimState state) {

		double currentTime = simulation.state.schedule.time();

		if (frame.isShowing()) {

			layout.setGraph(((KinshipModel) simulation.state).matchMaker.kinshipGraph);
			layout.reset();
			Relaxer relaxer = viewer.getModel().getRelaxer();
			relaxer.pause();
			layout.initialize();
			relaxer.resume();
			layout.lock(true, true);

		}

	}

	public ClanMapDisplay(GUIState simulation, long interval) {

		this.interval = interval;
		this.simulation = simulation;
		this.frame = new JFrame();

		final Dimension preferredSize = new Dimension(500, 500);

		final Transformer<Person, Shape> shapeTransformer = new Transformer<Person, Shape>() {

			public Shape transform(Person p) {
				double baseSize = 5;

				return new java.awt.geom.Rectangle2D.Double(-0.5 * baseSize, -0.5 * baseSize, baseSize, baseSize);
			}

		};

		final Transformer<Person, Point2D> positionTransformer = new Transformer<Person, Point2D>() {

			public Point2D transform(Person p) {

				return new kinship.support.DoublePoint2D(0.1 * preferredSize.getWidth() + 0.9 * preferredSize.getWidth() * p.household.location.x, 0.1 * preferredSize.getHeight() + 0.9
						* preferredSize.getHeight() * p.household.location.y);
			}

		};

		final Transformer<Person, Paint> fillColorTransformer = new Transformer<Person, Paint>() {

			public Paint transform(Person p) {
				// TODO Auto-generated method stub
				if (p.clan != null) {
					return p.clan.getColor();
				} else {
					return (Color.BLACK);
				}
			}

		};

		layout = new edu.uci.ics.jung.algorithms.layout.FRLayout2<Person, KinshipEdge>(((KinshipModel) simulation.state).matchMaker.kinshipGraph, preferredSize);
		layout.setInitializer(positionTransformer);
		viewer = new VisualizationViewer(layout);
		viewer.setPickSupport(new ShapePickSupport(viewer));
		viewer.setGraphMouse(new DefaultModalGraphMouse());
		viewer.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Person, KinshipEdge>());
		viewer.getRenderContext().setVertexFillPaintTransformer(fillColorTransformer);
		viewer.getRenderContext().setVertexDrawPaintTransformer(fillColorTransformer);
		viewer.setBackground(Color.white);
		viewer.getRenderContext().setVertexShapeTransformer(shapeTransformer);

		header = new Box(BoxLayout.X_AXIS);

		// Create the popup menu.
		togglebutton = new JToggleButton(LAYERS_ICON);
		togglebutton.setPressedIcon(LAYERS_ICON_P);
		togglebutton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		togglebutton.setToolTipText("Show and hide different layers");
		header.add(togglebutton);

		popup = new JPopupMenu();
		popup.setLightWeightPopupEnabled(false);

		// Add listener to components that can bring up popup menus.
		togglebutton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				popup.show(e.getComponent(), togglebutton.getLocation().x, togglebutton.getLocation().y + togglebutton.getSize().height);
			}

			public void mouseReleased(MouseEvent e) {
				togglebutton.setSelected(false);
			}
		});

		// add the movie button
		movieButton = new JButton(MOVIE_OFF_ICON);
		movieButton.setPressedIcon(MOVIE_OFF_ICON_P);
		movieButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		movieButton.setToolTipText("Create a Quicktime movie");
		movieButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (movieMaker == null) {
					// startMovie();
				} else {
					// stopMovie();
				}
			}
		});
		header.add(movieButton);

		// add the snapshot button
		snapshotButton = new JButton(CAMERA_ICON);
		snapshotButton.setPressedIcon(CAMERA_ICON_P);
		snapshotButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		snapshotButton.setToolTipText("Create a snapshot (as a PNG file)");
		snapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				takeSnapshot();
			}
		});
		header.add(snapshotButton);

		// add the option button
		optionButton = new JButton(OPTIONS_ICON);
		optionButton.setPressedIcon(OPTIONS_ICON_P);
		optionButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		optionButton.setToolTipText("Show the Option Pane");
		optionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				optionPane.setTitle(frame.getTitle() + " Options");
				optionPane.pack();
				optionPane.setVisible(true);
				;
			}
		});
		header.add(optionButton);

		// frame.getContentPane().add(p, BorderLayout.SOUTH);
		frame.getContentPane().add(header, BorderLayout.NORTH);
		frame.getContentPane().add(viewer);

		frame.setSize(600, 600);

	}

	public void reset() {
		// now reschedule myself
		if (stopper != null) {
			stopper.stop();
		}

		stopper = simulation.scheduleImmediateRepeat(true, this);

	}

	public void quit() {
		if (stopper != null)
			stopper.stop();
		stopper = null;
		// stopMovie();
	}

	static boolean isMacOSX() {
		try // we'll try to get certain properties if the security permits it
		{
			return (System.getProperty("mrj.version") != null); // Apple's
			// official
			// approach
		} catch (Throwable e) {
			return false;
		} // Non-Mac Web browsers will fail here
	}

	static boolean isWindows() {
		try // we'll try to get certain properties if the security permits it
		{
			return !isMacOSX() && (System.getProperty("os.name").startsWith("Win"));
		} catch (Throwable e) {
			return false;
		}
	}

	static String getVersion() {
		try {
			return System.getProperty("java.version");
		} catch (Throwable e) {
			return "unknown";
		}
	}

	public long getInterval() {
		synchronized (intervalLock) {
			return interval;
		}
	}

	/** Sets how many steps are skipped before the display updates itself. */
	public void setInterval(long i) {
		synchronized (intervalLock) {
			if (i > 0)
				interval = i;
		}
	}

	public MovieMaker movieMaker;

	public void takeSnapshot() {

		this.step(this.simulation.state);

		((KinshipModelWithUI) simulation).exportSnapshot("pictures//clanMap_" + (int) this.simulation.state.schedule.getTime() + ".pdf", viewer);

	}

}
