package org.asoem.greyfish.lang;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.asoem.greyfish.utils.collect.TreeNode;
import org.asoem.greyfish.utils.collect.Trees;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * User: christoph
 * Date: 05.10.11
 * Time: 10:48
 *
 * The tree is taken from the example at http://en.wikipedia.org/wiki/Tree_traversal#Example
 */
@RunWith(MockitoJUnitRunner.class)
public class TreesTest {

    final TreeNodeImpl a = node("A");
    final TreeNodeImpl c = node("C");
    final TreeNodeImpl e = node("E");
    final TreeNodeImpl d = node("D", c, e);
    final TreeNodeImpl b = node("B", a, d);
    final TreeNodeImpl h = node("H");
    final TreeNodeImpl i = node("I", h);
    final TreeNodeImpl g = node("G", i);
    final TreeNodeImpl f = node("F", b, g);

    @Test
    public void postOrderViewTest() {
        // when
        Iterator<TreeNodeImpl> postOrderView = Trees.postOrderView(f, new Function<TreeNodeImpl, Iterator<? extends TreeNodeImpl>>() {
            @Override
            public Iterator<? extends TreeNodeImpl> apply(@Nullable TreeNodeImpl stringTreeNode) {
                assert stringTreeNode != null;
                return stringTreeNode.childConditions().iterator();
            }
        });

        // then
        assertEquals(ImmutableList.of(a, c, e, d, b, h, i, g, f), ImmutableList.copyOf(postOrderView));
    }

    @Test
    public void preOrderViewTest() {
        // when
        Iterator<TreeNodeImpl> postOrderView = Trees.preOrderView(f, new Function<TreeNodeImpl, Iterator<? extends TreeNodeImpl>>() {
            @Override
            public Iterator<? extends TreeNodeImpl> apply(@Nullable TreeNodeImpl stringTreeNode) {
                assert stringTreeNode != null;
                return stringTreeNode.childConditions().iterator();
            }
        });

        // then
        assertEquals(ImmutableList.of(f, b, a, d, c, e, g, i, h), ImmutableList.copyOf(postOrderView));
    }

    private static TreeNodeImpl node(String name) {
        return new TreeNodeImpl(ImmutableList.<TreeNodeImpl>of(), name);
    }

    private static TreeNodeImpl node(String name, TreeNodeImpl... children) {
        return new TreeNodeImpl(ImmutableList.copyOf(children), name);
    }

    private static class TreeNodeImpl implements TreeNode<TreeNodeImpl> {

        private final String name;

        private final Iterable<TreeNodeImpl> children;

        public TreeNodeImpl(Iterable<TreeNodeImpl> treeNodes, String name) {
            this.children = treeNodes;
            this.name = name;
        }

        @Override
        public Iterable<TreeNodeImpl> childConditions() {
            return children;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
