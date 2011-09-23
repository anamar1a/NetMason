package netmason.main.templates;

import netmason.model.NetMason;



public class Xtask extends Xnode {

	public Xtask() {
		
	}
	
	public Xtask(String type, String name) {
		super(type, name);
	}

	public Xnode move(NetMason targetUniverse) {
		
		Xnode derivative = new Xtask(this.type, this.name);
		derivative.myModel = targetUniverse;
		return derivative;
		
	}
	

}
