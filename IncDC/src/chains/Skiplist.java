package chains;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

public class Skiplist {
	public int n=0;
	public int h;
	private skiplistnode head;
	private skiplistnode tail;
	private Random r;

	public Skiplist() {
		head = new skiplistnode(Integer.MIN_VALUE,Integer.MIN_VALUE,null) ;
		tail = new skiplistnode(Integer.MAX_VALUE,Integer.MAX_VALUE,null);
		head.right=tail;
		tail.left=head;
		n=0;
		h=0;
		r= new Random();
	}
	public skiplistnode find(Integer key1) {
		skiplistnode p;
		p=head;
		while(true) {
			while((p.right.key1!=Integer.MAX_VALUE&&p.right.key1<key1)) {
				p=p.right;
			}
			if(p.down!=null) {p=p.down;}
			else break;
		}
		return p;
	}

	public skiplistnode findkey2(Integer key2) {
		skiplistnode p;
		p=head;
		while(true) {
			while((p.right.key2!=Integer.MAX_VALUE&&p.right.key2<key2)) {
				p=p.right;
			}
			if(p.down!=null) {p=p.down;}
			else break;
		}
		return p;
	}
	public skiplistnode findkey22(Integer key2) {
		skiplistnode p;
		p=head;
		while(true) {
			while((p.right.key2!=Integer.MAX_VALUE&&p.right.key2>key2)) {
				p=p.right;
			}
			if(p.down!=null) {p=p.down;}
			else break;
		}
		return p;
	}


	public int checkinsert(int key1, int key2, int value,int flag,int type) {
		if(n==0) {insert(key1, key2,value,flag,type);return 0;}
		skiplistnode p=find(key1);

		if(p.right.key1==key1 && p.right.key2==key2) {
			p.right.value.add(value);
			n++;
			return 0;
		}

		if(type==3) {
			if(p.right.key1==key1) return 1;
			/**
			 * if(p.key1<Integer.MAX_VALUE)
			 * return Math.abs(p.right.key1-key1;
			 * */
			if(p.key1<Integer.MAX_VALUE&&p.right.key1>key1) return p.right.key1-key1;
			else return key1-p.right.key1;
		}

		if(type==2) {
			if(p.right.key1==key1 || p.right.key2*flag<=flag*key2||p.key2*flag>=key2*flag) return -1;
			else {
				if(p.key2>Integer.MIN_VALUE&&p.key2<Integer.MAX_VALUE)
					return Math.abs(key2 - p.key2);
				else {insert(key1,key2,value,flag,type);return 0;}

			}
		}

		if(type==1) {
			if(p.key2*flag>=key2*flag||p.right.key2*flag<=flag*key2) return -1;
			else {
				while(p.right.key1==key1) {
					if(p.right.key2*flag>=key2*flag||p.right.key2==Integer.MAX_VALUE) break;
					p=p.right;
				}
				if(p.right.key1==key1 && p.right.key2==key2) {
					p.right.value.add(value);
					n++;
					return 0;
				}
				if(p.key2>Integer.MIN_VALUE&&p.key2<Integer.MAX_VALUE) return Math.abs(key2 - p.key2);
				else {insert(key1,key2,value,flag,type);return 0;}
			}
		}

		if(type==0) {
			if(p.key2*flag>key2*flag) return -1;

			while(p.right.key1==key1) {
				if(p.right.key2*flag>flag*key2||p.right.key2==Integer.MAX_VALUE) break;
				p=p.right;
				}

			if(p.key1==key1&&p.key2==key2) {p.value.add(value);n++;return 0;}
			if(p.right.key2*flag<=key2*flag&&p.right.key2<Integer.MAX_VALUE) return -1;
			if((p.key2>Integer.MIN_VALUE&&p.key2<Integer.MAX_VALUE))
			{return Math.abs(key2 - p.key2);}
			else {insert(key1,key2,value,flag,type);return 0;}
		}

		System.out.println("error check of :" +key1+key2);
		return -1;
	}


	public void insert(int key1, int key2, int value,int flag,int type) {
		skiplistnode p=find(key1),q;

		if(p.right.key1==key1 && p.right.key2==key2) {
			if(!p.right.value.contains(value))
				p.right.value.add(value);
			n++;
			return;
		}
		if(type==3) {
			while(p.right.key1==key1) {
				if(p.right.key2>key2||p.right.key2==Integer.MAX_VALUE) break;
				p=p.right;
			}
		}


		if(type==1||type==0) {
			while(p.right.key1==key1) {
				if(p.right.key2*flag>key2*flag||p.right.key2==Integer.MAX_VALUE) break;
				p=p.right;
			}
		}
		if(p.key1==key1 && p.key2==key2) {
			if(!p.value.contains(value))
				p.value.add(value);
			n++;
			return;
		}

		if(p.right.key1==key1 && p.right.key2==key2) {
			if(!p.right.value.contains(value))
				p.right.value.add(value);
			n++;
			return;
		}
		int i=0;
		List<Integer> s = new ArrayList<Integer>();
		s.add(value);
		q=new skiplistnode(key1,key2,s);
		q.left=p;
		q.right=p.right;
		p.right.left=q;
		p.right=q;
		while(r.nextDouble()>=0.5) {
			if(i>=h) {
				addEmptylevel();
				if(i>2*h) break;
			}
			while(p.up==null) {
				p=p.left;
			}
			p=p.up;
			skiplistnode e;
			List<Integer> se = new ArrayList<Integer>();
			if(i==0) se.add(value);
			e=new skiplistnode(key1,key2,se);
			e.left=p;
			e.right=p.right;
			e.down=q;
			p.right.left=e;
			p.right=e;
			q.up=e;
			q=e;
			i=i+1;
		}
		n=n+1;
	}

	private void addEmptylevel() {
		skiplistnode p1,p2;
		p1=new skiplistnode(Integer.MIN_VALUE,Integer.MIN_VALUE,null);
		p2=new skiplistnode(Integer.MAX_VALUE,Integer.MAX_VALUE,null);
		p1.right=p2;
		p1.down=head;
		p2.left=p1;
		p2.down=tail;
		head.up=p1;
		tail.up=p2;
		head=p1;
		tail=p2;
		h=h+1;
	}

	public String toString() {
		skiplistnode p,q;
		p=head;
		q=head;
		StringBuffer s=new StringBuffer();
		while(q!=null) {
			while(p!=null) {
				s.append(p.tostring()+"  ----->   ");
				p=p.right;
			}
			p=q.down;
			s.append("\n");
			q=q.down;
		}
		return s.toString();
	}

	public int getH() {
		return h;
	}
}
