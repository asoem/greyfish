package org.asoem.sico.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.swing.JTable;

public abstract class JTableDataExporter implements Closeable {

	protected JTable table;
	protected BufferedWriter writer;

	public JTableDataExporter(JTable table, OutputStream stream) {
		this.setTable(table);
		this.setWriter(new BufferedWriter( new OutputStreamWriter ( ((stream instanceof BufferedOutputStream) ? stream : new BufferedOutputStream( stream )))));
	}

	public JTableDataExporter(JTable table, File file) throws IOException {
		this.setTable(table);
		this.setWriter( new BufferedWriter( new FileWriter( file )));
	}

	public void export() throws IOException {
		int[] rowIndices = new int[table.getRowCount()];
		for (int i = 0; i < rowIndices.length; i++)
			rowIndices[i] = i;

		int[] columnIndices = new int[table.getColumnCount()];
		for (int i = 0; i < columnIndices.length; i++)
			columnIndices[i] = i;

		export(rowIndices, columnIndices);
	}

	public void exportSelection() throws IOException {
		int[] columnIndices = new int[table.getColumnCount()];
		for (int i = 0; i < columnIndices.length; i++)
			columnIndices[i] = i;

		export(table.getSelectedRows(), columnIndices);
	}

	protected abstract void export(int[] rowIndices, int[] columnIndices) throws IOException;

	public void setTable(JTable table) {
		if (table == null)
			throw new IllegalArgumentException("table must not be null");
		this.table = table;
	}

	public JTable getTable() {
		return table;
	}

	public void setWriter(BufferedWriter writer) {
		if(writer == null)
			throw new IllegalArgumentException("writer must not be null");
		this.writer = writer;
	}

	public BufferedWriter getWriter() {
		return writer;
	}

	@Override
	public void close() throws IOException {
		writer.close();
	}
}
