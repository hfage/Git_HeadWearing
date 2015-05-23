package com.example.headwearing;


import java.util.ArrayList;

import libsvm.*;

import com.example.headwearing.MyDatas.SignalData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.XLabels;
import com.github.mikephil.charting.utils.XLabels.XLabelPosition;
import com.github.mikephil.charting.utils.YLabels;
import com.github.mikephil.charting.utils.YLabels.YLabelPosition;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class HeadWear extends Activity {
	public static boolean DEBUG = true;
	public static String DRAW_BARCHART = "DRAW BAR CHART";
	public static boolean viewAcceleration = true;
	public static String TAG = "testHeadWear";
	public static boolean mBLEDeviceConnected = false;
	public static boolean mBLEDeviceConnecting = true;
	public static int LABEL_NUM = 5;
	
	
	private String mDeviceName = "";
	private String mDeviceAddress = "";
	
	private BluetoothLeService mBluetoothLeService = null;
	private DataHandlerService mDataHandlerService = null;
	private BarChart mBarChart;
	private ArrayList<String> xVals = new ArrayList<String>();
	private LineChart mLineChart1;
	private LineChart mLineChart2;
	private LineChart mLineChart3;
	private ArrayList<String> xLineChartVals = new ArrayList<String>();
	private int xLineChartLen = 250;
	public static float YRANGE_MIN = 0f;
	public static float YRANGE_MAX = 256f;
	private ArrayList<Entry> yLineChartVals1 = new ArrayList<Entry>();
	private ArrayList<Entry> yLineChartVals2 = new ArrayList<Entry>();
	private ArrayList<Entry> yLineChartVals3 = new ArrayList<Entry>();
	
	//定义的一些控件：
	private TextView tv;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Spinner spinner_service;
	private ArrayAdapter spinner_service_adapter;
	private Spinner spinner_characteristic;
	private ArrayAdapter spinner_characteristic_adapter;
	private Spinner spinner_label;
	private ArrayAdapter spinner_label_adapter;
	private Spinner spinner_type;
	private ArrayAdapter spinner_type_adapter;
	public static int label = 0;
	public static int type = 0;
	
	//定义的一些全局变量
	public int service_index = 0;
	public int characteristic_index = 0;
	
	//
	public MySocket mMySocket = new MySocket();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_head_wear);
		registerReceiver(mBLEDateUpdateReciver,makeBLEIntentFilter());
		initView();
	}
	
	public void initView(){
		tv = (TextView) findViewById(R.id.msg);
		tv.setMovementMethod(new ScrollingMovementMethod());
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		button5 = (Button) findViewById(R.id.button5);
		button1.setOnClickListener(new ClickEvent());
		button2.setOnClickListener(new ClickEvent());
		button3.setOnClickListener(new ClickEvent());
		button4.setOnClickListener(new ClickEvent());
		button5.setOnClickListener(new ClickEvent());
		spinner_service = (Spinner) findViewById(R.id.spinner_service);
		spinner_characteristic = (Spinner) findViewById(R.id.spinner_characteristic);
		spinner_label = (Spinner) findViewById(R.id.spinner_label);
		spinner_type = (Spinner) findViewById(R.id.spinner_type);
		String[] service_string = new String[9];
		String[] characteristic_string = new String[9];
		String[] label_string = new String[9];
		String[] type_string = {"NN","SVM","THRESHOLD"};
		for(int i = 0; i < 9; i++){
			service_string[i] = String.valueOf(i);
			characteristic_string[i] = String.valueOf(i);
			label_string[i] = String.valueOf(i);
		}
		spinner_service_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,service_string);
		spinner_service_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_characteristic_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,characteristic_string);
		spinner_characteristic_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_label_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,label_string);
		spinner_label_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_type_adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,type_string);
		spinner_type_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_service.setAdapter(spinner_service_adapter);
		spinner_characteristic.setAdapter(spinner_characteristic_adapter);
		spinner_label.setAdapter(spinner_label_adapter);
		spinner_type.setAdapter(spinner_type_adapter);
		spinner_service.setVisibility(View.VISIBLE);
		spinner_characteristic.setVisibility(View.VISIBLE);
		spinner_label.setVisibility(View.VISIBLE);
		spinner_type.setVisibility(View.VISIBLE);
		spinner_service.setOnItemSelectedListener(new SpinnerServiceListener());
		spinner_characteristic.setOnItemSelectedListener(new SpinnerCharacteristicListener());
		spinner_label.setOnItemSelectedListener(new SpinnerLabelListener());
		spinner_type.setOnItemSelectedListener(new SpinnerTypeListener());
		
		mBarChart = (BarChart) findViewById(R.id.barchart);
		if(viewAcceleration){
			mBarChart.setDrawYValues(false);
			mBarChart.setDescription("");
			mBarChart.setMaxVisibleValueCount(5);
			mBarChart.set3DEnabled(false);
			mBarChart.setPinchZoom(false);
			mBarChart.setUnit(" du");
			mBarChart.setDrawGridBackground(true);
			mBarChart.setDrawHorizontalGrid(true);
			mBarChart.setDrawVerticalGrid(false);
			mBarChart.setValueTextSize(10f);
			mBarChart.setDrawBorder(false);
			mBarChart.setYRange(YRANGE_MIN, YRANGE_MAX, false);
			xVals.add("X");
			xVals.add("Y");
			xVals.add("Z");
			
			mLineChart1 = (LineChart) findViewById(R.id.linechart1);
			mLineChart1.setUnit(" du");
			mLineChart1.setDrawUnitsInChart(true);
			mLineChart1.setYRange(YRANGE_MIN, YRANGE_MAX, false);
			mLineChart1.setDrawYValues(false);
			mLineChart1.setDescription("");
			mLineChart1.setTouchEnabled(true);
			mLineChart1.setDragEnabled(true);

			mLineChart2 = (LineChart) findViewById(R.id.linechart2);
			mLineChart2.setUnit(" du");
			mLineChart2.setDrawUnitsInChart(true);
			mLineChart2.setYRange(YRANGE_MIN, YRANGE_MAX, false);
			mLineChart2.setDrawYValues(false);
			mLineChart2.setDescription("");
			mLineChart2.setTouchEnabled(true);
			mLineChart2.setDragEnabled(true);
			
			mLineChart3 = (LineChart) findViewById(R.id.linechart3);
			mLineChart3.setUnit(" du");
			mLineChart3.setDrawUnitsInChart(true);
			mLineChart3.setYRange(YRANGE_MIN, YRANGE_MAX, false);
			mLineChart3.setDrawYValues(false);
			mLineChart3.setDescription("");
			mLineChart3.setTouchEnabled(true);
			mLineChart3.setDragEnabled(true);
			
			int i = 0;
			while(i < xLineChartLen){
				xLineChartVals.add("" + i);
				yLineChartVals1.add(new Entry(110,i));
				yLineChartVals2.add(new Entry(20,i));
				yLineChartVals3.add(new Entry(30,i));
				i++;
			}

			setBarChartData(0,0,0);
		}else{
			mBarChart.setVisibility(View.GONE);
		}
