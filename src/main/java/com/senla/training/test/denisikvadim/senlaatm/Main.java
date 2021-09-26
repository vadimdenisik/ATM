package com.senla.training.test.denisikvadim.senlaatm;
import com.senla.training.test.denisikvadim.senlaatm.atm.ATM;

public class Main {
    public static void main(String[] args){
        ATM atm = new ATM();
        UnlockingThread th = new UnlockingThread();
        Thread thread = new Thread(th);
        thread.start();
        boolean tr = true;
        while (tr) {
         tr = atm.authorization();
      }
        UnlockingThread.setTr(false);
        System.exit(0);
    }
}
