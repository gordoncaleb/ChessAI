package com.gordoncaleb.chess.board;

public class MoveContainerFactory {

    public static SimpleMoveContainer[] buildMoveContainers(int size) {
        SimpleMoveContainer[] containers = new SimpleMoveContainer[size];
        for (int i = 0; i < size; i++) {
            containers[i] = new SimpleMoveContainer();
        }
        return containers;
    }
}
