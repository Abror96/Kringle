package com.example.kringle.kringle;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.example.kringle.kringle.adapter.TransactionsAdapter;
import com.example.kringle.kringle.model.Transactions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    List<Transactions> sample_list = new ArrayList<>();

    @BindView(R.id.scan_qr_code)
    ConstraintLayout scan_qr_code;

    @BindView(R.id.transactions_recycler)
    RecyclerView transactions_recycler;

    TransactionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        scan_qr_code.setOnClickListener(this);

        transactions_recycler.setHasFixedSize(true);
        transactions_recycler.setLayoutManager(new LinearLayoutManager(this));

        scaryThing();

        adapter = new TransactionsAdapter(getApplicationContext(), sample_list);
        transactions_recycler.setAdapter(adapter);

    }

    private void scaryThing() {
        sample_list.clear();

        Transactions transactions = new Transactions();
        transactions.setTransaction_name("Transation 1");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 2");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 3");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 4");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 5");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 6");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 7");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 8");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 9");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transation 10");
        sample_list.add(transactions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent().getStringExtra("response") != null) {
            String response = getIntent().getStringExtra("response");
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_qr_code:
                Intent loadQRIntent = new Intent(this, LoadQrCode.class);
                startActivity(loadQRIntent);
                break;
        }
    }
}
