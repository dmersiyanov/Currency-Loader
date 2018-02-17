import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputHelper {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private String[] curencies = {
            "USD", "JPY", "BGN", "CZK", "DKK",
            "GBP", "HUF", "PLN", "RON", "SEK",
            "CHF", "ISK", "NOK", "HRK", "RUB",
            "TRY", "AUD", "BRL", "CAD", "CNY",
            "HKD", "IDR", "ILS", "INR", "KRW",
            "MXN", "MYR", "NZD", "PHP", "SGD",
            "THB", "ZAR"};

    public  String getUserInput() throws IOException {
        String input;
        do {
            input = reader.readLine();
            System.out.println(input);
        } while (!isInputValid(input));
        return input;
    }

    private  boolean isInputValid(String currency) {
        for (String s : curencies) {
            if(currency.equals(s)) {
                return true;
            }
        }
        System.out.println("Указанная валюта не поддерживается, попробуйте еще раз");
        return false;
    }

    public  void closeReader() throws IOException {
        reader.close();
    }
}
