import java.util.Iterator;

public class Stabel<T> extends SuperListe{
  /**
   * Setter inn et element i starten av listen og flytter alle
   *elementer en plass opp(mot slutten).
   * @param        elementet som settes inn
   */
  @Override
  public void settInn(Object element){
	  Node newNode = new Node(element);
    if(forste == null){
      forste = newNode;
    } else {
      newNode.neste = (Node) forste;
      forste = newNode;
    }
  }

}
