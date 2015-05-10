package com.example.headwearing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;

import com.example.headwearing.MyDatas.NeuralNetworkML;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
//import com.example.headwearing.MyDatas;

public class DataHandlerService extends Service{
	public boolean DEBUG = true;
	public boolean simulation = true;
	public boolean train_nn = false;
	public boolean train_svm = false;
	public boolean isCalculate = false;
	public static int LEN_OF_RECEIVED_DATA = 5;
	private final static String TAG = "testDataHandlerSerivce";
	public final static String DATA_SIMULATION = "DATA SIMULATION";
	public final static String DATA_RECEIVE = "DATA RECEIVE";
	private SQLiteDatabase sqlitedb;
	MyDatas.NeuralNetworkML mNeuralNetwork = new MyDatas().new NeuralNetworkML();
	private File path = new File("/sdcard/dbfile/"); //数据库文件目录  
    private File file = new File("/sdcard/dbfile/headwearing.db"); //数据库文件  
	
    public boolean init(){
		registerReceiver(mReceiver, makeIntentFilter());
		if(simulation){
			new Thread(new Runnable() {                    
				@Override
				public void run() {
					try {
	    				Thread.sleep(20000);
	    			} catch (InterruptedException e) {
	    				// TODO Auto-generated catch block
	    				MyLog.i("DataH", "InterruptedException");
	    				e.printStackTrace();
	    			}
					dataSimulation();
				}
			}).start();
		}
		if(!path.exists()){
			if(path.mkdirs()){
				MyLog.w(TAG,"mkdir succ");
			}
			else {
				MyLog.w(TAG,"mkdir fail");
				return false;
			}
		}
		if(!file.exists()){
			try{
				file.createNewFile();
			}catch(IOException e){
				MyLog.w(TAG,"createNewFile error.");
				e.printStackTrace();
				return false;
			}
		}
		sqlitedb = SQLiteDatabase.openOrCreateDatabase(file, null);
		sqlitedb.execSQL("CREATE table if not exists acceleration_data (id INTEGER PRIMARY KEY AUTOINCREMENT, label int, data text, recv_time long)");
		//sqlitedb.execSQL("delete from acceleration_data where id > 1");
		return true;
	}
	
	public void dataSimulation(){
		InputStream inputStream = getResources().openRawResource(R.raw.a9t1);
		InputStreamReader inputStreamReader = null;  
	    try {  
	        inputStreamReader = new InputStreamReader(inputStream, "gbk");  
	    } catch (UnsupportedEncodingException e1) {  
	        e1.printStackTrace();  
	    }  
	    BufferedReader reader = new BufferedReader(inputStreamReader);  
	    String line;  
	    String data_signal = "";
	    String data_total = "";
	    int limit = 1024;
	    int delta = LEN_OF_RECEIVED_DATA;
	    char c = '\t';
	    try {  
	        while ((line = reader.readLine()) != null) {  
	        	data_signal = line.split(String.valueOf(c))[0]+"d"+line.split(String.valueOf(c))[1]+"d"+line.split(String.valueOf(c))[2];
	        	data_total = data_total + data_signal + "&";
	        	delta -= 1;
	        	if(delta == 0){
	        		Intent intent = new Intent(DATA_SIMULATION);
	        		intent.putExtra("data", data_total);
	        		MyLog.w("sent","sleep sendBrocast");
	        		sendBroadcast(intent);
	        		data_signal = "";
	        		data_total = "";
	        		delta = LEN_OF_RECEIVED_DATA;
	        		MyLog.i("test", "sleep1");
	        		try {
	    				Thread.sleep(20);
	    			} catch (InterruptedException e) {
	    				// TODO Auto-generated catch block
	    				MyLog.i("DataH", "InterruptedException");
	    				e.printStackTrace();
	    			}
	        		MyLog.i("test", "sleep2");
	        	}
	            limit--;
	        	if(limit == 0){
	        		break;
	        	}
	        }  
	    } catch (IOException e) {  
	        e.printStackTrace();  	
	    } 
	}
	
