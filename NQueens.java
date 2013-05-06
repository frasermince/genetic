import java.util.*;
import java.io.*;



class Genotype{
	Genotype(int n){
		size = n;
		board = new ArrayList<Integer>();
	}

	Genotype(Genotype other, int n){
		size = n;
		board = new ArrayList<Integer>();
		for(int i = 0; i < size; i++){
			board.add(other.get(i));
		}
	}

	public void randomStart(){
		Random selector = new Random();
		for(int i = 0; i < size; i++){
			board.add(selector.nextInt(size));
		}
	}

	public int evaluation(){
		int conflict = 0;
		bigLoop: for(int i = 0; i < size; i++){
			for(int j = i; j < size; j++){//rows
				if(board.get(i) == board.get(j) && i != j){
					conflict++;
					continue bigLoop;
				}
			}
			for(int j = i; j < size; j++){//diagnols
				if(board.get(i) == board.get(j) - (j - i) && i != j){
					conflict++;
					continue bigLoop;
				}
			}
			for(int j = i; j < size; j++){
				if(board.get(i) == board.get(j) + (j - i) && i != j){
					conflict++;
					continue bigLoop;
				}
			}	
		}
		eval = conflict;
		return eval;
	}

	public int getEval(){
		return eval;
	}

	public boolean fitness(double avg){
		if(eval == 0){
			return true;
		}
		fit = avg/(double)eval;
		return false;
	}

	public double getFit(){
		return fit;
	}

	public void add(int n, int fact){
		board.add(n, fact);
	}

	public void set(int n, int fact){
		board.set(n, fact);
	}

	public int get(int n){
		return board.get(n);
	}

	public int getSize(){
		return size;
	}

	/*public void flip(int n){
		board.flip(n);
	}*/

	public void print(){
		System.out.print("conflicts = " + eval + "\n");
		for(int i = 0; i < size; i++){
			System.out.print(board.get(i) + "  ");
		}
	}

	
	int eval;
	double fit;
	int size;
	ArrayList<Integer> board;
}

public class NQueens implements Runnable{
	NQueens(int n){
		count = 0;
		zero = false;
		catCount = 0;
		pass = n;
		best = pass + 1; // worst than possible
		worst = -1; //better than possible
		size = 0;
		initial = new ArrayList<Genotype>();
		size = 100;
	}

	public int randomStart(){
		int answer = -1;
		
		for(int i = 0; i < size; i++){
			initial.add(new Genotype(pass));
			initial.get(i).randomStart();
		}
		evaluation();
		answer = fitness();
		select();
		maxMin();
		breed();
		return answer;
	}
	public int step(){
		int answer = -1;
		evaluation();
		answer = fitness();
		select();
		breed();
		return answer;
	}

	public void add(Genotype member){
		initial.add(member);
		size++;
		maxMin();
	}

	public void conditionalAdd(Genotype member){
		Genotype newGene = new Genotype(member, pass);
		int newEval = newGene.evaluation();
		if(newGene.getEval() < worst){
			initial.add(newGene);
			avgEval = (avgEval * (double)size - (double)initial.get(worstIndex).getEval() + (double)newEval)/(double)size;
			initial.remove(worstIndex);
			maxMin();
		}
	}

	public void evaluation(){//Finds how close to the answer each genotype is.
		int answer;
		int total = 0;
		for(int i = 0; i < size; i++){
			count++;
			answer = initial.get(i).evaluation();
			if(answer == 0){
				zero = true;
			}
			total += answer;
		}
		if(size != 0){
			avgEval = (double)total/(double)size;
		}
		else 
			avgEval = 0;
	}

	public int fitness(){// Sets the fitness of each Genotype. Cannot be used until evaluation is run.
		for(int i = 0; i < size; i++){
			//System.out.print("eval = " + initial.get(i).getEval());
			if(initial.get(i).fitness(avgEval)){
				return i;
			}
		}
		return -1;
	}

	public void select(){//uses remainder stochastic sampling. Function cannot be called until fitness and evaluation are done. Currently choosing worst instead of best
		Random roulette = new Random();
		double prob;
		int recurrences;
		intermediate = new ArrayList<Genotype>();
		for(int i = 0; i < size; i++){
			prob = initial.get(i).getFit();
			if(prob >= 1.0){
				recurrences = (int)Math.floor(prob); //gives me the number of times this population should appear in the population
				for(int j = 0; j < recurrences; j++){
					intermediate.add(initial.get(i));//shallow copy. Might be bad.
				}
				prob =  prob - (double)recurrences;
			}
			if(roulette.nextDouble() <= prob){
				intermediate.add(initial.get(i));
			}
		}
		Collections.shuffle(intermediate);

	}

