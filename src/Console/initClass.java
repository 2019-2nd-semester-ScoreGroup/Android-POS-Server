package Console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class initClass {

    // 인풋값 : 1. 상품바코드정보, 2.행동

    private static int page = 0;
    private ArrayList<HashMap<String, String>> array;
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
        array = new ArrayList<HashMap<String, String>>();
    }

    public void clearScreen() {
        for (int i = 0; i < 80; i++) {
            System.out.println("");
        }
    }

    // 일치하는 코드가 입력되어 있는지 찾고, 없으면 -1, 있으면 인덱스 리턴

    /**
     * @param array : 물건이 담겨있는 HashMap<String, String> 타입의 ArrayList배열
     * @param item : 위의 HashMap 배열과 비교할 String 값
     * @return 아이템이 있을 경우 index, 없을 경우 -1을 리턴합니다.
     *
     */
    private int findItemIndex(ArrayList<HashMap<String, String>> array, String item){
        int i = 0;
        for(HashMap<String, String> tmp : array){
            if(item.equals(tmp.get("item"))){
                return i;
            };
            i++;
        }
        return (-1);
    }

    public String act(String scannerValue) {
        if ("1".equals(modType)) {
            if (page == 0) {
                System.out.println("상품정보를 입력해주세요~^^");
                page++;
            } else if (page == 1) {
                // 스캐너 값 입력 받았을때 행동
                if (!scannerValue.equals("done")) {
                    HashMap<String, String> hashmap = new HashMap<String, String>();

                    // 컴마 (구분자) 포함 여부에 따라 String 값을 설정합니다.
                    String input_item = (scannerValue.contains(",")) ? scannerValue.split(",")[0] : scannerValue;
                    String input_amount = (scannerValue.contains(",")) ? scannerValue.split(",")[1] : "1";

                    // 물건이 들어있는 hashmap 배열에서 동일한 item이 있으면 index값을, 없으면 -1을 리턴합니다.
                    int itemIndex = findItemIndex(array, input_item);

                    // 이미 값이 있을경우, amount만큼 기존값에 더합니다.
                    if(itemIndex!=-1) {
                        String cur_amount = array.get(itemIndex).get("amount");
                        array.get(itemIndex).put("amount", "" +
                                (Integer.parseInt(cur_amount) + Integer.parseInt(input_amount)));
                    }else{
                        hashmap.put("item", input_item);
                        hashmap.put("amount", input_amount);
                        array.add(hashmap);
                    }

                    // 어레이 돌면서 콘솔에 값 찍어주는 분기처리
                    for (HashMap<String, String> tmp : array) {
                        System.out.println("상품코드" + tmp.get("item"));
                        System.out.println("개수" + tmp.get("amount"));
                    }
                } else {
                    System.out.println("결제완료^ㅡ^!!");
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
                        System.out.println(startDate+i);
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