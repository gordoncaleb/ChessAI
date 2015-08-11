package com.gordoncaleb.chess.backend;

public enum Side {
	BLACK, WHITE, NONE, BOTH;

	public Side otherSide() {

		if (this == Side.WHITE) {
			return Side.BLACK;
		} else {
			if (this == Side.BLACK) {
				return Side.WHITE;
			} else {
				return this;
			}
		}
	}
}
