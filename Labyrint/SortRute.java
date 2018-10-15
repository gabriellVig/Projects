public class SortRute extends Rute {
	private char lukket = ('#');

	public SortRute(int x, int y) {
		super(x, y);

	}

	@Override
	public char tilTegn() {
		return lukket;
	}
}
