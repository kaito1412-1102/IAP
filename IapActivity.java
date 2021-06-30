package sadhu.com.speedtest.billing;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sadhu.com.speedtest.R;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SubcribeActivity extends AppCompatActivity implements PurchasesUpdatedListener {
    private static final String TAG = "TAG";
    @BindView(R.id.txt_price_monthly)
    TextView txtPriceMonthly;
    BillingClient billingClient;
    List<SkuDetails> skuDetailsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subcribe);
        ButterKnife.bind(this);
        setupBillingClient();
    }

    private void setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
                .enablePendingPurchases()
                .setListener(this::onPurchasesUpdated)
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
                Log.d(TAG, "onBillingServiceDisconnected: ");
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                initIAP();
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    List<Purchase> purchases = billingClient.queryPurchases(BillingClient.SkuType.SUBS).getPurchasesList();
                    Log.d(TAG, "onBillingSetupFinished: purchase: " + purchases.size());
                }
            }
        });
    }

    private void initIAP() {
        if (billingClient.isReady()) {
            Log.i(TAG, "ready ok");
            SkuDetailsParams params = SkuDetailsParams.newBuilder()
                    .setSkusList(Arrays.asList("key_remove_ads_monthly"))
                    .setType(BillingClient.SkuType.SUBS)
                    .build();
            billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
                @Override
                public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> list) {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && list.size() != 0) {
                        skuDetailsList.addAll(list);
                        txtPriceMonthly.setText(list.get(0).getPrice());
                    }
                }
            });
        } else {
            Log.i(TAG, "Not ready");
        }
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> list) {
        
    }

    @OnClick(R.id.txt_price_monthly)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_price_monthly:
                billingIAP();
                break;
        }
    }

    private void billingIAP() {
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetailsList.get(0))
                .build();
        billingClient.launchBillingFlow(this, billingFlowParams);
    }
}