public class seqRad{
  // viktig konstant
final static int NUM_BIT =7; // eller 6,8,9,10..

  int [] radixMulti(int [] a) {
    long tt = System.nanoTime();
    // 1-5 digit radixSort of : a[]
    int max = a[0],
        numBit = 2,
        numDigits,
        n = a.length;
    int [] bit ;
    // a) finn max verdi i a[]
    for (int i = 1 ; i < n ; i++){
      if (a[i] > max){
        max = a[i];
      }
    }

    while (max >= (1L << numBit)) {
      numBit++; // antall siffer i max
    }
    // bestem antall bit i numBits sifre
    numDigits = Math.max(1, numBit/NUM_BIT);
    bit = new int[numDigits];
    int rest = numBit%NUM_BIT,
        sum =0;
    // fordel bitene vi skal sortere paa jevnt
    for (int i = 0; i < bit.length; i++){
      bit[i] = numBit/numDigits;
       if(rest-- > 0){
         bit[i]++;
       }
    }

    int[] t = a,
          b = new int [n];
    for (int i =0; i < bit.length; i++) {
      radixSort(a, b, bit[i], sum);
      sum += bit[i];
      // swap arrays (pointers only)
      t = a;
      a = b;
      b = t;
    }
    if((bit.length & 1) != 0 ) {
      // et odde antall sifre, kopier innhold tilbake til original a[] (nå b)
      System.arraycopy (a,0,b,0,a.length);
    }
    long tid = (System.nanoTime() - tt);
    // System.out.format("\nSorterte %12d tall paa: %12d nanosek.\n",n, tid);
    testSort(a);
    return a;
  } // end radix2

  /** Sort a[] on one digit ; number of bits = maskLen, shiftet up 'shift' bits */
  void radixSort ( int [] a, int [] b, int maskLen, int shift){
    // i-te siffer fra a[] til b[]
    // System.out.println(" radixSort maskLen:"+maskLen+", shift :"+shift);
    int acumVal = 0, j,
        n = a.length;
    int mask = (1<<maskLen) -1;
    int [] count = new int [mask+1];
    // b) count=the frequency of each radix value in a
    for (int i = 0; i < n; i++) {
      count[(a[i]>>> shift) & mask]++;
    }
    /*
    System.out.format("\n#COUNTER BEFORE#\n");
    for (int i = 0; i < count.length; i++) {//Loop which prints how sumCount looks.
      System.out.format("%2d|", count[i]);
    }
    System.out.format("\n");
    */
    // c) Add up in 'count' - accumulated values, i.e pointers
    for (int i = 0; i <= mask; i++) {
      j = count[i];
      count[i] = acumVal;
      acumVal += j;
      // System.out.format("count[%3d] = %3d\n", i, count[i]);
    }
    /*
    System.out.format("\n#COUNTER AFTER#\n");
    for (int i = 0; i < count.length; i++) {//Print the values in count after acum.
      System.out.format("%2d|", count[i]);
    }
    System.out.format("\n");
    */

    // d) move numbers in sorted order a to b
    for (int i = 0; i < n; i++) {
      b[count[(a[i]>>>shift) & mask]++] = a[i];
      /*
      System.out.format("(b[count[(a[%d] >>> %d) & %s]++]\n", i, shift, Integer.toString(mask, 2));
      System.out.format("(b[count[%d & %s]++]\n",(a[i]>>>shift), Integer.toString(mask, 2));
      System.out.format("(b[count[%d]++]\n",((a[i]>>>shift) & mask)-1);
      System.out.format("(b[%d]\n", count[(a[i]>>>shift  ) & mask]);
      */
    }
  }// end radixSort
  void testSort(int [] a){
    for (int i = 0; i < a.length-1;i++) {
      if (a[i] > a[i+1]){
        System.out.println("SorteringsFEIL på plass: "+ i +" a["+i+"]:"+a[i]+" > a["+(i+1)+"]:"+a[i+1]);
        return;
      }
    }
  }// end simple sorteingstest
}
