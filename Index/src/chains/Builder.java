package chains;

import com.alibaba.fastjson.annotation.JSONField;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import evidenceset.IEvidenceSet;
import evidenceset.build.EvidenceSetBuilder;
import evidenceset.build.Operator;
import input.ColumnPair;
import input.ParsedColumn;
import predicates.Predicate;
import predicates.operands.ColumnOperand;
import predicates.sets.PredicateBitSet;

import java.io.Serializable;
import java.util.*;

import static predicates.PredicateBuilder.predicates;

public class Builder extends EvidenceSetBuilder implements Serializable {
	@JSONField(serialize = false)
	public transient Predicate pre1;
	@JSONField(serialize = false)
	public transient Predicate pre2;
	public String predicate1;
	public String predicate2;
	public int index21;
	public int index4;
	public int type=0;
	public int flag;
	public int sim;
	public Map<Integer , String> column;
	private List<Skiplist> chains=new ArrayList<>();
	public Map<Integer, List<Integer>> equalMap1 = new HashMap<>();
	public Map<Integer, List<Integer>> equalMap2 = new HashMap<>();
	public boolean cross = false;
	public int addData1=-1;
	public int addData2=-1;
	public static Multimap<Integer, Integer> vioTuples = HashMultimap.create();

	public Builder(){}
	public Builder(int[][] in, Predicate index1, Predicate index2, int index21, int index4, int addData1, int addData2, Map<Integer , String> column) {
		if(index21==2&&index4==2) {
			this.index21=index21;
			this.index4=index4;
			this.column=column;
			this.pre1=index1;
			this.pre2=index2;
			this.flag=3;
			this.type=3;
			sim=4;
			if(pre1.equals(pre2)){
				for(int i = 0; i < in.length; i++){
					int key1=in[i][getIndex(pre1.getOperand1())+1];
					int key2=in[i][getIndex(pre2.getOperand1())+1];
					equalMap1.computeIfAbsent(key1, k -> new ArrayList<>()).add(in[i][0]);
					equalMap2.computeIfAbsent(key2, k -> new ArrayList<>()).add(in[i][0]);
				}
			}
			else{
				sort(in, new int[] {getIndex(pre1.getOperand1())+1,getIndex(pre2.getOperand1())+1});
				Skiplist now = new Skiplist();
				for(int i=0; i<in.length;i++) {
					int key1=in[i][getIndex(pre1.getOperand1())+1];
					int key2=in[i][getIndex(pre2.getOperand1())+1];
					now.insert(key1,key2,in[i][0],3,3);
				}
				chains.add(now);
			}
		}
		else if(index21==2&&index4==-1) {
			this.index21=index21;
			this.index4=index4;
			this.column=column;
			this.pre1=index1;
			this.pre2=index2;
			this.flag=3;
			this.type=3;
			sim=3;
			sort(in, new int[] {getIndex(pre1.getOperand1())+1,getIndex(pre2.getOperand1())+1});
			Skiplist now = new Skiplist();
			for(int i=0; i<in.length;i++) {
				int key1=in[i][getIndex(pre1.getOperand1())+1];
				int key2=in[i][getIndex(pre2.getOperand1())+1];
				now.insert(key1,key2,in[i][0],3,3);
			}
			chains.add(now);
		}

		else {
			if(index21==6&&index4==6){
				cross = true;
				this.addData1 = addData1;
				this.addData2 = addData2;
			}
			this.column=column;
			int[][] input=in.clone();
			if(index1.getopindex()>2) {
				index1=index1.getSymmetric();
				index2=index2.getSymmetric();
			}
			this.pre1=index1;
			this.pre2=index2;
			if(index1.getopindex()==-1||index1.getopindex()==5){
				this.pre1=index2;
				this.pre2=index1;
			}
			if(this.pre2.getopindex()==-1||this.pre2.getopindex()==5){
				this.pre2=new Predicate(Operator.GREATER,pre2.getOperand1(),pre2.getOperand2());

			}
			switch(this.pre1.getopindex()*10+this.pre2.getopindex()) {
				case 0:                                                                   // >= , >=
				case 2:                                                                   // >= , =
				case 22:                                                                  // =  , =
				case 20:this.flag=1;this.type=0;buildingchains(input,1,0);break; // = , >=
				case 24:                                                                  // = , <=
				case 4:this.flag=-1;this.type=0;buildingchains(input,-1,0);break;// >= , <=

				case 1:                                                                    // >= , >
				case 21:this.flag=1;this.type=1;buildingchains(input,1,1);break;  // = . >
				case 3:                                                                    // >= , <
				case 23:this.flag=-1;this.type=1;buildingchains(input,-1,1);break;// = , <
				// > , <=
				case 14:this.flag=-1;this.type=1;this.pre1=index2.getSymmetric();int a=index21;
					index4=a;this.pre2=index1.getSymmetric();buildingchains(input,-1,1);break;//==
				case 12:                                                                    // > , =
					// > , >=
				case 10:this.flag=1;this.type=1;this.pre1=index2;this.pre2=index1;int a1=index21;
					index4=a1;buildingchains(input,1,1);break;

				case 11:this.flag=1;this.type=2;buildingchains(input,1,2);break;  // > , >
				case 13:this.flag=-1;this.type=2;buildingchains(input,-1,2);break;// > , <

				default: System.out.println("invalid index");

			}
			this.index21 = pre1.getopindex();
			this.index4 = pre2.getopindex();
			if(index4 == 7){
				sim = 0;
				this.index4 = Math.abs(4-this.index4);
			}
			else{
				sim = 1;
			}
		}
		predicate1 = pre1.toString();
		predicate2 = pre2.toString();
	}