	public void saveData(String data){
		if(HeadWear.label <= HeadWear.LABEL_NUM && HeadWear.label >= 1){
			String sql = "insert into acceleration_data(label, data, recv_time) values("+ HeadWear.label +",'" + data + "'," + System.currentTimeMillis() + ")";
			MyLog.w(TAG,sql);
			sqlitedb.execSQL(sql);
		}
	}
	
	public void dataHandler(String data){
		MyLog.w(TAG,"dataHandler:" + data);
		saveData(data);
		float[] x = new float[LEN_OF_RECEIVED_DATA];
		float[] y = new float[LEN_OF_RECEIVED_DATA];
		float[] z = new float[LEN_OF_RECEIVED_DATA];
		if(!simulation){
			int d = 0;
			for(int i = 0 ; i < LEN_OF_RECEIVED_DATA; i++){
				d = 6 * i;
				x[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 2)) + String.valueOf(data.charAt(d + 3)),16);
				y[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 4)) + String.valueOf(data.charAt(d + 5)),16);
				z[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 6)) + String.valueOf(data.charAt(d + 7)),16);
				if(train_nn){
					predictNN(x[i], y[i], z[i]);
				}
			}
		}else{
			String[] data_signal = new String[LEN_OF_RECEIVED_DATA];
			data_signal = data.split("&");
			//if(DEBUG)MyLog.w(TAG,"dataHandler data: " + data);
			BitSet bit = new BitSet(100);
			bit.set(1);
			for(int i = 0; i < LEN_OF_RECEIVED_DATA; i++){
				//MyLog.w(TAG,data_signal[i]);
				x[i] = (float)Double.parseDouble(data_signal[i].split("d")[0]);
				y[i] = (float)Double.parseDouble(data_signal[i].split("d")[1]);
				z[i] = (float)Double.parseDouble(data_signal[i].split("d")[2]);
				if(train_nn){
					MyLog.i("DataH", "train_nn = true");
					getFeature(x[i], y[i], z[i]);
				}
			}
		}
		if(HeadWear.viewAcceleration){
			Intent intent = new Intent(HeadWear.DRAW_BARCHART);
			intent.putExtra("X", x);
			intent.putExtra("Y", y);
			intent.putExtra("Z", z);
			sendBroadcast(intent);
		}
	}
	
	//原始数据，用以计算特征值
	MyDatas.SignalData data1 = new MyDatas().new SignalData();
	MyDatas.SignalData data2 = new MyDatas().new SignalData();
	public float[] getFeature(float x, float y, float z){
		//MyLog.i("DataHandlerService.getFeature", "getFeature");
		//data1 走了半个窗长后才开始使用data2
		float[] feature = null;
		if(data1.len == MyDatas.HALF_OF_SIGNAL_DATA){
			data2.used = true;
		}
		data1.used = true;
		if(data1.used){
			//MyLog.i("used", "data1.used = true");
			if(!data1.using){
				//MyLog.i("using", "data1.using = false");
				data1.enData(x, y, z);
				//MyLog.w("test",""+sd1.len);
				if(data1.len == MyDatas.LEN_OF_SIGNAL_DATA){
					isCalculate = true;
					data1.calculate();
					feature = data1.feature2list();
					data1.resetDatas();
					isCalculate = false;
				}
			}
		}
		if(data2.used){
			if(!data2.using){
				data2.enData(x, y, z);
				if(data2.len == MyDatas.LEN_OF_SIGNAL_DATA){
					isCalculate = true;
					data2.calculate();
					feature = data2.feature2list();
					data2.resetDatas();
					isCalculate = false;
				}
			}
		}
		return feature;
	}
	
	public void trainNN(){
		MyLog.i("DataHandlerService.trainNN", "trainNN");
		Cursor c;
		String data = "";
		String sql = "";
		float x, y, z;
		float[] feature;
		ArrayList<float[]> array_list_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_y = new ArrayList<Integer>();
		for(int j = 1; j <= HeadWear.LABEL_NUM -4; j++)
		{
			sql = "select * from acceleration_data where id > 1";//label = " + j;
			c = sqlitedb.rawQuery(sql, new String[]{});
			int k = 0;
	        while(c.moveToNext())
	        {
	        	MyLog.i("kkkkkk", "kkkkk:" + k);
	        	k++;
	        	data = c.getString(c.getColumnIndex("data"));
	        	if(!simulation){
	    			int d = 0;
	    			for(int i = 0 ; i < LEN_OF_RECEIVED_DATA; i++){
	    				d = 6 * i;
	    				x = (float) Integer.parseInt(String.valueOf(data.charAt(d + 2)) + String.valueOf(data.charAt(d + 3)),16);
	    				y = (float) Integer.parseInt(String.valueOf(data.charAt(d + 4)) + String.valueOf(data.charAt(d + 5)),16);
	    				z = (float) Integer.parseInt(String.valueOf(data.charAt(d + 6)) + String.valueOf(data.charAt(d + 7)),16);
	    				feature = getFeature(x, y, z);
	    				if(feature != null){
	    					array_list_x.add(feature);
	    					array_list_y.add(i);
	    				}
	    			}
	    		}else{
	    			String[] data_signal = new String[LEN_OF_RECEIVED_DATA];
	    			data_signal = data.split("&");
	    			for(int i = 0; i < LEN_OF_RECEIVED_DATA; i++){
	    				x = (float)Double.parseDouble(data_signal[i].split("d")[0]);
	    				y = (float)Double.parseDouble(data_signal[i].split("d")[1]);
	    				z = (float)Double.parseDouble(data_signal[i].split("d")[2]);
	    				feature = getFeature(x, y, z);
	    				if(feature != null){
	    					MyLog.i("DataH", "feature != null");
	    					array_list_x.add(feature);
	    					array_list_y.add(i);
	    				}
	    			}
	    		}//if(!simulation)
	        }//while(c.moveToNext())
	        c.close();
		}//for(int j = 1; j <= HeadWear.LABEL_NUM; j++)
		float[][] X = new float[array_list_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_x.size(); i++){
			X[i] = array_list_x.get(i);
		}
		float[][] Y = new float[array_list_y.size()][HeadWear.LABEL_NUM];
		int label = 0;
		for(int i = 0; i < array_list_y.size(); i++){
			label = array_list_y.get(i) - 1;
			for(int j = 0; j < HeadWear.LABEL_NUM; j++){
				if(j == label){
					Y[i][j] = 1;
				}else{
					Y[i][j] = 0;
				}
			}
		}
		mNeuralNetwork.view(X);
		mNeuralNetwork.view(Y);
		mNeuralNetwork.setDatas(X,Y);
		mNeuralNetwork.train(500);
		MyLog.i("datah", "trainNN finish. reset data1, data2");
		//data1.resetDatas();
		//data2.resetDatas();
		train_nn = true;
	}
	
	
	public void predictNN(float x, float y, float z){
		float[] f = getFeature(x, y, z);
		if(f != null){
			int pred = mNeuralNetwork.predict(f);
			MyLog.i("DataHandlerService.predict", "predict:" + pred);
		}
	}
	 
	public class LocalBinder extends Binder {
        DataHandlerService getService() {
            return DataHandlerService.this;
        }
    }
	
	private final IBinder mBinder = new LocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		if(DEBUG)MyLog.w(TAG,"onBind");
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		sqlitedb.close();
		unregisterReceiver(mReceiver);
		return super.onUnbind(intent);
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if(action.equals(DATA_SIMULATION)){
				final String data = intent.getStringExtra("data");
				MyLog.i("DataH", "send receive data:");
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						if(!isCalculate){
							dataHandler(data);
						}else{
							MyLog.i("DataH", "calculating");
						}
							
					}
				}).start();
			}else if(action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)){
				final String data = intent.getStringExtra("data");
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						dataHandler(data);
					}
				}).start();
			}
		}
	};
	
	private static IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DATA_SIMULATION);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}
	
}