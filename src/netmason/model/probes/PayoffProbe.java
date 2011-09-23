package netmason.model.probes;

import java.util.HashMap;
import java.util.Vector;

import netmason.main.agents.Strategist;
import netmason.main.messages.ConfirmActivity;
import netmason.main.messages.Message;
import netmason.main.templates.Xleaf;
import netmason.main.templates.Xtask;
import netmason.model.NetMason;


/**
 * @author  Maciek
 */
public class PayoffProbe implements ModelProbe {

	/**
	 * @uml.property  name="myModel"
	 * @uml.associationEnd  
	 */
	public NetMason myModel;

	public HashMap<Strategist, Double> payoffMemory = new HashMap<Strategist, Double>();

	public PayoffProbe(NetMason myModel) {
		this.myModel = myModel;
	}

	public double getPayoffOfAgent(Strategist myAgent) {
		if (this.payoffMemory.containsKey(myAgent)) {
			return this.payoffMemory.get(myAgent);
		} else {
			return 0;
		}
	}

	public void update() {

		for (Message m : myModel.myMaster.inbox) {
			if (m instanceof ConfirmActivity) {

				/* TODO Question: the following code will execute once for each ConfirmActivity message in the inbox, but makes no use use of the message itself. Is this OK? Or should it beak out of the loop after one execution? (NP 2009-03-12) */
				
				Vector<Xtask> taskList = this.traverseTree();
				Xleaf finalLeaf = myModel.payoffStructure.get(taskList);

				for (Integer i : finalLeaf.payoffs.keySet()) {

					Strategist s = myModel.strategistList.get(i);

					if (this.payoffMemory.containsKey(s)) {
						this.payoffMemory.put(s, finalLeaf.payoffs.get(i) + this.payoffMemory.get(s));
					} else {
						this.payoffMemory.put(s, finalLeaf.payoffs.get(i));
					}

				}

			}

		}

	}

	private Vector<Xtask> traverseTree() {

		Vector<Xtask> taskList = new Vector<Xtask>(myModel.strategistList.size());

		for (int i=0; i< myModel.strategistList.size(); i++) {
			Strategist s = myModel.strategistList.get(i);
			taskList.add(i, s.nextTask);
		}

		return taskList;

	}

	public void clearMemory() {
		payoffMemory.clear();
	}

}
