package org.asoem.greyfish.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTable;

public class CSVJTableDataExporter extends JTableDataExporter {

	protected String columnSeparator = ";";
	private boolean headerEnabled = true;

	public CSVJTableDataExporter(JTable table, OutputStream stream) {
		super(table, stream);
	}

	public CSVJTableDataExporter(JTable table, File file)
	throws IOException {
		super(table, file);
	}

	@Override
	protected void export(int[] rowIndices, int[] columnIndices) throws IOException {
		StringBuffer buffer = new StringBuffer();

		// Write header if enabled
		if (isHeaderEnabled()) {
			for (int i : columnIndices) {
				buffer.append( table.getColumnName(i) );
				buffer.append(columnSeparator);
			}

			buffer.delete(buffer.length() - columnSeparator.length(), buffer.length());

			writer.write(buffer.toString());
			buffer.setLength(0);
			writer.newLine();
		}

		for (int i : rowIndices) {
			for (int j : columnIndices) {
				buffer.append( table.getModel().getValueAt(i, j) );
				buffer.append( columnSeparator );
			}

			buffer.delete(buffer.length() - columnSeparator.length(), buffer.length());

			writer.write(buffer.toString());
			buffer.setLength(0);
			writer.newLine();
		}

		writer.flush();
	}

	public String getColumnSeparator() {
		return columnSeparator;
	}

	public void setColumnSeparator(String columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	public void setHeaderEnabled(boolean headerEnabled) {
		this.headerEnabled = headerEnabled;
	}

	public boolean isHeaderEnabled() {
		return headerEnabled;
	}
}
