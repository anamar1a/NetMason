package kinship.support;

import kinship.agents.Person;

public class ValuedPerson implements Comparable {

	public double score;

	public Person person;

	public ValuedPerson(Person person, double score) {
		this.person = person;
		this.score = score;
	}

	public int compareTo(Object arg0) {

		ValuedPerson target = (ValuedPerson) arg0;

		if (score < target.score) {
			return -1;
		} else {
			return 1;
		}
	}

}
