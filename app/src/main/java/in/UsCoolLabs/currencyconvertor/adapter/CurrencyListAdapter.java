package in.UsCoolLabs.currencyconvertor.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.UsCoolLabs.currencyconvertor.R;
import in.UsCoolLabs.currencyconvertor.activity.MainActivity;
import in.UsCoolLabs.currencyconvertor.model.Currency;

import static in.UsCoolLabs.currencyconvertor.activity.CurrencyList.fromTo;

/**
 * Created by ujjawal on 9/10/17.
 */

public class CurrencyListAdapter extends RecyclerView.Adapter<CurrencyListAdapter.ViewHolder> implements Filterable {

    private Activity context;
    private LayoutInflater inflater;
    public static List<Currency> currencies;
    public static List<Currency> mFilteredList;

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    mFilteredList = currencies;
                } else {
                    ArrayList<Currency> filteredList = new ArrayList<>();
                    for (Currency androidVersion : currencies) {
                        if (androidVersion.getName().toLowerCase().startsWith(charString) || androidVersion.getName().toLowerCase().contains(charString)) {
                            filteredList.add(androidVersion);
                        } else if (androidVersion.getCode().toLowerCase().contains(charString) || androidVersion.getCode().contains(charString)) {
                            filteredList.add(androidVersion);
                        }
                    }
                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<Currency>) filterResults.values;
                currencies = mFilteredList;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView currencyNameView;
        public LinearLayout main_layout;

        public ViewHolder(View itemView) {
            super(itemView);
            currencyNameView = itemView.findViewById(R.id.text_currency_name);
            main_layout = itemView.findViewById(R.id.main_layout);
        }
    }

    public CurrencyListAdapter(Activity context, List<Currency> currencyList) {
        this.context = context;
        this.currencies = currencyList;
        inflater = LayoutInflater.from(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_currency_list, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Currency currency = currencies.get(position);
        holder.currencyNameView.setText("("+(position+1)+").  "+currency.getName() + " (" + currency.getCode() + ")");
        holder.main_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fromTo.equals("from")) {
                    MainActivity.mFromCurrency = currency;
                } else {
                    MainActivity.mToCurrency = currency;
                }
                Intent returnIntent = new Intent();
                returnIntent.putExtra("name", currency.getName() + " (" + currency.getCode() + ")");
                context.setResult(Activity.RESULT_OK, returnIntent);
                context.finish();
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return currencies.size();
    }
}

