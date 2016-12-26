package application.connection.msg;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import application.connection.ConnectionManager;

//teljes uzenet osztalya(osszefonja a darabolt uzeneteket)////////////////////////STATIKUS OSZTALY
public class FullMsg {
	private static HashMap<String, FullMsg> sFullMsgs = new HashMap<>();
	
	private String mId;
	private String mChatRoom;
	private String mType;
	private int mLength;
	private ArrayList<Msg> mMsgs = new ArrayList<>();
	private boolean mIsBuildt = false;
	private FileOutputStream mFile = null;
	
	private FullMsg(String msg) {//inicializalas
		String[] strs = msg.split(""+ConnectionManager.BREAK_CHAR);
		mId = strs[0];
		mChatRoom = strs[1];
		mType = strs[2];
		String[] num = strs[3].split("/");
		mLength = Integer.valueOf(num[1]);
	}
	
	public static FullMsg addNew(String msg){//uj uzenet hozzadasa
		FullMsg f = new FullMsg(msg);
		sFullMsgs.put(f.mId, f);
		return f;
	}
	public static FullMsg addNew(String msg, FileOutputStream stream){//uj teljes uzenet
		FullMsg f = new FullMsg(msg);
		f.mFile = stream;
		sFullMsgs.put(f.mId, f);
		return f;
	}
	
	//fajloknal a fajlcim visszadasa
	public static FileOutputStream getStream(String msg){
		String[] s = msg.split(""+ConnectionManager.BREAK_CHAR);
		FullMsg f = sFullMsgs.get(s[0]);
		if(f!=null){
			return f.mFile;
		}
		return null;
	}
	
	//uzenet hozzadasa
	private void addMsg1(String msg){
		String[] strs = msg.split(""+ConnectionManager.BREAK_CHAR);
		String[] num = strs[3].split("/");
		int index = Integer.valueOf(num[0]);
		if(mId.equals(strs[0]) && mChatRoom.equals(strs[1]) && mType.equals(strs[2]) && index <= mLength && index >= 0){
			mMsgs.add(new Msg(index, strs[4]));
		}
		if(num[0].equals(num[1])){
			mIsBuildt = true;
		}
	}
	
	//elobbi statikus megvalositasa
	public static FullMsg addMsg(String msg){
		String[] s = msg.split(""+ConnectionManager.BREAK_CHAR);
		FullMsg f = sFullMsgs.get(s[0]);
		if(f!=null){
			f.addMsg1(msg);
			return f;
		}
		return null;
	}
	
	//kovetkezo uzenet megkeresese
	private String getNext(int i){
		Msg s = null;
		for(Msg m: mMsgs){
			if(m.mNum == i){
				s = m;
				break;
			}
		}
		if(s != null){
			mMsgs.remove(s);
			return s.mData;
		}
		return "";
	}
	
	//fel van-e epitve
	public boolean isBuildt(){
		return mIsBuildt;
	}
	
	//teljes uzenet felepitese
	private String build1(){
		StringBuilder build = new StringBuilder();
		for(int i = 1; i <= mLength; ++i){
			build.append(getNext(i));
		}
		return build.toString();
	}
	
	//elozo fugveny meghivasa statikusan
	public static String build(String msg){
		String[] s = msg.split(""+ConnectionManager.BREAK_CHAR);
		FullMsg f = sFullMsgs.get(s[0]);
		if(f!=null){
			return f.build1();
		}
		return null;
	}
}
