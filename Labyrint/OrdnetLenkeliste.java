import java.util.Iterator;

public class OrdnetLenkeliste<T extends Comparable<T>> extends SuperListe{

  /**
   * Setter inn et element i slutten av listen.
   * @param   element     elementet som settes inn
   */
  @Override
  public void settInn(Object element)
  {
	  Node newNode = new Node(element);
    if(forste == null)
    {
      forste = newNode;
      siste = newNode;
    } else
    {
      if(((Comparable<T>) newNode.data).compareTo((T) siste.data) > 0)
      {
        siste.neste = newNode;
        siste = newNode;
      } else if (((Comparable<T>) newNode.data).compareTo((T) forste.data) < 0)
      {
        newNode.neste = (Node) forste;
        forste = newNode;
      } else
      {
        for (Node t = forste; t!= null ; t = t.neste )
        {
          if (((Comparable<T>) newNode.data).compareTo((T) t.neste.data) < 0) {
            newNode.neste = t.neste;
            t.neste = newNode;
            return;
          }
        }
      }
    }
  }
}
