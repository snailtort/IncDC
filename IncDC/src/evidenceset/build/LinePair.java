package evidenceset.build;

import predicates.Predicate;

public class LinePair  implements java.lang.Comparable{
	public int index1;
	public int index2;
	
	public LinePair(int i,int j) {
		this.index1=i;
		this.index2=j;
	}

	public int getLine1() {
		return index1;
	}

	public int getLine2() {
		return index2;
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinePair other = (LinePair) obj;
		if (index1 != other.index1)
			return false;
		if(index2!=other.index2)
			return false;
		return true;
	}

	public int compareTo(Object o) {
		LinePair p=(LinePair) o;
		return index1=p.index1;
	}
}
