package org.eyalgenis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class Client implements Runnable {

    private int clientId;
    private final String USER_AGENT = "Mozilla/5.0";
    private String url;
    private final CountDownLatch latch;
    private final HashMap<String, Integer> nameCounter;

    public Client(String url, int clientId, CountDownLatch latch, HashMap<String, Integer> nameCounter) {
        this.clientId = clientId;
        this.url = url;
        this.latch = latch;
        this.nameCounter = nameCounter;
    }

    public void run() {

        try {
            sendGET();
        } catch (IOException e) {
            e.printStackTrace();
        }

        latch.countDown();
    }

    private void sendGET() throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String responseString = response.toString();
            parseAndCount(responseString);
        }
    }

    private void parseAndCount(String toParse) {
        String[] name = toParse.split("<h3>")[1].split("</h3>")[0].split("\\s+");
        synchronized (nameCounter) {
            if (name.length > 0) {
                String first = name[0];
                if (nameCounter.containsKey(first)) {
                    nameCounter.put(first, nameCounter.get(first) + 1);
                } else {
                    nameCounter.put(first, 1);
                }
            } else {
                return;
            }

            String last;
            if (name.length > 2) {
                last = name[2];
            } else {
                last = name[1];
            }

            if (nameCounter.containsKey(last)) {
                nameCounter.put(last, nameCounter.get(last) + 1);
            } else {
                nameCounter.put(last, 1);
            }
        }
    }
}
