import java.util.Iterator;
import java.util.NoSuchElementException;

public class SuperListe<T> implements Liste<T> {
    //Erklaeringer
    protected int antallElementer = 0;
    protected Node forste = null, siste = null;

    //Node klassen
    protected class Node<T> {
  		Node<T> neste;
  		T data;
      //Node Konstruktor
  		public Node(T data) {
  			this.data = data;
  		}
  	}
    //Lenkeliste Iterator
    protected class StabelIterator implements Iterator{
      	Node denne = forste;
      	Node forrige = null;
        public Iterator iterator(){
      	  if(denne == null){
      		  throw new NoSuchElementException();
      	  } else {
      		  return new StabelIterator();
      	  }
        }

        public boolean hasNext(){
      	  return denne != null;
        }

        public T next(){
          if(this.hasNext()){
            forrige = denne;
            denne = denne.neste;
            return (T) forrige.data;
          } else {
            throw new NoSuchElementException();
          }
        }
      }
    //Iterator Metoden
    public Iterator iterator() {
      return new StabelIterator();
    }


    /**
     * Beregner antall elementer i listen
     * @return      antall elementer i listen
     */
    public int storrelse(){
      antallElementer = 0;
      if (forste == null) {
        return 0;
      } else {
        for (Node t = forste; t != null; t = t.neste) {
        		antallElementer++;
        }
        return antallElementer;
      }
    }

    /**
     * Sjekker om listen er tom
     * @return true om listen er tom
     */
    public boolean erTom(){
      if (forste == null) {
        return true;
      } else {
        return false;
      }
    }

    /**
    * Fjerner et element fra starten av listen
    *Hvis listen er tom returneres null.
    * @return      elementet
    */
    public T fjern(){
      Node t = forste;
      if(t != null){
        forste = t.neste;
        return (T) t.data;
      } else{
        return null;
      }
    }
    public void settInn(T t){

    }
    

}
