public class times_object{
  private long prime_time;
  private long factor_time;
  private long overall_time;

  public times_object(long prime_time, long factor_time){
    this.prime_time = prime_time;
    this.factor_time = factor_time;

  }
  public times_object(){

  }

  long get_prime_time(){
    return prime_time/1000000;
  }

  long get_factor_time(){
    return factor_time/1000000;
  }
  long get_overall_time(){
    return overall_time/1000000;
  }


  void set_prime_time(long new_prime_time){
    prime_time = new_prime_time;
  }
  void set_factor_time(long new_factor_time){
    factor_time = new_factor_time;
  }
  void set_overall_time(long new_overall_time){
    overall_time = new_overall_time;
  }
}
