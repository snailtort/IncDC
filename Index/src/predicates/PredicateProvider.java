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
package predicates;

import java.util.HashMap;
import java.util.Map;

import evidenceset.build.Operator;
import predicates.operands.ColumnOperand;

public class PredicateProvider {
	private static PredicateProvider instance;

	private Map<Operator, Map<ColumnOperand<?>, Map<ColumnOperand<?>, Predicate>>> predicates;

	private PredicateProvider() {
		predicates = new HashMap<>();
	}
	
	public Predicate getPredicate(Operator op, ColumnOperand<?> op1, ColumnOperand<?> op2) {
		Map<ColumnOperand<?>, Predicate> map = predicates.computeIfAbsent(op,  a -> new HashMap<>()).computeIfAbsent(op1, a -> new HashMap<>());
		Predicate p = map.get(op2);
		if(p == null) {
			p = new Predicate(op, op1, op2);
			map.put(op2, p);
		}
		return p;
	}

	static {
        instance = new PredicateProvider();
	}

    public static PredicateProvider getInstance() {
        return instance;
    }
}
