package org.asoem.greyfish.utils.space;

/**
 * User: christoph
 * Date: 25.06.12
 * Time: 14:43
 */
public interface MovingObject2D extends Moving<Motion2D>, Projectable<Object2D> {
    void collision(MovingObject2D other);
}
