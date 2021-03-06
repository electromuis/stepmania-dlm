package com.electromuis.smdl.provider;

import com.electromuis.smdl.Pack;
import org.apache.http.HttpException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class HttpProvider extends DefaultProvider {
    private static final int BUFFER_SIZE = 4096;

    private Config config;

    public HttpProvider(String name, Config config) {
        super(name);
        this.config = config;
    }

    public List<Pack> getPacks() throws IOException {

        Document doc = Jsoup.connect(config.getEndpoint()).maxBodySize(0).get();
        //System.out.println(doc.html());
        //Document doc = Jsoup.parse(new File("C:\\Users\\Electromuis\\Downloads\\Stepmania Online.html"), "UTF-8");

        Elements packs = doc.select(config.getPattern());


        List<Pack> packsList = config.convertPacks(packs, this);

        return packsList;
    }
    @Override
    public InputStream getInputStream(Pack p) throws IOException {

        URL url = new URL(p.getUrl());

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();

        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();

            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = p.getUrl().substring(p.getUrl().lastIndexOf("/") + 1,
                        p.getUrl().length()).replace("%20", " ");
            }

            p.setContentLength(contentLength);

//            System.out.println("Content-Type = " + contentType);
//            System.out.println("Content-Disposition = " + disposition);
//            System.out.println("Content-Length = " + contentLength);
//            System.out.println("fileName = " + fileName);

            // opens input stream from the HTTP connection
            return httpConn.getInputStream();
        } else
            throw new IOException("No 200 OK");
    }

    public static abstract class Config {
        private String endpoint;
        private String pattern;

        public Config(String endpoint, String pattern) {
            this.endpoint = endpoint;
            this.pattern = pattern;
        }

        public abstract List<Pack> convertPacks(Elements e, HttpProvider provider);

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }
    }
}
