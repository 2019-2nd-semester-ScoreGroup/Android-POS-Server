package Console;

import java.util.Scanner;

public class Main {
    public static void main(String args[]) {
        System.out.println("2=결제 3=결제기록 4=매출통계 ");
        Scanner scanner = new Scanner(System.in);


        String to;

        to = scanner.next();
        if (to.equals("1")) {
            while (true) {
                입력(바코드 or 결제 or 취소);
                배열 확인;
                if (이미 있는 바코드){
                    개수 추가;
                }
                else{
                    바코드 추가;
                }
                if (결제){
                    결제기록 db로 보냄;
                    메인화면으로;
                }
                if (취소){
                    메인화면으로;
                }
            } //날짜


        } else if (to.equals("2")) {
            while(true){
                입력 (바코드,개수 or 취소)
                기록 저장;
                if(결제){
                    결제기록 db로 보냄
                }
                if (취소){
                    메인화면으로
                }
            }

        } else if (to.equals("3")) {
            입력(날짜or취소);
            if (날짜){
                해당날짜 시간순으로 결제기록 다보여주고 그 순서대로 1234....index 부여;
                입력(index);
                    if(index){
                        해당 기록 보여줌
                }
                    if(취소){
                        날짜입력으로
                    }
            }
            if (취소){
                메인화면으로
            }


        } else if (to.equals("4")) {
            시작날짜 입력;
            종료날짜 입력;
            if(시작날짜, 종료날짜){
                두 날짜 사이의 매출통계 보여주기;
            }
        }
    }
}