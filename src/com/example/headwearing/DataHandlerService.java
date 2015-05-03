package com.example.headwearing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
	public static int LEN_OF_RECEIVED_DATA = 5;
	private final static String TAG = "testDataHandlerSerivce";
	public final static String DATA_SIMULATION = "DATA SIMULATION";
	public final static String DATA_RECEIVE = "DATA RECEIVE";
	private SQLiteDatabase sqlitedb;
	private File path = new File("/sdcard/dbfile/"); //数据库文件目录  
    private File file = new File("/sdcard/dbfile/headwearing.db"); //数据库文件  
	
    public boolean init(){
		registerReceiver(mReceiver, makeIntentFilter());
		if(simulation){
			new Thread(new Runnable() {                    
				@Override
				public void run() {
					//dataSimulation();
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
		sqlitedb.execSQL("delete from acceleration_data where id > 1");
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
	        		sendBroadcast(intent);
	        		data_signal = "";
	        		data_total = "";
	        		delta = LEN_OF_RECEIVED_DATA;
	        	}
        		try {
    				Thread.sleep(10);
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
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
		if(HeadWear.label <= 5 && HeadWear.label >= 1){
			String sql = "insert into acceleration_data(label, data, recv_time) values("+ HeadWear.label +",'" + data + "'," + System.currentTimeMillis() + ")";
			MyLog.w(TAG,sql);
			sqlitedb.execSQL(sql);
		}
	}
	
	MyDatas.SignalData sd1 = new MyDatas().new SignalData();
	MyDatas.SignalData sd2 = new MyDatas().new SignalData();
	
	public void dataHandler(String data){
		MyLog.w(TAG,"dataHandler:" + data);
		saveData(data);
		float[] x = new float[LEN_OF_RECEIVED_DATA];
		float[] y = new float[LEN_OF_RECEIVED_DATA];
		float[] z = new float[LEN_OF_RECEIVED_DATA];
		boolean simulation = false;
		if(!simulation){
			int d = 0;
			for(int i = 0 ; i < LEN_OF_RECEIVED_DATA; i++){
				d = 6 * i;
				MyLog.i("dataHandler : d : ", "" + d + " data:" + data);
				x[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 2)) + String.valueOf(data.charAt(d + 3)),16);
				y[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 4)) + String.valueOf(data.charAt(d + 5)),16);
				z[i] = (float) Integer.parseInt(String.valueOf(data.charAt(d + 6)) + String.valueOf(data.charAt(d + 7)),16);
				train(x[i], y[i], z[i]);
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
				if(sd1.len == MyDatas.HALF_OF_SIGNAL_DATA){
					sd2.used = true;
				}
				sd1.used = true;
				if(sd1.used){
					sd1.enData(x[i],y[i],z[i]);
					//MyLog.w("test",""+sd1.len);
					if(sd1.len == MyDatas.LEN_OF_SIGNAL_DATA){
						sd1.calculate();
						sd1.resetDatas();
					}
				}
				if(sd2.used){
					sd2.enData(x[i],y[i],z[i]);
					if(sd2.len == MyDatas.LEN_OF_SIGNAL_DATA){
						sd2.calculate();
						sd2.resetDatas();
					}
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
	public void train(float x, float y, float z){
		//data1 走了半个窗长后才开始使用data2
		if(data1.len == MyDatas.HALF_OF_SIGNAL_DATA){
			data2.used = true;
		}
		data1.used = true;
		if(data1.used){
			if(!data1.using){
				data1.enData(x, y, z);
				//MyLog.w("test",""+sd1.len);
				if(data1.len == MyDatas.LEN_OF_SIGNAL_DATA){
					data1.calculate();
					data1.resetDatas();
					data1.feature2list();
				}
			}
		}
		if(data2.used){
			if(!data2.using){
				data2.enData(x, y, z);
				if(data2.len == MyDatas.LEN_OF_SIGNAL_DATA){
					data2.calculate();
					data2.resetDatas();
					data2.feature2list();
				}
			}
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
				
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						dataHandler(data);
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