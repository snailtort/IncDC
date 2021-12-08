package predicates.sets;

import java.util.List;

import ch.javasoft.bitset.IBitSet;
import predicates.Predicate;

public class PredicateSetFactory {

	public static PredicateBitSet create(Predicate... predicates) {
		PredicateBitSet set = new PredicateBitSet();
		for (Predicate p : predicates)
			set.add(p);
		return set;
	}
	
	public static PredicateBitSet create(List<Predicate> ls) {
		Predicate[] predicates= new Predicate[ls.size()];
		for(int i=0;i<ls.size();i++) predicates[i]=ls.get(i);
		return create(predicates);
	}
	
	public static PredicateBitSet create(IBitSet bitset) {
		return new PredicateBitSet(bitset);
	}

	public static PredicateBitSet create(PredicateBitSet pS) {
		return new PredicateBitSet(pS);
	}
}
