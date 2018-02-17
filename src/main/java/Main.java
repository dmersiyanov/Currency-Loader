import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {

    private static NetworkHelper networkHelper = new NetworkHelper();

    public static void main(String[] args) throws Exception {

        InputHelper inputHelper = new InputHelper();

        System.out.println("Введите первую валюту");
        String fromCurrency = inputHelper.getUserInput();
        System.out.println("Введите вторую валюту");
        String toCurrency = inputHelper.getUserInput();
        inputHelper.closeReader();

        networkHelper.showLoadingProgress(5);
        if(!validateCache(fromCurrency, toCurrency)) {
            networkHelper.loadRatesInBackground(fromCurrency, toCurrency);
        }
    }

    private static boolean validateCache(String from, String to) throws IOException {

        File cache = new File("rates_cache.txt");
        if (cache.exists()) {

            try (BufferedReader br = new BufferedReader(new FileReader(cache))) {

                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                String rawRate;

                while ((rawRate = br.readLine()) != null) {
                    ApiResponse apiResponse = networkHelper.getPojo(rawRate);
                    RateObject chachedRate = apiResponse.getRate();

                    if (from.equals(apiResponse.getBase()) & to.equals(chachedRate.getName()) & apiResponse.getDate().equals(currentDate)) {
                        System.out.println("\n" + from + " => " + to + " : " + chachedRate.getRate() + " (данные из кэша)");
                        return true;
                    }
                }

            }
        }
        return false;
    }
}
