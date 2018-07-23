package com.example.elukapo.wifidirect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends Activity implements DeviceActionListener, WifiP2pManager.ChannelListener {
    private WifiP2pManager manager;
    private Channel channel;
    private final IntentFilter intentFilter = new IntentFilter();
    private BroadcastReceiver receiver = null;
    private boolean isWifiP2pEnabled = false;
    public static final String TAG = "WifiDirect";
    private boolean retryChannel = false;

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

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.atn_direct_enable:
                if(manager != null && channel != null){
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                } else {
                    Log.e(TAG,"channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discover:
                if(!isWifiP2pEnabled){
                    Toast.makeText(MainActivity.this,
                            "Please enable WiFi Direct from action bar above or wystem settings.", Toast.LENGTH_SHORT).show();
                    return true;
                }
                final NeighbourListFragment fragment = (NeighbourListFragment) getFragmentManager().findFragmentById(R.id.neigh_list);
                fragment.onInitiateDiscovery();
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(MainActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(MainActivity.this, "Discovery Failed : " + reason,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default :
                    return super.onOptionsItemSelected(item);
        }
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
    public void connect(WifiP2pConfig config) {
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void createGroup() {
        manager.createGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Group created.",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Group not created with code: " + reason,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        final NeighbourDetailFragment fragment = (NeighbourDetailFragment) getFragmentManager().findFragmentById(R.id.neigh_detail);
        fragment.resetViews();
        deletePersistentGroup(manager,channel);
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "Disconnect failed. Reason :" + reason);
            }
        });
    }

    public static void deletePersistentGroup(WifiP2pManager manager, WifiP2pManager.Channel channel) {
        try {
            Method method = WifiP2pManager.class.getMethod("deletePersistentGroup",
                    WifiP2pManager.Channel.class, int.class, WifiP2pManager.ActionListener.class);

            for (int netId = 0; netId < 32; netId++) {
                method.invoke(manager, channel, netId, null);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelDisconnected() {
        if(manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Oups! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
