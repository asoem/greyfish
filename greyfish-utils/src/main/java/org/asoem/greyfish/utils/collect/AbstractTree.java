package org.asoem.greyfish.utils.collect;

import com.google.common.collect.Iterators;
import com.google.common.collect.TreeTraverser;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Iterator;

public abstract class AbstractTree<T> extends AbstractCollection<T> implements Tree<T> {

    @Override
    public Iterator<T> iterator() {
        if (rootNode().isPresent()) {
            return getTreeTraverser().postOrderTraversal(rootNode().get()).iterator();
        } else {
            return Iterators.emptyIterator();
        }
    }

    @Override
    public int size() {
        return Iterators.size(iterator());
    }

    @Nullable
    @Override
    public T root() {
        return rootNode().orNull();
    }

    protected abstract TreeTraverser<T> getTreeTraverser();
}
