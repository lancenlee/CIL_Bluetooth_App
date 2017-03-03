package com.example.jack.myapplicationofbluetoothdemo.Avtivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jack.myapplicationofbluetoothdemo.Adapter.MyAdapter;
import com.example.jack.myapplicationofbluetoothdemo.R;
import com.example.jack.myapplicationofbluetoothdemo.Util.Bluetoothes;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2016/10/27.
 */
public class ShowView extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private Button searchBtn;
    private ListView bluetoothLv;
    private MyAdapter myAdapter;
    private List<Bluetoothes> bluetoothes;
    public static BluetoothSocket btSocket;
    private BluetoothAdapter bluetoothAdapter;
    private Bluetoothes dataBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sleepingbaglistview);
        initView();
        initData();
        myAdapter = new MyAdapter(this, bluetoothes);
        bluetoothLv.setAdapter(myAdapter);
        searchBtn.setOnClickListener(this);
        bluetoothLv.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        bluetoothAdapter.disable();      //关闭蓝牙
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void initView() {
        bluetoothLv = (ListView) findViewById(R.id.sleepBLView_bluetooth_lv);
        searchBtn = (Button) findViewById(R.id.sleepBLView_search_btn);
    }

    public void initData() {
        bluetoothes = new ArrayList<>();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(ShowView.this, "Bluetooth is not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();              //开启蓝牙
            Intent enable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            enable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(enable);
        }
    }

    @Override
    public void onClick(View v) {
        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(ShowView.this, "PLEASE OPEN THE BLUETOOTH.", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothes != null) {
                bluetoothes.clear();
                if (myAdapter != null) {
                    myAdapter.notifyDataSetChanged();
                }
            }
            bluetoothAdapter.startDiscovery();
            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, intentFilter);
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //如果搜索到设备，取得设备的MAC地址
            if ((BluetoothDevice.ACTION_FOUND).equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                Bluetoothes bluetooth = new Bluetoothes();
                bluetooth.setName(name);
                bluetooth.setAddress(address);
                //将搜索到的设备添加到list中，保存到listView
                if (bluetoothes.indexOf(bluetooth) == -1) {
                    bluetoothes.add(bluetooth);
                    if (myAdapter != null) {
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        bluetoothAdapter.cancelDiscovery();        //停止搜索
        dataBluetooth = new Bluetoothes();
        dataBluetooth = bluetoothes.get(position);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(dataBluetooth.getAddress());
        try {
            Method clientMethod = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            btSocket = (BluetoothSocket) clientMethod.invoke(device, 1);
            connect(btSocket);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void connect(final BluetoothSocket btSocket) {
        try {
            btSocket.connect();
            if (btSocket.isConnected()) {
                Toast.makeText(ShowView.this, "success", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ShowView.this, MainActivity.class);
                intent.putExtra("msg", dataBluetooth);
                startActivity(intent);
            } else {
                Toast.makeText(ShowView.this, "failed", Toast.LENGTH_SHORT).show();
                btSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
