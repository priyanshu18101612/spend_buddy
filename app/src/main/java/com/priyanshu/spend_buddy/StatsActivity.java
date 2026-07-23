package com.priyanshu.spend_buddy;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.components.XAxis;

import java.text.SimpleDateFormat;
import java.util.*;

public class StatsActivity extends AppCompatActivity {

    BarChart barChart;
    LineChart lineChart;
    private TextView tvGrowth;

    TextView tvTotalSpent;
     private TextView tvActiveDays;
    TextView btnWeek, btnMonth, btnDay;
    private TextView tvHighestExpense;
    private TextView tvAverageSpend;
    private TextView tvTransactionCount;

    FirebaseFirestore db;
    String userId;

    boolean isWeek = true;
    boolean isDay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvGrowth = findViewById(R.id.tvGrowth);
        tvGrowth.setText("▲ Stable Spending");

        tvActiveDays=findViewById(R.id.tvActiveDays);

        barChart = findViewById(R.id.barChart);
        lineChart = findViewById(R.id.lineChart);

        btnMonth = findViewById(R.id.btnMonth);
        btnWeek = findViewById(R.id.btnWeek);
        btnDay = findViewById(R.id.btnDay);
        tvHighestExpense = findViewById(R.id.tvHighestExpense);
        tvAverageSpend = findViewById(R.id.tvAverageSpend);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);

        db = FirebaseFirestore.getInstance();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadStats();
        setActive(btnWeek);

        btnDay.setOnClickListener(v -> {
            isDay = true;
            isWeek = false;
            setActive(btnDay);
            loadStats();
        });

        btnWeek.setOnClickListener(v -> {
            isDay = false;
            isWeek = true;
            setActive(btnWeek);
            loadStats();
        });

        btnMonth.setOnClickListener(v -> {
            isDay = false;
            isWeek = false;
            setActive(btnMonth);
            loadStats();
        });
    }

    private void setActive(TextView active) {

        TextView[] buttons = {btnDay, btnWeek, btnMonth};

        for (TextView btn : buttons) {

            btn.setTextColor(Color.parseColor("#94A3B8"));

            btn.setBackgroundResource(R.drawable.unselected_toggle);

        }

        active.setTextColor(Color.BLACK);

        active.setBackgroundResource(R.drawable.selected_toggle);

    }

    private void loadStats() {

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) return;

                    long total = 0;
                    int count = 0;
                    long highest = 0;

                    Map<String, Long> dateMap = new LinkedHashMap<>();

                    long todayTotal = 0;
                    long yesterdayTotal = 0;

                    Calendar now = Calendar.getInstance();

                    // 🔥 DAY START CALCULATION
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    long todayStart = cal.getTimeInMillis();

                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    long yesterdayStart = cal.getTimeInMillis();

                    SimpleDateFormat sdf =
                            new SimpleDateFormat("EEE\ndd", Locale.getDefault());

                    for (DocumentSnapshot doc : query) {

                        Long amountObj = doc.getLong("amount");
                        Long date = doc.getLong("date");

                        if (amountObj == null || date == null) continue;

                        Calendar docCal = Calendar.getInstance();
                        docCal.setTimeInMillis(date);

                        boolean include = false;

                        if (isDay) {
                            include = date >= todayStart;
                        }
                        else if (isWeek) {
                            include = now.get(Calendar.WEEK_OF_YEAR) == docCal.get(Calendar.WEEK_OF_YEAR)
                                    && now.get(Calendar.YEAR) == docCal.get(Calendar.YEAR);
                        }
                        else {
                            include = now.get(Calendar.MONTH) == docCal.get(Calendar.MONTH)
                                    && now.get(Calendar.YEAR) == docCal.get(Calendar.YEAR);
                        }

                        if (!include) continue;

                        long amount = amountObj;

                        total += amount;
                        count++;

                        if (amount > highest) highest = amount;

                        // DATE
                        String day = sdf.format(new Date(date));
                        long oldDate = dateMap.containsKey(day) ? dateMap.get(day) : 0L;
                        dateMap.put(day, oldDate + amount);


                        // TODAY / YESTERDAY
                        if (date >= todayStart) {
                            todayTotal += amount;
                        } else if (date >= yesterdayStart && date < todayStart) {
                            yesterdayTotal += amount;
                        }
                    }

                    // 🔥 TEXT
                    tvTotalSpent.setText("₹" + total);
                    long diff = todayTotal - yesterdayTotal;

                    if(diff > 0){

                        tvGrowth.setText("📈 ₹"+diff+" more than yesterday");

                    }
                    else if(diff < 0){

                        tvGrowth.setText("📉 ₹"+Math.abs(diff)+" less than yesterday");

                    }
                    else {

                        tvGrowth.setText("⚖ Same as yesterday");

                    }


                    // 🔥 LINE CHART
                    ArrayList<Entry> entries = new ArrayList<>();
                    int i = 0;
                    for (Long val : dateMap.values()) {
                        entries.add(new Entry(i++, val));
                    }

                    LineDataSet lineSet = new LineDataSet(entries, "Daily");
                    lineSet.setColor(Color.GREEN);
                    lineSet.setCircleColor(Color.WHITE);
                    lineSet.setLineWidth(3f);
                    lineSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                    lineSet.setDrawFilled(true);
                    lineSet.setFillAlpha(80);
                    lineSet.setDrawValues(false);
                    lineSet.setCircleRadius(4f);
                    lineSet.setHighLightColor(Color.WHITE);
                    lineSet.setHighlightLineWidth(1f);
                    lineSet.setFillColor(Color.parseColor("#00E676"));

                    lineChart.setData(new LineData(lineSet));
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setEnabled(false);

                    lineChart.getAxisRight().setEnabled(false);
                    lineChart.getXAxis().setTextColor(Color.WHITE);
                    lineChart.getAxisLeft().setTextColor(Color.WHITE);

                    lineChart.setTouchEnabled(true);
                    lineChart.animateX(1200);

// 👇 YAHAN SE ADD KARO
                    ArrayList<String> labels = new ArrayList<>();

                    for (String day : dateMap.keySet()) {
                        labels.add(day);
                    }

                    XAxis xAxis = lineChart.getXAxis();

                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                    lineChart.invalidate();

                    // 🔥 BAR CHART (REAL COMPARISON)
                    ArrayList<BarEntry> bars = new ArrayList<>();
                    bars.add(new BarEntry(0, yesterdayTotal));
                    bars.add(new BarEntry(1, todayTotal));

                    BarDataSet barSet = new BarDataSet(bars, "Today vs Yesterday");
                    barSet.setColors(
                            Color.parseColor("#64748B"),
                            Color.parseColor("#00E676")
                    );

                    barChart.setData(new BarData(barSet));
                    barChart.getDescription().setEnabled(false);
                    barChart.animateY(1000);

                    tvHighestExpense.setText("₹" + highest);

                    if (count > 0) {
                        tvAverageSpend.setText("₹" + (total / count));
                    } else {
                        tvAverageSpend.setText("₹0");
                    }

                    tvTransactionCount.setText(String.valueOf(count));
                    tvActiveDays.setText(String.valueOf(dateMap.size()));


                })
                .addOnFailureListener(e -> Log.e("STATS_ERROR", e.getMessage()));
    }
}