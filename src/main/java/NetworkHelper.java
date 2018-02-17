import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelper {

    private static void saveToCache(File cache, String rawRate) throws IOException {

        FileWriter fileWriter = new FileWriter(cache, true);
        fileWriter.write(rawRate + "\r\n");
        fileWriter.flush();
        fileWriter.close();

    }

    public ApiResponse getPojo(String rawRate) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();

        ApiResponse apiResponse = null;
        try {
            apiResponse = gson.fromJson(rawRate, ApiResponse.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка форматирования файла rates_cache");
        }

        return apiResponse;
    }

    public void showLoadingProgress(int dotsCount) {
        for(int i = 0; i < dotsCount; i++) {
            System.out.print(".");
        }
    }

    public void loadRatesInBackground(String from, String to) {
        Thread httpThread = new Thread(() -> {
            getRates(from, to);
        });
        httpThread.start();
    }

    private void getRates(String from, String to) {

        try {
            String rawURL = "http://api.fixer.io/latest" + "?base=" + from + "&symbols=" + to;
            URL url = new URL(rawURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String rawRate;
            if ((rawRate = in.readLine()) != null) {

                ApiResponse apiResponse = getPojo(rawRate);
                RateObject rate = apiResponse.getRate();
                System.out.println("\n" + from + " => " + to + " : " + rate.getRate());

                saveToCache(new File("rates_cache.txt"), rawRate);

            }

            in.close();
            con.disconnect();
        } catch (Exception e) {
            System.out.println("Ошибка соединения с сервером");

        }
    }
}
