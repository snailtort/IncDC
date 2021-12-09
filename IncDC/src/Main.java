import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

import algorithms.hybrid.Incdc;
import input.Input;
import input.OriginDC;
import input.RelationalInput;
import denialconstraints.DenialConstraint;
import denialconstraints.DenialConstraintSet;
import predicates.PredicateBuilder;


public class Main{

	public static void main(String[] args) throws Exception {

		String line="example//atom_original.csv";// original dataset
		String incline="example//atom_incremental.csv";// incremental dataset
		String dcline="example//DC_atom_example.txt";// DCs on original dataset
		String indexline="example//index_example_atom.txt";// index file , namely predicate space
		int n=10500;// tuples in original dataset
		int m=3500;// tuples in incremental dataset
		int x=0;// threshold for column select

//		String line =args[0];
//		String incline =args[1];
//		String odline =args[2];
//		String indexline=args[3];
//		int n=Integer.valueOf(args[4]);
//		int m=Integer.valueOf(args[5]);
//		int x=Integer.valueOf(args[6]);

		int size=m;
		double alpha = 0.6;
		if(args.length>=8) {
			if(args[7].contains("size=")) {String s=args[7].replaceAll("size=","");size=Integer.parseInt(s);}
			if(args[7].contains("l=")) {String s=args[7].replaceAll("l=","");alpha=1-Double.parseDouble(s);}
		}

		if(args.length>=9) {
			if(args[8].contains("size=")) {String s=args[8].replaceAll("size=","");size=Integer.parseInt(s);}
			if(args[8].contains("l=")) {String s=args[8].replaceAll("l=","");alpha=1-Double.parseDouble(s);}
		}
		File file = new File(line);
		File incfile = new File(incline);
		File od = new File(dcline);
		File index=new File(indexline);

		RelationalInput data = new RelationalInput(file);
		RelationalInput incdata = new RelationalInput(incfile);
		Input input = new Input(data,incdata,n,m);

		System.out.println("data name : "+ line);
		System.out.println("origin data size : "+n);
		System.out.println("incremental data size : "+m);
		System.out.println("tuples in a single round : "+size);
		PredicateBuilder predicates = new PredicateBuilder(index,input);
		System.out.println("predicate space: "+predicates.getPredicates().size());
		System.out.println("threshold x: "+x);
//		PredicateBuilder predicates = new PredicateBuilder(input, false, 0.30d);
		OriginDC origin=new OriginDC(od,input.getColumns());
		System.out.println("total constraints size : "+origin.total.size());
		Incdc incdc = new Incdc();
		System.out.println("------------------ now is IncDC for DC -------------------");

		DenialConstraintSet dcs = incdc.run(input,predicates,origin,alpha,n,m,x,size);
		System.out.println("total used time(excluding time for building indexes) : "+ incdc.time+ " ms");
		System.out.println("minimal DCs size : "+dcs.size());

		String res="";
		int count=-1;
		for(Iterator<DenialConstraint> iter = dcs.iterator(); iter.hasNext();){
			count++;
		    res+="this is "+count+" "+iter.next().toString()+"\n";
		}
		try{
			String miniodline =line.replaceAll(".csv", "_")+"inc_DCs.txt";

			System.out.println("Writting # of dcs to file : "+miniodline);
			File miniodfile =new File(miniodline);
			FileWriter fileWritter = new FileWriter(miniodfile);
			fileWritter.write(res);
			if(!miniodfile.exists()){
				miniodfile.createNewFile();
			}
			fileWritter.close();
			System.out.println("Write dc Done");
		}catch(IOException e){
			e.printStackTrace();
		}

	}
}