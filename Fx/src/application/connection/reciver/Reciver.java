package application.connection.reciver;

import java.io.InputStreamReader;
import java.net.Socket;

//fogado osztaly
public class Reciver {
	private ReciverRunnable mRunable;//egy uj treden fog fogadni ami ezen osztalyt hajtja vegre
	
	public Reciver(){}
	//csatlakozas, inicializal egy szerverre es beallitja a feldolgozast
	public void connect(Socket client, WriteOutInterface msg){
		try{
			mRunable = new ReciverRunnable();
			mRunable.mStream = new InputStreamReader(client.getInputStream());//a fogado stream inicializalasa a threadnek
			mRunable.mWrite = msg;
			
	        Thread t = new Thread(mRunable);//a vegrehajto thread
	        t.start();
        }catch (Exception e) {
        	e.printStackTrace();
		}
	}
	
	//csatlakozas zarolasa
	public void close(){
		mRunable.mHasToWork = false;
	}
	//uj feldolgozasa hozzakapcsolasa
	public void reConnect(WriteOutInterface msg) {
		mRunable.mWrite = msg;
	}

}
