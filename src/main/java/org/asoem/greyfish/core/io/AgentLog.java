package org.asoem.greyfish.core.io;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * User: christoph
 * Date: 28.04.11
 * Time: 15:20
 */
public class AgentLog {

    private final Map<String, Object> map = Maps.newHashMap();

    private final AgentLogFactory agentLogFactory;

    public AgentLog(AgentLogFactory agentLogFactory) {
        this.agentLogFactory = agentLogFactory;
    }

    public void set(String key, Object value) {
        map.put(key, value);
    }

    public void add(String key, int value) {
        map.put(key, oldOrNew(key, Integer.class, 0) + value);
    }

    public void add(String key, double value) {
        map.put(key, oldOrNew(key, Double.class, 0.0) + value);
    }

    public void subtract(String key, int value) {
        map.put(key, oldOrNew(key, Integer.class, 0) - value);
    }

    public void subtract(String key, double value) {
        map.put(key, oldOrNew(key, Double.class, 0.0) - value);
    }

    public void commit() throws IOException {
        agentLogFactory.commit(this);
    }

    public Map<String, Object> getMap() {
        return Collections.unmodifiableMap(map);
    }

    private <T extends Number> T oldOrNew(String key, Class<T> clazz, T fallback) {
        return (map.containsKey(key)) ? clazz.cast(map.get(key)) : fallback;
    }
}