	private void buildingchains(int[][] input,int flag,int type){
		/*
		 * case0://>=;>=
		 * case 1://>=;>
		 * case 2://>,>
		 * */
		sort(input, new int[] {getIndex(pre1.getOperand1())+1,flag*(getIndex(pre2.getOperand1())+1)});
		for(int i=0; i<input.length;i++) {
			int count=-1;
			Skiplist now = new Skiplist();
			int in1=0;
			int key1=input[i][getIndex(pre1.getOperand1())+1];
			int key2=input[i][getIndex(pre2.getOperand1())+1];
			for(int i1=0;i1<chains.size();i1++) {
				int dis=chains.get(i1).checkinsert(key1, key2, input[i][0],flag,type);
				if(dis==0) {
					count=0;
					break;
				}
				if(count ==-1&dis>0) {count = dis;in1=i1;}
				else if(count>dis&dis>0) {
					count=dis;
					in1=i1;
				}
			}
			if(count==-1) {now.insert(key1,key2,input[i][0],flag,type); chains.add(now);}
			else{ if(count>0) {chains.get(in1).insert(key1,key2,input[i][0],flag,type);}}
		}
	}
	@JSONField(serialize = false)
	private int getIndex(ColumnOperand<?> operand1) {
		// TODO Auto-generated method stub
		for(Integer i:column.keySet()) {
			if(column.get(i).equals(operand1.getColumn().getName()))
			{
				return i;
			}
		}
		return -1;
	}

	public List<Skiplist> getChains(){
		return chains;
	}

	public String toString() {
		StringBuffer s=new StringBuffer();
		s.append(pre1.toString()+" "+pre2.toString()+" "+index21+" "+index4+" "+addData1+" "+addData2+" ");
		return s.toString();
	}

	private static void sort(int[][] ob, final int[] order) {
		Arrays.sort(ob, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				int[] one = (int[]) o1;
				int[] two = (int[]) o2;
				for (int i = 0; i < order.length; i++) {
					int k = order[i];
					boolean flag=false;
					if(k<0){flag=true;k=-k;}
					if(flag){
						if (one[k] < two[k]) {
							return 1;
						} else if (one[k] > two[k]) {
							return -1;
						} else {
							continue;
						}
					}
					else{
						if (one[k] > two[k]) {
							return 1;
						} else if (one[k] < two[k]) {
							return -1;
						} else {
							continue;
						}
					}
				}
				return 0;
			}
		});

	}

	public String tosymString() {
		// TODO Auto-generated method stub
		String s="";
		int a=4-index21;
		int b=4-index4;
		s+=pre1.toString()+" "+pre2.toString()+" "+a+" "+b+"\n";
		return s;
	}

	public String getPredicate1() {
		return predicate1;
	}

	public String getPredicate2() {
		return predicate2;
	}

	public int getIndex21() {
		return index21;
	}

	public int getIndex4() {
		return index4;
	}

	public int getType() {
		return type;
	}

	public int getFlag() {
		return flag;
	}

	public int getSim() {
		return sim;
	}

	public Map<Integer, String> getColumn() {
		return column;
	}

	public boolean getCross() {
		return cross;
	}

	public int getAddData1() {
		return addData1;
	}

	public int getAddData2() {
		return addData2;
	}

	public static Multimap<Integer, Integer> getVioTuples() {
		return vioTuples;
	}
}
