package com.senla.training.test.denisikvadim.senlaatm.atm;

import com.senla.training.test.denisikvadim.senlaatm.account.Card;
import com.senla.training.test.denisikvadim.senlaatm.fileManager.FileManager;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class ATM implements AtmInterface {
    public static final String BANKNOTES_PATH = "./banknotes.txt";
    public static final String CARDS_PATH = "./cards.txt";
    public static  SortedMap<Integer, Banknote> banknotes = new TreeMap<>();
    public static SortedMap<Long, Card> cards = new TreeMap<>();
    private static long ATMBalance = 0;
    private static Card activeCard = null;
    private static Scanner scanner = new Scanner(System.in);


    public ATM(){
        loadBanknotes();
        loadCards();
    }

    private void loadBanknotes() {
          ArrayList<Object> banknotesTemp = FileManager.banknoteFileManager.getData((FileManager.readFile(BANKNOTES_PATH)));
          if (banknotesTemp != null && banknotesTemp.size() > 0) {
              banknotes.clear();
              for (Object banknote : banknotesTemp) {
                  if (banknote instanceof Banknote) {
                      banknotes.put(((Banknote) banknote).getValue(), (Banknote) banknote);
                  }
              }
              banknotes.forEach((k,v)->{
                  ATMBalance+=v.getValue()*v.getAmount();
              });
          }
      }

    private void loadCards() {
        ArrayList<Object> cardsTemp = FileManager.cardFileManager.getData((FileManager.readFile(CARDS_PATH)));
        if (cardsTemp != null && cardsTemp.size() > 0) {
            cards.clear();
            for (Object card : cardsTemp) {
                if (card instanceof Card) {
                    cards.put(((Card) card).getNumber(), (Card) card);
                }
            }
        }
    }

    public boolean authorization(){
        System.out.println("Здравствуйте! Для авторизации введите номер своей карты. Для завершения работы введите \"quit\"");
        boolean isCorrectNumber = false;
        boolean isCorrectPin = false;
        long cardNumber;
        int PIN;
        int blockCount = 0;
        Card tempCard=null;
        do {
            if (scanner.hasNextLong()) {
                cardNumber = scanner.nextLong();
                if (getDigitCount(cardNumber)!=16){
                    System.out.println("Номер карты должен состоять из 16 цифр, попробуйте еще раз");
                    continue;
                } else {

                    if((tempCard = checkLogin(cardNumber))==null){
                        System.out.println("Карты с таким номером не существует. Введите другой номер");
                        continue;
                    } else {
                        if (tempCard.isLocked()){
                            System.out.println("Ваша карта заблокирована! Дождитесь разблокировки: " + tempCard.getUnlockDate());
                            continue;
                        }
                    }
                        isCorrectNumber=true;
                }

            } else if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
                return false;

            } else {
                System.out.println("Введите корректное значение!");
                isCorrectNumber = false;
                scanner.next();
            }
        } while (!isCorrectNumber);

     System.out.println("Введите PIN. Для возврата введите quit");
     do {
         if (scanner.hasNextInt()) {
             PIN = scanner.nextInt();
             if (getDigitCount(PIN) != 4) {
                 System.out.println("PIN должен состоять из 4 символов. Введите корректный код.");
                 continue;
             } else {
                 if (checkPin(tempCard, PIN)) {
                     System.out.println("Авторизация выполнена успешно");
                     isCorrectPin=true;
                 } else {
                     blockCount++;
                     if (blockCount<3)
                     System.out.println("Неверный пинкод, у вас осталось " + (3-blockCount) + " попытки! Если вы продолжите вводить пинкод неправильно, карта будет заблокирована");
                     else {
                         tempCard.lockCard();
                         blockCount=0;
                         System.out.println("Карта была заблокирована! Время снятия блокировки: " + tempCard.getUnlockDate());
                         tempCard=null;
                         FileManager.cardFileManager.refreshData();
                         return true;
                     }
                 }
             }
         } else  if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
             return true;
         } else  {
             System.out.println("Введите корректное значение!");
             isCorrectPin = false;
             scanner.next();
         }
     } while (!isCorrectPin);

     System.out.println("Переход в личный кабинет.");
     personalPage();
     return true;
    }

    private Card checkLogin(long cardNumber){
        return cards.get(cardNumber);
    }

    private boolean checkPin(Card card, int pin){
        if(card.getPin()==pin){
            activeCard=card;
            return true;
        } else return false;
    }

    private void personalPage() {
        String option;
        while (true) {
            System.err.println("Выберите действие");
            System.out.println();
            System.out.println("1. Сведения о балансе");
            System.out.println("2. Снятие наличных");
            System.out.println("3. Пополнение счета");
            System.out.println("4. Сменить пинкод");
            System.out.println("5. Информация о счете");
            System.out.println("6. Выход (сменить карту)");

            if (scanner.hasNextInt()) {
                option = scanner.nextLine();

                    switch (option){
                        case "1": getBalance();
                        break;
                        case "2": withdrawInterface();
                        break;
                        case "3": refillInterface();;
                        break;
                        case "4": changePin();
                        break;
                        case "5": getAccountInfo();
                        break;
                        case "6":
                            System.err.println("Вы вышли из аккаунта");
                            activeCard=null;
                            return;
                        default:
                            System.out.println("Введите корректное значение");
                    }

            } else {
                System.out.println("Введите корректное значение!");
                scanner.next();
            }
        }
    }

    private void getBalance(){
        if (activeCard!=null){
            System.out.println();
            System.out.println("Баланс вашей карты: " + activeCard.getBalance());
            System.out.println();
        }
    }

    private void getAccountInfo(){
        if (activeCard!=null){
            System.out.println("Номер карты: " + activeCard.getNumber());
            System.out.println("Пин-код: " + activeCard.getPin());
            System.out.println("Баланс: " + activeCard.getBalance());
            System.out.print("Статус блокировки: ");
            if(!activeCard.isLocked())
            System.out.println("не заблокирована");
            else {
                System.out.println("заблокирована");
                System.out.println("Дата(в нашем случае время) блокировки: " + activeCard.getLockDate());
                System.out.println("Дата(в нашем случае время) снятия блокировки: " + activeCard.getUnlockDate());
            }
            System.out.println();
            System.out.println();
        }
    }

    private void withdrawInterface(){
        if (activeCard!=null){
            boolean isCorrectNumber = false;
            int number = 0;
            do {
            System.out.println("Баланс вашего счета составляет: " + activeCard.getBalance());
            System.out.println("Введите сумму, которую хотите снять со счета. для возврата quit");

                if (scanner.hasNextInt()) {
                    number = scanner.nextInt();


                    if (activeCard.getBalance()<number){
                        System.out.println("Недостаточно средств на счете! Введите другое значение");
                        scanner.next();
                        continue;
                    }
                    if (ATMBalance<number){
                        System.out.println("Недостаточно средств в банкомате! Введите другое значение");
                        scanner.next();
                        continue;
                    }

                    ArrayList<Option> options = banknoteSelection(number);

                    if (options!=null && options.size()>0){
                        System.out.println("Какими купюрами вы хотели бы получить сумму? Введите номер опции(указан перед каждым вариантом)");

                        for (int i=0; i<options.size(); i++){
                            System.out.println((i+1)+": " + options.get(i).getFiveAmount() +" по 5 рублей, " +  options.get(i).getTenAmount() + " по 10 рублей, " + options.get(i).getTwentyAmount() + " по 20 рублей");
                        }

                        System.out.println("Какими купюрами вы хотели бы получить сумму? Введите номер опции(указан перед каждым вариантом)");

                        boolean isOption = false;
                        while (!isOption){
                            if (scanner.hasNextInt()){
                                int optionNumber = scanner.nextInt();
                                if (optionNumber>options.size()){
                                    System.out.println("Введите корректный номер");
                                    continue;
                                } else {
                                    banknoteExtraction(options.get(optionNumber-1));
                                    activeCard.withdrawMoney(number);
                                    isOption=true;
                                }

                            } else {
                                System.out.println("Введите корректный номер!");
                                isCorrectNumber=false;
                                scanner.next();
                            }
                        }

                    } else {
                        System.out.println("Такую сумму получить нельзя, введите новое значение");
                        continue;
                    }
                    isCorrectNumber = true;
                } else  if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
                    return;
                } else  {
                    System.out.println("Введите корректное значение!");
                    isCorrectNumber=false;
                    scanner.next();
                }
            } while (!isCorrectNumber);
            FileManager.banknoteFileManager.refreshData();
            System.out.println("Операция выполнена успешно.");
        }
    }

    private void refillInterface() {
        if (activeCard != null) {
            boolean isCorrectNumber = false;
            int number;
            do {
                System.out.println("Баланс вашего счета составляет: " + activeCard.getBalance());
                System.out.println("Введите сумму, на которую хотите пополнить счёт");

                if (scanner.hasNextInt()) {
                    number = scanner.nextInt();
                    if (number > 1000000) {
                        System.out.println("Слишком большая сумма. Пополните 999999, а потом еще на рубль");
                        scanner.next();
                        continue;
                    }
                    activeCard.addMoney(number);
                    isCorrectNumber=true;
                } else  if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
                    return;
                } else  {
                    System.out.println("Введите корректное значение!");
                    isCorrectNumber = false;
                    scanner.next();
                  }
            } while (!isCorrectNumber);
            System.out.println("Операция выполнена успешно.");
        }
    }

    private void banknoteExtraction(Option option){
      banknotes.get(5).setAmount(banknotes.get(5).getAmount()-option.getFiveAmount());
      banknotes.get(10).setAmount(banknotes.get(10).getAmount()-option.getTenAmount());
      banknotes.get(20).setAmount(banknotes.get(20).getAmount()-option.getTwentyAmount());
      FileManager.banknoteFileManager.refreshData();
    }

    private ArrayList<Option> banknoteSelection(int sum){
        ArrayList<Integer> list = new ArrayList<>(banknotes.keySet());
        ArrayList<Option> options = new ArrayList<>();

        for (int f=0; f<sum/list.get(0); f++){
            for (int t=0; t<(sum-(f*list.get(0))/list.get(1)); t++){
                for (int tw=0; tw<(sum-(f*list.get(0))-(tw*list.get(1))/list.get(2)); tw++){
                    if (f * list.get(0) + t * list.get(1) + tw * list.get(2) == sum) {
                        if (f <= banknotes.get(5).getAmount() && t <= banknotes.get(10).getAmount() && tw <= banknotes.get(20).getAmount()) {
                            options.add(new Option(f, t, tw));
                        }
                    }
                }
            }
        }
    return options;
    }

    private void changePin(){
        if (activeCard != null) {
            boolean isCorrectNumber = false;
            boolean isCorrectNumberNew = false;
            int PIN;
            int PINNew;
            do {
                System.out.println("Для смены PIN-кода введите текущий PIN. Для возврата введите quit");
                if (scanner.hasNextInt()) {
                    PIN = scanner.nextInt();
                    if (getDigitCount(PIN)!=4){
                        System.out.println("PIN должен состоять из 4 символов. Введите корректный код.");
                        continue;
                    } else {
                        if (PIN!=activeCard.getPin()){
                            System.out.println("Вы ввели неверный пинкод, попробуйте еще раз");
                            continue;
                        } else
                            isCorrectNumber=true;
                    }

                } else  if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
                    return;
                } else  {
                    System.out.println("Введите корректное значение!");
                    isCorrectNumber = false;
                    scanner.next();
                }
            } while (!isCorrectNumber);

            System.out.println("Введенные данные верны");
            do {
                System.out.println("Введите новый код. Для возврата введите quit");
                if (scanner.hasNextInt()) {
                    PINNew = scanner.nextInt();
                    if (getDigitCount(PINNew)!=4){
                        System.out.println("PIN должен состоять из 4 символов. Введите корректный код.");
                        continue;
                    } else {
                        activeCard.setPin(PINNew);
                        isCorrectNumberNew=true;
                    }
                } else  if (scanner.hasNextLine() && scanner.nextLine().equals("quit")) {
                    return;
                } else {
                    System.out.println("Введите корректное значение!");
                    isCorrectNumber = false;
                    scanner.next();
                }
            } while (!isCorrectNumberNew);

            System.out.println("Операция выполнена успешно.");
        }
    }

    private int getDigitCount(Number number) {
        if (number instanceof Integer){
            return String.valueOf(Math.abs((Integer) number)).length();
        }
        if (number instanceof Long){
            return String.valueOf(Math.abs((Long) number)).length();
        }
        if (number instanceof Double){
            return String.valueOf(Math.abs((Double) number)).length();
        }
        return 0;
    }

     class Option{
        private int fiveAmount;
        private int tenAmount;
        private int twentyAmount;

        Option(int fiveAmount, int tenAmount, int twentyAmount) {
            this.fiveAmount = fiveAmount;
            this.tenAmount = tenAmount;
            this.twentyAmount = twentyAmount;
        }

         int getFiveAmount() {
            return fiveAmount;
        }

         int getTenAmount() {
            return tenAmount;
        }

         int getTwentyAmount() {
            return twentyAmount;
        }
    }

}
