public class seqConvex{
  int n;
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
    seqMethod();
    // printPointsOnConvex();
    endTime = System.nanoTime();
    return endTime-startTime;
  }
  public void initate_variables(){
    koHyll = new IntList();
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

  public customInfo PAbove_PFurthest(int p1, int p2){
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
    IntList p_above = new IntList((int) n/2);
    for(int index = 0; index < x.length; index++){
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
        }
      }
    }
    return new customInfo(p_above,null, longest_relative_distance_index);
  }

  public void bestRec(int p1, int p2, int local_level, IntList p_to_Consider, customInfo ci_input){
    //System.out.format( "[%2d][Newrec on level [%2d] running from p1 : %d to p2 : %d.\n",id, level, p1, p2);
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
      //System.out.format("[%2d][%2d] - The line between p1 %d and p2 %d has no points above. Add points on the line and p2 %d as a point on convex.\n",id, level, p1, p2, p2);
      // addP(koHyll, p2);
      koHyll.add(p2);
      koHyll.addList_byDistanceP2(ci.p_on_line, x, y, p2);
      // addMultipleP(koHyll, pointOnLine);//Adding the points on the line.
    } else{
      //System.out.format("[%2d][%2d] - Found p3 %d. Continuing from p2 -> p3 and p3 -> p1.\n",id, level, p3);
      bestRec(p3, p2, local_level + 1, ci.p_to_Consider, null);
      bestRec(p1, p3, local_level + 1, ci.p_to_Consider, null);
    }
  }

  public void seqMethod(){
    // Worker min_max_w = new Worker(0, MIN_X_index, MAX_X_index, 0, true, true, null);
    customInfo p1p2 = PAbove_PFurthest(MIN_X_index, MAX_X_index);
    customInfo p2p1 = PAbove_PFurthest(MAX_X_index, MIN_X_index);
    bestRec(MIN_X_index, MAX_X_index, 0, p1p2.p_to_Consider, p1p2);
    bestRec(MAX_X_index, MIN_X_index, 0, p2p1.p_to_Consider, p2p1);
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


  public boolean correct_input(String[] args){
    try{
      n = Integer.parseInt(args[0]);
      return true;
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
  public void printPointsOnConvex(){
    for(int i = 0; i < koHyll.len; i++){
      System.out.format("[%3d] - (%d, %d)\n",koHyll.get(i), x[koHyll.get(i)], y[koHyll.get(i)]);
    }
  }
  //Main calls go()
  public static void main(String[] args) {
    seqConvex sc = new seqConvex();
    sc.go(args);
    if(args.length == 2){
      TegnUt tu = new TegnUt (sc, sc.koHyll);
    }
  }
}
