package org.eyalgenis;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NameGenerator {

    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String GET_URL = "https://www.fakenamegenerator.com/gen-random-us-us.php";
    private static HashMap<String, Integer> nameCounter = new HashMap<>();
    private static int requests = 100;

    public static void main(String[] args) throws InterruptedException {

        long start = System.currentTimeMillis();
        System.out.println("start time: " + start);

        CountDownLatch latch = new CountDownLatch(requests);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for(int i=0; i<requests; i++) {
            Client c = new Client(GET_URL, i, latch, nameCounter);
            executorService.submit(c);
        }

        latch.await(80, TimeUnit.SECONDS);
        executorService.shutdown();

        LinkedHashMap<String, Integer> sorted = sortHashMap();

        List<String> mapKeys = new ArrayList<>(sorted.keySet());
        Iterator<String> keyIt = mapKeys.iterator();
        int i=0;
        while (keyIt.hasNext() && i<10) {
            String key = keyIt.next();
            System.out.println(key + " " + sorted.get(key));
            i++;
        }

        long end = System.currentTimeMillis();
        System.out.println("\nend time: " + end);

        float diff = ((float)(end - start)) / 1000; // seconds
        System.out.println("total run time: " + diff + " seconds");
    }

    private static LinkedHashMap<String, Integer> sortHashMap() {

        List<String> mapKeys = new ArrayList<>(nameCounter.keySet());
        List<Integer> mapValues = new ArrayList<>(nameCounter.values());
        Collections.sort(mapValues);
        Collections.reverse(mapValues);
        Collections.sort(mapKeys);

        LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

        for (Integer val : mapValues) {
            Iterator<String> keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                String key = keyIt.next();
                Integer comp1 = nameCounter.get(key);

                if (comp1.equals(val)) {
                    keyIt.remove();
                    sortedMap.put(key, val);
                    break;
                }
            }
        }
        return sortedMap;
    }
}
