package network;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> extends AbstractSet<E> {
    private static final Object PRESENT = new Object();
    private final ConcurrentHashMap<E, Object> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }
}
