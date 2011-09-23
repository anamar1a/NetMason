package netmason.main.agents;

import java.util.Collection;
import java.util.HashMap;
import java.util.Queue;

import netmason.main.messages.AddFact;
import netmason.main.messages.Message;
import netmason.model.NetMason;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * @author Maciek
 */
public class MailMan implements Steppable {

	private static final long serialVersionUID = 1L;

	/**
	 * @uml.property name="simulation"
	 * @uml.associationEnd
	 */
	NetMason simulation;

	public int messageCounter = 0;

	public MailMan(NetMason attachment) {
		this.simulation = attachment;
	}

	public void step(SimState state) {

		Collection<Operative> operatives = simulation.nodeTable.get(Operative.class);

		for (Operative op : operatives) {

			while (op.outbox.size() > 0) {

				/*
				 * Here would be a good place for all the logic that checks if
				 * the message can be delivered, and if not, what kind of action
				 * should be taken.
				 */

				Message msg = op.outbox.remove();
				Operative dest = msg.getRecipient();
				dest.inbox.add(msg);

				/* Rudimentary wiretapping implementation. */

				HashMap<Integer, Strategist> strategists = simulation.strategistList;
				if (strategists.size() > 1) {
					double rand = simulation.random.nextDouble();
					if (rand < Double.valueOf(simulation.properties.get("interceptProb"))) {
							Strategist listener = strategists.get(simulation.random.nextInt(strategists.size()));
							AddFact copy = new AddFact(listener, msg.getSender(), msg.getSender(), msg.getRecipient());
							listener.inbox.add(copy);
					}
				}

				messageCounter++;
			}
		}

		Queue<Message> masterOutbox = simulation.myMaster.outbox;
		while (!masterOutbox.isEmpty()) {
			/*
			 * Here would be a good place for all the logic that checks if the
			 * message can be delivered, and if not, what kind of action should
			 * be taken.
			 */

			Message msg = masterOutbox.remove();
			msg.getRecipient().inbox.add(msg);
		}

	}

}
