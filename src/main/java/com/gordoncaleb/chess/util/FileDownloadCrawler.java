package com.gordoncaleb.chess.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

public class FileDownloadCrawler {
    public static final Logger logger = LoggerFactory.getLogger(FileDownloadCrawler.class);

    public static void main(String[] args) {
        FileDownloadCrawler crawler = new FileDownloadCrawler();

        try {
            crawler.downloadFilesFromUri("http://www.pgnmentor.com/files.html#openings", "./pgns", "(.*\\.pgn$)|(.*\\.zip$)");
        } catch (IOException e) {
            e.printStackTrace();
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
            try {
                String fileName = fileName(l);
                logger.info("Downloading file: " + fileName);
                URL website = new URL(baseUri + "/" + l);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(Paths.get(outputDir, fileName).toFile());
                logger.info("Saving file: " + fileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                logger.info("File download " + fileName + " successful!");
                done++;
                logger.info("{} Completed", (done * 100 / total));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String baseUri(String uri) {
        return uri.substring(0, uri.lastIndexOf("/"));
    }

    private String fileName(String uri) {
        return uri.substring(uri.lastIndexOf("/")).replaceFirst("/", "");
    }
}
