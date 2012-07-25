package org.asoem.greyfish.utils.base;

import com.google.common.base.Joiner;
import com.google.common.collect.ForwardingMap;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * User: christoph
 * Date: 17.07.12
 * Time: 11:50
 */
public class ArgumentMap extends ForwardingMap<String, Object> implements Arguments {

    private final Map<String, Object> map;

    public ArgumentMap(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    protected Map<String, Object> delegate() {
        return map;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public Object get(Object identifier) {
        checkArgument(identifier instanceof String, "Argument identifier must be a String");
        if (!map.containsKey(identifier))
            throw new IllegalArgumentException("No argument with identifier " + identifier + " was found. Possible identifiers are: " + Joiner.on(", ").join(map.keySet()));
        return map.get(identifier);
    }

    public static Arguments of(String identifier, Object o) {
        return new ArgumentMap(ImmutableMap.of(identifier, o));
    }

    public static Arguments of(String i1, Object o1, String i2, Object o2) {
        return new ArgumentMap(ImmutableMap.of(i1, o1, i2, o2));
    }

    public static Arguments of() {
        return EMPTY_MAP;
    }

    private static final ArgumentMap EMPTY_MAP = new ArgumentMap(ImmutableMap.<String, Object>of());
}
