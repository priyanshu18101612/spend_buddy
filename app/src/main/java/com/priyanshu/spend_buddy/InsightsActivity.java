package com.priyanshu.spend_buddy;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.*;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class InsightsActivity extends AppCompatActivity {

    TextView topCategory, avgSpend, totalSpend, totalTransactions, insightText;
    TextView tvWeekTotal, tvWeekAverage, tvWeekTransactions, tvWeekTopCategory;
    PieChart pieChart;
    LineChart lineChart;

    FirebaseFirestore db;
    String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insights);

        // 🔹 UI Bind
        topCategory = findViewById(R.id.topCategory);
        avgSpend = findViewById(R.id.avgSpend);
        totalSpend = findViewById(R.id.totalSpend);
        totalTransactions = findViewById(R.id.totalTransactions);

        pieChart = findViewById(R.id.pieChart);
        lineChart= findViewById(R.id.lineChart);
        insightText= findViewById(R.id.insightText);
        tvWeekTotal = findViewById(R.id.tvWeekTotal);
        tvWeekAverage = findViewById(R.id.tvWeekAverage);
        tvWeekTransactions = findViewById(R.id.tvWeekTransactions);
        tvWeekTopCategory = findViewById(R.id.tvWeekTopCategory);
        db = FirebaseFirestore.getInstance();

        loadInsights();
    }

    private void loadInsights() {

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(query -> {

                    Log.d("INSIGHTS", "Docs: " + query.size());

                    if (query.isEmpty()) {
                        Log.d("INSIGHTS", "No data ❌");
                        return;
                    }

                    long total = 0;
                    int count = 0;

                    Map<String, Long> categoryMap = new HashMap<>();
                    Map<String, Long> dateMap = new LinkedHashMap<>();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd", Locale.getDefault());

                    for (DocumentSnapshot doc : query) {

                        String category = doc.getString("category");
                        Long amount = doc.getLong("amount");
                        Long date = doc.getLong("date");

                        if (category == null) category = "Other";
                        if (amount == null) continue;

                        total += amount;
                        count++;

                        // 📊 Category Map
                        long old = categoryMap.containsKey(category) ? categoryMap.get(category) : 0L;
                        categoryMap.put(category, old + amount);

                        // 📅 Date Map (FIXED)
                        if (date != null) {
                            String day = sdf.format(new Date(date));

                            long oldVal = dateMap.containsKey(day) ? dateMap.get(day) : 0L;
                            dateMap.put(day, oldVal + amount);
                        }
                    }

                    Log.d("CATEGORY_MAP", categoryMap.toString());
                    Log.d("DATE_MAP", dateMap.toString());

                    // ================= CARDS =================

                    totalSpend.setText("₹" + total);
                    totalTransactions.setText(String.valueOf(count));

                    if (count > 0) {
                        avgSpend.setText("₹" + (total / count));
                    }

                    // 🏆 Top Category
                    String topCat = "None";
                    long max = 0;

                    for (String key : categoryMap.keySet()) {
                        if (categoryMap.get(key) > max) {
                            max = categoryMap.get(key);
                            topCat = key;
                        }
                    }

                    topCategory.setText(topCat + " ₹" + max);
                    tvWeekTotal.setText("₹" + total);

                    if (count > 0) {
                        tvWeekAverage.setText("₹" + (total / count));
                    } else {
                        tvWeekAverage.setText("₹0");
                    }

                    tvWeekTransactions.setText(String.valueOf(count));

                    tvWeekTopCategory.setText(topCat);


                    // ================= PIE CHART =================

                    ArrayList<PieEntry> pieEntries = new ArrayList<>();

                    for (String key : categoryMap.keySet()) {
                        pieEntries.add(new PieEntry(categoryMap.get(key), key));
                    }

                    if (pieEntries.isEmpty()) {
                        pieChart.setNoDataText("No data");
                        pieChart.invalidate();
                    } else {
                        PieDataSet pieDataSet = new PieDataSet(pieEntries, "");
                        ArrayList<Integer> colors = new ArrayList<>();

                        colors.add(Color.parseColor("#00E676")); // Green
                        colors.add(Color.parseColor("#3B82F6")); // Blue
                        colors.add(Color.parseColor("#A855F7")); // Purple
                        colors.add(Color.parseColor("#FACC15")); // Yellow
                        colors.add(Color.parseColor("#F97316")); // Orange

                        pieDataSet.setColors(colors);
                        pieDataSet.setSliceSpace(4f);
                        pieDataSet.setSelectionShift(8f);
                        pieDataSet.setValueTextColor(Color.WHITE);
                        pieDataSet.setValueTextSize(12f);

                        PieData pieData = new PieData(pieDataSet);
                        pieChart.setData(pieData);
                        pieChart.getDescription().setEnabled(false);
                        pieChart.animateY(1000);
                        pieChart.invalidate();
                        pieChart.setDrawHoleEnabled(true);
                        pieChart.setHoleRadius(60f);
                        pieChart.setTransparentCircleRadius(65f);
                        pieChart.setUsePercentValues(false);

                        pieChart.setRotationEnabled(true);

                        pieChart.setDragDecelerationFrictionCoef(0.95f);

                        pieChart.setExtraOffsets(10f,10f,10f,10f);

                        pieChart.setDrawEntryLabels(false);

                        pieChart.setEntryLabelColor(Color.WHITE);
                        pieChart.setCenterText("₹" + totalSpend + "\nTotal Spend");
                        pieChart.setCenterTextColor(Color.WHITE);
                        pieChart.setCenterTextSize(20f);
                        pieChart.setCenterTextTypeface(ResourcesCompat.getFont(this, R.font.poppins_bold));

                        pieChart.getDescription().setEnabled(false);
                        pieChart.getLegend().setTextColor(Color.WHITE);
                    }

                    // ================= BAR CHART =================
                    ArrayList<Entry> entries = new ArrayList<>();

                    int index = 0;

                    TreeMap<String, Long> sortedMap = new TreeMap<>(dateMap);
                    ArrayList<String> labels = new ArrayList<>();

                    for (Map.Entry<String, Long> entry : sortedMap.entrySet()) {

                        entries.add(new Entry(index, entry.getValue()));

                        labels.add(entry.getKey());

                        index++;
                    }

                    if(entries.isEmpty()){

                        lineChart.setNoDataText("No Data");

                        lineChart.invalidate();

                    }
                    else{

                        LineDataSet dataSet = new LineDataSet(entries,"Daily Spend");
                        dataSet.setColor(Color.parseColor("#00E676"));
                        dataSet.setCircleColor(Color.WHITE);
                        dataSet.setCircleRadius(5f);
                        dataSet.setLineWidth(3f);
                        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                        dataSet.setDrawFilled(true);
                        dataSet.setFillColor(Color.parseColor("#00E676"));
                        dataSet.setFillAlpha(60);
                        dataSet.setValueTextColor(Color.WHITE);
                        dataSet.setValueTextSize(11f);
                        LineData lineData = new LineData(dataSet);
                        lineChart.setData(lineData);

                    }
                    lineChart.getDescription().setEnabled(false);
                    lineChart.getLegend().setTextColor(Color.WHITE);
                    lineChart.getAxisRight().setEnabled(false);
                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextColor(Color.WHITE);
                    xAxis.setGranularity(1f);
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setDrawGridLines(false);

                    YAxis leftAxis = lineChart.getAxisLeft();
                    leftAxis.setTextColor(Color.WHITE);
                    leftAxis.setAxisMinimum(0);
                    leftAxis.setDrawGridLines(true);
                    leftAxis.setGridColor(Color.parseColor("#40FFFFFF"));

                    lineChart.animateX(1200);

                    lineChart.animateY(1200);

                    lineChart.invalidate();

                    String insight;

                    String topCategoryName = topCat;

// No Expenses
                    if (count == 0) {

                        insight = "🎉 Great! No expenses recorded yet. Keep saving today!";

                    }

// Food
                    else if (topCategoryName.equalsIgnoreCase("Food")) {

                        insight = "🍔 Food is your biggest expense. Cooking at home could help you save more.";

                    }

// Shopping
                    else if (topCategoryName.equalsIgnoreCase("Shopping")) {

                        insight = "🛍 Shopping expenses are high. Avoid impulse purchases this week.";

                    }

// Travel
                    else if (topCategoryName.equalsIgnoreCase("Travel")) {

                        insight = "🚌 Travel costs are increasing. Consider public transport to save money.";

                    }

// Very Low Spending
                    else if (total < 200) {

                        insight = "🌟 Excellent! Your spending is well under control today.";

                    }

// High Spending
                    else if (total > 1000) {

                        insight = "💸 You've spent quite a lot today. Review your expenses before making another purchase.";

                    }

// Average Spending
                    else {

                        insight = "✅ Your spending looks balanced. Keep maintaining healthy financial habits.";

                    }

                    insightText.setText(insight);




                })
                .addOnFailureListener(e -> {
                    Log.e("INSIGHTS_ERROR", e.getMessage());
                });
    }
}