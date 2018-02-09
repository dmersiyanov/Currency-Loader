import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
    private static boolean isLoading = false;

    public static void main(String[] args) throws Exception {

        getUserInput();
        if (!cache.exists()) {

            showLoadingProgress();
            loadRatesInBackground();
        } else {

            showLoadingProgress();
            validateCache();
        }

    }

    private static ApiResponse getPojo(String rawRate) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();

        ApiResponse apiResponse = null;
        try {
            apiResponse = gson.fromJson(rawRate, ApiResponse.class);
        } catch (JsonSyntaxException e) {
            isLoading = false;
            System.out.println("Ошибка форматирования файла rates_cache");
        }

        return apiResponse;

    }

    private static void getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter from currency");
        String firstCurrency = reader.readLine();

        if (isInputValid(firstCurrency)) {
            fromCurrency = firstCurrency;
        } else {
            System.out.println("Указанная валюта не поддерживается, попробуйте еще раз");
            System.exit(0);
        }

        System.out.println("Enter to currency");
        String secondCurrency = reader.readLine();

        if (isInputValid(secondCurrency)) {
            toCurrency = secondCurrency;
        } else {
            System.out.println("Указанная валюта не поддерживается, попробуйте еще раз");
            System.exit(0);
        }

        reader.close();
    }

    private static void validateCache() throws IOException {

        try (BufferedReader br = new BufferedReader(new FileReader(cache))) {

            RateObject chachedRate;
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            ApiResponse apiResponse;
            String date;
            Boolean isCacheValid = false;

            while ((rawRate = br.readLine()) != null) {

                apiResponse = getPojo(rawRate);
                chachedRate = apiResponse.getRate();
                date = apiResponse.getDate();

                if (fromCurrency.equals(apiResponse.getBase()) & toCurrency.equals(chachedRate.getName()) & date.equals(currentDate)) {
                    System.out.println("\n" + fromCurrency + " => " + toCurrency + " : " + chachedRate.getRate());
                    isCacheValid = true;
                    isLoading = false;
                    break;
                } else continue;
            }

            if (!isCacheValid) loadRatesInBackground();

        } catch (Exception e) {
            e.printStackTrace();
            isLoading = false;

        }
    }


    private static synchronized void showLoadingProgress() {
        Thread th = new Thread(() -> {
            try {
                isLoading = true;
                while (isLoading) {
                    System.out.write(".".getBytes());
                    Thread.sleep(30);
                }
            } catch (IOException e) {
                isLoading = false;
                e.printStackTrace();
            } catch (InterruptedException e) {
                isLoading = false;
                e.printStackTrace();
            }
        });
        th.start();
    }



    private static void loadRatesInBackground() {

        Thread httpThread = new Thread(() -> {
            getRates();
            isLoading = false;
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

                ApiResponse apiResponse = getPojo(rawRate);
                RateObject rate = apiResponse.getRate();
                System.out.println("\n" + fromCurrency + " => " + toCurrency + " : " + rate.getRate());

                try {

                    FileWriter fileWriter = new FileWriter(cache, true);
                    fileWriter.write(rawRate + "\r\n");
                    fileWriter.flush();
                    fileWriter.close();

                } catch (IOException e) {
                    isLoading = false;
                    e.printStackTrace();
                }
            }

            in.close();
            con.disconnect();
        } catch (Exception e) {
            isLoading = false;
            System.out.println(" Ошибка соединения с сервером");

        }
    }

    private static String[] curencies = {
            "USD",
            "JPY",
            "BGN",
            "CZK",
            "DKK",
            "GBP",
            "HUF",
            "PLN",
            "RON",
            "SEK",
            "CHF",
            "ISK",
            "NOK",
            "HRK",
            "RUB",
            "TRY",
            "AUD",
            "BRL",
            "CAD",
            "CNY",
            "HKD",
            "IDR",
            "ILS",
            "INR",
            "KRW",
            "MXN",
            "MYR",
            "NZD",
            "PHP",
            "SGD",
            "THB",
            "ZAR"};

    private static boolean isInputValid(String curency) {
        Boolean isCurrencyValid = false;
        for (String s : curencies) {
            if (curency.equals(s)) isCurrencyValid = true;
        }
        return isCurrencyValid;
    }
}
