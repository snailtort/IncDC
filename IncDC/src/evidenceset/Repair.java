package evidenceset;

import chains.Builder;
import input.ColumnPair;
import java.util.*;


public class Repair{
	private List<Builder> chains;
	public Map<Integer , String> column;

	public Repair(List<Builder> chains,Map<Integer , String> column) {
		this.chains=chains;
		this.column=column;
	}

	public IEvidenceSet getevidence(int[][] data,int[][] add_data,Collection<ColumnPair> pairs) throws Exception {
		berfind bf=new berfind(chains, data, add_data,pairs);
		return bf.evidence;
	}
}
