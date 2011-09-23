package netmason.main.templates;

import netmason.model.NetMason;



public class Xknowledge extends Xnode {

	public Xknowledge() {
		
	}
	
	public Xknowledge(String type, String name) {
		super(type, name);
	}

	public Xnode move(NetMason targetUniverse) {
		
		Xnode derivative = new Xknowledge(this.type, this.name);
		derivative.myModel = targetUniverse;
		return derivative;
		
	}

}
