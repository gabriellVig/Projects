import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.BrokenBarrierException;
import java.util.Arrays;
public class pConvex{
  int n, k;
  int[] x, y;//How the points are stored.

  int MAX_X, MAX_Y;//Stores the max x/y values.
  int MAX_X_index, MAX_Y_index;//Stores the index of the points which have the max x/y values.
  int MIN_X, MIN_Y;//Stores the min x/y values.
  int MIN_X_index, MIN_Y_index;//Stores the points which have the min x/y values.
  IntList koHyll;
  public long go(String[] args){
    if(!correct_input(args)){
      return 0;
    }
    initate_variables_doNotTime();//Create array x and y and fill with points.
    long startTime, endTime;
    startTime = System.nanoTime();
    initate_variables();

    setMinMaxXY(x, y);
    splitPointsInto4Sectors();
    a();
    // para_bestMetode();
    try{
      b2.await();
    }catch(InterruptedException ie){}catch (BrokenBarrierException bbe){}
    endTime = System.nanoTime();
    return endTime-startTime;
  }

  startWorker[] startworkers;
  CyclicBarrier b1;
  //This class is run to see if the threads step on each other toes. It checks if the threads found exclusive points
  //It also checks if the threads found all the ponts.
  private class barrier1 implements Runnable{
    @Override
    public void run(){
      int totalsize = 0;
      for(int i = 0; i < startworkers.length; i++){//for each worker
        totalsize += startworkers[i].pointsInMySector.len;
        for(int ii = 0; ii < startworkers[i].pointsInMySector.len; ii++){//for each index
          for(int iii = 0; iii < startworkers.length; iii++){//for each worker
            if(i != iii){
              if(startworkers[iii].pointsInMySector.hasVal(startworkers[i].pointsInMySector.get(ii))){
                System.out.format("Worker %d has value %d (%d, %d) which worker %d also has.\n", iii, startworkers[i].pointsInMySector.get(ii), x[startworkers[i].pointsInMySector.get(ii)], y[startworkers[i].pointsInMySector.get(ii)], i);
              }
            }
          }
        }
      }
      if(totalsize != n){
        System.out.format("Totalsize: %d != %d. Some elements are lost. Method splitPointsInto4Sectors() is probably not working.\n", totalsize, n);
      }
    }
  }

  int[] possiblePointsOnConvex;
  IntList p_on_c;
  CyclicBarrier b2;
  private class barrier2 implements Runnable{
    @Override
    public void run(){
      possiblePointsOnConvex = concatAll(startworkers[0].pointsOnMyConvex, startworkers[1].pointsOnMyConvex, startworkers[2].pointsOnMyConvex, startworkers[3].pointsOnMyConvex);
      /*

      for(int i = 0; i < possiblePointsOnConvex.length; i++){
        System.out.println(possiblePointsOnConvex[i]);
      }
      */
      final_seqMethod();
      // printForGeoGebra();
      // printForGeoGebra2();
    }
  }

  public void final_seqMethod(){
    p_on_c = new IntList(possiblePointsOnConvex);
    customInfo p1p2 = final_P_On_Above_Furthest(MIN_X_index, MAX_X_index, p_on_c);
    customInfo p2p1 = final_P_On_Above_Furthest(MAX_X_index, MIN_X_index, p_on_c);
    final_bestRec(MIN_X_index, MAX_X_index, 0, p1p2.p_to_Consider, p1p2);
    final_bestRec(MAX_X_index, MIN_X_index, 0, p2p1.p_to_Consider, p2p1);
  }

