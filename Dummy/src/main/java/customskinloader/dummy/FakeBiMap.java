package customskinloader.dummy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.Sets;

public class FakeBiMap<K, V> implements BiMap<K, V> {
    private final Map<K, V> map = new HashMap<K, V>();

    public static <K, V> BiMap<K, V> create(BiMap<K, V> map) {
        return new FakeBiMap<>();
    }

    private FakeBiMap() {
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return this.map.get(key);
    }

    @Override
    public V put(K k, V v) {
        return this.map.put(k, v);
    }

    @Override
    public V remove(Object key) {
        return this.map.remove(key);
    }

    @Override
    public V forcePut(K k, V v) {
        return this.map.put(k, v);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        this.map.putAll(map);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    public Set<V> values() {
        return Sets.newHashSet(this.map.values());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public BiMap<V, K> inverse() {
        throw new UnsupportedOperationException();
    }
}
