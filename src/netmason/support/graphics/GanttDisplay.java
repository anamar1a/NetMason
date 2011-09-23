/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ---------------
 * GanttDemo2.java
 * ---------------
 * (C) Copyright 2003, 2004, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: GanttDisplay.java,v 1.2 2008/01/15 17:15:43 mlatek Exp $
 *
 * Changes
 * -------
 * 10-Jan-2003 : Version 1 (based on GanttDemo1) (DG);
 * 16-Sep-2003 : Transferred dataset creation from DemoDatasetFactory to this class (DG);
 *
 */

package netmason.support.graphics;

import java.awt.Color;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import netmason.main.agents.Operative;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.IntervalCategoryDataset;
import org.jfree.data.gantt.Task;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.gantt.TaskSeriesCollection;
import org.jfree.ui.ApplicationFrame;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * A simple demonstration application showing how to create a Gantt chart with
 * multiple bars per task.
 * 
 */
public class GanttDisplay extends ApplicationFrame implements Steppable {

	TaskSeries s1 = new TaskSeries("Observed");

	JFreeChart chart;

	TaskSeriesCollection collection = new TaskSeriesCollection();

	private double nextUpdate = 0;

	HashMap<String, Task> taskMap = new HashMap<String, Task>();

	HashMap<Operative, Task> agentMap = new HashMap<Operative, Task>();

	public GanttDisplay(String title) {

		super(title);

		collection.add(s1);

		// create the chart...
		chart = ChartFactory.createGanttChart("Gantt Chart", // chart title
				"Agent", // domain axis label
				"Date", // range axis label
				collection, // data
				false, // include legend
				true, // tooltips
				false // urls
				);

		CategoryPlot plot = (CategoryPlot) chart.getPlot();

		CategoryItemRenderer renderer = plot.getRenderer();
		plot.setDomainGridlinePosition(CategoryAnchor.START);
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setTickLabelPaint(Color.blue);
		domainAxis.setAxisLineVisible(false);

		domainAxis.setCategoryLabelPositionOffset(0);
		domainAxis.setMaximumCategoryLabelLines(2);
		domainAxis.setMaximumCategoryLabelWidthRatio(0.3f);

		renderer.setSeriesPaint(0, Color.blue);

		// add the chart to a panel...
		ChartPanel chartPanel = new ChartPanel(chart);
		setContentPane(chartPanel);

	}

