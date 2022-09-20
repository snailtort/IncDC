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
package de.hpi.naumann.dc.helpers;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class BitSetTranslator {
	private Integer[] indexes;

	public BitSetTranslator(Integer[] indexes) {
		this.indexes = indexes;
	}

	public IBitSet bitsetRetransform(IBitSet bitset) {
		IBitSet valid = LongBitSet.FACTORY.create();
		for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
			valid.set(indexes[i].intValue());
		}
		return valid;
	}

	public int retransform(int i) {
		return indexes[i].intValue();
	}

	public IBitSet bitsetTransform(IBitSet bitset) {
		IBitSet bitset2 = LongBitSet.FACTORY.create();
		for (Integer i : indexes) {
			if (bitset.get(indexes[i.intValue()].intValue())) {
				bitset2.set(i.intValue());
			}
		}
		return bitset2;
	}

	public Collection<IBitSet> transform(Collection<IBitSet> bitsets) {
		return bitsets.stream().map(bitset -> bitsetTransform(bitset)).collect(Collectors.toList());
	}

	public Collection<IBitSet> retransform(Set<IBitSet> bitsets) {
		return bitsets.stream().map(bitset -> bitsetRetransform(bitset)).collect(Collectors.toList());
	}
}
