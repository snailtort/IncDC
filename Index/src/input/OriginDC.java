package input;

import java.util.ArrayList;
import java.util.List;

import evidenceset.build.Operator;
import denialconstraints.DenialConstraint;
import denialconstraints.DenialConstraintSet;
import predicates.Predicate;
import predicates.operands.ColumnOperand;

import java.io.*;


public class OriginDC {
	public DenialConstraintSet total=new DenialConstraintSet();
	public static final String NOT = "\u00AC";
	public static final String AND = "∧";

	public OriginDC(File hydra, ParsedColumn<?> [] col){
		int columncount=col.length;
		String list;
		try{
			BufferedReader br = new BufferedReader(new FileReader(hydra));
			String s = null;
			while((s = br.readLine())!=null){
				if(s.length()==0) continue;
				s=s.substring(s.indexOf("[")+5);
				List<Predicate> ls = new ArrayList<Predicate>();;
				while(s.contains(")") || s.contains("]")) {
					boolean flag = false;
					String col1 = s.substring(s.indexOf("t0")+3,s.indexOf(")")+1);
					s=s.substring(s.indexOf(")")+1);
					String op = s.substring(1,s.indexOf("t")-1);
					Operator oper=getoperator(op);
					String col2 = s.substring(s.indexOf("t")+3,s.indexOf(")")+1);
					//char num;
					if(s.charAt(s.indexOf("t")+1)=='0'){
						flag=true;
					}
					if(s.contains("  ") && s.contains("t0")) s=s.substring(s.indexOf("  ")+1);
					else s = "-1";
					boolean flag1=false;
					boolean flag2=false;
					ColumnOperand operand1 = new ColumnOperand();
					ColumnOperand operand2 = new ColumnOperand();
					for(int i=0;i<columncount;i++) {
						if(col[i].getName().equals(col1)) {
							operand1=new ColumnOperand(col[i],0);
							flag1=true;
						}
						if(col[i].getName().equals(col2)) {
							if(flag) operand2=new ColumnOperand(col[i],0);
							else operand2=new ColumnOperand(col[i],1);
							flag2=true;
						}
						if(flag1 && flag2){
							Predicate e = new Predicate(oper,operand1,operand2);
							ls.add(e);
							break;
						}
					}
					if(!flag1 || !flag2) System.out.println(col1 + " " +col2);
				}
				total.add(new DenialConstraint(ls));
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	public Operator getoperator(String s) {
		switch(s) {
		case ">=" : return Operator.GREATER_EQUAL;
		case ">":return Operator.GREATER;
		case "==":return Operator.EQUAL;
		case "<": return Operator.LESS;
		case "<=":return Operator.LESS_EQUAL;
		case "<>": return Operator.UNEQUAL;
		}
		return null;
	}
	public DenialConstraintSet getTotal() {
		return total;
	}
    public String toString() {
        return total.toString();
      }
}