  public customInfo final_P_On_Above_Furthest(int p1, int p2, IntList p_to_Consider){
    int longest_relative_distance = 0,
        longest_relative_distance_index = 0,
        temp_relative_distance = 0;
    int p1_x = x[p1],
        p1_y = y[p1],
        p2_x = x[p2],
        p2_y = y[p2];
    int ax, by;
    int a = p1_y - p2_y,
        b = p2_x - p1_x,
        c = (p2_y * p1_x) - (p1_y * p2_x);
    IntList pointsOnLine = new IntList();
    IntList p_above = new IntList();
    for(int i = 0; i < p_to_Consider.len; i++){//Iterate through the points to consider.
      int index = p_to_Consider.get(i);
      if(index != p1 && index != p2){//Check that the current point is not p1, p2
        ax = a * x[index];
        by = b * y[index];
        temp_relative_distance = ax + by + c;//Relative distance to line equation, without divison of sqrt(a^2 + b^2)
        if(0 < temp_relative_distance){
          p_above.add(index);
          if(longest_relative_distance < temp_relative_distance){//This point is the furthest away from the line
            longest_relative_distance = temp_relative_distance;//Set as the new point furthest away
            longest_relative_distance_index = index;//remember the index
          }
        } else if (temp_relative_distance == 0){
          pointsOnLine.add(index);
        }
      }
    }
    return new customInfo(p_above, pointsOnLine, longest_relative_distance_index);
  }

  public void final_bestRec(int p1, int p2, int local_level, IntList p_to_Consider, customInfo ci_input){
    // System.out.format( "[%2d][Newrec on local_level [%2d] running from p1 : %d to p2 : %d.\n",id, local_level, p1, p2);
    IntList pol = new IntList();
    customInfo ci;
    int p3;
    if(ci_input == null){
      ci = final_P_On_Above_Furthest(p1, p2, p_to_Consider);
    } else {
      ci = ci_input;
    }
    p3 = ci.longest_relative_distance_index;
    if(ci.p_to_Consider.len == 0){//there was no point above the line between p1 and p2.
      // System.out.format("[%2d][%2d] - The line between p1 %d and p2 %d has no points above. Add points on the line and p2 %d as a point on convex.\n",id, local_level, p1, p2, p2);
      // addP(koHyll, p2);
      koHyll.add(p2);
      // koHyll.addList(ci.p_on_line);
      koHyll.addList_byDistanceP2(ci.p_on_line, x, y, p2);
      // addMultipleP(koHyll, pointOnLine);//Adding the points on the line.
    } else{
      // System.out.format("[%2d][%2d] - Found p3 %d. Continuing from p2 -> p3 and p3 -> p1.\n",id, local_level, p3);
      final_bestRec(p3, p2, local_level + 1, ci.p_to_Consider, null);
      final_bestRec(p1, p3, local_level + 1, ci.p_to_Consider, null);
    }
  }


  public void a(){
    startworkers = new startWorker[4];
    for(int i = 0; i < 4; i++){
      startworkers[i] = new startWorker(i);
      Thread t = new Thread(startworkers[i]);
      t.start();
    }
  }

  /*Worker which iterates through the points and finds all the points in their range.*/
  private class startWorker implements Runnable{
    int id;
    private startWorker(int id){
      this.id = id;
    }
    IntList pointsOnMyConvex = new IntList();
    IntList pointsInMySector;
    int l_xStart, l_xEnd;
    int l_yStart, l_yEnd;
    int l_minX, l_maxX,
        l_minX_index, l_maxX_index,
        l_minY, l_maxY,
        l_minY_index, l_maxY_index;
    @Override
    public void run(){
      //Step 1. Find which points are in this threads sector.
      pointsInMySector = new IntList();

      l_xStart = x_start[id];
      l_xEnd = x_end[id];
      l_yStart = y_start[id];
      l_yEnd = y_end[id];

      l_minX = l_xEnd;
      l_maxX = l_xStart;
      l_minY = l_yEnd;
      l_maxY = l_yStart;

      for(int i = 0; i < x.length; i++){//Iterate through all points.
        int x_val = x[i];
        int y_val = y[i];
        if((x_val >= l_xStart && x_val < l_xEnd) && (y_val >= l_yStart && y_val < l_yEnd)){
          pointsInMySector.add(i);
          updateBiggestSmallest(i, x_val, y_val);
        }
      }

      /*
      //wait for this barrier to check if the threads manage to properly split all points between them.
      try{
        b1.await();
      }catch(InterruptedException ie){}catch (BrokenBarrierException bbe){}
      System.out.println(l_minX + " . " + l_minY + " . " + l_maxX  + " . " + l_maxY);//DEBUG
      */
      //Step 1 finished. Start finding the convex hull of the points currently found.
      seqMethod();
      try{
        b2.await();
      }catch(InterruptedException ie){}catch (BrokenBarrierException bbe){}
    }
    public void seqMethod(){
      customInfo p1p2 = P_On_Above_Furthest(l_minX_index, l_maxX_index, pointsInMySector);
      customInfo p2p1 = P_On_Above_Furthest(l_maxX_index, l_minX_index, pointsInMySector);
      bestRec(l_minX_index, l_maxX_index, 0, p1p2.p_to_Consider, p1p2);
      bestRec(l_maxX_index, l_minX_index, 0, p2p1.p_to_Consider, p2p1);
    }

