package com.example.jack.myapplicationofbluetoothdemo.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jack.myapplicationofbluetoothdemo.R;
import com.example.jack.myapplicationofbluetoothdemo.Util.Bluetoothes;

import java.util.List;

/**
 * Created by Jack on 2016/10/27.
 */
public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private List<Bluetoothes> bluetoothes;

    public MyAdapter() {
    }

    public MyAdapter(Context context, List<Bluetoothes> bluetoothes) {
        mContext = context;
        this.bluetoothes = bluetoothes;
    }

    @Override
    public int getCount() {
        return bluetoothes.size();
    }

    @Override
    public Object getItem(int position) {
        return bluetoothes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.bluetooth_item, null);
            viewHolder.nameTv = (TextView) view.findViewById(R.id.blueitem_name_tv);
            viewHolder.addressTv = (TextView) view.findViewById(R.id.blueitem_address_tv);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        viewHolder.nameTv.setText(bluetoothes.get(position).getName());
        viewHolder.addressTv.setText(bluetoothes.get(position).getAddress());
        return view;
    }

    class ViewHolder {
        public TextView nameTv, addressTv;
    }

}