//		WebView mWebView = (WebView) findViewById(R.id.wv);
//		mWebView.getSettings().setJavaScriptEnabled(true);
//		//requestFocus();
//		mWebView.requestFocus();
//		mWebView.loadUrl("http://www.baidu.com");
		
	}
	
	class SpinnerServiceListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			//tv.setText("spinner_service click position : " + position);
			service_index = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class SpinnerCharacteristicListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			//tv.setText("spinner_characteristic click position : " + position);
			int i = Integer.parseInt("FF",16);
			MyLog.i("test","" + i);
			tv.setText("" + i);
			characteristic_index = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class SpinnerLabelListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			//tv.setText("spinner_service click position : " + position);
			label = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	class SpinnerTypeListener implements OnItemSelectedListener{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			tv.setText("train position : " + position);
			type = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}

	class ClickEvent implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == button1){
				// write notification
				tv.setText("button1 onclick");
				if(mBluetoothLeService == null){
					tv.setText("mBluetoothLeService didn't connect. \nCan not write notification.");
				}else{
					BluetoothGattCharacteristic characteristic = mBluetoothLeService.mBluetoothGatt.getServices().get(service_index).getCharacteristics().get(characteristic_index);
					tv.setText("Now writing notification \n Characteristic : " + characteristic.getUuid().toString());
					mBluetoothLeService.setCharacteristicNotification(characteristic, true);
				}
			}else if(v == button2){
				if(mBluetoothLeService == null){
					tv.setText("mBluetoothLeService didn't connect. \nCan not view service or characteristic.");
				}else{
					String text = "Services and characteristics: \n";
					for(BluetoothGattService bs : mBluetoothLeService.mBluetoothGatt.getServices()){
						text += bs.getUuid().toString() + "\n";
						for(BluetoothGattCharacteristic c : bs.getCharacteristics()){
							text += " * " + c.getUuid().toString() + "\n";
						}
						tv.setText(text);
					}
				}
			}else if(v == button3){
				mBluetoothLeService.closeNotification();
			}else if(v == button4){
				MyLog.i("hello", "world");
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						//socket_test();
					}
				}).start();
				String data = "4F";
				int x = Integer.parseInt(String.valueOf(data.charAt(0)) + String.valueOf(data.charAt(1)),16);
				if(x >= 128){
					x = x - 256;
				}
				x += 128;
				tv.setText(Integer.toHexString(80) + "," + Integer.toHexString(16) + "," + Integer.toHexString(x) + " x:" + x);
				//neural_test();
				//String svm_test_result = svm_test();
				//tv.setText("button2 onclick \n svm_result: " + svm_test_result);
			}else if(v == button5){
				if(type == 0){
					tv.setText("Train NN ");
					mDataHandlerService.trainNN();
				}else if(type == 1){
					tv.setText("Train SVM");
					mDataHandlerService.trainSVM();
				}else if(type == 2){
					tv.setText("Train threshold");
					mDataHandlerService.trainThresholdNew();
				}
			}else{
				// 
			}
			
		}
		
	}
	
	public void socket_test(){
		
		if(mMySocket.connect("192.168.1.100", 30001)){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMySocket.sendMsg("12345678123456781234567812345678123456781234567812345678123456781234567812345678");
			mMySocket.recvMsg();
		}
	}
	
	public void initTestDataForSVM(){
		//初始化测试数据，总共500个数据
		float[][] test_x = new float[50][13];
		double[] test_y = new double[50];
		float d = 3f;
		//100个label为1的数据，向量在[1,1,1,1,1,1,1,1,1,1,1,1]附近
		for(int i = 0; i < 10; i++){
			for(int j = 1; j < 13; j++){
				test_x[i][j] = j + (float) (Math.random() - 0.5) * d;
			}
			test_y[i] = 1;
		}
		//100个label为2的数据，向量在[10,10,10,10,10,10,10,10,10,10,10,10]附近
		for(int i = 10; i < 20; i++){
			for(int j = 1; j < 13; j++){
				test_x[i][j] = (13-j) + (float) (Math.random() - 0.5) * d;
			}
			test_y[i] = 2;
		}
		//100个label为3的数据，向量在[100,100,100,100,100,100,100,100,100,100,100,100]附近
		for(int i = 20; i < 30; i++){
			for(int j = 1; j < 13; j++){
				test_x[i][j] = 100 + (float) (Math.random() - 0.5) * d;
			}
			test_y[i] = 3;
		}
		//100个label为4的数据，向量在[1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000]附近
		for(int i = 30; i < 40; i++){
			for(int j = 1; j < 13; j++){
				test_x[i][j] = (float) (Math.pow(-1,j) * j + (float) (Math.random() - 0.5) * d);
			}
			test_y[i] = 4;
		}
		//100个label为5的数据，向量在[10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000]附近
		for(int i = 40; i < 50; i++){
			for(int j = 1; j < 13; j++){
				test_x[i][j] = (float) Math.pow(2, j) + (float) (Math.random() - 0.5) * d;
			}
			test_y[i] = 5;
		}
		for(int i = 0; i < 50; i++){
			test_x[i][0] = 1;
		}
		label_svm = test_y;
		datas_svm = test_x;
	}
	double[] label_svm;
	float[][] datas_svm;
	
	public String svm_test(){
		initTestDataForSVM();
		String result = "";
		double[] label = {1,2,3};
		float[][] datas = { {10,10},
							{-10,-10},
							{0,0} };
		MyDatas mMyDatas = new MyDatas();
		svm_problem mProblem = mMyDatas.returnSvmProblem(label_svm,datas_svm);
		svm_parameter mParam = new svm_parameter();
		mParam.cache_size = 100;
		mParam.eps = 0.00001;
		mParam.C = 100;
		mParam.gamma = 0.001;
		mParam.kernel_type = svm_parameter.RBF;
		result += "check: " + svm.svm_check_parameter(mProblem, mParam) + "\n gamma:" + mParam.gamma + "\n";
		
		svm_model model = svm.svm_train(mProblem, mParam); //svm.svm_train()训练出SVM分类模型
		float[] predict_datas1 = {1,1,2,3,4,5,6,7,8,9,10,11,12};
		float[] predict_datas2 = {1,12,11,10,9,8,7,6,5,4,3,2,1};
		float[] predict_datas3 = {1,100,100,100,100,100,100,100,100,100,100,100,100};
		float[] predict_datas4 = {1,-1,2,-3,4,-5,6,-7,8,-9,10,-11,12};
		float[] predict_datas5 = {1,2,4,8,16,32,64,128,256,512,1024,2048,4096};
		svm_node[] mPredict = mMyDatas.returnSvmPredictData(predict_datas1);
		result += "result: " + svm.svm_predict(model, mPredict) ;
		mPredict = mMyDatas.returnSvmPredictData(predict_datas2);
		result += "\nresult: " + svm.svm_predict(model, mPredict) ;
		mPredict = mMyDatas.returnSvmPredictData(predict_datas3);
		result += "\nresult: " + svm.svm_predict(model, mPredict) ;
		mPredict = mMyDatas.returnSvmPredictData(predict_datas4);
		result += "\nresult: " + svm.svm_predict(model, mPredict) ;
		mPredict = mMyDatas.returnSvmPredictData(predict_datas5);
		result += "\nresult: " + svm.svm_predict(model, mPredict) ;
		return result;
	}
	
	MyDatas.NeuralNetworkML nn = new MyDatas().new NeuralNetworkML();
	public void neural_test(){
		MyLog.i("HeadWear.neural_test","neural test");
//		float[][] a = new float[1][1];
//		nn.init();
//		nn.train(200);
//		float[] pred1 = {100,100,100,100,100,100,100,100,100,100};
//		nn.forward(pred1);
//		String s = "";
//		for(int i = 0; i < nn.h.length; i++){
//			s += "," + nn.h[i];
//		}
		float[][] a = new float[1][1];
		nn.init();
		float[] test = {-10000f, -0.000005f, 0, 0.000005f, 100000f};
		float[] sss = nn.sigmod(test);
		String ss = "";
		for(int i = 0; i < sss.length; i++){
			ss += "," + sss[i];
		}
		tv.setText(ss);
		nn.train(5000);
		String s = "";
		float[] pred1 = new float[13];
		
		
		for(int i = 0; i < 2; i++){
			pred1[0] = 1;
			for(int j = 1; j < 13; j++){
				pred1[j] = j;
			}
		}
		int h = nn.predict(pred1);
		MyLog.i("Pred",  "Pred:" + h);
		
		
		pred1[0] = 1;
		for(int j = 1; j < 13; j++){
			pred1[j] = 13 - j;
		}
		h = nn.predict(pred1);
		MyLog.i("Pred",  "Pred:" + h);
		
		pred1[0] = 1;
		for(int j = 1; j < 13; j++){
			pred1[j] = 100;
		}
		h = nn.predict(pred1);
		MyLog.i("Pred",  "Pred:" + h);
		
		pred1[0] = 1;
		for(int j = 1; j < 13; j++){
			pred1[j] = (float) (Math.pow(-1,j) * j);
		}
		h = nn.predict(pred1);
		MyLog.i("Pred",  "Pred:" + h);
		
		pred1[0] = 1;
		for(int j = 1; j < 13; j++){
			pred1[j] = (float) (Math.pow(2,j) * j);
		}
		h = nn.predict(pred1);
		MyLog.i("Pred",  "Pred:" + h);
	}

	public void setBarChartData(float x, float y, float z){
		ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
		yVals.add(new BarEntry(x, 0));
		yVals.add(new BarEntry(y, 1));
		yVals.add(new BarEntry(z, 2));
		BarDataSet bardataset = new BarDataSet(yVals, "Acceleration");
		bardataset.setBarSpacePercent(30f);
		ArrayList<BarDataSet> arraybardataset = new ArrayList<BarDataSet>();
		arraybardataset.add(bardataset);
		BarData data = new BarData(xVals, arraybardataset);
		mBarChart.setData(data);
		mBarChart.invalidate();
		setLineChartData(x,y,z);
	}
	
	public void setLineChartData(float x, float y, float z){
		yLineChartVals1.remove(0);
		yLineChartVals2.remove(0);
		yLineChartVals3.remove(0);
		int i = 0;
		for(i = 0; i < xLineChartLen - 1; i++){
			yLineChartVals1.get(i).setXIndex(i);
			yLineChartVals2.get(i).setXIndex(i);
			yLineChartVals3.get(i).setXIndex(i);
		}
		yLineChartVals1.add(new Entry(x,xLineChartLen - 1));
		yLineChartVals2.add(new Entry(y,xLineChartLen - 1));
		yLineChartVals3.add(new Entry(z,xLineChartLen - 1));
		LineDataSet set; 
		ArrayList<LineDataSet> linedataset;
		LineData data;
		
		set = new LineDataSet(yLineChartVals1, "X");
		set.setColor(Color.BLUE);
		set.disableDashedLine();
		set.setDrawCircles(false);
		linedataset = new ArrayList<LineDataSet>();
		linedataset.add(set);
		data = new LineData(xLineChartVals, linedataset);
		mLineChart1.setData(data);
		mLineChart1.invalidate();
		
		set = new LineDataSet(yLineChartVals2, "Y");
		set.setColor(Color.BLUE);
		set.disableDashedLine();
		set.setDrawCircles(false);
		linedataset = new ArrayList<LineDataSet>();
		linedataset.add(set);
		data = new LineData(xLineChartVals, linedataset);
		mLineChart2.setData(data);
		mLineChart2.invalidate();
		
		set = new LineDataSet(yLineChartVals3, "Z");
		set.setColor(Color.BLUE);
		set.disableDashedLine();
		set.setDrawCircles(false);
		linedataset = new ArrayList<LineDataSet>();
		linedataset.add(set);
		data = new LineData(xLineChartVals, linedataset);
		mLineChart3.setData(data);
		mLineChart3.invalidate();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(mBLEDeviceConnected){
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_stop).setVisible(true);
		}
		else{
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_stop).setVisible(false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch(id){
			case R.id.menu_scan:{
				if(!mBLEDeviceConnected){
					boolean inTest = true;
					if(inTest){
						Intent sendIntent = new Intent(BLEDevice.BLE_CONNECT_DEVICE);
						sendIntent.putExtra(BLEDevice.BLE_DEVICE_NAME, "a");
						sendIntent.putExtra(BLEDevice.BLE_DEVICE_ADDRESS, "11:11:11:11:11:11");
						sendBroadcast(sendIntent);
					}else{
						Intent intent = new Intent(this,BLEDevice.class);
						startActivity(intent);
					}
				}
				break;
			}
			case R.id.menu_stop:{
				if(mBLEDeviceConnected){
					
				}
				break;
			}
		}
		invalidateOptionsMenu();
		return super.onOptionsItemSelected(item);
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    // .setIcon(R.drawable.services)
                    .setTitle("退出程序？")
                    .setMessage("真的要退出吗？")
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,int which) {
                                	
                                }
                            })
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                	if(mBluetoothLeService != null){
                                		MyLog.e("mBluetoothLeService",mBluetoothLeService.toString());
                                		try {
                                			MyLog.i("unbindService", "unbind mBLEServiceConnection");
                                			unbindService(mBLEServiceConnection);
                                			if(mMySocket.client.isConnected()){
                                				mMySocket.closeSocket();
                                			}
                                		} catch (Exception e) {
                        					// TODO Auto-generated catch block
                        					e.printStackTrace();
                        				}
                                		
                                	}
                                	if(mDataHandlerService != null){
                                		MyLog.i("unbindService", "unbind mDataHandlerServiceConnection");
                                		unbindService(mDataHandlerServiceConnection);
                                	}
                                	unregisterReceiver(mBLEDateUpdateReciver);
                                    finish();
                                }
                            }).show();
            return true;
        }else{

        	return super.onKeyDown(keyCode, event);
        }
    }
	
	private final ServiceConnection mBLEServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            MyLog.i("test","HeadWear onServiceConnected");
            if(!mBluetoothLeService.initialize()){
            	Toast.makeText(HeadWear.this, "Can not connect to the device: " + mDeviceAddress + "cannot init" ,Toast.LENGTH_SHORT).show();
            	unbindService(mBLEServiceConnection);
            }else{
            	if(!mBluetoothLeService.connect(mDeviceAddress)){
                	if(DEBUG){
                		MyLog.e(TAG,"Can not connect to the device: " + mDeviceAddress );
                		tv.setText("Can't connect to BLE device.\n Address: " + mDeviceAddress + "\n Name:");
                	}
                	mBluetoothLeService = null;
                	unbindService(mBLEServiceConnection);
                	Toast.makeText(HeadWear.this, "Can not connect to the device: " + mDeviceAddress ,Toast.LENGTH_SHORT).show();
                }else{
                	tv.setText("Connected to BLE.");
                }
            };
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mBluetoothLeService = null;
        }
    };
    
    private final ServiceConnection mDataHandlerServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
        	mDataHandlerService = ((DataHandlerService.LocalBinder) service).getService();
            MyLog.i("test","HeadWear onServiceConnected mDataHandlerServiceConnection");
            if(!mDataHandlerService.init()){
            	tv.setText("DataHandlerService cannot innit. Maybe you should disconnect your USB.");
            	unbindService(mDataHandlerServiceConnection);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mDataHandlerService = null;
        }
    };
	
	private final BroadcastReceiver mBLEDateUpdateReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(DEBUG){
//            	MyLog.i(TAG,"onReceive : " + action);
            }
            if(BLEDevice.BLE_CONNECT_DEVICE.equals(action)){
            	mDeviceName = intent.getStringExtra(BLEDevice.BLE_DEVICE_NAME);
            	mDeviceAddress = intent.getStringExtra(BLEDevice.BLE_DEVICE_ADDRESS);
            	MyLog.i("receiver : ", "address : " + mDeviceAddress);
            	Intent gattServiceIntent = new Intent(HeadWear.this, BluetoothLeService.class);
                if(bindService(gattServiceIntent, mBLEServiceConnection, BIND_AUTO_CREATE)){
                	MyLog.i(TAG,"bindsuccessfully");
                	
                }else{
                	MyLog.i(TAG,"bind un successfully");
                }
                if(DEBUG){
                	MyLog.i(TAG,"bindService");
                }
                Intent dataHandlerServiceIntent = new Intent(HeadWear.this, DataHandlerService.class);
                bindService(dataHandlerServiceIntent, mDataHandlerServiceConnection, BIND_AUTO_CREATE);
            }else if(DRAW_BARCHART.equals(action)){
            	if(viewAcceleration){
            		float[] x = new float[DataHandlerService.LEN_OF_RECEIVED_DATA];
            		float[] y = new float[DataHandlerService.LEN_OF_RECEIVED_DATA];
            		float[] z = new float[DataHandlerService.LEN_OF_RECEIVED_DATA];
	            	
	            	x = intent.getFloatArrayExtra("X");
	            	//x = 128 * (float) Math.sin(x / 100);
	            	y = intent.getFloatArrayExtra("Y");
	            	//y = 128 * (float) Math.cos(y / 100);
	            	z = intent.getFloatArrayExtra("Z");
	            	//z = 128 * (float) Math.tan(z / 100);
	            	for(int i = 0; i < DataHandlerService.LEN_OF_RECEIVED_DATA; i++){
	            		setBarChartData(x[i],y[i],z[i]);
	            	}
            	}
            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
            	MyLog.i("disconnected","headwear : gatt service disconnected");
            	tv.setText("BLE Server disconnected");
            	unbindService(mBLEServiceConnection);
            	unbindService(mDataHandlerServiceConnection);
            }
        }
	};
	
	private static IntentFilter makeBLEIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEDevice.BLE_CONNECT_DEVICE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(DRAW_BARCHART);
        return intentFilter;
    }
}
