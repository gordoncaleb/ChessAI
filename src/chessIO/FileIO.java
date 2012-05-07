package chessIO;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

public class FileIO {

	private static boolean logFileReady;
	private static boolean useLogFile = true;

	/**
	 * Fetch the entire contents of a text file, and return it in a String. This
	 * style of implementation does not throw Exceptions to the caller.
	 * 
	 * @param aFile
	 *            is a file which already exists and can be read.
	 */
	public static String readFile(String fileName) {

		URL fileURL = FileIO.class.getResource("doc/" + fileName);

		// ...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new InputStreamReader(fileURL.openStream()));
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

	/**
	 * Change the contents of text file in its entirety, overwriting any
	 * existing text.
	 * 
	 * This style of implementation throws all exceptions to the caller.
	 * 
	 * @param aFile
	 *            is an existing file which can be written to.
	 * @throws IllegalArgumentException
	 *             if param does not comply.
	 * @throws FileNotFoundException
	 *             if the file does not exist.
	 * @throws IOException
	 *             if problem encountered during write.
	 */
	public static void writeFile(String fileName, String aContents, boolean append) {

		URL fileURL = FileIO.class.getResource("doc/" + fileName);

		File aFile;
		try {
			aFile = new File(fileURL.toURI());
			Writer output = new BufferedWriter(new FileWriter(aFile, append));

			try {
				// FileWriter always assumes default encoding is OK!
				output.write(aContents);
			} finally {
				output.close();
			}

		} catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}

		// use buffering

	}

	private static void initLog() {

		if (useLogFile) {
			writeFile("log.txt", "Log File Start:\n", false);
			logFileReady = true;
		}

	}

	public static void setLogEnabled(boolean enable) {
		useLogFile = enable;
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

	public static void log(String msg) {

		if (useLogFile) {

			if (!logFileReady) {
				initLog();
			}

			writeFile("log.txt", msg + "\n", true);

		}

		System.out.println(msg);

	}

}