	/**
	 * Creates a sample dataset for a Gantt chart, using sub-tasks. In general,
	 * you won't hard-code the dataset in this way - it's done here so that the
	 * demo is self-contained.
	 * 
	 * @return The dataset.
	 */
	private IntervalCategoryDataset createSampleDataset() {

		final Task t1 = new Task("Write Proposal", date(1, Calendar.APRIL, 2001), date(5, Calendar.APRIL, 2001));
		t1.setPercentComplete(1.00);
		s1.add(t1);

		final Task t2 = new Task("Obtain Approval", date(9, Calendar.APRIL, 2001), date(9, Calendar.APRIL, 2001));
		t2.setPercentComplete(1.00);
		s1.add(t2);

		// here is a task split into two subtasks...
		final Task t3 = new Task("Requirements Analysis", date(10, Calendar.APRIL, 2001), date(5, Calendar.MAY, 2001));
		final Task st31 = new Task("Requirements 1", date(10, Calendar.APRIL, 2001), date(25, Calendar.APRIL, 2001));
		st31.setPercentComplete(1.0);
		final Task st32 = new Task("Requirements 2", date(1, Calendar.MAY, 2001), date(5, Calendar.MAY, 2001));
		st32.setPercentComplete(1.0);
		t3.addSubtask(st31);
		t3.addSubtask(st32);
		s1.add(t3);

		// and another...
		final Task t4 = new Task("Design Phase", date(6, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		final Task st41 = new Task("Design 1", date(6, Calendar.MAY, 2001), date(10, Calendar.MAY, 2001));
		st41.setPercentComplete(1.0);
		final Task st42 = new Task("Design 2", date(15, Calendar.MAY, 2001), date(20, Calendar.MAY, 2001));
		st42.setPercentComplete(1.0);
		final Task st43 = new Task("Design 3", date(23, Calendar.MAY, 2001), date(30, Calendar.MAY, 2001));
		st43.setPercentComplete(0.50);
		t4.addSubtask(st41);
		t4.addSubtask(st42);
		t4.addSubtask(st43);
		s1.add(t4);

		final Task t5 = new Task("Design Signoff", date(2, Calendar.JUNE, 2001), date(2, Calendar.JUNE, 2001));
		s1.add(t5);

		final Task t6 = new Task("Alpha Implementation", date(3, Calendar.JUNE, 2001), date(31, Calendar.JULY, 2001));
		t6.setPercentComplete(0.60);

		s1.add(t6);

		final Task t7 = new Task("Design Review", date(1, Calendar.AUGUST, 2001), date(8, Calendar.AUGUST, 2001));
		t7.setPercentComplete(0.0);
		s1.add(t7);

		final Task t8 = new Task("Revised Design Signoff", date(10, Calendar.AUGUST, 2001), date(10, Calendar.AUGUST, 2001));
		t8.setPercentComplete(0.0);
		s1.add(t8);

		final Task t9 = new Task("Beta Implementation", date(12, Calendar.AUGUST, 2001), date(12, Calendar.SEPTEMBER, 2001));
		t9.setPercentComplete(0.0);
		s1.add(t9);

		final Task t10 = new Task("Testing", date(13, Calendar.SEPTEMBER, 2001), date(31, Calendar.OCTOBER, 2001));
		t10.setPercentComplete(0.0);
		s1.add(t10);

		final Task t11 = new Task("Final Implementation", date(1, Calendar.NOVEMBER, 2001), date(15, Calendar.NOVEMBER, 2001));
		t11.setPercentComplete(0.0);
		s1.add(t11);

		final Task t12 = new Task("Signoff", date(28, Calendar.NOVEMBER, 2001), date(30, Calendar.NOVEMBER, 2001));
		t12.setPercentComplete(0.0);
		s1.add(t12);

		final TaskSeriesCollection collection = new TaskSeriesCollection();
		collection.add(s1);

		return collection;
	}

	/**
	 * Utility method for creating <code>Date</code> objects.
	 * 
	 * @param day
	 *            the date.
	 * @param month
	 *            the month.
	 * @param year
	 *            the year.
	 * 
	 * @return a date.
	 */
	private static Date date(final int day, final int month, final int year) {

		final Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, day);
		final Date result = calendar.getTime();
		return result;

	}

	/**
	 * Starting point for the demonstration application.
	 * 
	 * @param args
	 *            ignored.
	 */

	public void step(SimState state) {

		double currentTime = state.schedule.time();
//		NetMason world = (NetMason) state;

		/*
		 * Check if it is appropriate time to update, draw only if the display
		 * can be seen.
		 */

		if (currentTime > nextUpdate) {

			/* synchronized (world) {

				

				if ((world.schedule.time() < world.maxTime - world.updateInterval) && (world.schedule.time() > 1)) {
					String sql = "select * from XMESSAGE where TIME_ENTER between " + (world.schedule.time() - world.updateInterval) + " AND "
							+ (world.schedule.time() + " AND (MY_VERB LIKE 'ConfirmActivity' OR MY_VERB LIKE 'TakeTask') ");
					sql = sql + " AND MY_GRAPH_ID = " + DataObjectUtils.pkForObject(world.mainGraph);
					SQLTemplate selectQuery = new SQLTemplate(Xmessage.class, sql);
					List<Xmessage> messageList = world.context.performQuery(selectQuery);

					for (Xmessage m : messageList) {

						String taskSubject = ((Xnode) m.properties.get("activity").getValue()).getMyName();
						
						Operative taskOwner;
						if (m.getMyVerb().equalsIgnoreCase("confirmActivity")) {
							taskOwner = m.getXfrom();
						} else {
							taskOwner = m.getXto();
						}
						String taskName = taskOwner.getMyName() + "  " + taskSubject;
						Task tempTask;

						if (taskMap.containsKey(taskName)) {

							tempTask = taskMap.get(taskName);
							SimpleTimePeriod tPeriod = new SimpleTimePeriod(tempTask.getDuration().getStart(), date((int) world.schedule.time(),
									Calendar.APRIL, 2001));
							tempTask.setDuration(tPeriod);
							tempTask.setPercentComplete(1.0);

						} else {

							tempTask = new Task(taskName, date((int) world.schedule.time(), Calendar.APRIL, 2001), date((int) world.schedule.time(),
									Calendar.APRIL, 2001));
							tempTask.setPercentComplete(0.5);
							taskMap.put(taskName, tempTask);

							Operative master = m.getXfrom();

							if ((m.getMyVerb().equalsIgnoreCase("delegateActivity")) && (agentMap.containsKey(master))) {

								Task masterTask = agentMap.get(master);
								SimpleTimePeriod tPeriod = new SimpleTimePeriod(masterTask.getDuration().getStart(), date((int) world.schedule.time(),
										Calendar.APRIL, 2001));
								masterTask.setDuration(tPeriod);
								masterTask.addSubtask(tempTask);

							} else {
								collection.getSeries(0).add(tempTask);
							}
						}

						collection.getSeries(0).fireSeriesChanged();
						agentMap.put(taskOwner, tempTask);

					}

				}
			}

			nextUpdate = currentTime + world.updateInterval; */

		}

	}

	public void reset() {
		// TODO Auto-generated method stub

	}

}
