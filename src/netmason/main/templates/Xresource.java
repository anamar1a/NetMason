package netmason.main.templates;

import netmason.model.NetMason;

public class Xresource extends Xnode {

	public Xresource() {
		
	}
	
	
	public Xresource(String type, String name) {
		super(type, name);
		
	}

	public Xnode move(NetMason targetUniverse) {
		
		Xnode derivative = new Xresource(this.type, this.name);
		derivative.myModel = targetUniverse;
		return derivative;
		
	}

}
