package netmason.experiments;

import netmason.model.NetMason;


public class SimpleWrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String prefix = args[0];
				
			for (int t = 1; t < 2; t++) {

				NetMason hhw = new NetMason(System.currentTimeMillis());
				
				hhw.properties.put("prefix", prefix);
				hhw.runID = t;
				
				double startTime = System.currentTimeMillis();
				hhw.start();
				while ((hhw.schedule.step(hhw))) {
				}
				hhw.finish();
				System.out.println("Run: " + t + " Time: " + (System.currentTimeMillis() - startTime));
			}

	}
}
