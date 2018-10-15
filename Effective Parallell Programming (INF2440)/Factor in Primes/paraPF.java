import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;

import java.util.Scanner;
import java.io.File;
import java.lang.NullPointerException;
import java.io.FileNotFoundException;
public class paraPF{
  private int n, k;
  private int sqrt2_n, sqrt_n;//sqrt_2n is the square root of sqrt_n which is the square root of n.
  private int amount_of_primes;

  private int currentPrime;//Used to check f.ex how big the currently newest found prime is

  public int[] all_primes;
  private int iAP;//Index all primes

  private byte[] byte_array;
  private byte byte_MAX_VALUE = (byte) 255;
  private int ba_range, ba_excess;
  private int iBA;//Base index for byte array
  private int[] ba_start;
  private int[] ba_end;

  Thread[] threads;

  //factorization
  private final int factorize_amount = 100;
  @SuppressWarnings("unchecked")  LinkedList<Long>[] finished_bignums = new LinkedList[factorize_amount];
  @SuppressWarnings("unchecked")  LinkedList<Long>[] completely_finished_bignums = new LinkedList[factorize_amount];
  @SuppressWarnings("unchecked")  LinkedList<Long>[][] big_nums;
  // @SuppressWarnings("unchecked")  LinkedList<Long>[][] big_nums = new LinkedList[16][100];
  private long biggest_num;

  public static void main(String[] args) {
    if(args.length != 2){
      System.out.format("Incorrect input. Correct:\njava paraPF <n> <k>\n");
      return;
    }
    try{
      paraPF ppf = new paraPF(Integer.parseInt(args[0]), Integer.parseInt(args[1]), true);
      ppf.go();
    } catch (NumberFormatException e){
      System.out.format("Input invalid. Quitting.\n");
      return;
    }
  }
  public paraPF(int n, int k, boolean print_100_biggest){
    this.n = n;
    this.k = k;
    this.print_100_biggest = print_100_biggest;
    sqrt_n = (int) Math.round(Math.sqrt(n));
    sqrt2_n = (int) Math.round(Math.sqrt(sqrt_n));

    if(n <= 1000){
      amount_of_primes = (int) Math.round((n/Math.log(n)) * 1.5);//50 % padding, about 25 % waste.
    } else if (n <= 10000){
      amount_of_primes = (int) Math.round((n/Math.log(n)) * 1.15);//15 % padding
    } else if (n <= 100000){
      amount_of_primes = (int) Math.round((n/Math.log(n)) * 1.14);//15 % padding
    } else {
      amount_of_primes = (int) Math.round((n/Math.log(n)) * 1.10);//15 % padding
    }

    all_primes = new int[amount_of_primes];
    byte_array = new byte[((n/16) + 1)];

    all_found_primes = new boolean[k];//Used to know which threads need to have their linkedlist checked in sync 1.
    all_l_primes = new LinkedList[k];//Where the threads store their local primes, used for merging

    primes_merger = new CyclicBarrier(k + 1, new merge_local_primes());
    bigNum_splitter = new CyclicBarrier(k, new split_bignums());
    final_barrier = new CyclicBarrier(k + 1);

    big_nums = new LinkedList[k][factorize_amount];
    long temp_n = new Long(n);
    biggest_num = temp_n*temp_n-1;

    fpo = new FactorPrintOut("gabriecv", n);
  }

