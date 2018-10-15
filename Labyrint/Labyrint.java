import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Iterator;

public class Labyrint {
	public Rute[][] tester;
	boolean utSkrift = true;
	int antallX;
	int antallY;
	public OrdnetLenkeliste<String> Veier = new OrdnetLenkeliste<>();
	public String korteste = "";

	private Labyrint(int antallX, int antallY, Rute[][] array) {
		this.tester = array;
		this.antallX = antallX;
		this.antallY = antallY;
	}
	private Labyrint() {

	}
	public void LeggTilKorteste(String b){
		korteste = b;
	}
	public String getKorteste(){
		return korteste;
	}

	//metode for a sette rute i arrayet
	private static boolean setRute(int xPos, int yPos, Rute rute, Rute[][] array) {
		if(array[yPos][xPos] == null) {
			array[yPos][xPos] = rute;
			return true;
		} else {
			return false;
		}
	}

	public Rute hentRute(int xPos, int yPos) {
		return tester[yPos][xPos];
	}

	public static Labyrint lesFraFil(File fil) throws FileNotFoundException {
		//Scanner for fil
		Scanner filScanner = new Scanner(fil);
		//Bestemmer storrelse
		int tempAntallY = Integer.parseInt(filScanner.next());
		int tempAntallX = Integer.parseInt(filScanner.next());
		//Selve arrayet
		Rute[][] tempArray = new Rute[tempAntallY][tempAntallX];
		//Imens det er mer aa lese saa leser den inn alle ruter og erklarer + setter de inn
		while(filScanner.hasNext())
    {
			for (int y = 0; y < tempAntallY; y++)
      {
				String temp = filScanner.next();
				for (int x = 0; x < tempAntallX; x++)
        {
					if(temp.charAt(x) == '#')
          {
						setRute(x, y, new SortRute(x,y), tempArray);
					} else if (temp.charAt(x) == '.')
          {
						setRute(x, y, new HvitRute(x,y), tempArray);
					} else
          {
						System.out.println("Kjente ikke igjen tegnet fra filen.");
					}
				}
			}
		}

    //Begge for lokkene brukes for aa sette alle ruter i ytterkant til aapninger
    for (int y = 0; y < tempArray.length ;y++ ) {
      if(tempArray[y][0] instanceof HvitRute) {
        tempArray[y][0] = new Aapning(0, y);
      }
      if(tempArray[y][tempArray[y].length-1] instanceof HvitRute) {
        tempArray[y][tempArray[y].length-1] = new Aapning(tempArray[y].length-1, y);
      }
    }
    for(int x = 0; x < tempArray[0].length; x++) {
      if(tempArray[0][x] instanceof HvitRute) {
        tempArray[0][x] = new Aapning(x, 0);
      }
      if(tempArray[tempArray.length-1][x] instanceof HvitRute) {
        tempArray[tempArray.length-1][x] = new Aapning(x, tempArray.length-1);
      }
    }
		//Brukes for aa sette naboene til alle ruter.
		for (int y = 0; y < tempArray.length ;y++ ) {
			for (int x = 0; x < tempArray[0].length ;x++ ) {
					try{
							tempArray[y][x].nord = tempArray[y-1][x];
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
							tempArray[y][x].sor = tempArray[y+1][x];
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
							tempArray[y][x].vest = tempArray[y][x-1];
					} catch(Exception ArrayIndexOutOfBounds){}
					try{
							tempArray[y][x].ost = tempArray[y][x+1];
					} catch(Exception ArrayIndexOutOfBounds){}
			}
		}

		filScanner.close();
		//Returnerer labyriten etter innskriving
    return new Labyrint(tempAntallX, tempAntallY, tempArray);
  }


	public void setNaboer(){
		//For alle ruter i Rute[][]
		for (int y = 0; y < tester.length ;y++ ) {
			for (int x = 0; x < tester[0].length ;x++ ) {
				//Hvis det er en hvitRute eller en Aapning
				if(tester[y][x] instanceof HvitRute || tester[y][x] instanceof Aapning){
					//Saa prov aa sett naboene
					try{
						if (tester[y-1][x] instanceof HvitRute || tester[y-1][x] instanceof Aapning) {
							((HvitRute) tester[y][x]).nord = (HvitRute) tester[y-1][x];
						}
						//Fang opp hvis det kommer utenfor
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
						if (tester[y+1][x] instanceof HvitRute || tester[y+1][x] instanceof Aapning) {
							((HvitRute) tester[y][x]).sor = (HvitRute) tester[y+1][x];
						}
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
						if (tester[y][x-1] instanceof HvitRute || tester[y][x-1] instanceof Aapning) {
							((HvitRute) tester[y][x]).vest = (HvitRute) tester[y][x-1];
						}
					} catch(Exception ArrayIndexOutOfBounds){}
					try{
						if (tester[y][x+1] instanceof HvitRute || tester[y][x+1] instanceof Aapning) {
							((HvitRute) tester[y][x]).ost = (HvitRute) tester[y][x+1];
						}
					} catch(Exception ArrayIndexOutOfBounds){}
				}
			}
		}
	}
	public void setAlleNaboer(){
		for (int y = 0; y < tester.length ;y++ ) {
			for (int x = 0; x < tester[0].length ;x++ ) {
					try{
							tester[y][x].nord = tester[y-1][x];
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
							tester[y][x].sor = tester[y+1][x];
					} catch(Exception ArrayIndexOutOfBounds) {}
					try{
							tester[y][x].vest = tester[y][x-1];
					} catch(Exception ArrayIndexOutOfBounds){}
					try{
							tester[y][x].ost = tester[y][x+1];
					} catch(Exception ArrayIndexOutOfBounds){}

			}
		}
	}

