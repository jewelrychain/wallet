package com.wallet.crypto.di;

import com.wallet.crypto.interact.FetchTokensInteract;
import com.wallet.crypto.interact.FindDefaultNetworkInteract;
import com.wallet.crypto.repository.EthereumNetworkRepository;
import com.wallet.crypto.repository.TokenRepository;
import com.wallet.crypto.router.AddTokenRouter;
import com.wallet.crypto.router.SendTokenRouter;
import com.wallet.crypto.router.MainRouter;
import com.wallet.crypto.viewmodel.TokensViewModelFactory;

import dagger.Module;
import dagger.Provides;

@Module
class TokensModule {

    @Provides
    TokensViewModelFactory provideTokensViewModelFactory(
            FindDefaultNetworkInteract findDefaultNetworkInteract,
            FetchTokensInteract fetchTokensInteract,
            AddTokenRouter addTokenRouter,
            SendTokenRouter sendTokenRouter,
            MainRouter mainRouter) {
        return new TokensViewModelFactory(
                findDefaultNetworkInteract,
                fetchTokensInteract,
                addTokenRouter,
                sendTokenRouter,
                mainRouter);
    }

    @Provides
    FindDefaultNetworkInteract provideFindDefaultNetworkInteract(
            EthereumNetworkRepository networkRepository) {
        return new FindDefaultNetworkInteract(networkRepository);
    }

    @Provides
    FetchTokensInteract provideFetchTokensInteract(TokenRepository tokenRepository) {
        return new FetchTokensInteract(tokenRepository);
    }

    @Provides
    AddTokenRouter provideAddTokenRouter() {
        return new AddTokenRouter();
    }

    @Provides
    SendTokenRouter provideSendTokenRouter() {
        return new SendTokenRouter();
    }

    @Provides
    MainRouter provideTransactionsRouter() {
        return new MainRouter();
    }
}
