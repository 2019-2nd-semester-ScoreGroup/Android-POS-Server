package Console;

import java.util.ArrayList;
import java.util.Scanner;

public class initClass {

    // 인풋값 : 1. 상품바코드정보, 2.행동

    private static int page = 0;
    private ArrayList<String> array;
    public String modType;
    public String startDate;
    public String endDate;

    // 생성자입니다.
    public initClass() {
        System.out.println("POS 모드를 설정해주세요 \n 1 = 결제하기 \n 2 = 납품하기 \n 3 = 결제 내역 확인");
    }

    public void init(String modType) {
        this.modType = modType;
        page = 0;
        array = new ArrayList<String>();
    }

    public void clearScreen() {
        for (int i = 0; i < 80; i++) {
            System.out.println("");
        }
    }

    public String act(String scannerValue) {
        if ("1".equals(modType)) {

            if (page == 0) {
                System.out.println("상품정보를 입력해주세요~^^");
                page++;
            } else if (page == 1) {
                if (!scannerValue.equals("done")) {

                    array.add(scannerValue);

                    for (String tmp : array) {
                        String[] tmpArr = tmp.split("/");
                        System.out.println("삼품명" + tmpArr[0]);
                        System.out.println("상품코드" + tmpArr[1]);
                        System.out.println("가격" + tmpArr[2]);
                    }

                } else {
                    System.out.println("결제완료^ㅡ^!! \n -메인화면으로 가시려면 \"home\"을 입력해주세요!1");
                    page = 0;
                }
            }

        } else if ("2".equals(modType)) {
            if (page == 0) {
                System.out.println("날짜를 입력해주세요");
                page++;
            } else if (page == 1) {
                System.out.println("pong2"); //결제기록 보여줘라 날짜 순서대로 index부여해서 클래스 만들어야할듯
                page--; // if 하나 더만들어서 인덱스 입력하면 해당 결제기록 보여주기
            }
        } else if ("3".equals(modType)) {
            if (page == 0) {
                System.out.println("날짜를 입력해주세요");
                page++;

            } else if (page == 1) {
                if (startDate == null) {
                    startDate = scannerValue;
                } else if (endDate == null) {
                    endDate = scannerValue;
                    System.out.println(startDate+" "+endDate);
                }
            }else if (page == 2){

            }
        }
        return null;
    }

    public static void main(String args[]) {
        initClass init = new initClass();
        Scanner scan = new Scanner(System.in);
        String tmp = "";
        do {
            tmp = scan.nextLine();
            init.clearScreen();
            // home을 입력해서 메인메뉴로 돌아가는 경우,
            if ("home".equals(tmp)) {
                init = new initClass();
            } else if (init.modType == null) {
                init.init(tmp);
                init.act(tmp);
            } else {
                init.act(tmp);

            }
        } while (!"exit".equals(tmp));

        System.out.println("종료 되었습니다.");
        scan.close();
    }
}