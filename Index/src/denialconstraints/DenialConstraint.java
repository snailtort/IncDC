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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.search.NTreeSearch;
import evidenceset.build.Operator;
import input.ParsedColumn;
import predicates.Predicate;
import predicates.operands.ColumnOperand;
import predicates.sets.Closure;
import predicates.sets.PredicateBitSet;
import predicates.sets.PredicateSetFactory;

public class DenialConstraint {

	public PredicateBitSet predicateSet;
	public Predicate[] predicates;
	public DenialConstraint(Predicate... predicates) {
		predicateSet = PredicateSetFactory.create(predicates);
		this.predicates=predicates;
	}

	public DenialConstraint(PredicateBitSet predicateSet) {
		this.predicateSet = predicateSet;
	}

	public DenialConstraint(List<Predicate> ls) {
		// TODO Auto-generated constructor stub
		Predicate[] l=new Predicate[ls.size()];
		for(int i=0;i<ls.size();i++) l[i]=ls.get(i);
		predicateSet = PredicateSetFactory.create(l);
		this.predicates=l;
	}

	public boolean isTrivial() {
		int count =0;
		for(Predicate pre:predicates) {
			if(pre.getOperator()==Operator.UNEQUAL)
				count++;
			if(count>=2) return true;
		}
		return !new Closure(predicateSet).construct();
	}

	public boolean isImpliedBy(NTreeSearch tree) {
		Closure c = new Closure(predicateSet);
		if (!c.construct())
			return true;

		return isImpliedBy(tree, c.getClosure());
	}

	public boolean isImpliedBy(NTreeSearch tree, PredicateBitSet closure) {
		IBitSet subset = tree.getSubset(PredicateSetFactory.create(closure).getBitset());
		if (subset != null) {
			return true;
		}

		DenialConstraint sym = getInvT1T2DC();
		if (sym != null) {
			Closure c = new Closure(sym.getPredicateSet());
			if (!c.construct())
				return true;
			IBitSet subset2 = tree.getSubset(PredicateSetFactory.create(c.getClosure()).getBitset());
			return subset2 != null;
		}

		return false;

	}


	public DenialConstraint getInvT1T2DC() {
		PredicateBitSet invT1T2 = PredicateSetFactory.create();
		for (Predicate predicate : predicateSet) {
			Predicate sym = predicate.getInvT1T2();
			if (sym == null)
				return null;
			invT1T2.add(sym);
		}
		return new DenialConstraint(invT1T2);
	}

	public PredicateBitSet getPredicateSet() {
		return predicateSet;
	}

	public int getPredicateCount() {
		return predicateSet.size();
	}

	private boolean containedIn(PredicateBitSet otherPS) {
		for (Predicate p : predicateSet) {
			if (!otherPS.containsPredicate(p) && !otherPS.containsPredicate(p.getSymmetric()))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		// final int prime = 31;
		int result1 = 0;
		for (Predicate p : predicateSet) {
			result1 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
		}
		int result2 = 0;
		if (getInvT1T2DC() != null)
			for (Predicate p : getInvT1T2DC().predicateSet) {
				result2 += Math.max(p.hashCode(), p.getSymmetric().hashCode());
			}
		return Math.max(result1, result2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DenialConstraint other = (DenialConstraint) obj;
		if (predicateSet == null) {
			return other.predicateSet == null;
		} else if (predicateSet.size() != other.predicateSet.size()) {
			return false;
		} else {
			PredicateBitSet otherPS = other.predicateSet;
			return containedIn(otherPS) || getInvT1T2DC().containedIn(otherPS)
					|| containedIn(other.getInvT1T2DC().predicateSet);
		}
	}

	public static final String NOT = "\u00AC";
	public static final String AND = "∧";
	@Override
	public String toString() {
		return "[DC: " + predicateSet.toString() + "]";
	}

	public Predicate[] getPredicates(){
		int i=0;
		Predicate [] predicates=new Predicate[predicateSet.size()];
		for(Predicate pre:predicateSet)
			predicates[i++]=pre;
		return predicates;
	}
}