    public customInfo P_On_Above_Furthest(int p1, int p2, IntList p_to_Consider){
      int longest_relative_distance = 0,
          longest_relative_distance_index = 0,
          temp_relative_distance = 0;
      int p1_x = x[p1],
          p1_y = y[p1],
          p2_x = x[p2],
          p2_y = y[p2];
      int ax, by;
      int a = p1_y - p2_y,
          b = p2_x - p1_x,
          c = (p2_y * p1_x) - (p1_y * p2_x);
      IntList pointsOnLine = new IntList();
      IntList p_above = new IntList();
      for(int i = 0; i < p_to_Consider.len; i++){//Iterate through the points to consider.
        int index = p_to_Consider.get(i);
        if(index != p1 && index != p2){//Check that the current point is not p1, p2
          ax = a * x[index];
          by = b * y[index];
          temp_relative_distance = ax + by + c;//Relative distance to line equation, without divison of sqrt(a^2 + b^2)
          if(0 < temp_relative_distance){
            p_above.add(index);
            if(longest_relative_distance < temp_relative_distance){//This point is the furthest away from the line
              longest_relative_distance = temp_relative_distance;//Set as the new point furthest away
              longest_relative_distance_index = index;//remember the index
            }
          } else if (temp_relative_distance == 0){
            pointsOnLine.add(index);
          }
        }
      }
      return new customInfo(p_above, pointsOnLine, longest_relative_distance_index);
    }

    public void bestRec(int p1, int p2, int local_level, IntList p_to_Consider, customInfo ci_input){
      // System.out.format( "[%2d][Newrec on local_level [%2d] running from p1 : %d to p2 : %d.\n",id, local_level, p1, p2);
      IntList pol = new IntList();
      customInfo ci;
      int p3;
      if(ci_input == null){
        ci = P_On_Above_Furthest(p1, p2, p_to_Consider);
      } else {
        ci = ci_input;
      }
      p3 = ci.longest_relative_distance_index;
      if(ci.p_to_Consider.len == 0){//there was no point above the line between p1 and p2.
        // System.out.format("[%2d][%2d] - The line between p1 %d and p2 %d has no points above. Add points on the line and p2 %d as a point on convex.\n",id, local_level, p1, p2, p2);
        // addP(koHyll, p2);
        pointsOnMyConvex.add(p2);
        // pointsOnMyConvex.addList(ci.p_on_line);
        pointsOnMyConvex.addList_byDistanceP2(ci.p_on_line, x, y, p2);
        // addMultipleP(koHyll, pointOnLine);//Adding the points on the line.
      } else{
        // System.out.format("[%2d][%2d] - Found p3 %d. Continuing from p2 -> p3 and p3 -> p1.\n",id, local_level, p3);
        bestRec(p3, p2, local_level + 1, ci.p_to_Consider, null);
        bestRec(p1, p3, local_level + 1, ci.p_to_Consider, null);
      }
    }

    private void updateBiggestSmallest(int index, int x_in, int y_in){
      if(x_in < l_minX){
        l_minX = x_in;
        l_minX_index = index;
      }
      if(x_in > l_maxX){
        l_maxX = x_in;
        l_maxX_index = index;
      }
      if(y_in < l_minY){
        l_minY = y_in;
        l_minY_index = index;
      }
      if(y_in > l_maxY){
        l_maxY = y_in;
        l_maxY_index = index;
      }
    }


  }
  private class customInfo{
    IntList p_to_Consider;
    IntList p_on_line;
    int longest_relative_distance_index;
    customInfo(IntList p_to_Consider,IntList p_on_line, int longest_relative_distance_index){
      this.p_to_Consider = p_to_Consider;
      this.p_on_line = p_on_line;
      this.longest_relative_distance_index = longest_relative_distance_index;
    }
  }

