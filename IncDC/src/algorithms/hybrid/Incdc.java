package algorithms.hybrid;

import chains.Builder;
import chains.Chains;
import de.hpi.naumann.dc.cover.PrefixMinimalCoverSearch;
import denialconstraints.DenialConstraint;
import denialconstraints.DenialConstraintSet;
import evidenceset.IEvidenceSet;
import evidenceset.Repair;
import evidenceset.build.EvidenceSetBuilder;
import evidenceset.build.Operator;
import input.ColumnPair;
import input.Input;
import input.OriginDC;
import input.ParsedColumn;
import predicates.Predicate;
import predicates.PredicateBuilder;
import predicates.operands.ColumnOperand;

import java.io.*;
import java.util.*;

public class Incdc extends EvidenceSetBuilder {
    public long time;

    public DenialConstraintSet run (Input data, PredicateBuilder predicates, OriginDC origin, double alpha, int n, int m,
                                    int size, File indexFile) throws Exception {
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

        int threshold=(m+n)/10000;
        Set<Integer> columnSet=new HashSet<>();
        for(int i=0;i<=columncount;i++){
            Set<Integer> set=new HashSet<>();
            for(int j=0;j<(m+n);j++){
                set.add(data_all[j][i]);
            }
            if(set.size()<threshold)
                columnSet.add(i);
        }

        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile));
        Chains chains = (Chains) ois.readObject();
        ois.close();
        for(Builder builder : chains.indexes){
            builder.pre1 = strToPre(data, builder.predicate1);
            builder.pre2 = strToPre(data, builder.predicate2);
        }


        System.out.println("Ind(E): " + chains.indexes.size());
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

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile));
        oos.writeObject(chains);
        oos.flush();
        oos.close();
        return dcs;
    }
    private Predicate strToPre(Input input, String preString){
        Predicate pre = new Predicate();
        int columncount = input.getColumns().length;
        ParsedColumn<?> [] col = input.getColumns();
        boolean flag = false;
        String col1 = preString.split(" ")[0];
        String op = preString.split(" ")[1];
        Operator oper=getoperator(op);
        String col2 = preString.split(" ")[2];
        //char num;
        if(col2.charAt(col2.indexOf("t")+1)=='0'){
            flag=true;
        }
        boolean flag1=false;
        boolean flag2=false;
        ColumnOperand operand1 = new ColumnOperand();
        ColumnOperand operand2 = new ColumnOperand();
        for(int i=0;i<columncount;i++) {
            if(col[i].getName().equals(col1.substring(col1.indexOf("t")+3))) {
                operand1=new ColumnOperand(col[i],0);
                flag1=true;
            }
            if(col[i].getName().equals(col2.substring(col2.indexOf("t")+3))) {
                if(flag) operand2=new ColumnOperand(col[i],0);
                else operand2=new ColumnOperand(col[i],1);
                flag2=true;
            }
            if(flag1 && flag2){
                pre = new Predicate(oper,operand1,operand2);
                break;
            }
        }
        if(!flag1 || !flag2) System.out.println(col1 + " " +col2);
        return pre;
    }
    public Operator getoperator(String s) {
        switch(s) {
            case ">=" : return Operator.GREATER_EQUAL;
            case ">":return Operator.GREATER;
            case "==":return Operator.EQUAL;
            case "<": return Operator.LESS;
            case "<=":return Operator.LESS_EQUAL;
            case "<>": return Operator.UNEQUAL;
        }
        return null;
    }
}
