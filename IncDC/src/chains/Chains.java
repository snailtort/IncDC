package chains;

import denialconstraints.DenialConstraint;
import evidenceset.build.Operator;
import predicates.Predicate;
import predicates.operands.ColumnOperand;

import java.util.*;
import java.util.Map.Entry;


public class Chains {
	public List<Builder> indexes = new ArrayList();
	private Map<String,Double> map = new TreeMap<String,Double>();
	public Map<Integer, String> column;
	public int[][] diff;

	public Chains(){}
	public Chains(List<DenialConstraint> dcs, int[][] input, double alpha, Map<Integer, String> column,Set<Integer> columnList/*, PredicateBuilder predicates*/) throws Exception {
		int[] contain=new int[dcs.size()];
		this.column=column;
		int columnsize=column.size()+1;
		Set<Integer> sig = new HashSet<>();
		diff = new int[columnsize][columnsize];
		int count=Math.min(10000, (input.length-1)/10+2);
		HashSet<Integer> set1 = new HashSet<>();
		randomSet(0,input.length,count,set1);
		int[][] sample = new int[count][columnsize];
		int index=0;
		for(int i:set1){
			for(int j=0;j<columnsize;j++){
				sample[index][j]=input[i][j];
			}
			index++;
		}
		for(int i=0;i<columnsize;i++) {
			double[] cov=new double[input[0].length];
			for(int k=i;k<columnsize;k++) {
				int[] order=new int[]{i,k};
				sort(sample,order);
				for(int j=0;j<count-1;j++) {
					if(sample[j][i]!=sample[j+1][i]||sample[j][k]!=sample[j+1][k]) {
						diff[i][k]++;
						diff[k][i]++;
					}
				}
			}
			diff[i][i]=diff[i][i]/2;
		}
		double[] mean=new double[sample[0].length];
		for(int k=0;k<columnsize;k++) {
			for(int j=0;j<count;j++)
				mean[k]+=(1.0*sample[j][k])/count;
		}
		double[] sigma=new double[sample[0].length];

		for(int k=0;k<sample.length;k++) {
			for(int j=0;j<columnsize;j++) {
				sigma[j]+=(sample[k][j]-mean[j])*(sample[k][j]-mean[j]);
			}
		}
		double[][] p=new double[columnsize][columnsize];
		for(int i=0;i<columnsize;i++) {
			int[] order=new int[]{i};
			sort(sample,order);
			double[] cov=new double[columnsize];
			for(int j=0;j<sample.length-1;j++) {
				for(int k=i+1;k<columnsize;k++) {
					cov[k]+=(sample[j][i]-mean[i])*(sample[j][k]-mean[k]);
				}
			}
			for(int k=i+1;k<columnsize;k++) {
				p[i][k]=cov[k]/(Math.sqrt(sigma[i])*Math.sqrt(sigma[k]));//即计算r(A,B)
			}
		}

//		for(int i=0;i<dcs.size();i++){
//			//处理仅有两个谓词的情况
//			if(dcs.get(i).getPredicateCount()==2) {
//				Predicate pre1 = dcs.get(i).predicates[0];
//				Predicate pre2 = dcs.get(i).predicates[1];
//				/**
//				 * 前置条件判断
//				 * 只判断一个操作数是因为我没有考虑跨列 跨列本来也不多
//				 * 如果检查跨列应该检查所有操作数
//				 */
//				if(columnList.contains(pre1.getOperand1().getColumnIndex()) &&columnList.contains(pre2.getOperand1().getColumnIndex())){
//					//contain[i]=1;
//					continue;
//				}
//
//				//某一op为≠，未考虑全是≠的情况以及≠为string的情况
//				if(pre1.getopindex()==-1&&!(isString(pre1.getOperand1()))){
//					dealWithUne(input,pre1,pre2);
//					contain[i]=1;
//					continue;
//				}
//				if(pre2.getopindex()==-1&&!(isString(pre2.getOperand1()))){
//					dealWithUne(input,pre2,pre1);
//					contain[i]=1;
//					continue;
//				}
//				//有跨列跨元组谓词时
//				if(!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn())&&(!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))){
//					continue;
//				}
//				else if(!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn())||(!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))){
//					contain[i]=1;
//					continue;
//				}
//				if(contain[i]==0){
//					//不等式索引
////					Builder ber;
////					if(diff[getIndex(pre1.getOperand1())+1][getIndex(pre1.getOperand1())+1]>diff[getIndex(pre2.getOperand1())+1][getIndex(pre2.getOperand1())+1])
////						ber=new Builder(input,pre1,pre2, pre1.getopindex(), pre2.getopindex(),0,0, column);
////					else
////						ber=new Builder(input,pre2,pre1, pre2.getopindex(), pre1.getopindex(),0,0, column);
////					System.out.println("after if contain index:"+ber.toString());
////					indexes.add(ber);
//					contain[i]=1;
//				}
//			}
//		}

		for(int i=0;i<dcs.size();i++) {
			if(contain[i]==1) continue;
			for(int j=0;j<dcs.get(i).getPredicateCount();j++) {
				if(dcs.get(i).predicates[j].getOperand1().getIndex()==dcs.get(i).predicates[j].getOperand2().getIndex()){
					continue;
				}
				if(dcs.get(i).predicates[j].getopindex()==2) { //op为equal
					if(sig.contains(getIndex(dcs.get(i).predicates[j].getOperand1()))) {
						contain[i]=1;
						break;
					}
					if(contain[i]==0&&diff[getIndex(dcs.get(i).predicates[j].getOperand1())+1][getIndex(dcs.get(i).predicates[j].getOperand1())+1]>alpha*count) {
						Predicate pre1=new Predicate(Operator.GREATER_EQUAL,dcs.get(i).predicates[j].getOperand1(),dcs.get(i).predicates[j].getOperand2());

						if(columnList.contains(pre1.getOperand1().getColumnIndex()) ){
							continue;
						}
						Builder ber=new Builder(input,pre1,pre1,2,2,0,0,column);//是否用于A=
						System.out.println("A= A= (more predicates) builder:"+ber.toString());
						indexes.add(ber);
						sig.add(getIndex(dcs.get(i).predicates[j].getOperand1()));
						contain[i]=1;
					}
				}
			}
			if(contain[i]==0) {
				int in=0;
				for(int j=0;j<dcs.get(i).getPredicateCount();j++) {
					if(dcs.get(i).predicates[j].getOperator()==Operator.EQUAL) in++;
				}
				if(in==dcs.get(i).getPredicateCount()||in==dcs.get(i).getPredicateCount()-1){
					continue;
				}
				for(int j=0;j<dcs.get(i).getPredicateCount()-1;j++) {
					for(int k=j+1;k<dcs.get(i).getPredicateCount();k++) {
						Predicate pre1 = dcs.get(i).predicates[j];
						Predicate pre2 = dcs.get(i).predicates[k];

						if(columnList.contains(pre1.getOperand1().getColumnIndex()) && columnList.contains(pre2.getOperand1().getColumnIndex())){
							continue;
						}
						if(pre1.getopindex()>-1&&pre2.getopindex()>-1&&pre1.getopindex()!=2&&pre2.getopindex()!=2) {
							if(!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn())&&(!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))){
								continue;
							}
							if(pre1.getOperand1().getcolumn().equals(pre2.getOperand1().getcolumn())){
								continue;
							}
							String s=getIndex(dcs.get(i).predicates[j].getOperand1())+" "+dcs.get(i).predicates[j].getopindex()+" "+getIndex(dcs.get(i).predicates[k].getOperand1())+" "+dcs.get(i).predicates[k].getopindex();
							String s1=getIndex(dcs.get(i).predicates[k].getOperand1())+" "+dcs.get(i).predicates[k].getopindex()+" "+getIndex(dcs.get(i).predicates[j].getOperand1())+" "+dcs.get(i).predicates[j].getopindex();
							if(map.containsKey(s))
								map.put(s,map.get(s)+1);
							else if(map.containsKey(s1)) {map.put(s1,map.get(s1)+1);}
							else map.put(s,1.0);
						}
					}
				}
			}
		}

		Set<String> set=new HashSet<>();
		for(String s:map.keySet()) set.add(s);
		for(String s:set) {
			String sr=s;
			int index1=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index2=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index3=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index4=Integer.parseInt(sr);
			int ind=Math.max(index1, index3);
			int in=index1+index3-ind;
			double score =Math.abs(p[in+1][ind+1]);
			map.put(s,score*map.get(s));
		}
		List<Map.Entry<String,Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());
		Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
			public int compare(Entry<String, Double> o1,
							   Entry<String, Double> o2) {
				Double a = Math.abs(o1.getValue());
				Double b=Math.abs(o2.getValue());
				return b.compareTo(a);
			}
		});

		for(Map.Entry<String,Double> sc:list) {
			String s = sc.getKey();
			String sr=s;
			int index1=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index2=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index3=Integer.parseInt(sr.substring(0,sr.indexOf(" ")));
			sr=sr.substring(sr.indexOf(" ")+1);
			int index4=Integer.parseInt(sr);
			int change=0;
			ColumnOperand preCol1 = new ColumnOperand();
			ColumnOperand preCol2 = new ColumnOperand();
			for(int i=0;i<dcs.size();i++) {
				if(contain[i]==1) continue;
				int flag=0;
				int flag1=0;
				int flag2=0;
				int flag3=0;
				int preIndex11 = -1;
				int preIndex12 = -1;
				int preIndex21 = -1;
				int preIndex22 = -1;
				for(int j=0;j<dcs.get(i).getPredicateCount();j++) {
					if(getIndex(dcs.get(i).predicates[j].getOperand1())!=getIndex(dcs.get(i).predicates[j].getOperand2())) continue;
					if(getIndex(dcs.get(i).predicates[j].getOperand1())==index1&&type(dcs.get(i).predicates[j].getopindex(),index2)==1)
					{
						flag++;
						preIndex11 = j;
					}
					else if(getIndex(dcs.get(i).predicates[j].getOperand1())==index3&&type(dcs.get(i).predicates[j].getopindex(),index4)==1){
						flag1++;
						preIndex12 = j;
					}
					if(flag>0&&flag1>0) {
						contain[i]=1;
						change+=1;
						preCol1 = dcs.get(i).predicates[preIndex11].getOperand1();
						preCol2 = dcs.get(i).predicates[preIndex12].getOperand1();
					}
					if(getIndex(dcs.get(i).predicates[j].getOperand1())==index1&&type(4-dcs.get(i).predicates[j].getopindex(),index2)==1)
					{
						flag2++;
						preIndex21 = j;
					}
					else if(getIndex(dcs.get(i).predicates[j].getOperand1())==index3&&type(4-dcs.get(i).predicates[j].getopindex(),index4)==1){
						flag3++;
						preIndex22 = j;
					}
					if(flag2>0&&flag3>0) {
						contain[i]=1;
						change+=1;
						preCol1 = dcs.get(i).predicates[preIndex21].getOperand1();
						preCol2 = dcs.get(i).predicates[preIndex22].getOperand1();
					}
				}
			}
			if(change>0) {
				if(p[Math.min(index1,index3)+1][Math.max(index1,index3)+1]>=0) {
					if(diff[index1+1][index1+1]>diff[index3+1][index3+1]){
						int a=index3;
						index3=index1;
						index1=a;
						int b=index4;
						index4=index2;
						index2=b;
						ColumnOperand temp = new ColumnOperand();
						temp = preCol2;
						preCol2 = preCol1;
						preCol1 = temp;
					}
					Predicate pre1 = new Predicate(getOp(index2),new ColumnOperand(preCol1.getColumn(), 0),new ColumnOperand(preCol1.getColumn(), 1));
					Predicate pre2 = new Predicate(getOp(index4),new ColumnOperand(preCol2.getColumn(), 0),new ColumnOperand(preCol2.getColumn(), 1));
					if(Math.abs(index2-index4)>1) {
						pre2 = new Predicate(getOp(Math.abs(4-index4)),new ColumnOperand(preCol2.getColumn(), 0),new ColumnOperand(preCol2.getColumn(), 1));
						index4 = 7;
					}
					Builder ber=new Builder(input,pre1,pre2,index2,index4,0,0,column);
					boolean have=false;
					for(Builder b:indexes){
						if(b.toString().equals(ber.toString())||b.toString().equals(ber.tosymString()))
							have=true;
					}
					if(!have){
						indexes.add(ber);
					}
				}
				else if(p[Math.min(index1,index3)+1][Math.max(index1,index3)+1]<0) {
					if(diff[index1+1][index1+1]>diff[index3+1][index3+1]){
						int a=index3;
						index3=index1;
						index1=a;
						int b=index4;
						index4=index2;
						index2=b;
						ColumnOperand temp = new ColumnOperand();
						temp = preCol2;
						preCol2 = preCol1;
						preCol1 = temp;
					}
					Predicate pre1 = new Predicate(getOp(index2),new ColumnOperand(preCol1.getColumn(), 0),new ColumnOperand(preCol1.getColumn(), 1));
					Predicate pre2 = new Predicate(getOp(index4),new ColumnOperand(preCol2.getColumn(), 0),new ColumnOperand(preCol2.getColumn(), 1));
					if(Math.abs(index2-index4)<=1) {
						pre2=new Predicate(getOp(Math.abs(4-index4)),new ColumnOperand(preCol2.getColumn(), 0),new ColumnOperand(preCol2.getColumn(), 1));
						index4 = 7;
					}
					Builder ber=new Builder(input,pre1,pre2,index2,index4,0,0,column);
					boolean have=false;
					for(Builder b:indexes){
						if(b.toString().equals(ber.toString())||b.toString().equals(ber.tosymString()))
							have=true;
					}
					if(!have){
						indexes.add(ber);
					}
				}
			}
		}
		for(int i=0;i<dcs.size();i++){
			if(contain[i]==1) continue;
			if(dcs.get(i).getPredicateCount()==2) {
				Predicate pre1 = dcs.get(i).predicates[0];
				Predicate pre2 = dcs.get(i).predicates[1];
				if(columnList.contains(pre1.getOperand1().getColumnIndex()) &&columnList.contains(pre2.getOperand1().getColumnIndex())){
					continue;
				}

				if(pre1.getopindex()==-1&&!(isString(pre1.getOperand1()))){
					dealWithUne(input,pre1,pre2);
					contain[i]=1;
					continue;
				}
				if(pre2.getopindex()==-1&&!(isString(pre2.getOperand1()))){
					dealWithUne(input,pre2,pre1);
					contain[i]=1;
					continue;
				}
				if(!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn())&&(!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))){
					continue;
				}
				else if(!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn())||(!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))){
					contain[i]=1;
					continue;
				}
				if(contain[i]==0){
					contain[i]=1;
				}
			}
		}

		for(int i=0;i<contain.length;i++)
			if(contain[i]!=1){
				for(int j=0;j<dcs.get(i).getPredicateCount()-1;j++) {
					for (int k = j + 1; k < dcs.get(i).getPredicateCount(); k++) {
						Predicate pre1 = dcs.get(i).predicates[j];
						Predicate pre2 = dcs.get(i).predicates[k];
						if (pre1.getopindex() > -1 && pre2.getopindex() > -1) {
							if (!pre1.getOperand1().getcolumn().equals(pre1.getOperand2().getcolumn()) && (!pre2.getOperand1().getcolumn().equals(pre2.getOperand2().getcolumn()))) {
								dealWithCross(input, pre1, pre2);
								contain[i] = 1;
								continue;
							}
						}
					}
				}
			}
	}

	private void sort(int[][] input, int[] order) {
		// TODO Auto-generated method stub
		Arrays.sort(input, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				int[] one = (int[]) o1;
				int[] two = (int[]) o2;
				for (int i = 0; i < order.length; i++) {
					int k = order[i];
					if (one[k] > two[k]) {
						return 1;
					} else if (one[k] < two[k]) {
						return -1;
					} else {
						continue;
					}
				}
				return 0;
			}
		});
	}

	public String tostring() {
		StringBuffer s=new StringBuffer();
		int count = 0;
		for(Builder ber: indexes) {
			s.append( "this is the "+count+" -th chains "+ber.toString()+""+"\n");
		}
		return s.toString();
	}
	public int type(int i,int j) {
		if(i==-1&&j!=2) return 1;
		if(j==-1&&i!=2) return 1;
		if(i==j) return 1;
		switch(10*i+j) {
			case 10://i=1(>),j=0(>=)
			case 20://i=2(=),j=0(>=)
			case 24://i=2(=),j=4(<=)
			case 34://i=3(<),j=4(<=)
				return 1;
		}
		return 0;
	}

	public static void randomSet(int min, int max, int n, HashSet<Integer> set) {//随机采样n个元组
		if (n > (max - min + 1) || max < min) {
			return;
		}
		for (int i = 0; i < n; i++) {
			int num = (int) (Math.random() * (max - min)) + min;
			if(!set.contains(num)) set.add(num);
		}
		int setSize = set.size();
		if (setSize < n) {
			randomSet(min, max, n - setSize, set);
		}
	}

	private int getIndex(ColumnOperand<?> operand1) {
		// TODO Auto-generated method stub
		for(Integer i:column.keySet()) {
			if(column.get(i).equals(operand1.getColumn().getName())) {
				return i;
			}
		}
		return -1;
	}
	private Operator getOp(int opindex){
		switch(opindex){
			case 0: return Operator.GREATER_EQUAL;
			case 1: return Operator.GREATER;
			case 2: return Operator.EQUAL;
			case 3: return Operator.LESS;
			case 4: return Operator.LESS_EQUAL;
			default: return Operator.UNEQUAL;
		}
	}

	private boolean isString(ColumnOperand c1){
		if(c1.getcolumn().contains("String"))
			return true;
		else
			return false;
	}

	private void dealWithUne(int[][] input,Predicate pre1,Predicate pre2) throws Exception {
		Predicate pre3 = new Predicate(Operator.GREATER, pre1.getOperand1(), pre1.getOperand2());
		Predicate pre4 = new Predicate(Operator.LESS, pre1.getOperand1(), pre1.getOperand2());
		Builder ber1=new Builder(input,pre3,pre2,1, pre2.getopindex(),0,0,column);
		Builder ber2=new Builder(input,pre4,pre2,3, pre2.getopindex(), 0,0,column);
		System.out.println("deal with une(ber1):"+ber1.toString());
		System.out.println("deal with une(ber2):"+ber2.toString());
		indexes.add(ber1);
		indexes.add(ber2);
	}

	private void dealWithCross(int[][] input, Predicate pre1, Predicate pre2) throws Exception {
		ColumnOperand a0=new ColumnOperand(pre1.getOperand1().getColumn(),0);
		ColumnOperand a1=new ColumnOperand(pre1.getOperand1().getColumn(),1);
		ColumnOperand b0=new ColumnOperand(pre2.getOperand1().getColumn(),0);
		ColumnOperand b1=new ColumnOperand(pre2.getOperand1().getColumn(),1);
		ColumnOperand c0=new ColumnOperand(pre1.getOperand2().getColumn(),0);
		ColumnOperand c1=new ColumnOperand(pre1.getOperand2().getColumn(),1);
		ColumnOperand d0=new ColumnOperand(pre2.getOperand2().getColumn(),0);
		ColumnOperand d1=new ColumnOperand(pre2.getOperand2().getColumn(),1);
		Predicate pre3 = new Predicate(pre1.getOperator(),a0,a1);
		Predicate pre4 = new Predicate(pre2.getOperator(),b0,b1);
		Predicate pre5 = new Predicate(pre1.getOperator().getSymmetric(), c0,c1);
		Predicate pre6 = new Predicate(pre2.getOperator().getSymmetric(),d0,d1);
		if(pre3.getOperand1().getcolumn().equals(pre4.getOperand1().getcolumn())||
						pre5.getOperand1().getcolumn().equals(pre6.getOperand1().getcolumn())) {
		}
		else{
			Builder ber1 = new Builder(input,pre3,pre4,6,6,getIndex(pre1.getOperand2()),getIndex(pre2.getOperand2()),column);
			Builder ber2 = new Builder(input,pre5,pre6,6,6,getIndex(pre1.getOperand1()),getIndex(pre2.getOperand1()),column);

			boolean flag1 = false;
			boolean flag2 = false;
			for(Builder b:indexes){
				if(flag1 && flag2) break;
				if(b.toString().equals(ber1.toString())||b.toString().equals(ber1.tosymString())){
					flag1 = true;
				}
				if(b.toString().equals(ber2.toString())||b.toString().equals(ber2.tosymString())){
					flag2 = true;
				}
			}
			if(!flag1){
				indexes.add(ber1);
				System.out.println("cross builder1: "+ber1.toString());
			}
			if(!flag2){
				indexes.add(ber2);
				System.out.println("cross builder2: "+ber2.toString());
			}
		}
	}


}