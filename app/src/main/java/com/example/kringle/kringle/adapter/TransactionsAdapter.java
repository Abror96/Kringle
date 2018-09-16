package com.example.kringle.kringle.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kringle.kringle.R;
import com.example.kringle.kringle.model.TransactionsResponseData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.ViewHolder> {

    private Context context;
    private List<TransactionsResponseData> list;

    public TransactionsAdapter(Context context, List<TransactionsResponseData> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.transaction_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.transaction_date.setText(getDate(list.get(position).getTimestamp()));
        holder.transaction_amount.setText(
                String.format("%s%s", isIncoming(list.get(position).getIncoming()),
                        String.valueOf(list.get(position).getAmount())));
        holder.transaction_currency.setText("Kringle");
        holder.transaction_status.setText(String.valueOf(list.get(position).getStatus()));
    }

    private String isIncoming(int num) {
        if (num == 1) return "+";
        else return "-";
    }

    private String getDate(long timestamp) {
        Date date = new Date((long)timestamp*1000);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return String.valueOf(dateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_transaction_date)
        TextView transaction_date;
        @BindView(R.id.tv_transaction_amount)
        TextView transaction_amount;
        @BindView(R.id.tv_transaction_currency)
        TextView transaction_currency;
        @BindView(R.id.tv_transaction_status)
        TextView transaction_status;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void add(List<TransactionsResponseData> transactions) {
        list.addAll(transactions);
        notifyDataSetChanged();
    }

}
