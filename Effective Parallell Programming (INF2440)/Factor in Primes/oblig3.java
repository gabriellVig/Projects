import static java.lang.Math.toIntExact;
import java.util.Scanner;
import java.io.File;
import java.util.NoSuchElementException;
import java.lang.IllegalStateException;

public class oblig3{
  public static void main(String[] args) {
    int input_n = 0;
    int input_k = 0;
    boolean correct_input = true;
    String nfe = "";
    try{
        correct_input = (args.length == 2);
        input_n = Integer.parseInt(args[0]);
        input_k = Integer.parseInt(args[1]);
        int threadsAvailable = Runtime.getRuntime().availableProcessors();
        if(input_k == 0){
          input_k = threadsAvailable;
        } else if(input_k > threadsAvailable){
          System.out.println("You asked for too many cores. Set to max: " + threadsAvailable);
          input_k = threadsAvailable;
        }
      }catch(NumberFormatException t_nfe){
        correct_input = false;
        nfe = t_nfe.toString();
      }
    if(!correct_input){
      System.out.format("Invalid input. Correct usage:\njava oblig3 <n> <k>\nError:\n%s\n", nfe);
      return;
    }
    oblig3 app = new oblig3(input_n, input_k);
    app.go(args);
  }
  public oblig3(int n, int k){
    this.n = n;
    this.k = k;
  }
  //This sorter keeps the integrity of the list.
  public int[] sortInteger(int[] input){
    int temp_int;
    for(int i = 0; i < input.length-1; i++){
      while(input[i] > input[i+1]){
        temp_int = input[i+1];
        input[i+1] = input[i];
        input[i] = temp_int;
      }
    }
    return input;
  }
  paraPF parallell_program;
  int n, k;
  times_object[] para_times = new times_object[7];

