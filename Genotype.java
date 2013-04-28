import java.util.*;
import java.io.*;

public class Genotype{
	Genotype(int n){
		size = n;
		board = new BitSet(size * size);
	}

	public void randomStart(){
		boolean temp;
		Random selector = new Random();
		for(int i = 0; i < size * size; i++){
			temp = selector.nextBoolean();
			//System.out.print("randomizing " + temp + "\n");
			board.set(i, temp);
		}
	}

	public int evaluation(){
		int count = 0;
		int conflict = 0;
		for(int i = 0; i < board.length(); i++){
			for(int j = i; j < i / size + size; j++){//rows
				if(board.get(i) && board.get(j) && i != j){
					System.out.print("rows " + i + " and " + j + "\n");
					conflict++;
				}
			}
			for(int j = i; j < board.length(); j+=size){//columns
				if(board.get(i) && board.get(j) && i != j){
					System.out.print("columns " + i + " and " + j + "\n");
					conflict++;
				}
			}
			count = i;
			while (count < board.length()){//diagnols
				if(board.get(i) && board.get(count) && i != count){
					System.out.print("right " + i + " and " + count + "\n");
					conflict++;
				}
				if(count % size != 6){
					count += size + 1;
				}
				else break;
			}
			count = i;
			while (count < board.length()){
				if(board.get(i) && board.get(count) && i != count){
					System.out.print("left " + i + " and " + count + "\n");
					conflict++;
				}
				if(count % size != 0){
					count += size - 1;
				}
				else break;
			}	
		}
		eval = conflict;
		return eval;
	}
		public int getEval(){
		return eval;
	}

	public void fitness(int avg){
		fit = eval/avg;
	}

	public double getFit(){
		return fit;
	}

	public void set(int n, boolean fact){
		board.set(n, fact);
	}

	public boolean get(int n){
		return board.get(n);
	}
	
	int eval;
	double fit;
	int size;
	BitSet board;

	public static void main(String args[]){
		Genotype queen = new Genotype(7);
		//queen.randomStart();
		/*for(int i = 0; i < 8 * 8; i++){
			System.out.print(queen.get(i));
		}
		System.out.print("\n");
		System.out.print(queen.evaluation());
		System.out.print("\n");*/
		for(int i = 0; i < 8 * 8; i++){
			if(i == 3 || i == 13 || i == 16 || i == 26 || i == 29 || i == 39 || i == 42)
				queen.set(i, true);
			else
				queen.set(i, false);
		}
		System.out.print(queen.evaluation());
	}
}