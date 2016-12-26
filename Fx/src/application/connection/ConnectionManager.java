package application.connection;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import application.connection.reciver.Reciver;
import application.connection.reciver.WriteOutInterface;
import application.connection.writer.Writter;

public class ConnectionManager {
	//szervernel is megtalalhato konstansok
	public static final char BREAK_CHAR = (char)178;
	public static final char BREAK_CHAR_START = (char)179;
	public static final char BREAK_CHAR_STOP = (char)180;
	public static final char PRIVATE_CHANEL = (char)181;
	
	public static final String TYPE_REGISTER = "0";
	public static final String TYPE_LOGIN = "1";
	public static final String TYPE_DISCONNECT = "2";
	public static final String TYPE_MSG = "3";
	public static final String TYPE_FILE_MSG = "4";
	public static final String TYPE_REQ_ROOMS = "5";
	public static final String TYPE_REQ_CLIENTS_FROM_ROOM = "6";
	public static final String TYPE_JOIN_ROOM = "7";
	public static final String TYPE_REQ_CHAT_WITH_CLIENT = "8";
	public static final String TYPE_CONF_CHAT_WITH_CLIENT = "9";
	
	public static final String GLOBAL_CHANEL = "Global";
	
	private static int ID = 0;
	
	public static ConnectionManager cm;
	
	private Reciver mReciver;
	private Writter mWritter;
	private Socket mServerSocket;
	private String mUser = null;
	private String mChanel;
	private boolean mIsOk = false;
	
	//konstruktor, csatlakozas a sockethez es inicializalas
	public ConnectionManager(String ip, int port) {
		System.out.println("Connecting to " + ip + " on port " + port);
		try {
			mServerSocket = new Socket(ip, port);
			System.out.println("Just connected to " + mServerSocket.getRemoteSocketAddress());
			mIsOk = true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mReciver = new Reciver();
		mWritter = new Writter();
		mChanel = GLOBAL_CHANEL;
		cm = this;//kulso hivasokhoz onmaga
	}
	
	public void setUser(String user){//felhasznalo beallitasa
		if(mUser == null){
			this.mUser = user;
		}
	}
	public String getUser(){//felhasznalo lekerese
		return mUser;
	}
	
	public void setChanel(String s){//csatorna beallitasa
		mChanel = s;
	}
	public String getChanel(){//csatorna lekerese
		return mChanel;
	}
	
	//feldolgozas csatlakoztatasa
	public void connect(WriteOutInterface msg){
		mReciver.connect(mServerSocket, msg);
		mWritter.connect(mServerSocket);
	}
	//feldolgozas ujracsatlakoztatasa
	public void reConnect(WriteOutInterface msg){
		mReciver.reConnect(msg);
	}
	//id generalas
	private String generateID(){
		if(mUser == null){
			int id = ID++;
			return id + "/" + mServerSocket.getLocalAddress().toString();
		}else{
			int id = ID++;
			return id + "/" + mUser;
		}
	}
	
	//kuldes
	public void send(String chanel,String type, String data, boolean reqUserName){
		Pattern pattern = Pattern.compile("[ \\t\\r\\n\\v\\f]*");
	    Matcher matcher = pattern.matcher(data);
	    if(matcher.matches()){
	    	return;
	    }
	    data = cutExtraWhitespaces(data);//folosleges whitespacek levagasa
	    String[] msg;
	    if(chanel == null){//darabolas kulombozo parameterek eseten
	    	if(reqUserName){
	    		msg = calcData(BREAK_CHAR_START + generateID() + BREAK_CHAR + mChanel + BREAK_CHAR + type + BREAK_CHAR, mUser + ": " + data);
	    	}else{
	    		msg = calcData(BREAK_CHAR_START + generateID() + BREAK_CHAR + mChanel + BREAK_CHAR + type + BREAK_CHAR, data);
	    	}
	    }else{
	    	if(reqUserName){
	    		msg = calcData(BREAK_CHAR_START + generateID() + BREAK_CHAR + chanel + BREAK_CHAR + type + BREAK_CHAR, mUser + ": " + data);
	    	}else{
	    		msg = calcData(BREAK_CHAR_START + generateID() + BREAK_CHAR + chanel + BREAK_CHAR + type + BREAK_CHAR, data);
	    	}
	    }
		for(String m: msg){//darabok kuldese
			mWritter.send(m);
		}
	}
	
	//fajl kuldese
	public void sendFile(String data, String fileName){
		Pattern pattern = Pattern.compile("[ \\t\\r\\n\\v\\f]*");
	    Matcher matcher = pattern.matcher(data);
	    if(matcher.matches()){
	    	return;
	    }
	    data = cutExtraWhitespaces(data);
	    String pre = BREAK_CHAR_START + generateID() + BREAK_CHAR + mChanel + BREAK_CHAR + TYPE_FILE_MSG + BREAK_CHAR;//fajlnev atkuldese
	    String[] msg = calcData(pre, data);//darabolas
	    String preMsg = pre + "0/" + msg[0].split(""+BREAK_CHAR)[3].substring(2) + BREAK_CHAR + fileName + BREAK_CHAR_STOP;
	    mWritter.send(preMsg);//////////////////////////////////////////////////////////////////////////////////////////////////////////idaig
		for(String m: msg){//darabok kuldese
			System.out.println("Out:"+m);
			mWritter.send(m);
		}
	}
	//extra feherkarakterek levagasa
	private String cutExtraWhitespaces(String data) {
		String[] strs =  {" ","\\t","\\r","\\n","\\f"};
		String[] strs2 = {" ","\t", "\r", "\n", "\f" };
		for(int i = 0; i < strs.length; ++i){
			data = data.replaceAll("["+strs[i]+"]+", strs2[i]);
		}
		return data;
	}

	//darabolas
	private String[] calcData(String prev, String aft) {//a fejresz mindegyiknel ugyanaz csak a darabmutato valtozik
		int prevlen = prev.length();
		int aftlen = aft.length();
		int len = prevlen + aftlen;
		int lenner = 1024 - prevlen - 50;
		int size = (len / lenner)+1;
		String[] s = new String[size];
		for(int i = 0; i < size; ++i){
			String aftSub;
			if((i+1)*lenner > aftlen){
				aftSub = aft.substring(i * lenner, i * lenner + (aftlen - i * lenner));
			}else{
				aftSub = aft.substring(i * lenner, (i + 1) * lenner);
			}
			s[i] = prev + (i+1) + "/" + size + BREAK_CHAR + aftSub + BREAK_CHAR_STOP;
		}
		return s;
	}

	//kapcsolat zarasa
	public void close(){
		mReciver.close();
		mWritter.send(BREAK_CHAR_START + generateID() + BREAK_CHAR + GLOBAL_CHANEL + BREAK_CHAR + TYPE_DISCONNECT + BREAK_CHAR_STOP);
		mWritter.close();
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//van kapcsolat
	public boolean isOk(){
		return mIsOk;
	}

}
