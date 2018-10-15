import java.util.Iterator;

public class Koe<T> extends SuperListe{

  /**
   * Setter inn et element i slutten av listen.
   * @param   element     elementet som settes inn
   */
  @Override
  public void settInn(Object element)
  {
	  Node newNode = new Node(element);
    if(forste == null){
      forste = newNode;
      siste = newNode;
    } else
    {
      siste.neste = newNode;
      siste = newNode;
    }
  }
}