  private boolean print_100_biggest;
  CyclicBarrier final_barrier;
  FactorPrintOut fpo;
  public times_object go(){
    times_object to = new times_object();
    long p_startTime, p_endTime ,
         f_startTime, f_endTime;
    p_startTime = System.nanoTime();


    if(n < 20000){
      // System.out.println("n < 10000. Dfferent run.");
      findPrimesUpTo(n, n, all_primes);
      threads = creator();
      splitPrimes();
      for(int i = 0; i < k; i++){
        threads[i].start();//Step 2. Find primes up to sqrt(n)
      }
      m_wait4Barrier(final_barrier, 4, false);
      m_wait4Barrier(final_barrier, 4, false);
    } else {

      // System.out.println("finding first primes");
      findPrimesUpTo(sqrt2_n, sqrt_n, all_primes);//Step 1. Find primes up to sqrt(sqrt(n)) and mark numbers up to sqrt(n)
      threads = creator();
      // System.out.println("starting threads.");
      for(int i = 0; i < k; i++){
        threads[i].start();//Step 2. Find primes up to sqrt(n)
      }
      // System.out.println("waiting for barrier");
      m_wait4Barrier(primes_merger, 1, false);//Merge the newly found primes
      //Mark numbers up to n.
      // System.out.println("threads marking numbers up to n & finding primes.");
      m_wait4Barrier(primes_merger, 2, false);

      p_endTime = System.nanoTime();
      to.set_prime_time(p_endTime - p_startTime);

      f_startTime = System.nanoTime();
      m_wait4Barrier(final_barrier, 4, false);
      m_wait4Barrier(final_barrier, 4, false);
      f_endTime = System.nanoTime();

      to.set_factor_time(f_endTime - f_startTime);
    }

    long number = biggest_num;
    for(int i = 0; i < finished_bignums.length; i++){
      int size = finished_bignums[i].size();
      for(int ii = 0; ii < size; ii++){
        fpo.addFactor(number, finished_bignums[i].poll());
      }
      number--;
    }
    fpo.writeFactors();
    //INstead of sorting the factos here, sort them when you merge the factors.
    //This means that: When you merge the factors of the bignums, try to sort them.
    File filen;
    Scanner scanner;
    if(print_100_biggest){
      String filename = "gabriecv_" + n + ".txt";
      try{
        filen = new File(filename);
        scanner = new Scanner(filen);
        System.out.println("Paralell program factors:");
        while(scanner.hasNextLine()){
          System.out.println(scanner.nextLine());
        }
        scanner.close();
      } catch(FileNotFoundException fnfe){}
        catch(NullPointerException npe){}
      double ratio = (((double)(iAP)) / all_primes.length) * 100;
      System.out.format("Square root of %d = %d. Square root of %d = %d\n",n, sqrt_n, sqrt_n, sqrt2_n);
      System.out.format("THere are probably %d primes less than %d. Creating array.\n", amount_of_primes, n);
      System.out.format("Highest prime: %d. Array size: %d Spaces left in array: %d. IAP %d. Space efficiency: %f%%\n", all_primes[iAP], all_primes.length, (all_primes.length - iAP), iAP, ratio);
    }
    return to;
    //Threads can now commence factorization of the 100 biggest numbers smaller than n*n

  }

  public void update_byteArrayVars(int max, boolean changeiBA){

    ba_start = new int[k];
    ba_end = new int[k];

    if(changeiBA){
      iBA = marked_upTo / 16;
    }

    int range = ((max - iBA*16)/16);//Max number divided by 16 gives bytecell

    ba_range = (range/k);
    if(range < k){//Smaller
      ba_range = 0;//Range would be less than 1.
      ba_excess = range;//That means only some threads will need the excess.
      //while the range is to small, add to the excess.
      while((ba_excess * 16 + iBA* 16) < max){//while the excess real number and the index of byte array's real number is less than max
        ba_excess++;
      }
    } else if(range > k){//Bigger
      ba_excess = range % k;//Something is actually in excess
    } else {//Equal
      ba_excess = 0;
      while((range * 16 + iBA* 16) < max){
        range++;
      }
    }
    int i = 0;
    while(i < k){
      if(ba_excess != 0){
        if(i == 0){
          ba_start[i] = iBA;
          ba_end[i] = ba_start[i] + ba_range + 1;// + 1 to get the excess
        } else {
          ba_start[i] = ba_end[i-1];
          ba_end[i] = ba_start[i] + ba_range + 1;// + 1 to get the excess
        }
        ba_excess--;//Excess - 1 for next loop
      } else {//if there isn't any excess
        if(i == 0){
          ba_start[i] = iBA;//let the first thread start where the last one left off
          ba_end[i] = ba_start[i] + ba_range;
        } else {
          ba_start[i] = ba_end[i-1];
          ba_end[i] = ba_start[i] + ba_range;
        }
      }
      i++;
    }
  }

  private Thread[] creator(){
    update_byteArrayVars(sqrt_n, false);
    Thread[] output = new Thread[k];
    for(int i = 0; i < k; i++){
      output[i] = new Thread(new Worker(i));
    }
    return output;
  }

