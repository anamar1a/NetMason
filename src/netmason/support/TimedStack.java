package netmason.support;

import java.util.LinkedList;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;

public class TimedStack {

	LinkedList<Pair<Xnode, Double>> stack = new LinkedList<Pair<Xnode, Double>>();

	/**
	 * @uml.property name="agent"
	 * @uml.associationEnd
	 */
	public Operative agent;

	public TimedStack(Operative agent) {
		this.agent = agent;
	}

	public void push(Xnode node) {
		push(node, new Double(agent.myModel.schedule.getTime()));
	}

	public void push(Pair<Xnode, Double> timedNode) {
		stack.addLast(timedNode);
	}

	public void push(Xnode node, Double time) {
		push(new Pair<Xnode, Double>(node, time));
	}

	public Pair<Xnode, Double> pop() {
		return stack.removeLast();
	}
	
	public Pair<Xnode, Double> removeLast() {
		return stack.removeLast();
	}

	public Pair<Xnode, Double> removeLastOccurenceOf(Xnode node) {

		for (int i = stack.size() - 1; i > 0; i--)
			if (stack.get(i).getKey() == node)
				return stack.remove(i);

		return null; // not found
	}

	public boolean containsNode(Xnode node) {

		for (Pair<Xnode, Double> p : stack)
			if (p.getKey() == node)
				return true;

		return false; // not found
	}

	public Xnode getLastNode() {
		if (stack.size() == 0)
			return null;
		return stack.peekLast().getKey();
	}

	public int size() {
		return stack.size();
	}

	public boolean isEmpty() {
		return stack.size() == 0;
	}

	public Xnode getRandomNode() {
		int randomIndex = agent.myModel.random.nextInt(size());
		return stack.get(randomIndex).getKey();
	}

	/** Adds node to the timedstack if it does not contain it already */
	public void ensureContains(Xnode node) {
		if (!containsNode(node)) {
			push(node);
		}
	}
	
	public String toString() {
		
		String temp = "";
		
		for (Pair<Xnode, Double> p : this.stack) {
			temp = temp + " " + p.node.name; 
		}
		
		temp = temp+"";
		
		return temp;
		
	}

}
