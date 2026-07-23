package com.priyanshu.spend_buddy;

public class RecurringExpense {
    public String title;
    public long amount;
    public String category;
    public long day;

    public RecurringExpense(){}

    public RecurringExpense(String id, long amount,String category,long day){
        this.title=id;
        this.amount=amount;
        this.category=category;
        this.day=day;
    }
}
