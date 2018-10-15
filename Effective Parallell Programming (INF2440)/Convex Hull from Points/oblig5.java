import java.util.Arrays;

public class oblig5{

  public static void main(String[] args) {
    oblig5 app = new oblig5();
    if(app.correct_input(args)){
      app.program();
    }
  }
  public void program(){
    long para_times[] = new long[7];
    long seq_times[] = new long[7];
    for(int i = 0; i < para_times.length; i++){
      pConvex pc = new pConvex();
      String[] input = {Integer.toString(n), Integer.toString(k)};
      para_times[i] = pc.go(input);
    }
    for(int i = 0; i < seq_times.length; i++){
      seqConvex sc = new seqConvex();
      String[] input = {Integer.toString(n)};
      seq_times[i] = sc.go(input);
    }
    Arrays.sort(para_times);
    Arrays.sort(seq_times);
    double speedup;
    System.out.format("Run. Sequential Time. Parallell Time. Speedup.\n");
    for(int i = 0; i < para_times.length; i++){
      speedup = (double) seq_times[i]/para_times[i];
      System.out.format("[%1d]   %12dms. %12dms. %f\n",i, seq_times[i]/1000000, para_times[i]/1000000, speedup);
    }
    System.out.format("Median times:\n");
    speedup = (double) seq_times[3]/para_times[3];

    System.out.format("[%1d]   %12dms. %12dms. %f\n",3 ,seq_times[3]/1000000, para_times[3]/1000000, speedup);
    System.out.format("%d\n%d\n%d\n",n, seq_times[3], para_times[3]);
    if(print){
      seqConvex sc = new seqConvex();//for drawing convex
      String[] s_input = {Integer.toString(100)};
      sc.go(s_input);
      System.out.format("Sequential program found points on convex in this order:\n");
      sc.printPointsOnConvex();
      TegnUt s_tu = new TegnUt (sc, sc.koHyll);

      pConvex pc = new pConvex();//for drawing convex
      String[] p_input = {Integer.toString(100), Integer.toString(k)};
      pc.go(p_input);
      System.out.format("Parallell program found points on convex in this order:\n");

      pc.printPointsOnConvex();
      TegnUtP p_tu = new TegnUtP (pc, pc.koHyll);

    }
  }
  int n, k;
  boolean print = false;
  public boolean correct_input(String[] args){
    try{
      if(args.length >= 2){
        n = Integer.parseInt(args[0]);
        k = Integer.parseInt(args[1]);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        if(k > availableProcessors){
          System.out.format("%d processors available. old k = %d. New k = %d.\n",availableProcessors, k, availableProcessors );
        }
        if(k == 0){
          k = availableProcessors;
        }
        if(args.length == 3){
          print = true;
        } else {
          System.out.format("NOTE:\nPass an extra argument to print the convexhull of n = 100 for both versions.\nEND NOTE\n");
        }
        return true;
      } else {
        System.out.format("Illegal input. Correct input where n and k is a number:\njava para_convexHull <n> <k> <something-if-print>\n");
        return false;
      }
    } catch (NumberFormatException e){
      System.out.format("Illegal input. Correct input where n is a number:\njava seq_GrahamScan <n>\n");
      return false;
    }
  }
}
