import java.util.Random;
import java.util.Arrays;
public class oblig4{
  final int seed = 1234;

  int max_value = 0;
  int k;
  public static void main(String[] args) {
    if(args.length != 2){
      System.out.println("Incorrect usage. Correct:\njava oblig4 <n> <k>");
      return;
    }
    oblig4 thisprog = new oblig4();
    thisprog.start_oblig4(args);
  }

  public void start_oblig4(String[] args){
    int size;
    try{
      size = Integer.parseInt(args[0]);//Get N.
      k = Integer.parseInt(args[1]);
      if(k == 0 && size < Runtime.getRuntime().availableProcessors()){
        System.out.println("Size cannot be smaller than k or available Processors.");
        return;
      } else if(size < k){
        System.out.println("Size cannot be smaller than k or available Processors.");
        return;
      }
    } catch (NumberFormatException e){
      System.out.println("Incorrect usage. Correct:\njava oblig4 <n> <k>\nwhere <n> & <k> are numbers.");
      return;
    }
    max_value = size;// max value = N.

    seqRad sequential_program = new seqRad();
    // paraMaxFinder pMF = new paraMaxFinder(16, size);//16 = k, size = n.
    paraRad pr = new paraRad(k);//16 = k

    System.out.println("Running sequential version.");
    runSeq(sequential_program, create_random_array(size), true);//Check if it sorts itr succesfully
    long[] seq_times = new long[7];
    for (int i = 0; i < seq_times.length; i++) {//Run it 7 times
      seq_times[i] = runSeq(sequential_program, create_random_array(size), false);
      System.out.format("[S] - Run : %2d - Used time: %12dms\n",i, seq_times[i]/1000000);
    }

    System.out.println("Running Parallell version.");
    runPara(pr, create_random_array(size), true);
    long[] para_times = new long[7];
    for (int i = 0; i < para_times.length; i++) {//Run 7 times.
      para_times[i] = runPara(pr, create_random_array(size), false);
      System.out.format("[P] - Run : %2d - Used time: %12dms\n",i, para_times[i]/1000000);
    }
    Arrays.sort(seq_times);
    Arrays.sort(para_times);
    System.out.println(size);
    System.out.format("[S] - Median time: %12dms\n", seq_times[3]/1000000);
    // System.out.format("%f\n", (double) seq_times[3]/1000000);//When printing for report
    System.out.format("[P] - Median time: %12dms\n", para_times[3]/1000000);
    // System.out.format("%f\n", (double) para_times[3]/1000000);//When printing for report

    double speedup = (double) seq_times[3]/para_times[3];

    System.out.format("Speedup: %f\n", speedup);
    // System.out.format("%f\n", speedup);//When printing for report
  }

  public long runSeq(seqRad app, int[] a, boolean verify_integrity){
    int[] temp;
    long start_time, end_time;
    start_time = System.nanoTime();
    temp = app.radixMulti(a);
    end_time = System.nanoTime() - start_time;
    if(verify_integrity){
      testSort(temp);
    }
    return end_time;
  }

  public long runPara(paraRad app, int[] a, boolean verify_integrity){
    int[] temp;
    long start_time, end_time;
    start_time = System.nanoTime();
    temp = app.right_radix(a);//The actual call to start the program.
    end_time = System.nanoTime() - start_time;
    if(verify_integrity){
      testSort(temp);
    }
    return end_time;
  }

  int[] copy_array(int[] copy_this){
    int[] retur_array = new int[copy_this.length];
    for (int i = 0; i < copy_this.length; i++) {
      retur_array[i] = copy_this[i];
    }
    return retur_array;
  }

  int[] create_random_array(int size){
    Random r = new Random(seed);
    int[] output = new int[size];
    for(int i = 0; i < size; i++){
      if(max_value != 0){
        output[i] = r.nextInt(max_value);
      } else {
        output[i] = r.nextInt();
      }
    }
    return output;
  }

  void testSort(int [] a){
    for (int i = 0; i < a.length-1;i++) {
      if (a[i] > a[i+1]){
        System.out.println("SorteringsFEIL på plass: "+ i +" a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
        return;
      }
    }
  }// end simple sorteingstest
}
