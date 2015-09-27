package com.gordoncaleb.chess.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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

	@JsonCreator
	public Side fromString(String s){
		return Side.valueOf(s);
	}

	@JsonValue
	public String asString(){
		return toString();
	}
}
