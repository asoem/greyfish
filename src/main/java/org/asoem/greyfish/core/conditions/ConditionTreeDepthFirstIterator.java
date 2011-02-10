package org.asoem.greyfish.core.conditions;

import java.util.*;

public class ConditionTreeDepthFirstIterator implements Iterator<GFCondition> {

	private final Stack<GFCondition> stack = new Stack<GFCondition>();
	private final Stack<Integer> depthStack = new Stack<Integer>();

	private int currentDepth = -1;

	private ConditionTreeDepthFirstIterator(GFCondition condition) {
		if (condition != null) {
			stack.push(condition);
			depthStack.push(0);
		}
	}

	@Override
	public boolean hasNext() {
		return ! stack.empty();
	}

	@Override
	public GFCondition next() {
		if ( ! hasNext() ) {
			throw new NoSuchElementException("tree ran out of elements");
		}
		GFCondition node = stack.pop( );
		currentDepth = depthStack.pop();
		if (!node.isLeafCondition()) {
			final List<GFCondition> childConditionsList = node.getChildConditions();
			for (ListIterator<GFCondition> iterator = childConditionsList.listIterator(childConditionsList.size()); iterator.hasPrevious();) {
				GFCondition condition = iterator.previous();
				stack.push(condition);
				depthStack.push(currentDepth+1);
			}
		}
		return node;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public int getDepth() {
		return currentDepth;
	}

    public static ConditionTreeDepthFirstIterator forRoot(final GFCondition root) {
        return new ConditionTreeDepthFirstIterator(root);
    }
}
