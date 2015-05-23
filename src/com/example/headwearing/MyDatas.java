package com.example.headwearing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;
import libsvm.svm_problem;
import libsvm.svm_parameter;
import libsvm.svm_node;

class MyDatas{
	public static int LEN_OF_SIGNAL_DATA = 200;
	public static int HALF_OF_SIGNAL_DATA = LEN_OF_SIGNAL_DATA / 2;
	public static int FEATURE_NUM = 12;
	public static String TAG = "MyDatas ";
	public class SignalData{
		ArrayList<Float> data_x = new ArrayList<Float>();
		ArrayList<Float> data_y = new ArrayList<Float>();
		ArrayList<Float> data_z = new ArrayList<Float>();
		int len = 0;
		float total_x_value = 0f;
		float total_y_value = 0f;
		float total_z_value = 0f;
		float mean_x_value = 0f;
		float mean_y_value = 0f;
		float mean_z_value = 0f;
		float n_variance_x_value = 0f;
		float n_variance_y_value = 0f;
		float n_variance_z_value = 0f;
		float standard_deviation_x_value = 0f;
		float standard_deviation_y_value = 0f;
		float standard_deviation_z_value = 0f;
		float skewness_x_value = 0f;
		float skewness_y_value = 0f;
		float skewness_z_value = 0f;
		float kurtosis_x_value = 0f;
		float kurtosis_y_value = 0f;
		float kurtosis_z_value = 0f;
		float correlation_x_y_value = 0f;
		float correlation_y_z_value = 0f;
		float correlation_z_x_value = 0f;
		public boolean used = false;
		public boolean using = false;
		public int error_time = 5;
		
		float delta_x = 0f;
		float delta_y = 0f;
		float delta_z = 0f;
		float boxingyinzi_x = 0f;
		float boxingyinzi_y = 0f;
		float boxingyinzi_z = 0f;
		float angle_x = 0f;
		float angle_y = 0f;
		float angle_z = 0f;
		
		public boolean enData(float x, float y, float z){
			//MyLog.i("MyDatas.endData", "enData ");
			if(!using){
				if(len < LEN_OF_SIGNAL_DATA){
					len++;
					data_x.add(x);
					data_y.add(y);
					data_z.add(z);
				}else{
					MyLog.w("test","else");
					data_x.remove(0);
					data_x.add(x);
					data_y.remove(0);
					data_y.add(y);
					data_z.remove(0);
					data_z.add(z);
				}
				//MyLog.w("test","endata"+len);
				return true;
			}else{
				MyLog.i("MyDatas","add data while using!");
				error_time--;
				if(error_time == 0){
					//resetDatas();
				}
				return false;
			}
			
		}
		
		public boolean resetDatas(){
			if(using){
				return false;
			}
//			MyLog.w(TAG,"resetDatas");
			data_x.clear();
			data_y.clear();
			data_z.clear();
			len = 0;
			total_x_value = 0f;
			total_y_value = 0f;
			total_z_value = 0f;
			mean_x_value = 0f;
			mean_y_value = 0f;
			mean_z_value = 0f;
			n_variance_x_value = 0f;
			n_variance_y_value = 0f;
			n_variance_z_value = 0f;
			standard_deviation_x_value = 0f;
			standard_deviation_y_value = 0f;
			standard_deviation_z_value = 0f;
			kurtosis_x_value = 0f;
			kurtosis_y_value = 0f;
			kurtosis_z_value = 0f;
			correlation_x_y_value = 0f;
			correlation_y_z_value = 0f;
			correlation_z_x_value = 0f;
			angle_x = 0f;
			angle_y = 0f;
			angle_z = 0f;
			//used = false;
			using = false;
			error_time =  5;
			return true;
		}
		
		public void calculate(){
			MyLog.w(TAG,"calculate");
			if(data_x.size() != LEN_OF_SIGNAL_DATA){
				MyLog.w("MyDatas.calculate", "size != LEN_OF_SIGNAL_DATA");
				return;
			}
			using = true;
			sum();
			meanValue();
			delta();
//			boxingyinzi();
			angle();
//			special();
			nVariance();
//			standardDeviation();
//			skewness();
//			kurtosis();
			correlation();
			using = false;
//			MyLog.w(TAG,"calculate over.");
			if(HeadWear.DEBUG){
//				MyLog.w("ViewFeature" + "meanValue:", "" + mean_x_value);
//				MyLog.w("ViewFeature" + "meanValue:", "" + mean_y_value);
//				MyLog.w("ViewFeature" + "meanValue:", "" + mean_z_value);
//				MyLog.w("ViewFeature" + "n_variance:Value", "" + n_variance_x_value);
//				MyLog.w("ViewFeature" + "n_varianceValue:", "" + n_variance_y_value);
//				MyLog.w("ViewFeature" + "n_varianceValue:", "" + n_variance_z_value);
//				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_x_value);
//				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_y_value);
//				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_z_value);
//				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_x_value);
//				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_y_value);
//				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_z_value);
//				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_x_y_value);
//				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_y_z_value);
//				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_z_x_value);
			}
		}
		
		public void angle(){
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				angle_x += Math.atan(data_x.get(i) / (Math.pow(data_y.get(i), 2) + Math.pow(data_z.get(i), 2) ));
				angle_y += Math.atan(data_y.get(i) / (Math.pow(data_x.get(i), 2) + Math.pow(data_z.get(i), 2) ));
				angle_z += Math.atan(Math.sqrt((Math.pow(data_x.get(i), 2) + Math.pow(data_y.get(i), 2) )) / data_z.get(i));
			}
			angle_x = angle_x / LEN_OF_SIGNAL_DATA;
			angle_y = angle_y / LEN_OF_SIGNAL_DATA;
			angle_z = angle_z / LEN_OF_SIGNAL_DATA;
			
		}
		
