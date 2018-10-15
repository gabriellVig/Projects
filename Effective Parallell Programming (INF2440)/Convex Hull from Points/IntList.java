import java.util.concurrent.locks.ReentrantLock;
class IntList {
   int [] data;
   int len = 0;

   IntList(int len) {
      data = new int[Math.max(2,len)];
   }

   IntList() {
      data = new int[16];  // some default size
   }
   IntList(int[] info){
     data = info;
     len = info.length;
   }

   void add(int elem) {
      if (len == data.length) { // dynamically expand array
         int [] b = new int [data.length*2];
         System.arraycopy(data,0, b,0,len);
         //for (int i = 0; i < len; i++) b[i] = data[i];
         data = b;
      }
      data[len++] = elem;
   } // end add

  boolean hasVal(int val){
    for(int i = 0; i < len;i++){
      if(data[i] == val){
        return true;
      }
    }
    return false;
  }
  // adds IntList other to this IntList as last part
  void append (IntList other) {
      if ( len + other.len > data.length) {
         int newLen = Math.max(2*len,len + 2*other.len);
         int [] b = new int [newLen];
         System.arraycopy(data,0,b,0,len);
         data = b;
      }
      System.arraycopy(other.data,0, data, len, other.len);
      len += other.len;
   } // end join other Intlist to this IntList

   void clear(){
      len =0;
   } // end clear;

   int get (int pos){
      if (pos > len-1 ) return -1; else return data [pos];
   } //end get

   int size() {
      return len;
   } //end size

  ReentrantLock listLock = new ReentrantLock();
  void sync_add(int elem) {
    listLock.lock();
    try{
      if (len == data.length) { // dynamically expand array
       int [] b = new int [data.length*2];
       System.arraycopy(data,0, b,0,len);
       //for (int i = 0; i < len; i++) b[i] = data[i];
       data = b;
      }
      if(elem == 0){
        System.out.format("###Added element = 0\n");
      }
      data[len++] = elem;
    } finally{
      listLock.unlock();
    }
  }
  void sync_addList(IntList elem) {
     listLock.lock();
     try{
      if (len + elem.len > data.length) { // dynamically expand array
         int [] b = new int [data.length*2 + elem.len];
         System.arraycopy(data,0, b,0,len);
         data = b;
      }
      int temp_len = 0;
      for(int i = 0; i < elem.len; i++){
        data[len+i] = elem.get(i);
        temp_len++;
      }
      len += temp_len;
    } finally{
      listLock.unlock();
    }
  }
  void addList(IntList elem) {
     listLock.lock();
     if (len + elem.len > data.length) { // dynamically expand array
       int [] b = new int [data.length*2 + elem.len];
       System.arraycopy(data,0, b,0,len);
       data = b;
     }
     int temp_len = 0;
     // for(int i = 0; i < elem.len; i++){
     for(int i = elem.len-1; i >= 0; i--){
       data[len+i] = elem.get(i);
       temp_len++;
     }
     len += temp_len;
  }

  //Distance C from p1 to p2 -> C = sqrt((x1-x2)^2 + (y1-y2)^2)
  //Simplified distance -> C = abs(x1-x2) + abs(y1-y2)
  void addList_byDistanceP2(IntList elem, int[] x, int[]y, int p2){
    int p2_x = x[p2], p2_y = y[p2];
    IntList distances = new IntList(elem.len);
    int temp_P;
    for(int i = 0; i < elem.len; i++){
      temp_P = elem.get(i);
      distances.add(Math.abs(p2_x - x[temp_P]) + Math.abs(p2_y - y[temp_P]));
    }

    for(int i = 0; i < distances.len; i++){//For each number a
      int d_temp, e_temp;
      for(int ii = 0; ii < distances.len; ii++){//for each number b
        if(distances.data[i] > distances.data[ii]){//check if a is bigger than b
          d_temp = distances.data[ii];//if it is, switch
          e_temp = elem.data[ii];//in both IntLists

          distances.data[ii] = distances.data[i];
          elem.data[ii] = elem.data[i];

          distances.data[i] = d_temp;
          elem.data[i] = e_temp;

        }
      }
    }
    if (len + elem.len > data.length) { // dynamically expand array
       int [] b = new int [data.length + elem.len + 1];
       System.arraycopy(data,0, b,0,len);
       //for (int i = 0; i < len; i++) b[i] = data[i];
       data = b;
    }
    int counter = 0;
    int min_val = len - 1;
    for(int i = len + elem.len - 1; i > min_val; i--){
      data[i] = elem.get(counter++);
      len++;
    }

  }
  void add_sortedByArray(int elem, int[] array, boolean min_max){
    len++;
    if (len == data.length) { // dynamically expand array
      int [] b = new int [data.length*2];
      System.arraycopy(data,0, b,0,len);
      data = b;
    }
    int x_val = array[elem];
    int i;
    if(min_max){
      for(i = 0; i < len-1; i++){//Find where the new point fits in
        if(x_val < array[data[i]]){
          break;
        }
      }
    } else{
      for(i = 0; i < len-1; i++){//Find where the new point fits in
        if(x_val > array[data[i]]){
          break;
        }
      }

    }
    int tempval = data[i];
    data[i++] = elem;//increasing index, starts at val over the newly added.
    for(i = i; i < len; i++){//In case the new value is the biggest, len + 1
      data[i] = tempval;//replace the current value with the previous
      tempval = data[i + 1];//set tempvalue to be the next
    }
  }
} // end class IntList
