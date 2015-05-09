package com.example.headwearing;

import java.util.ArrayList;

import android.util.Log;
import libsvm.svm_problem;
import libsvm.svm_parameter;
import libsvm.svm_node;

class MyDatas{
	public static int LEN_OF_SIGNAL_DATA = 100;
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
			MyLog.w(TAG,"resetDatas");
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
			nVariance();
			standardDeviation();
			kurtosis();
			correlation();
			using = false;
			MyLog.w(TAG,"calculate over.");
			if(HeadWear.DEBUG){
				MyLog.w("ViewFeature" + "meanValue:", "" + mean_x_value);
				MyLog.w("ViewFeature" + "meanValue:", "" + mean_y_value);
				MyLog.w("ViewFeature" + "meanValue:", "" + mean_z_value);
				MyLog.w("ViewFeature" + "n_variance:Value", "" + n_variance_x_value);
				MyLog.w("ViewFeature" + "n_varianceValue:", "" + n_variance_y_value);
				MyLog.w("ViewFeature" + "n_varianceValue:", "" + n_variance_z_value);
				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_x_value);
				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_y_value);
				MyLog.w("ViewFeature" + "standard_deviationValue:", "" + standard_deviation_z_value);
				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_x_value);
				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_y_value);
				MyLog.w("ViewFeature" + "kurtosisValue:", "" + kurtosis_z_value);
				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_x_y_value);
				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_y_z_value);
				MyLog.w("ViewFeature" + "correlationValue:", "" + correlation_z_x_value);
			}
		}
		
		public boolean correlation(){
			if(HeadWear.DEBUG){
				MyLog.w(TAG,"method: correlation");
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
			MyLog.w(TAG,"sum");
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
			MyLog.w(TAG,"meanValue");
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
			MyLog.w(TAG,"nVariance");
			for(int i = 0; i < LEN_OF_SIGNAL_DATA; i++){
				n_variance_x_value += Math.pow((data_x.get(i) - mean_x_value),2);
				n_variance_y_value += Math.pow((data_y.get(i) - mean_y_value),2);
				n_variance_z_value += Math.pow((data_z.get(i) - mean_z_value),2);
			}
			return true;
		}
	
		public float[] feature2list(){
			float[] f = new float[12];
			f[0] = standard_deviation_x_value;
			f[1] = standard_deviation_y_value;
			f[2] = standard_deviation_z_value;
			f[3] = skewness_x_value;
			f[4] = skewness_y_value;
			f[5] = skewness_z_value;
			f[6] = kurtosis_x_value;
			f[7] = kurtosis_y_value;
			f[8] = kurtosis_z_value;
			f[9] = correlation_x_y_value;
			f[10] = correlation_y_z_value;
			f[11] = correlation_z_x_value;
			String s = "";
			for(int i = 0; i < 12; i++){
				s += "f[" + i + "]:" + f[i] + " &";
			}
			MyLog.i("MyDatas.feature2list",s);
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
				MyLog.i("NeuralNetwork.begin","cost: " + cost);
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
		public static final int layer1_num = 12;
		public static final int layer2_num = 10;
		public static final int layer3_num = 5;
		public static final float lambda = 1f;
		public float alpha = 0.25f;
		
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
					theta1[i][j] = (float) ((Math.random() - 0.0) * 0.1);
				}
			}
			for(int i = 0; i < layer2_num + 1; i++){
				for(int j = 0; j < layer3_num ; j++){
					theta2[i][j] = (float) ((Math.random() - 0.0) * 0.1);
				}
			}
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
			float[][] new_x = new float[input_x.length][layer1_num + 1];
			for(int i = 0; i < input_x.length; i++){
				new_x[i][0] = 1;
				for(int j = 1; j < layer1_num + 1; j++){
					new_x[i][j] = input_x[i][j-1];
				}
			}
			X = new_x;
			y = input_y;
		}
		
		public void train(int iteration){
			init();
			boolean inTest = false;
			if(inTest){
				initTestData();
			}
			float cost = 0f;
			for(int i = 0; i < iteration; i++){
				cost = nnCostFunction();
				MyLog.i("nnCostFunction", "cost: " + cost);
				if(iteration == 50)alpha = 0.1f;
//				if(iteration == 500)alpha = alpha / 10;
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