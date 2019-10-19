import java.net.*;
import java.io.*;

public class URLReader {
    public static  void main(String[] args) throws Exception {
        String site;
        if (args.length == 0)
            site = "https://ru.wikipedia.org/wiki/Java";
        else
            site = args[0];
        String thumbnailSketch = GetThumbnailSketch(site);
        ProcessText(thumbnailSketch);
    }

    public static String GetThumbnailSketch(String site) throws Exception{
        URL website = new URL(site);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(website.openStream()));

        String inputLine;
        StringBuilder text = new StringBuilder("");
        Boolean fl = false;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains("<div")) {
                fl = false;
            }
            if (fl) {
                text.append(inputLine);
            }
            if (inputLine.contains("<p><b>")) {
                text.append(inputLine);
                fl = true;
            }
        }
        System.out.println(text);
        System.out.println("");
        in.close();
        return String.valueOf(text);
    } // необработанный абзац с javascript конструкцией

    public static String ProcessText(String text){
        if (text.length() == 0)
            return "";
        StringBuilder newText = new StringBuilder("");
        Boolean flSkip = false;
        Boolean flJ = false; //флаг на символ &(случай &#91; = '[')
        int flB = 0; //флаг на [], когда включен фл flJ
        //Boolean flE = true;

        for (int i = 0; i < text.length(); i++) {
            var textArray = text.toCharArray();
            if (textArray[i] == '>') {
                flSkip = true;
                continue;
            }
            if (textArray[i] == '<') {
                flSkip = false;
                continue;
            }
            if ((textArray[i] == '&') && (textArray[i + 1] == '#')) {
                if (flJ){
                    i += 4;
                    flJ = false;
                    continue;
                }
                if (textArray[i + 3] == ';') {
                    i += 3;
                    continue;
                }
                if (textArray[i + 4] == ';') {
                    i += 4;
                    flJ = true;
                    continue;
                }
                if (textArray[i + 5] == ';') {
                    i += 5;
                    continue;
                }
                continue;
            }
            if (flJ)
                continue;
            if (flSkip)
                newText.append(textArray[i]);
        }
        System.out.println(newText);
        return String.valueOf(newText);
    }
}
