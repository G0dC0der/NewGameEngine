package pojahn.lang;

public class Entry<K, V> {

    public K key;
    public V value;

    public Entry(final K key, final V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
