package netmason.model.probes;

import java.util.HashMap;

import netmason.main.agents.Strategist;
import netmason.main.agents.behaviours.optimizer.Strategy;
import netmason.model.NetMason;

/**
 * @author  Maciek
 */
public class StrategyProbe implements ModelProbe {
	
	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	NetMason myModel;
	
	public StrategyProbe(NetMason myModel) {
		this.myModel = myModel;
	}

	public HashMap<Integer, Strategy> getStrategies() {
		
		HashMap<Integer, Strategy> solutions = new HashMap<Integer, Strategy>();
		
		for (Strategist i: myModel.strategistList.values()) {
			solutions.put(i.agentID, i.getStrategy());
		}
		
		return solutions;
	}

	public void update() {
		
		
	}

}
