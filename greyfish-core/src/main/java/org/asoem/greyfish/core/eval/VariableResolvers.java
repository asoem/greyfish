package org.asoem.greyfish.core.eval;

import com.google.common.collect.ForwardingMap;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.Map;


public final class VariableResolvers {

    private VariableResolvers() {}

    public static VariableResolver forMap(final Map<String, ?> map) {

        return new AbstractVariableResolver() {
            @Override
            public boolean canResolveLocal(final String name) {
                return map.containsKey(name);
            }

            @Override
            public Object resolveLocal(final String varName) throws VariableResolutionException {
                return map.get(varName);
            }

            @Override
            public Bindings bindings() {
                return new BindingsAdaptor((Map<String, Object>) map);
            }
        };
    }

    public static VariableResolver emptyResolver() {
        return new AbstractVariableResolver() {

            private final Bindings bindings = new SimpleBindings();

            @Override
            public Bindings bindings() {
                return bindings;
            }

            @Override
            public boolean canResolveLocal(final String name) {
                return false;
            }

            @Override
            public Object resolveLocal(final String varName) throws VariableResolutionException {
                throw new VariableResolutionException("No variable can be resolved by " + this + " and therefore also not variable " + varName);
            }
        };
    }

    private static class BindingsAdaptor extends ForwardingMap<String, Object> implements Bindings {
        private final Map<String, Object> map;

        public BindingsAdaptor(final Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public Object put(final String name, final Object value) {
            return new UnsupportedOperationException();
        }

        @Override
        protected Map<String, Object> delegate() {
            return map;
        }
    }
}
