package net.petersil98.core.http.ratelimit;

import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Implementation of a Double Key Map, which means two Keys uniquely identify a value. It used two nested {@link HashMap} internally
 * @param <K1> Type of the First Key
 * @param <K2> Type of the Second Key
 * @param <V> Type of the Value
 */
public class DoubleKeyMap<K1, K2, V> {

    private final Map<K1, Map<K2, V>> map;

    /**
     * Constructor
     */
    public DoubleKeyMap() {
        this.map = new ConcurrentHashMap<>();
    }

    /**
     * Puts a value in the Map under the given keys
     * @see Map#put(Object, Object)
     * @param key1 First Key
     * @param key2 Second Key
     * @param value The value to be stored
     */
    public void put(K1 key1, K2 key2, V value) {
        if(!map.containsKey(key1)) {
            map.put(key1, new ConcurrentHashMap<>());
        }
        map.get(key1).put(key2,value);
    }

    /**
     * Retrieves the value identified by the given keys
     * @param key1 First Key
     * @param key2 Second Key
     * @return The value associated with the two keys
     */
    public V get(K1 key1, K2 key2) {
        return map.get(key1).get(key2);
    }

    /**
     * Checks whether there is a value associated with the given keys
     * @param key1 First Key
     * @param key2 Second Key
     * @return whether there is a value associated with the given keys
     */
    public boolean containsKey(K1 key1, K2 key2) {
        if(map.get(key1) == null) return false;
        if(map.get(key1).get(key2) == null) return false;
        return true;
    }

    /**
     * Creates and return a Set of Entries as given in the Double Key Map
     * @return Set of Entries
     */
    public Set<Entry<K1, K2, V>> entrySet() {
        Set<Entry<K1, K2, V>> set = new HashSet<>();
        for(Map.Entry<K1, Map<K2, V>> entries: map.entrySet()) {
            for (Map.Entry<K2, V> entry: entries.getValue().entrySet()) {
                set.add(new Entry<>(entries.getKey(), entry.getKey(), entry.getValue()));
            }
        }
        return set;
    }

    public void forEach(TriConsumer<? super K1, ? super K2, ? super V> action) {
        Objects.requireNonNull(action);
        for (Entry<K1, K2, V> entry : entrySet()) {
            K1 k1;
            K2 k2;
            V v;
            try {
                k1 = entry.getKey1();
                k2 = entry.getKey2();
                v = entry.getValue();
            } catch (IllegalStateException ise) {
                // this usually means the entry is no longer in the map.
                throw new ConcurrentModificationException(ise);
            }
            action.accept(k1, k2, v);
        }
    }

    /**
     * Represents an Entry in the Double Key Map
     * @param <K1> Type of the First Key
     * @param <K2> Type of the Second Key
     * @param <V> Type of the Value
     */
    public static class Entry<K1, K2, V> {
        private final K1 key1;
        private final K2 key2;
        private final V value;

        public Entry(K1 key1, K2 key2, V value) {
            this.key1 = key1;
            this.key2 = key2;
            this.value = value;
        }

        /**
         * Get the first Key
         * @return First Key
         */
        public K1 getKey1() {
            return key1;
        }

        /**
         * Get the second Key
         * @return Second Key
         */
        public K2 getKey2() {
            return key2;
        }

        /**
         * Get the value Key
         * @return Value
         */
        public V getValue() {
            return value;
        }
    }
}