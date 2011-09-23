package netmason.main.messages;

import netmason.main.agents.Operative;

public abstract class Message {

	/*
	 * creationTime might not be necessary in the long run, but SimulationMaster
	 * currently makes use of the message timestamp, so I added it to replace
	 * the old Xmessage.properties.get("timeEnter"). (NP 2009-03-12)
	 */
	protected double creationTime;

	protected Operative recipient;

	protected Operative sender;

	public Message(Operative sender, Operative recipient) {
		this.recipient = recipient;
		this.sender = sender;
		creationTime = sender.myModel.schedule.getTime();
	}

	public double getCreationTime() {
		return creationTime;
	}

	public Operative getRecipient() {
		return recipient;
	}

	public Operative getSender() {
		return sender;
	}

	/** Interpret the message and construct adequate response if required. */
	abstract public void interpret();

	public void setRecipient(Operative o) {
		this.recipient = o;
	}

	public void setSender(Operative o) {
		this.sender = o;
	}

}
