package com.gordoncaleb.chess.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class FileIO {
    private static final Logger logger = LoggerFactory.getLogger(FileIO.class);

    public static BufferedReader getResourceAsBufferedReader(String fileName) throws Exception {
        return Files.newBufferedReader(Paths.get(FileIO.class.getResource(fileName).toURI()));
    }

    public static String readResource(String fileName) {
        try {
            return Files
                    .lines(Paths.get(FileIO.class.getResource(fileName).toURI()))
                    .collect(Collectors
                            .joining(System.getProperty("line.separator")));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static DataOutputStream getDataOutputStream(String fileName) throws FileNotFoundException {
        return new DataOutputStream(new FileOutputStream(fileName));
    }

    public static void writeFile(String fileName, String aContents, boolean append) {

        URL fileURL = FileIO.class.getResource("doc/" + fileName);

        if (fileURL == null) {
            try {
                fileURL = (new File(fileName)).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        try {
            File aFile;

            if (fileURL == null) {
                aFile = new File(fileName);
            } else {
                aFile = new File(fileURL.toURI());
            }

            try (Writer output = new BufferedWriter(new FileWriter(aFile, append))) {
                output.write(aContents);
            }

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage readImage(String fileName) {
        URL fileURL = FileIO.class.getResource("img/" + fileName);

        BufferedImage image = null;

        try {
            image = ImageIO.read(fileURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    public static void clearDirectory(String dir) {
        File directory = new File(dir);

        // Get all files in directory

        File[] files = directory.listFiles();

        if (files.length > 0) {
            for (File file : files) {
                // Delete each file
                if (!file.delete()) {
                    // Failed to delete file
                    logger.debug("Failed to delete " + file);
                }
            }
        }
    }

}
