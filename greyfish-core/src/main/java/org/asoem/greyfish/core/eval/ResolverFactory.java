package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;


public interface ResolverFactory<T> extends Function<T, VariableResolver> {
}
