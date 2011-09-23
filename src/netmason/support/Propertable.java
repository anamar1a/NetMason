package netmason.support;

import java.util.Hashtable;

import netmason.main.templates.Xnode;


public interface Propertable {
	
	public Hashtable<String, String> getMyProperties();
	
	public Hashtable<String, Xnode> getMyEntites();

}
