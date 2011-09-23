package netmason.main.templates;

import java.util.HashSet;
import java.util.Hashtable;

import netmason.model.NetMason;
import netmason.support.Moveable;
import netmason.support.Propertable;


/**
 * @author  Maciek
 */
public class Xnode implements Propertable, Comparable, Moveable {

	private static final long serialVersionUID = 1L;

	public Hashtable<String, String> properties = new Hashtable<String, String>();
	
	public String type;
	
	public String name;

	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	public NetMason myModel;
	
	public HashSet<String> teams = new HashSet<String>();

	public Xnode() {
		
	}
	
	public void addToTeam(String team) {
		this.teams.add(team);
	} 
	
	public boolean belongsToTeam(String team) {
		return this.teams.contains(team);
	}
	
	
	public Xnode(String type, String name) {
		this.type = type;
		this.name = name;
		
	}

	public String getNodeType() {
		return this.type;
	}
	
	public boolean isType(String type) {
		return this.type.equalsIgnoreCase(type);
	}

	public String toString() {
		return this.name;

	}

	public String getMyName() {
		return this.name;
	}

	public Hashtable<String, String> getMyProperties() {
		return this.properties;
	}

	public Hashtable<String, Xnode> getMyEntites() {
		return null;
	}
	
	public Xnode move(NetMason targetUniverse) {
		
		Xnode derivative = new Xnode(this.type, this.name);
		derivative.myModel = targetUniverse;
		return derivative;
		
	}

	public int compareTo(Object o) {
		return 0;
	}

}
