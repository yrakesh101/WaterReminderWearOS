package com.example.mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mobile.entity.Record;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RecordSharedPrefUtil {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String saveRecord(Integer amount, Context context) {
        String recordId = "";
        SharedPreferences sharedPreferences = context.getSharedPreferences("RECORD_LIST", Context.MODE_PRIVATE);
        Map<String, ?> map = sharedPreferences.getAll();
        recordId = String.valueOf(map.size() + 1);
        String dateTime = dateFormat.format(new Date());
        String recordJson = new Gson().toJson(new Record(recordId, dateTime, amount));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(recordId, recordJson);
        editor.apply();
        return recordId;
    }

    public static List<Record> getAllRecords(String queryData, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("RECORD_LIST", Context.MODE_PRIVATE);
        List<Record> appointmentList = new ArrayList<>();
        Map<String, ?> map = sharedPreferences.getAll();
        Set set = map.entrySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String recordJson = (String) entry.getValue();
            if (recordJson != null) {
                Record record = new Gson().fromJson(recordJson, Record.class);
                if(record.getTime().startsWith(queryData)) {
                    appointmentList.add(record);
                }
            }
        }
        return appointmentList;
    }
}