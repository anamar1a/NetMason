package netmason.experiments;

import java.util.Vector;

import netmason.main.templates.Xleaf;
import netmason.main.templates.Xtask;
import netmason.model.NetMason;


public class Sparrow {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String prefix = args[0];

		for (int t = 1; t < 100; t++) {

			NetMason hhw = new NetMason(System.currentTimeMillis());

			hhw.properties.put("prefix", prefix);
			hhw.runID = t;

			double beta = 2 * hhw.random.nextDouble() - 1;
			double delta = (double) hhw.random.nextInt(3) / 10;

			hhw.strategistList.get(0).level = 2;
			hhw.strategistList.get(1).level = 2;

			hhw.strategistList.get(0).planningHorizon = 1;
			hhw.strategistList.get(1).planningHorizon = 1;

			hhw.properties.put("beta", "" + beta);
			hhw.properties.put("delta", "" + delta);
			hhw.properties.put("numSamples", "" + 100);

			for (Xtask a : hhw.projectList.get(0)) {
				for (Xtask b : hhw.projectList.get(1)) {

					Vector<Xtask> newList = new Vector<Xtask>();

					newList.add(0, a);
					newList.add(1, b);

					double payoffa = 0;
					double payoffb = 0;

					if (a.name.equalsIgnoreCase("RT1")) {
						if (b.name.equalsIgnoreCase("BT1")) {
							payoffa = 1;
							payoffb = -1 * beta;
						} else if (b.name.equalsIgnoreCase("BT2")) {
							payoffa = 0;
							payoffb = 1;
						} else {
							payoffa = beta;
							payoffb = 0;
						}
					} else if (a.name.equalsIgnoreCase("RT2")) {
						if (b.name.equalsIgnoreCase("BT1")) {
							payoffa = beta;
							payoffb = 0;
						} else if (b.name.equalsIgnoreCase("BT2")) {
							payoffa = 1;
							payoffb = -1 * beta;
						} else {
							payoffa = 0;
							payoffb = 1;
						}
					} else {
						if (b.name.equalsIgnoreCase("BT1")) {
							payoffa = 0;
							payoffb = 1;
						} else if (b.name.equalsIgnoreCase("BT2")) {
							payoffa = beta;
							payoffb = 0;
						} else {
							payoffa = 1;
							payoffb = -beta;
						}
					}

					Xleaf tempLeaf = new Xleaf("leaf", "leaf");

					tempLeaf.payoffs.put(0, payoffa);
					tempLeaf.payoffs.put(1, payoffb);

					hhw.payoffStructure.put(newList, tempLeaf);

				}
			}

			double startTime = System.currentTimeMillis();
			hhw.start();

			while ((hhw.schedule.step(hhw))) {
			}
			hhw.finish();
			System.out.println("Run: " + t + " Time: " + (System.currentTimeMillis() - startTime));
		}

	}
}
