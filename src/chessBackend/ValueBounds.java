package chessBackend;

public enum ValueBounds {
	EXACT, ATMOST, ATLEAST, NA;

	public ValueBounds opposite() {

		if (this == EXACT) {
			return ATMOST;
		} else {
			if (this == ATLEAST) {
				return ATMOST;
			} else {
				if (this == ATMOST) {
					return ATLEAST;
				} else {
					return this;
				}
			}
		}
	}
}
