package com.example.headwearing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MySocket{
	static Socket client;
	public static final int Buffer_Len = 64;
    
    public boolean connect(String site, int port){
        try{
            client = new Socket(site,port);
            MyLog.d("MySocket", "Connection");
            return true;
            //System.out.println("Client is created! site:"+site+" port:"+port);
        }catch (UnknownHostException e){
        	MyLog.d("MySocket", "Unknow");
            e.printStackTrace();
            return false;
        }catch (ConnectException e){
        	MyLog.d("MySocket", "ConnectException");
        	return false;
        }catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
    }
    
    public void sendMsg(String msg){
        try{
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(msg);
            out.flush();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public void recvMsg(){
    	new Thread(new Runnable() {                    
			@Override
			public void run() {
				while(true){
					try {
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						char[] c = new char[Buffer_Len];
						int len = in.read(c);
						if(len == -1){
							//¶Ï¿ªÁË
							return;
						}
						MyLog.d("recvMsg", "len: " + len + "msg:" + String.valueOf(c));
					} catch (IOException e) {
						MyLog.d("recvMsg", "IOException");
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
    }
    
    public void closeSocket(){
        try{
            client.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception{
        
    }
}
