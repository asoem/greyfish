package org.asoem.sico.utils;

public interface TableListener {

	public void rowAdded(Table source, Integer rowIndex);
	public void columnAdded(Table source, Integer colIndex);
	public void tableEntryChanged(Table source, int column, int row, Object oldValue,
			Object newValue);
}
