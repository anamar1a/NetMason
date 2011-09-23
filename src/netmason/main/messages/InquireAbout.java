package netmason.main.messages;

import java.util.Collection;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;


/* This is Interpreter for InquireAbout message. */

public class InquireAbout extends Message {

	private Xnode objectOfInterest;

	public InquireAbout(Operative sender, Operative recipient, Xnode objectOfInterest) {
		super(sender, recipient);
		this.objectOfInterest = objectOfInterest;
	}

	public void interpret() {

		Collection<Xnode> neighbors = recipient.beliefs.getNeighbors(objectOfInterest);
		boolean foundConnection = false;
		if (neighbors != null)
			for (Xnode n : neighbors) {
				if (n.isType("agent")) {
					foundConnection = true;
					// notify original sender of found connection
					// (since it's a response, recipient and sender are
					// inversed)
					recipient.outbox.add(new AddFact(recipient, sender, (Operative) n, objectOfInterest));
				}
			}

		if (foundConnection) {
			recipient.chatterStack.removeLastOccurenceOf(objectOfInterest);
		} else {
			recipient.chatterStack.ensureContains(objectOfInterest);
		}
	}
}