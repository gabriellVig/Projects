public class HvitRute extends Rute {
	private char ledig = (' ');
	public HvitRute(int x, int y) {
		super(x, y);

	}

	@Override
	public char tilTegn() {
		boolean retur = labyrint.getKorteste().contains(this.toString());
		if (!retur) {
			return ledig;
		} else{
			return '-';
		}
	}
}
