package com.wallet.crypto.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wallet.crypto.R;
import com.wallet.crypto.MercuryConstants;
import com.wallet.crypto.entity.ErrorEnvelope;
import com.wallet.crypto.entity.GasSettings;
import com.wallet.crypto.entity.Wallet;
import com.wallet.crypto.ui.dialog.InputPwdDialog;
import com.wallet.crypto.ui.dialog.interfaces.IPositiveButtonDialogListener;
import com.wallet.crypto.util.BalanceUtils;
import com.wallet.crypto.viewmodel.ConfirmationViewModel;
import com.wallet.crypto.viewmodel.ConfirmationViewModelFactory;
import com.wallet.crypto.viewmodel.GasSettingsViewModel;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;

import javax.inject.Inject;

import static com.wallet.crypto.ui.dialog.DialogManagerKt.showCusDialog;

public class ConfirmationActivity extends BaseActivity implements IPositiveButtonDialogListener {
    AlertDialog dialog;

    @Inject
    ConfirmationViewModelFactory confirmationViewModelFactory;
    ConfirmationViewModel viewModel;

    private TextView fromAddressText;
    private TextView toAddressText;
    private TextView valueText;
    private TextView gasPriceText;
    private TextView gasLimitText;
    private TextView networkFeeText;
    private Button sendButton;

    private BigInteger amount;
    private int decimals;
    private String contractAddress;
    private boolean confirmationForTokenTransfer = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        AndroidInjection.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_confirm);
        toolbar();

        fromAddressText = findViewById(R.id.text_from);
        toAddressText = findViewById(R.id.text_to);
        valueText = findViewById(R.id.text_value);
        gasPriceText = findViewById(R.id.text_gas_price);
        gasLimitText = findViewById(R.id.text_gas_limit);
        networkFeeText = findViewById(R.id.text_network_fee);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(view -> onSend());

        String toAddress = getIntent().getStringExtra(MercuryConstants.EXTRA_TO_ADDRESS);
        contractAddress = getIntent().getStringExtra(MercuryConstants.EXTRA_CONTRACT_ADDRESS);
        amount = new BigInteger(getIntent().getStringExtra(MercuryConstants.EXTRA_AMOUNT));
        decimals = getIntent().getIntExtra(MercuryConstants.EXTRA_DECIMALS, -1);
        String symbol = getIntent().getStringExtra(MercuryConstants.EXTRA_SYMBOL);
        symbol = symbol == null ? MercuryConstants.ETH_SYMBOL : symbol;

        confirmationForTokenTransfer = contractAddress != null;

        toAddressText.setText(toAddress);

        String amountString = "-" + BalanceUtils.subunitToBase(amount, decimals).toPlainString() + " " + symbol;
        valueText.setText(amountString);
        valueText.setTextColor(ContextCompat.getColor(this, R.color.red));

        viewModel = ViewModelProviders.of(this, confirmationViewModelFactory)
                .get(ConfirmationViewModel.class);

        viewModel.defaultWallet().observe(this, this::onDefaultWallet);
        viewModel.gasSettings().observe(this, this::onGasSettings);
        viewModel.sendTransaction().observe(this, this::onTransaction);
        viewModel.progress().observe(this, this::onProgress);
        viewModel.error().observe(this, this::onError);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.confirmation_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit: {
                viewModel.openGasSettings(ConfirmationActivity.this);
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        viewModel.prepare(confirmationForTokenTransfer);
    }

    private void onProgress(boolean shouldShowProgress) {
        hideDialog();
        if (shouldShowProgress) {
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.title_dialog_sending)
                    .setView(new ProgressBar(this))
                    .setCancelable(false)
                    .create();
            dialog.show();
        }
    }

    private void hideDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void onSend() {
        // 输入密码对话框
        showCusDialog(this, new InputPwdDialog());
        hideDialog();
    }

    private void transfer() {
        GasSettings gasSettings = viewModel.gasSettings().getValue();

        if (!confirmationForTokenTransfer) {
            viewModel.createTransaction(
                    fromAddressText.getText().toString(),
                    toAddressText.getText().toString(),
                    amount,
                    gasSettings.gasPrice,
                    gasSettings.gasLimit);
        } else {
            viewModel.createTokenTransfer(
                    fromAddressText.getText().toString(),
                    toAddressText.getText().toString(),
                    contractAddress,
                    amount,
                    gasSettings.gasPrice,
                    gasSettings.gasLimit);
        }
    }

    private void onDefaultWallet(Wallet wallet) {
        fromAddressText.setText(wallet.getAddress());
    }

    private void onTransaction(String hash) {
        hideDialog();
        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.transaction_succeeded)
                .setMessage(hash)
                .setPositiveButton(R.string.button_ok, (dialog1, id) -> {
                    finish();
                })
                .setNeutralButton(R.string.copy, (dialog1, id) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("transaction hash", hash);
                    clipboard.setPrimaryClip(clip);
                    finish();
                })
                .create();
        dialog.show();
    }

    private void onGasSettings(GasSettings gasSettings) {
        String gasPrice = BalanceUtils.weiToGwei(gasSettings.gasPrice) + " " + MercuryConstants.GWEI_UNIT;
        gasPriceText.setText(gasPrice);
        gasLimitText.setText(gasSettings.gasLimit.toString());

        String networkFee = BalanceUtils.weiToEth(gasSettings
                .gasPrice.multiply(gasSettings.gasLimit)).toPlainString() + " " + MercuryConstants.ETH_SYMBOL;
        networkFeeText.setText(networkFee);
    }

    private void onError(ErrorEnvelope error) {
        hideDialog();
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.error_transaction_failed)
                .setMessage(error.message)
                .setPositiveButton(R.string.button_ok, (dialog1, id) -> {
                    // Do nothing
                })
                .create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == GasSettingsViewModel.SET_GAS_SETTINGS) {
            if (resultCode == RESULT_OK) {
                BigInteger gasPrice = new BigInteger(intent.getStringExtra(MercuryConstants.EXTRA_GAS_PRICE));
                BigInteger gasLimit = new BigInteger(intent.getStringExtra(MercuryConstants.EXTRA_GAS_LIMIT));
                GasSettings settings = new GasSettings(gasPrice, gasLimit);
                viewModel.gasSettings().postValue(settings);
            }
        }
    }

    @Override
    public void onPositiveButtonClicked(int requestCode, @NotNull Object any) {
        viewModel.getPassword().setValue((String) any);
        transfer();
    }
}