	public String printLab() {
		for (int y = 0; y < this.antallY; y++) {
			for (int x = 0; x < this.antallX; x++) {
				System.out.print(this.tester[y][x].tilTegn());
			}
			System.out.print("\n");
		}
		return null;
	}
	public void printNaboer(){
		for (int y = 0; y < tester.length ;y++ ) {
			for (int x = 0; x < tester[0].length ;x++ ) {
				if(tester[y][x] instanceof HvitRute || tester[y][x] instanceof Aapning){
					String nord = "", sor = "", vest = "", ost = "";
					if(((HvitRute)tester[y][x]).nord != null){
						nord =((HvitRute) tester[y][x]).nord.toString();
					}
					if(((HvitRute)tester[y][x]).sor != null){
						sor = ((HvitRute) tester[y][x]).sor.toString();
					}
					if(((HvitRute)tester[y][x]).vest != null){
						vest = ((HvitRute) tester[y][x]).vest.toString();
					}
					if(((HvitRute)tester[y][x]).ost != null){
						ost = ((HvitRute) tester[y][x]).ost.toString();
					}
					System.out.format("Rute paa posisjon: %s \n-Nord: %s \n-Sor: %s \n-Vest: %s \n-Ost: %s \n",
																		tester[y][x].toString(), nord, sor, vest, ost);
				}
			}
		}
	}

	public OrdnetLenkeliste<String> finnUtveiFra(int kol, int rad){
		//Forst saa refererer vi hver rute til Labyriten
		for (int y = 0; y < tester.length ;y++ ) {
			for (int x = 0; x < tester[0].length ;x++ ) {
				tester[y][x].setLab(this);
			}
			
		}
		//Forst saa cleaner den Veier, i tilfelle en annen labyrint har blitt lost for.
		try{
			while(Veier.storrelse() != 0){
				Veier.fjern();
			}
			//Setter den korteste utveien til a vaere tom.
			korteste = "";
			//Hvis utSkrift er paa, sa skriver den ikke ut labyrinten for losning
			if(utSkrift){
				//Printer ut Labyrinten for losning
				for (int y = 0; y < this.antallY; y++) {
					for (int x = 0; x < this.antallX; x++) {
						System.out.print(this.tester[y][x].tilTegn());
					}
					System.out.print("\n");
				}
			}
			tester[kol-1][rad-1].finnUtvei();
			return Veier;
		} catch(Exception ArrayIndexOutOfBounds){
			System.out.println("Koordinatene er utenfor arrayet");
		}
		return null;
	}
	public void leggTilUtvei(String b){
		Veier.settInn(b);
	}
	public String kortesteUtveiFra(int kol, int rad){
		korteste = "";
		while(!(Veier.erTom())){
			Veier.fjern();
		}
		
		for (int y = 0; y < tester.length ;y++ ) {
			for (int x = 0; x < tester[0].length ;x++ ) {
				tester[y][x].setLab(this);
			}
		}
		try{
			tester[kol-1][rad-1].finnUtvei();
			//Bruker iterator for aa hente ut den forste Strengen i OrdnetLenkeliste
			@SuppressWarnings("unchecked")
			Iterator<String> itr = Veier.iterator();
			if(itr.hasNext()){
				korteste = itr.next();
				//printer ut en labyrint med sti om minimalutskrift ikke er false
				if(utSkrift){
					for (int y = 0; y < tester.length; y++) {
						for (int x = 0; x < tester[0].length; x++) {
							System.out.print(this.tester[y][x].tilTegn());
						}
						System.out.print("\n");
					}
					//for a kille
				}
				return korteste;
			}
		} catch(Exception ArrayIndexOutOfBounds){
			System.out.println("Koordinatene er utenfor arrayet");
		}
		return null;
	}
	public void settMinimalUtskrift(){
		utSkrift = false;
	}
	
	public String kortesteUtveiFraGUI(String s){
		System.out.println("Strengen som ble sendt med: " + s);
		String[] tempAr = s.split(" . ");
		for(String a: tempAr){
			System.out.println(a);
		}
		int tY = Integer.parseInt(tempAr[0]);
		int tX = Integer.parseInt(tempAr[1]);
		return kortesteUtveiFra(tY+1, tX+1);
	}
}
