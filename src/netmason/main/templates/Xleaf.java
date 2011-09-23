package netmason.main.templates;

import java.util.HashMap;

public class Xleaf extends Xnode {
	
	public HashMap<Integer, Double> payoffs = new HashMap<Integer, Double>();
	
	public Xleaf(String type, String name) {
	
		super(type, name);
		
	}

}
