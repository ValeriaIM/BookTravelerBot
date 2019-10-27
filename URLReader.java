import java.net.*;
import java.io.*;


public class URLReader {
    enum InfoAbout{
        Author,
        ThumbnailSketchBook,
        Books
    }

    public static void main(String[] args) throws Exception{
        //System.out.println(GetInfo("https://ru.wikipedia.org/wiki/Java", InfoAbout.Author));
        //System.out.println(GacetInfo("https://ru.wikipedia.org/wiki/Виноваты_звёзды_(роман)", InfoAbout.ThumbnailSketchBook));
        //System.out.println(GetThumbnailSketch("https://litlife.club/books/174926/read?page=7", "</div>", "<script src", "</script>", "<a id="));
    }

    public static String GetThumbnailSketch(String site, String preBegin, String begin, String preEnd, String end) throws Exception{
        URL website = new URL(site);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(website.openStream()));
        String inputLine;
        StringBuilder text = new StringBuilder("");
        Boolean fl = false;
        Boolean flEnd = false;
        Boolean flBegin = false;
        while ((inputLine = in.readLine()) != null) {
            if ((flEnd) && (inputLine.contains(end)))
                break;
            else
                flEnd = false;
            if ((inputLine.contains(preEnd)) && fl) {
                flEnd = true;
            }
            if (fl) {
                text.append(inputLine);
            }
            if (flBegin && inputLine.contains(begin)){
                text.append(inputLine);
                fl = true;
                flBegin = false;
                continue;
            }
            else
                flBegin = false;
            if (inputLine.contains(preBegin))
                flBegin = true;
        }
        in.close();
        return text.toString();
    } // необработанный абзац с javascript конструкцией

    public static String ProcessText(String text){
        if (text.length() == 0)
            return "";
        StringBuilder newText = new StringBuilder("");
        Boolean flSkip = false;
        Boolean flJ = false; //флаг на символ &(случай &#91; = '[')
        int flDiv = 0;
        var textArray = text.toCharArray();
        for (int i = 0; i < text.length(); i++) {
            if ((textArray[i] == ' ') && (textArray[i + 1] == '<') && (textArray[i + 2] == '/'))
                continue;
            if (textArray[i] == '>') {
                flSkip = false;
                continue;
            }
            if (textArray[i] == '<') {
                if ((textArray[i + 1] == '/') && (textArray[i + 2] == 'p'))
                    newText.append('\n');
                flSkip = true;
                continue;
            }
            if ((textArray[i] == 'd') && (textArray[i + 1] == 'i') && (textArray[i + 2] == 'v')) {
                if (textArray[i - 1] == '/')
                    flDiv--;
                else
                    flDiv++;
                i += 3;
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
                    newText.append(' ');
                    i += 5;
                    continue;
                }
                continue;
            }
            if ((flJ) || (flDiv > 0))
                continue;
            if (!(flSkip))
                newText.append(textArray[i]);
        }
        return newText.toString();
    }

    public static String GetInfo(String site, InfoAbout info)throws Exception{
        if (site == null)
            site = "https://ru.wikipedia.org/wiki/Java";
        if (info == InfoAbout.ThumbnailSketchBook) {
            return ProcessText(GetThumbnailSketch(site, "</h2", "<p", "</p", "<h2"));
        }
        if (info == InfoAbout.Author) {
            return ProcessText(GetThumbnailSketch(site, "</tabl", "<p>", "</p", "<div"));
        }
        return ""; // читать книгу
    }
}
