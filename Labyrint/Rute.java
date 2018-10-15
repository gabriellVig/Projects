import java.util.ArrayList;
public abstract class Rute {
	//x pos, y pos
	protected Labyrint labyrint;
	public int x, y;
	//Naborutene
	public Rute nord, sor, vest, ost;
	//For aa ikke gaa paa samme rute flere ganger
	public boolean gaatHer = false;
	//Konsturktor
	public Rute(int x, int y) {
		this.y = y;
		this.x = x;
	}
	public void setLab(Labyrint l){
		this.labyrint = l;
	}

	public Labyrint getLab(){
		return labyrint;
	}
//tilTegn
	public abstract char tilTegn();

	//ToString
	@Override
	public String toString(){
		//+1 siden dere vil telle fra 1 og oppover, ikke 0
		int tmpX= x+1;
		int tmpY = y+1;
		return ("(" + tmpX + ", " + tmpY + ")");
		//(x, y)
	}

	public void gaa(char retning, String vei){
		//Hvis det er en aapning saa lagre hele stien siden det er en utvei
		if(this instanceof Aapning){
			labyrint.leggTilUtvei(vei);
			return; //for aa stoppe.
		}
		//Hvis den ikke har gaat her saa
		if(!gaatHer){
			//Passer paa aa ikke gaa tilbake. Sjekker om naboen er en HvitRute/Aapning
			if(retning != 'n' && nord instanceof HvitRute) {
				gaatHer = true;
				nord.gaa('s', vei +"--->"+ this.toString());
			}
			if(retning != 's' && sor instanceof HvitRute) {
				gaatHer = true;
				sor.gaa('n', vei +"--->"+ this.toString());
			}
			if(retning != 'v' && vest instanceof HvitRute) {
				gaatHer = true;
				vest.gaa('o', vei +"--->"+ this.toString());
			}
			if(retning != 'o' && ost instanceof HvitRute) {
				gaatHer = true;
				//System.out.println("ost" + this.toString());
				ost.gaa('v', vei +"--->"+ this.toString());
			}
		}
	}
	//Finnutvei bare kjorer gaa paa objektet kallet kom til.

	public void finnUtvei(){
		this.gaa('a', "START");
	}
}
