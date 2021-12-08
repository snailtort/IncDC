package algorithms.hybrid;

import ch.javasoft.bitset.LongBitSet;
import chains.Builder;
import chains.Chains;
import com.carrotsearch.sizeof.RamUsageEstimator;
import de.hpi.naumann.dc.cover.PrefixMinimalCoverSearch;
import denialconstraints.DenialConstraint;
import denialconstraints.DenialConstraintSet;
import evidenceset.IEvidenceSet;
import evidenceset.Repair;
import evidenceset.build.EvidenceSetBuilder;
import input.ColumnPair;
import input.Input;
import input.OriginDC;
import input.ParsedColumn;
import predicates.PredicateBuilder;
import java.util.*;

public class Incdc extends EvidenceSetBuilder {
    public long time;

    public DenialConstraintSet run (Input data, PredicateBuilder predicates, OriginDC origin, double alpha, int n, int m,int x, int size/**/) throws Exception {
        ParsedColumn[] cols = data.getColumns();
        int[][] input = data.getInts();

        Map<Integer,String> column = new HashMap<>();
        for (int i = 0; i < cols.length; ++i) {
            column.put(i, cols[i].getName());
        }
        int columncount = cols.length;
        int[][] input_data = new int[n][columncount + 1];
        int[][] data_all = new int[n + m][columncount + 1];
        int[][] add_data = new int[m][columncount + 1];
        for (int i = 0; i < m + n; ++i) {
            for (int j = 0; j < columncount; ++j) {
                if (i < n) {
                    input_data[i][0] = i;
                    input_data[i][(j + 1)] = input[i][j];
                } else {
                    add_data[(i - n)][0] = i;
                    add_data[(i - n)][(j + 1)] = input[i][j];
                }
                data_all[i][0] = i;
                data_all[i][(j + 1)] = input[i][j];
            }
        }

        int threshold=x;
        Set<Integer> columnSet=new HashSet<>();
        for(int i=0;i<=columncount;i++){
            Set<Integer> set=new HashSet<>();
            for(int j=0;j<(m+n);j++){
                set.add(data_all[j][i]);
            }
            if(set.size()<threshold)
                columnSet.add(i);
        }

        System.out.println("attribute: ");
        for(int i:columnSet)
            System.out.println(i);

        System.out.println("building index");
        long indexstart=System.currentTimeMillis();

        Chains chains = new Chains(origin.getTotal().getdc(), input_data, alpha, column,columnSet);

        long indexend=System.currentTimeMillis();
        System.out.println("bulid index time : "+(indexend-indexstart)+" ms");
        System.out.println("Ind(E): " + chains.indexes.size());
        System.out.println("Data size: "+ RamUsageEstimator.sizeOf(data)/(1024*1024)+" MB");
        System.out.println("Index size: "+ RamUsageEstimator.sizeOf(chains.indexes)/(1024*1024)+" MB");
        System.out.println("-----------------------------------------------------------");

        long time = 0;
        long start=System.currentTimeMillis();
        Collection<ColumnPair> pairs= getColumnPairs();
        createSets(pairs);
        Repair repair=new Repair(chains.indexes,chains.column);
        DenialConstraintSet dcs = origin.getTotal();
        for (int i = 0; i < add_data.length / size; i++) {
            long rs = System.currentTimeMillis();
            int[][] add = new int[size][columncount + 1];
            for (int index = 0; index < size; ++index) {
                add[index] = add_data[(i * size + index)];
            }
            IEvidenceSet invalid=repair.getevidence(data_all,add,pairs);
            if (!invalid.isEmpty()) {
                long st=System.currentTimeMillis();
                dcs = new PrefixMinimalCoverSearch(predicates,dcs.getdc()).getDenialConstraints(invalid);
                long e=System.currentTimeMillis();
                System.out.println("evidence inversion time : "+(e-st)+"ms");
                long sm = System.currentTimeMillis();
                dcs.minimize();
                long se = System.currentTimeMillis();
                System.out.println("minimize time:"+(se - sm)+"ms");
                long checkStartTime=System.currentTimeMillis();
                dcs.checkImplication();
                long checkEndtime=System.currentTimeMillis();
                System.out.println("time for checking predicates:"+(checkEndtime-checkStartTime)+"ms");
            }
            long re = System.currentTimeMillis();
            System.out.println("the " + i + " rounds time: " + (re - rs) + "ms");
        }
        long lasttime = System.currentTimeMillis();
        long usetime=lasttime-start;
        time+=usetime;
        System.out.println("-----------------------------------------------------------");
        this.time = time;
        Set<String> set1 = new HashSet<>();
        Set<String> set2 = new HashSet<>();
        Set<String> set3 = new HashSet<>();
        for(DenialConstraint dc : origin.getTotal().getdc()){
            set1.add(dc.toString());
            set2.add(dc.toString());
        }
        for(DenialConstraint dc : dcs.getdc()){
            set3.add(dc.toString());
        }
        set1.removeAll(set3);
        set3.removeAll(set2);
        System.out.println("invalid DC size(sigma-): "+set1.size());
        System.out.println("extend DC size(sigma+): "+set3.size());
        return dcs;
    }
}
