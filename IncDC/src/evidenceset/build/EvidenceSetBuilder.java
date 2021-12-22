package evidenceset.build;

import java.util.*;

import input.ColumnPair;
import input.ParsedColumn;
import predicates.Predicate;
import predicates.PredicateBuilder;
import predicates.PredicateProvider;
import predicates.operands.ColumnOperand;
import predicates.sets.PredicateBitSet;
import predicates.sets.PredicateSetFactory;
import static predicates.PredicateBuilder.predicates;
public abstract class EvidenceSetBuilder {

	public EvidenceSetBuilder() {

	}

	protected PredicateBitSet getStatic(Collection<ColumnPair> pairs, int i) {
		PredicateBitSet set = PredicateSetFactory.create();
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			if (p.getC1().equals(p.getC2()))
				continue;

			PredicateBitSet[] list = map.get(p);
			if (p.getC1().getType().equals(String.class)) {
				if (equals(i, i, p))
					set.addAll(list[2]);
				else
					set.addAll(list[3]);
			}
			else {
				int compare2 = compare(i, i, p);
				if (compare2 < 0) {
					set.addAll(list[7]);
				} else if (compare2 == 0) {
					set.addAll(list[8]);
				} else {
					set.addAll(list[9]);
				}
			}

		}
		return set;
	}

	protected PredicateBitSet getPredicateSet(PredicateBitSet staticSet, Collection<ColumnPair> pairs, int i, int j) {
		PredicateBitSet set = PredicateSetFactory.create(staticSet);
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			PredicateBitSet[] list = map.get(p);
			if (p.getC1().getType().equals(String.class)) {
				if (equals(i, j, p))
					set.addAll(list[0]);
				else
					set.addAll(list[1]);
			}
			else {
				int compare = compare(i, j, p);
				if (compare < 0) {
					set.addAll(list[4]);
				} else if (compare == 0) {
					set.addAll(list[5]);
				} else {
					set.addAll(list[6]);
				}

			}

		}
		return set;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int compare(int i, int j, ColumnPair p) {
		return ((Comparable) getValue(i, p.getC1())).compareTo((getValue(j, p.getC2())));
	}

	private boolean equals(int i, int j, ColumnPair p) {
		return getValue(i, p.getC1()) != null && getValue(i, p.getC1()).equals(getValue(j, p.getC2()));
	}

	private Object getValue(int i, ParsedColumn<?> p) {
		return p.getValue(i);
	}

	//	protected Map<ColumnPair, PredicateBitSet[]> map;
	public static Map<ColumnPair, PredicateBitSet[]> map;


	protected void createSets(Collection<ColumnPair> pairs) {
		map = new HashMap<>();
		// which predicates are satisfied by these two lines?
		for (ColumnPair p : pairs) {
			PredicateBitSet[] list = new PredicateBitSet[10];
			for(int i = 0; i < list.length; ++i)
				list[i] = PredicateSetFactory.create();
			map.put(p, list);
			if (p.isJoinable()) {
				addIfValid(p, list[0], Operator.EQUAL, 1);
				addIfValid(p, list[1], Operator.UNEQUAL, 1);
				if (!p.getC1().equals(p.getC2())) {
					addIfValid(p, list[2], Operator.EQUAL, 0);
					addIfValid(p, list[3], Operator.UNEQUAL, 0);
				}
			}
			if (p.isComparable()) {

				addIfValid(p, list[4], Operator.LESS, 1);
				addIfValid(p, list[4], Operator.LESS_EQUAL, 1);
				addIfValid(p, list[4], Operator.UNEQUAL, 1);

				addIfValid(p, list[5], Operator.LESS_EQUAL, 1);
				addIfValid(p, list[5], Operator.GREATER_EQUAL, 1);
				addIfValid(p, list[5], Operator.EQUAL, 1);

				addIfValid(p, list[6], Operator.GREATER_EQUAL, 1);
				addIfValid(p, list[6], Operator.GREATER, 1);
				addIfValid(p, list[6], Operator.UNEQUAL, 1);

				if (!p.getC1().equals(p.getC2())) {
					addIfValid(p, list[7], Operator.LESS, 0);
					addIfValid(p, list[7], Operator.LESS_EQUAL, 0);
					addIfValid(p, list[7], Operator.UNEQUAL, 0);

					addIfValid(p, list[8], Operator.LESS_EQUAL, 0);
					addIfValid(p, list[8], Operator.GREATER_EQUAL, 0);
					addIfValid(p, list[8], Operator.EQUAL, 0);

					addIfValid(p, list[9], Operator.GREATER_EQUAL, 0);
					addIfValid(p, list[9], Operator.GREATER, 0);
					addIfValid(p, list[9], Operator.UNEQUAL, 0);
				}
			}

		}
	}

	private void addIfValid(ColumnPair p, PredicateBitSet list, Operator op, int index2) {
		Predicate pr = predicateProvider.getPredicate(op, new ColumnOperand<>(p.getC1(), 0),
				new ColumnOperand<>(p.getC2(), index2));
		if(predicates/*.getPredicates()*/.contains(pr))
			list.add(pr);
	}

	@SuppressWarnings("unused")
//	private static Logger log = LoggerFactory.getLogger(EvidenceSetBuilder.class);

	private static final PredicateProvider predicateProvider = PredicateProvider.getInstance();
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
}
