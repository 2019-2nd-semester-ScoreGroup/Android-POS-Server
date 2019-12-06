package Console;

import Data.Change;
import Data.Event;
import Data.EventList;
import Data.Stock;
import Database.DBManager;

import java.awt.desktop.SystemEventListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class initClass {

    // 인풋값 : 1. 상품바코드정보, 2.행동

    private static int page = 0;
    private ArrayList<Change> array;
    private String modType, startDate, endDate, date;
    private long index_long;

    DBManager db = new DBManager("localhost", "androidpos", "root", "201512087");
    Timestamp tsStartDate;
    Timestamp tsEndDate;
    Event vo;

    // 생성자입니다.
    public initClass() {
        System.out.println("POS 모드를 설정해주세요 \n 1 = 결제 \n 2 = 결제기록 \n 3 = 통계\n 4 = 납품\n 5 = 납품기록 \n");
    }

    public void init(String modType) {
        this.modType = modType;
        page = 0;
        array = new ArrayList<Change>();
    }

    public void clearScreen() {
        for (int i = 0; i < 80; i++) {
            System.out.println("");
        }
    }

    // 일치하는 코드가 입력되어 있는지 찾고, 없으면 -1, 있으면 인덱스 리턴


    private int findItemIndex(ArrayList<Change> array, String item) {
        int i = 0;
        for (Change tmp : array) {
            if (item.equals(tmp.getStockKey())) {
                return i;
            }
            i++;
        }
        return (-1);
    }

    public String act(String scannerValue) {

        if ("1".equals(modType)) {
            if (page == 0) {
                System.out.println("상품정보를 입력해주세요~^^(stockkey,amount)");
                System.out.println("결제를 원하시면 done을 입력해주세요");
                page++;
            } else if (page == 1) {
                // 스캐너 값 입력 받았을때 행동
                if (!scannerValue.equals("done")) {

                    // 컴마 (구분자) 포함 여부에 따라 String 값을 설정합니다.
                    String input_item = (scannerValue.contains(",")) ? scannerValue.split(",")[0].trim() : scannerValue.trim();
                    String input_amount_String = (scannerValue.contains(",")) ? scannerValue.split(",")[1].trim() : "1";
                    input_amount_String = input_amount_String.replaceAll("\\D", "");
                    int input_amount = Integer.parseInt(input_amount_String);


                    if (input_item.isEmpty()) {
                        System.out.println("ㅈㄹㄴ");
                        return null;
                    }
                    // 물건이 들어있는 배열에서 동일한 item이 있으면 index값을, 없으면 -1을 리턴합니다.
                    int itemIndex = findItemIndex(array, input_item);

                    // 이미 값이 있을경우, amount만큼 기존값에 더합니다.
                    if (itemIndex != -1) {
                        int cur_amount = array.get(itemIndex).getAmount();
                        array.get(itemIndex).setAmount(cur_amount + input_amount);
                    } else {
                        array.add(new Change(input_item, input_amount));
                    }

                    // 어레이 돌면서 콘솔에 값 찍어주는 분기처리
                    for (Change tmp : array) {
                        System.out.println("상품코드" + tmp.getStockKey());
                        System.out.println("개수" + tmp.getAmount());
                    }
                } else {
                    Date date = new Date();

                    // 직접 DB에 넣는 친구
                    Event evt = new Event((byte) 1, new Timestamp(date.getTime()), "");
                    long 변수이름_추천 = db.addEvent(evt);

                    for (Change tmpCng : array) {
                        Change cng = new Change(tmpCng.getStockKey(), tmpCng.getAmount());

                        cng.setEventKey(변수이름_추천);

                        db.addChange(cng);
                    }


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
                System.out.println("전체 결제기록입니다.");
                EventList[] eventLists = db.getEventList((byte) 1);
                for (EventList tmp : eventLists) {
                    long tmpKey = tmp.getKey();
                    byte statusB = db.getEvent(tmpKey).getStatus();
                    System.out.println(statusB);
                    String status = (statusB == 0) ? "결제완료" : "결제취소";
                    System.out.println(tmpKey + "     /  상태 :" + status);
                }

                System.out.println("키를 입력해주세요");
                page++;
            } else if (page == 1) {
                index_long = (long) Integer.parseInt(scannerValue);

                Event event = db.getEvent(index_long);
                System.out.println("결제기록입니다.");
                ArrayList<Change> cngArr = event.getData();
                for (Change tmpCng : cngArr) {
                    System.out.println("상품 코드" + tmpCng.getStockKey() + " 개수 " + tmpCng.getAmount());
                }
                System.out.println("결제기록을 삭제하시겠습니까?(yes/no)");

                page++;

            } else if (page == 2) {

                if ("yes".equals(scannerValue)) {
                    db.tryChangeEvent(index_long, (byte) 1);
                    System.out.println("결제기록이 삭제되었습니다.");
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                }
                if ("no".equals(scannerValue)) {
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                }
                page = 0;
            }
        } else if ("3".equals(modType)) {
            if (page == 0) {
                System.out.println("시작날짜를 입력해주세요(yyyy-mm-dd hh:mm:ss)");
                page++;
            } else if (page == 1) {
                if (startDate == null) {
                    startDate = scannerValue;
                    System.out.println("종료날짜를 입력해주세요(yyyy-mm-dd hh:mm:ss)");
                } else if (endDate == null) {
                    endDate = scannerValue;
                    tsStartDate = Timestamp.valueOf(startDate);
                    tsEndDate = Timestamp.valueOf(endDate);
                    Stock[] stocks = db.getSelling(tsStartDate, tsEndDate);
                    for (Stock tmp : stocks) {
                        String stockName = tmp.getName();
                        int stockPrice = tmp.getPrice();
                        int amount = tmp.getAmount();
                        System.out.println("받아온 정보 : " + stockName + " , 가격 : " + stockPrice + " , 갯수 : " + amount);
                    }
                }
            }
        } else if ("4".equals(modType)) {
            if (page == 0) {
                System.out.println("상품정보를 입력해주세요~^^(stockkey,amount)");
                System.out.println("결제를 원하시면 done을 입력해주세요");
                page++;
            } else if (page == 1) {
                // 스캐너 값 입력 받았을때 행동
                if (!scannerValue.equals("done")) {

                    // 컴마 (구분자) 포함 여부에 따라 String 값을 설정합니다.
                    String input_item = (scannerValue.contains(",")) ? scannerValue.split(",")[0].trim() : scannerValue.trim();
                    String input_amount_String = (scannerValue.contains(",")) ? scannerValue.split(",")[1].trim() : "1";
                    input_amount_String = input_amount_String.replaceAll("\\D", "");
                    int input_amount = Integer.parseInt(input_amount_String);


                    if (input_item.isEmpty()) {
                        System.out.println("ㅈㄹㄴ");
                        return null;
                    }
                    // 물건이 들어있는 hashmap 배열에서 동일한 item이 있으면 index값을, 없으면 -1을 리턴합니다.
                    int itemIndex = findItemIndex(array, input_item);

                    // 이미 값이 있을경우, amount만큼 기존값에 더합니다.
                    if (itemIndex != -1) {
                        int cur_amount = array.get(itemIndex).getAmount();
                        array.get(itemIndex).setAmount(cur_amount + input_amount);
                    } else {
                        array.add(new Change(input_item, input_amount));
                    }

                    // 어레이 돌면서 콘솔에 값 찍어주는 분기처리
                    for (Change tmp : array) {
                        System.out.println("상품코드" + tmp.getStockKey());
                        System.out.println("개수" + tmp.getAmount());
                    }
                } else {
                    Date date = new Date();

                    // 직접 DB에 넣는 친구
                    Event evt = new Event((byte) 1, new Timestamp(date.getTime()), "");
                    long 변수이름_추천 = db.addEvent(evt);

                    for (Change tmpCng : array) {
                        Change cng = new Change(tmpCng.getStockKey(), tmpCng.getAmount());

                        cng.setEventKey(변수이름_추천);

                        db.addChange(cng);
                    }
                }
            }
        } else if ("5".equals(modType)) {
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
                System.out.println("전체 납품기록입니다.");
                EventList[] eventLists = db.getEventList((byte) 2);
                for (EventList tmp : eventLists) {
                    long tmpKey = tmp.getKey();
                    byte statusB = db.getEvent(tmpKey).getStatus();
                    System.out.println(statusB);
                    String status = (statusB == 0) ? "납품완료" : "납품취소";
                    System.out.println(tmpKey + "     /  상태 :" + status);
                }
                System.out.println("키를 입력해주세요");
                page++;
            } else if (page == 1) {
                index_long = (long) Integer.parseInt(scannerValue);
                Event event = db.getEvent(index_long);
                System.out.println("해당 납품기록입니다.");
                ArrayList<Change> cngArr = event.getData();
                for (Change tmpCng : cngArr) {
                    System.out.println("상품 코드" + tmpCng.getStockKey() + " 개수 " + tmpCng.getAmount());
                }
                System.out.println("납품기록을 삭제하시겠습니까?(yes/no)");
                page++;
            } else if (page == 2) {
                if ("yes".equals(scannerValue)) {
                    db.tryChangeEvent(index_long, (byte) 2);
                    System.out.println("납품기록이 삭제되었습니다.");
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                }
                if ("no".equals(scannerValue)) {
                    System.out.println("메인화면으로 가시려면 \"home\"을 입력해주세요!");
                }
                page = 0;
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