  public void initate_variables(){
    koHyll = new IntList();
    // b1 = new CyclicBarrier(4, new barrier1());//Enable to see if threads properly split up points.
    b2 = new CyclicBarrier(4 + 1, new barrier2());
  }

  public static void main(String[] args) {
    pConvex pc = new pConvex();
    pc.go(args);
    if(args.length == 3){
      TegnUtP tu = new TegnUtP (pc, pc.koHyll);
    }
  }
  int[] x_start;
  int[] x_end;
  int[] y_start;
  int[] y_end;
  public void splitPointsInto4Sectors(){
    int sectors = 4;//needs to be 1 - 2 - 4 - 16 - 256
    int sqrt_sectors = (int) Math.round(Math.sqrt(sectors));
    int x_delta, y_delta;
    int x_range, y_range;
    int x_leftOver, y_leftOver;
    x_start = new int[sectors];
    x_end = new int[sectors];
    y_start = new int[sectors];
    y_end = new int[sectors];

    x_delta = MAX_X - MIN_X;
    y_delta = MAX_Y - MIN_Y;

    x_range = x_delta / sqrt_sectors;
    y_range = y_delta / sqrt_sectors;

    x_leftOver = x_delta % sqrt_sectors;
    y_leftOver = y_delta % sqrt_sectors;

    for(int i = 0; i < sqrt_sectors; i++){
      x_start[i] = i * x_range + MIN_X;
      if(i == 1){
        x_end[i] = x_start[i] + x_range + x_leftOver + 1;//+ 1 because we go from and with num to num.
        /*If max x is 88, then this would make x_end 88, not 89 which is needed, therefore + 1*/
      } else {
        x_end[i] = x_start[i] + x_range;
      }

      y_start[i] = MIN_Y;
      y_end[i] = y_start[i] + y_range;
    }

    for(int i = 2; i < sqrt_sectors + 2; i++){
      x_start[i] = (i - 2) * x_range + MIN_X;
      if(i == 3){
        x_end[i] = x_start[i] + x_range + x_leftOver + 1;//+ 1 because we go from and with num to num.
        /*If max x is 88, then this would make x_end 88, not 89 which is needed, therefore + 1*/
      } else {
        x_end[i] = x_start[i] + x_range;
      }
      y_start[i] = MIN_Y + y_range;

      y_end[i] = y_start[i] + y_range + y_leftOver + 1;//+ 1 because we go from and with num to num.

    }
    //Debug. Run to see how the points are split
    /*
    for(int i = 0; i < x_start.length;i++){
      System.out.format("Thread %1d. X_Start: %4d. X_End: %4d. Y_Start: %4d. Y_End: %4d.\n", i, x_start[i], x_end[i], y_start[i], y_end[i]);

    }
    */

  }
  //Sets the MAX_X/Y & MIN_X/Y variables.
  public void setMinMaxXY(int[] x_in, int[] y_in){
    int[] min_max_X = find_min_max(x_in);
    MIN_X_index = min_max_X[0];
    MIN_X = min_max_X[1];
    MAX_X_index = min_max_X[2];
    MAX_X = min_max_X[3];

    int[] min_max_Y = find_min_max(y_in);
    MIN_Y_index = min_max_Y[0];
    MIN_Y = min_max_Y[1];
    MAX_Y_index = min_max_Y[2];
    MAX_Y = min_max_Y[3];
    // System.out.format("Y: Max %d. Min %d.\n", MAX_Y, MIN_Y);//DEBUG
    // System.out.format("X: Max %d. Min %d.\n", MAX_X, MIN_X);//DEBUG
  }
  //Returns the index of the minimum and maximum values in the array. output[0] = min, output[1] = max.
  public int[] find_min_max(int[] x_input){
    int current_min = x_input[0], current_max = x_input[0];
    int current_min_index = 0, current_max_index = 0;
    for(int i = 1; i < x_input.length; i++){
      if(x_input[i] < current_min){
        current_min = x_input[i];
        current_min_index = i;
      }
      if(x_input[i] > current_max){
        current_max = x_input[i];
        current_max_index = i;
      }
    }
    //System.out.format("Min index: %d val :%d. Max i: %d. val: %d.\n",current_min_index, current_min, current_max_index, current_max );
    // return new int[] {current_min, current_max};
    return new int[] {current_min_index, current_min, current_max_index, current_max};
  }

