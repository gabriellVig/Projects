import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.Random;

public class paraRad{
  private final static int NUM_BIT = 7;

  int n, k;
  private int[] a;//Array to be sorted.
  private int[] b;//Array used to move items from-to a.

  private int[] bit;//Used to know how many bit's to read at each radixSort.

  Thread[] threads;

  private int a_range, a_excess;//For splitting up a into k parts.

  //Vars needed for: Finding max number
  private final ReentrantLock max_lock = new ReentrantLock();//To synchronize updating of global_max
  private int global_max;

  //Vars needed for: Counting occurences of values
  int[][] allCount;//Storing each thread's counting
  int[] sumCount;//Product of each row from allcount.

  //No longer needed after moving step D inside runnable synchronization_4
  //int[] original_sumCount;//Sumcount before it adds each value to the one to the right.

  int count_length;//The length of sumCount.

  //Vars needed for: Step b, c, d
  int sum;//Needed for knowing how much to shift.
  int bit_index;//Which index inside bit[] to currently read.

  //Vars needed for: Step c: Sum occurances of values.
  int count_range, count_excess;

  private CyclicBarrier a_sync_divideBits;//After finding max value
  private CyclicBarrier b_sync_sumCounted;//After counting amount of values
  private CyclicBarrier c_sync_moveItems;//After summing values into sumCount
  private CyclicBarrier d_sync_setVars;//Used as Step D + reset & change of vars.
  private CyclicBarrier mainbarrier;//Used to sync with main.


  public paraRad(int k){
    int available = Runtime.getRuntime().availableProcessors();
    if(k > available){
      this.k = available;
      System.out.format("Asked for %d too many cores. Cores available: %d. K set to %d.\n", k - available, available, this.k);
    } else{
      if(k == 0){
        this.k = available;
      } else {
        this.k = k;
      }
    }
  }

  public int[] right_radix(int[] input){
    initiate_variables(input);//Initiate values

    for(int i = 0; i < k; i++){//Start the threads
      threads[i].start();
    }
    //Threads excecute step a-d
    //Find maximum value. -> Divide up bits from highest number
    //Radix-sort:
    // - Count values
    // - Sum values
    // - Move over values
    wait4Barrier(mainbarrier, 5);
    return a;
  }

  /*This method initiates all variables needed for running the program. It:
   *sets a to be the proper input array
   *calculates the ranges for the threads inside array a.
   *creates threads*/
  public void initiate_variables(int[] input){
    //Vars needed for general program:
    a = input;
    n = input.length;
    b = new int[n];

    //Vars needed to split up a.
    a_range = n / k;
    a_excess = n % k;

    a_sync_divideBits = new CyclicBarrier(k, new synchronization_1());//After finding max value
    b_sync_sumCounted = new CyclicBarrier(k, new synchronization_2());//After counting amount of values
    c_sync_moveItems = new CyclicBarrier(k, new synchronization_3());//After summing values into sumCount
    d_sync_setVars = new CyclicBarrier(k, new synchronization_4());//Used as Step D + reset & change of vars.

    mainbarrier = new CyclicBarrier(k + 1);//To Synchronize with main.

    threads = create_threads();//Creation of threads
  }

  /*This method creates k amount of threads and returns them in a Thread Array
   *It also gives each thread it's range for the array a.*/
  private Thread[] create_threads(){
    int a_left, a_right;//a_x because it splits up int[] a.
    Thread[] output = new Thread[k];

    for(int i = 0; i < k; i++){
      a_left = i * a_range;//Index * range gives 0...(max-range).
      a_right = a_left + a_range;// gives range...max

      if(i == k-1){
        a_right = a_right + a_excess;//Excess given to the last thread.
      }
      // System.out.format("[%2d] - a_left: %6d. a_right: %6d.\n",i, a_left, a_right);
      output[i] = new Thread(new Worker(i, a_left, a_right));
    }
    return output;
  }

  private class Worker implements Runnable{
    private int id, left, right;
    private int local_max;

    private int[] count;
    private int count_left, count_right;

    private Worker(int id, int left, int right){
      this.id = id;
      this.left = left;
      this.right = right;
      local_max = 0;
    }

    @Override
    public void run(){
      findSetMax(a);//Step a
      wait4Barrier(a_sync_divideBits, 1);

      for (int i = 0; i < bit.length; i++) {
        //Step b
        allCount[id] = countOccurences(a, bit[i], sum);//Count values
        wait4Barrier(b_sync_sumCounted, 2);//Calculates the range & excess used for summing the count.

        //Step c
        sumCountedOccurences(allCount, sumCount);//Sum the counted values
        wait4Barrier(c_sync_moveItems, 3);//Sums sumCount (previosly saved a copy of sumCount)

        // moveItems(sumCount, original_sumCount, b);
        /*MoveItems was replaced with a sequential soloution because:
         *You cannot split up allCount and still loop through 0..n numbers.
         * If you split up allCount, you only loop through the counted values
         * - which are counted ONLY based upon some of the bits of the value; therefore
         * to properly move items from a to b, you have to loop through 0..n, and then
         * check each number in (a[0..n] >>> sum), which gives you the values you have counted, and -
         * the index to place values which start with those bits. It does NOT give you the value you are
         * actually supposed to insert into b from a, therefore, this method ONLY works when bit.length == 1. */

        //Step d.
        wait4Barrier(d_sync_setVars, 4);// Step D runs inside here.
      }

      wait4Barrier(mainbarrier, 5);
    }


