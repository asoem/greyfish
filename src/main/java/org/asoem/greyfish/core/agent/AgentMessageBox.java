package org.asoem.greyfish.core.agent;

import com.google.common.base.Predicate;
import org.asoem.greyfish.utils.collect.Searchable;
import org.asoem.greyfish.utils.collect.SearchableCollection;

import java.util.Collection;
import java.util.List;

/**
 * User: christoph
 * Date: 09.10.12
 * Time: 14:37
 */
public interface AgentMessageBox<A extends Agent<A, ?>> extends SearchableCollection<AgentMessage<A>> {
    List<AgentMessage<A>> extract(Predicate<? super AgentMessage<A>> predicate);
}
