package com.example.kringle.kringle;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kringle.kringle.adapter.TransactionsAdapter;
import com.example.kringle.kringle.model.Account;
import com.example.kringle.kringle.model.ExchangeRate;
import com.example.kringle.kringle.model.ExchangeRateData;
import com.example.kringle.kringle.model.LogOut;
import com.example.kringle.kringle.model.QrCode;
import com.example.kringle.kringle.model.TransactionsPostResponse;
import com.example.kringle.kringle.model.TransactionsResponse;
import com.example.kringle.kringle.model.TransactionsResponseData;
import com.example.kringle.kringle.retrofit.IAccount;
import com.example.kringle.kringle.retrofit.IExchangeRate;
import com.example.kringle.kringle.retrofit.ILogOut;
import com.example.kringle.kringle.retrofit.ITransactions;
import com.example.kringle.kringle.retrofit.ITransactionsPost;
import com.example.kringle.kringle.retrofit.RetrofitClient;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TransactionsAddDialog.TransactionsAddDialogListener {

    private List<TransactionsResponseData> transactionsList = new ArrayList<>();

    @BindView(R.id.preloader)
    LinearLayout preloader;

    @BindView(R.id.main_layout)
    ConstraintLayout main_layout;

    @BindView(R.id.scan_qr_code)
    ConstraintLayout scan_qr_code;

    @BindView(R.id.account_info)
    ConstraintLayout account_info_layout;

    @BindView(R.id.transactions_recycler)
    RecyclerView transactions_recycler;

    @BindView(R.id.tv_account_email)
    TextView tv_account_email;

    @BindView(R.id.tv_account_balance)
    TextView tv_account_balance;

//    @BindView(R.id.exchange_rate)
//    TextView tv_exchange_rate;

    @BindView(R.id.btn_transaction_add)
    FloatingActionButton btn_transaction_add;

    @BindView(R.id.app_version)
    TextView app_version;


    Snackbar bar;

    private TransactionsAdapter adapter;

    private Retrofit retrofit;

    private LinearLayoutManager layoutManager;

    // Interfaces
    private ITransactions iTransactionsResponse;
    private IAccount iAccount;
    private IExchangeRate iExchangeRate;
    private ITransactionsPost iTransactionsPost;
    private ILogOut iLogOut;

    // saving data
    private SharedPreferences accountPrefs;

    // user data variables
    private String userEmail;
    private String userBalance;
    private String userLastTransactionTimestamp;

    // exchange rates
    public double usd_cur = 0;
    public double eur_cur = 0;

    // timer
    Timer timer;

    // limit of the list of transactions
    int start = 0;
    int limit = 10;

    // variables for pagination
    private boolean isLoading = true;
    private int pastVisibleItems, visibleItemCount, totalItemCount, previous_total = 0;

    // menu item
    MenuItem menu_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Dashboard");
        getSupportActionBar().hide();

        // getting version of app
        app_version.setText("v. " + getVersion());

        preloader.setVisibility(View.VISIBLE);

        scan_qr_code.setOnClickListener(this);
        btn_transaction_add.setOnClickListener(this);

        btn_transaction_add.setVisibility(View.GONE);

        accountPrefs = getSharedPreferences("AccountData", MODE_PRIVATE);

        // init recycler
        transactions_recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        transactions_recycler.setLayoutManager(layoutManager);

        // init api
        retrofit = RetrofitClient.getInstance();

        timer = new Timer();


        // checking whether the user is authorized
        isAuthorized();

        transactions_recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();


                if (!recyclerView.canScrollVertically(1)) {

                    if (dy > 0) {
                        if (isLoading) {
                            if (totalItemCount > previous_total) {
                                isLoading = false;
                                previous_total = totalItemCount;
                            }
                        }
                        if (!isLoading
                                && (totalItemCount-visibleItemCount) <= (pastVisibleItems+10)) {

                            limit += 10;
                            loadMoreDataToRecycler();
                            isLoading = true;
                        }
                    }
                }
            }
        });
    }

    private String getVersion() {
        String version = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    // handle returning from LoadQrCode.java
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == LoadQrCode.REQUEST_CODE_FOR_INTENT && data != null) {
            if (data.getStringExtra("response") != null) {
                String response = data.getStringExtra("response");
                parseResponseJson("{"+response+"}");

                showLoadingData();
            }
        }
    }

    private void isAuthorized() {

        final String token = accountPrefs.getString("token", null);
        final int id = accountPrefs.getInt("id", 0);

        // if user is authorized
        if(token != null && id != 0) {

            // getting account data with saved id and token
            getAccountData(id, token);

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    getAccountData(id, token);
                    Log.d("LOGGER", "Data is updated");
                }
            }, 300000, 300000);
        } else {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // toggle visibility of views
                            toggleVisibilityOfViews(id, token);


                            preloader.setVisibility(View.GONE);
                            getSupportActionBar().show();
                            if (bar != null)
                                bar.dismiss();
                        }
                    }, 1500);
        }
    }

    // Getting Account Data
    private void getAccountData(final int id, final String token) {

        iAccount = retrofit.create(IAccount.class);

        Call<Account> accountCall = iAccount.getAccountData(
                createKey(id, token, "10", "0"),
                id,
                currentTimestamp(),
                0,
                10

        );

        accountCall.enqueue(new Callback<Account>() {

            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                int statusCode = response.code();
                Log.d("LOGGER Account", "response code: " + statusCode);

                Account accountResponse = response.body();

                if (accountResponse.getStatus().equals("ok")) {
                    userEmail = accountResponse.getData().get(0).getEmail();
                    userBalance = String.valueOf(accountResponse.getData().get(0).getBalance());
                    userLastTransactionTimestamp = String.valueOf(accountResponse.getData().get(0).getLast_transaction_timestamp());

                    // saving account data
                    saveAccountData(id, token);

                    // get transactions
                    getTransactionData(id, token);

                    // get exchange rate
                    getExchangeRate(id, token);

                    Log.d("LOGGER", "onResponse: usd: " + usd_cur );
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    double res = Integer.parseInt(userBalance) * usd_cur;
                                    String str_res = new DecimalFormat("##.##").format(res);
                                    tv_account_email.setText(userEmail);
                                    tv_account_balance.setText(userBalance + " Kringle ($" + str_res + ")");
                                }
                            }, 700);

                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    // toggle visibility of views
                                    toggleVisibilityOfViews(id, token);


                                    preloader.setVisibility(View.GONE);
                                    getSupportActionBar().show();
                                    if (bar != null)
                                        bar.dismiss();
                                }
                            }, 1500);




                } else {
                    Snackbar.make(main_layout, "Authorization error, please scan the actual QR-Code", Snackbar.LENGTH_LONG).show();

                    isAuthorized();
                }


            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                Log.d("Error", "onFailure: " + t.getMessage());
            }
        });
    }

    // getting last transactions
    private void getTransactionData(final int id, final String token) {

        iTransactionsResponse = retrofit.create(ITransactions.class);

        Call<TransactionsResponse> transactionsResponseCall = iTransactionsResponse.getTransactions(
                createKey(id, token, String.valueOf(limit), "0"),
                id,
                currentTimestamp(),
                0,
                limit
        );

        transactionsResponseCall.enqueue(new Callback<TransactionsResponse>() {
            @Override
            public void onResponse(Call<TransactionsResponse> call, Response<TransactionsResponse> response) {
                int statusCode = response.code();
                Log.d("LOGGER Transactions", "response code: " + statusCode);

                TransactionsResponse transactionsResponse = response.body();
                if (transactionsResponse.getStatus().equals("ok")) {
                    if (transactionsList.size() > 0) transactionsList.clear();

                    transactionsList.addAll(transactionsResponse.getData());

                    // pull data to recyclerView
                    fillRecyclerWithData();
                } else {
                    Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<TransactionsResponse> call, Throwable t) {
                Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                Log.d("LOGGER Transactions", "onFailure: " + t.getMessage());
            }
        });


    }

    // getting exchange rate
    private void getExchangeRate(final int id, final String token) {
        iExchangeRate = retrofit.create(IExchangeRate.class);

        Call<ExchangeRate> exchangeRateCall = iExchangeRate.getExchangeRate(
                createKey(id, token),
                id,
                currentTimestamp()
        );

        exchangeRateCall.enqueue(new Callback<ExchangeRate>() {
            @Override
            public void onResponse(Call<ExchangeRate> call, Response<ExchangeRate> response) {
                int statusCode = response.code();
                Log.d("LOGGER Exchangerate", "response code: " + statusCode);

                ExchangeRate exchangeRate = response.body();
                if (exchangeRate.getStatus().equals("ok")) {
                    ExchangeRateData exchangeRateData = exchangeRate.getData().get(0);
//                    tv_exchange_rate.setText("1 K = " + exchangeRateData.getUsd() + " $");

                    usd_cur = exchangeRateData.getUsd();
                    eur_cur = exchangeRateData.getEur();


                } else {
                    Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<ExchangeRate> call, Throwable t) {
                Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                Log.d("LOGGER Exchangerate", "onFailure: " + t.getMessage());
            }
        });
    }

    // posting transaction
    private void postTransactionsData(final String token, final int id, String address, int amount, String currency) {
        iTransactionsPost = retrofit.create(ITransactionsPost.class);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("id", String.valueOf(id))
                .addFormDataPart("timestamp", String.valueOf(currentTimestamp()))
                .addFormDataPart("address", address)
                .addFormDataPart("amount", String.valueOf(amount))
                .addFormDataPart("currency", currency)
                .build();

        Call<TransactionsPostResponse> transactionsPostResponseCall = iTransactionsPost.postTransactions(
                createKey(
                        id,
                        token,
                        address,
                        amount,
                        currency), requestBody
        );

        transactionsPostResponseCall.enqueue(new Callback<TransactionsPostResponse>() {
            @Override
            public void onResponse(Call<TransactionsPostResponse> call, Response<TransactionsPostResponse> response) {
                int statusCode = response.code();
                Log.d("LOGGER PostTransactions", "response code: " + statusCode);

                TransactionsPostResponse transactionsPostResponse = response.body();
                Log.d("LOGGER", "onResponse: " + transactionsPostResponse.getStatus());
                if (transactionsPostResponse.getStatus().equals("ok")) {
                    Snackbar.make(main_layout, "Transaction has been sent successfully", Snackbar.LENGTH_LONG).show();
                    new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                Log.d("LOGGER","Data is updated after posting transaction");
                                getAccountData(id, token);
                            }
                        }, 10000);
                } else {
                    Snackbar.make(main_layout, "Something goes wrong while posting data", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionsPostResponse> call, Throwable t) {
                Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                Log.d("LOGGER Transactions", "onFailure: " + t.getMessage());
            }
        });
    }

    // logging out
    private void logOut(final int id, final String token) {
        iLogOut = retrofit.create(ILogOut.class);

        Call<LogOut> logOutCall = iLogOut.logout(
                createKey(id, token),
                id,
                currentTimestamp()
        );

        logOutCall.enqueue(new Callback<LogOut>() {
            @Override
            public void onResponse(Call<LogOut> call, Response<LogOut> response) {
                int statusCode = response.code();
                Log.d("LOGGER Logout", "response code: " + statusCode);

                SharedPreferences.Editor editor = getSharedPreferences("AccountData", MODE_PRIVATE).edit();

                editor.clear();
                editor.apply();

                isAuthorized();
            }

            @Override
            public void onFailure(Call<LogOut> call, Throwable t) {
                Snackbar.make(main_layout, "Something goes wrong while logging out", Snackbar.LENGTH_LONG).show();
                Log.d("LOGGER Logout", "onFailure: " + t.getMessage());
            }
        });
    }

    // put data to recycler view
    private void fillRecyclerWithData() {

        adapter = new TransactionsAdapter(getApplicationContext(), transactionsList);
        transactions_recycler.setAdapter(adapter);


    }

    // load more data to recyclerView
    private void loadMoreDataToRecycler() {

        // display loading dialog
        showLoadingData();

        final String token = accountPrefs.getString("token", null);
        final int id = accountPrefs.getInt("id", 0);

        // if user is authorized
        if(token != null && id != 0) {

            iTransactionsResponse = retrofit.create(ITransactions.class);

            Call<TransactionsResponse> transactionsResponseCall = iTransactionsResponse.getTransactions(
                    createKey(id, token, String.valueOf(limit), String.valueOf(start)),
                    id,
                    currentTimestamp(),
                    start,
                    limit
            );

            transactionsResponseCall.enqueue(new Callback<TransactionsResponse>() {
                @Override
                public void onResponse(Call<TransactionsResponse> call, Response<TransactionsResponse> response) {
                    int statusCode = response.code();
                    Log.d("LOGGER Transactions", "response code: " + statusCode);

                    TransactionsResponse transactionsResponse = response.body();
                    if (transactionsResponse.getStatus().equals("ok")) {
                        if (transactionsList.size() > 0) transactionsList.clear();

                        List<TransactionsResponseData> transactionsResponseDataList = transactionsResponse.getData();
                        adapter.add(transactionsResponseDataList);


                        if (bar != null)
                            bar.dismiss();
                    } else {
                        Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                    }
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onFailure(Call<TransactionsResponse> call, Throwable t) {
                    Snackbar.make(main_layout, "Something goes wrong while downloading data", Snackbar.LENGTH_LONG).show();
                    Log.d("LOGGER Transactions", "onFailure: " + t.getMessage());
                }
            });
        }
    }

    // set visibilities of views
    private void toggleVisibilityOfViews(int id, String token) {
        // if user is authorized
        if(token != null && id != 0) {
            // toggling visibility of views
            scan_qr_code.setVisibility(View.GONE);
            account_info_layout.setVisibility(View.VISIBLE);
            transactions_recycler.setVisibility(View.VISIBLE);
            //tv_exchange_rate.setVisibility(View.VISIBLE);
            btn_transaction_add.setVisibility(View.VISIBLE);
            if (menu_logout != null)
                menu_logout.setVisible(true);
        }
        // if user isn't authorized
        else {
            // toggling visibility of views
            account_info_layout.setVisibility(View.GONE);
            scan_qr_code.setVisibility(View.VISIBLE);
            //tv_exchange_rate.setVisibility(View.GONE);

            transactions_recycler.setVisibility(View.GONE);
            btn_transaction_add.setVisibility(View.GONE);
            if (menu_logout != null)
                menu_logout.setVisible(false);
        }
    }


    // parsing json data
    private void parseResponseJson(String response) {
        Gson gson = new Gson();
        QrCode resp = gson.fromJson(response, QrCode.class);

        // getting account data
        getAccountData(resp.getId(), resp.getApi_token());

    }

    // creating key for Get Account and Get Transactions
    private String createKey(int id, String token, String limit, String start) {

        int timestamp_str = currentTimestamp();

        Map<String, String> map = new HashMap<>();

        map.put("id", String.valueOf(id));
        map.put("timestamp", String.valueOf(timestamp_str));
        map.put("start", start);
        map.put("limit", limit);

        return concatAndHashKey(map, token);
    }

    // creating key for getting Exchange Rate
    private String createKey(int id, String token) {
        int timestamp_str = currentTimestamp();

        Map<String, String> map = new HashMap<>();

        map.put("id", String.valueOf(id));
        map.put("timestamp", String.valueOf(timestamp_str));

        return concatAndHashKey(map, token);
    }

    // creating key for posting transactions
    private String createKey(int id, String token, String address, int amount, String currency) {

        int timestamp_str = currentTimestamp();

        Map<String, String> map = new HashMap<>();

        map.put("id", String.valueOf(id));
        map.put("timestamp", String.valueOf(timestamp_str));
        map.put("address", address);
        map.put("amount", String.valueOf(amount));
        map.put("currency", currency);


        return concatAndHashKey(map, token);
    }

    // encoding header key
    private String concatAndHashKey(Map map, String token) {
        TreeMap<String, String> sortedMap = new TreeMap<>(map);

        String auth_signature = "";

        for (String value : sortedMap.values()) {
            auth_signature += value;
        }

        auth_signature = auth_signature + token;

        return Hashing.sha256().hashString(auth_signature, StandardCharsets.UTF_8).toString();
    }

    // getting current timestamp
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
            case R.id.btn_transaction_add:
                openDialog();
        }
    }

    // open dialog to fill data
    private void openDialog() {
        Bundle bundle = new Bundle();
        bundle.putDouble("usd_cur", usd_cur);
        bundle.putDouble("eur_cur", eur_cur);

        TransactionsAddDialog transactionsAddDialog = new TransactionsAddDialog();
        transactionsAddDialog.setArguments(bundle);
        transactionsAddDialog.show(getSupportFragmentManager(), "Transaction adding dialog");
    }

    // saving id and token of account
    private void saveAccountData(int id, String token) {
        SharedPreferences.Editor editor = getSharedPreferences("AccountData", MODE_PRIVATE).edit();
        editor.putInt("id", id);
        editor.putString("token", token);
        editor.apply();
    }

    // show progress dialog while downloading data
    private void showLoadingData() {

        bar = Snackbar.make(main_layout, "Downloading data", Snackbar.LENGTH_INDEFINITE);
        ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
        ProgressBar item = new ProgressBar(this);
        contentLay.addView(item,0);
        bar.show();
    }

    //checking the internet connection
    public boolean isOffline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue != 0);
        }
        catch (IOException | InterruptedException e)          { e.printStackTrace(); }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOffline()) {
            Snackbar.make(main_layout, "There is no internet connection. Check your network.", Snackbar.LENGTH_LONG).show();
        } else
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            preloader.setVisibility(View.GONE);
                            getSupportActionBar().show();
                            isAuthorized();
                        }
                    }, 1500);
    }

    @Override
    public void applyData(String address, int amount, String currency) {
        String token = accountPrefs.getString("token", null);
        int id = accountPrefs.getInt("id", 0);

        // if user is authorized
        if(token != null && id != 0) {

            postTransactionsData(
                    token,
                    id,
                    address,
                    amount,
                    currency
            );

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu_logout = menu.findItem(R.id.logout);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                String token = accountPrefs.getString("token", null);
                int id = accountPrefs.getInt("id", 0);

                // if user is authorized
                if(token != null && id != 0) {
                    logOut(id, token);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
