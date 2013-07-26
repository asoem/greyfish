package org.asoem.greyfish.core.eval;

import com.google.common.base.Function;

/**
 * User: christoph
 * Date: 13.09.11
 * Time: 12:06
 */
public interface ResolverFactory<T> extends Function<T, VariableResolver> {
}
