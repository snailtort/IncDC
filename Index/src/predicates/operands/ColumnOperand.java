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
package predicates.operands;

import input.ParsedColumn;

public class ColumnOperand<T extends Comparable<T>> {
	private ParsedColumn<T> column;
	private int index;

	public ColumnOperand(ParsedColumn<T> column, int index) {
		this.column = column;
		this.index = index;
	}

    public ColumnOperand(){

	}

	/**
	 * 获取该列在数据表中的列数
	 * @return
	 */
	public int getColumnIndex(){
		return column.getIndex()+1;
	}

    public T getValue(int line1, int line2) {
		return column.getValue(index == 0 ? line1 : line2);
	}

	public ParsedColumn<T> getColumn() {
		return column;
	}

	public int getIndex() {
		return index;
	}

	public ColumnOperand<T> getInvT1T2() {
		return new ColumnOperand<>(getColumn(), index == 0 ? 1 : 0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((column == null) ? 0 : column.hashCode());
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColumnOperand<?> other = (ColumnOperand<?>) obj;
		if (column == null) {
			if (other.column != null)
				return false;
		} else if (!column.equals(other.column))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "t" + index + "." + column.toString();
	}

	public String getcolumn() {
		// TODO Auto-generated method stub
		return column.toString();
	}
}
