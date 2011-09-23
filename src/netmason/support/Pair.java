package netmason.support;


public class Pair<S, T> {
	
	S node;
	T time;

	public Pair(S node, T time) {
		this.node = node;
		this.time = time;
	}

	public S getKey() {
		return this.node;
	}

	public T getValue() {
		return time;
	}

}
