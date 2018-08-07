package com.wallet.crypto.repository;

import android.text.TextUtils;

import com.wallet.crypto.entity.NetworkInfo;
import com.wallet.crypto.entity.Ticker;
import com.wallet.crypto.service.TickerService;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.Single;

import static com.wallet.crypto.MercuryConstants.CLASSIC_NETWORK_NAME;
import static com.wallet.crypto.MercuryConstants.ETC_SYMBOL;
import static com.wallet.crypto.MercuryConstants.ETHEREUM_NETWORK_NAME;
import static com.wallet.crypto.MercuryConstants.ETH_SYMBOL;
import static com.wallet.crypto.MercuryConstants.KOVAN_NETWORK_NAME;
import static com.wallet.crypto.MercuryConstants.POA_NETWORK_NAME;
import static com.wallet.crypto.MercuryConstants.POA_SYMBOL;
import static com.wallet.crypto.MercuryConstants.RINKEBY_NETWORK_NAME;
import static com.wallet.crypto.MercuryConstants.ROPSTEN_NETWORK_NAME;

public class EthereumNetworkRepository {

    private final NetworkInfo[] NETWORKS = new NetworkInfo[]{
            new NetworkInfo(ETHEREUM_NETWORK_NAME, ETH_SYMBOL,
                    "https://mainnet.infura.io/hFFqayIX3GgnYc6B569b",
                    "https://api.trustwalletapp.com/",
                    "https://etherscan.io/", 1, true),
            new NetworkInfo(CLASSIC_NETWORK_NAME, ETC_SYMBOL,
                    "https://mewapi.epool.io/",
                    "https://classic.trustwalletapp.com",
                    "https://gastracker.io", 61, true),
            new NetworkInfo(POA_NETWORK_NAME, POA_SYMBOL,
                    "https://core.poa.network",
                    "https://poa.trustwalletapp.com", "poa", 99, false),
            new NetworkInfo(KOVAN_NETWORK_NAME, ETH_SYMBOL,
                    "https://kovan.infura.io/hFFqayIX3GgnYc6B569b",
                    "https://kovan.trustwalletapp.com/",
                    "https://kovan.etherscan.io", 42, false),
            new NetworkInfo(ROPSTEN_NETWORK_NAME, ETH_SYMBOL,
                    "https://ropsten.infura.io/hFFqayIX3GgnYc6B569b",
                    "https://ropsten.trustwalletapp.com/",
                    "https://ropsten.etherscan.io", 3, false),
            new NetworkInfo(RINKEBY_NETWORK_NAME, ETH_SYMBOL,
                    "https://rinkeby.infura.io/hFFqayIX3GgnYc6B569b",
                    "https://rinkeby.trustwalletapp.com/",
                    "https://rinkeby.etherscan.io", 4, false)
    };

    private final SharedPreferenceRepository preferences;
    private final TickerService tickerService;
    private NetworkInfo defaultNetwork;
    private final Set<OnNetworkChangeListener> onNetworkChangedListeners = new HashSet<>();

    public EthereumNetworkRepository(SharedPreferenceRepository preferenceRepository, TickerService tickerService) {
        this.preferences = preferenceRepository;
        this.tickerService = tickerService;
        defaultNetwork = getByName(preferences.getDefaultNetwork());
        if (defaultNetwork == null) {
            defaultNetwork = NETWORKS[0];
        }
    }

    private NetworkInfo getByName(String name) {
        if (!TextUtils.isEmpty(name)) {
            for (NetworkInfo NETWORK : NETWORKS) {
                if (name.equals(NETWORK.name)) {
                    return NETWORK;
                }
            }
        }
        return null;
    }

    public NetworkInfo getDefaultNetwork() {
        return defaultNetwork;
    }

    public void setDefaultNetworkInfo(NetworkInfo networkInfo) {
        defaultNetwork = networkInfo;
        preferences.setDefaultNetwork(defaultNetwork.name);

        for (OnNetworkChangeListener listener : onNetworkChangedListeners) {
            listener.onNetworkChanged(networkInfo);
        }
    }

    public NetworkInfo[] getAvailableNetworkList() {
        return NETWORKS;
    }

    public void addOnChangeDefaultNetwork(OnNetworkChangeListener onNetworkChanged) {
        onNetworkChangedListeners.add(onNetworkChanged);
    }

    public Single<Ticker> getTicker() {
        return Single.fromObservable(tickerService
                .fetchTickerPrice(getDefaultNetwork().symbol));
    }
}
