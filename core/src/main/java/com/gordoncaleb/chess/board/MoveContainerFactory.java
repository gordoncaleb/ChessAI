package com.gordoncaleb.chess.board;

public class MoveContainerFactory {

    public static MoveContainer[] buildMoveContainers(int size) {
        MoveContainer[] containers = new MoveContainer[size];
        for (int i = 0; i < size; i++) {
            containers[i] = new MoveContainer();
        }
        return containers;
    }
}
