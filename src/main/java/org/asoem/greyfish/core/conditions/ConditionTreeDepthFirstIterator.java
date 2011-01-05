package org.asoem.sico.core.conditions;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Stack;

public class ConditionTreeDepthFirstIterator implements Iterator<GFCondition> {

	private Stack<GFCondition> stack = new Stack<GFCondition>();
	private Stack<Integer> depthStack = new Stack<Integer>();

	private int currentDepth = -1;

	public ConditionTreeDepthFirstIterator(GFCondition condition) {
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
		if (node.isLeafCondition() == false) {
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
}
