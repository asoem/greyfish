package org.asoem.greyfish.utils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

/*
 *  This class listens for changes made to the data in the table via the
 *  TableCellEditor. When editing is started, the value of the cell is saved
 *  When editing is stopped the new value is saved. When the oold and new
 *  values are different, then the provided Action is invoked.
 *
 *  The source of the Action is a TableCellListener instance.
 */
public class TableCellListenerSupport implements PropertyChangeListener, Runnable
{
	private JTable table;

	private int row;
	private int column;
	private Object oldValue;
	private Object newValue;

	private Vector<TableCellListener> list = new Vector<TableCellListener>();

	public void addTableCellListener(TableCellListener l) {
		list.add(l);
	}

	public void removePropertyChangeListener(TableCellListener l) {
		list.remove(l);
	}


	/**
	 *  Create a TableCellListener.
	 *
	 *  @param table   the table to be monitored for data changes
	 *  @param action  the Action to invoke when cell data is changed
	 */
	public TableCellListenerSupport(JTable table)
	{
		this.table = table;
		this.table.addPropertyChangeListener( this );
	}

	/**
	 *  Get the column that was last edited
	 *
	 *  @return the column that was edited
	 */
	public int getColumn()
	{
		return column;
	}

	/**
	 *  Get the new value in the cell
	 *
	 *  @return the new value in the cell
	 */
	public Object getNewValue()
	{
		return newValue;
	}

	/**
	 *  Get the old value of the cell
	 *
	 *  @return the old value of the cell
	 */
	public Object getOldValue()
	{
		return oldValue;
	}

	/**
	 *  Get the row that was last edited
	 *
	 *  @return the row that was edited
	 */
	public int getRow()
	{
		return row;
	}

	/**
	 *  Get the table of the cell that was changed
	 *
	 *  @return the table of the cell that was changed
	 */
	public JTable getTable()
	{
		return table;
	}
	//
	//  Implement the PropertyChangeListener interface
	//
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		//  A cell has started/stopped editing

		if ("tableCellEditor".equals(e.getPropertyName()))
		{
			if (table.isEditing())
				processEditingStarted();
			else
				processEditingStopped();
		}
	}

	/*
	 *  Save information of the cell about to be edited
	 */
	private void processEditingStarted()
	{
		//  The invokeLater is necessary because the editing row and editing
		//  column of the table have not been set when the "tableCellEditor"
		//  PropertyChangeEvent is fired.
		//  This results in the "run" method being invoked

		SwingUtilities.invokeLater( this );
	}
	/*
	 *  See above.
	 */
	@Override
	public void run()
	{
		row = table.convertRowIndexToModel( table.getEditingRow() );
		column = table.convertColumnIndexToModel( table.getEditingColumn() );
		oldValue = table.getModel().getValueAt(row, column);
		newValue = null;
	}

	/*
	 *	Update the Cell history when necessary
	 */
	private void processEditingStopped()
	{
		newValue = table.getModel().getValueAt(row, column);

		if (oldValue != newValue) {

			TableCellEvent event = new TableCellEvent(row, column, oldValue, newValue);

			for (Iterator<TableCellListener> i=list.iterator(); i.hasNext(); ) {
				TableCellListener l = i.next();
				try {
					l.cellChange(event);
				}
				catch (RuntimeException e) {
					Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).warning("Unexpected exception in listener");
					i.remove();
				}
			}
		}
	}
}