	public boolean breed(){
		isConverged = false;
		int newEval = 0;
		boolean add = false;
		Genotype newGene;
		Random chooser = new Random();
		int count;
		int start = 0;
		int finish = 0;
		int interSize = intermediate.size();
		boolean choice;
		int recurrences = interSize / size;
		recurrences++;
		for(int i = 0; i < intermediate.size() - 1; i += 2){
			
			newGene = new Genotype(pass);
			for(int j = 0; j < pass; j++){
				newGene.add(j, 0);
			}
			start = chooser.nextInt(pass);
			finish = start;
			while(start == finish){
				finish = chooser.nextInt(pass);
			}
			count = start;
			while(count % pass != finish){// two point crossover
				if(chooser.nextDouble() < .05){
					newGene.set(count % pass, chooser.nextInt(pass));
				}
				else{
					newGene.set(count % pass,intermediate.get(i).get(count % pass));
				}
				count++;
			}

			count = finish;
			while(count % pass != start){
				if(chooser.nextDouble() < .1){
					newGene.set(count % pass, chooser.nextInt(pass));
				}
				else{
					newGene.set(count % pass,intermediate.get(i + 1).get(count % pass));
				}
				count++;
			}

			conditionalAdd(newGene);
			newEval = newGene.evaluation();
		}

		return add;
	}

	public boolean converge(){
		int count = 0;
		for(int i = 0; i < initial.size() - 1; i++){
			if(initial.get(i).getEval() != initial.get(i + 1).getEval()){
				isConverged = false;
				return isConverged;
			}
		}
		isConverged = true;
		return isConverged;
	}

	public int getCount(){
		return count;
	}

	public int getSize(){
		return size;
	}

	public int getBest(){
		return best;
	}

	public int getBestIndex(){
		return bestIndex;
	}

	public Genotype getBestGene(){
		return initial.get(bestIndex);
	}

	public int getWorst(){
		return worst;
	} 

	public boolean isZero(){
		return zero;
	}

	public void maxMin(){
		int answer = 0;
		best = pass * pass;
		worst = -1;
		if(initial != null){
			for(int i = 0; i <initial.size(); i++){
				answer = initial.get(i).getEval();
				if(answer < best){
					best = answer;
					bestIndex = i;
				}
				if(answer > worst){
					worst = answer;
					worstIndex = i;
				}
			}
		}
	}

	public void get(int n){
		initial.get(n).print();
	}

	public int getCatCount(){
		return catCount;
	}

	public void catMut(){
		catCount++;
		int count = pass;
		int chosen;
		Random chooser = new Random();
		for(int i = 1; i < size; i++){
			for(int j = 0; j < pass; j++){
				if( chooser.nextDouble() < .2 ){
					initial.get(i).add(j, chooser.nextInt(pass));
				}
			}
		}
		evaluation();
		maxMin();
	}

	public void run(){
		int cycles = 0;
		boolean add = true;
		//int catCount = 0;
		int answer = -1;
		int temp = -1;
		
		while(cycles < 10){
			answer = fitness();
			select();
			add = breed();
			cycles++;
			if(answer != -1){
				zero = true;
				break;
			}
			if(converge()){
				catMut();
				catCount++;
			} 
		}
	}
		
	int count;	
	boolean zero;
	boolean isConverged;
	int bestIndex;
	int best;
	int worst;
	int worstIndex;
	int pass;
	int size;
	ArrayList<Genotype> initial;
	ArrayList<Genotype> intermediate;
	double avgEval;
	int catCount;
	

	public static void main (String args[]) throws Exception{
		int threads = 1000;
		int answerIndex = 0;
		int popIndex = 0;
		boolean breakCondition = false;
		NQueens[] queenArray = new NQueens[threads];
		Thread[] threadArray = new Thread[threads];
		for(int i = 0; i < threads; i++){
			queenArray[i] = new NQueens(20);
			queenArray[i].randomStart();
		}


		bigLoop: while(!breakCondition){
			breakCondition = true;
			for(int i = 0; i < threads; i++){
				threadArray[i] = new Thread(queenArray[i]);
				threadArray[i].start();
			}

			for(int i = 0; i < threads; i++){
				for(int j = 0; j < threads; j++){
					threadArray[j].join();
					if(queenArray[j].isZero()){
						break bigLoop;
					}
					queenArray[j].maxMin();
					queenArray[j].conditionalAdd(queenArray[i].getBestGene());
				}
			}
			for(int i = 0; i < threads; i++){
				if(queenArray[i].getCatCount() < 1000){
					breakCondition = false;
				}
			}
		}
		int max = 1000000;
		int total = 0;
		for(int i = 0; i < threads; i++){
			queenArray[i].maxMin();
			if(queenArray[i].getBest() < max){
				answerIndex = queenArray[i].getBestIndex();
				popIndex = i;
				max = queenArray[i].getBest();
			}
			total += queenArray[i].getCount();
		}
		System.out.print("total = " + total + "\n");
		queenArray[popIndex].get(answerIndex);
		System.out.print("\n");
	}
}