  int p_range, p_excess;
  private void splitPrimes(){
    p_range = (iAP) / k;
    if(p_range != 0){
      p_excess = (iAP) % p_range;//last worker takes the excess primes.
    } else {
      p_excess = 0;
    }
  }

  int[] bn_startAt;
  int[] bn_endAt;
  private void splitBigNums(){//Called inside barrier bigNum_splitter
    bn_startAt = new int[k];
    bn_endAt = new int[k];
    int range = factorize_amount / k;
    int excess = factorize_amount % range;
    int i = 0;
    while(i < k){
      if(excess != 0){
        if(i == 0){//If it's the first
          bn_startAt[i] = 0;
          bn_endAt[i] = range + 1;
        } else {
          bn_startAt[i] = bn_endAt[i-1];
          bn_endAt[i] = bn_startAt[i] + range + 1;
        }
        excess--;
      } else {
        if(i == 0){//If it's the first
          bn_startAt[i] = 0;
          bn_endAt[i] = range;
        } else {
          bn_startAt[i] = bn_endAt[i-1];
          bn_endAt[i] = bn_startAt[i] + range;
        }
      }
      i++;
    }
  }

  private CyclicBarrier bigNum_splitter;
  private class split_bignums implements Runnable{
    @Override
    public void run(){
      splitBigNums();
    }
  }

  private CyclicBarrier primes_merger;//Used with runnable merge_local_primes, which takes each threads local primes and stores them globally in all_primes
  private boolean[] all_found_primes;//Used to know which threads need to have their linkedlist checked in sync 1.
  int step = 0;
  @SuppressWarnings("unchecked")  LinkedList<Integer>[] all_l_primes;// = new LinkedList[factorize_amount];//Used to store pointers to the threads local primes.
  private class merge_local_primes implements Runnable{
    @Override
    public void run(){
      int temp_p = 0, size;
      for (int i = 0; i < threads.length; i++) {//For each thread
        if(all_found_primes[i]){//if the thread found primes
          size = all_l_primes[i].size();//Get the size
          for(int counter = 0; counter < size; counter++){//Do this size times.
            temp_p = all_l_primes[i].remove();//get and remove the first element of the list.
            all_primes[++iAP] = temp_p;//Add prime to list.
          }
        }
      }
      if(step == 0){//If this has not happened.
        update_byteArrayVars(n, true);//Updates the ranges for the threads inside byteArray
        step++;
      } else if (step == 1){
        splitPrimes();
      }
    }

    private boolean contains_element(int[] search_this, int search_for){
      for(int i = 0; i < search_this.length; i++){
        if(search_this[i] == search_for){
          return true;
        }
      }
      return false;
    }
  }

  private class Worker implements Runnable{
    int id;
    int ba_left, ba_right;

    int p_left, p_right;
    int bn_left, bn_right;
    private LinkedList<Integer> l_primes = new LinkedList<Integer>();

    private Worker(int id){
      this.id = id;
      update_BAvars();
    }

    public void update_BAvars(){
      this.ba_left = ba_start[id];
      this.ba_right = ba_end[id];
      if(id == k-1){
        this.ba_right++;
      }
      // System.out.format("[T:%2d] - ba_left: %4d. ba_right: %4d.\n",id, ba_left, ba_right);
    }

