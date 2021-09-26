package com.senla.training.test.denisikvadim.senlaatm;

import com.senla.training.test.denisikvadim.senlaatm.account.Card;
import static java.lang.Thread.sleep;

 class UnlockingThread implements Runnable
{
    private boolean tr = true;

    public static void setTr(boolean tr) {
        tr = tr;
    }

    public void run()
    {
        while (tr) {
            try {
                sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Card.checkLocked();
        }
        return;
    }
}
