package predicates;

import evidenceset.build.Operator;
import input.ColumnPair;
import input.Input;
import input.ParsedColumn;
import predicates.operands.ColumnOperand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static predicates.sets.PredicateBitSet.indexProvider;

public class PredicateBuilder {
	private double COMPARE_AVG_RATIO = 0.1d;

	private double minimumSharedValue = 0.15d;

	private boolean noCrossColumn = false;

	public static Set<Predicate> predicates = new HashSet<>();
	private Collection<Collection<Predicate>> predicateGroups;

	public PredicateBuilder(Input input, boolean noCrossColumn, double minimumSharedValue) {
		predicates = new HashSet<>();
		predicateGroups = new ArrayList<>();
		this.noCrossColumn = noCrossColumn;
		this.minimumSharedValue = minimumSharedValue;
		constructColumnPairs(input).forEach(pair -> {
			ColumnOperand<?> o1 = new ColumnOperand<>(pair.getC1(), 0);
			addPredicates(o1, new ColumnOperand<>(pair.getC2(), 1), pair.isJoinable(), pair.isComparable());
			if (pair.getC1() != pair.getC2()) {
				addPredicates(o1, new ColumnOperand<>(pair.getC2(), 0), pair.isJoinable(), false);
			}
		});
	}

	public PredicateBuilder(File index,Input input) throws IOException {
		predicateGroups = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(index));
		String s=null;
		int lasti1=-1,lastj1=-1;
		Set<Predicate> tempPres = new HashSet<Predicate>();
		while((s=br.readLine())!=null){
			String[] temp=s.split(" ");
			Operator op=getoperator(temp[1]);
			int i;
			int i1=0,j1=0;
			for(i=0;i<input.getColumns().length;i++){
				if(temp[0].substring(3).equals((input.getColumns()[i]).toString()))
					i1=i;
				if(temp[2].substring(3).equals((input.getColumns()[i]).toString()))
					j1=i;
			}
			ColumnOperand operand1=new ColumnOperand(input.getColumns()[i1], Integer.parseInt(temp[0].substring(1,2)));
			ColumnOperand operand2=new ColumnOperand(input.getColumns()[j1], Integer.parseInt(temp[2].substring(1,2)));
			Predicate res= new Predicate(op,operand1,operand2);
			indexProvider.getIndex(res);
			predicates.add(res);
			if((lasti1==-1&&lastj1==-1)||(lasti1==i1&&lastj1==j1)){
				tempPres.add(res);
			}
			else{
				this.predicateGroups.add(tempPres);
				tempPres=new HashSet<>();
				tempPres.add(res);

			}
			lasti1=i1;lastj1=j1;
		}
		this.predicateGroups.add(tempPres);
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

	private ArrayList<ColumnPair> constructColumnPairs(Input input) {
		ArrayList<ColumnPair> pairs = new ArrayList<ColumnPair>();
		for (int i = 0; i < input.getColumns().length; ++i) {
			ParsedColumn<?> c1 = input.getColumns()[i];
			for (int j = i; j < input.getColumns().length; ++j) {
				ParsedColumn<?> c2 = input.getColumns()[j];
				boolean joinable = isJoinable(c1, c2);
				boolean comparable = isComparable(c1, c2);
				if (joinable || comparable)
					pairs.add(new ColumnPair(c1, c2, joinable, comparable));
			}
		}
		return pairs;
	}
	private boolean isJoinable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2);

		if (!c1.getType().equals(c2.getType()))
			return false;

		return c1.getSharedPercentage(c2) > minimumSharedValue;
	}

	private boolean isComparable(ParsedColumn<?> c1, ParsedColumn<?> c2) {
		if (noCrossColumn)
			return c1.equals(c2) && (c1.getType().equals(Double.class) || c1.getType().equals(Long.class));

		if (!c1.getType().equals(c2.getType()))
			return false;

		if (c1.getType().equals(Double.class) || c1.getType().equals(Long.class)) {
			if (c1.equals(c2))
				return true;

			double avg1 = c1.getAverage();
			double avg2 = c2.getAverage();
			return Math.min(avg1, avg2) / Math.max(avg1, avg2) > COMPARE_AVG_RATIO;
		}
		return false;
	}

	public Set<Predicate> getPredicates() {
		return predicates;
	}

	public Collection<Collection<Predicate>> getPredicateGroups() {
		return predicateGroups;
	}

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

	private void addPredicates(ColumnOperand<?> o1, ColumnOperand<?> o2, boolean joinable, boolean comparable) {
		Set<Predicate> predicates = new HashSet<Predicate>();
		for (Operator op : Operator.values()) {
			if (op == Operator.EQUAL || op == Operator.UNEQUAL) {
				if (joinable && (o1.getIndex() != o2.getIndex())){
					predicates.add(predicateProvider.getPredicate(op, o1, o2));
				}
			} else if (comparable) {
				predicates.add(predicateProvider.getPredicate(op, o1, o2));
			}
		}
		this.predicates.addAll(predicates);
		this.predicateGroups.add(predicates);
	}

	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();
}
