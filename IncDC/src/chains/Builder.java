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
	@JSONField(serialize = false)
	public void getEvidence(int pos1, int pos2, IEvidenceSet evidence, Collection<ColumnPair> pairs){
		if(vioTuples.containsEntry(pos1,pos2)){
			return;
		}
		evidence.add(getev(pos1, pos2, pairs));
		evidence.add(getev(pos2, pos1, pairs));
		vioTuples.put(pos1,pos2);
		vioTuples.put(pos2,pos1);
	}

	public void findvio(int[][] data, int[][] add_data,IEvidenceSet evidence,Collection<ColumnPair> pairs) {
		for(int k=0;k<add_data.length;k++) {
			skiplistnode p,q;
			int key1,key2,add_key1,add_key2;

			if(cross){
				key1=add_data[k][addData1+1];
				key2=add_data[k][addData2+1];
				add_key1 = add_data[k][getIndex(pre1.getOperand1())+1];
				add_key2 = add_data[k][getIndex(pre2.getOperand1())+1];
				this.index21 = pre1.getopindex();
				this.index4 = pre2.getopindex();
			}
			else{
				key1=add_data[k][getIndex(pre1.getOperand1())+1];
				key2=add_data[k][getIndex(pre2.getOperand1())+1];
				add_key1 = key1;
				add_key2 = key2;
			}
			if(sim == 4 && pre1.equals(pre2)){
				if(equalMap1.get(key1) == null){
					equalMap1.put(key1, Lists.newArrayList(add_data[k][0]));
				}
				else{
					List<Integer> list = equalMap1.get(key1);
					for(int i : list) {
						getEvidence(i,data.length-add_data.length+k,evidence,pairs);
					}
					list.add(add_data[k][0]);
					equalMap1.put(key1, list);
				}
				continue;
			}
			boolean insert=true;
			for(Skiplist l:chains) {
				p=l.find(key1);
				if(pre2.getopindex()>2)
					q = l.findkey22(key2);
				else{
					q=l.findkey2(key2);
				}
				if(sim==4) {
					while(p.right!=null&&p.right.key1==key1) {
						if(p.right.key2==key2) {
							for(int i:p.right.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}
							p.right.value.add(add_data[k][0]);
							insert=false;
							break;
						}
						if(p.right.key2>key2) {break;}
						p=p.right;
					}
					if(insert&&(p.right.key1>key1||p.right.key2>key2)) {
						l.insert(key1,key2,add_data[k][0],3,3);
						insert=false;
					}
				}

				if(sim==3) {
					while(p.right!=null&&p.right.key1==key1) {
						if(p.right.key2!=key2) {
							for(int i:p.right.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}
						}
						else{
							p.right.value.add(add_data[k][0]);
							insert=false;
						}
						p=p.right;
					}
					if(insert) {
						l.insert(key1,key2,add_data[k][0],3,3);
						insert=false;
					}
				}
				if(sim==1) {
					if(q.key1<=p.key1) {
						skiplistnode temp=p;
						p=q;
						q=temp;
					}
					if(p.right!=null) p=p.right;
					if(!cross){
						while(p.key1>Integer.MIN_VALUE) {
							int op01 = getop(key1,p.key1);
							int op02= getop(key2,p.key2);
							if(p.key1>Integer.MIN_VALUE&&p.key1<Integer.MAX_VALUE&&samedir(op01,index21)&&samedir(op02,index4)) {
								for(int i:p.value) {
									getEvidence(i,data.length-add_data.length+k,evidence,pairs);
								}
							}
							else if(p.key1>Integer.MIN_VALUE&&p.key1<Integer.MAX_VALUE&&samedir(4-op01,index21)&&samedir(4-op02,index4)) {
								for(int i:p.value) {
									getEvidence(i,data.length-add_data.length+k,evidence,pairs);
								}

							}
							p=p.left;
						}
					}
					while(q.key1<Integer.MAX_VALUE) {
						int op01 = getop(key1,q.key1);
						int op02= getop(key2,q.key2);
						if(q.key1>Integer.MIN_VALUE&&q.key1<Integer.MAX_VALUE&&samedir(op01,index21)&&samedir(op02,index4)) {
							for(int i:q.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}
						}
						else if(q.key1>Integer.MIN_VALUE&&q.key1<Integer.MAX_VALUE&&samedir(4-op01,index21)&&samedir(4-op02,index4)) {
							for(int i:q.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}

						}
						q=q.right;
					}
				}
				if(sim==0) {
					if(q.key1<=p.key1) {
						skiplistnode temp=p;
						p=q;
						q=temp;
					}
					if(p.left!=null) p=p.left;
					if(q.right!=null)q=q.right;
					if(p.left!=null) p=p.left;
					while(q.right!=null && q.right.key1 == q.key1) q=q.right;
					if(index21==2) {
						p=p.right;
						if(p.right!=null) q=p.right;
						else q=p;
					}
					else if(index4==2) {
						p=q.left;
					}
					while(p!=q.right) {
						int op01 = getop(key1,p.key1);
						int op02= getop(key2,p.key2);
						if(p.key1>Integer.MIN_VALUE&&p.key1<Integer.MAX_VALUE&&samedir(op01,index21)&&samedir(op02,index4)) {
							for(int i:p.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}
						}
						else if(p.key1>Integer.MIN_VALUE&&p.key1<Integer.MAX_VALUE&&samedir(4-op01,index21)&&samedir(4-op02,index4)) {
							for(int i:p.value) {
								getEvidence(i,data.length-add_data.length+k,evidence,pairs);
							}

						}
						p=p.right;
					}
				}
				if(insert){
					int canin =l.checkinsert(add_key1, add_key2, add_data[k][0], flag, type);
					if(canin==0){
						insert=false;
					}
					else if(canin>0) {
						l.insert(add_key1, add_key2,add_data[k][0],flag,type);
						insert=false;
					}
				}
			}
			if(insert) {
				Skiplist l=new Skiplist();
				l.insert(add_key1, add_key2,add_data[k][0], flag, type);
				chains.add(l);
			}
		}
	}

	private PredicateBitSet getev(int pos1, int pos2,Collection<ColumnPair> pairs){
		PredicateBitSet staticSet=getStatic(pairs,pos1);
		PredicateBitSet set=getPredicateSet(staticSet,pairs,pos1,pos2);

		return set;
	}
	@JSONField(serialize = false)
	public Collection<ColumnPair> getColumnPairs() {
		Set<List<ParsedColumn<?>>> joinable = new HashSet<>();
		Set<List<ParsedColumn<?>>> comparable = new HashSet<>();
		Set<List<ParsedColumn<?>>> all = new HashSet<>();
		for (Predicate p : predicates) {
			List<ParsedColumn<?>> pair = new ArrayList<>();
			pair.add(p.getOperand1().getColumn());
			pair.add(p.getOperand2().getColumn());

			if (p.getOperator() == Operator.EQUAL)
				joinable.add(pair);

			if (p.getOperator() == Operator.LESS)
				comparable.add(pair);

			all.add(pair);
		}

		Set<ColumnPair> pairs = new HashSet<>();
		for (List<ParsedColumn<?>> pair : all) {
			pairs.add(new ColumnPair(pair.get(0), pair.get(1), joinable.contains(pair), comparable.contains(pair)));
		}
		return pairs;
	}

	private int getop(int key1, int key12) {
		// TODO Auto-generated method stub

		if(key1<key12) return 3;
		if(key1==key12) return 2;
		if(key1>key12) return 1;
		return 0;
	}


	private boolean samedir(int op1, int opindex) {
		// TODO Auto-generated method stub
		if(opindex==-1) {
			if(op1!=2)return true;
			else return false;
		}
		if(op1==-1) {
			if(opindex!=2) return true;
			else return false;
		}
		switch(10*op1+opindex) {
			case 0:
			case 10:
			case 11:
			case 20:
			case 24:
			case 22:
			case 33:
			case 34:
			case 44:
				return true;
		}
		return false;
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
