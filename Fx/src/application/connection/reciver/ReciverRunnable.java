package application.connection.reciver;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import application.Main;
import application.connection.ConnectionManager;
import application.connection.msg.FullMsg;
import javafx.application.Platform;
import javafx.collections.FXCollections;

//futtathato osztalyimplementacio a fogado thread szamara
public class ReciverRunnable implements Runnable{
	private static final int BUF_LEN = 1024;//puffer hossz
	
	public boolean mHasToWork = false;//has to work?
	public InputStreamReader mStream = null;//fogado stram
	public WriteOutInterface mWrite;//feldolgozasi osztaly
	private String mOldMsg = "";//ha maradt meg feluzenet
	
	//fo futtatasi pont
	@Override
	public void run() {
		mHasToWork = true;
		BufferedReader br = new BufferedReader(mStream);//ez az olvaso (c read(...))
		while(mHasToWork){//amig van dolga addig olvas be
			char[] buffer = new char[BUF_LEN];
			int count = 0;
			try {
				count = br.read(buffer, 0, BUF_LEN);//beolvassa az uzenetet
			} catch (IOException e) {
				break;
			}
			String reply = new String(buffer, 0, count);//elkesziti a hozzarendelt stringet
			
			String[] replays = getReplays(reply);//feldarabolja az uzeneteket, tcp-vel egybe megerkezhet tobb uzenet
			
			for(String r: replays){
				if(!preRender(r)){//ellenorzi a es kezeli a specialis muveleteket, ami marad azt kiirja a kepernyore
					sendMsgIfFull(r);
				}
			}
		}//kapcsolat zarasa
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//darabolasi metodus
	private String[] getReplays(String reply) {
		ArrayList<String> strs = new ArrayList<>();
		String[] st = reply.split("" + ConnectionManager.BREAK_CHAR_START);//feldarabolja az uzeneteket a kezdokarakter szintjen
		if(!mOldMsg.equals("")){//ha van megmaradt uzenet akkor az eleibe tuzi a most erkezettnek es ugy dolgozza fel, majd uresre allitja a regi uzeneteket jelzo mezot
			strs.add(mOldMsg + st[0].substring(0, st[0].length()-1));
			for(int i = 1; i < st.length - 1; ++i){
				strs.add(st[i].substring(0, st[i].length()-1));
			}
			mOldMsg = "";
		}else{//maskulomben csak darabol
			for(int i = 1; i < st.length - 1; ++i){
				strs.add(st[i].substring(0, st[i].length()-1));
			}
		}
		if(st[st.length-1].contains(""+ConnectionManager.BREAK_CHAR_STOP)){//ha az utolso darabban nincs zaro karakter akkor berakja a megmaradt uzenetekhez maskulomben berakja a kesz uzenetek koze
			strs.add(st[st.length-1].substring(0, st[st.length-1].length()-1));
		}else{
			mOldMsg = st[st.length-1];
		}
		
		return strs.toArray(new String[strs.size()]);//atalakitja sima tombe es ugy adja tovabb
	}
	//kulonleges uzenetek ellenorzese es kezelese
	private boolean preRender(String reply) {
		String[] strs = reply.split("" + ConnectionManager.BREAK_CHAR);//darabolja az uzenetet
		if(strs.length == 5 && strs[1].equals(ConnectionManager.GLOBAL_CHANEL) && strs[2].equals(ConnectionManager.TYPE_JOIN_ROOM)){//ha szobacsatlakozas
			String[] strs1 = strs[4].split(":");
			if(strs1[0].equals("true")){
				Main.uzenetek = new ArrayList<>();
				ConnectionManager.cm.setChanel(strs1[1]);
				return true;
			}
		}else if(strs.length == 5 && strs[1].equals(ConnectionManager.GLOBAL_CHANEL) && strs[2].equals(ConnectionManager.TYPE_REQ_ROOMS)){//ha szobakeres
			String[] strs1 = strs[4].split(":");
			Main.rooms = new ArrayList<>();
			for(String s: strs1){
				if(!Main.rooms.contains(s) && !s.contains(""+ConnectionManager.PRIVATE_CHANEL)){
					Main.rooms.add(s);
				}
			}
			if(!Main.rooms.contains(ConnectionManager.GLOBAL_CHANEL)){
				Main.rooms.add(ConnectionManager.GLOBAL_CHANEL);
			}
			Main.oRooms = FXCollections.observableArrayList(Main.rooms);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Main.listRooms.setItems(Main.oRooms);
				}
			});
			return true;
		}else if(strs.length == 5 && strs[1].equals(ConnectionManager.GLOBAL_CHANEL) && strs[2].equals(ConnectionManager.TYPE_REQ_CLIENTS_FROM_ROOM)){//ha ha kliensek lekerese a szobabol
			String[] strs1 = strs[4].split(":");
			Main.clients = new ArrayList<>();
			for(String s: strs1){
				if(!Main.clients.contains(s)){
					Main.clients.add(s);
				}
			}
			Main.oClients = FXCollections.observableArrayList(Main.clients);
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Main.listClients.setItems(Main.oClients);
				}
			});
			return true;
		}else if(strs.length == 5 && strs[1].equals(ConnectionManager.GLOBAL_CHANEL) && strs[2].equals(ConnectionManager.TYPE_REQ_CHAT_WITH_CLIENT)){//ha privat cseteles kezdemenyezes
			Platform.runLater(new Runnable() {	
				@Override
				public void run() {//kerdo ablak felmutatasa
					Main.showRequestDialog("Private!", "Do you want to massage with " + strs[4] + "?", 
							new Runnable() {//pozitiv valasz eseten
								@Override
								public void run() {
									ConnectionManager.cm.send(ConnectionManager.GLOBAL_CHANEL, ConnectionManager.TYPE_CONF_CHAT_WITH_CLIENT, strs[4], false);
								}
							}, 
							new Runnable() {//negativ valasz eseten
								@Override
								public void run() {
									/*Kozoljuk a rossz hirt vagy nem :)*/
								}
							});	
				}
			});
			return true;
		}else if(strs.length == 5 && strs[2].equals(ConnectionManager.TYPE_FILE_MSG)){//ha fajlkuldes
			String[] num = strs[3].split("/");
			if(num[0].equals("0")){//ha elso uzenet(fajlnev)
				try {
					FileOutputStream out = new FileOutputStream(strs[4]);
					out.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					FullMsg.addNew(reply,new FileOutputStream(strs[4], true));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}else if(num[0].equals(num[1])){//vege, osszerakas
				FullMsg.addMsg(reply);
				byte[] bytes = Main.decode(FullMsg.build(reply));
				FileOutputStream file = FullMsg.getStream(reply);
				try {
					file.write(bytes);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						file.close();
					} catch (IOException e) {
						e.printStackTrace();
						return true;
					}
					Platform.runLater(new Runnable() {
						@Override
						public void run() {//kikuldjuk a linket(nincs megoldva a klikkelheto link)
							mWrite.write("LinkToLocation:...");
						}
					});
				}
				
			}else{//koztes informaciogyujtes
				FullMsg.addMsg(reply);
			}
			return true;
		}
		//System.out.println("NoPre!");//ha nem dolgozta fel akkor tovabblep a megjelenitesi fazisba
		return false;
	}
	//szoveges uzenet osszegyujtese es kuldese
	private void sendMsgIfFull(String reply) {
		String[] strs = reply.split(""+ConnectionManager.BREAK_CHAR);//uzenet darabolasa
		if(strs.length == 5){//ha 5 elemu, csak ez maradhat
			String[] num = strs[3].split("/");
			if(num[0].equals("1")){//elso uzenet
				//System.out.println("new");
				FullMsg.addNew(reply);
			}else{//koztes uzenetek
				//System.out.println("Append:"+reply);
				FullMsg.addMsg(reply);
			}
			if(num[0].equals(num[1])){//utolso uzenet
				FullMsg.addMsg(reply);
				Platform.runLater(new Runnable() {//kikuldes
					@Override
					public void run() {
						String m = FullMsg.build(reply);
						mWrite.write(m);
					}
				});
			}
			return;
		}
		System.out.println("Fatal Error:"+reply);
		Platform.runLater(new Runnable() {//ha valami tovabbjott kikuldi ellenorzes celjabol, a regisztracional es loginnal van mas szerepe is(erkezhet csupasz true vagy false valasz a szervertol)
			@Override
			public void run() {
				if(reply.length() < 30){
					mWrite.write(reply);
				}
			}
		});
	}

}
