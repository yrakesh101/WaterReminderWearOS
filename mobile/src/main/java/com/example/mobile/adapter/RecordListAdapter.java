package com.example.mobile.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobile.databinding.RecordListLayoutBinding;
import com.example.mobile.entity.Record;

import java.util.List;

public class RecordListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;

    private static final int VIEW_TYPE_ITEM = 1;

    private List<Record> recordList;

    private View headerView;

    RecordListLayoutBinding recordListLayoutBinding;

    public RecordListAdapter(View headerView, List<Record> recordList, Context context) {
        super();
        this.headerView = headerView;
        this.recordList = recordList;
    }

    // Override getItemViewType to specify view type
    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(headerView);
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            recordListLayoutBinding =  RecordListLayoutBinding.inflate(layoutInflater, parent, false);
            return new DataViewHolder(recordListLayoutBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            // Bind data to your data view holder
            DataViewHolder dataHolder = (DataViewHolder) holder;
            dataHolder.bindView(recordList.get(position - 1));
        }
    }

    @Override
    public int getItemCount() {
        return recordList.size() + 1;
    }


    // Create a view holder for the header
    private static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class DataViewHolder extends RecyclerView.ViewHolder {

        RecordListLayoutBinding recordListLayoutBinding;

        /**
         * init view holder
         * @param recordListLayoutBinding
         */
        public DataViewHolder(RecordListLayoutBinding recordListLayoutBinding) {
            super(recordListLayoutBinding.getRoot());
            this.recordListLayoutBinding = recordListLayoutBinding;
        }

        /**
         * bind data to a row
         * @param record
         */
        public void bindView(Record record) {
            recordListLayoutBinding.listRecordTime.setText(record.getTime());
            recordListLayoutBinding.listRecordAmount.setText(record.getAmount().toString());
            Log.d("bindView", "bindView");
        }
    }
}
