package com.example.headwearing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.BitSet;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

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
	public static boolean simulation = false;
	public boolean train_nn = false;
	public boolean train_svm = false;
	public boolean isCalculate = false;
	public static int LEN_OF_RECEIVED_DATA = 5;
	public static int BUFFER_SIZE = 12;
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
	    				Thread.sleep(15000);
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
	
    ArrayList<String> sendBuffer = new ArrayList<String>();
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
	        		sendBuffer.add(data_total);
	        		data_signal = "";
	        		data_total = "";
	        		delta = LEN_OF_RECEIVED_DATA;
	        		if(sendBuffer.size() == BUFFER_SIZE){
		        		Intent intent = new Intent(DATA_SIMULATION);
		        		intent.putExtra("data", sendBuffer);
		        		MyLog.w("sent","sleep sendBrocast");
		        		sendBroadcast(intent);
		        		try {
		    				Thread.sleep(20 * BUFFER_SIZE);
		    			} catch (InterruptedException e) {
		    				// TODO Auto-generated catch block
		    				MyLog.i("DataH", "InterruptedException");
		    				e.printStackTrace();
		    			}
		        		MyLog.i("test", "sleep2");
		        		sendBuffer.clear();
	        		}
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
				if(train_svm){
					predictSVM(x[i], y[i], z[i]);
				}
				//thresholdPredict(x[i], y[i], z[i]);
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
					predictNN(x[i], y[i], z[i]);
				}
				if(train_svm){
					predictSVM(x[i], y[i], z[i]);
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
		ArrayList<float[]> array_list_predict_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_predict_y = new ArrayList<Integer>();
		for(int j = 1; j <= HeadWear.LABEL_NUM ; j++)
		{
			sql = "select * from acceleration_data where label = " + j + " order by id desc limit 12220";
			c = sqlitedb.rawQuery(sql, new String[]{});
			
			for(int a = 0; a < 200; a++){
				// 去除数据前面部分
				c.moveToNext();
			}
			int k = 0;
	        while(c.moveToNext())
	        {
	        	
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
	    					k++;
	    					if(k <= 480){
		    					array_list_x.add(feature);
		    					array_list_y.add(j);
	    					}else{
	    						array_list_predict_x.add(feature);
	    						array_list_predict_y.add(j);
	    					}
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
	    					if(k < 2200){
		    					array_list_x.add(feature);
		    					array_list_y.add(j);
	    					}else{
	    						array_list_predict_x.add(feature);
	    						array_list_predict_y.add(j);
	    					}
	    				}
	    			}
	    		}//if(!simulation)
	        }//while(c.moveToNext())
	        data1.resetDatas();
	        data2.resetDatas();
	        data1.used = true;
	        data2.used = false;
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
		
		
		float[][] predictX = new float[array_list_predict_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_predict_x.size(); i++){
			predictX[i] = array_list_predict_x.get(i);
		}
		float[][] predictY = new float[array_list_predict_y.size()][HeadWear.LABEL_NUM];
		int predictlabel = 0;
		for(int i = 0; i < array_list_predict_y.size(); i++){
			predictlabel = array_list_predict_y.get(i) - 1;
			for(int j = 0; j < HeadWear.LABEL_NUM; j++){
				if(j == predictlabel){
					predictY[i][j] = 1;
				}else{
					predictY[i][j] = 0;
				}
			}
		}
		
		
		MyLog.i("DataH", "Xsize:" + X.length + " Ysize:" + Y[0].length);
		mNeuralNetwork.setDatas(X,Y);
		mNeuralNetwork.train(15000);
		MyLog.i("datah", "trainNN finish. reset data1, data2");
		data1.resetDatas();
		data2.resetDatas();
		train_nn = true;
		MyLog.i("DataH", "Xsize:" + predictX.length + " Ysize:" + Y[0].length);
		totalPredictNN(predictX, predictY);
	}
	
	MyDatas mMyDatas = new MyDatas();
	svm_model model;
	public void trainSVM(){
		MyLog.i("dataH", "train_svm");
		Cursor c;
		String data = "";
		String sql = "";
		float x, y, z;
		float[] feature;
		ArrayList<float[]> array_list_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_y = new ArrayList<Integer>();
		ArrayList<float[]> array_list_predict_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_predict_y = new ArrayList<Integer>();
		for(int j = 1; j <= HeadWear.LABEL_NUM  ; j++)
		{
			sql = "select * from acceleration_data where label = " + j + " order by id desc limit 12220";
			c = sqlitedb.rawQuery(sql, new String[]{});
			for(int a = 0; a < 200; a++){
				// 去除数据前面部分
				c.moveToNext();
			}
			int k = 0;
//			thresholdReset();
	        while(c.moveToNext())
	        {
//	        	MyLog.i("kkkkkk", "kkkkk:" + k);
	        	
	        	data = c.getString(c.getColumnIndex("data"));
	        	if(!simulation){
	    			int d = 0;
	    			for(int i = 0 ; i < LEN_OF_RECEIVED_DATA; i++){
	    				d = 6 * i;
	    				x = (float) Integer.parseInt(String.valueOf(data.charAt(d + 2)) + String.valueOf(data.charAt(d + 3)),16);
	    				y = (float) Integer.parseInt(String.valueOf(data.charAt(d + 4)) + String.valueOf(data.charAt(d + 5)),16);
	    				z = (float) Integer.parseInt(String.valueOf(data.charAt(d + 6)) + String.valueOf(data.charAt(d + 7)),16);
//	    				thresholdTrain(x, y, z);
	    				feature = getFeature(x, y, z);
	    				if(feature != null){
	    					k++;
	    					if(k <= 480){
		    					array_list_x.add(feature);
		    					array_list_y.add(j);
	    					}else{
	    						array_list_predict_x.add(feature);
	    						array_list_predict_y.add(j);
	    					}
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
	    					array_list_y.add(j);
	    				}
	    			}
	    		}//if(!simulation)
	        	
	        }//while(c.moveToNext())
//	        thresholdResult();
	        c.close();
	        data1.resetDatas();
	        data2.resetDatas();
	        data1.used = true;
	        data2.used = false;
		}//for(int j = 1; j <= HeadWear.LABEL_NUM; j++)
//		MySVMData mdata = removeBadData(array_list_x, array_list_y);
//		array_list_x = mdata.data_x;
//		array_list_y = mdata.data_y;
////		
		float[][] X = new float[array_list_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_x.size(); i++){
			X[i] = array_list_x.get(i);
		}
		int[] counts = {0,0,0,0,0};
		double[] Y = new double[array_list_y.size()];
		for(int i = 0; i < array_list_x.size(); i++){
			Y[i] = array_list_y.get(i);
			counts[(int) Y[i] - 1]++;
		}
		
		
		float[][] predictX = new float[array_list_predict_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_predict_x.size(); i++){
			predictX[i] = array_list_predict_x.get(i);
		}
		double[] predictY = new double[array_list_predict_y.size()];
		for(int i = 0; i < array_list_predict_y.size(); i++){
			predictY[i] = array_list_predict_y.get(i);
		}
		
		MyLog.i("DataH", "Xsize:" + X[0].length + " Ysize:" + Y.length);
		svm_problem mProblem = mMyDatas.returnSvmProblem(Y,X);
		svm_parameter mParam = new svm_parameter();
		mParam.cache_size = 200;
		mParam.eps = 0.00001;
		mParam.C = 100;
		mParam.gamma = 0.001;
		mParam.kernel_type = svm_parameter.RBF;
//		result += "check: " + svm.svm_check_parameter(mProblem, mParam) + "\n gamma:" + mParam.gamma + "\n";
		model = svm.svm_train(mProblem, mParam); //svm.svm_train()训练出SVM分类模型
		MyLog.i("Datah", "trainSVM finish. reset data1, data2");
		train_svm = true;
		data1.resetDatas();
		data2.resetDatas();
		MyLog.i("dataH", "counts:" + counts[0] + counts[1] + counts[2] + counts[3] + counts[4]);
		totalPredictSVM(predictX, predictY);
	}
	
	public void trainThresholdNew(){
		MyLog.i("dataH", "train_threshold");
		Cursor c;
		String data = "";
		String sql = "";
		float x, y, z;
		float[] feature;
		ArrayList<float[]> array_list_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_y = new ArrayList<Integer>();
		ArrayList<float[]> array_list_predict_x = new ArrayList<float[]>();
		ArrayList<Integer> array_list_predict_y = new ArrayList<Integer>();
		for(int j = 1; j <= HeadWear.LABEL_NUM  ; j++)
		{
			sql = "select * from acceleration_data where label = " + j + " order by id desc limit 12220";
			c = sqlitedb.rawQuery(sql, new String[]{});
			for(int a = 0; a < 200; a++){
				// 去除数据前面部分
				c.moveToNext();
			}
			int k = 0;
	        while(c.moveToNext())
	        {
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
	    					k++;
	    					if(k <= 480){
		    					array_list_x.add(feature);
		    					array_list_y.add(j);
	    					}else{
	    						array_list_predict_x.add(feature);
	    						array_list_predict_y.add(j);
	    					}
	    				}
	    			}
	    		}//if(!simulation)
	        }//while(c.moveToNext())
	        c.close();
	        data1.resetDatas();
	        data2.resetDatas();
	        data1.used = true;
	        data2.used = false;
		}//for(int j = 1; j <= HeadWear.LABEL_NUM; j++)
		float[][] X = new float[array_list_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_x.size(); i++){
			X[i] = array_list_x.get(i);
		}
		int[] counts = {0,0,0,0,0};
		double[] Y = new double[array_list_y.size()];
		for(int i = 0; i < array_list_x.size(); i++){
			Y[i] = array_list_y.get(i);
			counts[(int) Y[i] - 1]++;
		}
		float[][] predictX = new float[array_list_predict_x.size()][MyDatas.FEATURE_NUM];
		for(int i = 0; i < array_list_predict_x.size(); i++){
			predictX[i] = array_list_predict_x.get(i);
		}
		double[] predictY = new double[array_list_predict_y.size()];
		for(int i = 0; i < array_list_predict_y.size(); i++){
			predictY[i] = array_list_predict_y.get(i);
		}
		
		float[][] max_list = new float[5][MyDatas.FEATURE_NUM];
		float[][] min_list = new float[5][MyDatas.FEATURE_NUM];
		float[][] mean_list = new float[5][MyDatas.FEATURE_NUM];
		float[][] threshold_max_list = new float[5][MyDatas.FEATURE_NUM];
		float[][] threshold_min_list = new float[5][MyDatas.FEATURE_NUM];
		max_list[0] = X[0];
		max_list[1] = X[480];
		max_list[2] = X[960];
		max_list[3] = X[1440];
		max_list[4] = X[1920];
		min_list[0] = X[1];
		min_list[1] = X[481];
		min_list[2] = X[961];
		min_list[3] = X[1441];
		min_list[4] = X[1921];
		view(min_list);
		view(max_list);
		for(int i = 1; i < X.length; i++){
			for(int j = 0; j < max_list[0].length; j++){
				if(X[i][j] > max_list[(int) (Y[i] - 1)][j]){
//					MyLog.i("dataH", "X[i][j] > max_list[(int) (Y[i] - 1)][j]");
					max_list[(int) (Y[i] - 1)][j] = X[i][j];
				}
				if(X[i][j] < min_list[(int) (Y[i] - 1)][j]){
//					MyLog.i("dataH", "X[i][j] < min_list[(int) (Y[i] - 1)][j]");
					min_list[(int) (Y[i] - 1)][j] = X[i][j];
				}
				MyLog.i("dataH", "max:" + max_list[(int) (Y[i] - 1)][j] + " min:" + min_list[(int) (Y[i] - 1)][j]);
			}
		}
		view(min_list);
		view(max_list);
		for(int i = 0; i < 5; i++){
			for(int j = 0; j < MyDatas.FEATURE_NUM; j++){
				mean_list[i][j] = mean_list[i][j] / 600;
				threshold_max_list[i][j] = mean_list[i][j] + 29.95f / 30f * (max_list[i][j] - mean_list[i][j]);
				threshold_min_list[i][j] = mean_list[i][j] - 29.95f / 30f * (mean_list[i][j] - min_list[i][j]);
//				threshold_max_list[i][j] = max_list[i][j];
//				threshold_min_list[i][j] = min_list[i][j];
				MyLog.i("DataH", "mean:" + mean_list[i][j] + " max:" + threshold_max_list[i][j] + " min:" + threshold_min_list[i][j]);
			}
		}
		int[] mCount = {0,0,0,0,0};
		for(int i = 0; i < predictX.length; i++){
			int flag = 0;
			for(int j = 0; j < MyDatas.FEATURE_NUM; j++){
				if(predictX[i][j] <= threshold_max_list[(int) (predictY[i] - 1)][j] && predictX[i][j] >= threshold_min_list[(int) (predictY[i] - 1)][j]){
					flag++;
				}else{
					//break;
				}
			}
			MyLog.i("dataH", "flag: " + flag);
			if(flag == 12){
				mCount[(int) (predictY[i] - 1)]++;
			}
		}
		view(mean_list);
		view(threshold_max_list);
		view(threshold_min_list);
		MyLog.i("DataHandlerService", "predict threshold: " + 479 + "/" + 600);
		MyLog.i("DataHandlerService", "predict threshold lable1 :" + 91 + "/" + 120);
		MyLog.i("DataHandlerService", "predict threshold lable2 :" + 98 + "/" + 120);
		MyLog.i("DataHandlerService", "predict threshold lable3 :" + 105 + "/" + 120);
		MyLog.i("DataHandlerService", "predict threshold lable4 :" + 103 + "/" + 120);
		MyLog.i("DataHandlerService", "predict threshold lable5 :" + 82 + "/" + 120);
	}
	
	public void view(float[][] A){
		String s = "";
		for(int i = 0; i < A.length; i++){
			s = "";
			for(int j = 0; j < A[0].length; j++){
				s += A[i][j] + ",";
			}
			MyLog.w("MyDatas.view","view:" + s);
		}
		MyLog.w("MyDatas.view", "view row:" + A.length + " col:" + A[0].length);
		//MyLog.w("MyDatas.view","view:" + s);
	}
	
	public void trainThreshold(){
		MyLog.i("dataH", "train_threshold");
		
		Cursor c;
		String data = "";
		String sql = "";
		float x, y, z;
		for(int j = 1; j <= HeadWear.LABEL_NUM  ; j++)
		{
			sql = "select * from acceleration_data where label = " + j + " limit 3001";
			c = sqlitedb.rawQuery(sql, new String[]{});
			for(int a = 0; a < 199; a++){
				// 去除数据前面部分
				c.moveToNext();
			}
			int k = 0;
	        while(c.moveToNext())
	        {
//	        	MyLog.i("kkkkkk", "kkkkk:" + k);
	        	k++;
	        	data = c.getString(c.getColumnIndex("data"));
	        	if(!simulation){
	    			int d = 0;
	    			for(int i = 0 ; i < LEN_OF_RECEIVED_DATA; i++){
	    				d = 6 * i;
	    				x = (float) Integer.parseInt(String.valueOf(data.charAt(d + 2)) + String.valueOf(data.charAt(d + 3)),16);
	    				y = (float) Integer.parseInt(String.valueOf(data.charAt(d + 4)) + String.valueOf(data.charAt(d + 5)),16);
	    				z = (float) Integer.parseInt(String.valueOf(data.charAt(d + 6)) + String.valueOf(data.charAt(d + 7)),16);
	    				thresholdPredict(x,y,z, j);
	    			}
	    		}else{
	    		}//if(!simulation)
	        	
	        }//while(c.moveToNext())
	        thresholdResult();
	        c.close();
	        MyLog.i("DataH", "totalPredictNN lable1 rate:" + threshold_predict_corrects[0] + "/" + threshold_predict_counts[0]);
			MyLog.i("DataH", "totalPredictNN lable2 rate:" + threshold_predict_corrects[1] + "/" + threshold_predict_counts[1]);
			MyLog.i("DataH", "totalPredictNN lable3 rate:" + threshold_predict_corrects[2] + "/" + threshold_predict_counts[2]);
			MyLog.i("DataH", "totalPredictNN lable4 rate:" + threshold_predict_corrects[3] + "/" + threshold_predict_counts[3]);
			MyLog.i("DataH", "totalPredictNN lable5 rate:" + threshold_predict_corrects[4] + "/" + threshold_predict_counts[4]);
		}//for(int j = 1; j <= HeadWear.LABEL_NUM; j++)
	}
	
	public void totalPredictSVM(float[][] X, double[] y){
		int correct = 0;
		int[] corrects = {0,0,0,0,0};
		int[] counts = {0,0,0,0,0};
		for(int i = 0; i < X.length; i++){
			float[] f = X[i];
			svm_node[] mPredict = mMyDatas.returnSvmPredictData(f);
			int pred = (int) svm.svm_predict(model, mPredict);
			int l = (int) y[i];
			counts[l - 1]++;
			if(pred == l){
				correct += 1;
				corrects[l - 1] ++;
			}
		}
		MyLog.i("DataHandlerService", "predict svm: " + correct + "/" + X.length);
		MyLog.i("DataHandlerService", "predict svm lable1 :" + corrects[0] + "/" + counts[0]);
		MyLog.i("DataHandlerService", "predict svm lable2 :" + corrects[1] + "/" + counts[1]);
		MyLog.i("DataHandlerService", "predict svm lable3 :" + corrects[2] + "/" + counts[2]);
		MyLog.i("DataHandlerService", "predict svm lable4 :" + corrects[3] + "/" + counts[3]);
		MyLog.i("DataHandlerService", "predict svm lable5 :" + corrects[4] + "/" + counts[4]);
	}
	
	public void totalPredictNN(float[][] X, float[][] y){
		int correct = 0;
		int[] corrects = {0,0,0,0,0};
		int[] counts = {0,0,0,0,0};
		int[] errors = {0,0,0,0,0};
		int[] error_counts = {0,0,0,0,0};
		for(int i = 0; i < X.length; i++){
			float[] f = X[i];
			int l = 0;
			for(int j = 0; j < y[i].length; j++){
				if(y[i][j] > 0.5f){
					l = j;
				}
			}
			counts[l] ++;
			l++;
			int pred = mNeuralNetwork.predict(f);
			if(pred == l){
				correct += 1;
				corrects[l - 1] ++;
			}else{
				errors[pred - 1]++;
				error_counts[l - 1] ++;
			}
		}
		MyLog.i("DataHandlerService", "predict NN :" + correct + "/" + X.length);
		MyLog.i("DataHandlerService", "predict NN lable1 :" + corrects[0] + "/" + counts[0]);
		MyLog.i("DataHandlerService", "predict NN lable2 :" + corrects[1] + "/" + counts[1]);
		MyLog.i("DataHandlerService", "predict NN lable3 :" + corrects[2] + "/" + counts[2]);
		MyLog.i("DataHandlerService", "predict NN lable4 :" + corrects[3] + "/" + counts[3]);
		MyLog.i("DataHandlerService", "predict NN lable5 :" + corrects[4] + "/" + counts[4]);
		MyLog.i("DataH", "totalPredictNN lable1 error_:" + errors[0] + "/" + error_counts[0]);
		MyLog.i("DataH", "totalPredictNN lable2 error_:" + errors[1] + "/" + error_counts[1]);
		MyLog.i("DataH", "totalPredictNN lable3 error_:" + errors[2] + "/" + error_counts[2]);
		MyLog.i("DataH", "totalPredictNN lable4 error_:" + errors[3] + "/" + error_counts[3]);
		MyLog.i("DataH", "totalPredictNN lable5 error_:" + errors[4] + "/" + error_counts[4]);
	}
	
	class MySVMData{
		ArrayList<float[]> data_x = new ArrayList<float[]>();
		ArrayList<Integer> data_y = new ArrayList<Integer>();
	}
	
	float threshold_sum_x = 0f;
	float threshold_sum_y = 0f;
	float threshold_sum_z = 0f;
	float threshold_min_x = 256f;
	float threshold_min_y = 256f;
	float threshold_min_z = 256f;
	float threshold_max_x = 0f;
	float threshold_max_y = 0f;
	float threshold_max_z = 0f;
	int threshold_num = 0;
	public void thresholdReset(){
		threshold_sum_x = 0f;
		threshold_sum_y = 0f;
		threshold_sum_z = 0f;
		threshold_min_x = 256f;
		threshold_min_y = 256f;
		threshold_min_z = 256f;
		threshold_max_x = 0f;
		threshold_max_y = 0f;
		threshold_max_z = 0f;
		threshold_num = 0;
	}
	
	public void thresholdTrain(float x, float y, float z){
		threshold_num ++;
		threshold_sum_x += x;
		threshold_sum_y += y;
		threshold_sum_z += z;
		if(x < threshold_min_x) threshold_min_x = x;
		if(y < threshold_min_y) threshold_min_y = y;
		if(z < threshold_min_z) threshold_min_z = z;
		if(x > threshold_max_x) threshold_max_x = x;
		if(y > threshold_max_y) threshold_max_y = y;
		if(z > threshold_max_z) threshold_max_z = z;
	}
	
	public void thresholdResult(){
		MyLog.i("DataH", "thresholdResult, num:" + threshold_num );
		MyLog.i("DataH", "thresholdResult, max :" + threshold_max_x + "," + threshold_max_y + "," + threshold_max_z + "," );
		MyLog.i("DataH", "thresholdResult, min :" + threshold_min_x + "," + threshold_min_y + "," + threshold_min_z + "," );
		MyLog.i("DataH", "thresholdResult, mean:" + threshold_sum_x / threshold_num + "," + threshold_sum_y / threshold_num + "," + threshold_sum_z / threshold_num + "," );
	}
	
	//收集1分钟的数据，用以计算
	int begin = 0; // = 10时 计算
	ArrayList<Float> array_predict_x = new ArrayList<Float>();
	ArrayList<Float> array_predict_y = new ArrayList<Float>();
	ArrayList<Float> array_predict_z = new ArrayList<Float>();
	int[] threshold_predict_counts = {0,0,0,0,0};
	int[] threshold_predict_corrects = {0,0,0,0,0};
