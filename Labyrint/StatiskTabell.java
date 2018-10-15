import java.util.Iterator;
import java.util.NoSuchElementException;

public class StatiskTabell<T> implements Tabell {
  //Deklarasjoner
  protected int arrayLengde;
  protected T[] tabell;

  //Konstruktor
  public StatiskTabell(int arrayLengde)
  {
    this.arrayLengde = arrayLengde;
    this.tabell = (T[]) new Object[arrayLengde];
  }

  //Metode som henter antall objekter i tabellen som != null
  public int storrelse()
  {
    int temp = 0;
    for (T item : this.tabell)
    {
      if (item != null)
      {
        temp++;
      }
    }
    return temp;
  }

  //Metode som sier om tabellen er tom eller ikke.
  public boolean erTom()
  {
    return storrelse() == 0;
  }

  public T hentFraPlass(int plass)
  {
    try{
      return tabell[plass];
    } catch (Exception ArrayIndexOutOfBoundsException) {
      throw new UgyldigPlassUnntak(plass, tabell.length);
    }
  }


  @Override
  public void settInn(Object element)
  {
    for (int i = 0; i < this.arrayLengde; i++)
    {
      if (this.tabell[i] == null)
      {
        this.tabell[i] = (T) element;
        return;
      }
    }
    throw new FullTabellUnntak(arrayLengde);
  }

  //Iterator Metoden
  @Override
  public Iterator iterator()
  {
    StatiskIterator lok = new StatiskIterator();
    return lok;
  }

  //Iterator klassen
  public class StatiskIterator implements Iterator{
    public int indeks = -1;

    public boolean hasNext()
    {
      try
      {
        if(tabell[indeks+1] == null)
        {
          return false;
        } else
        {
          return true;
        }
      }catch (Exception ArrayIndexOutOfBoundsException)
      {
        return false;
      }
    }

    public T next()
    {
      if(hasNext())
      {
        indeks++;
        return (T) tabell[indeks];
      } else
      {
        throw new NoSuchElementException();
      }
    }
  }
}
