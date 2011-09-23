package netmason.main.messages;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;

public class TakeTask extends Message {

	private Xnode activity;
	
	public TakeTask(Operative sender, Operative recipient, Xnode activity) {
		super(sender, recipient);
		this.activity = activity;
	}

	public void interpret() {

		/*
		 * Accepting task is composed of adding a task / resource to be
		 * obtained to the projectStack (through the expand method) and
		 * registering who issued a given order.
		 */

		recipient.expandTask(activity);
		
		recipient.delegatedFrom.put(activity, sender);
		recipient.myModel.print("[" + (int) recipient.myModel.schedule.time() + "] " + recipient.getMyName() + ": " + "Task " + activity.getMyName()
				+ " and dependencies added to project stack.");
	}
}
