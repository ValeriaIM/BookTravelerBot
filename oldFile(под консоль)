import java.io.*;
import java.util.Scanner;
import java.io.IOException;
import java.net.*;
import java.util.Date;

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
                    Help();
                    break;
                case ("2"):
                    Authors();;
                    break;
                case ("3"):
                    PrintDate();;
                    break;
                case ("4"):
                    Library();
                    break;
                case ("5"):
                    fl = false;
                    break;
                case ("6"):
                    echo();
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
    
    public static String FindStrInText(int n, String nameFile){
        try {
            File file = new File(nameFile);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);
            String line = reader.readLine();
            for (int i = 1; i < n; i++)
                line = reader.readLine();
            return  line;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static void Library(){
        PrintFile("src\\Library.txt");
        boolean fl = true;
        Scanner in = new Scanner(System.in);
        while (fl){
            int answer = in.nextInt();
            switch (answer){
                case (0):
                    fl = false;
                    PrintFile("src\\Start work.txt");
                    break;
                default:
                    System.out.println(htmltext(FindStrInText(answer, "src\\Links.txt").substring(3)));

                    fl = false;
                    break;
            }
        }
    }
    
    public static void Help(){
        PrintFile("src\\Help.txt");
        exitToMenu("Нажмите 0 для выхода в меню");
    }

    public static void Authors(){
        PrintFile("src\\Авторы.txt");
        exitToMenu("Нажмите 0 для выхода в меню");
    }
    
    public static void PrintDate(){
        Date date = new Date();
        System.out.println(date.toString());
        PrintFile("src\\Start work.txt");
    }
    
    public static void exitToMenu(String str){
        boolean fl = true;
        Scanner in = new Scanner(System.in);
        while (fl){
            String answer = in.nextLine();
            switch (answer){
                case ("0"):
                    fl = false;
                    PrintFile("src\\Start work.txt");
                    break;
                default:
                    if (str == "")
                        System.out.println(answer);
                    else
                        System.out.println(str);
                    break;
            }
        }
    }
    
    public static void echo(){
        PrintFile("src\\echo.txt");
        exitToMenu("");
    }
}
