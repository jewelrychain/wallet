package com.wallet.crypto.widget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.wallet.crypto.R;
import com.wallet.crypto.entity.Wallet;
import com.wallet.crypto.ui.widget.OnDepositClickListener;

import static com.wallet.crypto.MercuryConstants.CHANGELLY_REF_ID;
import static com.wallet.crypto.MercuryConstants.COINBASE_WIDGET_CODE;
import static com.wallet.crypto.MercuryConstants.ETH_SYMBOL;
import static com.wallet.crypto.MercuryConstants.SHAPESHIFT_KEY;

public class DepositView extends FrameLayout implements View.OnClickListener {

    private static final Uri coinbaseri = Uri.parse("https://buy.coinbase.com/widget")
            .buildUpon()
            .appendQueryParameter("code", COINBASE_WIDGET_CODE)
            .appendQueryParameter("amount", "0")
//            .address={address}
            .appendQueryParameter("crypto_currency", ETH_SYMBOL)
            .build();
    private static final Uri shapeshiftUri = Uri.parse("https://shapeshift.io/shifty.html")
            .buildUpon()
            .appendQueryParameter("apiKey", SHAPESHIFT_KEY)
            .appendQueryParameter("amount", "0")
//            ?destination=\(address)
            .appendQueryParameter("output", ETH_SYMBOL)
            .build();

    private static final Uri changellyteUri = Uri.parse("https://changelly.com/widget/v1?auth=email&from=BTC")
            .buildUpon()
            .appendQueryParameter("to", ETH_SYMBOL)
            .appendQueryParameter("merchant_id", CHANGELLY_REF_ID)
//            address=\\(address)
            .appendQueryParameter("amount", "0")
            .appendQueryParameter("ref_id", CHANGELLY_REF_ID)
            .appendQueryParameter("color", "00cf70")
            .build();

    private OnDepositClickListener onDepositClickListener;
    @NonNull
    private Wallet wallet;

    public DepositView(Context context, @NonNull Wallet wallet) {
        this(context, R.layout.layout_dialog_deposit, wallet);
    }

    public DepositView(Context context, @LayoutRes int layoutId, @NonNull Wallet wallet) {
        super(context);

        init(layoutId, wallet);
    }

    private void init(@LayoutRes int layoutId, @NonNull Wallet wallet) {
        this.wallet = wallet;
        LayoutInflater.from(getContext()).inflate(layoutId, this, true);
        findViewById(R.id.action_coinbase).setOnClickListener(this);
        findViewById(R.id.action_shapeshift).setOnClickListener(this);
        findViewById(R.id.action_changelly).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Uri uri;
        switch (v.getId()) {
            case R.id.action_shapeshift: {
                uri = shapeshiftUri.buildUpon()
                        .appendQueryParameter("destination", wallet.getAddress()).build();
            } break;
            case R.id.action_changelly: {
                uri = changellyteUri.buildUpon()
                        .appendQueryParameter("address", wallet.getAddress()).build();
            } break;
            default:
            case R.id.action_coinbase: {
                uri = coinbaseri.buildUpon()
                        .appendQueryParameter("address", wallet.getAddress()).build();
            } break;
        }
        if (onDepositClickListener != null) {
            onDepositClickListener.onDepositClick(v, uri);
        }
    }

    public void setOnDepositClickListener(OnDepositClickListener onDepositClickListener) {
        this.onDepositClickListener = onDepositClickListener;
    }
}
