/*
 * =============================================================================

 * Copyright (c) 2017, Tobias Bleifuß, Sebastian Kruse, Felix Naumann
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */
package input;

import java.util.ArrayList;
import java.util.List;

import helpers.IndexProvider;

public class Input {
	private final int lineCount;
	private final List<ParsedColumn<?>> parsedColumns;
	private final String name;

	public Input(RelationalInput relationalInput, int rowLimit) throws InputIterationException {
		final int columnCount = relationalInput.numberOfColumns();
		Column[] columns = new Column[columnCount];
		for (int i = 0; i < columnCount; ++i) {
			columns[i] = new Column(relationalInput.relationName(), relationalInput.columnNames[i]);
		}

		int lineCount = 0;
		while (relationalInput.hasNext()) {
			List<String> line = relationalInput.next();
			for (int i = 0; i < columnCount; ++i) {
				columns[i].addLine(line.get(i));
			}
			++lineCount;
			if (rowLimit > 0 && lineCount >= rowLimit)
				break;
		}
		this.lineCount = lineCount;

		parsedColumns = new ArrayList<>(columns.length);
		createParsedColumns(relationalInput, columns);

		name = relationalInput.relationName();
	}

	private void createParsedColumns(RelationalInput relationalInput, Column[] columns) {
		int i = 0;
		for (Column c : columns) {
			switch (c.getType()) {
			case Integer:
			case LONG: {
				ParsedColumn<Long> parsedColumn = new ParsedColumn<Long>(relationalInput.relationName(), c.getName(),
						Long.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getLong(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			case NUMERIC: {
				ParsedColumn<Double> parsedColumn = new ParsedColumn<Double>(relationalInput.relationName(),
						c.getName(), Double.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getDouble(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			case STRING: {
				ParsedColumn<String> parsedColumn = new ParsedColumn<String>(relationalInput.relationName(),
						c.getName(), String.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getString(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			default:
				break;
			}

			++i;
		}
	}

	public int getLineCount() {
		return lineCount;
	}

	public ParsedColumn<?>[] getColumns() {
		return parsedColumns.toArray(new ParsedColumn[0]);
	}

	public String getName() {
		return name;
	}

	public int[][] getInts() {
		final int COLUMN_COUNT = parsedColumns.size();
		final int ROW_COUNT = getLineCount();

		//long time = System.currentTimeMillis();
		int[][] input2s = new int[ROW_COUNT][COLUMN_COUNT];
		IndexProvider<String> providerS = new IndexProvider<>();
		IndexProvider<Long> providerL = new IndexProvider<>();
		IndexProvider<Double> providerD = new IndexProvider<>();
		for (int col = 0; col < COLUMN_COUNT; ++col) {

			if (parsedColumns.get(col).getType() == String.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else if (parsedColumns.get(col).getType() == Double.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

				}
			} else if (parsedColumns.get(col).getType() == Long.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
				}
			}
		}
		providerS = IndexProvider.getSorted(providerS);
		providerL = IndexProvider.getSorted(providerL);
		providerD = IndexProvider.getSorted(providerD);
		for (int col = 0; col < COLUMN_COUNT; ++col) {
			if (parsedColumns.get(col).getType() == String.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else if (parsedColumns.get(col).getType() == Double.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

				}
			} else if (parsedColumns.get(col).getType() == Long.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
				}
			}
		}

		return input2s;
	}


}
