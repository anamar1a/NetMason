package netmason.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import netmason.main.agents.Operative;
import netmason.main.agents.SimulationMaster;
import netmason.main.agents.Strategist;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xknowledge;
import netmason.main.templates.Xleaf;
import netmason.main.templates.Xnode;
import netmason.main.templates.Xresource;
import netmason.main.templates.Xtask;
import netmason.model.NetMason;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * @author  Maciek
 */
public class ModelFactory {

	public boolean returnSim = false;

	/**
	 * @uml.property  name="target"
	 * @uml.associationEnd  
	 */
	public NetMason target;

	String fileName;

	ArrayList<Operative> agentList = new ArrayList<Operative>();

	ArrayList<Xknowledge> skillList = new ArrayList<Xknowledge>();

	ArrayList<Xtask> taskList = new ArrayList<Xtask>();

	ArrayList<Xresource> resourceList = new ArrayList<Xresource>();

	public ModelFactory(NetMason target) {

		/* Read in logging settings. */

		org.apache.log4j.PropertyConfigurator.configure("setups//log.properties");

		if (target == null) {
			returnSim = true;
			target = new NetMason(System.currentTimeMillis());
		} else {
			this.target = target;
		}

		/* Read in simulation settings and set them to the target. */

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("setups//main.properties"));
		} catch (IOException e) {
		}

		this.fileName = properties.getProperty("filename", "xml//bombings.xml");
		String mode = properties.getProperty("mode", "fromDyNetML");

		target.properties.put("updateInterval", properties.getProperty("updateInterval", "1"));
		target.properties.put("parallelSequence", properties.getProperty("parallelSequence", "false"));
		target.properties.put("socialActivity", properties.getProperty("socialActivity", "0.1"));
		target.properties.put("maxTime", properties.getProperty("maxTime", "1000"));
		target.properties.put("maxNumEvaluations", properties.getProperty("maxNumEvaluations", "10"));
		target.properties.put("fitnessLandscape", properties.getProperty("fitnessLandscape", "false"));
		target.properties.put("numSamples", properties.getProperty("numSamples", "10"));
		target.properties.put("delta", properties.getProperty("delta", "0"));
		target.properties.put("interceptProb", properties.getProperty("interceptProb", "0.01"));

		/*
		 * Create reference Xgraph that will serve as a contain from all the
		 * information gathered during a run.
		 */

		if (mode.equalsIgnoreCase("fromDyNetML")) {
			try {
				setupFromDyNetML();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (mode.equalsIgnoreCase("fromNetML")) {
			setupFromNetML();
		} else if (mode.equalsIgnoreCase("fromDatabase")) {
			setupFromDatabase();
		}

	}

	public void setupFromDyNetML() throws InstantiationException, IllegalAccessException {

		target.nodeTable.put(Operative.class, agentList);
		target.nodeTable.put(Xknowledge.class, skillList);
		target.nodeTable.put(Xtask.class, taskList);
		target.nodeTable.put(Xresource.class, resourceList);

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = (Document) builder.parse(new File(fileName));

			NodeList nodes = document.getElementsByTagName("node");

			int strategistID = 0;

			for (int a = 0; a < nodes.getLength(); a++) {

				Node tempNode = nodes.item(a);
				String type = tempNode.getParentNode().getAttributes().getNamedItem("type").getNodeValue();

				Xnode tempXnode = null;
				Class tempNodeClass = null;
				String className = "";

				if (type.equalsIgnoreCase("agent")) {

					String subtype = tempNode.getAttributes().getNamedItem("subtype").getNodeValue();

					if (subtype.equals("Operative")) {

						className = "netmason.main.agents.Operative";
						tempXnode = new Operative(type, tempNode.getAttributes().getNamedItem("id").getNodeValue());
						tempNodeClass = Class.forName(className);
						target.nodeTable.get(netmason.main.agents.Operative.class).add(tempXnode);
						target.masterGraph.addVertex(tempXnode);

					} else {

						className = "netmason.main.agents.Strategist";
						tempXnode = new Strategist(type, tempNode.getAttributes().getNamedItem("id").getNodeValue());
						tempNodeClass = Class.forName(className);
						target.nodeTable.get(netmason.main.agents.Operative.class).add(tempXnode);
						((Strategist) tempXnode).agentID = strategistID;
						target.masterGraph.addVertex(tempXnode);
						target.strategistList.put(strategistID, (Strategist) tempXnode);
						target.projectList.put(new Integer(strategistID), new ArrayList<Xtask>());
						strategistID++;

					}

				} else if (type.equalsIgnoreCase("task")) {
					className = "netmason.main.templates.X" + type;
					tempXnode = (Xnode) Class.forName(className).newInstance();
					tempXnode.name = tempNode.getAttributes().getNamedItem("id").getNodeValue();
					tempXnode.type = type;
					tempNodeClass = Class.forName(className);
					target.planGraph.addVertex(tempXnode);
					target.nodeTable.get(tempNodeClass).add(tempXnode);
				} else {
					className = "netmason.main.templates.X" + type;
					tempXnode = (Xnode) Class.forName(className).newInstance();
					tempXnode.name = tempNode.getAttributes().getNamedItem("id").getNodeValue();
					tempXnode.type = type;
					tempNodeClass = Class.forName(className);
					target.nodeTable.get(tempNodeClass).add(tempXnode);
					target.masterGraph.addVertex(tempXnode);
					target.planGraph.addVertex(tempXnode);
				}

				String team = tempNode.getParentNode().getParentNode().getParentNode().getAttributes().getNamedItem("id").getNodeValue();
				tempXnode.addToTeam(team);
				readPropertiesForXMLNode(tempNode, tempXnode);

			}

			NodeList edges = document.getElementsByTagName("edge");

			for (int e = 0; e < edges.getLength(); e++) {

				Node temp = edges.item(e);
				String targetName = temp.getAttributes().getNamedItem("target").getNodeValue();
				String sourceName = temp.getAttributes().getNamedItem("source").getNodeValue();

				Xnode targetNode = null;
				Xnode sourceNode = null;

				for (Xnode tempNode : target.masterGraph.getVertices()) {

					if (tempNode.getMyName().equalsIgnoreCase(targetName)) {
						targetNode = tempNode;
					}
					if (tempNode.getMyName().equalsIgnoreCase(sourceName)) {
						sourceNode = tempNode;
					}
				}

				for (Xnode tempNode : target.planGraph.getVertices()) {

					if (tempNode.getMyName().equalsIgnoreCase(targetName)) {
						targetNode = tempNode;
					}
					if (tempNode.getMyName().equalsIgnoreCase(sourceName)) {
						sourceNode = tempNode;
					}
				}

				Xedge tempEdge = new Xedge();

				if (temp.getParentNode().getAttributes().getNamedItem("id").getNodeValue().equalsIgnoreCase("dependencies")) {
					if (!target.planGraph.getNeighbors(sourceNode).contains(targetNode)) {
						target.planGraph.addEdge(tempEdge, sourceNode, targetNode);
					}
				} else {
					if (!target.masterGraph.getNeighbors(sourceNode).contains(targetNode)) {
						target.masterGraph.addEdge(tempEdge, targetNode, sourceNode);
					}
				}

				readPropertiesForXMLNode(temp, tempEdge);

			}

			/* Create task library for each of the Strategists. */

			for (Xnode tempNode : target.planGraph.getVertices()) {
				if (tempNode.type.equalsIgnoreCase("task")) {
					if (target.planGraph.inDegree(tempNode) == 0) {
						for (Integer strat : target.projectList.keySet()) {
							Strategist tempStrategist = target.strategistList.get(strat);
							for (String team : tempStrategist.teams) {
								if (tempNode.belongsToTeam(team)) {
									target.projectList.get(strat).add((Xtask) tempNode);
								}
							}

						}
					}
				}
			}

			/* Create Simulation Master and read in pre-scripted events. */

			SimulationMaster masterAgent = new SimulationMaster("master", "GameMaster");
			masterAgent.myModel = target;

			target.myMaster = masterAgent;

			/* TODO I removed the following block of code that deals with messages, as it was 
			 * not easily adaptable to the new Message class hierarchy. It is probably doable
			 * with reflection, but I think we need to discuss it before refactoring (NP 2009-03-12) 
			 */
			
//			NodeList messages = document.getElementsByTagName("message");
//
//			for (int a = 0; a < messages.getLength(); a++) {
//
//				Node temp = messages.item(a);
				
//				Xmessage tempMessage = new Xmessage();
//				
//				String targetName = temp.getAttributes().getNamedItem("to").getNodeValue();
//				Operative targetNode = null;
//				Operative sourceNode = masterAgent;
//
//				for (Operative tempNode : agentList) {
//					if (tempNode.getMyName().equalsIgnoreCase(targetName)) {
//						targetNode = tempNode;
//					}
//				}
//
//				tempMessage.setXfrom(sourceNode);
//				tempMessage.setXto(targetNode);
//				tempMessage.properties.put("verb", temp.getAttributes().getNamedItem("verb").getNodeValue());
//				tempMessage.properties.put("timeEnter", temp.getAttributes().getNamedItem("timeEnter").getNodeValue());
//
//				readPropertiesForXMLNode(temp, tempMessage);
//
//				masterAgent.messageCache.add(tempMessage);
//
//			}

			/* Read in payoff structure */

			Node payoffs = document.getElementsByTagName("payoffs").item(0);

			for (int i = 0; i < payoffs.getChildNodes().getLength(); i++) {

				Node n = payoffs.getChildNodes().item(i);
				if (n.getNodeName().equalsIgnoreCase("payoff")) {

					String taskName = n.getAttributes().getNamedItem("name").getNodeValue();
					Xnode taskNode = null;

					for (Xnode tempNode : taskList) {
						if (tempNode.getMyName().equalsIgnoreCase(taskName)) {
							taskNode = tempNode;
						}
					}

					Vector<Xtask> tempKey = new Vector<Xtask>();
					tempKey.add((Xtask) taskNode);

					parsePayoff(n, tempKey);

				}

			}

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Recreate beliefs of agents */

		for (Operative j : agentList) {
			j.myModel = target;
			j.reCreate();
		}

	}

	public void parsePayoff(Node temp, Vector<Xtask> tempKey) {

		for (int i = 0; i < temp.getChildNodes().getLength(); i++) {

			Node n = temp.getChildNodes().item(i);
			if (n.getNodeName().equalsIgnoreCase("payoff")) {

				String taskName = n.getAttributes().getNamedItem("name").getNodeValue();
				Xnode taskNode = null;

				for (Xnode tempNode : taskList) {
					if (tempNode.getMyName().equalsIgnoreCase(taskName)) {
						taskNode = tempNode;
					}
				}

				tempKey.add((Xtask) taskNode);
				parsePayoff(n, tempKey);

			} else if (n.getNodeName().equalsIgnoreCase("leaf")) {

				Xleaf tempLeaf = new Xleaf("leaf", "leaf");

				for (int j = 0; j < n.getChildNodes().getLength(); j++) {
					
					if (n.getChildNodes().item(j).getNodeName().equalsIgnoreCase("atom")) {
					
					String agentName = n.getChildNodes().item(j).getAttributes().getNamedItem("name").getNodeValue();
					Double agentPayoff = Double.valueOf(n.getChildNodes().item(j).getAttributes().getNamedItem("payoff").getNodeValue());

					for (Strategist tempStrat : target.strategistList.values()) {
						if (tempStrat.getMyName().equalsIgnoreCase(agentName)) {
							tempLeaf.payoffs.put(tempStrat.agentID, agentPayoff);
						}
					}
					
					}

				}

				target.payoffStructure.put(tempKey, tempLeaf);

			} else {

			}

		}

	}

	public void setupFromNetML() {

	}

	public void setupFromDatabase() {

	}

	public void readPropertiesForXMLNode(Node temp, Propertable destination) {

		if (temp.hasChildNodes()) {

			NodeList propertyList = temp.getChildNodes().item(1).getChildNodes();

			try {

				for (int z = 0; z < propertyList.getLength(); z++) {

					Node tempProperty = propertyList.item(z);

					if (tempProperty.getNodeName().equalsIgnoreCase("property")) {
						String tempKey = tempProperty.getAttributes().getNamedItem("name").getNodeValue();
						String tempValue = tempProperty.getAttributes().getNamedItem("value").getNodeValue();
						String tempType = tempProperty.getAttributes().getNamedItem("type").getNodeValue();

						if (tempType.equalsIgnoreCase("main.Xtask")) {

							/* Find corresponding instance of task */

							Xtask tempTask = null;

							for (Xtask tt : taskList) {
								if (tt.getMyName().equalsIgnoreCase(tempValue)) {
									tempTask = tt;
								}
							}

							destination.getMyEntites().put(tempKey, tempTask);

						} else {

							destination.getMyProperties().put(tempKey, tempValue);

						}

					}

				}

			} catch (DOMException e) {
				e.printStackTrace();
			}
		}

	}

}
