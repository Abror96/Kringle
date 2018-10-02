package com.example.kringle.kringle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionsAddDialog extends AppCompatDialogFragment {

    TransactionsAddDialogListener dialogListener;

    MainActivity mainActivity;
    TextInputEditText et_transaction_address;
    TextInputEditText et_transaction_amount;
    Spinner currency_spinner;
    TextView readonly_currency;
    ImageView qr_btn;

    double usd_cur;
    double eur_cur;
    String response;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_transactions_add_dialog, null);

        if (getArguments() != null) {
            usd_cur = getArguments().getDouble("usd_cur");
            eur_cur = getArguments().getDouble("eur_cur");
            response = getArguments().getString("response");
        }

        mainActivity = new MainActivity();

        // init views
        et_transaction_address = view.findViewById(R.id.et_transaction_address);
        et_transaction_amount = view.findViewById(R.id.et_transaction_amount);
        currency_spinner = view.findViewById(R.id.currency_spinner);
        readonly_currency = view.findViewById(R.id.readonly_currency);
        qr_btn = view.findViewById(R.id.qr_code_btn);

        if (response != null) {
            response = response.substring(4, response.length());
        }

        et_transaction_address.setText(response);

        // Initializing a String Array
        final String[] currency_list = new String[]{
                "Currency",
                "Kringle",
                "USD"
        };

        ArrayAdapter<?> adapter = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item,  currency_list
        ){
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        currency_spinner.setAdapter(adapter);

        currency_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] choose = currency_list;
                int amount = 0;
                if (!et_transaction_amount.getText().toString().equals("")) {
                    amount = Integer.parseInt(et_transaction_amount.getText().toString());

                    String result;

                    if (choose[position].equals("Kringle")) {
                        result = new DecimalFormat("##.##").format(amount * usd_cur);
                        readonly_currency.setText(
                                result + " USD"
                        );
                    } else {
                        result = new DecimalFormat("##.##").format(Math.ceil(amount / usd_cur));
                        readonly_currency.setText(
                                result + " Kringle"
                        );
                    }
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        qr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).qrCodeBtnListener();
                dismiss();
            }
        });

        builder.setView(view)
                .setTitle("Send TBC")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!et_transaction_address.getText().toString().isEmpty()
                                && !et_transaction_amount.getText().toString().isEmpty()
                                && !currency_spinner.getSelectedItem().toString().isEmpty()) {
                            String address = et_transaction_address.getText().toString().trim();
                            int amount = Integer.parseInt(et_transaction_amount.getText().toString());
                            String currency = currency_spinner.getSelectedItem().toString().trim();

                            dialogListener.applyData(
                                    address,
                                    amount,
                                    currency);
                        } else {
                            Toast.makeText(getContext(), "Fill all the fields", Toast.LENGTH_LONG).show();
                        }

                    }
                });

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            dialogListener = (TransactionsAddDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement TransactionsDialogListener");
        }

    }

    public interface TransactionsAddDialogListener {
        void applyData(String address, int amount, String currency);
    }
}
