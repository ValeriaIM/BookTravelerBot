import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Reader {
    public String readFile(String nameFile) {
        try {
            File file = new java.io.File(nameFile);
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

    private int countLines(String str) {
        String[] lines = str.split("\r\n|\r|\n");
        return lines.length;
    }

    public int getCountLinesInFile(String nameFile) {
        return countLines(readFile(nameFile));
    }

    public String readFileLine(String nameFile, int n) {
        try {
            File file = new java.io.File(nameFile);
            FileReader fileReader = new FileReader(file, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(fileReader);
            int count = 1;
            String line = reader.readLine();
            while ((line != null) && (count < n)) {
                line = reader.readLine();
                count++;
            }
            reader.close();
            return line;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getCurrentBookName(int currentBook) {
        var result = new StringBuffer();
        var name = readFileLine("src\\main\\resources\\library.txt", currentBook).toCharArray();
        var fl = false;
        for (var i = 0; i < name.length; i++) {
            if (fl)
                result.append(name[i]);
            if (name[i] == '"')
                fl = !fl;
        }
        return result.deleteCharAt(result.length() - 1).toString();
    }
}
