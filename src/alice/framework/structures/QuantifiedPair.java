package alice.framework.structures;

public class QuantifiedPair<K> implements Comparable<QuantifiedPair<K>>{
	public K key;
	public int value;
	
	public QuantifiedPair(K key, int value) {
		this.key = key; this.value = value;
	}

	@Override
	public int compareTo(QuantifiedPair<K> arg0) {
		return arg0.value - this.value;
	}
}