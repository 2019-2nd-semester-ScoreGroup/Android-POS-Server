package Console;

import java.awt.desktop.SystemEventListener;
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
    public int count;

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
     * @param item  : 위의 HashMap 배열과 비교할 String 값
     * @return 아이템이 있을 경우 index, 없을 경우 -1을 리턴합니다.
     */
    private int findItemIndex(ArrayList<HashMap<String, String>> array, String item) {
        int i = 0;
        for (HashMap<String, String> tmp : array) {
            if (item.equals(tmp.get("item"))) {
                return i;
            }
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
                    String input_item = (scannerValue.contains(",")) ? scannerValue.split(",")[0].trim() : scannerValue.trim();
                    String input_amount = (scannerValue.contains(",")) ? scannerValue.split(",")[1].trim() : "1";
                    input_amount = input_amount.replaceAll("\\D", "");
                    if(input_item.isEmpty()){
                        System.out.println("ㅈㄹㄴ");
                       return null;
                    }
                    // 물건이 들어있는 hashmap 배열에서 동일한 item이 있으면 index값을, 없으면 -1을 리턴합니다.
                    int itemIndex = findItemIndex(array, input_item);

                    // 이미 값이 있을경우, amount만큼 기존값에 더합니다.
                    if (itemIndex != -1) {
                        String cur_amount = array.get(itemIndex).get("amount");
                        array.get(itemIndex).put("amount", "" +
                                (Integer.parseInt(cur_amount) + Integer.parseInt(input_amount)));
                    } else {
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
            if (page != 0 && "back".equals(scannerValue)) {
                if (page <= 0) {
                    date = null;
                } else if (page == 1) {
                    page--;
                } else {
                    page -= 2;
                }
            }
            if (page == 0) {
                System.out.println("날짜를 입력해주세요");
                page++;
            } else if (page == 1) {
                if (date == null) {
                    date = scannerValue;
                }

                page++;
                System.out.println(date + "목록입니다");

                for (int i = 0; i < 5; i++) {
                    System.out.println(i + " " + date);
                }
                System.out.println("인덱스 입력");
            } else if (page == 2) {
                System.out.println(scannerValue + "번 결제목록입니다.");
                for (int i = 0; i < 2; i++) {
                    System.out.println("상품코드 123123" + i + "개수" + i);
                }
                page++;
                System.out.println("삭제하시겠습니까?");

            } else if (page == 3) {
                if ("yes".equals(scannerValue)) {
                    System.out.println("결제기록이 삭제되었습니다.");
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                }
                if ("no".equals(scannerValue)) {
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");

                }
                page = 0;
            } else if (page == 4) {
            }
        } else if ("3".equals(modType)) {
            if (page == 0) {
                System.out.println("시작날짜를 입력해주세요");
                page++;
            } else if (page == 1) {
                System.out.println("종료날짜를 입력해주세요");
                if (startDate == null) {
                    startDate = scannerValue;
                } else if (endDate == null) {
                    endDate = scannerValue;
                    System.out.println("시작날짜 " + startDate + " " + "종료날짜 " + endDate);
                    //startDate endDate 날짜 사이의 더미데이터 보여주기
                    for (int i = Integer.parseInt(startDate); i <= Integer.parseInt(endDate); i++) {
                        for (int j = 0; j < 2; j++) {
                            System.out.println(count + " " + i);
                            count++;
                        }
                    }
                    count = 0;
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                    page = 0;
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