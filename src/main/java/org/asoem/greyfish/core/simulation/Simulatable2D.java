package org.asoem.greyfish.core.simulation;

import org.asoem.greyfish.utils.space.Motion2D;
import org.asoem.greyfish.utils.space.Moving;
import org.asoem.greyfish.utils.space.Object2D;
import org.asoem.greyfish.utils.space.Projectable;

/**
 * User: christoph
 * Date: 29.02.12
 * Time: 13:06
 */
public interface Simulatable2D extends Simulatable, Projectable<Object2D>, Moving<Motion2D> {
}
