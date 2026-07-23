package com.priyanshu.spend_buddy;

import java.util.HashMap;
import java.util.Map;

public class MerchantCategory {

    private static final Map<String,String> merchants = new HashMap<>();

    static {

        // FOOD
        merchants.put("swiggy","Food");
        merchants.put("zomato","Food");
        merchants.put("dominos","Food");
        merchants.put("pizza hut","Food");
        merchants.put("kfc","Food");
        merchants.put("mcdonald","Food");
        merchants.put("burger king","Food");
        merchants.put("starbucks","Food");
        merchants.put("subway","Food");
        merchants.put("haldiram","Food");

        // SHOPPING
        merchants.put("amazon","Shopping");
        merchants.put("flipkart","Shopping");
        merchants.put("myntra","Shopping");
        merchants.put("ajio","Shopping");
        merchants.put("meesho","Shopping");
        merchants.put("nykaa","Shopping");

        // GROCERY
        merchants.put("blinkit","Groceries");
        merchants.put("zepto","Groceries");
        merchants.put("bigbasket","Groceries");
        merchants.put("instamart","Groceries");
        merchants.put("dmart","Groceries");

        // TRAVEL
        merchants.put("uber","Travel");
        merchants.put("ola","Travel");
        merchants.put("rapido","Travel");
        merchants.put("irctc","Travel");
        merchants.put("redbus","Travel");

        // FUEL
        merchants.put("indianoil","Fuel");
        merchants.put("bharat petroleum","Fuel");
        merchants.put("hp petrol","Fuel");
        merchants.put("shell","Fuel");

        // HEALTH
        merchants.put("apollo","Health");
        merchants.put("pharmeasy","Health");
        merchants.put("netmeds","Health");

        // ENTERTAINMENT
        merchants.put("netflix","Entertainment");
        merchants.put("spotify","Entertainment");
        merchants.put("prime video","Entertainment");
        merchants.put("hotstar","Entertainment");

        // BILLS
        merchants.put("airtel","Bills");
        merchants.put("jio","Bills");
        merchants.put("bsnl","Bills");
        merchants.put("vi","Bills");
        merchants.put("electricity","Bills");
        merchants.put("water","Bills");
        merchants.put("gas","Bills");
    }

    public static String detectCategory(String sms){

        String lower = sms.toLowerCase();

        for(String key : merchants.keySet()){

            if(lower.contains(key)){
                return merchants.get(key);
            }
        }

        return "Other";
    }

    public static String detectMerchant(String sms){

        String lower = sms.toLowerCase();

        for(String key : merchants.keySet()){

            if(lower.contains(key)){

                String merchant = key;

                String[] words = merchant.split(" ");

                StringBuilder builder = new StringBuilder();

                for(String word : words){

                    builder.append(
                            Character.toUpperCase(word.charAt(0))
                    );

                    if(word.length()>1){

                        builder.append(word.substring(1));
                    }

                    builder.append(" ");
                }

                return builder.toString().trim();
            }
        }

        return "Unknown";
    }

}
