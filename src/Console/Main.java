package Console;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String args[]){
        System.out.println("1=판매, 2=납품 ,3=재고");
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> arraylist = new ArrayList<String>();

        String to;

        to = scanner.next();
        if (to.equals("1")){
            while(true){
                System.out.println("코드 입력 or stop");
                String input=scanner.nextLine();
                if(input.equals("stop")) {
                    System.out.println(arraylist);
                    break;
                }
                else arraylist.add(input);

            }

        }
        else if (to.equals("2")){


        }
        else if (to.equals("3")){

        }
        else{
            System.out.println("장난 Nope");
        }

    }
}