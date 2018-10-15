import java.util.LinkedList;

import java.util.Scanner;
import java.io.File;
import java.lang.NullPointerException;
import java.io.FileNotFoundException;
public class seq_sieve{
  private final int factorize_amount = 100;
  private final String user = "gabriecv_sequential";

  private long n, max;

  private FactorPrintOut fpo;

  private int factored_counter;
  private byte[] odd_array;

  public LinkedList<Long> primes = new LinkedList<Long>();

  private boolean print_100_biggest;
  public seq_sieve(long n, int k, boolean print_100_biggest){
    this.n = n;
    this.print_100_biggest = print_100_biggest;
    max = n*n-1;
    factored_counter = 0;
    if(primes.size() != 0){
      primes.clear();
    }
    fpo = new FactorPrintOut(user, (int) n);
    primes = new LinkedList<Long>();
    odd_array = new byte[(int) ((n/16) + 1)];
  }

  public void findPrimesLessThanN(){
    primes.add(new Long(2));
    primes.add(new Long(3));
    long temp_prime = 3, long_index;
    int i;
    boolean no_more_primes;
    int sqrt_n = (int) Math.round(Math.sqrt(n));

    while(temp_prime < n){

      if(temp_prime < sqrt_n){

        //For loop goes through each number larger than p*p and smaller than n where this prime is a factor
        for(long_index = temp_prime*temp_prime; long_index < n; long_index += temp_prime * 2){
          //each of these numbers are divide-able by the prime, so they are not primes. Flip their bit.
          odd_array[(int) long_index/16] |= (1 << ((long_index/2) % 8));
          //divide i by 16 because 8 bits in each byte, all bits represent odd numbers, so divide by 2. 8 * 2 = 16.
          //(long_index/2) % 8) gives which bit is going to be flipped.
        }
      }

      //For loop finds the next prime.
      //iterates over odd numbers from temp_prime + 2 to n until it finds a prime.
      no_more_primes = true;
      for(i = (int) (temp_prime + 2); i < n; i += 2){
        if((odd_array[i/16] & (1 << ((i/2) % 8))) == 0){//If the current odd number's bit hasn't been flipped
          //then it is a prime
          no_more_primes = false;
          temp_prime = i;
          primes.add(new Long(i));
          break;
        }
      }
      if(no_more_primes){
        break;
      }
    }
  }

  public void factorBiggest100(){
    //orignal biggest starts from n*n-1
    long orignal_biggest = max, current_biggest;
    while(factored_counter < factorize_amount){
      current_biggest = orignal_biggest;
      for(long p: primes){
        while(current_biggest % p == 0){
          current_biggest /= p;
          fpo.addFactor(orignal_biggest, p);
          // System.out.println("Added factor.");
        }
      }
      if(current_biggest > 1){
        fpo.addFactor(orignal_biggest, current_biggest);
        // System.out.format("Current biggest: %12d is a prime!\n", current_biggest);
      }
      orignal_biggest--;
      factored_counter++;
    }
  }

  public times_object sequential_timed_run(){
    long overAll_startTime, overAll_elapsedTime, findPrimes_startTime, findPrimes_elapsedTime, factorize_startTime, factorize_elapsedTime;
    overAll_startTime = System.nanoTime();

    findPrimes_startTime = overAll_startTime;
    findPrimesLessThanN();
    findPrimes_elapsedTime = System.nanoTime() - findPrimes_startTime;
    // System.out.format("Biggest prime yet : %9d.\n", primes.get(primes.size()-1));

    factorize_startTime = System.nanoTime();
    factorBiggest100();
    factorize_elapsedTime = System.nanoTime() - factorize_startTime;

    overAll_elapsedTime = System.nanoTime() - overAll_startTime;

    times_object times_from_this_run = new times_object(findPrimes_elapsedTime, factorize_elapsedTime);
    fpo.writeFactors();
    File filen;
    Scanner scanner;
    if(print_100_biggest){
      String filename = user + "_" + n + ".txt";
      try{
        filen = new File(filename);
        scanner = new Scanner(filen);
        System.out.println("Sequential print from FPO File.");
        while(scanner.hasNextLine()){
          System.out.println(scanner.nextLine());
        }
      } catch(FileNotFoundException fnfe){}
        catch(NullPointerException npe){}
    }
    return times_from_this_run;
  }

  public static void main(String[] args) {
    seq_sieve s_s = new seq_sieve(Integer.parseInt(args[0]), Integer.parseInt(args[1]), true);
    times_object time = s_s.sequential_timed_run();
    System.out.format("N = %10d.\nMax = %12d.\nPrime Time:   %12d\nFactor time:  %12d.\n", s_s.n, s_s.max, time.get_prime_time(), time.get_factor_time());
    /*
    for(long p : s_s.primes){
      System.out.println(p);
    }*/
    s_s.fpo.writeFactors();
  }
}
