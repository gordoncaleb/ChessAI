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

    private final String libDirectory;
    private final Random random;
    private final List<File> pgnFilesInLib;

    public PGNGameLibrary(String libDirectory) {
        this.libDirectory = libDirectory;
        this.random = new Random();
        this.pgnFilesInLib = pgnFilesFromDir(libDirectory);
    }

    public Stream<PGNGame> randomGames() {
        return Stream.generate(this::randomGame).flatMap(b -> Stream.of(b.get()));
    }

    public Stream<Board> randomBoards() {
        return randomGames().flatMap(g -> {
            try {
                return Stream.of(PGNParser.getPGNGameAsBoard(g));
            } catch (Exception e) {
                return Stream.empty();
            }
        });
    }

    public Stream<Board> randomPositions() {
        return randomBoards().map(b -> {
            b.undo((int) (0.5 * b.getMoveNumber()));
            return b;
        });
    }

    private Optional<PGNGame> randomGame() {
        File file = randomFile();
        try {
            List<PGNGame> games = PGNParser.loadFile(file.getCanonicalPath());
            return Optional.of(games.get(random.nextInt(games.size())));
        } catch (Exception e) {
            LOGGER.error("File: {} failed to load!", file.getName());
            LOGGER.error("Could not get board from file", e);
            return Optional.empty();
        }
    }

    private List<File> pgnFilesFromDir(final String libDirectory) {
        File[] files = Paths.get(libDirectory).toFile().listFiles();

        return Stream.of(files)
                .filter(file -> file.getName().endsWith(".pgn"))
                .collect(Collectors.toList());
    }

    private File randomFile() {
        return pgnFilesInLib.get(random.nextInt(pgnFilesInLib.size()));
    }
}
