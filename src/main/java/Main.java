import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static String fromCurrency;
    private static String toCurrency;
    private static File cache = new File("rates_cache.txt");
    private static String rawRate;

    public static void main(String[] args) throws Exception {

        getUserInput();

        if (!cache.exists()) {

            loadInBackground();

        } else {

            try (BufferedReader br = new BufferedReader(new FileReader(cache))) {
                while ((rawRate = br.readLine()) != null && !rawRate.isEmpty()) {

                    ApiResponse apiResponse = getPojo(rawRate);
                    RateObject rate = apiResponse.getRates();
                    String date = apiResponse.getDate();
                    String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                    if (fromCurrency.equals(apiResponse.getBase()) & toCurrency.equals(rate.getName()) & date.equals(currentDate)) {
                        System.out.println("\n" + fromCurrency + " => " + toCurrency + " : " + rate.getRate());
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }
        }
    }

    private static ApiResponse getPojo(String rawRate) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();

        return gson.fromJson(rawRate, ApiResponse.class);

        // git config --global user.email "dmersiyanov@yandex.ru" git config --global user.name "Dmitry Mersiyanov"

    }

    private static void getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter from currency");
        fromCurrency = reader.readLine();
        System.out.println("Enter to currency");
        toCurrency = reader.readLine();
        reader.close();
    }


    private static void loadInBackground() {
        Thread httpThread = new Thread(() -> {
            getRates();
        });
        httpThread.start();
    }

    private static void getRates() {

        try {
            String rawURL = "http://api.fixer.io/latest" + "?base=" + fromCurrency + "&symbols=" + toCurrency;
            URL url = new URL(rawURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            if ((rawRate = in.readLine()) != null) {

                getPojo(rawRate);

                try {

                    FileWriter fileWriter = new FileWriter(cache, true);
                    fileWriter.write(rawRate + "\r\n");
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            in.close();
            con.disconnect();
        } catch (Exception e) {
            System.out.println(e.toString());

        }
    }

}
