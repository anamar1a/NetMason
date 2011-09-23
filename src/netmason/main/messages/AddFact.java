package netmason.main.messages;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;

/* AddFact message is used by Operatives to inform other Operatives about existing connection between two Xnodes. 
 * The message body contains pointers to source and target and a confidence value. Recipient Operative will
 * check if endnodes and the edge between them were present in it's belief and correct the confidence of the fact 
 * with general trust coefficent towards the sender Operative.   
 */

public class AddFact extends Message {

	private Xnode sourceNode;

	private Xnode targetNode;

	public AddFact(Operative sender, Operative recipient, Xnode sourceNode, Xnode targetNode) {
		super(sender, recipient);
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
	}

	/** Copy constructor */
	public AddFact(AddFact m) {
		this(m.sender, m.recipient, m.sourceNode, m.targetNode);
	}
	
	/** Returns a new copy of this message */
	public AddFact clone() {
		return new AddFact(this);
	}

	/**
	 * Check if the given node is an agent and there's no existing path in the
	 * beliefs network.
	 */
	private boolean needsChatter(Xnode node) {
		if (!node.equals(recipient)) {
			return (recipient.getPath(recipient, node).size() == 0);
		} else {
			return false;
		}
	}

	public void interpret() {

		recipient.addBelief(sourceNode, targetNode);
		
		if (needsChatter(sourceNode))
			recipient.chatterStack.ensureContains(sourceNode);
		if (needsChatter(targetNode))
			recipient.chatterStack.ensureContains(targetNode);
	}
}
