package com.senla.training.test.denisikvadim.senlaatm.atm;

public class Banknote {
   private int value;
   private int amount;

   public Banknote(int value, int amount){
        this.value=value;
        this.amount=amount;
    }

    public int getAmount() {
        return amount;
    }

    public int getValue() {
        return value;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
