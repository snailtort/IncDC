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
package ch.javasoft.bitset.search;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import ch.javasoft.bitset.IBitSet;
import de.hpi.naumann.dc.helpers.ArrayIndexComparator;
import de.hpi.naumann.dc.helpers.BitSetTranslator;

public class TranslatingTreeSearch implements ITreeSearch {

	private NTreeSearch search = new NTreeSearch();

	private BitSetTranslator translator;
	private Collection<IBitSet> bitsetListTransformed;

	public TranslatingTreeSearch(int[] priorities, List<IBitSet> bitsetList) {
		ArrayIndexComparator comparator = new ArrayIndexComparator(priorities, ArrayIndexComparator.Order.DESCENDING);
		this.translator = new BitSetTranslator(comparator.createIndexArray());
		this.bitsetListTransformed = translator.transform(bitsetList);
	}

	@Override
	public boolean add(IBitSet bs) {
		IBitSet translated = translator.bitsetTransform(bs);
		return search.add(translated);
	}

	@Override
	public void forEachSuperSet(IBitSet bitset, Consumer<IBitSet> consumer) {
		search.forEachSuperSet(bitset, superset -> consumer.accept(translator.bitsetRetransform(superset)));
	}

	@Override
	public void forEach(Consumer<IBitSet> consumer) {
		search.forEach(bitset -> consumer.accept(translator.bitsetRetransform(bitset)));
	}

	@Override
	public void remove(IBitSet remove) {
		search.remove(translator.bitsetTransform(remove));
	}

	@Override
	public boolean containsSubset(IBitSet bitset) {
		return search.containsSubset(translator.bitsetTransform(bitset));
	}

	@Override
	public Collection<IBitSet> getAndRemoveGeneralizations(IBitSet invalidDC) {
		Set<IBitSet> temp = search.getAndRemoveGeneralizations(invalidDC);
		return translator.retransform(temp);
	}

	public Comparator<IBitSet> getComparator() {
		return new Comparator<IBitSet>() {

			@Override
			public int compare(IBitSet o1, IBitSet o2) {
				int erg = Integer.compare(o2.cardinality(), o1.cardinality());
				return erg != 0 ? erg : translator.bitsetTransform(o2).compareTo(translator.bitsetTransform(o1));
			}
		};

	}

	public void handleInvalid(IBitSet invalidDCU) {
		IBitSet invalidDC = translator.bitsetTransform(invalidDCU);
		Collection<IBitSet> remove = search.getAndRemoveGeneralizations(invalidDC);
		for (IBitSet removed : remove) {
			for (IBitSet bitset : bitsetListTransformed) {
				IBitSet temp = removed.clone();
				temp.and(bitset);
				// already one bit in block set?
				if (temp.isEmpty()) {
					IBitSet valid = bitset.clone();
					valid.andNot(invalidDC);
					for (int i = valid.nextSetBit(0); i >= 0; i = valid.nextSetBit(i + 1)) {
						IBitSet add = removed.clone();
						add.set(i);
						if (!search.containsSubset(add)) {
							search.add(add);
						}
					}
				}
			}
		}
	}

}
