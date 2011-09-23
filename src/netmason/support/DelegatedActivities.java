package netmason.support;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;


public class DelegatedActivities {

	HashMap<Xnode, Queue<Operative>> nodes = new HashMap<Xnode, Queue<Operative>>();

	public int size() {
		return nodes.size();
	}

	public boolean containsActivity(Xnode activity) {
		return nodes.containsKey(activity);
	}

	public Operative getOperative(Xnode activity) {
		return nodes.get(activity).peek();
	}

	public void remove(Xnode activity) {

		nodes.get(activity).poll();
		if (nodes.get(activity).size() <= 1) {
			nodes.remove(activity);
		}

	}

	public void put(Xnode activity, Operative op) {
		if (nodes.containsKey(activity)) {
			nodes.get(activity).add(op);
		} else {
			Queue<Operative> q = new LinkedList<Operative>();
			q.add(op);
			nodes.put(activity, q);
		}
	}

}
