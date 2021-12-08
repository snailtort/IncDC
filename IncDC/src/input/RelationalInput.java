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