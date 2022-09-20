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

import com.google.common.collect.HashMultiset;

public class ParsedColumn<T extends Comparable<T>> {
	private final String tableName;
	private final String name;
	private final HashMultiset<T> valueSet = HashMultiset.create();
	private final List<T> values = new ArrayList<>();
	private final Class<T> type;
	private final int index;

	public ParsedColumn(String tableName, String name, Class<T> type, int index) {
		this.tableName = tableName;
		this.name = name;
		this.type = type;
		this.index = index;
	}

	public void addLine(T value) {
		valueSet.add(value);
		values.add(value);
	}

	public T getValue(int line) {
		return values.get(line);
	}

	public String getTableName() {
		return tableName;
	}

	public String getName() {
		return name;
	}

/*	public ColumnIdentifier getColumnIdentifier() {
		return new ColumnIdentifier(tableName, name);
	}
*/

	public int getIndex() {
		return index;
	}

	public Class<T> getType() {
		return type;
	}

	@Override
	public String toString() {
		return /*tableName + "." + */name;
	}

	public boolean isComparableType() {
		return getType().equals(Double.class) || getType().equals(Long.class);
	}

	public double getAverage() {
		double avg = 0.0d;
		int size = values.size();
		if (type.equals(Double.class)) {
			for (int i = 0; i < size; i++) {
				Double l = (Double) values.get(i);
				double tmp = l.doubleValue() / size;
				avg += tmp;
			}
		} else if (type.equals(Long.class)) {
			for (int i = 0; i < size; i++) {
				Long l = (Long) values.get(i);
				double tmp = l.doubleValue() / size;
				avg += tmp;
			}
		}

		return avg;
	}

	public double getSharedPercentage(ParsedColumn<?> c2) {
		int totalCount = 0;
		int sharedCount = 0;
		for (T s : valueSet.elementSet()) {
			int thisCount = valueSet.count(s);
			int otherCount = c2.valueSet.count(s);
			sharedCount += Math.min(thisCount, otherCount);
			totalCount += Math.max(thisCount, otherCount);
		}
		return ((double) sharedCount) / ((double) totalCount);
	}

	public String getColumnIdentifier() {
		// TODO Auto-generated method stub
		return name;
	}

}
