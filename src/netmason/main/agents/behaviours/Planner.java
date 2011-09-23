package netmason.main.agents.behaviours;

import java.util.List;

import netmason.main.agents.Operative;
import netmason.main.messages.ConfirmActivity;
import netmason.main.messages.TakeTask;
import netmason.main.templates.Xedge;
import netmason.main.templates.Xnode;


/**
 * Planner is the main class that represents problem solving by an agent. This
 * class operates by shifting task / resources between project, chatter and
 * delegated stacks and ocassionally spawning execution delegation /
 * confirmation messages.
 * 
 * @author Maciek
 */
public class Planner {

	/**
	 * @uml.property name="agent"
	 * @uml.associationEnd
	 */
	Operative agent;

	public Planner(Operative myAgent) {
		this.agent = myAgent;
	}

	/* Master planner method. */

	public void runPlanner() {
		/*-
		 * If this agent can handle this activity, 
		 *   Respond with ConfirmActivity to project supervisor
		 * Else if activity is a "task"
		 *   Respond with ConfirmActivity to project supervisor 
		 * Else if activity has already been delegated to someone 
		 *   Do nothing 
		 * Else if ability to perform project is in belief network somewhere 
		 *   Find a path there
		 *   Send a DelegateTask message to your nearest neighbor on this path.
		 * Else (I don't know about this resource) 
		 *   Push project to chatter stack
		 *
		 * The logic is based on a following ordering of Operative preferences:
		 *   a) try to solve as much of a task as possible yourself,
		 *   b) try to delegate and
		 *   c) try chatting to obtain new facts.
		 */

		if (agent.projectStack.isEmpty())
			return;

		Xnode activity = agent.projectStack.getLastNode();

		// Note: if a task is atop the stack, then its dependencies must have
		// already been met.
		boolean isTask = activity.isType("task");
		
		// Use getNeighbors.(...)contains(...) instead of isNeighbor(...) because the latter will throw an 
		// exception if the activity is not yet in the graph.
		if (isTask || agent.beliefs.getNeighbors(agent).contains(activity)) {

			/*
			 * If have access to resource or if no dependencies are left pop a
			 * task and report success back to GameMaster / supervisor (should
			 * any be assigned).
			 */
			agent.projectStack.removeLast();

			if (agent.delegatedFrom.containsActivity(activity)) {
				Operative supervisor = (Operative) agent.delegatedFrom.getOperative(activity);
				agent.delegatedFrom.remove(activity);
				agent.outbox.add(new ConfirmActivity(agent, supervisor, activity));
			}

			String s = String.format("[%d] %s: ", (int) agent.myModel.schedule.time(), agent.getMyName());
			if (isTask)
				s += String.format("Task %s has been successfuly executed.", activity.toString());
			else
				s += String.format("%s is ready.", activity.toString());
			agent.myModel.print(s);

		} else if (agent.delegatedTo.containsActivity(activity)) {

			// Wait!

		} else if (agent.beliefs.containsVertex(activity)) {
			
			List<Xedge> path = agent.getPath(agent, activity);
			if (path.size() > 0) {

				/*
				 * Continuous path has been found. Delegate task.
				 */

				Xnode to = (Xnode) agent.beliefs.getOpposite(agent, (Xedge) path.get(0));

				if (to.isType("agent")) {
					Operative delegate = (Operative) to;
					agent.outbox.add(new TakeTask(agent, delegate, activity));
					agent.delegatedTo.put(activity, delegate);

					agent.myModel.print("[" + (int) agent.myModel.schedule.time() + "] " + agent.getMyName() + ": " + "Obtaining of " + activity
							+ " had been delegated to " + delegate.getMyName());

					/*- TODO Here's how you'd do this using the String.format function.
					 * agent.myModel.print(String.format("[%d] %s: Obtaining of %s had been delegated to %s", (int)agent.myModel.schedule.time(), agent.getMyName(), activity, delegate.getMyName()));
					 */
				}
				/*
				 * TODO There should be an "else" here. What if the nearest
				 * neighbor is not an agent?
				 */
			} else {
				// No path was found. The only option left is to chatter.
				agent.chatterStack.ensureContains(activity);
			}

		} else {
			/*
			 * In this case, agent will attempt to either delegate the task or
			 * try to acquire new information that will enable him to execute
			 * the task.
			 */
			agent.chatterStack.ensureContains(activity);
		}
	}

}
