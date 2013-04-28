import java.util.*;
import java.io.*;



class Genotype{
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
					//System.out.print("rows " + i + " and " + j + "\n");
					conflict++;
				}
			}
			for(int j = i; j < board.length(); j+=size){//columns
				if(board.get(i) && board.get(j) && i != j){
					//System.out.print("columns " + i + " and " + j + "\n");
					conflict++;
				}
			}
			count = i;
			while (count < board.length()){//diagnols
				if(board.get(i) && board.get(count) && i != count){
					//System.out.print("right " + i + " and " + count + "\n");
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
					//System.out.print("left " + i + " and " + count + "\n");
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
}

class Population{
	Population(int n){
		pass = n;
		size = 20;
		initial = new ArrayList<Genotype>();
	}

	public void randomStart(){
		for(int i = 0; i < size; i++){
			initial.add(new Genotype(pass));
			initial.get(i).randomStart();
		}
	}

	public void add(Genotype member){
		initial.add(member);
	}

	public void evaluation(){//Finds how close to the answer each genotype is.
		int total = 0;
		for(int i = 0; i < size; i++){
			total += initial.get(i).evaluation();
		}
		avgEval = total/size;
	}

	public void fitness(){// Sets the fitness of each Genotype. Cannot be used until evaluation is run.
		for(int i = 0; i < size; i++){
			initial.get(i).fitness(avgEval);
		}
	}

	public void select(){//uses remainder stochastic sampling. Function cannot be called until fitness and evaluation are done.
		Random roulette = new Random();
		double prob;
		int recurrences;
		//int index = 0;
		intermediate = new ArrayList<Genotype>();
		for(int i = 0; i < size; i++){
			prob = initial.get(i).getFit();
			if(prob >= 1.0){
				recurrences = (int)initial.get(i).getFit(); //gives me the number of times this population should appear in the population
				for(int j = 0; j < recurrences; j++){
					intermediate.add(initial.get(i));//shallow copy. Might be bad.
				}
				prob =  prob - (double)recurrences;
			}
			if(roulette.nextDouble() <= prob){
				intermediate.add(initial.get(i));
			}
		}
	}

	public Population breed(){
		Population newPop =  new Population(pass);
		Genotype newGene;
		Random chooser = new Random();
		int crossPoint;
		for(int i = 0; i < size; i += 2){
			newGene = new Genotype(pass * pass);
			crossPoint = chooser.nextInt(pass * pass);
			for(int first = 0; first < crossPoint; first++){
				newGene.set(0,intermediate.get(i).get(first));
			}
			for(int second = crossPoint; second < pass * pass; second++){
				newGene.set(0,intermediate.get(i+1).get(second));
			}
			newPop.add(newGene);
		}
		return newPop;
	}

	int pass;
	int size;
	ArrayList<Genotype> initial;
	ArrayList<Genotype> intermediate;
	int avgEval;
}

public class NQueens{
	NQueens(int n){
		populations.add(new Population(n));
		populations.get(0).randomStart();
	}
	public void step(){
		int index = populations.size() - 1;
		populations.get(index).evaluation();
		populations.get(index).fitness();
		populations.get(index).select();
		populations.add(populations.get(index).breed());
	}
	ArrayList<Population> populations;
	public static void main(String args[]){
		NQueens queen = new NQueens(8);
		queen.step();
	}
}
