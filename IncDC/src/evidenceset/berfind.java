package evidenceset;

import chains.Builder;
import input.ColumnPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class berfind {
	private long time;
	private CountDownLatch countDownLatch;//使一个线程等待其他线程各自执行完毕后再执行
	public IEvidenceSet evidence= new HashEvidenceSet();
	public List<Builder> chains;
	public Collection<ColumnPair> pairs;
	public berfind(List<Builder> chains, int[][] data, int[][] add, Collection<ColumnPair> pairs) throws Exception{
		this.chains=chains;
		this.pairs = pairs;
		List<Thread> t1 = new ArrayList<Thread>();
		for(int i=0;i<chains.size();i++) {
			 Thread t = new Thread (new RunnableDemo(chains.get(i), data, add));
			 t1.add(t);
		}
		countDownLatch = new CountDownLatch(chains.size());
		long start = System.currentTimeMillis();
		for(int i=0;i<chains.size();i++) 
			t1.get(i).start();
	    countDownLatch.await();
		long end = System.currentTimeMillis();
		time=end-start;
	}
	public long getTime() {
		// TODO Auto-generated method stub
		return time;
	}
	
	class RunnableDemo implements Runnable {
		   private Thread t;
		   private Builder ber;
		   private int[][] data;
		   private int[][] add;
		   RunnableDemo(Builder ber, int[][] data, int[][] add) {
			   this.ber=ber;
			   this.data=data;
			   this.add = add;
		   }

		   public void run() {
//			   System.out.println("index(builder):"+ber.toString());
			   ber.findvio(data, add,evidence,pairs);
//			   System.out.println("free memory: "+(Runtime.getRuntime().maxMemory()-Runtime.getRuntime().totalMemory()+Runtime.getRuntime().freeMemory())/(1024*1024)+" MB");
			   countDownLatch.countDown();
		   }
		public void start () {
		    	  t.start();
		   }
		}
}   