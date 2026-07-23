package com.priyanshu.spend_buddy;

public class Expenses {

    public String id;

    public String title;

    public String category;

    public String merchant;

    public String bank;

    public long amount;

    public long date;

    public Expenses() {
        // Required for Firebase
    }

    public Expenses(String id,
                    String title,
                    String category,
                    String merchant,
                    String bank,
                    long amount,
                    long date) {

        this.id = id;
        this.title = title;
        this.category = category;
        this.merchant = merchant;
        this.bank = bank;
        this.amount = amount;
        this.date = date;
    }
}