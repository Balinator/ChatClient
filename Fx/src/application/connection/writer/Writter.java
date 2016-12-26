package application.connection.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class Writter {
	private PrintWriter mPrint;
	
	public Writter(){}
	
	//socketre csatlakozas
	public void connect(Socket client){
		try {
			mPrint = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//kuldes
	public void send(String msg){
		mPrint.print(msg);
		mPrint.flush();
	}
	
	//bezaras
	public void close(){
		mPrint.close();
	}
}
