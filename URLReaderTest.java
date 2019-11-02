import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class URLReaderTest {
    String GetTextFromFile(String namefile){
        try{
            File file = new java.io.File(namefile);
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            StringBuffer text = new StringBuffer();
            while (line != null) {
                text.append(line + "\n");
                line = reader.readLine();
            }
            return text.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    @Test
    void processText_DefaultSite() throws Exception {
        String actual = URLReader.ProcessText(URLReader.GetThumbnailSketch("https://ru.wikipedia.org/wiki/Java", "<div", "<p><b>", "</p", "<div"));
        String expected = GetTextFromFile("src\\main\\resources\\textForURLReaderTest-2m-1.txt");
        assertEquals(expected, actual);
    }

    @Test
    void processText_SiteOfAuthor() throws Exception {
        String actual = URLReader.ProcessText(URLReader.GetThumbnailSketch("https://ru.wikipedia.org/wiki/Гослинг,_Джеймс", "</table", "<p><b>", "</p", "<div"));// поправить
        String expected = GetTextFromFile("src\\main\\resources\\textForURLReaderTest-2m-2.txt");
        assertEquals(expected, actual);
    }

    @Test
    void processText_SiteOfBook() throws Exception {
        String actual = URLReader.ProcessText(URLReader.GetThumbnailSketch("https://ru.wikipedia.org/wiki/Золушка", "</h2", "<p>", "</p", "<h2"));
        String expected = GetTextFromFile("src\\main\\resources\\textForURLReaderTest-2m-3.txt");
        assertEquals(expected, actual);
    }

    @Test
    void getInfo() throws Exception {
        String finalText = URLReader.GetInfo("https://ru.wikipedia.org/wiki/Золушка", URLReader.InfoAbout.ThumbnailSketchBook);
        String expected = GetTextFromFile("src\\main\\resources\\textForURLReaderTest-2m-3.txt");
        assertEquals(expected, finalText);
    }
}