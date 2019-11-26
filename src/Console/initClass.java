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
    public String date;

    // 생성자입니다.
    public initClass() {
        System.out.println("POS 모드를 설정해주세요 \n 1 = 결제,납품 \n 2 = 결제기록 \n 3 = 통계");
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
                        System.out.println("상품코드" + tmpArr[0]);
                        System.out.println("개수" + tmpArr[1]);
                    }
                } else {
                    System.out.println("결제완료^ㅡ^!!");
                    //String tmp : array 모아서 프린트하기
                    System.out.println(array);
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                    page = 0;
                }
            }
        } else if ("2".equals(modType)) {
            if (page == 0) {
                System.out.println("날짜를 입력해주세요");
                page++;
            } else if (page == 1) {
                if (date == null) {
                    date = scannerValue;
                    page++;
                }
                System.out.println(date + "목록입니다");

                for (int i = 0; i < 5; i++) {
                    System.out.println(i + " " + date);
                }
                page++;

            } else if (page == 2) {
                System.out.println("인덱스 입력");
                //
                page++;
            } else if (page == 3) {
                System.out.println("결제목록 detail");
                page++; //취소시 page++

            } else if (page == 4) {
                System.out.println("결제기록이 취소되었습니다.");

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
                    System.out.println(startDate + " " + endDate);
                    //startDate endDate 날짜 사이의 더미데이터 보여주기
                    for (int i = 0; i < 5; i++) {
                        System.out.println();

                    }

                }
            }
        } else if (page == 2) {

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