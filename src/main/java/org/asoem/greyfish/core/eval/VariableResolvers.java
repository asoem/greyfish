package org.asoem.greyfish.core.eval;

import com.google.common.collect.ForwardingMap;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.util.Map;

/**
 * User: christoph
 * Date: 20.04.11
 * Time: 14:22
 */
public class VariableResolvers {
    public static VariableResolver forMap(final Map<String, ?> map) {

        return new AbstractVariableResolver() {
            @Override
            public boolean canResolveLocal(String name) {
                return map.containsKey(name);
            }

            @Override
            public Object resolveLocal(String varName) throws VariableResolutionException {
                return map.get(varName);
            }

            @Override
            public Bindings bindings() {
                return new BindingsAdaptor(Maps.transformEntries(map, new Maps.EntryTransformer<String, Object, Object>() {
                    @Override
                    public Object transformEntry(@Nullable String s, @Nullable Object o) {
                        return o;
                    }
                }));
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
            public boolean canResolveLocal(String name) {
                return false;
            }

            @Override
            public Object resolveLocal(String varName) throws VariableResolutionException {
                throw new VariableResolutionException("No variable can be resolved by " + this + " and therefore also not variable " + varName);
            }
        };
    }

    private static class BindingsAdaptor extends ForwardingMap<String, Object> implements Bindings {
        private final Map<String, Object> map;

        public BindingsAdaptor(Map<String,Object> map) {
            this.map = map;
        }

        @Override
        public Object put(String name, Object value) {
            return new UnsupportedOperationException();
        }

        @Override
        protected Map<String, Object> delegate() {
            return map;
        }
    }
}
