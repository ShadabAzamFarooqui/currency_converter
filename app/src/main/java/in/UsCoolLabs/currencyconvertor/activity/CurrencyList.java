package in.UsCoolLabs.currencyconvertor.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import in.UsCoolLabs.currencyconvertor.R;
import in.UsCoolLabs.currencyconvertor.adapter.CurrencyListAdapter;
import in.UsCoolLabs.currencyconvertor.database.DatabaseHelper;
import in.UsCoolLabs.currencyconvertor.fragment.AboutDialogFragment;
import in.UsCoolLabs.currencyconvertor.fragment.ErrorDialogFragment;
import in.UsCoolLabs.currencyconvertor.model.Currency;
import in.UsCoolLabs.currencyconvertor.model.CurrencyApi;
import in.UsCoolLabs.currencyconvertor.model.RateResponse;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CurrencyList extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static final String KEY_TIMESTAMP = "key_timestamp";
    private CurrencyApi mCurrencyApi;
    private DatabaseHelper mHelper;
    private CurrencyListAdapter mCurrencyAdapter;
    private HashMap<String, String> mCurrencyMappings;
    private List<Currency> mCurrencies;
    private RateResponse mResponse;
    private ProgressBar mProgressBar;
    private String key;
    RecyclerView mRecycler_view;
    public static String fromTo;
    EditText search;
    DatabaseHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.currency_list);
        key = getString(R.string.key);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressBar = findViewById(R.id.progress_loading);
        helper = DatabaseHelper.getInstance(this);
        mHelper = DatabaseHelper.getInstance(this);
        initAdapter();
        mProgressBar.setVisibility(View.GONE);
        search = findViewById(R.id.search);
    }

    @Override
    protected void onResume() {
        super.onResume();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                List<Currency> currencyList = mHelper.getCurrencies();
                Collections.sort(currencyList, new Comparator() {

                    @Override
                    public int compare(Object a1, Object a2) {
                        final Currency app1 = (Currency) a1;
                        final Currency app2 = (Currency) a2;
                        return app1.getName().toLowerCase(Locale.getDefault()).compareTo(app2.getName().toLowerCase(Locale.getDefault()));
                    }
                });

                CurrencyListAdapter.mFilteredList = currencyList;
                CurrencyListAdapter.currencies =currencyList;
                mCurrencyAdapter.getFilter().filter(s.toString());
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
                finish();
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
        } else {
            //Db has data

            Log.i(TAG, "Loading data from db");
            List<Currency> currencyList = helper.getCurrencies();
            Collections.sort(currencyList, new Comparator() {

                @Override
                public int compare(Object a1, Object a2) {
                    final Currency app1 = (Currency) a1;
                    final Currency app2 = (Currency) a2;
                    return app1.getName().toLowerCase(Locale.getDefault()).compareTo(app2.getName().toLowerCase(Locale.getDefault()));
                }
            });
            setAdapter();
        }
    }


    public void downloadData() {

        if (!isNetworkConnected()) {
            ErrorDialogFragment fragment = ErrorDialogFragment.newInstance(
                    getString(R.string.title_error_no_network)
                    , getString(R.string.message_error_no_network));

            fragment.show(getFragmentManager(), "FRAGMENT_ERROR");
        } else {

            mProgressBar.setVisibility(View.VISIBLE);


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


    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    void setAdapter() {
        mRecycler_view = findViewById(R.id.recycler_view);
        mRecycler_view.setHasFixedSize(true);
        mRecycler_view.setLayoutManager(new LinearLayoutManager(this));
        DatabaseHelper helper = DatabaseHelper.getInstance(this);
        List<Currency> currencyList = helper.getCurrencies();
        Collections.sort(currencyList, new Comparator() {

            @Override
            public int compare(Object a1, Object a2) {
                final Currency app1 = (Currency) a1;
                final Currency app2 = (Currency) a2;
                return app1.getName().toLowerCase(Locale.getDefault()).compareTo(app2.getName().toLowerCase(Locale.getDefault()));
            }
        });
        mCurrencyAdapter = new CurrencyListAdapter(this, currencyList);
        mRecycler_view.setAdapter(mCurrencyAdapter);
    }
}
