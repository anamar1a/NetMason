package netmason.support.graphics;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

import netmason.main.agents.Operative;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xnode;
import netmason.model.NetMason;
import netmason.model.NetMasonWithUI;

import org.apache.commons.collections15.Transformer;
import org.freehep.util.export.ExportDialog;

import sim.display.Display2D;
import sim.engine.SimState;
import sim.engine.Steppable;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout2;
import edu.uci.ics.jung.algorithms.layout.util.Relaxer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * A demonstrator for some of the graph layout algorithms. Allows the user to
 * interactively select one of several graphs, and one of several layouts, and
 * visualizes the combination.
 * 
 * @@author Danyel Fisher
 * @@author Joshua O'Madadhain
 */
public class BeliefDisplay implements Steppable {

	public static final ImageIcon CAMERA_ICON = iconFor("Camera.png");

	public static final ImageIcon CAMERA_ICON_P = iconFor("CameraPressed.png");

	protected static Graph<? extends Object, ? extends Object>[] graphArray;

	protected static int graphIndex;

	protected static String[] graphNames;

	NetMason sim;

	public static JFrame frame;

	static VisualizationViewer vv;

	public static class GraphChooser implements ActionListener {
		private JComboBox layout_combo;

		public GraphChooser(JComboBox layout_combo) {
			this.layout_combo = layout_combo;
		}

		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			graphIndex = cb.getSelectedIndex();
			layout_combo.setSelectedIndex(layout_combo.getSelectedIndex()); // rebuild
			// the
			// layout
		}
	}

	private static final class LayoutChooser implements ActionListener {
		private final JComboBox jcb;

		private final VisualizationViewer vv;

		private LayoutChooser(JComboBox jcb, VisualizationViewer vv) {
			super();
			this.jcb = jcb;
			this.vv = vv;
		}

		public void actionPerformed(ActionEvent arg0) {
			Object[] constructorArgs = { graphArray[graphIndex] };

			Class layoutC = (Class) jcb.getSelectedItem();
			Class lay = layoutC;
			try {
				Constructor constructor = lay.getConstructor(new Class[] { Graph.class });
				Object o = constructor.newInstance(constructorArgs);
				Layout l = (Layout) o;
				l.setInitializer(vv.getGraphLayout());
				l.setSize(vv.getSize());

				//TODO this is where the party ends. When we add types <Xnode, Xedge> to VisualizationViewer
				// and cascade those changes down, including Layout<Xnode, Xedge>, the following line generates 
				// an error because it's expecting a <String, Integer> parameterization. I'm not sure what to do. JFH Mar-13.
				LayoutTransition<String, Integer> lt = new LayoutTransition<String, Integer>(vv, vv.getGraphLayout(), l);
				Animator animator = new Animator(lt);
				animator.start();
				vv.getRenderContext().getMultiLayerTransformer().setToIdentity();
				vv.repaint();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public BeliefDisplay(NetMasonWithUI withUI) {

		this.sim = (NetMason) withUI.state;

		recreateLists();

		JPanel jp = getGraphPanel();

		frame = new JFrame();
		frame.getContentPane().add(jp);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);

	}

	private static JPanel getGraphPanel() {

		vv = new VisualizationViewer(new FRLayout(graphArray[0]));
		vv.getModel().getRelaxer().setSleepTime(500);

		vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer(vv.getPickedVertexState(), Color.red, Color.yellow));

		final ModdedModalGraphMouse graphMouse = new ModdedModalGraphMouse();

		vv.setGraphMouse(graphMouse);

		Transformer<Xnode, String> vertexLabeller = new Transformer<Xnode, String>() {

			public String transform(Xnode v) {

				return v.getMyName();
			}

		};

		Transformer<Xnode, Paint> vertexPainter = new Transformer<Xnode, Paint>() {

			public Paint transform(Xnode v) {
				if (v.isType("agent")) {
					return Color.RED;
				} else if (v.isType("task")) {
					return Color.BLUE;
				} else if (v.isType("resource")) {
					return Color.GREEN;
				} else if (v.isType("knowledge")) {
					return Color.YELLOW;
				} else {
					return Color.CYAN;
				}
			}

		};

		Transformer<Xnode, Paint> vertexDrawTransformer = new Transformer<Xnode, Paint>() {

			public Paint transform(Xnode v) {
				if (v.isType("agent")) {
					Operative a = (Operative) v;

					if (a.delegatedFrom.size() > 0) {
						return Color.GREEN;
					} else {
						return Color.BLACK;
					}

				} else {
					return Color.BLACK;
				}
			}

		};

		Transformer<Xedge, Stroke> edgeStrokeTransformer = new Transformer<Xedge, Stroke>() {

			public Stroke transform(Xedge e) {

				float width;

				if (e.properties.containsKey("reinforcement")) {
					width = (float) new Double(e.properties.get("reinforcement")).floatValue();
				} else {
					width = 1;
				}

				e.properties.put("reinforcement", "" + Math.max(1.0, width * 0.95));

				return new BasicStroke(width);
			}

		};

		vv.getRenderContext().setVertexLabelTransformer(vertexLabeller);
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPainter);

		vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);

		vv.getRenderContext().setVertexDrawPaintTransformer(vertexDrawTransformer);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1 / 1.1f, vv.getCenter());
			}
		});
		JButton reset = new JButton("reset");
		reset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Layout layout = vv.getGraphLayout();
				layout.initialize();
				Relaxer relaxer = vv.getModel().getRelaxer();
				if (relaxer != null) {
					// if(layout instanceof IterativeContext) {
					relaxer.stop();
					relaxer.prerelax();
					relaxer.relax();
				}
			}
		});

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(((ModdedModalGraphMouse) vv.getGraphMouse()).getModeListener());

		JPanel jp = new JPanel();
		jp.setBackground(Color.WHITE);
		jp.setLayout(new BorderLayout());
		jp.add(vv, BorderLayout.CENTER);
		Class[] combos = getCombos();
		final JComboBox jcb = new JComboBox(combos);
		// use a renderer to shorten the layout name presentation
		jcb.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				String valueString = value.toString();
				valueString = valueString.substring(valueString.lastIndexOf('.') + 1);
				return super.getListCellRendererComponent(list, valueString, index, isSelected, cellHasFocus);
			}
		});
		jcb.addActionListener(new LayoutChooser(jcb, vv));
		jcb.setSelectedItem(FRLayout.class);

		JPanel control_panel = new JPanel(new GridLayout(2, 1));
		JPanel topControls = new JPanel();
		JPanel bottomControls = new JPanel();
		control_panel.add(topControls);
		control_panel.add(bottomControls);
		jp.add(control_panel, BorderLayout.NORTH);

		final JComboBox graph_chooser = new JComboBox(graphNames);

		graph_chooser.addActionListener(new GraphChooser(jcb));

		topControls.add(jcb);
		topControls.add(graph_chooser);
		bottomControls.add(plus);
		bottomControls.add(minus);
		bottomControls.add(modeBox);
		bottomControls.add(reset);

		JButton snapshotButton;
		snapshotButton = new JButton(CAMERA_ICON);
		snapshotButton.setPressedIcon(CAMERA_ICON_P);
		snapshotButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		snapshotButton.setToolTipText("Create a snapshot (as a PNG file)");
		snapshotButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				takeSnapshot();
			}
		});
		bottomControls.add(snapshotButton);

		return jp;
	}

	/**
	 * @@return
	 */
	private static Class<?>[] getCombos() {
		List<Class <?>> layouts = new ArrayList<Class <?>>();
		layouts.add(KKLayout.class);
		layouts.add(FRLayout.class);
		layouts.add(CircleLayout.class);
		layouts.add(SpringLayout.class);
		layouts.add(SpringLayout2.class);
		layouts.add(ISOMLayout.class);
		return (Class[]) layouts.toArray(new Class[0]);
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	double nextUpdate = 0;

	public void step(SimState state) {
		// TODO Auto-generated method stub

		double currentTime = sim.schedule.time();

		if ((currentTime > nextUpdate)) {

			// Layout layout = vv.getGraphLayout();
			// layout.setGraph(layout.getGraph());
			// Relaxer relaxer = vv.getModel().getRelaxer();

			// if (relaxer != null) {

			// relaxer.stop();
			// relaxer.prerelax();
			// relaxer.relax();
			// }

			vv.repaint();
			recreateLists();
			nextUpdate = currentTime + Double.valueOf(sim.properties.get("updateInterval"));

		}

	}

	public void recreateLists() {

		ArrayList agentList = sim.nodeTable.get(Operative.class);
		graphNames = new String[agentList.size()];
		graphArray = new UndirectedSparseGraph[agentList.size()];

		for (int i = 0; i < agentList.size(); i++) {
			graphNames[i] = ((Operative) agentList.get(i)).getMyName();
			graphArray[i] = ((Operative) agentList.get(i)).beliefs;
		}

	}

	public static void takeSnapshot() {

		  Color original  = vv.getBackground();
		  vv.setBackground(Color.WHITE);
		  ExportDialog export = new ExportDialog();
          export.showExportDialog(vv, "Export view as ...", vv, "export");
          vv.setBackground(original);
          
	}

	static ImageIcon iconFor(String name) {
		return new ImageIcon(Display2D.class.getResource(name));
	}
}
