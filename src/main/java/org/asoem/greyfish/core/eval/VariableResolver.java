package org.asoem.greyfish.core.eval;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * User: christoph
 * Date: 18.05.11
 * Time: 17:00
 */
public interface VariableResolver {
      @Nullable
      Object resolve(@Nonnull String varName);
}
