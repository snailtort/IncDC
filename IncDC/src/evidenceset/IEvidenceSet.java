package evidenceset;

import java.util.Iterator;
import java.util.Set;

import predicates.sets.PredicateBitSet;

public interface IEvidenceSet extends Iterable<PredicateBitSet> {

	boolean add(PredicateBitSet predicateSet);

	boolean add(PredicateBitSet create, long count);

	long getCount(PredicateBitSet predicateSet);

	Iterator<PredicateBitSet> iterator();

	Set<PredicateBitSet> getSetOfPredicateSets();

	int size();

	boolean isEmpty();

}