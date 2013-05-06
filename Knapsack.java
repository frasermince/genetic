import java.util.*;
import java.io.*;


class Item{
	Item(int w, int v){
		weight = w;
		value = v;
	}
	public int weight;
	public int value;
}

class Genotype{
	Genotype(int n){
		size = n;
		board = new BitSet();
	}

	public void randomStart(){
		Random selector = new Random();
		for(int i = 0; i < size; i++){
			board.set(i,selector.nextBoolean());
		}
	}

	public int evaluation(ArrayList<Item> inventory, int maximum){
		int total = 0;
		int w = 0;
		for(int i = 0; i < inventory.size(); i++){
			if(board.get(i)){
				total += inventory.get(i).value;
				w += inventory.get(i).weight;
			}
		}
		if(w > maximum){
			total = 0;
		}
		eval = total;
		weight = w;
		return eval;
	}

	public int getEval(){
		return eval;
	}

	public void fitness(double avg){
		fit = (double)eval/avg;
	}

	public double getFit(){
		return fit;
	}

	public void add(int n, boolean fact){
		board.set(n, fact);
	}

	public boolean get(int n){
		return board.get(n);
	}

	public int getSize(){
		return size;
	}

	public void print(){
		System.out.print("size = " + size + "\n");
		System.out.print("value = " + eval + "\n");
		System.out.print("weight = " + weight + "\n");
		for(int i = 0; i < size; i++){
			System.out.print(board.get(i) + "  ");
		}
	}

	int weight;
	int eval;
	double fit;
	int size;
	BitSet board;
}

public class Knapsack{
	Knapsack(int n, ArrayList<Item> inv, int max){
		count = 0;
		maximum = max;
		inventory = inv;
		pass = n;
		best = -1; // worst than possible
		worst = pass + 1; //better than possible
		size = 0;
		initial = new ArrayList<Genotype>();
	}

	public void randomStart(){
		int answer = -1;
		size = 80;
		for(int i = 0; i < size; i++){
			initial.add(new Genotype(pass));
			initial.get(i).randomStart();
		}
		System.out.print("size " + initial.get(0).getSize() + "\n");
		evaluation();
		fitness();
		select();
		maxMin();
		breed();
	}
	public void step(){
		int answer = -1;
		evaluation();
		fitness();
		select();
		breed();
	}

	public void add(Genotype member){
		initial.add(member);
		size++;
		maxMin();
	}

	public void evaluation(){//Finds how close to the answer each genotype is.
		int answer;
		int total = 0;
		for(int i = 0; i < size; i++){
			count++;
			answer = initial.get(i).evaluation(inventory, maximum);
			total += answer;
		}
		if(size != 0){
			avgEval = (double)total/(double)size;
		}
		else 
			avgEval = 0;
	}

	public void fitness(){// Sets the fitness of each Genotype. Cannot be used until evaluation is run.
		for(int i = 0; i < size; i++){
			initial.get(i).fitness(avgEval);
		}
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
					intermediate.add(initial.get(i));
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
		int newEval = 0;
		boolean add = false;
		int secondOff = 0;
		Genotype newGene;
		Random chooser = new Random();
		int crossPoint;
		int interSize = intermediate.size();
		boolean choice;
		int recurrences = interSize / size;
		recurrences++;
		for(int i = 0; i < intermediate.size(); i += 2){
			if(i != intermediate.size() - 1){
					secondOff = 1;
				}
			else
			secondOff= -1;
			
			newGene = new Genotype(pass);
			for(int k = 0; k < pass; k++){//random crossover
				choice = chooser.nextBoolean();
				if(chooser.nextDouble() < .05){
					newGene.add(k,chooser.nextBoolean());
				}

				else if(choice){
					newGene.add(k,intermediate.get(i).get(k));
				}
				else{
					newGene.add(k,intermediate.get(i + secondOff).get(k));
				}
			}
			count++;
			newEval = newGene.evaluation(inventory, maximum);
			if(newGene.getEval() > worst){
				initial.add(newGene);
				avgEval = (avgEval * (double)size - (double)initial.get(worstIndex).getEval() + (double)newEval)/(double)size;
				initial.remove(worstIndex);
				maxMin();
				add = true;
			}
		}

		return add;
	}

	public boolean converge(){
		int count = 0;
		for(int i = 0; i < initial.size() - 1; i++){
			if(initial.get(i).getEval() != initial.get(i + 1).getEval()){
				return false;
			}
		}
		return true;
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

	public int getWorst(){
		return worst;
	}

	public void maxMin(){
		int answer = 0;
		best = -1;
		worst = 100000000;
		for(int i = 0; i <initial.size(); i++){
			answer = initial.get(i).getEval();
			if(answer > best){
				best = answer;
				bestIndex = i;
			}
			if(answer < worst){
				worst = answer;
				worstIndex = i;
			}
		}
	}

	public void get(int n){
		initial.get(n).print();
	}

	public void catMut(){
		int count = pass;
		int chosen;
		Random chooser = new Random();
		for(int i = 1; i < size; i++){
			for(int j = 0; j < pass; j++){
				if( chooser.nextDouble() < .5 ){
					initial.get(i).add(j, chooser.nextBoolean());
				}
			}
		}
		evaluation();
		maxMin();
	}


	int maximum;
	ArrayList<Item> inventory;
	int bestIndex;
	int best;
	int worst;
	int worstIndex;
	int pass;
	int size;
	ArrayList<Genotype> initial;
	ArrayList<Genotype> intermediate;
	double avgEval;
	int count;
	

	public static void main (String args[]){
		ArrayList<Item> inventory = new ArrayList<Item>();
		Item tempItem;
		Scanner read = null;
		try{
			read = new Scanner(new File("/Users/Fraser/Desktop/Programming/Artificial Intelligence/k_basic/k10.txt"));
		}
		catch(FileNotFoundException e){
			System.err.println("FileNotFoundException: " + e.getMessage());
		}
		int max = read.nextInt();
		while(read.hasNext()){
			tempItem = new Item(read.nextInt(), read.nextInt());
			inventory.add(tempItem);
			read.next();
		}

		int cycles = 0;
		boolean add = true;
		int catCount = 0;
		int answer = -1;
		int temp = -1;
		Knapsack sack = new Knapsack(inventory.size(), inventory, max);
		sack.randomStart();
		while(true){
			sack.fitness();
			sack.select();
			add = sack.breed();
			cycles++;
			if(catCount >= 10){
				break;
			}
			if(sack.converge()){
				cycles = 0;
				sack.catMut();
				
				catCount++;
			} 
		}
		System.out.print("evaluations: " + sack.getCount() + "\n");
		sack.get(sack.getBestIndex());
		System.out.print("\n");
	}
}
