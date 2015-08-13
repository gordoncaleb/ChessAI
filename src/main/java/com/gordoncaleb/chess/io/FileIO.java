package com.gordoncaleb.chess.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

public class FileIO {
    private static final Logger logger = LoggerFactory.getLogger(FileIO.class);

    private static boolean logFileReady;
    private static boolean debugOutput = true;
    private static boolean useLogFile = true;

    /**
     * Fetch the entire contents of a text file, and return it in a String. This
     * style of implementation does not throw Exceptions to the caller.
     *
     * @param aFile is a file which already exists and can be read.
     */
    public static String readFile(String fileName) {

        URL fileURL = FileIO.class.getResource("doc/" + fileName);
        File aFile = null;

        if (fileURL == null) {
            try {
                aFile = new File(fileName);
                fileURL = aFile.toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }

        if (fileURL == null) {
            return null;
        } else {
            if (aFile != null) {
                if (!aFile.canRead()) {
                    return null;
                }
            }
        }

        // ...checks on aFile are elided
        StringBuilder contents = new StringBuilder();

        try {
            // use buffering, reading one line at a time
            // FileReader always assumes default encoding is OK!
            InputStreamReader is = new InputStreamReader(fileURL.openStream());
            BufferedReader input = new BufferedReader(is);
            try {
                String line = null; // not declared within while loop
                /*
				 * readLine is a bit quirky : it returns the content of a line
				 * MINUS the newline. it returns null only for the END of the
				 * stream. it returns an empty String if two newlines appear in
				 * a row.
				 */
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return contents.toString();
    }

    public static DataOutputStream getDataOutputStream(String fileName) {

        try {

            FileOutputStream fos = new FileOutputStream(fileName);
            DataOutputStream dout = new DataOutputStream(fos);

            return dout;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * Change the contents of text file in its entirety, overwriting any
     * existing text.
     * <p>
     * This style of implementation throws all exceptions to the caller.
     *
     * @param aFile is an existing file which can be written to.
     * @throws IllegalArgumentException if param does not comply.
     * @throws FileNotFoundException    if the file does not exist.
     * @throws IOException              if problem encountered during write.
     */
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

    public static void setLogEnabled(boolean enable) {
        useLogFile = enable;
    }

    public static void setDebugOutput(boolean enable) {
        debugOutput = enable;
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
