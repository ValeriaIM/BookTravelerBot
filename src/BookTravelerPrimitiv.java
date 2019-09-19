import java.io.*;
import java.util.Scanner;
import java.io.IOException;
import java.util.Date;

public class BookTravelerPrimitiv {
    public static void main(String[] args){
        Scanner in = new Scanner(System.in);
        boolean fl = true;
        PrintFile("src\\Start work.txt");
        while(fl)
        {
            String answer = in.next();
            switch (answer){
                case ("1"):
                    help();
                    break;
                case ("2"):
                    authors();
                    break;
                case ("3"):
                    printDate();
                    break;
                case ("4"):
                    library();
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

    //public static void getHtmlText(String html) {
    //    URL file = null;
    //    try {
    //       file = new URL(html);
    //    } catch (MalformedURLException e) {
    //        e.printStackTrace();
    //    }
    //    BufferedReader in = null;
    //    try {
    //        in = new BufferedReader(
    //               new InputStreamReader(file.openStream()));
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }

    //    String inputLine = "";
    //    while (true) {
    //        try {
    //            if (!((inputLine = in.readLine()) != null)) break;
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        System.out.println(inputLine);
    //    }
    //    try {
    //        in.close();
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //    }
    //}

    public static void library(){
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
                    System.out.println(FindStrInText(answer, "src\\Links.txt").substring(3));
                    //Task<DriveContents> openFileTask =
                    //        getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY);

                    fl = false;
                    break;
            }
        }
    }

    public static void printDate(){
        Date date = new Date();
        System.out.println(date.toString());
        PrintFile("src\\Start work.txt");
    }

    public static void help(){
        PrintFile("src\\Help.txt");
        exitToMenu();
    }

    public static void authors(){
        PrintFile("src\\Авторы.txt");
        exitToMenu();
    }

    public static void exitToMenu(){
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
                    System.out.println("Нажмите 0 для выхода в меню");
                    break;
            }
        }
    }
}
