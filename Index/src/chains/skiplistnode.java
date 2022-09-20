package chains;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
public class skiplistnode implements Serializable {
	public int key1;
	public int key2;

	public List<Integer> value=new CopyOnWriteArrayList<Integer>();
	public skiplistnode up, down, left, right;
	public List<Integer> getValue() {
		return value;
	}

	public skiplistnode(int key1, int key2,  List<Integer> value) {
		this.value=value;
		this.key1=key1;
		this.key2=key2;
	}

	public skiplistnode(int key12, int key22, int i) {
		// TODO Auto-generated constructor stub
		this.value.add(i);
		this.key1=key12;
		this.key2=key22;

	}

	public void setvalue( List<Integer> value) {
		this.value=value;
	}


	public void setKey(int key1, int key2){
		this.key1=key1;
		this.key2=key2;
	}


	public String tostring() {

		if(value!= null)	{
			return "SLN:" +"("+ value +"," + key1+","+key2+")";}
		else return "LLN"+"("+key1+","+key2+")";
	}

}