    //Step a. Find highest number
    /*This method loops through find_max_here and updates it's local max value.
     *When it has looped through all it's numbers, it safely updates the global max.*/
    private void findSetMax(int[] input){
      for (int i = left ; i < right ; i++){
        if (input[i] > local_max){
          local_max = input[i];
        }
      }
      updateMax(local_max);
    }

    /*This thread-safe method checks if @param new_max is bigger than the current value of global_max
     *if new_max is bigger, then global_max is set to the new value.*/
    private void updateMax(int new_max){
      max_lock.lock();//lock the lock
      try{//check & maybe change global_max
        if(new_max > global_max){
          global_max = new_max;
        }
      } finally{//always...
        max_lock.unlock();//unlock the lock
      }
    }

    //Step B. Count values.
    /*This method loops through the find_max_here and counts how many occurances there are of each number.
     *It returns an array which is mask+1 long.
     *masklen is the amount of bits to be checked against
     *shift is used to read the correct bit's in the number's we are sorting*/
    public int[] countOccurences(int[] input, int maskLen, int shift){
      int local_mask = (1<<maskLen) -1;
      int[] output = new int[local_mask + 1];
      for (int i = left; i < right; i++) {
        output[(input[i]>>> shift) & local_mask]++;
      }
      return output;
    }

    //Step C. Sum the counting of the values.
    /*This method adds each value of input[y][x] to output[x]
     *y is 0..k
     *x is count_left...count_right*/
    public void sumCountedOccurences(int[][] input, int[] output){
      count_left = count_range * id;
      count_right = count_left + count_range;

      if(id == k-1){
        count_right += count_excess;
      }
      for(int y = 0; y < k; y++){
        for(int x = count_left; x < count_right; x++){
          output[x] = output[x] + input[y][x];
        }
      }
    }

    /*This method is not used. Step D moved inside synchronization 4

     *Step D. Insert items into output based on their count.
     *Input = sumCount
     *orignal_input = original_sumCount
     *output = b*/
    public void moveItems(int[] input, int[] orignal_input, int[] output){
      for (int c_i = count_left; c_i < count_right; c_i++) {//Loop goes through numbers given to this thread.
        for(int amount_i = 0; amount_i < orignal_input[c_i]; amount_i++){//Loops = occurances of c_i
          output[input[c_i] + amount_i] = c_i;//The index of c_i is the spot to place c_i. + each occurance of this value.
        }
      }
    }

  }

  //This runnable is used for dividing up the bits we are sorting with.
  private class synchronization_1 implements Runnable{
    @Override
    public void run(){
      int numBit = 2,
          numDigits;
      while (global_max >= (1L << numBit)) {
        numBit++; // amount of numbers in max.
      }
      numDigits = Math.max(1, numBit/NUM_BIT);//amount of possible numbers based on amount of bits.
      bit = new int[numDigits];//Array with 1->1 ratio for indeks->natural numbers representable with numBit bits.
      int rest = numBit%NUM_BIT;
      // System.out.format("Max value: %d\n", global_max);
      for (int i = 0; i < bit.length; i++){//Loop to evenly distribute bits
        bit[i] = numBit/numDigits;
         if(rest-- > 0){
           bit[i]++;
         }
         // System.out.format("bit[%2d] = %6d\n", i, bit[i]);//Debug
      }
      initiate_step_B();
    }
  }

  /*This method initiates the variables needed for step b.
   *It is run inside synchronization_1, before counting.*/
  private void initiate_step_B(){
    bit_index = 0;
    sum = 0;
    count_length = (1 << bit[bit_index]);
    allCount = new int[k][count_length];
    sumCount = new int[count_length];
  }

  /*This method calculates the range each thread has inside allcount.
   *It is excecuted after the counting of values and before the summing of values.*/
  private class synchronization_2 implements Runnable{
    @Override
    public void run(){
      count_range = count_length / k;
      count_excess = count_length % k;
    }
  }

  /*This runnable is inside c_sync_moveItems
   *It copies sumCount while summing the values inside.*/
  private class synchronization_3 implements Runnable{
    @Override
    public void run(){
      // original_sumCount = new int[count_length];//Step D now in synchronization_4
      int acumVal = 0, j;
      for (int i = 0; i < count_length; i++) {
        // original_sumCount[i] = sumCount[i];//Step D now in synchronization_4
        j = sumCount[i];
        sumCount[i] = acumVal;
        // acumVal += original_sumCount[i];//Step D now in synchronization_4
        acumVal += j;
      }
    }
  }

  /*This runnable is inside d_sync_setVars
   *change: It moves items from a to b.
   *It changes sum += bit[bit_index], increments sum, and resets allCount & SumCount
   *It also swaps pointers between a & b*/
  private class synchronization_4 implements Runnable{
    @Override
    public void run(){

      for (int i = 0; i < n; i++) {
        b[(sumCount[(a[i] >>> sum) & (count_length-1)]++)] = a[i];
      }

      sum += bit[bit_index];
      bit_index++;

      if(bit_index < bit.length){
        count_length = (1 << bit[bit_index]);
        allCount = new int[k][count_length];
        sumCount = new int[count_length];
      }

      //Swap pointers
      int[] t = a;
      a = b;
      b = t;
    }
  }

  /*This method is used for waiting for barriers.
   *It waits at the barrier, catches possible errors and prints these to the error output.*/
  private void wait4Barrier(CyclicBarrier b, int step){
    try {
      b.await();
    } catch (InterruptedException ex) {
      System.err.format("Error: ## %s ## caught inside wait4Barrier at step %d.\n", ex, step);
    } catch (BrokenBarrierException ex) {
      System.err.format("Error: ## %s ## caught inside wait4Barrier at step %d.\n", ex, step);
    }
  }
}
