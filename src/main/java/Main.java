import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static String fromCurrency;
    private static String toCurrency;
    private static File cache = new File("rates_cache.txt");
    private static String rawRate;
    static Boolean loading = false;

    public static void main(String[] args) throws Exception {

        getUserInput();
        if (!cache.exists()) loadRatesInBackground();
        else validateCache();

    }

    private static ApiResponse getPojo(String rawRate) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(RateObject.class, new RatesDeserializer())
                .create();

        return gson.fromJson(rawRate, ApiResponse.class);

    }

    private static void getUserInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter from currency");
        fromCurrency = reader.readLine();
        System.out.println("Enter to currency");
        toCurrency = reader.readLine();
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
                    break;
                } else continue;
            }

            if (!isCacheValid) getRates();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }
    }


    private static void loadRatesInBackground() {

//        ExecutorService e = Executors.newSingleThreadExecutor();
//        Future f = e.submit(new Runnable(){
//            public void run(){
//                while(!Thread.currentThread().isInterrupted()){
//                    try {
//                        Thread.sleep(1000); //exclude try/catch for brevity
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                    System.out.print(".");
//                }
//            }
//        });
//        //do excel work

        // SETUP
        Runnable notifier = new Runnable() {
            public void run() {
                System.out.print(".");
            }
        };

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


        // IN YOUR WORK THREAD
        scheduler.scheduleAtFixedRate(notifier, 1, 1, TimeUnit.SECONDS);
        getRates();

        // DO YOUR WORK
        scheduler.shutdownNow();


        Thread httpThread = new Thread(() -> {
            loading = true;
            getRates();
        });
        httpThread.start();


//        f.cancel(true);
//        e.shutdownNow();

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
