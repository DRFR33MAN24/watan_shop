package com.sabeal.watanshop.packages;

import static com.airbnb.lottie.L.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonObject;
import com.sabeal.watanshop.R;
import com.sabeal.watanshop.packages.adapter.PaymentToastsModel;
import com.sabeal.watanshop.utills.Network.RestService;
import com.sabeal.watanshop.utills.SettingsMain;
import com.sabeal.watanshop.utills.UrlController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import co.paystack.android.Paystack;
import co.paystack.android.PaystackSdk;
import co.paystack.android.Transaction;
import co.paystack.android.model.Card;
import co.paystack.android.model.Charge;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayStackActivity extends AppCompatActivity {
    private TextInputLayout mCardNumber;
    private TextInputLayout mCardExpiry;
    private TextInputLayout mCardCVV;
    SettingsMain settingsMain;
    PayStackModel payStackModel;
    TextView errorText;
    ProgressBar progressBar;
    EditText cardNumber, cardExpiry, cvv;
    String packageId, packageType;
    RestService restService;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_stack);
        settingsMain =  new SettingsMain(this);
        restService = UrlController.createService(RestService.class, settingsMain.getUserEmail(), settingsMain.getUserPassword(), this);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait...");
        initializePaystack();
        initializeFormVariables();
    }
    private void initializePaystack() {
        PaystackSdk.initialize(PayStackActivity.this);
        PaystackSdk.setPublicKey(settingsMain.getPayStackModel().getPublicKey());
    }
    private void initializeFormVariables() {
        mCardNumber = findViewById(R.id.til_card_number);
        mCardExpiry = findViewById(R.id.til_card_expiry);
        mCardCVV = findViewById(R.id.til_card_cvv);
        progressBar = findViewById(R.id.progressBar3);
        errorText = findViewById(R.id.error_text_view);
        cardNumber = findViewById(R.id.card_number);
        cardExpiry = findViewById(R.id.card_expiry);
        cvv = findViewById(R.id.cvv);
        packageId = getIntent().getStringExtra("package_id");
        packageType = getIntent().getStringExtra("payment_from");


        // this is used to add a forward slash (/) between the cards expiry month
        // and year (11/21). After the month is entered, a forward slash is added
        // before the year
        cardNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                errorText.setVisibility(View.GONE);
                return false;
            }
        });
        cvv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                errorText.setVisibility(View.GONE);
                return false;
            }
        });
        cardExpiry.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                errorText.setVisibility(View.GONE);
                return false;
            }
        });
        Objects.requireNonNull(mCardExpiry.getEditText()).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                errorText.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                errorText.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                errorText.setVisibility(View.GONE);
                if(s.toString().length() == 2 && !s.toString().contains("/")) {
                    s.append("/");
                }
            }
        });

        Button button = findViewById(R.id.btn_make_payment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cardNumber.getText().toString().isEmpty() || cardExpiry.getText().toString().isEmpty() ||
                cvv.getText().toString().isEmpty()){
                    errorText.setVisibility(View.VISIBLE);
                }
                else {
                    progressBar.setVisibility(View.VISIBLE);
                    errorText.setVisibility(View.GONE);
                    performCharge();
                }
            }
        });
    }
    private void performCharge() {
        Intent intent = getIntent();

        String cardNumber = mCardNumber.getEditText().getText().toString();
        String cardExpiry = mCardExpiry.getEditText().getText().toString();
        String cvv = mCardCVV.getEditText().getText().toString();

        String[] cardExpiryArray = cardExpiry.split("/");
        int expiryMonth = Integer.parseInt(cardExpiryArray[0]);
        int expiryYear = Integer.parseInt(cardExpiryArray[1]);
        String amount = intent.getStringExtra("amount");
        int amountInInteger = Integer.parseInt(amount.toString());

        Card card = new Card(cardNumber, expiryMonth, expiryYear, cvv);

        Charge charge = new Charge();
        charge.setAmount(amountInInteger);
        charge.setEmail(settingsMain.getPayStackModel().getEmail());
        charge.setCard(card);
        charge.setCurrency(settingsMain.getPayStackModel().getCurrency());


        PaystackSdk.chargeCard(this, charge, new Paystack.TransactionCallback() {
            @Override
            public void onSuccess(Transaction transaction) {
                progressBar.setVisibility(View.GONE);
                parseResponse(transaction.getReference());
                adforest_Checkout();
            }

            @Override
            public void beforeValidate(Transaction transaction) {
                Log.d("Main Activity", "beforeValidate: " + transaction.getReference());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Before Validdate: " + transaction + transaction.getReference(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable error, Transaction transaction) {
                Log.d("Main Activity", "onError: " + error.getLocalizedMessage());
                Log.d("Main Activity", "onError: " + error);
                Log.d("Main Activity", "Transaction: " + transaction);
                Toast.makeText(getApplicationContext(), "Error: "+ error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

        });
    }
    private void parseResponse(String transactionReference) {
        String message = "Payment Successful - " + transactionReference;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void adforest_Checkout() {

        if (SettingsMain.isConnectingToInternet(this)) {

            settingsMain.showDilog(this);
            JsonObject params = new JsonObject();
            params.addProperty("package_id", packageId);
            params.addProperty("payment_from", packageType);
            Log.d("info Send Checkout", params.toString());

            Call<ResponseBody> myCall = restService.postCheckout(params, UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info Checkout Resp", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            Log.d("info Checkout object", "" + response.toString());
                            if (response.getBoolean("success")) {
                                settingsMain.setPaymentCompletedMessage(response.get("message").toString());
                                adforest_getDataForThankYou();
                            } else{
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                finish();
                            }


                        }else{
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), PaymentToastsModel.payment_failed + PaymentToastsModel.something_wrong, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } catch (JSONException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    } catch (IOException e) {
                        SettingsMain.hideDilog();
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog.dismiss();
                    if (t instanceof TimeoutException) {
                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof SocketTimeoutException || t instanceof NullPointerException) {

                        Toast.makeText(getApplicationContext(), settingsMain.getAlertDialogMessage("internetMessage"), Toast.LENGTH_SHORT).show();
                        settingsMain.hideDilog();
                    }
                    if (t instanceof NullPointerException || t instanceof UnknownError || t instanceof NumberFormatException) {
                        Log.d("info Checkout ", "NullPointert Exception" + t.getLocalizedMessage());
                        settingsMain.hideDilog();
                    } else {
                        SettingsMain.hideDilog();
                        Log.d("info Checkout err", String.valueOf(t));
                        Log.d("info Checkout err", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    }
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, settingsMain.getAlertDialogTitle("error"), Toast.LENGTH_SHORT).show();
        }
    }


    public void adforest_getDataForThankYou() {
        if (SettingsMain.isConnectingToInternet(this)) {
            Call<ResponseBody> myCall = restService.getPaymentCompleteData(UrlController.AddHeaders(this));
            myCall.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> responseObj) {
                    try {
                        if (responseObj.isSuccessful()) {
                            Log.d("info ThankYou Details", "" + responseObj.toString());

                            JSONObject response = new JSONObject(responseObj.body().string());
                            if (response.getBoolean("success")) {
                                JSONObject responseData = response.getJSONObject("data");

                                Log.d("info ThankYou object", "" + response.getJSONObject("data"));

                                Intent intent = new Intent(PayStackActivity.this, Thankyou.class);
                                intent.putExtra("data", responseData.getString("data"));
                                intent.putExtra("order_thankyou_title", responseData.getString("order_thankyou_title"));
                                intent.putExtra("order_thankyou_btn", responseData.getString("order_thankyou_btn"));
                                startActivity(intent);
                                SettingsMain.hideDilog();
                                PayStackActivity.this.finish();
                            } else {
                                dialog.dismiss();
                                SettingsMain.hideDilog();
                                Toast.makeText(getApplicationContext(), response.get("message").toString(), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                        SettingsMain.hideDilog();
                        finish();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    dialog.dismiss();
                    SettingsMain.hideDilog();
                    Log.d("info ThankYou error", String.valueOf(t));
                    Log.d("info ThankYou error", String.valueOf(t.getMessage() + t.getCause() + t.fillInStackTrace()));
                    finish();
                }
            });
        } else {
            SettingsMain.hideDilog();
            Toast.makeText(this, "Internet error", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    @Override
    protected void onStop() {
        Log.w(TAG, "App stopped");

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.w(TAG, "App destroyed");

        super.onDestroy();
    }
}