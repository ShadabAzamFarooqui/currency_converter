package in.UsCoolLabs.currencyconvertor.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import in.UsCoolLabs.currencyconvertor.fragment.AboutDialogFragment;
import in.UsCoolLabs.currencyconvertor.model.CurrencyApi;
import in.UsCoolLabs.currencyconvertor.fragment.ErrorDialogFragment;
import in.UsCoolLabs.currencyconvertor.R;
import in.UsCoolLabs.currencyconvertor.adapter.CurrencyAdapter;
import in.UsCoolLabs.currencyconvertor.database.DatabaseHelper;
import in.UsCoolLabs.currencyconvertor.model.Currency;
import in.UsCoolLabs.currencyconvertor.model.RateResponse;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static in.UsCoolLabs.currencyconvertor.activity.CurrencyList.fromTo;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String KEY_TIMESTAMP = "key_timestamp";

    private CurrencyApi mCurrencyApi;
    private DatabaseHelper mHelper;
    private CurrencyAdapter mCurrencyAdapter;

    private HashMap<String, String> mCurrencyMappings;
    private List<Currency> mCurrencies;
    private RateResponse mResponse;

    private TextView mCurrencyEditText;
    private Button btnConvert;
    private ProgressBar mProgressBar;

    TextView from_currency;
    TextView to_currency;
    TextView result;

    private String key;
    public static Currency mFromCurrency;
    public static Currency mToCurrency;
    TextView zero;
    TextView one;
    TextView two;
    TextView three;
    TextView four;
    TextView five;
    TextView six;
    TextView seven;
    TextView eight;
    TextView nine;
    TextView point;
    TextView clear;
    TextView delete;

    public static final int FROM = 1;
    public static final int TO = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        key = getString(R.string.key);
        mCurrencyEditText = findViewById(R.id.edit_amount);
        mProgressBar = findViewById(R.id.progress_loading);

        result = findViewById(R.id.result);

        zero = (TextView) findViewById(R.id.zero);
        one = (TextView) findViewById(R.id.one);
        two = (TextView) findViewById(R.id.two);
        three = (TextView) findViewById(R.id.three);
        four = (TextView) findViewById(R.id.four);
        five = (TextView) findViewById(R.id.five);
        six = (TextView) findViewById(R.id.six);
        seven = (TextView) findViewById(R.id.seven);
        eight = (TextView) findViewById(R.id.eight);
        nine = (TextView) findViewById(R.id.nine);
        point = (TextView) findViewById(R.id.point);

        clear = (TextView) findViewById(R.id.clear);
        delete = (TextView) findViewById(R.id.del);
        from_currency = (TextView) findViewById(R.id.from_currency);
        to_currency = (TextView) findViewById(R.id.to_currency);
        btnConvert = findViewById(R.id.btn_convert);


        initAdapter();
        initBtnOnClick();

        mProgressBar.setVisibility(View.GONE);


        from_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromTo = "from";
                Intent intent = new Intent(getApplicationContext(), CurrencyList.class);
                startActivityForResult(intent, FROM);
            }
        });
        to_currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fromTo = "to";
                Intent intent = new Intent(getApplicationContext(), CurrencyList.class);
                startActivityForResult(intent, TO);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_refresh:
                downloadData();
                return true;

            case R.id.action_about:
                AboutDialogFragment fragment = AboutDialogFragment.newInstance();
                fragment.show(getFragmentManager(), "FRAGMENT_ABOUT");
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initAdapter() {
        final RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint("http://openexchangerates.org/api")
                .build();

        mCurrencyApi = adapter.create(CurrencyApi.class);

        mHelper = DatabaseHelper.getInstance(this);


        if (mHelper.isDatabaseEmpty()) {
            //Download the data
            Log.i(TAG, "Downloading data");

            downloadData();
        }
// else {
//            //Db has data
//
//            Log.i(TAG, "Loading data from db");
//            DatabaseHelper helper = DatabaseHelper.getInstance(this);
//            List<Currency> currencyList = helper.getCurrencies();
//            Collections.sort(currencyList, new Comparator() {
//
//                @Override
//                public int compare(Object a1, Object a2) {
//                    final Currency app1 = (Currency) a1;
//                    final Currency app2 = (Currency) a2;
//                    return app1.getName().toLowerCase(Locale.getDefault()).compareTo(app2.getName().toLowerCase(Locale.getDefault()));
//                }
//            });
//        }
    }

    private void initBtnOnClick() {
        btnConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrencyEditText.getText().toString().isEmpty()) {
                    createAlertDialog("Amount can't be empty", "Please enter Amount");
                } else if (from_currency.getText().toString().isEmpty()) {
                    createAlertDialog("Base currency can't be empty", "Choose a currency from which you wan to convert");
                } else if (to_currency.getText().toString().isEmpty()) {
                    createAlertDialog("Convert currency can't be empty", "Choose a currency to which you want to convert");
                } else {
                    Double amount = Double.parseDouble(mCurrencyEditText.getText().toString());
                    double calculatedAmount = Double.parseDouble(new DecimalFormat("##.###")
                            .format(mToCurrency.getRate() * (1 / mFromCurrency.getRate()) * amount));
                    String resultStr = /*amount + " " + mFromCurrency.getCode() + " = " +*/ calculatedAmount + " " /*+ mToCurrency.getCode()*/;
                    result.setText(String.valueOf(resultStr));

                }
            }
        });

        clickListener(zero, 0);
        clickListener(one, 1);
        clickListener(two, 2);
        clickListener(three, 3);
        clickListener(four, 4);
        clickListener(five, 5);
        clickListener(six, 6);
        clickListener(seven, 7);
        clickListener(eight, 8);
        clickListener(nine, 9);

        point.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = mCurrencyEditText.getText().toString();
                if (!str.contains(".")) {
                    if (str.isEmpty()) {
                        str = str.concat("0.");
                    } else {
                        str = str.concat(".");
                    }
                    mCurrencyEditText.setText(str);
                }

            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCurrencyEditText.setText("");
                result.setText("");
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = mCurrencyEditText.getText().toString();
                if (str.length() > 0) {
                    mCurrencyEditText.setText(str.substring(0, str.length() - 1));
                }
                calculation();
            }
        });
    }

    public void downloadData() {

        if (!isNetworkConnected()) {
            ErrorDialogFragment fragment = ErrorDialogFragment.newInstance(
                    getString(R.string.title_error_no_network)
                    , getString(R.string.message_error_no_network));

            fragment.show(getFragmentManager(), "FRAGMENT_ERROR");

        } else {
            mProgressBar.setVisibility(View.VISIBLE);
            btnConvert.setEnabled(false);

            mCurrencyApi.getCurrencyMappings(key, new Callback<HashMap<String, String>>() {
                @Override
                public void success(HashMap<String, String> hashMaps, Response response) {
                    Log.i(TAG, "Got rates:" + hashMaps.toString());
                    mCurrencyMappings = hashMaps;

                    mCurrencyApi.getRates(key, new Callback<RateResponse>() {
                        @Override
                        public void success(RateResponse rateResponse, Response response) {
                            Log.i(TAG, "Got names: " + rateResponse.getRates().toString());

                            mResponse = rateResponse;

                            Log.i(TAG, "Timestamp: " + rateResponse.getTimestamp());

                            SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                            prefs.edit()
                                    .putLong(KEY_TIMESTAMP, rateResponse.getTimestamp())
                                    .apply();

                            if (mCurrencyMappings != null) {
                                mCurrencies = Currency.generateCurrencies(mCurrencyMappings, mResponse);

                                Log.i(TAG, "Generated Currencies: " + Arrays.toString(mCurrencies.toArray()));

                                mHelper.addCurrencies(mCurrencies);
                                initAdapter();
                                mProgressBar.setVisibility(View.GONE);
                                btnConvert.setEnabled(true);

                            }

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            Log.e(TAG, error.getLocalizedMessage());
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, error.getLocalizedMessage());
                }
            });
        }
    }

    private void createAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.show();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    void clickListener(View view, final Integer number) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str = mCurrencyEditText.getText().toString();
                if (number!=0) {
                    str = str.concat("" + number);
                    mCurrencyEditText.setText(str);

                }else {
                    if (str.isEmpty()||str.equals("0")){
                        return;
                    }
                    str = str.concat("" + number);
                    mCurrencyEditText.setText(str);
                }
                calculation();

            }
        });


    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == RESULT_OK) {
            if (requestCode == FROM) {
                from_currency.setText(data.getStringExtra("name"));
                calculation();
            }
            if (requestCode == TO) {
                to_currency.setText(data.getStringExtra("name"));
                calculation();
            }
        }
    }

    void calculation() {
        if (from_currency.getText().toString().isEmpty()) {
            return;
        }
        if (to_currency.getText().toString().isEmpty()) {
            return;
        }
        if (mCurrencyEditText.getText().toString().isEmpty()) {
            result.setText("");
        } else if (from_currency.getText().toString().isEmpty()) {
            result.setText("");
        } else if (to_currency.getText().toString().isEmpty()) {
            result.setText("");
        } else {
            Double amount = Double.parseDouble(mCurrencyEditText.getText().toString());
            double calculatedAmount = Double.parseDouble(new DecimalFormat("##.###")
                    .format(mToCurrency.getRate() * (1 / mFromCurrency.getRate()) * amount));
            String resultStr = /*amount + " " + mFromCurrency.getCode() + " = " +*/ calculatedAmount + " " /*+ mToCurrency.getCode()*/;
            result.setText(String.valueOf(resultStr));
        }
    }
}
