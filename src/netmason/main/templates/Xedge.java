package netmason.main.templates;

import java.util.Hashtable;

import netmason.support.Propertable;


public class Xedge implements Propertable, Comparable {

	public Hashtable<String, String> properties = new Hashtable<String, String>();
	
	public void reinforce() {

		float width;

		if (properties.containsKey("reinforcement")) {
			width = (float) new Double(properties.get("reinforcement")).floatValue();
		} else {
			width = 1;
		}

		properties.put("reinforcement", "" + (Math.max(1.0, width + 0.1)));

	}

	public Hashtable<String, String> getMyProperties() {
		return this.properties;
	}

	public Hashtable<String, Xnode> getMyEntites() {
		return null;
	}

	public int compareTo(Object o) {
		return 0;
	}

}
