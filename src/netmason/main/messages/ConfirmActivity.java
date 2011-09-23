package netmason.main.messages;

import netmason.main.agents.Operative;
import netmason.main.templates.Xnode;

public class ConfirmActivity extends Message {

	private Xnode activity;

	public ConfirmActivity(Operative sender, Operative recipient, Xnode activity) {
		super(sender, recipient);
		this.activity = activity;
	}

	public void interpret() {

		/* Find out who delegated this task and what exactly got delegated. */

		/* Output this information to the log. */

		recipient.myModel.print("[" + (int) recipient.myModel.schedule.time() + "] " + recipient.getMyName() + ": Execution of activity " + activity
				+ " has been reported back by " + sender.getMyName());

		/* Remove executed activity from all the stacks */

		recipient.chatterStack.removeLastOccurenceOf(activity);
		recipient.projectStack.removeLastOccurenceOf(activity);

		/*
		 * TODO: I think it's possible that we could be removing the wrong
		 * delegate: if we assigned the same activity to different delegates
		 * (which is possible if we received it twice, but our belief network
		 * changed in between) but the delegate reporting completion is not the
		 * first one we assigned (NP 2009-03-12)
		 */
		recipient.delegatedTo.remove(activity);

		/*
		 * Spawn message confirming execution of a task and send it to the
		 * supervisor.
		 */

		if (recipient.delegatedFrom.containsActivity(activity)) {
			/*
			 * TODO: if we got the same activity delegated to us from multiple
			 * sources, shouldn't we report completion to everyone who assigned it
			 * to us? (NP 2009-03-12)
			 */
			Operative supervisor = (Operative) recipient.delegatedFrom.getOperative(activity);
			ConfirmActivity msg = new ConfirmActivity(recipient, supervisor, activity);
			supervisor.outbox.add(msg);

			recipient.delegatedFrom.remove(activity);
		}
	}
}