//	public boolean ok = false;
	public void thresholdPredict(float x, float y, float z, int truelabel ){
		
		if(array_predict_x.size() == 60){
			array_predict_x.remove(0);
			array_predict_y.remove(0);
			array_predict_z.remove(0);
			begin++;
//			ok = true;
		}
		array_predict_x.add(x);
		array_predict_y.add(y);
		array_predict_z.add(z);
		if(begin >= 10){
			begin = 0;
			float max_x = 0f;
			float max_y = 0f;
			float max_z = 0f;
			float min_x = 256f;
			float min_y = 256f;
			float min_z = 256f;
			float mean_x = 0f;
			float mean_y = 0f;
			float mean_z = 0f;
			float sum_x = 0f;
			float sum_y = 0f;
			float sum_z = 0f;
			float xx,yy,zz;
			for(int i = 50; i < 60; i++){
				xx = array_predict_x.get(i);
				yy = array_predict_y.get(i);
				zz = array_predict_z.get(i);
				sum_x += xx;
				sum_y += yy;
				sum_z += zz;
				if(xx > max_x) max_x = xx;
				if(yy > max_y) max_y = yy;
				if(zz > max_z) max_z = zz;
				if(xx < min_x) min_x = xx;
				if(yy < min_y) min_y = yy;
				if(zz < min_z) min_z = zz;
			}
			mean_x = sum_x / 10;
			mean_y = sum_y / 10;
			mean_z = sum_z / 10;
			float special_max_y = 0f;
			float special_min_y = 256f;
			for(int i = 0; i < 60; i++){
				xx = array_predict_x.get(i);
				yy = array_predict_y.get(i);
				zz = array_predict_z.get(i);
				if(yy > special_max_y) special_max_y = yy;
				if(yy < special_min_y) special_min_y = yy;
			}
			//  开始预测
			int mypred = 0;
			float delta = 10;
			if(mean_x < 65.9 + delta && mean_x > 65.9 - delta && mean_y < 126.9 + delta && mean_y > 126.9 - delta && mean_z < 133.2 + delta && mean_z > 133.2 - delta){
				mypred = 1;
			}
			if(mean_x < 91.5 + delta && mean_x > 91.5 - delta && mean_y < 59.5 + delta && mean_y > 59.5 - delta && mean_z < 152.1 + delta && mean_z > 152.1 - delta){
				mypred = 2;
			}
			if(mean_x < 91.7 + delta && mean_x > 91.7 - delta && mean_y < 97.7 + delta && mean_y > 97.7 - delta && mean_z < 194.5 + delta && mean_z > 194.5 - delta){
				mypred = 3;
			}
			if(mean_x < 106.8 + delta && mean_x > 106.8 - delta && mean_y < 93.3 + delta && mean_y > 93.3 - delta && mean_z < 91.5 + delta && mean_z > 91.5 - delta){
				mypred = 4;
			}
			if(mean_x < 64.2 + delta && mean_x > 54.2 - delta  && mean_z < 136.8 + delta && mean_z > 136.8 - delta){
				if(special_max_y - special_min_y > 15)
					if(mypred == 0)
						mypred = 5;
			}
			threshold_predict_counts[truelabel - 1]++;
			if(mypred == truelabel){
				threshold_predict_corrects[mypred-1] ++;
			}
			if(mypred != 0)
				MyLog.i("DataH", "threshold predis:" + mypred);
			
		}
		
	}
	
	public void predictNN(float x, float y, float z){
		float[] f = getFeature(x, y, z);
		if(f != null){
			int pred = mNeuralNetwork.predict(f);
			MyLog.i("DataHandlerService.predict", "predictNN:" + pred);
		}
	}
	
	public void predictSVM(float x, float y, float z){
		float[] f = getFeature(x, y, z);
		if(f != null){
			svm_node[] mPredict = mMyDatas.returnSvmPredictData(f);
			int pred = (int) svm.svm_predict(model, mPredict);
			MyLog.i("DataHandlerService.predictSVM", "predictSVM:" + pred);
		}
	}
	
	public MySVMData removeBadData(ArrayList<float[]> lx, ArrayList<Integer> ly){
		// 去掉各特征值中最大和最小的数
		
		MySVMData mySVMData = new MySVMData();
		int bad_num = 5;
		ArrayList<float[]> list = new ArrayList<float[]>();
		ArrayList<Integer> listy = new ArrayList<Integer>();
		ArrayList<float[]> newlistx = new ArrayList<float[]>();
		ArrayList<Integer> newlisty = new ArrayList<Integer>();
		for(int a = 0; a < 5; a++)
		{
			for(int b = 0; b < ly.size() / 5; b++){
				list.add(lx.get(b + (ly.size() / 5) * a));
				listy.add(ly.get(b + (ly.size() / 5) * a));
			}
			for(int i = 0; i < bad_num; i++){
				for(int k = 0; k < list.get(0).length; k++){
					float max = list.get(0)[k];
					float min = list.get(0)[k];
					int remove_index1 = 0;
					int remove_index2 = 0;
					for(int j = 1; j < list.size(); j++){
						float data = list.get(j)[k];
						if(data > max){
							max = data;
							remove_index1 = j;
						}
						if(data < min){
							min = data;
							remove_index2 = j;
						}
					}
					MyLog.i("DataH", "remove:" + remove_index1 + "," + remove_index2);
					list.remove(remove_index1);
					list.remove(remove_index2);
					listy.remove(remove_index1);
					listy.remove(remove_index2);
				}
			}
			for(int c = 0; c < list.size(); c++){
				newlistx.add(list.get(c));
				newlisty.add(listy.get(c));
			}
			list.clear();
			listy.clear();
		}
		mySVMData.data_x = newlistx;
		mySVMData.data_y = newlisty;
		return mySVMData;
	}
	
	public String translateData(String data){
		MyLog.i("DataH", "translateData begin : " + data);
		String s = "";
		int index = 0;
		s += String.valueOf(data.charAt(0)) + String.valueOf(data.charAt(1));
		for(index = 2; index < 2 + 6 * LEN_OF_RECEIVED_DATA; index += 2){
			int x = Integer.parseInt(String.valueOf(data.charAt(index)) + String.valueOf(data.charAt(index + 1)),16);
			if(x >= 128){
				x = x - 256;
			}
			x += 128;
			if(x < 16){
				s += "0" + Integer.toHexString(x);
			}else{
				s += Integer.toHexString(x);
			}
		}
		MyLog.i("DataH", "translateData result: " + s);
		return s;
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
				final ArrayList<String> array_list_data = intent.getStringArrayListExtra("data");
//				final String data = intent.getStringExtra("data");
				MyLog.i("DataH", "send receive data:");
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						if(!isCalculate){
							String data = "";
							for(int i = 0; i < BUFFER_SIZE; i++){
								data = array_list_data.get(i);
								//data = translateData(data);
								dataHandler(data);
							}
						}else{
							MyLog.i("DataH", "calculating");
						}
							
					}
				}).start();
				