  public void printPointsOnConvex(){
    for(int i = 0; i < koHyll.len; i++){
      System.out.format("[%3d] - (%d, %d)\n",koHyll.get(i), x[koHyll.get(i)], y[koHyll.get(i)]);
    }
  }
  public boolean correct_input(String[] args){
    try{
      if(args.length >= 2){
        n = Integer.parseInt(args[0]);
        k = Integer.parseInt(args[1]);
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
  //Initates x array, y array, and fills them with values (points).
  public void initate_variables_doNotTime(){
     x = new int[n];
     y = new int[n];
     NPunkter17 p = new NPunkter17(n);
     p.fyllArrayer(x, y);
  }
  /*method taken from:
    https://stackoverflow.com/a/784842
    Edited by me to fit with Intlist
  */
  public int[] concatAll(IntList first, IntList... rest) {
    int totalLength = first.len;
    for (IntList array : rest) {
      totalLength += array.len;
    }
    int[] result = Arrays.copyOf(first.data, totalLength);
    int offset = first.len;
    for (IntList array : rest) {
      System.arraycopy(array.data, 0, result, offset, array.len);
      offset += array.len;
    }
    return result;
  }
  public void printForGeoGebra(){
    System.out.println("FOR GEOGEBRA START:");
    System.out.println("All points:");
    String points = "points = {{";
    // System.out.format("points = {{");
    for(int i = 0; i < x.length; i++){
      points += ("(" + x[i] + ", " + y[i] + "),");
      // System.out.format("(%d, %d),", x[i], y[i]);
    }
    // System.out.format("}}");
    points = points.substring(0, points.length() - 1);
    points += "}}";
    System.out.println(points);
    System.out.println("Points on convex hull:");
    // System.out.format("onHull = {{");
    String onHull = "onHull = {{";
    for(int i = 0; i < possiblePointsOnConvex.length; i++){
      onHull += String.format("(%d, %d),", x[possiblePointsOnConvex[i]], y[possiblePointsOnConvex[i]]);
      // System.out.format("(%d, %d),", x[koHyll.get(i)], y[koHyll.get(i)]);
    }
    onHull = onHull.substring(0, onHull.length() - 1);
    // System.out.format("}}");
    onHull += "}}";
    System.out.println(onHull);
    System.out.println("FOR GEOGEBRA END");
  }
  public void printForGeoGebra2(){
    System.out.println("FOR GEOGEBRA START:");
    System.out.println("All points:");
    String points = "points = {{";
    // System.out.format("points = {{");
    for(int i = 0; i < x.length; i++){
      points += ("(" + x[i] + ", " + y[i] + "),");
      // System.out.format("(%d, %d),", x[i], y[i]);
    }
    // System.out.format("}}");
    points = points.substring(0, points.length() - 1);
    points += "}}";
    System.out.println(points);
    System.out.println("Points on convex hull:");
    // System.out.format("onHull = {{");
    String onHull = "onHull = {{";
    for(int i = 0; i < koHyll.len; i++){
      onHull += String.format("(%d, %d),", x[koHyll.get(i)], y[koHyll.get(i)]);
      // System.out.format("(%d, %d),", x[koHyll.get(i)], y[koHyll.get(i)]);
    }
    onHull = onHull.substring(0, onHull.length() - 1);
    // System.out.format("}}");
    onHull += "}}";
    System.out.println(onHull);
    System.out.println("FOR GEOGEBRA END");
  }

}
