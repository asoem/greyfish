package org.asoem.greyfish.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.asoem.greyfish.lang.Functor;

import com.google.common.base.Preconditions;

public class Table {
	private final List<ArrayList<Object>> table = new ArrayList<ArrayList<Object>>();
	private final List<Class<?>> classes = new ArrayList<Class<?>>();
	private final List<String> names = new ArrayList<String>();
	private int rows;
	private final ListenerSupport<TableListener> listenerSupport = new ListenerSupport<TableListener>();

	public Table() {
	}

	public void addTableListener(TableListener listener) {
		listenerSupport.addListener(listener);
	}

	public void removeTableListener(TableListener listener) {
		listenerSupport.removeListener(listener);
	}

	public Table(Class<?>[] classes) {
		this.classes.addAll(Arrays.asList(classes));
	}

	public Table(Collection<Class<?>> classes) {
		this.classes.addAll(classes);
	}

	public <T> void addColumn(String name, Class<T> clazz, T initialValue) {
		classes.add(clazz);
		names.add(name);
		final ArrayList<Object> newCol = new ArrayList<Object>(rows);
		for (int i = 0; i < newCol.size(); i++) {
			newCol.set(i, initialValue);
		}
		table.add(newCol);
		fireColAdded(this, getColumnCount()-1);
	}

	public void addRow(Object[] values) {
		Preconditions.checkArgument(values.length == classes.size());

		for (int i = 0; i < values.length; i++)
			if ( ! classes.get(i).isInstance(values[i]) )
				throw new ClassCastException();
		for (int i = 0; i < values.length; i++)
			table.get(i).add(values[i]);
		++rows;
		fireRowAdded(this, getRowCount()-1);
	}

	public void setValueAt(int col, int row, Object value) {
		Preconditions.checkArgument(classes.get(col).isInstance(value), "Class of value does not match Class of target column.");

		final Object oldValue = getValueAt(col, row);
		table.get(col).set(row, value);
		fireTableEntryChanged(this, col, row, oldValue, value);
	}

	public Object getValueAt(int col, int row) {
		Preconditions.checkArgument(col < classes.size() && row < rows, "Index out of bounds.");
		return table.get(col).get(row);
	}

	public int getColumnCount() {
		return classes.size();
	}

	public int getRowCount() {
		return rows;
	}

	public String getColumnName(int column) {
		Preconditions.checkArgument(getColumnCount() > column, "Index out of bounds.");
		return names.get(column);
	}

	public Class<?> getColumnClass(int column) {
		Preconditions.checkArgument(getColumnCount() > column, "Index out of bounds.");
		return classes.get(column);
	}

	private void fireRowAdded(final Table t, final int index) {
		listenerSupport.notifyListeners( new Functor<TableListener>() {

			@Override
			public void update(TableListener listener) {
				listener.rowAdded(t, index);
			}
		});
	}

	private void fireColAdded(final Table t, final int index) {
		listenerSupport.notifyListeners( new Functor<TableListener>() {

			@Override
			public void update(TableListener listener) {
				listener.columnAdded(t, index);
			}
		});
	}

	private void fireTableEntryChanged(final Table t, final int column, final int row, final Object oldValue, final Object newValue) {

		listenerSupport.notifyListeners( new Functor<TableListener>() {

			@Override
			public void update(TableListener listener) {
				listener.tableEntryChanged(t, column, row, oldValue, newValue);
			}
		});
	}
}
