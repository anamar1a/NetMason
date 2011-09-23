package netmason.main.agents;

import java.util.ArrayList;

import netmason.main.messages.Message;
import netmason.model.probes.ModelProbe;

import sim.engine.SimState;
import sim.engine.Steppable;

public class SimulationMaster extends Operative implements Steppable {

	public ArrayList<Message> messageCache = new ArrayList<Message>();
	
	public ArrayList<ModelProbe> probeList = new ArrayList<ModelProbe>();

	private static final long serialVersionUID = 1L;

	public SimulationMaster(String type, String name) {
		super(type, name);
	}

	public void step(SimState state) {

		/*
		 * Select messages to be sent out next round that correspond to a given
		 * run.
		 */

		synchronized (myModel) {
			double currentTime = myModel.schedule.getTime();
			for (Message m : messageCache) {
				if (m.getCreationTime() < currentTime + 1) {
					outbox.add(m);
				}
			}
			messageCache.removeAll(outbox);
		}
		
		for (ModelProbe p: probeList) {
			p.update();
		}
		
		inbox.clear();

	}

}