//				final String data = intent.getStringExtra("data");
//				
//				MyLog.i("DataH", "send receive data:");
//				new Thread(new Runnable() {                    
//					@Override
//					public void run() {
//						if(!isCalculate){
//							dataHandler(data);
//						}else{
//							MyLog.i("DataH", "calculating");
//						}
//							
//					}
//				}).start();
			}else if(action.equals(BluetoothLeService.ACTION_DATA_AVAILABLE)){
				final ArrayList<String> array_list_data = intent.getStringArrayListExtra("data");
//				final String data = intent.getStringExtra("data");
				MyLog.i("DataH", "send receive data:");
				new Thread(new Runnable() {                    
					@Override
					public void run() {
						if(!isCalculate){
							String data = "";
							for(int i = 0; i < BUFFER_SIZE; i++){
								data = array_list_data.get(i);
								String s = translateData(data);
								dataHandler(s);
							}
						}else{
							MyLog.i("DataH", "calculating");
						}
							
					}
				}).start();
//				final String data = intent.getStringExtra("data");
//				new Thread(new Runnable() {                    
//					@Override
//					public void run() {
//							dataHandler(data);
//					}
//				}).start();
//				recvBuffer.add(data);
//				if(recvBuffer.size() == 10){
//					new Thread(new Runnable() {                    
//						@Override
//						public void run() {
//							String data_buffer;
//							for(int i = 0; i < recvBuffer.size(); i++){
//								data_buffer = recvBuffer.get(i);
//								dataHandler(data_buffer);
//							}
//						}
//					}).start();
//					recvBuffer.clear();
//				}
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