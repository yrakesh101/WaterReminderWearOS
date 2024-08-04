package com.example.mobile.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.example.shared.utils.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class SyncService extends JobIntentService {

    private static final int JOB_ID = 1000;

    public static void startSync(Context context, int waterIntake) {
        Intent intent = new Intent();
        intent.putExtra(Constants.KEY_WATER_INTAKE, waterIntake);
        enqueueWork(context, SyncService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        int waterIntake = intent.getIntExtra(Constants.KEY_WATER_INTAKE, 0);
        syncWaterIntakeData(waterIntake);
    }

    private void syncWaterIntakeData(int waterIntake) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Constants.WATER_INTAKE_PATH);
        putDataMapReq.getDataMap().putInt(Constants.KEY_WATER_INTAKE, waterIntake);
        putDataMapReq.getDataMap().putLong("timestamp", System.currentTimeMillis());
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest().setUrgent();
        Wearable.getDataClient(this).putDataItem(putDataReq).addOnSuccessListener(new OnSuccessListener<DataItem>() {
            @Override
            public void onSuccess(DataItem dataItem) {
                // Save the synced data
                SharedPreferences sharedPreferences = getSharedPreferences("WaterIntakePrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("lastSyncedWaterIntake", waterIntake);
                editor.apply();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e("SyncService", "Sync failed: " + e.getMessage());
            }
        });
    }

}