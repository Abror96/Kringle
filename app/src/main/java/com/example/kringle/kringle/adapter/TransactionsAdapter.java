package com.example.kringle.kringle.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private static int currentPosition = -1;
    private boolean isOpened = false;
    private int oldPosition;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

        holder.transaction_date.setText(getDate(list.get(position).getTimestamp()));
        holder.transaction_amount.setText(
                String.format("%s%s", isIncoming(list.get(position).getIncoming()),
                        String.valueOf(list.get(position).getAmount())));
        holder.transaction_currency.setText("Kringle");
        holder.transaction_status.setText(String.valueOf(list.get(position).getStatus()));
        holder.address_from.setText(String.valueOf(list.get(position).getAddress_from()));
        holder.address_to.setText(String.valueOf(list.get(position).getAddress_to()));
        if (list.get(position).getIncoming() == 1)
            holder.transaction_icon.setBackgroundResource(R.drawable.ic_in);
        else
            holder.transaction_icon.setBackgroundResource(R.drawable.ic_out);

        holder.hidden_linearLayout.setVisibility(View.GONE);

        if (currentPosition == position) {
            if (oldPosition == currentPosition) {
                if (!isOpened) {
                    //creating an animation
                    Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                    //toggling visibility
                    holder.hidden_linearLayout.setVisibility(View.VISIBLE);
                    //adding sliding effect
                    holder.hidden_linearLayout.startAnimation(slideDown);
                    isOpened = true;
                } else {
                    //creating an animation
                    Animation slide_up = AnimationUtils.loadAnimation(context, R.anim.slide_up);
                    //toggling visibility
                    holder.hidden_linearLayout.setVisibility(View.GONE);
                    //adding sliding effect
                    holder.hidden_linearLayout.startAnimation(slide_up);
                    isOpened = false;
                }
            } else {
                Animation slideDown = AnimationUtils.loadAnimation(context, R.anim.slide_down);
                //toggling visibility
                holder.hidden_linearLayout.setVisibility(View.VISIBLE);
                //adding sliding effect
                holder.hidden_linearLayout.startAnimation(slideDown);
                isOpened = true;
            }


        }

        holder.ll_transaction_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oldPosition = currentPosition;
                currentPosition = position;
                notifyDataSetChanged();
            }
        });

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
        @BindView(R.id.address_from)
        TextView address_from;
        @BindView(R.id.address_to)
        TextView address_to;
        @BindView(R.id.linearLayout)
        LinearLayout hidden_linearLayout;
        @BindView(R.id.transaction_item)
        LinearLayout ll_transaction_item;
        @BindView(R.id.transaction_icon)
        ImageView transaction_icon;


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