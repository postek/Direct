package com.example.elukapo.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements DeviceActionListener {
    private WifiP2pManager manager;
    private Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver receiver = null;
    private boolean isWifiP2pEnabled = false;
    public static final String TAG = "WifiDirect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setActionBar(toolbar);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    public void resetData() {
        NeighbourListFragment fragmentList = (NeighbourListFragment) getFragmentManager() // obiekt klasy fragmentList - uchwyt do frag_list
                .findFragmentById(R.id.neigh_list);
        NeighbourDetailFragment fragmentDetails = (NeighbourDetailFragment) getFragmentManager() // obiekt klasy detailsFragment - uchwyt do frag_detail
                .findFragmentById(R.id.neigh_detail);
        if (fragmentList != null) {
            fragmentList.clearPeers(); // czyszczenie listy w razie gdy nie jest pusta
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews(); // czyszczenie szczegułów w razie gdy nie są puste
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        NeighbourDetailFragment fragment = (NeighbourDetailFragment) getFragmentManager()
                .findFragmentById(R.id.neigh_detail); // uchwyt do frag_detail
        fragment.setDetails(device);
    }

    @Override
    public void cancelDisconnect() {

    }

    @Override
    public void connect(WifiP2pConfig config) {

    }

    @Override
    public void createGroup() {

    }

    @Override
    public void disconnect() {

    }
}
