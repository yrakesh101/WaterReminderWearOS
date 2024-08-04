package com.example.mobile.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobile.databinding.ActivityMobileMainBinding;
import com.example.mobile.services.SyncService;
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

public class MobileMainActivity extends AppCompatActivity implements DataClient.OnDataChangedListener {

    private ActivityMobileMainBinding binding;
    private int waterIntake = 0;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMobileMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("WaterIntakePrefs", MODE_PRIVATE);
        waterIntake = sharedPreferences.getInt("waterIntake", 0);

        updateWaterIntakeDisplay();

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
                            waterIntake = newWaterIntake;
                            updateWaterIntakeDisplay();
                            saveWaterIntake();
                            Toast.makeText(MobileMainActivity.this, "Water intake updated: " + newWaterIntake + " ml", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }
}