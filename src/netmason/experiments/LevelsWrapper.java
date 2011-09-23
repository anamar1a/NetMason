package netmason.experiments;

import netmason.model.NetMason;

public class LevelsWrapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String prefix = args[0];

		for (int t = 1; t < 10; t++) {

			NetMason hhw = new NetMason(System.currentTimeMillis());

			hhw.properties.put("prefix", prefix);
			hhw.runID = t;

			hhw.strategistList.get(0).level = 1 + hhw.random.nextInt(3);
			hhw.strategistList.get(1).level = 1 + hhw.random.nextInt(3);

			double startTime = System.currentTimeMillis();
			hhw.start();
			while ((hhw.schedule.step(hhw))) {
			}
			hhw.finish();
			System.out.println("Run: " + t + " Time: " + (System.currentTimeMillis() - startTime));
		}

	}
}