    public void update_primeVars(){
      this.p_left = id * p_range;
      if(id == k-1){
        this.p_right = p_left + p_range + p_excess;
      } else {
        this.p_right = p_left + p_range;
      }
      // System.out.format("[T:%2d] - p_left = %4d | p_right = %4d.\n",id, p_left, p_right);
    }
    public void update_bigNumVars(){
      bn_left = bn_startAt[id];
      bn_right = bn_endAt[id];
      // System.out.format("[T:%2d] - bn_left = %2d | bn_right = %2d.\n",id, bn_left, bn_right);
    }
    @Override
    public void run(){

      if(n < 20000){
        update_primeVars();
        factorize_all();

        wait4Barrier(bigNum_splitter,3 ,false);
        update_bigNumVars();

        merge_factors();
        wait4Barrier(final_barrier, 4, false);//At this point, go through the big nums and find out how many times each factor can divide the corresponding big num
        amount_of_factors();
        wait4Barrier(final_barrier, 4, false);//At this point, go through the big nums and find out how many times each factor can divide the corresponding big num
        return;
      }

      if(ba_left == ba_right){
        // System.out.format("[T:%2d] - Not running\n",id );
        all_found_primes[id] = false;
      } else {
        // System.out.format("[T:%2d] - finding primes\n",id);
        findPrimes(sqrt_n);//Find primes up to sqrt(n)
        all_found_primes[id] = true;
        all_l_primes[id] = l_primes;
      }

      // System.out.format("[T:%2d] - waiting to merge primes\n",id);
      wait4Barrier(primes_merger, 1, false);//

      update_BAvars();
      if(ba_left == ba_right){
        // System.out.format("[T:%2d] - Not running\n",id );
        all_found_primes[id] = false;
      } else {
        all_l_primes[id] = l_primes;
        traverseByteArray();
        findPrimes(n);
        all_found_primes[id] = true;
      }
      wait4Barrier(primes_merger, 2, false);

      update_primeVars();
      //Commence factorization
      factorize_all();
      wait4Barrier(bigNum_splitter,3 ,false);
      update_bigNumVars();
      merge_factors();
      wait4Barrier(final_barrier, 4, false);//At this point, go through the big nums and find out how many times each factor can divide the corresponding big num
      amount_of_factors();
      wait4Barrier(final_barrier, 5, false);
    }

    public void factorize_all(){
      long bn = biggest_num;

      for(int counter = 0; counter < factorize_amount; counter++){//For each big number
        // System.out.format("[T:%2d] - Factoring: %d\n",id, bn);
        big_nums[id][counter] = new LinkedList<Long>();//Create a list for the factors
        for(int p_i = p_left; p_i < p_right; p_i++){//Then iterate through your primes.
          // p = new Long(all_primes[p_i]);
          if(bn % all_primes[p_i] == 0){//If the big number is divisible by the prime
            big_nums[id][counter].add(new Long(all_primes[p_i]));//then add the prime as a factor
          }
        }
        bn--;
      }
    }
    /*
    */
    public Long[] lList2Array(LinkedList<Long> ll){
      int size = ll.size(), i = 0;
      Long[] output = new Long[size];
      for(long p : ll){
        output[i++] = p;
      }
      return output;
    }

    public void amount_of_factors(){
      long b;
      long currentFactor;
      Long[] factors;
      for (int i = bn_startAt[id]; i < bn_endAt[id]; i++) {//HER DU SLUTTA FORRIGE GANG. DET DU IKKE FIKK TIL ER AT NOEN TRÅDER LÅSER SEG PÅ amount_of_factors().
        b = biggest_num-i;//b is the currently worked on big num

        factors = lList2Array(finished_bignums[i]);

        for (int ii = 0; ii < factors.length; ii++) {
          b = b/factors[ii];
        }
        /*
        if(b == 0){
          long temp_dividethis = biggest_num-i;
          for (int ii = 0; ii < factors.length; ii++) {
            System.out.format("[T:%2d] - B = ZERO. Orig: %8d. | %8d/%4d = %8d\n",id, biggest_num-i,  temp_dividethis, factors[ii], temp_dividethis/factors[ii]);
            temp_dividethis = temp_dividethis/factors[ii];
          }
        }
        */
        int linkedlist_index;
        if(b != 1){
          for (int ii = 0; ii < factors.length; ii++) {
            while(b % factors[ii] == 0){
              linkedlist_index = finished_bignums[i].indexOf(factors[ii]);//The factors have to be monotomicly increasing
              finished_bignums[i].add(linkedlist_index, factors[ii]);
              b /= factors[ii];
            }
          }
        }
        if(b != 1){
          finished_bignums[i].add(b);
        }

      }
    }

    public void findPrimes(int max_val){
      for(int x = ba_left; x < ba_right; x++){
        if(byte_array[x] != byte_MAX_VALUE){
          for(int bit = 0; bit < 8; bit++){
            if((byte_array[x] & (1 << bit)) == 0){
              l_primes.add((x*16 + bit*2 + 1));
            }
          }
        }
      }
    }

