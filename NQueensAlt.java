import java.util.*;
import java.io.*;
//Old code. I cannot guarantee it works. Shows how I was originally using a bitset.


class Genotype{
	Genotype(int n){
		size = n;

		board = new BitSet(size * size);
		//System.out.print("size" + board.length() + "\n");
	}

	public void randomStart(){
		boolean temp;
		int random;
		Random selector = new Random();
		for(int i = 0; i < size * size; i++){
			board.set(i,false);
		}
		for(int i = 0; i < size; i++){
			//temp = selector.nextBoolean();
			//System.out.print("randomizing " + temp + "\n");
			random = selector.nextInt(size * size);
			if(board.get(random) == false || i == 0)
				board.set(random);
			else
				i--;
		}
	}

	public int evaluation(){
		int count = 0;
		int conflict = 0;
		bigLoop: for(int i = 0; i < size * size; i++){
			for(int j = i; j < i / size * size + size; j++){//rows
				if(board.get(i) && board.get(j) && i != j){
					//System.out.print("rows " + i + " and " + j + "\n");
					conflict++;
					continue bigLoop;
				}
			}
			for(int j = i; j < board.length(); j+=size){//columns
				if(board.get(i) && board.get(j) && i != j){
					//System.out.print("columns " + i + " and " + j + "\n");
					conflict++;
					continue bigLoop;
				}
			}
			count = i;
			while (count < board.length()){//diagnols
				if(board.get(i) && board.get(count) && i != count){
					//System.out.print("right " + i + " and " + count + "\n");
					conflict++;
					continue bigLoop;
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
					continue bigLoop;
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

	public boolean fitness(int avg){
		if(eval == 0){
			return true;
		}
		//System.out.print("average " + avg + " ");
		//System.out.print("evaluation " + eval + "\n");
		fit = (double)avg/(double)eval;
		return false;
		//System.out.print("fitness " + fit + "\n");
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

	public int getSize(){
		return size * size;
	}

	public void flip(int n){
		board.flip(n);
	}

	public void print(){
		System.out.print("conflicts = " + eval + "\n");
		for(int i = 0; i < size * size; i++){
			if(board.get(i) ==true){
				System.out.print("1 ");
			}
			else{
				System.out.print("0 ");
			}
			
			if(i % size == size - 1){
				System.out.print("\n");
			}
		}
		//System.out.print(board.get(63));
	}

	
	int eval;
	double fit;
	int size;
	BitSet board;
}

public class NQueensAlt{
	NQueensAlt(int n){
		pass = n;
		best = pass * pass * pass; // better than possible
		worst = -1; //worst than possible
		size = 0;
		initial = new ArrayList<Genotype>();
	}

	public int randomStart(){
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

	public void evaluation(){//Finds how close to the answer each genotype is.
		int answer;
		int total = 0;
		for(int i = 0; i < size; i++){
			answer = initial.get(i).evaluation();
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
			avgEval = total/size;
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
			// System.out.print("Parent 1:\n");
			// intermediate.get(i).print();
			// System.out.print("\n");
// 
			// System.out.print("Parent 2:\n");
			// intermediate.get(i + secondOff).print();
			// System.out.print("\n");
			//System.out.print("Crosspoint: " +  crossPoint + "\n");
			
			for(int k = 0; k < pass; k++){
				choice = chooser.nextBoolean();
				crossPoint = chooser.nextInt(pass * pass);

				if(choice){
					if(intermediate.get(i).get(crossPoint) == true && newGene.get(crossPoint) == false)
						newGene.set(crossPoint,true);
					else k--;
				}
				else{
					
					if(intermediate.get(i + secondOff).get(crossPoint) == true && newGene.get(crossPoint) == false)
						newGene.set(crossPoint, true);
					else k--;
				}
			}

			// System.out.print("Child:\n");
			// newGene.print();
			// System.out.print("\n");
			// System.out.print("\n");
			newGene.evaluation();
			newGene.fitness(avgEval);
			if(newGene.getEval() < worst){
				//System.out.print("adding: " + newGene.getEval() + "\n");
				initial.add(newGene);
				//System.out.print("removing: " + worst + "\n");
				//initial.get(worstIndex).print();
				//System.out.print("\n");
				initial.remove(worstIndex);
				maxMin();
				add = true;
				//remove worst
			}
			
		//}
		}

		return add;
		//return newPop;
	}

	public boolean converge(){
		for(int i = 0; i < initial.size(); i++){
			for(int j = 0; j < initial.size(); j++){
				for(int k = 0; k < pass * pass; k++){
					if(initial.get(i).get(k) != initial.get(j).get(k)){
						return false;
					}
				}
			}
		}
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
		worst = -1;
		for(int i = 0; i <initial.size(); i++){
			answer = initial.get(i).getEval();
			if(answer < best){
				best = answer;
				bestIndex = i;
				//System.out.print("new best: " + best + "\n");
				//initial.get(i).print();
				//System.out.print("\n");
			}
			if(answer >= worst){
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
			for(int j = 0; j < pass * pass; j++){
				if( chooser.nextDouble() < .2 ){
					if(initial.get(i).get(j) == false){
						initial.get(i).set(j, true);
						count++;
					}
					else {
						initial.get(i).set(j, false);
						count--;
					}
				}
				/*if(initial.get(i).get(chosen) && queen){
					initial.get(i).set(chosen, false);
					queen = false;
				}
				else if(noQueen){
					initial.get(i).set(chosen, false);
				}*/
			}
			if(count < pass){
				for(int j = 0; j < pass * pass; j++){
					if( chooser.nextDouble() < .2 ){
						if(initial.get(i).get(j) == false){
							initial.get(i).set(j, true);
							count++;
						}
					}
					if(count == pass) break;
					else if(j == pass * pass - 1){
						j = 0;
					}
				}

			}
			else if(count > pass){
				for(int j = 0; j < pass * pass; j++){
					if( chooser.nextDouble() < .2 ){
						if(initial.get(i).get(j) == true){
							initial.get(i).set(j, false);
							count--;
						}
					}
					if(count == pass) break;
					else if(j == pass * pass - 1){
						j = 0;
					}
				}
			}
		}
	}



	int bestIndex;
	int best;
	int worst;
	int worstIndex;
	int pass;
	int size;
	ArrayList<Genotype> initial;
	ArrayList<Genotype> intermediate;
	int avgEval;
	

	public static void main (String args[]){
		int cycles = 0;
		//boolean add = true;
		int catCount = 0;
		int answer = -1;
		int temp = -1;
		NQueensAlt queen = new NQueensAlt(8);
		queen.randomStart();
		while(catCount < 3){
			temp = queen.getBest();
			queen.evaluation();
			answer = queen.fitness();
			queen.select();
			queen.breed();
			cycles++;
			//System.out.print("cycles: " + cycles + "\n");
			if(answer != -1){
				//queen.get(answer);
				break;
			}
			if(queen.converge()){
				System.out.print("cycles per convergance: " + cycles + "\n");
				cycles = 0;
				queen.catMut();
				catCount++;
			} 
			//System.out.print("conflicts: " + queen.getBest() + "\n");
			//System.out.print("size: " + queen.getSize() + "\n");
		}
		queen.get(queen.getBestIndex());
		System.out.print("conflicts: " + queen.getBest() + "\n");
	}
}


