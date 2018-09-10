package com.example.kringle.kringle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.kringle.kringle.adapter.TransactionsAdapter;
import com.example.kringle.kringle.model.Account;
import com.example.kringle.kringle.model.QrCode;
import com.example.kringle.kringle.model.Transactions;
import com.example.kringle.kringle.model.TransactionsRequest;
import com.example.kringle.kringle.model.TransactionsResponse;
import com.example.kringle.kringle.retrofit.IAccount;
import com.example.kringle.kringle.retrofit.ITransactions;
import com.example.kringle.kringle.retrofit.RetrofitClient;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private List<Transactions> sample_list = new ArrayList<>();

    @BindView(R.id.scan_qr_code)
    ConstraintLayout scan_qr_code;

    @BindView(R.id.account_info)
    ConstraintLayout account_info_layout;

    @BindView(R.id.transactions_recycler)
    RecyclerView transactions_recycler;


    private TransactionsAdapter adapter;

    private Retrofit retrofit;

    // Interfaces
    private ITransactions iTransactions;
    private IAccount iAccount;

    // saving data
    SharedPreferences accountPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        scan_qr_code.setOnClickListener(this);

        accountPrefs = getSharedPreferences("AccountData", MODE_PRIVATE);

        // checking whether the user is authorized
        isAuthorized();

        transactions_recycler.setHasFixedSize(true);
        transactions_recycler.setLayoutManager(new LinearLayoutManager(this));

        scaryThing();

        adapter = new TransactionsAdapter(getApplicationContext(), sample_list);
        transactions_recycler.setAdapter(adapter);

        // init api
        retrofit = RetrofitClient.getInstance();

    }

    private void isAuthorized() {
        String token = accountPrefs.getString("token", null);
        int id = accountPrefs.getInt("id", 0);

        // if user is authorized
        if(token != null && id != 0) {
            Toast.makeText(getApplicationContext(), "Authorized", Toast.LENGTH_LONG).show();
            scan_qr_code.setVisibility(View.GONE);
            account_info_layout.setVisibility(View.VISIBLE);
        }
        // if user isn't authorized
        else {
            Toast.makeText(getApplicationContext(), "Not authorized", Toast.LENGTH_LONG).show();
            account_info_layout.setVisibility(View.GONE);
            scan_qr_code.setVisibility(View.VISIBLE);
        }
    }

    private void getAccountData(int id, String token) {
        iAccount = retrofit.create(IAccount.class);

        Call<Account> accountCall = iAccount.getAccountData(
                createKey(id, token),
                id,
                currentTimestamp(),
                0,
                10

        );

        accountCall.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                int statusCode = response.code();
                Log.d("LOGGER", "response code: " + statusCode);

                Account accountResponse = response.body();
                Log.d("LOGGER",
                        statusCode + "\n" +
                                accountResponse.getStatus() + "\n" +
                                accountResponse.getData().get(0).getEmail() + "\n" +
                                accountResponse.getData().get(0).getBalance() + "\n" +
                                accountResponse.getData().get(0).getLast_transaction_timestamp() + "\n"
                );
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void scaryThing() {
        sample_list.clear();

        Transactions transactions = new Transactions();
        transactions.setTransaction_name("Transaction 1");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 2");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 3");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 4");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 5");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 6");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 7");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 8");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 9");
        sample_list.add(transactions);

        transactions = new Transactions();
        transactions.setTransaction_name("Transaction 10");
        sample_list.add(transactions);
    }

    private void parseResponseJson(String response) {
        Gson gson = new Gson();
        QrCode resp = gson.fromJson(response, QrCode.class);

        // getting account data
        getAccountData(resp.getId(), resp.getApi_token());

        // saving account id and token
        saveAccountData(resp.getId(), resp.getApi_token());

    }

    private String createKey(int id, String token) {

        int timestamp_str = currentTimestamp();


        Map<String, String> map = new HashMap<>();

        map.put("id", String.valueOf(id));
        map.put("timestamp", String.valueOf(timestamp_str));
        map.put("start", "0");
        map.put("limit", "10");

        TreeMap<String, String> sortedMap = new TreeMap<>(map);

        String auth_signature = "";

        for (String value : sortedMap.values()) {
            auth_signature += value;
        }

        auth_signature = auth_signature + token;

        return Hashing.sha256().hashString(auth_signature, StandardCharsets.UTF_8).toString();
    }

    private int currentTimestamp() {
        // current timestamp
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return (int) (timestamp.getTime()/1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_qr_code:
                Intent loadQRIntent = new Intent(this, LoadQrCode.class);
                startActivityForResult(loadQRIntent, LoadQrCode.REQUEST_CODE_FOR_INTENT);
                break;
        }
    }

    // saving id and token of account
    private void saveAccountData(int id, String token) {
        SharedPreferences.Editor editor = getSharedPreferences("AccountData", MODE_PRIVATE).edit();
        editor.putInt("id", id);
        editor.putString("token", token);
        editor.apply();
    }

    // handle returning from LoadQrCode.java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == LoadQrCode.REQUEST_CODE_FOR_INTENT) {
            if (data.getStringExtra("response") != null) {
                String response = data.getStringExtra("response");
                parseResponseJson("{"+response+"}");

                // checking whether the user is authorized
                isAuthorized();
            }
        }
    }
}
