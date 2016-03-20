package com.gordoncaleb.chess.util;


import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.serdes.PGNParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gordoncaleb.chess.board.serdes.PGNParser.PGNGame;

public class PGNGameLibrary {
    private static final Logger LOGGER = LoggerFactory.getLogger(PGNGameLibrary.class);

    private final Random random;
    private final List<File> pgnFilesInLib;

    public PGNGameLibrary(String libDirectory) {
        this.random = new Random();
        this.pgnFilesInLib = pgnFilesFromDir(libDirectory);
    }

    public Stream<Optional<PGNGame>> randomGames() {
        return Stream.generate(this::randomGame);
    }

    public Stream<Optional<Board>> randomBoards() {
        return randomGames().map(optGame -> optGame
                .flatMap(g -> {
                    try {
                        return Optional.of(PGNParser.getPGNGameAsBoard(g));
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                })
        );
    }

    public Stream<Optional<Board>> randomPositions() {
        return randomBoards().map(optBoard ->
                optBoard.map(b -> {
                    b.undo((int) (0.5 * b.getMoveNumber()));
                    return b;
                })
        );
    }

    private Optional<PGNGame> randomGame() {
        Optional<File> optFile = randomFile();
        return optFile.flatMap(file ->
                PGNParser.loadFile(file)
                        .map(games -> games.get(random.nextInt(games.size())))
        );
    }

    private List<File> pgnFilesFromDir(final String libDirectory) {
        Optional<File[]> files = Optional.ofNullable(
                Paths.get(libDirectory).toFile().listFiles()
        );

        return files.map(Stream::of)
                .orElse(Stream.empty())
                .filter(file -> file.getName().endsWith(".pgn"))
                .collect(Collectors.toList());
    }

    private Optional<File> randomFile() {
        if (!pgnFilesInLib.isEmpty()) {
            return Optional.ofNullable(
                    pgnFilesInLib.get(random.nextInt(pgnFilesInLib.size()))
            );
        } else {
            return Optional.empty();
        }
    }
}