    public void merge_factors(){
      int size;
      for(int i = bn_startAt[id]; i < bn_endAt[id]; i++){
        finished_bignums[i] = new LinkedList<Long>();
      }
      for(int y = 0; y < k; y++){
        for(int x = bn_startAt[id]; x < bn_endAt[id]; x++) {
          size = big_nums[y][x].size();
          for (int i = 0; i < size; i++) {
            finished_bignums[x].add(big_nums[y][x].poll());
          }
        }
      }
    }

    public void traverseByteArray(){
      int p, missing2StartAtLeft, real_left = ba_left*16,
                    real_right = ba_right*16;

      for(int i = 1; i <= iAP; i++){

        p = all_primes[i];
        missing2StartAtLeft = real_left % p;

        int number, byteCell, bit;
        number = (real_left + p - missing2StartAtLeft);
        if((number & 1) == 0){//So that it never starts on an even number.
          number += p;
        }
        for(int p_val = number; p_val < real_right; p_val += p*2){
          if((p_val & 1) == 1){//If p_val is odd.
            byteCell = p_val / 16;
            bit = (p_val/2) % 8;
            byte_array[byteCell] |= (1 << bit);//Flip each bit where currentPrime is a factor.
          }
        }
      }
    }

    public boolean wait4Barrier(CyclicBarrier barrier, int step, boolean print){
      try {
        if(print){
          System.out.format("Thread  %02d waiting for step %2d.\n", id, step);
        }
        barrier.await();
        return true;
       } catch (InterruptedException ex) {
        return false;
       } catch (BrokenBarrierException ex) {
        return false;
      }
    }
  }

  private boolean first_FP_run = true;
  int marked_upTo = 0;
  // int primes_upTo = 0;
  //This method only finds primes with values up to x, marking numbers up to mark_upTo, inserting primes into output.
  public void findPrimesUpTo(int x, int mark_upTo, int[] output){
    // System.out.format("Finding primes up to: %d. Marking up to: %d.\n",x, mark_upTo );
    int missing_nums = mark_upTo % 16;//100 % 16 = 4. Need 16-4 higher mark-upto.
    int missing_bits = (x/2) % 8;
    if(missing_nums != 0){
      mark_upTo += (16-missing_nums);
    }
    marked_upTo = mark_upTo;
    if(x % 16 != 0){
      x += 16 - (x % 16);
    }
    // System.out.println("X = " + x);

    if(first_FP_run){
      all_primes[0] = 2;
      all_primes[1] = 3;
      iAP = 1;
      first_FP_run = false;
    }
    currentPrime = all_primes[iAP];//First run this is set to be 3.

    while(currentPrime <= x){
      int byteCell = (currentPrime / 16),
          bit = (currentPrime/2) % 8;
      for(int i = currentPrime*currentPrime; i <= mark_upTo; i += currentPrime *2){//Loops from p*p..n and marks all non-primes wich is a factor for any currently found primes.
        if((i & 1) == 1){
          byteCell = i / 16;
          bit = (i/2) % 8;
          byte_array[byteCell] |= (1 << bit);//Flip each bit where currentPrime is a factor.
        }
        // System.out.format("ByteCell[%1d] - Bit: %1d. Marking num: %d\n",byteCell, bit, i);
      }
      // System.out.format("Bytecell was %d\n", byteCell);
      currentPrime = findNextPrime(currentPrime+2, x);//Start at the next odd number and find the first unmarked bit.
      if(currentPrime == 0 || currentPrime > x){
        return;
      }
      output[++iAP] = currentPrime;//Add the prime.
    }
  }
  //Finds the first 0 bit from starting location @param int startAt and returns the number this bit represents.
  public int findNextPrime(int startAt, int max){
    for(int i = startAt; i <= max; i += 2){
      int byteCell = i/16,
          bit = (i/2) % 8;
      if((byte_array[byteCell] & (1 << bit)) == 0){
        iBA = byteCell + 1;
        // System.out.format("Next prime: %4d. iBA: %2d. Bytecell: %2d. Bit: %2d\n ",i, iBA, byteCell, bit);
        return i;
      }
    }
    return 0;
  }

  public boolean m_wait4Barrier(CyclicBarrier barrier, int step, boolean print){
    try {
      if(print){
        System.out.format("Main waiting for step %2d.\n", step);
      }
      barrier.await();
      return true;
     } catch (InterruptedException ex) {
      return false;
     } catch (BrokenBarrierException ex) {
      return false;
    }
  }
}