  times_object[] seq_times = new times_object[7];
  public void go(String[] args){
    long para_startTime, para_endTime,
         para_p_startTime, para_p_endTime,
         para_f_startTime, para_f_endtTime;
    long seq_startTime, seq_endTime,
         seq_p_startTime, seq_p_endTime,
         seq_f_startTime, seq_f_endtTime;
    //indent
    paraPF pp;
    int_seq_sieve iss;
    seq_sieve ss;
    Scanner keyboard = new Scanner(System.in);
    System.out.format("Do you want to check if the parallell & Sequential programs contain the same primes?\nOR make them print the factors of (n^2-100...n^2)\ny/n?->");
    try{
      if(keyboard.next().charAt(0) == 'y'){
        System.out.format("Do you want to print the prime factors of (n^2-100...n^2)\ny/n?->");
        boolean print_factors = keyboard.next().charAt(0) == 'y';

        System.out.format("Do you want to check if the parallell and sequential programs generated the same primes?\ny/n?->");
        boolean check_primes = keyboard.next().charAt(0) == 'y';

        iss = new int_seq_sieve(n, k, print_factors);//Creating sequential Primenumber Factoring object
        iss.sequential_timed_run();

        pp = new paraPF(n, k, print_factors);//Creating parallell Primenumber Factoring object
        pp.go();//Running parallell program


        if(check_primes){
          pp.all_primes = sortInteger(pp.all_primes);//Sorting the parallell program primes before checking(saves alot of time)
          //Sequential prgoram which stores primes as Long. END
          */
          //Sequential prgoram which stores primes as integer. START
          int i = 0;
          for(int p : iss.primes){
            if(p != pp.all_primes[i]){
              System.out.format("Seq_P[%d] -> %d != %d <- Para_P[%d].\n",i, iss.primes.get(i), pp.all_primes[i], i);
              try{
                for(int x = i-5; x < i+5; x++){
                  System.out.format("Seq_P[%4d] = %5d Para_P[%4d] = %5d.\n",x, iss.primes.get(x),x, pp.all_primes[x]);
                }

              } catch (IndexOutOfBoundsException ioobe){}
              break;
            }
            i++;
          }
        }
        //Sequential prgoram which stores primes as integer. END
      }
    } catch (NoSuchElementException nee){
      System.out.format("Error from input. Taking that as a no. Error:\n%s", nee.toString());
    } catch(IllegalStateException ise){
      System.out.format("Error from scanner, scanner is closed. Exiting..Error:\n%s", ise.toString());
      return;
    }

    System.out.println("Paralell runs:");
    for(int i = 0; i < para_times.length;i++){
      para_startTime = System.nanoTime();
      pp = new paraPF(n, k, false);//Creating parallell Primenumber Factoring object
      para_times[i] = pp.go();
      para_endTime = System.nanoTime();
      para_times[i].set_overall_time(para_endTime-para_startTime);
      System.out.format("PrimeTime: %8d. Factoring time: %8d. Overall Time: %8d.\n", para_times[i].get_prime_time(), para_times[i].get_factor_time(),para_times[i].get_overall_time() );
    }
    System.out.println("Sequential runs:");

    for(int i = 0; i < seq_times.length;i++){
      seq_startTime = System.nanoTime();
      iss = new int_seq_sieve(n, k, false);//Creating sequential INTEGER Primenumber Factoring object
      // ss = new seq_sieve(new Long(n), k, false);//Creating sequential LONG Primenumber Factoring object
      seq_times[i] = iss.sequential_timed_run();//Running INTEGER
      // seq_times[i] = ss.sequential_timed_run();//RUnning LONG
      seq_endTime = System.nanoTime();
      seq_times[i].set_overall_time(seq_endTime-seq_startTime);
      System.out.format("PrimeTime: %8d. Factoring time: %8d. Overall Time: %8d.\n", seq_times[i].get_prime_time(), seq_times[i].get_factor_time(), seq_times[i].get_overall_time());
    }

    seq_times = sorter(seq_times);
    para_times = sorter(para_times);
    double su_p, su_f, su_o;
    su_p = ((double) seq_times[3].get_prime_time())/((double) para_times[3].get_prime_time());
    su_f = ((double) seq_times[3].get_factor_time())/((double) para_times[3].get_factor_time());
    su_o = ((double) seq_times[3].get_overall_time())/((double) para_times[3].get_overall_time());
    System.out.format("All speedup times calculated from run 3 after sorting by overall timing.\nALl times are in ms.\n");
    System.out.format("Prime: Seq # Paral = Speedup | Factor: Seq # Paral = Speedup | Overall: Seq # Para. Speedup |\n");
    System.out.format("     %5.0f # %5.0f     %2.3f |       %5.0f # %5.0f     %2.3f |        %5.0f # %5.0f   %2.3f |\n",
                        (double) seq_times[3].get_prime_time(),
                        (double) para_times[3].get_prime_time(),su_p,
                        (double) seq_times[3].get_factor_time(),
                        (double) para_times[3].get_factor_time(), su_f,
                        (double) seq_times[3].get_overall_time(),
                        (double) para_times[3].get_overall_time(), su_o);
    System.out.format("Prime   speedup: %f\nFactor  speedup: %f\nOverall Speedup: %f\n",su_p,su_f,su_o);

    /*For printing for inserting into f.ex excel.
    System.out.format("%d\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f\n%f",n,
                     (double) seq_times[3].get_prime_time(),
                     (double) para_times[3].get_prime_time(),su_p,
                     (double) seq_times[3].get_factor_time(),
                     (double) para_times[3].get_factor_time(), su_f,
                     (double) seq_times[3].get_overall_time(),
                     (double) para_times[3].get_overall_time(), su_o);
    */
  }
  public times_object[] sorter(times_object[] input){
    times_object temp;
    for(int i = 0; i < input.length-1; i++){
      int innerI = 0;
      while(input[i].get_overall_time() > input[innerI].get_overall_time()){
        temp = input[innerI];
        input[innerI] = input[i];
        input[i] = temp;
        innerI++;
      }
    }
    return input;
  }

}