		public boolean correlation(){
			if(HeadWear.DEBUG){
//				MyLog.w(TAG,"method: correlation");
			}
			float d_x = 0f;
			float d_y = 0f;
			float d_z = 0f;
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				d_x = data_x.get(i) - mean_x_value;
				d_y = data_y.get(i) - mean_y_value;
				d_z = data_z.get(i) - mean_z_value;
				correlation_x_y_value += d_x * d_y;
				correlation_y_z_value += d_y * d_z;
				correlation_z_x_value += d_z * d_x;
			}
			correlation_x_y_value = (float) (correlation_x_y_value / (Math.sqrt(n_variance_x_value * n_variance_y_value)));
			correlation_y_z_value = (float) (correlation_y_z_value / (Math.sqrt(n_variance_y_value * n_variance_z_value)));
			correlation_z_x_value = (float) (correlation_z_x_value / (Math.sqrt(n_variance_z_value * n_variance_x_value)));
			return true;
		}
		
		public boolean kurtosis(){
			if(HeadWear.DEBUG){
				MyLog.w(TAG,"kurtosis");
			}
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				kurtosis_x_value += Math.pow((data_x.get(i) - mean_x_value),4);
				kurtosis_y_value += Math.pow((data_x.get(i) - mean_y_value),4);
				kurtosis_z_value += Math.pow((data_x.get(i) - mean_z_value),4);
			}
			kurtosis_x_value = (float) (kurtosis_x_value / (LEN_OF_SIGNAL_DATA * Math.pow(standard_deviation_x_value, 4)));
			kurtosis_y_value = (float) (kurtosis_y_value / (LEN_OF_SIGNAL_DATA * Math.pow(standard_deviation_y_value, 4)));
			kurtosis_z_value = (float) (kurtosis_z_value / (LEN_OF_SIGNAL_DATA * Math.pow(standard_deviation_z_value, 4)));
			return true;
		}
		
		public boolean skewness(){
			if(HeadWear.DEBUG){
				MyLog.w(TAG,"skewness");
			}
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				skewness_x_value += Math.pow((data_x.get(i) - mean_x_value),3);
				skewness_y_value += Math.pow((data_y.get(i) - mean_y_value),3);
				skewness_z_value += Math.pow((data_z.get(i) - mean_z_value),3);
			}
			skewness_x_value = (float) ((LEN_OF_SIGNAL_DATA * skewness_x_value) / ((LEN_OF_SIGNAL_DATA - 1) * (LEN_OF_SIGNAL_DATA - 2) * Math.pow(standard_deviation_x_value, 3)));
			skewness_y_value = (float) ((LEN_OF_SIGNAL_DATA * skewness_y_value) / ((LEN_OF_SIGNAL_DATA - 1) * (LEN_OF_SIGNAL_DATA - 2) * Math.pow(standard_deviation_y_value, 3)));
			skewness_z_value = (float) ((LEN_OF_SIGNAL_DATA * skewness_z_value) / ((LEN_OF_SIGNAL_DATA - 1) * (LEN_OF_SIGNAL_DATA - 2) * Math.pow(standard_deviation_z_value, 3)));
			return true;
		}
		
		public boolean standardDeviation(){
			MyLog.w(TAG,"standardDeviation");
			standard_deviation_x_value = (float) Math.sqrt(n_variance_x_value / LEN_OF_SIGNAL_DATA);
			standard_deviation_y_value = (float) Math.sqrt(n_variance_y_value / LEN_OF_SIGNAL_DATA);
			standard_deviation_z_value = (float) Math.sqrt(n_variance_z_value / LEN_OF_SIGNAL_DATA);
			return true;
		}
		
		public boolean sum(){
//			MyLog.w(TAG,"sum");
			if(len == LEN_OF_SIGNAL_DATA){
				for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
					total_x_value += data_x.get(i);
					total_y_value += data_y.get(i);
					total_z_value += data_z.get(i);
				}
				return true;
			}else{
				return false;
			}
		}
		
		public boolean meanValue(){
//			MyLog.w(TAG,"meanValue");
			if(len == LEN_OF_SIGNAL_DATA){
				mean_x_value = total_x_value / LEN_OF_SIGNAL_DATA;
				mean_y_value = total_y_value / LEN_OF_SIGNAL_DATA;
				mean_z_value = total_z_value / LEN_OF_SIGNAL_DATA;
				return true;
			}else{
				return false;
			}
		}
		
		public boolean nVariance(){
//			MyLog.w(TAG,"nVariance");
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				n_variance_x_value += Math.pow((data_x.get(i) - mean_x_value),2);
				n_variance_y_value += Math.pow((data_y.get(i) - mean_y_value),2);
				n_variance_z_value += Math.pow((data_z.get(i) - mean_z_value),2);
			}
			return true;
		}
	
		public boolean delta(){
			float max_x = 0f;
			float max_y = 0f;
			float max_z = 0f;
			float min_x = 256f;
			float min_y = 256f;
			float min_z = 256f;
			float xx,yy,zz;
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				xx = data_x.get(i);
				yy = data_y.get(i);
				zz = data_z.get(i);
				if(xx > max_x) max_x = xx;
				if(yy > max_y) max_y = yy;
				if(zz > max_z) max_z = zz;
				if(xx < min_x) min_x = xx;
				if(yy < min_y) min_y = yy;
				if(zz < min_z) min_z = zz;
			}
			delta_x = max_x - min_x;
			delta_y = max_y - min_y;
			delta_z = max_z - min_z;
			return true;
		}
		
		public boolean boxingyinzi(){
			float max_x = 0f;
			float max_y = 0f;
			float max_z = 0f;
			float min_x = 256f;
			float min_y = 256f;
			float min_z = 256f;
			float xx,yy,zz;
			for(int i = 0; i < HALF_OF_SIGNAL_DATA; i++){
				xx = data_x.get(i);
				yy = data_y.get(i);
				zz = data_z.get(i);
				if(xx > max_x) max_x = xx;
				if(yy > max_y) max_y = yy;
				if(zz > max_z) max_z = zz;
				if(xx < min_x) min_x = xx;
				if(yy < min_y) min_y = yy;
				if(zz < min_z) min_z = zz;
			}
			boxingyinzi_x = max_x - min_x;
			boxingyinzi_y = max_y - min_y;
			boxingyinzi_z = max_z - min_z;
			max_x = 0f;
			max_y = 0f;
			max_z = 0f;
			min_x = 256f;
			min_y = 256f;
			min_z = 256f;
			
			for(int i = HALF_OF_SIGNAL_DATA; i < LEN_OF_SIGNAL_DATA; i++){
				xx = data_x.get(i);
				yy = data_y.get(i);
				zz = data_z.get(i);
				if(xx > max_x) max_x = xx;
				if(yy > max_y) max_y = yy;
				if(zz > max_z) max_z = zz;
				if(xx < min_x) min_x = xx;
				if(yy < min_y) min_y = yy;
				if(zz < min_z) min_z = zz;
			}
			boxingyinzi_x += max_x - min_x;
			boxingyinzi_y += max_y - min_y;
			boxingyinzi_z += max_z - min_z;
			return true;
		}
		
		public float special_y = 0f;
		public void special(){
			int up = 0;
			int down = 0;
			int len = 15;
			for(int i = 0; i < LEN_OF_SIGNAL_DATA - len - 1; i++){
				for(int j = i; j < i + len + 1; j++){
					if(data_y.get(j) > data_y.get(j + 1))
						break;
					if(j == i + len){
						up++;
					}
				}
				for(int j = i; j < i + len + 1; j++){
					if(data_y.get(j) < data_y.get(j + 1))
						break;
					if(j == i + len){
						down++;
					}
				}
			}
			special_y = up + down;
		}
		
		public float[] feature2list(){
			float[] f = new float[FEATURE_NUM];
			boolean inTest = false;
			if(inTest){
				f = new float[300];
				for(int i = 0; i < 100; i++){
					f[i] = data_x.get(i);
				}
				for(int i = 100; i < 200; i++){
					f[i] = data_y.get(i-100);
				}
				for(int i = 200; i < 300; i++){
					f[i] = data_z.get(i-200);
				}
				//layer2_num = 25;
			}else{
//			float[] f = new float[FEATURE_NUM];
			f[0] = mean_x_value; //standard_deviation_x_value;
			f[1] = mean_y_value; //standard_deviation_y_value;
			f[2] = mean_z_value; //standard_deviation_z_value;
			f[3] = delta_x;
			f[4] = delta_y;
			f[5] = delta_z;
			f[6] = angle_x;
			f[7] = angle_y;
			f[8] = angle_z;
			f[9] = correlation_x_y_value;
			f[10] = correlation_y_z_value;
			f[11] = correlation_z_x_value;
//			f[6] = mean_x_value - mean_y_value;// Math.abs(correlation_x_y_value);
//			f[7] = mean_y_value - mean_z_value;// Math.abs(correlation_y_z_value);
//			f[8] = mean_z_value - mean_x_value;// Math.abs(correlation_z_x_value);
//			//f[9] = boxingyinzi_x;
//			f[9] = boxingyinzi_y;
//			f[10] = special_y;
			//f[11] = boxingyinzi_z;
//			f[3] = skewness_x_value;
//			f[4] = skewness_y_value;
//			f[5] = skewness_z_value;
//			f[6] = kurtosis_x_value;
//			f[7] = kurtosis_y_value;
//			f[8] = kurtosis_z_value;
//			f[9] = correlation_x_y_value;
//			f[10] = correlation_y_z_value;
//			f[11] = correlation_z_x_value;
			String s = "";
			for(int i = 0; i < FEATURE_NUM; i++){
				s += "f[" + i + "]:" + f[i] + " &";
			}
//			MyLog.i("MyDatas.feature2list",s);
			}
			return f;
		}
	}
	
	public class NeuralNetwork{
		//神经网络定义为3层，每层10个单元加一个bias
		//输入层有12个参数加一个bias
		//一个中间层，为10
		//输出层向量为5
		public static final int hidden_layer_num = 2;
		public static final int layer1_num = 12;
		public static final int layer2_num = 10;
		public static final int layer3_num = 5; //10;
		//public static final int layer4_num = 5;
		public static final float lambda = 0.01f, alpha = 0.01f;
		public float[][] input_x, input_y;
		public float[][] W1, gradW1;// = new float[layer1_num][layer2_num];
		public float[] b1, z2, a2, delta2, gradb1;// = new float[layer2_num];
		public float[][] W2, gradW2;// = new float[layer2_num][layer3_num];
		public float[] b2, z3, a3, h, delta3, gradb2;// = new float[layer3_num];
		//public float[][] W3, gradW3 = new float[layer3_num + 1][layer4_num];
		
		public void init(){
			W1 = new float[layer1_num][layer2_num];
			gradW1 = new float[layer1_num][layer2_num];
			b1 = new float[layer2_num];
			z2 = new float[layer2_num];
			a2 = new float[layer2_num];
			delta2 = new float[layer2_num];
			gradb1 = new float[layer2_num];
			W2 = new float[layer2_num][layer3_num];
			gradW2 = new float[layer2_num][layer3_num];
			b2 = new float[layer3_num];
			z3 = new float[layer3_num];
			a3 = new float[layer3_num];
			h = new float[layer3_num];
			delta3 = new float[layer3_num];
			gradb2 = new float[layer3_num];
			for(int i = 0; i < layer2_num; i++){
				for(int j = 0; j < layer1_num; j++){
					W1[j][i] = (float) ((Math.random() - 0.5) * 0.1);
				}
				b1[i] = (float) ((Math.random() - 0.5) * 0.1);
			}
			for(int i = 0; i < layer3_num; i++){
				for(int j = 0; j < layer2_num; j++){
					W2[j][i] = (float) ((Math.random() - 0.5) * 0.1);
				}
				b2[i] = (float) ((Math.random() - 0.5) * 0.1);
			}
		}
		
		public void train(int iteration, float[][] input_xx, float[][] input_yy){
			boolean inTest = true;
			if(inTest){
				initTestData();
			}else{
				input_x = input_xx;
				input_y = input_yy;
			}
			float cost = 0f;
			for(int i = 0; i < iteration; i++){
				cost = costFunction();
				MyLog.i("NeuralNetwork.begin","iter:"+i+" cost: " + cost);
			}
		}
		
		public void initTestData(){
			//初始化测试数据，总共500个数据
			float[][] test_x = new float[500][12];
			float[][] test_y = new float[500][5];
			float d = 3f;
			//100个label为1的数据，向量在[1,1,1,1,1,1,1,1,1,1,1,1]附近
			for(int i = 0; i < 100; i++){
				for(int j = 0; j < 12; j++){
					test_x[i][j] = 1 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 1;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为2的数据，向量在[10,10,10,10,10,10,10,10,10,10,10,10]附近
			for(int i = 100; i < 200; i++){
				for(int j = 0; j < 12; j++){
					test_x[i][j] = 10 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 1;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为3的数据，向量在[100,100,100,100,100,100,100,100,100,100,100,100]附近
			for(int i = 200; i < 300; i++){
				for(int j = 0; j < 12; j++){
					test_x[i][j] = 100 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 1;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为4的数据，向量在[1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000]附近
			for(int i = 300; i < 400; i++){
				for(int j = 0; j < 12; j++){
					test_x[i][j] = 1000 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 1;
				test_y[i][4] = 0;
			}
			//100个label为5的数据，向量在[10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000]附近
			for(int i = 400; i < 500; i++){
				for(int j = 0; j < 12; j++){
					test_x[i][j] = 10000 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 1;
			}
			input_x = test_x;
			input_y = test_y;
		}
		
		public float costFunction(){
			float cost = 0f;
			int m = input_x.length;
			float[][] deltaW1 = new float[layer1_num][layer2_num];
			float[][] deltaW2 = new float[layer2_num][layer3_num];
			float[] deltab1 = new float[layer2_num];
			float[]	deltab2 = new float[layer3_num];
			for(int i = 0; i < m; i++){
				forward(input_x[i]);
				backward(input_x[i], input_y[i]);
				deltaW1 = metricPlus(deltaW1, gradW1);
				deltaW2 = metricPlus(deltaW2, gradW2);
				deltab1 = vectorPlus(deltab1, gradb1);
				deltab2 = vectorPlus(deltab2, gradb2);
				for(int j = 0; j < h.length; j++){
					cost += (Math.pow((h[j] - input_y[i][j]),2) / 2.0f);
				}
			}
			cost = cost / m;
			cost += (lambda / 2.0f) * (calculateSquareW(W1) + calculateSquareW(W2));
			W1 = updateW(W1, deltaW1, m);
			W2 = updateW(W2, deltaW2, m);
			b1 = updateB(b1, deltab1, m);
			b2 = updateB(b2, deltab2, m);
			return cost;
		}
		
		public float[][] updateW(float[][] A, float[][] B, int m){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] - alpha * ((1f / m) * B[i][j] + lambda * A[i][j]);
				}
			}
			return C;
		}
		
		public float[] updateB(float[] A, float[] B, int m){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] - alpha * ((1f / m) * B[i]);
			}
			return C;
		}
		
		public float[][] metricPlus(float[][] A, float[][] B){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] + B[i][j];
				}
			}
			return C;
		}
		
		public float[][] metricMinus(float[][] A, float[][] B){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] - B[i][j];
				}
			}
			return C;
		}
		
		public float[] vectorPlus(float[] A, float[] B){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] + B[i];
			}
			return C;
		}
		
		public float[] vectorMinus(float[] A, float[] B){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] - B[i];
			}
			return C;
		}
		
		public float calculateSquareW(float[][] W){
			float tmp = 0f;
			for(int i = 0; i < W.length; i++){
				for(int j = 0; j < W[0].length; j++){
					tmp += W[i][j] * W[i][j];
				}
			}
			return tmp;
		}
		
		public void forward(float[] x){
			z2 = calculateZ(W1, x, b1);
			a2 = calculateA(z2);
			z3 = calculateZ(W2, a2, b2);
			a3 = calculateA(z3);
			h = a3;
		}
		
		public void backward(float[] x, float[] y){
			float[] sgz3 = sigmodGradient(z3);
			float[] sgz2 = sigmodGradient(z2);
			for(int i = 0; i < delta3.length; i++){
				delta3[i] = -(y[i] - a3[i]) * sgz3[i];
				gradb2[i] = delta3[i];
			}
			float[] w2_times_delta3 = new float[delta2.length];
			for(int i = 0; i < delta2.length; i++){
				for(int j = 0; j < delta3.length; j++){
					w2_times_delta3[i] += W2[i][j];
				}
			}
			for(int i = 0; i < delta2.length; i++){
				delta2[i] = w2_times_delta3[i] * sgz2[i];
				gradb1[i] = delta2[i];
			}
			for(int i = 0; i < layer2_num; i++){
				for(int j = 0; j < layer3_num; j++){
					gradW2[i][j] = delta3[j] * a2[i];
				}
			}
			for(int i = 0; i < layer1_num; i++){
				for(int j = 0; j < layer2_num; j++){
					gradW1[i][j] = delta2[j] * x[i];
				}
			}
		}
		
		public float[] calculateZ(float[][] W, float[] x, float[] b){
			float[] Z = new float[b.length];
			for(int i = 0; i < x.length; i++){
				for(int j = 0; j < b.length; j++){
					Z[j] = W[i][j] * x[i] + b[j];
				}
			}
			return Z;
		}
		
		public float[] calculateA(float[] Z){
			float[] A = new float[Z.length];
			A = sigmod(Z);
			return A;
		}
		
		public float[] sigmod(float[] z){
			float[] z_sigmod = new float[z.length];
			for(int i = 0; i < z.length; i++)
			{
				z_sigmod[i] = (float) (1.0 / (1.0 + Math.exp(-z[i])));
			}
			return z_sigmod;
		}
		
		public float[] sigmodGradient(float[] z){
			float[] s = sigmod(z);
			float[] g = new float[z.length];
			for(int i = 0; i < z.length; i++){
				g[i] = (s[i] * (1.0f - s[i]));
			}
			return g;
			
		}
	}

	public class NeuralNetworkML{
		public  int layer1_num = 12;
		public  int layer2_num = 10;
		public  int layer3_num = 5;
		public  float lambda = 1f;
		public float alpha = 0.1f;
		
		public float[][] theta1, theta1_grad; // 13 * 10
		public float[][] theta2, theta2_grad; // 11 * 5
		public float[][] X; // m * 13, X[i][0] = 1
		public float[][] y; // m * 5
		
		public void init(){
			theta1 = new float[layer1_num + 1][layer2_num];
			theta1_grad = new float[layer1_num + 1][layer2_num];
			theta2 = new float[layer2_num + 1][layer3_num];
			theta2_grad = new float[layer2_num + 1][layer3_num];
			for(int i = 0; i < layer1_num + 1; i++){
				for(int j = 0; j < layer2_num ; j++){
					theta1[i][j] = (float) ((Math.random() - 0.5) * 0.1);
				}
			}
			for(int i = 0; i < layer2_num + 1; i++){
				for(int j = 0; j < layer3_num ; j++){
					theta2[i][j] = (float) ((Math.random() - 0.5) * 0.1);
				}
			}
//			theta1[0][0] = 0.0954f;
//			theta1[0][1] = 0.0351f;
//			theta1[0][2] = 0.093f;
//			theta1[0][3] = 0.0975f;
//			theta1[0][4] = 0.0933f;
//			theta1[0][5] = 0.0726f;
//			theta1[0][6] = 0.0552f;
//			theta1[0][7] = 0.0189f;
//			theta1[0][8] = 0.0097f;
//			theta1[0][9] = 0.0898f;
//			theta1[1][0] = 0.0108f;
//			theta1[1][1] = 0.0252f;
//			theta1[1][2] = 0.1601f;
//			theta1[1][3] = -0.2761f;
//			theta1[1][4] = -0.3198f;
//			theta1[1][5] = -0.1531f;
//			theta1[1][6] = -0.0127f;
//			theta1[1][7] = 0.0645f;
//			theta1[1][8] = 0.0089f;
//			theta1[1][9] = 0.0082f;
//			theta1[2][0] = 0.0454f;
//			theta1[2][1] = 0.0597f;
//			theta1[2][2] = 0.146f;
//			theta1[2][3] = 0.0813f;
//			theta1[2][4] = 0.1033f;
//			theta1[2][5] = -0.0575f;
//			theta1[2][6] = 0.053f;
//			theta1[2][7] = 0.2639f;
//			theta1[2][8] = 0.0377f;
//			theta1[2][9] = 0.0394f;
//			theta1[3][0] = 0.0791f;
//			theta1[3][1] = 0.1014f;
//			theta1[3][2] = -0.3012f;
//			theta1[3][3] = 0.1685f;
//			theta1[3][4] = 0.1885f;
//			theta1[3][5] = -0.1151f;
//			theta1[3][6] = 0.0839f;
//			theta1[3][7] = -0.1746f;
//			theta1[3][8] = 0.0601f;
//			theta1[3][9] = 0.0756f;
//			theta1[4][0] = 0.0003f;
//			theta1[4][1] = 0.0024f;
//			theta1[4][2] = 0.127f;
//			theta1[4][3] = 0.0076f;
//			theta1[4][4] = 0.0182f;
//			theta1[4][5] = -0.0042f;
//			theta1[4][6] = -0.0007f;
//			theta1[4][7] = -0.0621f;
//			theta1[4][8] = 0.0011f;
//			theta1[4][9] = 0.0015f;
//			theta1[5][0] = -0.0034f;
//			theta1[5][1] = 0.0022f;
//			theta1[5][2] = 0.4202f;
//			theta1[5][3] = 0.0283f;
//			theta1[5][4] = 0.0656f;
//			theta1[5][5] = -0.0029f;
//			theta1[5][6] = -0.0064f;
//			theta1[5][7] = -0.2201f;
//			theta1[5][8] = 0.0005f;
//			theta1[5][9] = 0.0013f;
//			theta1[6][0] = 0.0011f;
//			theta1[6][1] = 0.0025f;
//			theta1[6][2] = 0.0529f;
//			theta1[6][3] = 0.0004f;
//			theta1[6][4] = 0.004f;
//			theta1[6][5] = -0.0051f;
//			theta1[6][6] = 0.0006f;
//			theta1[6][7] = -0.0205f;
//			theta1[6][8] = 0.0013f;
//			theta1[6][9] = 0.0015f;
//			theta1[7][0] = 0.0f;
//			theta1[7][1] = 0.0f;
//			theta1[7][2] = 0.0001f;
//			theta1[7][3] = -0.0f;
//			theta1[7][4] = 0.0f;
//			theta1[7][5] = 0.0f;
//			theta1[7][6] = 0.0f;
//			theta1[7][7] = 0.0f;
//			theta1[7][8] = 0.0f;
//			theta1[7][9] = 0.0f;
//			theta1[8][0] = 0.0f;
//			theta1[8][1] = 0.0001f;
//			theta1[8][2] = 0.0f;
//			theta1[8][3] = 0.0001f;
//			theta1[8][4] = 0.0f;
//			theta1[8][5] = -0.0f;
//			theta1[8][6] = 0.0f;
//			theta1[8][7] = 0.0f;
//			theta1[8][8] = 0.0f;
//			theta1[8][9] = 0.0f;
//			theta1[9][0] = 0.0002f;
//			theta1[9][1] = 0.0003f;
//			theta1[9][2] = 0.0018f;
//			theta1[9][3] = -0.0014f;
//			theta1[9][4] = -0.0016f;
//			theta1[9][5] = -0.001f;
//			theta1[9][6] = 0.0001f;
//			theta1[9][7] = 0.0024f;
//			theta1[9][8] = 0.0002f;
//			theta1[9][9] = 0.0002f;
//			theta1[10][0] = 0.0001f;
//			theta1[10][1] = -0.0f;
//			theta1[10][2] = -0.0047f;
//			theta1[10][3] = -0.0003f;
//			theta1[10][4] = -0.0006f;
//			theta1[10][5] = 0.0f;
//			theta1[10][6] = 0.0001f;
//			theta1[10][7] = 0.0022f;
//			theta1[10][8] = 0.0f;
//			theta1[10][9] = -0.0f;
//			theta1[11][0] = -0.0f;
//			theta1[11][1] = 0.0f;
//			theta1[11][2] = 0.0078f;
//			theta1[11][3] = 0.0003f;
//			theta1[11][4] = 0.0008f;
//			theta1[11][5] = 0.0f;
//			theta1[11][6] = -0.0001f;
//			theta1[11][7] = -0.0031f;
//			theta1[11][8] = 0.0f;
//			theta1[11][9] = 0.0f;
//			theta1[12][0] = 0.0f;
//			theta1[12][1] = 0.0f;
//			theta1[12][2] = -0.0032f;
//			theta1[12][3] = -0.0002f;
//			theta1[12][4] = -0.0004f;
//			theta1[12][5] = -0.0f;
//			theta1[12][6] = 0.0001f;
//			theta1[12][7] = 0.001f;
//			theta1[12][8] = 0.0f;
//			theta1[12][9] = 0.0f;
//			theta2[0][0] = -2.0725f;
//			theta2[0][1] = -0.1062f;
//			theta2[0][2] = -1.9982f;
//			theta2[0][3] = -1.4264f;
//			theta2[0][4] = -2.2268f;
//			theta2[1][0] = -0.2639f;
//			theta2[1][1] = 0.3604f;
//			theta2[1][2] = -0.1298f;
//			theta2[1][3] = -0.2874f;
//			theta2[1][4] = -0.909f;
//			theta2[2][0] = -0.2612f;
//			theta2[2][1] = 0.3545f;
//			theta2[2][2] = -0.1272f;
//			theta2[2][3] = -0.2877f;
//			theta2[2][4] = -0.9206f;
//			theta2[3][0] = -3.354f;
//			theta2[3][1] = -4.1146f;
//			theta2[3][2] = -3.3978f;
//			theta2[3][3] = 4.5855f;
//			theta2[3][4] = 4.5818f;
//			theta2[4][0] = 0.8925f;
//			theta2[4][1] = -1.5736f;
//			theta2[4][2] = 0.9945f;
//			theta2[4][3] = -1.6157f;
//			theta2[4][4] = 1.1666f;
//			theta2[5][0] = 1.9023f;
//			theta2[5][1] = -3.5677f;
//			theta2[5][2] = 1.9743f;
//			theta2[5][3] = -3.0913f;
//			theta2[5][4] = 2.8841f;
//			theta2[6][0] = 0.1668f;
//			theta2[6][1] = -0.1897f;
//			theta2[6][2] = 0.1666f;
//			theta2[6][3] = -0.1909f;
//			theta2[6][4] = 0.1223f;
//			theta2[7][0] = -0.2704f;
//			theta2[7][1] = 0.3578f;
//			theta2[7][2] = -0.1478f;
//			theta2[7][3] = -0.2502f;
//			theta2[7][4] = -0.8541f;
//			theta2[8][0] = 1.0774f;
//			theta2[8][1] = 0.0013f;
//			theta2[8][2] = -1.0583f;
//			theta2[8][3] = 0.3825f;
//			theta2[8][4] = -0.8021f;
//			theta2[9][0] = -0.2602f;
//			theta2[9][1] = 0.3511f;
//			theta2[9][2] = -0.1304f;
//			theta2[9][3] = -0.2857f;
//			theta2[9][4] = -0.9158f;
//			theta2[10][0] = -0.2665f;
//			theta2[10][1] = 0.3623f;
//			theta2[10][2] = -0.133f;
//			theta2[10][3] = -0.2821f;
//			theta2[10][4] = -0.9073f;
			theta1[0][0] = 0.0954f;
			theta1[0][1] = 0.0351f;
			theta1[0][2] = 0.093f;
			theta1[0][3] = 0.0975f;
			theta1[0][4] = 0.0933f;
			theta1[0][5] = 0.0726f;
			theta1[0][6] = 0.0552f;
			theta1[0][7] = 0.0189f;
			theta1[0][8] = 0.0097f;
			theta1[0][9] = 0.0898f;
			theta1[1][0] = 0.0108f;
			theta1[1][1] = 0.0252f;
			theta1[1][2] = 0.1605f;
			theta1[1][3] = -0.2775f;
			theta1[1][4] = -0.3232f;
			theta1[1][5] = -0.153f;
			theta1[1][6] = -0.0127f;
			theta1[1][7] = 0.0626f;
			theta1[1][8] = 0.0089f;
			theta1[1][9] = 0.0082f;
			theta1[2][0] = 0.0454f;
			theta1[2][1] = 0.0597f;
			theta1[2][2] = 0.148f;
			theta1[2][3] = 0.0814f;
			theta1[2][4] = 0.1021f;
			theta1[2][5] = -0.0575f;
			theta1[2][6] = 0.053f;
			theta1[2][7] = 0.269f;
			theta1[2][8] = 0.0377f;
			theta1[2][9] = 0.0394f;
			theta1[3][0] = 0.0791f;
			theta1[3][1] = 0.1013f;
			theta1[3][2] = -0.3005f;
			theta1[3][3] = 0.1687f;
			theta1[3][4] = 0.1863f;
			theta1[3][5] = -0.115f;
			theta1[3][6] = 0.0839f;
			theta1[3][7] = -0.176f;
			theta1[3][8] = 0.0601f;
			theta1[3][9] = 0.0756f;
			theta1[4][0] = 0.0003f;
			theta1[4][1] = 0.0024f;
			theta1[4][2] = 0.1275f;
			theta1[4][3] = 0.0073f;
			theta1[4][4] = 0.0157f;
			theta1[4][5] = -0.0042f;
			theta1[4][6] = -0.0007f;
			theta1[4][7] = -0.0587f;
			theta1[4][8] = 0.0011f;
			theta1[4][9] = 0.0015f;
			theta1[5][0] = -0.0034f;
			theta1[5][1] = 0.0022f;
			theta1[5][2] = 0.4221f;
			theta1[5][3] = 0.0281f;
			theta1[5][4] = 0.0636f;
			theta1[5][5] = -0.0029f;
			theta1[5][6] = -0.0064f;
			theta1[5][7] = -0.2208f;
			theta1[5][8] = 0.0005f;
			theta1[5][9] = 0.0013f;
			theta1[6][0] = 0.0011f;
			theta1[6][1] = 0.0025f;
			theta1[6][2] = 0.0527f;
			theta1[6][3] = 0.0003f;
			theta1[6][4] = 0.0034f;
			theta1[6][5] = -0.0051f;
			theta1[6][6] = 0.0006f;
			theta1[6][7] = -0.0124f;
			theta1[6][8] = 0.0013f;
			theta1[6][9] = 0.0015f;
			theta1[7][0] = -0.0f;
			theta1[7][1] = -0.0f;
			theta1[7][2] = 0.0001f;
			theta1[7][3] = -0.0f;
			theta1[7][4] = -0.0f;
			theta1[7][5] = 0.0f;
			theta1[7][6] = 0.0f;
			theta1[7][7] = -0.0f;
			theta1[7][8] = 0.0f;
			theta1[7][9] = -0.0f;
			theta1[8][0] = -0.0f;
			theta1[8][1] = 0.0001f;
			theta1[8][2] = 0.0f;
			theta1[8][3] = 0.0001f;
			theta1[8][4] = -0.0f;
			theta1[8][5] = 0.0f;
			theta1[8][6] = -0.0f;
			theta1[8][7] = 0.0f;
			theta1[8][8] = -0.0f;
			theta1[8][9] = -0.0f;
			theta1[9][0] = 0.0002f;
			theta1[9][1] = 0.0003f;
			theta1[9][2] = 0.0018f;
			theta1[9][3] = -0.0014f;
			theta1[9][4] = -0.0016f;
			theta1[9][5] = -0.001f;
			theta1[9][6] = 0.0001f;
			theta1[9][7] = 0.0024f;
			theta1[9][8] = 0.0002f;
			theta1[9][9] = 0.0002f;
			theta1[10][0] = 0.0001f;
			theta1[10][1] = 0.0f;
			theta1[10][2] = -0.0047f;
			theta1[10][3] = -0.0003f;
			theta1[10][4] = -0.0006f;
			theta1[10][5] = -0.0f;
			theta1[10][6] = 0.0001f;
			theta1[10][7] = 0.0021f;
			theta1[10][8] = 0.0f;
			theta1[10][9] = 0.0f;
			theta1[11][0] = -0.0f;
			theta1[11][1] = -0.0f;
			theta1[11][2] = 0.0079f;
			theta1[11][3] = 0.0003f;
			theta1[11][4] = 0.0008f;
			theta1[11][5] = 0.0f;
			theta1[11][6] = -0.0001f;
			theta1[11][7] = -0.0034f;
			theta1[11][8] = -0.0f;
			theta1[11][9] = -0.0f;
			theta1[12][0] = 0.0f;
			theta1[12][1] = 0.0f;
			theta1[12][2] = -0.0032f;
			theta1[12][3] = -0.0002f;
			theta1[12][4] = -0.0004f;
			theta1[12][5] = -0.0f;
			theta1[12][6] = 0.0001f;
			theta1[12][7] = 0.0012f;
			theta1[12][8] = 0.0f;
			theta1[12][9] = 0.0f;
			theta2[0][0] = -2.0792f;
			theta2[0][1] = -0.1015f;
			theta2[0][2] = -1.9964f;
			theta2[0][3] = -1.4289f;
			theta2[0][4] = -2.2321f;
			theta2[1][0] = -0.2704f;
			theta2[1][1] = 0.3649f;
			theta2[1][2] = -0.1279f;
			theta2[1][3] = -0.2897f;
			theta2[1][4] = -0.9139f;
			theta2[2][0] = -0.2677f;
			theta2[2][1] = 0.359f;
			theta2[2][2] = -0.1253f;
			theta2[2][3] = -0.29f;
			theta2[2][4] = -0.9254f;
			theta2[3][0] = -3.3552f;
			theta2[3][1] = -4.1323f;
			theta2[3][2] = -3.4077f;
			theta2[3][3] = 4.596f;
			theta2[3][4] = 4.5982f;
			theta2[4][0] = 0.8958f;
			theta2[4][1] = -1.5885f;
			theta2[4][2] = 1.0108f;
			theta2[4][3] = -1.6282f;
			theta2[4][4] = 1.1737f;
			theta2[5][0] = 1.905f;
			theta2[5][1] = -3.5815f;
			theta2[5][2] = 1.99f;
			theta2[5][3] = -3.103f;
			theta2[5][4] = 2.8903f;
			theta2[6][0] = 0.1667f;
			theta2[6][1] = -0.1896f;
			theta2[6][2] = 0.1665f;
			theta2[6][3] = -0.1908f;
			theta2[6][4] = 0.1222f;
			theta2[7][0] = -0.2769f;
			theta2[7][1] = 0.3623f;
			theta2[7][2] = -0.1459f;
			theta2[7][3] = -0.2525f;
			theta2[7][4] = -0.859f;
			theta2[8][0] = 1.1717f;
			theta2[8][1] = -0.0223f;
			theta2[8][2] = -1.1408f;
			theta2[8][3] = 0.4071f;
			theta2[8][4] = -0.8218f;
			theta2[9][0] = -0.2667f;
			theta2[9][1] = 0.3556f;
			theta2[9][2] = -0.1285f;
			theta2[9][3] = -0.288f;
			theta2[9][4] = -0.9206f;
			theta2[10][0] = -0.273f;
			theta2[10][1] = 0.3668f;
			theta2[10][2] = -0.1311f;
			theta2[10][3] = -0.2844f;
			theta2[10][4] = -0.9122f;
		}
		
		public void initTestData(){
			//初始化测试数据，总共500个数据
			float[][] test_x = new float[50][13];
			float[][] test_y = new float[50][5];
			float d = 3f;
			//100个label为1的数据，向量在[1,1,1,1,1,1,1,1,1,1,1,1]附近
			for(int i = 0; i < 10; i++){
				for(int j = 1; j < 13; j++){
					test_x[i][j] = j + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 1;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为2的数据，向量在[10,10,10,10,10,10,10,10,10,10,10,10]附近
			for(int i = 10; i < 20; i++){
				for(int j = 1; j < 13; j++){
					test_x[i][j] = (13-j) + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 1;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为3的数据，向量在[100,100,100,100,100,100,100,100,100,100,100,100]附近
			for(int i = 20; i < 30; i++){
				for(int j = 1; j < 13; j++){
					test_x[i][j] = 100 + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 1;
				test_y[i][3] = 0;
				test_y[i][4] = 0;
			}
			//100个label为4的数据，向量在[1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000]附近
			for(int i = 30; i < 40; i++){
				for(int j = 1; j < 13; j++){
					test_x[i][j] = (float) (Math.pow(-1,j) * j + (float) (Math.random() - 0.5) * d);
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 1;
				test_y[i][4] = 0;
			}
			//100个label为5的数据，向量在[10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000,10000]附近
			for(int i = 40; i < 50; i++){
				for(int j = 1; j < 13; j++){
					test_x[i][j] = (float) Math.pow(2, j) + (float) (Math.random() - 0.5) * d;
				}
				test_y[i][0] = 0;
				test_y[i][1] = 0;
				test_y[i][2] = 0;
				test_y[i][3] = 0;
				test_y[i][4] = 1;
			}
			for(int i = 0; i < 50; i++){
				test_x[i][0] = 1;
			}
			X = test_x;
			y = test_y;
		}
		
		public void setDatas(float[][] input_x, float[][] input_y){
			layer1_num = input_x[0].length;
			float[][] new_x = new float[input_x.length][layer1_num + 1];
			for(int i = 0; i < input_x.length; i++){
				new_x[i][0] = 1;
				for(int j = 1; j < layer1_num + 1; j++){
					new_x[i][j] = input_x[i][j-1];
				}
			}
			X = new_x;
			y = input_y;
			File path = new File("/sdcard/dbfile/"); //数据库文件目录  
		    File file = new File("/sdcard/dbfile/feature.txt"); //数据库文件  
			if(!path.exists()){
				if(path.mkdirs()){
					MyLog.w(TAG,"mkdir succ");
				}
				else {
					MyLog.w(TAG,"mkdir fail");
				}
			}
			if(!file.exists()){
				try{
					file.createNewFile();
				}catch(IOException e){
					MyLog.w(TAG,"createNewFile error.");
					e.printStackTrace();
				}
			}
//			String filename = "/sdcard/dbfile/feature.txt";
//			String write_str = "";
//			for(int i = 0; i < X.length; i++){
//				write_str += "[";
//				for(int j = 1; j < X[0].length; j++){
//					write_str += X[i][j] + ",";
//				}
//				write_str += "];";
//			}
//			write_str += "\n[";
//			for(int i = 0; i < y.length; i++){
////				write_str += "[";
//				int index = 0;
//				for(int j = 0; j < y[0].length; j++){
//					if(y[i][j] == 1){
//						index = j + 1;
//						break;
//					}
//				}
//				write_str += index + ",";
////				write_str += "];";
//			}
//			write_str += "];";
//			writeFileSdcardFile(filename, write_str);
		}
		
		public void writeFileSdcardFile(String fileName, String write_str) {
			try {
				FileOutputStream fout = new FileOutputStream(fileName);
				byte[] bytes = write_str.getBytes();

				fout.write(bytes);
				fout.close();
			}

			catch (Exception e) {
				e.printStackTrace();
			}
		} 
		
		public void train(int iteration){
			init();
			boolean inTest = false;
			if(inTest){
				initTestData();
			}
			float cost = 0f;
			float cost_flag = 0f;
			for(int i = 0; i < iteration; i++){
				cost = nnCostFunction();
				if(HeadWear.label == 6){
					break;
				}
				if(cost < 0.97){
//					Log.i("MyDatas", "cost < 1");
					if(Math.abs(cost - cost_flag) < 0.001f){
						MyLog.i("nnCostFunction", "cost - cost_flag < 0.0001f, cost:" + cost + ",cost_flag:" + cost_flag);
						break;
					}
					break;
				}
				cost_flag = cost;
				MyLog.i("nnCostFunction", "iter:" + i + "cost: " + cost);
				if(iteration == 50)alpha = 0.1f;
//				if(iteration == 500)alpha = 0.01f;
//				if(iteration == 1500)alpha = alpha / 10;
				theta2 = metricMinus(theta2, theta2_grad, alpha);
				theta1 = metricMinus(theta1, theta1_grad, alpha);
			}
		}
		
		public int predict(float[] inputx){
			float[] a1 = new float[layer1_num + 1];
			if(inputx.length == layer1_num){
				a1[0] = 1;
				for(int i = 1; i < layer1_num + 1; i++){
					a1[i] = inputx[i-1];
				}
			}else{
				a1 = inputx;
			}
			float[] z2 = forwardVectorTimesMetric(a1, theta1); // a1 * theta1
			float[] a2_tmp = sigmod(z2); // m * 10
			float[] a2 = new float[layer2_num + 1]; //m * 11
			a2[0] = 1f;
			for(int j = 1; j < layer2_num + 1; j++){
				a2[j] = a2_tmp[j-1];
			}
			
			float[] z3 = forwardVectorTimesMetric(a2, theta2);
			float[] a3 = sigmod(z3);
			float[] h = a3;
			return max(h);
		}
		
		public int max(float[] A){
			int l = 0;
			int len = A.length;
			float temp = A[0];
			for(int i = 1; i < len; i++){
				if(A[i] > temp){
					temp = A[i];
					l = i;
				}
			}
			return l + 1;
		}

		public float nnCostFunction(){
			int m = X.length;
			float J = 0;
			float[][] a1 = X; // m * 13
			float[][] z2 = metricTimes(a1, theta1); // m * 10
			float[][] a2_tmp = sigmod(z2); // m * 10
			float[][] a2 = new float[m][layer2_num + 1]; //m * 11
			for(int i = 0; i < m; i++){
				a2[i][0] = 1f;
				for(int j = 1; j < layer2_num + 1; j++){
					a2[i][j] = a2_tmp[i][j-1];
				}
			}
			float[][] z3 = metricTimes(a2, theta2);  // m * 5
			float[][] a3 = sigmod(z3); // m * 5
			float[][] h = a3;
			float sum = sumMetric( metricPlus(metricDotTimes(y, logMetric(h)) , metricDotTimes(metricMinus(onesMetric(y),y), logMetric(metricMinus(onesMetric(h),h ))))  );
			J = (-1f / m) * sum;
			for(int i = 0; i < h.length; i++){
				for(int j = 0; j < h[0].length; j++){
					if(h[i][j] > 1f || h[i][j] < 0){
						MyLog.e("ERROR", "error! h[i][j] > 1f || h[i][j] < 0 h:" + h[i][j]);
					}
				}
			}
			float[][] tmp_theta1 = new float[layer1_num][layer2_num];
			for(int i = 0; i < layer1_num; i++){
				for(int j = 0; j < layer2_num; j++){
					tmp_theta1[i][j] = theta1[i+1][j];
				}
			}
			float[][] tmp_theta2 = new float[layer2_num][layer3_num]; //10 * 5
			for(int i = 0; i < layer2_num; i++){
				for(int j = 0; j < layer3_num; j++){
					tmp_theta2[i][j] = theta2[i+1][j];
				}
			}
			J += J + lambda / (2f * m) * (sumMetric(metricSquare(tmp_theta1)) + sumMetric(metricSquare(tmp_theta2)));
			float[][] Delta1 = new float[layer1_num + 1][layer2_num]; // 13 * 10
			float[][] Delta2 = new float[layer2_num + 1][layer3_num]; // 11 * 5
			float[] delta3 = new float[layer3_num];//5  
			float[] delta2 = new float[layer2_num];//10
			for(int i = 0; i < m; i++){
				delta3 = vectorMinus(a3[i], y[i]);
				delta2 = vectorDotTimes(vectorTimesMetric(delta3, tmp_theta2), sigmodGradient(z2[i]));
				Delta2 = metricPlus(Delta2, vectorTimesVector(a2[i], delta3));
				Delta1 = metricPlus(Delta1, vectorTimesVector(a1[i], delta2));
			}
			theta2_grad = metricDevide(Delta2, m);
			theta1_grad = metricDevide(Delta1, m);
			for(int i = 0; i < theta1_grad.length; i++){
				for(int j = 1; j < theta1_grad[0].length; j++){
					theta1_grad[i][j] = theta1_grad[i][j] + lambda * theta1[i][j] / m; 
				}
			}
			for(int i = 0; i < theta2_grad.length; i++){
				for(int j = 1; j < theta2_grad[0].length; j++){
					theta2_grad[i][j] = theta2_grad[i][j] + lambda * theta2[i][j] / m; 
				}
			}
			return J;
		}
		
		public float[][] metricMinus(float[][] A, float[][] B, float a){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] - a * B[i][j];
				}
			}
			return C;
		}
		
		public float[][] metricDevide(float[][] A, float n){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] / n;
				}
			}
			return C;
		}
		
		public float[][] vectorTimesVector(float[] A, float[] B){
			float[][] C = new float[A.length][B.length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < B.length; j++){
					C[i][j] = A[i] * B[j];
				}
			}
			return C;
		}
		
		public float[] vectorDotTimes(float[] A, float[] B){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] * B[i];
			}
			return C;
		}
		
		public float[] vectorTimesMetric(float[] A, float[][] B){
			int A_row = A.length;
			int B_row = B.length;
			int B_col = B[0].length;
			if(A_row != B_col ){
				MyLog.i("Mydatas.vectorTimesMetric", "vectorTimesMetric: A_row != B_col");
				return null;
			}
			float[] C = new float[B_row];
			for(int i = 0; i < B_row; i++){
				for(int j = 0; j < A_row; j++){
					C[i] += A[j] * B[i][j];
				}
			}
			return C;
		}
		
		public float[] forwardVectorTimesMetric(float[] A, float[][] B){
			int A_row = A.length;
			int B_row = B.length;
			int B_col = B[0].length;
			if(A_row != B_row ){
				MyLog.i("Mydatas.vectorTimesMetric", "vectorTimesMetric: A_row != B_row");
				return null;
			}
			float[] C = new float[B_col];
			for(int i = 0; i < B_col; i++){
				for(int j = 0; j < A_row; j++){
					C[i] += A[j] * B[j][i];
				}
			}
			return C;
		}
		
		public float[] vectorPlus(float[] A, float[] B){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] + B[i];
			}
			return C;
		}
		
		public float[] vectorMinus(float[] A, float[] B){
			float[] C = new float[A.length];
			for(int i = 0; i < A.length; i++){
				C[i] = A[i] - B[i];
			}
			return C;
		}
		
		public float[][] metricSquare(float[][] A){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = (float) Math.pow((A[i][j]),2);
				}
			}
			return C;
		}
		
		public float[][] logMetric(float[][] A){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = (float) Math.log(A[i][j]);
				}
			}
			return C;
		}
		
		public float sumMetric(float[][] A){
			float sum = 0f;
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					sum += A[i][j];
				}
			}
			return sum;
		}
		
		public float[][] onesMetric(float[][] A){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = 1f;
				}
			}
			return C;
		}
		
		public float[][] metricPlus(float[][] A, float[][] B){
			//MyLog.i("metricPlus", "metricPlus " + A.length + A[0].length + B.length + B[0].length);
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] + B[i][j];
				}
			}
			return C;
		}
		
		public float[][] metricMinus(float[][] A, float[][] B){
			float[][] C = new float[A.length][A[0].length];
			for(int i = 0; i < A.length; i++){
				for(int j = 0; j < A[0].length; j++){
					C[i][j] = A[i][j] - B[i][j];
				}
			}
			return C;
		}
		
		public float[][] metricTimes(float[][] A, float[][] B){
			int A_row = A.length;
			int A_col = A[0].length;
			int B_row = B.length;
			int B_col = B[0].length;
			if(A_col != B_row){
				MyLog.i("MyDatas.metricTimes", "Error. A_col != B.row");
				return null;
			}
			float[][] C = new float[A_row][B_col];
			for(int i = 0; i < A_row; i++){
				for(int j = 0; j < B_col; j++){
					for(int k = 0; k < A_col; k++){
						C[i][j] += A[i][k] * B[k][j];
					}
				}
			}
			return C;
		}
		
		public float[][] metricDotTimes(float[][] A, float[][] B){
			int A_row = A.length;
			int A_col = A[0].length;
			int B_row = B.length;
			int B_col = B[0].length;
			if(A_row != B_row || A_col != B_col){
				MyLog.i("MyDatas.metricTimes", "Error. A_row != B_row || A_col != B_col");
				return null;
			}
			float[][] C = new float[A_row][B_col];
			for(int i = 0; i < A_row; i++){
				for(int j = 0; j < A_col; j++){
					C[i][j] = A[i][j] * B[i][j];
				}
			}
			return C;
		}
		
		public float[][] sigmod(float[][] z){
			float[][] z_sigmod = new float[z.length][z[0].length];
			for(int i = 0; i < z.length; i++){
				for(int j = 0; j < z[0].length; j++){
					z_sigmod[i][j] = (float) (1.0 / (1.0 + Math.exp(-z[i][j])));
				}
			}
			return z_sigmod;
		}
		
		public float[] sigmod(float[] z){
			float[] z_sigmod = new float[z.length];
			for(int i = 0; i < z.length; i++)
			{
				z_sigmod[i] = (float) (1.0 / (1.0 + Math.exp(-z[i])));
			}
			return z_sigmod;
		}
		
		public float[] sigmodGradient(float[] z){
			float[] s = sigmod(z);
			float[] g = new float[z.length];
			for(int i = 0; i < z.length; i++){
				g[i] = (s[i] * (1.0f - s[i]));
			}
			return g;
			
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
	
	}

	public svm_problem returnSvmProblem(double[] label, float[][] datas){
		
		svm_node[][] mSvmDatas = new svm_node[datas.length][datas[0].length];

		svm_problem problem = new svm_problem();
		int i = 0, j = 0;
		for(float[] datas_2 : datas){
			j = 0;
			for(float datas_3 : datas_2){
				//MyLog.e("returnSvmProblem","i:" + i + " j:" + j + " index:" + mSvmDatas[i][j].index);
				mSvmDatas[i][j] = new svm_node();
				mSvmDatas[i][j].index = j;
				mSvmDatas[i][j].value = datas_3;
				j++;
			}
			i++;
		}
		problem.l = datas.length;
		problem.y = label;
		problem.x = mSvmDatas;
		return problem;
	}
	
	public svm_node[] returnSvmPredictData(float[] datas){
		svm_node[] mPredictData = new svm_node[datas.length];
		int i = 0;
		for(float data : datas){
			mPredictData[i] = new svm_node();
			mPredictData[i].index = i;
			mPredictData[i].value = data;
			i++;
		}
		return mPredictData;
	}
}