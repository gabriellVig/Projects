class FullTabellUnntak extends RuntimeException {
    FullTabellUnntak(int storrelse) {
        super(String.format("Storrelse: %d", storrelse));
    }
}
