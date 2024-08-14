package com.example.mobile.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.R;
import com.example.mobile.adapter.RecordListAdapter;
import com.example.mobile.databinding.ActivityMobileMainBinding;
import com.example.mobile.entity.Record;
import com.example.mobile.util.RecordSharedPrefUtil;
import com.example.shared.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MobileMainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener, View.OnClickListener {

    private ActivityMobileMainBinding binding;
    private int waterIntake = 0;
    private SharedPreferences sharedPreferences;

    DatePickerDialog datePickerDialog;

    RecordListAdapter recordListAdapter;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String queryData;
    List<Record> recordList = new ArrayList<Record>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("WaterIntakePrefs", MODE_PRIVATE);
        waterIntake = sharedPreferences.getInt("waterIntake", 0);

        updateWaterIntakeDisplay();

        binding.selectedDate.setInputType(InputType.TYPE_NULL);
        binding.selectedDate.setOnClickListener(this);

        binding.addWaterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addWater(Constants.WATER_INCREMENT);
            }
        });

        binding.syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncData();
            }
        });

        init();
    }

    /**
     * get passed data
     */
    private void init() {
        binding.querywaterbtn.setOnClickListener(this);
        queryData = dateFormat.format(new Date());
        binding.selectedDate.setText(queryData);
        bindAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.getDataClient(this).removeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.getDataClient(this).removeListener(this);
    }

    private void addWater(int amount) {
        waterIntake += amount;
        updateWaterIntakeDisplay();
        saveWaterIntake();
        syncData();
        Toast.makeText(this, "Water added!", Toast.LENGTH_SHORT).show();
    }

    private void updateWaterIntakeDisplay() {
        binding.waterIntakeText.setText(String.format("%d ml", waterIntake));
    }

    private void syncData() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.WATER_INTAKE_PATH);
        putDataMapReq.getDataMap().putInt(Constants.KEY_WATER_INTAKE, waterIntake);
        putDataMapReq.getDataMap().putLong("timestamp", System.currentTimeMillis());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();

        Task<DataItem> putDataTask = Wearable.getDataClient(this).putDataItem(putDataReq);

        putDataTask.addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                Log.d("MobileMainActivity", "Data synced successfully: " + dataItem.getUri());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MobileMainActivity.this, "Data synced!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        putDataTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MobileMainActivity", "Data sync failed: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MobileMainActivity.this, "Sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void saveWaterIntake() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("waterIntake", waterIntake);
        editor.apply();
    }

    @Override
    public void onDataChanged(@NonNull DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals(Constants.WATER_INTAKE_PATH)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(item);
                    DataMap dataMap = dataMapItem.getDataMap();
                    final int newWaterIntake = dataMap.getInt(Constants.KEY_WATER_INTAKE);
                    Log.d("MainActivity", "Received new water intake: " + newWaterIntake);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            int thisTimeWater = newWaterIntake - waterIntake;
                            waterIntake = newWaterIntake;
                            updateWaterIntakeDisplay();
                            saveWaterIntake();
                            RecordSharedPrefUtil.saveRecord(thisTimeWater, getApplicationContext());
                            updateRecordAdapter();
                            Toast.makeText(MobileMainActivity.this, "Water intake updated: " + newWaterIntake + " ml", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == binding.selectedDate.getId()) {
            // show datepick dialog, and set it to today
            Calendar calendar = Calendar.getInstance();
            int dayOfSales = calendar.get(Calendar.DAY_OF_MONTH);
            int monthOfSales = calendar.get(Calendar.MONTH);
            int yearOfSales = calendar.get(Calendar.YEAR);
            datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int day) {
                    int intMonth = month + 1;
                    String finalMonth;
                    if(intMonth >= 10) {
                        finalMonth = String.valueOf(intMonth);
                    } else {
                        finalMonth = "0" + String.valueOf(intMonth);
                    }
                    binding.selectedDate.setText(year + "-" + finalMonth + "-" + day);
                }
            }, yearOfSales, monthOfSales, dayOfSales);
            datePickerDialog.show();
        } else if(v.getId() == binding.querywaterbtn.getId()) {
            // query water intake for the selected date
            queryData = binding.selectedDate.getText().toString();
            updateRecordAdapter();
        }
    }

    private void updateRecordAdapter() {
        recordList.clear();
        bindAdapter();
    }

    private void bindAdapter() {
        recordList = RecordSharedPrefUtil.getAllRecords(queryData, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.listRecordRecyclerView.setLayoutManager(layoutManager);
        View headerView = LayoutInflater.from(this).inflate(R.layout.record_list_layout, binding.listRecordRecyclerView, false);
        recordListAdapter = new RecordListAdapter(headerView, recordList, this);
        binding.listRecordRecyclerView.setAdapter(recordListAdapter);
        recordListAdapter.notifyDataSetChanged();
    }
}