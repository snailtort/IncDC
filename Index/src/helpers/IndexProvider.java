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
package helpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

public class IndexProvider<T> {
	private Map<T, Integer> indexes = new HashMap<>();
	private List<T> objects = new ArrayList<>();

	private int nextIndex = 0;

	public Integer getIndex(T object) {
		Integer index = indexes.putIfAbsent(object, Integer.valueOf(nextIndex));
		if (index == null) {
			index = Integer.valueOf(nextIndex);
			++nextIndex;
			objects.add(object);
		}
		return index;
	}

	public T getObject(int index) {
		return objects.get(index);
	}

	public IBitSet getBitSet(Iterable<T> objects) {
		IBitSet result = LongBitSet.FACTORY.create();
		for (T i : objects) {
			result.set(getIndex(i).intValue());
		}
		return result;
	}

	public Collection<T> getObjects(IBitSet bitset) {
		ArrayList<T> objects = new ArrayList<>();
		for (int i = bitset.nextSetBit(0); i >= 0; i = bitset.nextSetBit(i + 1)) {
			objects.add(getObject(i));
		}
		return objects;
	}

	public static <A extends Comparable<A>> IndexProvider<A> getSorted(IndexProvider<A> r) {
		IndexProvider<A> sorted = new IndexProvider<>();
		List<A> listC = new ArrayList<A>(r.objects);
		Collections.sort(listC);
		for (A c : listC) {
			sorted.getIndex(c);
		}
		return sorted;
	}

	public int size() {
		return nextIndex;
	}
}
