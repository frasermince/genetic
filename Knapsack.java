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
		//System.out.print("size" + board.length() + "\n");
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
				//System.out.print(inventory.get(i).weight + " ");
				//System.out.print(inventory.get(i).value + "\n");
				total += inventory.get(i).value;
				w += inventory.get(i).weight;
			}
		}
		if(w > maximum){
			total = 0;
		}
		eval = total;
		weight = w;
		//System.out.print("\n");
		return eval;
	}

	public int getEval(){
		return eval;
	}

	public void fitness(double avg){
		//System.out.print("average " + avg + " ");
		//System.out.print("evaluation " + eval + "\n");
		fit = (double)eval/avg;
		//System.out.print("fitness " + fit + "\n");
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

	/*public void flip(int n){
		board.flip(n);
	}*/

	public void print(){
		System.out.print("size = " + size + "\n");
		System.out.print("value = " + eval + "\n");
		System.out.print("weight = " + weight + "\n");
		for(int i = 0; i < size; i++){
			System.out.print(board.get(i) + "  ");
		}
		//System.out.print(board.get(63));
	}

	int weight;
	int eval;
	double fit;
	int size;
	BitSet board;
}

public class Knapsack{
	Knapsack(int n, ArrayList<Item> inv, int max){
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
			//initial.get(i).print();
			//System.out.print("\n");
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
			answer = initial.get(i).evaluation(inventory, maximum);
			total += answer;
			//System.out.print(answer + "\n");
			/*if(answer < best){
				best = answer;
				bestIndex = i;
			}
			if(answer > worst){
				worst = answer;
				worstIndex = i;
			}*/
		}
		if(size != 0){
			avgEval = (double)total/(double)size;
		}
		else 
			avgEval = 0;
	}

	public void fitness(){// Sets the fitness of each Genotype. Cannot be used until evaluation is run.
		for(int i = 0; i < size; i++){
			//System.out.print("eval = " + initial.get(i).getEval());
			initial.get(i).fitness(avgEval);
		}
	}

	public void select(){//uses remainder stochastic sampling. Function cannot be called until fitness and evaluation are done. Currently choosing worst instead of best
		Random roulette = new Random();
		double prob;
		int recurrences;
		//int index = 0;
		intermediate = new ArrayList<Genotype>();
		for(int i = 0; i < size; i++){
			prob = initial.get(i).getFit();
			if(prob >= 1.0){
				recurrences = (int)Math.floor(prob); //gives me the number of times this population should appear in the population
				//System.out.print("adding index " + i + " " + recurrences + " times. Fitness = " + prob + "\n");
				for(int j = 0; j < recurrences; j++){
					intermediate.add(initial.get(i));//shallow copy. Might be bad.
				}
				prob =  prob - (double)recurrences;
			}
			if(roulette.nextDouble() <= prob){
				//System.out.print("adding index " + i + " " + " one more time. Fitness = " + prob + "\n");
				intermediate.add(initial.get(i));
			}
		}
		Collections.shuffle(intermediate);

	}

	public boolean breed(){
		int newEval = 0;
		boolean add = false;
		int secondOff = 0;
		//Population newPop =  new Population(pass);
		Genotype newGene;
		Random chooser = new Random();
		int crossPoint;
		int interSize = intermediate.size();
		boolean choice;
		int recurrences = interSize / size;
		recurrences++;
		//System.out.print("recurrences = " + recurrences + "\n");
		for(int i = 0; i < intermediate.size(); i += 2){
			//if(i != intermediate.size() - 1){
			if(i != intermediate.size() - 1){
					secondOff = 1;
				}
			else
			secondOff= -1;
			
			newGene = new Genotype(pass);
			/*for(int bit = 0; bit < pass * pass; bit++){
				newGene.set(bit,false);
			}*/
			
			//System.out.print(pass * pass + " cross " + crossPoint + "\n");
			//System.out.print("Parent 1:\n");
			//intermediate.get(i).print();
			//System.out.print("\n");
//
			//System.out.print("Parent 2:\n");
			//intermediate.get(i + secondOff).print();
			//System.out.print("\n");
			//System.out.print("Crosspoint: " +  crossPoint + "\n");
			
			for(int k = 0; k < pass; k++){//implement two point
				choice = chooser.nextBoolean();
				//crossPoint = chooser.nextInt(pass);
				if(chooser.nextDouble() < .05){
					newGene.add(k,chooser.nextBoolean());
				}

				else if(choice){
					//if(intermediate.get(i).get(crossPoint) == true && newGene.get(crossPoint) == false)
					newGene.add(k,intermediate.get(i).get(k));
					//else k--;
				}
				else{
					
					//if(intermediate.get(i + secondOff).get(crossPoint) == true && newGene.get(crossPoint) == false)
					newGene.add(k,intermediate.get(i + secondOff).get(k));
					//else k--;
				}
			}

			// System.out.print("Child:\n");
			// newGene.print();
			// System.out.print("\n");
			// System.out.print("\n");
			newEval = newGene.evaluation(inventory, maximum);
			//newGene.fitness(avgEval);
			if(newGene.getEval() > worst){
				//System.out.print("adding: " + newGene.getEval() + "\n");
				//newGene.print();
				//System.out.print("\n");
				//newEval = newGene.evaluation();
				initial.add(newGene);

				//System.out.print("removing: " + worst + "\n");
				//initial.get(worstIndex).print();
				//System.out.print("\n");
				//System.out.print("old avg eval: " + avgEval + "\n");
				//System.out.print("(" + avgEval + " * " + size + " - " + initial.get(worstIndex).getEval() + " + " + newEval + ")/ " + size + "\n");
				avgEval = (avgEval * (double)size - (double)initial.get(worstIndex).getEval() + (double)newEval)/(double)size;
				//System.out.print("new avg eval: " + avgEval + "\n");
				initial.remove(worstIndex);
				maxMin();
				add = true;
				//remove worst
			}
			/*else{
				System.out.print("rejecting: " + newGene.getEval() + "\n");
				newGene.print();
				System.out.print("\n");
				System.out.print("keeping: " + worst + "\n");
				initial.get(worstIndex).print();
				System.out.print("\n");
			}*/
			
		//}
		}

		return add;
		//return newPop;
	}

	public boolean converge(){
		int count = 0;
		for(int i = 0; i < initial.size() - 1; i++){
			/*for(int j = 0; j < initial.size(); j++){
				for(int k = 0; k < pass; k++){
					count++;
					if(initial.get(i).get(k) != initial.get(j).get(k)){
						System.out.print("converge time = " + count + "\n");
						return false;
					}
				}*/
				if(initial.get(i).getEval() != initial.get(i + 1).getEval()){
					return false;
				}
			//}
		}
		//System.out.print("converge time = " + "\n");
		return true;
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
		worst = pass * pass;
		for(int i = 0; i <initial.size(); i++){
			answer = initial.get(i).getEval();
			if(answer > best){
				best = answer;
				bestIndex = i;
				//System.out.print("new best: " + best + "\n");
				//initial.get(i).print();
				//System.out.print("\n");
			}
			if(answer < worst){
				worst = answer;
				worstIndex = i;
				//System.out.print("new worst: " + worst + "\n");
				//initial.get(i).print();
				//System.out.print("\n");
			}
		}
	}

	public void get(int n){
		initial.get(n).print();
	}

	public void catMut(){
		//boolean first = true;
		int count = pass;
		int chosen;
		Random chooser = new Random();
		for(int i = 1; i < size; i++){
			//System.out.print("original\n");
			//initial.get(i).print();
			//System.out.print("\n");
			for(int j = 0; j < pass; j++){
				if( chooser.nextDouble() < .5 ){
					initial.get(i).add(j, chooser.nextBoolean());
				}
				/*if(initial.get(i).get(chosen) && queen){
					initial.get(i).set(chosen, false);
					queen = false;
				}
				else if(noQueen){
					initial.get(i).set(chosen, false);
				}*/
			}
			//System.out.print("mutated\n");
			//initial.get(i).print();
			//System.out.print("\n");
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
	

	public static void main (String args[]){
		ArrayList<Item> inventory = new ArrayList<Item>();
		Item tempItem;
		Scanner read = null;
		try{
			read = new Scanner(new File("/Users/Fraser/Desktop/Programming/Artificial Intelligence/k_basic/k30.txt"));
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
			//temp = queen.getBest();
			sack.fitness();
			sack.select();
			add = sack.breed();
			cycles++;
			//if(catCount == 2)
			//	System.out.print("cycles: " + cycles + "\n");
			if(catCount >= 1000){
				break;
			}
			if(sack.converge()){
				//System.out.print(catCount + " times\n");
				//queen.get(queen.getBestIndex());
				//System.out.print("\n");
				//System.out.print("cycles per convergance: " + cycles + "\n");
				cycles = 0;
				sack.catMut();
				
				catCount++;
			} 
			//System.out.print("conflicts: " + queen.getBest() + "\n");
			//System.out.print("size: " + queen.getSize() + "\n");
		}
		//queen.maxMin();
		sack.get(sack.getBestIndex());
		System.out.print("\n");
		//System.out.print("conflicts: " + queen.getBest() + "\n");
	}
}

/*public class NQueens{
	NQueens(int n){
		populations = new ArrayList<Population>();
		populations.add(new Population(n));
		populations.get(0).randomStart();
		//System.out.print("size " + populations.get(0).getSize() + "\n");
	}
	public void step(){
		int index = populations.size() - 1;
		//System.out.print(populations.get(index).getBest() + "\n");
		populations.get(index).evaluation();
		populations.get(index).fitness();
		populations.get(index).select();
		populations.add(populations.get(index).breed());
		//System.out.print("size = " + populations.get(index + 1).getSize() + "\n");
	}

	public int getBest(){
		if(populations.size() != 1)
			return populations.get(populations.size() - 2).getBest();
		else
			return -1;
	}

	ArrayList<Population> populations;
	public static void main(String args[]){
		int temp = 0;
		NQueens queen = new NQueens(8);
		while(queen.getBest() != temp){
			temp = queen.getBest();
			queen.step();
			System.out.print(queen.getBest() + "\n");
			//queen.step();
			//System.out.print(queen.getBest() + "\n");
			//queen.step();
			//System.out.print(queen.getBest() + "\n");
		}
	}
}*/
