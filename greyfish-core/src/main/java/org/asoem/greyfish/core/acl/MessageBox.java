package org.asoem.greyfish.core.acl;

import com.google.common.base.Predicate;
import org.asoem.greyfish.utils.collect.FunctionalCollection;

import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 14:37
 */
public interface MessageBox<M extends ACLMessage<?>> extends FunctionalCollection<M> {
    /**
     * Get all messages satisfying the predicate and remove them from the box
     *
     * @param predicate The predicate a message has to satisfy
     * @return All messages that satisfy the predicate
     */
    List<M> extract(Predicate<? super M> predicate);
}
