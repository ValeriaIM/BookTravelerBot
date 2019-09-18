import java.io.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        boolean fl = true;
        PrintFile("src\\Start work.txt");
        while(fl)
        {
            String answer = in.next();
            switch (answer){
                case ("1"):
                    PrintFile("src\\Help.txt");
                    break;
                case ("2"):
                    PrintFile("src\\Авторы.txt");
                    break;
                case ("3"):
                    //PrintFile("Help.txt");
                    break;
                case ("4"):
                    PrintFile("src\\Library.txt");
                    break;
                case ("5"):
                    fl = false;
                    break;
                default:
                    System.out.println("Упс, попробуйте ввести цифры без точки");
            }
        }
        in.close();
    }

    public static void PrintFile(String nameFile){
        try {
            File file = new File(nameFile);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
