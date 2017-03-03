package com.example.jack.myapplicationofbluetoothdemo.Avtivity;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jack.myapplicationofbluetoothdemo.R;
import com.example.jack.myapplicationofbluetoothdemo.Util.Bluetoothes;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidObjectException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView temptureTv, blueTv;
    private final static int LOGO = 1;
    private int hour;
    private int minute;
    private int second;
    private Calendar calendar;
    private String nowTempter;
    private Button viewBtn;

    private GraphicalView chart;
    private Timer timer = new Timer();
    private TimerTask task; //任务
    private Float addY;
    private String addX;
    LinearLayout zhexianView;
    RelativeLayout firstView;

    String[] xkedu = new String[5];//x轴数据缓冲

    Float[] ycache = new Float[5];
    //private final static int SERISE_NR = 1; //曲线数量
    private XYSeries series;//用来清空第一个再加下一个
    private XYMultipleSeriesDataset dataset1;//xy轴数据源
    private XYMultipleSeriesRenderer render;
    SimpleDateFormat shijian = new SimpleDateFormat("hh:mm:ss");

    Handler handler2;
    TextView guangzhi2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initData();
        new TimeThread().start();
    }

    public void initData() {
        temptureTv = (TextView) findViewById(R.id.activity_bluetooth_tv);
        blueTv = (TextView) findViewById(R.id.activity_showTemp_tv);
        firstView = (RelativeLayout) findViewById(R.id.activity_firstView_layout);
        viewBtn = (Button) findViewById(R.id.activity_view_Btn);
        viewBtn.setOnClickListener(this);
        zhexianView = (LinearLayout) findViewById(R.id.activity_zhexianView_layout);
        Bluetoothes bluetooth = (Bluetoothes) getIntent().getSerializableExtra("msg");
        temptureTv.setText(bluetooth.toString());
    }

    public void initData_View() {
        guangzhi2 = (TextView) findViewById(R.id.guangzhi2);
        //制作曲线图，貌似不好下手只能变抄边理解，阿门
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.guangzhexian);
        //通过ChartFactory生成图表
        chart = ChartFactory.getLineChartView(this, getdemodataset(), getdemorenderer());
        linearLayout.removeAllViews();//先remove再add可以实现统计图更新

        //将图表添加到布局中去
        linearLayout.addView(chart, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        //通过Handler+Task形成一个定时任务,实现定时任务
        handler2 = new Handler() {
            public void handleMessage(Message msg) {
                updatechart();
                guangzhi2.setText(String.valueOf(addY));
            }
        };
        //定时器
        task = new TimerTask() {
            public void run() {
                Message msg = new Message();
                msg.what = 200;
                handler2.sendMessage(msg);
            }
        };
        timer.schedule(task, 0, 2000);//运行时间为0.间隔时间为2000ms
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.activity_view_Btn) {
            initData_View();
            firstView.setVisibility(View.GONE);
            zhexianView.setVisibility(View.VISIBLE);
        }
    }

    class TimeThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            do {
                try {
                    byte[] buffer = null;
                    buffer = new byte[1024];
                    InputStream inputStream = null;
                    inputStream = ShowView.btSocket.getInputStream();
                    int num = 0;
                    num = inputStream.read(buffer);
                    String str = "";
                    str = new String(buffer, 0, 5);
                    Thread.sleep(500);
                    nowTempter = str;
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = str;
                    mHandler.sendMessage(msg);
                } catch (Exception e) {
                    // TODO: handle exception
                }

            } while (true);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String str = (String) msg.obj;
                    blueTv.setText(str);
                    break;

                default:
                    break;
            }
        }
    };

    //更新折线图
    private void updatechart() {
        //判断当前点集中到底有多少点，因为屏幕总共只能容纳5个，所以当点数超过5时，长度永远是5
        int length = series.getItemCount();
        int a = length;
        if (length > 5) {
            length = 5;
        }

        addX = shijian.format(new java.util.Date());

        //addY = (float) (Math.random() * 10);
        //addY = Float.parseFloat(nowTempter);
        //移除数据集中旧的点集
        dataset1.removeSeries(series);
        if (a < 5)//当数据集中不够五个点的时候直接添加就好，因为初始化的时候只有一个点，所以前几次更新的时候直接添加
        {
            series.add(a + 1, addY);//第一个参数代表第几个点，要与下面语句中的第一个参数对应
            render.addXTextLabel(a + 1, addX);
            xkedu[a] = addX;
        } else //超过了五个点要去除xcache【0】换成【1】的.....
        {
            //将旧的点集中x和y的数值取出来放入backup中，造成曲线向左平移的效果
            for (int i = 0; i < length - 1; i++) {
                ycache[i] = (float) series.getY(i + 1);
                xkedu[i] = xkedu[i + 1];
            }

            //点集先清空，为了做成新的点集而准备
            series.clear();
            //将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中

            for (int k = 0; k < length - 1; k++) {
                series.add(k + 1, ycache[k]);
                render.addXTextLabel(k + 1, xkedu[k]);
            }
            xkedu[4] = addX;
            series.add(5, addY);
            render.addXTextLabel(5, addX);
        }
        //在数据集中添加新的点集
        dataset1.addSeries(series);
        //视图更新，没有这一步，曲线不会呈现动态
        chart.invalidate();
    }

    private XYMultipleSeriesRenderer getdemorenderer() {
        // TODO Auto-generated method stub
        render = new XYMultipleSeriesRenderer();
        render.setChartTitle("温度值实时曲线");
        render.setChartTitleTextSize(20);//设置整个图表标题文字的大小
        render.setAxisTitleTextSize(16);//设置轴标题文字的大小
        render.setAxesColor(Color.BLACK);
        render.setXTitle("时间");
        render.setYTitle("温度值");

        render.setLabelsTextSize(16);//设置轴刻度文字的大小
        render.setLabelsColor(Color.BLACK);
        render.setXLabelsColor(Color.BLACK);
        render.setYLabelsColor(0, Color.BLACK);
        render.setLegendTextSize(15);//设置图例文字大小
        //render.setShowLegend(false);//显示不显示在这里设置，非常完美

        XYSeriesRenderer r = new XYSeriesRenderer();//设置颜色和点类型
        r.setColor(Color.RED);
        r.setPointStyle(PointStyle.CIRCLE);
        r.setFillPoints(true);
        r.setChartValuesSpacing(3);

        render.addSeriesRenderer(r);
        render.setYLabelsAlign(Paint.Align.RIGHT);//刻度值相对于刻度的位置
        render.setShowGrid(true);//显示网格
        render.setYAxisMax(30);//设置y轴的范围
        render.setYAxisMin(0);
        render.setYLabels(6);//分6等份


        render.setInScroll(true);
        render.setLabelsTextSize(20);
        render.setLabelsColor(Color.BLACK);
        //render.getSeriesRendererAt(0).setDisplayChartValues(true); //显示折线上点的数值
        render.setPanEnabled(false, false);//禁止报表的拖动
        render.setPointSize(5f);//设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
        render.setMargins(new int[]{20, 30, 90, 10}); //设置图形四周的留白
        render.setMarginsColor(Color.WHITE);
        render.setXLabels(0);// 取消X坐标的数字zjk,只有自己定义横坐标是才设为此值


        return render;
    }

    private XYMultipleSeriesDataset getdemodataset() {
        // TODO Auto-generated method stub
        dataset1 = new XYMultipleSeriesDataset();//xy轴数据源
        series = new XYSeries("温度值 ");//这个事是显示多条用的，显不显示在上面render设置
        //这里相当于初始化，初始化中无需添加数据，因为如果这里添加第一个数据的话，
        //很容易使第一个数据和定时器中更新的第二个数据的时间间隔不为两秒，所以下面语句屏蔽
        //这里可以一次更新五个数据，这样的话相当于开始的时候就把五个数据全部加进去了，但是数据的时间是不准确或者间隔不为二的
        //for(int i=0;i<5;i++)
        //series.add(1, Math.random()*10);//横坐标date数据类型，纵坐标随即数等待更新


        dataset1.addSeries(series);
        return dataset1;
    }

    @Override
    protected void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }
}
