package com.gordoncaleb.chess.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class FileDownloadCrawler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadCrawler.class);

    public static void main(String[] args) {
        FileDownloadCrawler crawler = new FileDownloadCrawler();

        try {
            crawler.downloadFilesFromUri("http://www.pgnmentor.com/files.html#openings", "./pgnmentor", "(.*\\.pgn$)|(.*\\.zip$)");
        } catch (IOException e) {
            LOGGER.error("Download error", e);
        }
    }

    public void downloadFilesFromUri(final String uri, final String outputDir, final String fileNameRegex) throws IOException {

        Document doc = Jsoup.connect(uri).get();
        Elements links = doc.select("a[href]");

        Set<String> files = links.stream()
                .map(l -> l.attr("href"))
                .filter(l -> l.matches(fileNameRegex))
                .collect(Collectors.toSet());

        final String baseUri = baseUri(doc.baseUri());

        int total = files.size();
        int done = 0;
        for (String l : files) {
            String fileName = fileName(l);
            LOGGER.info("Downloading file: " + fileName);
            URL website = new URL(baseUri + "/" + l);
            File file = Paths.get(outputDir, fileName).toFile();
            file.getParentFile().mkdirs();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                LOGGER.info("Saving file: " + fileName);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                LOGGER.info("File download " + fileName + " successful!");
                done++;
                LOGGER.info("{}% Completed", done * 100 / total);
            } catch (IOException e) {
                LOGGER.error("Error saving file {}", fileName, e);
            }
        }
    }

    private static String baseUri(String uri) {
        return uri.substring(0, uri.lastIndexOf("/"));
    }

    private static String fileName(String uri) {
        return uri.substring(uri.lastIndexOf("/")).replaceFirst("/", "");
    }
}
