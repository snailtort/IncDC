import java.io.*;
import java.util.*;


import chains.Chains;
import input.Input;
import input.OriginDC;
import input.ParsedColumn;
import input.RelationalInput;
import predicates.PredicateBuilder;


public class Main{

	public static void main(String[] args) throws Exception {

//		String line="example//airport_original.csv";// original dataset
//		String dcline="example//DC_airport_example.txt";// DCs on original dataset
//		int n=30000;// tuples in original dataset

		String line =args[0];
		String dcline =args[1];
		int n=Integer.valueOf(args[2]);


		double alpha = 0.6;
		if(args.length>=4){
			if(args[3].contains("l=")) {String s=args[3].replaceAll("l=","");alpha=1-Double.parseDouble(s);}
		}

		File file = new File(line);
		File dc = new File(dcline);

		String indexLine = line.replaceAll(".csv", "_") + "index.ind";
		File indexFile = new File(indexLine);

		RelationalInput data = new RelationalInput(file);
		Input input = new Input(data,n);

		System.out.println("data name : "+ line);
		System.out.println("origin data size : "+n);
		PredicateBuilder predicates = new PredicateBuilder(input, false, 0.30d);
		System.out.println("predicate space: "+predicates.getPredicates().size());
		OriginDC origin=new OriginDC(dc,input.getColumns());
		System.out.println("total constraints size : "+origin.total.size());

		System.out.println("------------------ now is IncDC for DC -------------------");

		ParsedColumn[] cols = input.getColumns();
		int[][] inputs = input.getInts();

		Map<Integer,String> column = new HashMap<>();
		for (int i = 0; i < cols.length; ++i) {
			column.put(i, cols[i].getName());
		}
		int columncount = cols.length;
		int[][] input_data = new int[n][columncount + 1];
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < columncount; ++j) {
				input_data[i][0] = i;
				input_data[i][(j + 1)] = inputs[i][j];
			}
		}

		int threshold=(n)/10000;
		Set<Integer> columnSet=new HashSet<>();
		for(int i=0;i<=columncount;i++){
			Set<Integer> set=new HashSet<>();
			for(int j=0;j<(n);j++){
				set.add(input_data[j][i]);
			}
			if(set.size()<threshold)
				columnSet.add(i);
		}
		System.out.println("building index");
		Chains chains = new Chains(origin.getTotal().getDc(), input_data, alpha, column,columnSet);
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile));
		oos.writeObject(chains);
		oos.flush();
		oos.close();
		System.out.println("write done");
	}
}
