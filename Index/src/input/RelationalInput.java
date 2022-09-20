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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Relational inputs can be iterated, but iterators may throw {@link InputIterationException}s when
 * iterating.
 *
 * @author Jakob Zwiener
 */
public class RelationalInput{
	
	private BufferedReader br;
	public int numberOfColumns;
	public String relationName;
	public String[] columnNames;
	public List<String> next = new ArrayList<String>();
	
	public RelationalInput(File file) throws IOException {
		br= new BufferedReader(new FileReader(file));
		columnNames=br.readLine().split(",");
		numberOfColumns=columnNames.length;
		relationName = file.getName();
		System.out.println(numberOfColumns);
	}
	
	public boolean hasNext() {
		// TODO Auto-generated method stub
		String[] a = new String[numberOfColumns];
		String line;
		next.clear();
		try {
			if((line=br.readLine())==null) return false;
			a=line.split(",");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String s:a) next.add(s);
		if(next.get(0)==null) return false;
		return true;
	}

	public List<String> next() {
		// TODO Auto-generated method stub
		return next;
	}

	public int numberOfColumns() {
		// TODO Auto-generated method stub
		return numberOfColumns;
	}

	public String relationName() {
		// TODO Auto-generated method stub
		return relationName;
	}

	public String[] columnNames() {
		// TODO Auto-generated method stub
		return columnNames;
	}

}