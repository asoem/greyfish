package org.asoem.greyfish.core.individual;

import java.util.Map;

/**
 * User: christoph
 * Date: 15.05.12
 * Time: 11:39
 */
public interface Callback<C, T> {
      T apply(C caller, Map<? super String, ?> localVariables);
}
