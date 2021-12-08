package de.hpi.naumann.dc.cover;

import java.util.*;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;
import ch.javasoft.bitset.search.ITreeSearch;
import ch.javasoft.bitset.search.TranslatingTreeSearch;
import ch.javasoft.bitset.search.TreeSearch;
import denialconstraints.DenialConstraint;
import denialconstraints.DenialConstraintSet;
import evidenceset.IEvidenceSet;
import predicates.Predicate;
import predicates.PredicateBuilder;
import predicates.sets.PredicateBitSet;
import predicates.sets.PredicateSetFactory;


public class PrefixMinimalCoverSearch {

	private List<IBitSet> bitsetList = new ArrayList<>();

	private final Collection<IBitSet> startBitsets = new ArrayList<>();

	private TranslatingTreeSearch posCover;

	public PrefixMinimalCoverSearch(PredicateBuilder predicates2, List<DenialConstraint> origin) {
		this(predicates2, (TranslatingTreeSearch) null);
		for(DenialConstraint dc : origin){
			this.startBitsets.add(dc.getPredicateSet().getBitset());
		}
	}

	private PrefixMinimalCoverSearch(PredicateBuilder predicates2, TranslatingTreeSearch tree) {
		for (Predicate p : predicates2.getPredicates()) {
			IBitSet bitset = LongBitSet.FACTORY.create();
			bitset.or(PredicateSetFactory.create(p).getBitset());
			bitsetList.add(bitset);
		}
		this.posCover = tree;
	}

	private Collection<IBitSet> getBitsets(IEvidenceSet evidenceSet) {
		System.out.println("Evidence Set size: " + evidenceSet.size());
		if (posCover == null) {
			int[] counts = getCounts(evidenceSet);
			posCover = new TranslatingTreeSearch(counts, bitsetList);
		}

		System.out.println("Building new bitsets..");
		List<IBitSet> sortedNegCover = new ArrayList<IBitSet>();
		for (PredicateBitSet ps : evidenceSet) {
			sortedNegCover.add(ps.getBitset());
		}

		System.out.println("Sorting new bitsets..");
		sortedNegCover = minimize(sortedNegCover);

		mostGeneralDCs(posCover);

		Collections.sort(sortedNegCover, posCover.getComparator());
		System.out.println("Finished sorting neg 2. list size:" + sortedNegCover.size());

		for (int i = 0; i < sortedNegCover.size(); ++i) {
			posCover.handleInvalid(sortedNegCover.get(i));
		}

		Collection<IBitSet> result = new ArrayList<IBitSet>();
		posCover.forEach(bs -> result.add(bs));

		return result;
	}

	public DenialConstraintSet getDenialConstraints(IEvidenceSet evidenceSet) {

		DenialConstraintSet set = new DenialConstraintSet();
		getBitsets(evidenceSet).forEach(valid -> {
			set.add(new DenialConstraint(PredicateSetFactory.create(valid)));
		});
		return set;
	}

	private int[] getCounts(IEvidenceSet evidenceSet) {

		int[] counts = new int[PredicateBitSet.indexProvider.size()];
		for (PredicateBitSet ps : evidenceSet) {
			IBitSet bitset = ps.getBitset();
			for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
				counts[i]++;
			}
		}
		return counts;
	}

	private List<IBitSet> minimize(final List<IBitSet> sortedNegCover) {
		Collections.sort(sortedNegCover, new Comparator<IBitSet>() {
			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : o2.compareTo(o1);
			}
		});

		System.out.println("starting inverting size " + sortedNegCover.size());
		TreeSearch neg = new TreeSearch();
		sortedNegCover.stream().forEach(invalid -> addInvalidToNeg(neg, invalid));

		final ArrayList<IBitSet> list = new ArrayList<IBitSet>();
		neg.forEach(invalidFD -> list.add(invalidFD));
		return list;
	}

	private void mostGeneralDCs(ITreeSearch posCover) {
		for (IBitSet start : startBitsets) {
			posCover.add(start);
		}
	}

	private void addInvalidToNeg(TreeSearch neg, IBitSet invalid) {
		if (neg.findSuperSet(invalid) != null)
			return;

		neg.getAndRemoveGeneralizations(invalid);
		neg.add(invalid);
	}

}
