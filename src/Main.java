package web;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static List<String> listaStron; // kolekcja adresow URL do odwiedzenia
    private static List<String> listaSlowKluczowych; // kolekcja slow kluczowych
    private static final int count = 2; // parametr: ile slow kluczowych musi byc na stronie
    private static final String plikStron = "pages.txt"; // nazwa pliku z lista stron do odwiedzenia
    private static final String plikSlowKluczowych = "words.txt"; // nazwa pliku z lista slow kluczowych

    public static void main(String[] args) {
        czytajListeStron();
        czytajListeSlowKluczowych();

        for(String strona : listaStron) { // przechodzimy przez wszystkie strony z listy stron do odwiedzenia
            String HTMLstrony = czytajURL(strona);
            String head = dajNaglowek(HTMLstrony);
            String domena = dajDomene(strona);
            // sprawdzenie ile slow kluczowych jest na stronie
            int ile = 0;
            List<String> znalezione = new ArrayList<>();
            for(String word : listaSlowKluczowych)
                if (head.contains(word)) {
                    ile++;
                    znalezione.add(word);
                }
            if (ile < count) continue;
            // teraz sprawdzamy czy na stronie sa linki do innych stron
            String body = dajBody(HTMLstrony);
            List<String> listaLinkow = dajLinki(body, domena);
            if (listaLinkow.size() > 0) {
                System.out.println("Strona: " + strona);
                System.out.println("Domena: " + domena);
                System.out.print("Slowa: ");
                for(String w : znalezione)
                    System.out.print(w + ",");
                System.out.println();
                System.out.println("Znalezione linki:");
                for(String link : listaLinkow)
                    System.out.println(link);
                System.out.println("@@@========== **** =========@@@");
            }
        }
    }

    /**
     * Funkcja dajLinki wyszukuje linki na stronie z domena inna niz 'domena'
     * @param strona - przeszukiwana strona (kod HTML)
     * @param domena - domena przeszukiwanej strony
     * @return
     */
    private static List<String> dajLinki(String strona, String domena) {
        List<String> znalezioneLinki = new ArrayList<>();
        //  <a href="http://sport.interia.pl/pilka-nozna/reprezentacja-polski">
        boolean jest = true;
        do {
            int nr1 = strona.indexOf("a href"); // sprawdzamy wszystkie znaczniki <a href=...
            if (nr1 >=0) {
                nr1 = strona.indexOf("\"", nr1+2); // przesuwamy siÄ™ do pierwszego znaku cudzyslowu "
                int nr2 = strona.indexOf("\"", nr1 + 9); // sprawdzamy gdzie jest cudzyslow zamykajacy znacznik <a href=...
                String link = strona.substring(nr1+1, nr2);
                strona = strona.substring(nr2);
                nr1 = link.indexOf("http:");
                if (nr1 == -1)
                    nr1 = link.indexOf("https:");
                if (nr1 >=0) { // uwzgledniamy tylko linki posiadajace 'http' lub 'https'
                    String domenaLinku = dajDomene(link);
                    if (domenaLinku.equals(domena) == false)
                        if (znalezioneLinki.contains(link) == false) // zabezpieczamy sie przed wielokrotnym dodaniem tego samego linku
                            znalezioneLinki.add(link); // jesli domena linku jest inna niz domena storny, to dodajemy link do listy znalezionych
                }
            }
            else
                jest = false;

        } while (jest);
        return znalezioneLinki;
    }
    /**
     * Funkcja dajDomene zwraca domene wycieta z adresu url strony
     */
    private static String dajDomene(String strona) {
        String domena = strona;
        if (strona.contains(":/")) {
            int nr = strona.indexOf(":/");
            domena = domena.substring(nr + 3);
        }
        if (domena.contains("/")) {
            int nr = domena.indexOf("/");
            domena = domena.substring(0, nr);
        }
        return domena;
    }
    /**
     * Funkcja czytajListeStron wczytuje z pliku strony WWW, ktore maja byc sprawdzone
     */
    private static void czytajListeStron() {
        listaStron = new ArrayList<>();
        try {
            FileReader fr = new FileReader(plikStron);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                listaStron.add(line);
            }
            br.close();
            fr.close();
        }
        catch(FileNotFoundException fe) {
            System.out.println(fe.getMessage());
        }
        catch(IOException ie) {
            System.out.println(ie.getMessage());
        }
    }

    /**
     * Funkcja readWords wczytuje z pliku slowa kluczowe, ktore beda wyszukiwane na stronach WWW
     */
    private static void czytajListeSlowKluczowych() {
        listaSlowKluczowych = new ArrayList<>();
        File file;
        try {
            FileReader fr = new FileReader(plikSlowKluczowych);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                listaSlowKluczowych.add(line.toLowerCase());
            }
            br.close();
            fr.close();
        }
        catch(FileNotFoundException fe) {
            System.out.println(fe.getMessage());
        }
        catch(IOException ie) {
            System.out.println(ie.getMessage());
        }
    }

    /**
     * Funkcja czytajjURL wczytuje kod HTML odwiedzanej strony (czytamy bezposrednio z adresu URL)
     */
    private static String czytajURL(String plik) {
        String strona = "";
        StringBuilder sb = new StringBuilder();
        try {
            URL test = new URL(plik);
            URLConnection uc = test.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/4.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(uc
                    .getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        }
        catch(MalformedURLException me) {
            System.out.println(me.getMessage());
        }
        catch(IOException ie) {
            System.out.println(ie.getMessage());
        }
        strona = sb.toString();
        return strona;
    }

    /**
     * Funkcja dajNaglowek pobiera zawartosc znacznika <HEAD></HEAD> dla danej strony
     * @param strona - strona, ktorej zawartosc bedzie wycinana
     * @return
     */
    private static String dajNaglowek(String strona) {
        String head = "";
        strona = strona.toLowerCase();
        int nr1 = strona.indexOf("<head>");
        int nr2 = strona.indexOf("</head>");
        head = strona.substring(nr1+7, nr2-1);
        return head;
    }
    /**
     * Funkcja dajBody pobiera zawartosc znacznika <BODY></BODY> dla danej strony
     * @param strona - strona, ktorej zawartosc bedzie wycinana
     * @return
     */
    private static String dajBody(String strona) {
        String body = "";
        strona = strona.toLowerCase();
        int nr1 = strona.indexOf("<body>");
        if (nr1 == -1)
            nr1 = strona.indexOf("<body ");
        int nr2 = strona.indexOf("</body>");
        body = strona.substring(nr1 + 7, nr2);
        return body;
    }
}
