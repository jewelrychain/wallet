package com.wallet.crypto.viewmodel;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.Intent;

import com.wallet.crypto.entity.GasSettings;
import com.wallet.crypto.entity.Wallet;
import com.wallet.crypto.interact.CreateTransactionInteract;
import com.wallet.crypto.interact.FetchGasSettingsInteract;
import com.wallet.crypto.interact.FindDefaultWalletInteract;
import com.wallet.crypto.repository.TokenRepository;
import com.wallet.crypto.router.GasSettingsRouter;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ConfirmationViewModel extends BaseViewModel {
    private final MutableLiveData<String> newTransaction = new MutableLiveData<>();
    private final MutableLiveData<Wallet> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<GasSettings> gasSettings = new MutableLiveData<>();

    private final FindDefaultWalletInteract findDefaultWalletInteract;
    private final FetchGasSettingsInteract fetchGasSettingsInteract;
    private final CreateTransactionInteract createTransactionInteract;

    private final GasSettingsRouter gasSettingsRouter;
    private final MutableLiveData<String> password = new MutableLiveData<>();

    boolean confirmationForTokenTransfer = false;
    public MutableLiveData<String> getPassword() {
        return password;
    }


    public ConfirmationViewModel(FindDefaultWalletInteract findDefaultWalletInteract,
                                 FetchGasSettingsInteract fetchGasSettingsInteract,
                                 CreateTransactionInteract createTransactionInteract,
                                 GasSettingsRouter gasSettingsRouter) {
        this.findDefaultWalletInteract = findDefaultWalletInteract;
        this.fetchGasSettingsInteract = fetchGasSettingsInteract;
        this.createTransactionInteract = createTransactionInteract;
        this.gasSettingsRouter = gasSettingsRouter;
    }

    public void createTransaction(String from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit) {
        getProgress().postValue(true);
        setDisposable(createTransactionInteract
                .create(password.getValue(),new Wallet(from), to, amount, gasPrice, gasLimit, null)
                .subscribe(this::onCreateTransaction, this::onError));
    }

    public void createTokenTransfer(String from, String to, String contractAddress, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit) {
        getProgress().postValue(true);
        final String data = TokenRepository.createTokenTransferData(to, amount);
        setDisposable(createTransactionInteract
                .create(password.getValue(),new Wallet(from), contractAddress, BigInteger.valueOf(0), gasPrice, gasLimit, data)
                .subscribe(this::onCreateTransaction, this::onError));
    }

    public LiveData<Wallet> defaultWallet() {
        return defaultWallet;
    }

    public MutableLiveData<GasSettings> gasSettings() {
        return gasSettings;
    }

    public LiveData<String> sendTransaction() { return newTransaction; }

    public void prepare(boolean confirmationForTokenTransfer) {
        this.confirmationForTokenTransfer = confirmationForTokenTransfer;
        setDisposable(findDefaultWalletInteract
                .find()
                .subscribe(this::onDefaultWallet, this::onError));
    }

    private void onCreateTransaction(String transaction) {
        getProgress().postValue(false);
        newTransaction.postValue(transaction);
    }

    private void onDefaultWallet(Wallet wallet) {
        defaultWallet.setValue(wallet);
        if (gasSettings.getValue() == null) {
            onGasSettings(fetchGasSettingsInteract.fetch(confirmationForTokenTransfer));
        }
    }

    private void onGasSettings(GasSettings gasSettings) {
        this.gasSettings.setValue(gasSettings);
    }

    public void openGasSettings(Activity context) {
        gasSettingsRouter.open(context, gasSettings.getValue());
    }
}
