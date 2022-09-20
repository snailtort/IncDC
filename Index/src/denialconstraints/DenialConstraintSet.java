/*
 * =============================================================================

 * Copyright (c) 2017, Tobias Bleifuß, Sebastian Kruse, Felix Naumann
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */
package denialconstraints;

import java.util.*;
import java.util.Map.Entry;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import chains.Builder;
import evidenceset.build.Operator;
import input.ParsedColumn;
import predicates.Predicate;
import predicates.sets.Closure;
import predicates.sets.PredicateBitSet;
import predicates.sets.PredicateSetFactory;

import static predicates.sets.PredicateBitSet.indexProvider;

public class DenialConstraintSet implements Iterable<DenialConstraint> {

	private Set<DenialConstraint> constraints = new HashSet<>();

	public boolean contains(DenialConstraint dc) {
		return constraints.contains(dc);
	}

	public DenialConstraintSet checkImplication() {
		DenialConstraintSet dcs=new DenialConstraintSet();
		for(DenialConstraint dc:constraints){
			Predicate[] predicates=dc.getPredicates();
			PredicateBitSet temp= dc.getPredicateSet();
			for(int i=0;i<predicates.length-1;i++){
				if(predicates[i].getOperator().getShortString().equals("==")||predicates[i].getOperator().getShortString().equals("<>"))
					continue;
				for(int j=i+1;j<predicates.length;j++){
					if(predicates[j].getOperator().getShortString().equals("==")||predicates[j].getOperator().getShortString().equals("<>"))
						continue;
					if(comparePredicate(predicates[i],predicates[j])){
						if((predicates[i].getOperator().getShortString().equals(">=")&&predicates[j].getOperator().getShortString().equals(">"))||
								(predicates[i].getOperator().getShortString().equals("<=")&&predicates[j].getOperator().getShortString().equals("<"))){
							int index=indexProvider.getIndex(predicates[i]);
							temp.getBitset().set(index,false);
						}
						if((predicates[i].getOperator().getShortString().equals(">")&&predicates[j].getOperator().getShortString().equals(">="))||
								(predicates[i].getOperator().getShortString().equals("<")&&predicates[j].getOperator().getShortString().equals("<="))){
							int index=indexProvider.getIndex(predicates[j]);
							temp.getBitset().set(index,false);
						}
					}
				}
			}
			dcs.add(new DenialConstraint(temp));
		}
		return dcs;
	}

	private boolean comparePredicate(Predicate p1,Predicate p2) {
		if(p1.getOperand1().equals(p2.getOperand1())&&
				p1.getOperand2().equals(p2.getOperand2())&&
				!p1.getOperator().getShortString().equals(p2.getOperator().getShortString()))
			return true;
		return false;
	}

	private static class MinimalDCCandidate {
		DenialConstraint dc;
		IBitSet bitset;

		public MinimalDCCandidate(DenialConstraint dc) {
			this.dc = dc;
			this.bitset = PredicateSetFactory.create(dc.getPredicateSet()).getBitset();
		}

		public boolean shouldReplace(MinimalDCCandidate prior) {
			if (prior == null)
				return true;

			if (dc.getPredicateCount() < prior.dc.getPredicateCount())
				return true;

			if (dc.getPredicateCount() > prior.dc.getPredicateCount())
				return false;

			return bitset.compareTo(prior.bitset) <= 0;
		}
	}

	public void minimize() {
		Map<PredicateBitSet, MinimalDCCandidate> constraintsClosureMap = new HashMap<>();
		for (DenialConstraint dc : constraints) {
			PredicateBitSet predicateSet = dc.getPredicateSet();
			Closure c = new Closure(predicateSet);
			if (c.construct()) {
				MinimalDCCandidate candidate = new MinimalDCCandidate(dc);
				PredicateBitSet closure = c.getClosure();
				MinimalDCCandidate prior = constraintsClosureMap.get(closure);
				if (candidate.shouldReplace(prior))
					constraintsClosureMap.put(closure, candidate);
			}
		}

		List<Entry<PredicateBitSet, MinimalDCCandidate>> constraints2 = new ArrayList<>(constraintsClosureMap.entrySet());

		constraints2.sort((entry1, entry2) -> {
			int res = Integer.compare(entry1.getKey().size(), entry2.getKey().size());
			if (res != 0)
				return res;
			res = Integer.compare(entry1.getValue().dc.getPredicateCount(), entry2.getValue().dc.getPredicateCount());
			if (res != 0)
				return res;
			return entry1.getValue().bitset.compareTo(entry2.getValue().bitset);
		}
		);

		constraints = new HashSet<>();
		NTreeSearch tree = new NTreeSearch();
		for (Entry<PredicateBitSet, MinimalDCCandidate> entry : constraints2) {
			if (tree.containsSubset(PredicateSetFactory.create(entry.getKey()).getBitset()))
				continue;

			DenialConstraint inv = entry.getValue().dc.getInvT1T2DC();
			if (inv != null) {
				Closure c = new Closure(inv.getPredicateSet());
				if (!c.construct())
					continue;
				 if
				 (tree.containsSubset(PredicateSetFactory.create(c.getClosure()).getBitset()))
				 continue;
			}

			constraints.add(entry.getValue().dc);
			tree.add(entry.getValue().bitset);
			 if(inv != null)
				 tree.add(PredicateSetFactory.create(inv.getPredicateSet()).getBitset());
		}
	}

	public void add(DenialConstraint dc) {
		constraints.add(dc);
	}


	@Override
	public Iterator<DenialConstraint> iterator() {
		return constraints.iterator();
	}

	public int size() {
		return constraints.size();
	}

	public List<DenialConstraint> getdc() {
		// TODO Auto-generated method stub
		List<DenialConstraint> dcs =new ArrayList<>();
		for(DenialConstraint dc:constraints) dcs.add(dc);
		return dcs;
	}

	public List<DenialConstraint> getDc() {
		// TODO Auto-generated method stub
		List<DenialConstraint> dcs =new ArrayList<>();
		for(DenialConstraint dc:constraints){
			List<Predicate> dc1 = new ArrayList<>();
			List<Predicate> dc2 = new ArrayList<>();
			Predicate pre = new Predicate();
			boolean flag =false;
			for(int i = 0; i < dc.getPredicateCount(); i++){
				if(!flag && dc.predicates[i].getopindex()<0 && !dc.predicates[i].getOperand1().getcolumn().contains("String")){
					pre = dc.predicates[i];
					flag = true;
				}
				else{
					dc1.add(dc.predicates[i]);
					dc2.add(dc.predicates[i]);
				}
			}
			if(flag){
				dc1.add(new Predicate(Operator.GREATER, pre.getOperand1(), pre.getOperand2()));
				dc2.add(new Predicate(Operator.LESS, pre.getOperand1(), pre.getOperand2()));
				dcs.add(new DenialConstraint(dc1));
				dcs.add(new DenialConstraint(dc2));
			}
			else{
				dcs.add(dc);
			}
		}
		return dcs;
	}
}
