package com.wallet.crypto.interact.rx.operator;

import com.wallet.crypto.entity.Wallet;
import com.wallet.crypto.repository.WalletRepository;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;

public class SavePasswordOperator implements SingleTransformer<Wallet, Wallet> {

    private final WalletRepository walletRepository;

    public SavePasswordOperator(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public SingleSource<Wallet> apply(Single<Wallet> upstream) {
        Wallet wallet = upstream.blockingGet();
        return Single.fromCallable(() -> wallet);
//        return passwordStore
//                .setPassword(wallet, password)
//                .onErrorResumeNext(err -> walletRepository.deleteWallet(wallet.getAddress())
//                        .lift(observer -> new DisposableCompletableObserver() {
//                            @Override
//                            public void onComplete() {
//                                observer.onError(err);
//                            }
//
//                            @Override
//                            public void onError(Throwable e) {
//                                observer.onError(e);
//                            }
//                        }))
//                .toSingle(() -> wallet);
    }
}
