package application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import application.connection.ConnectionManager;
import application.connection.reciver.WriteOutInterface;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Main extends Application {
	private static ConnectionManager mConnection;
	private boolean isLogeedIn = false;
	
	public static ArrayList<String> uzenetek;
	
	public static ArrayList<String> rooms;
	public static ListView<String> listRooms;
	public static ObservableList<String> oRooms;
	
	public static ListView<String> listClients;
	public static ArrayList<String> clients;
	public static ObservableList<String> oClients;
	
	private static  ListView<String> list;
	
	public static Stage owner;
	
	//fo programhivas
	@Override
	public void start(Stage primaryStage) {
		owner = primaryStage;
		//szerver es portja meghatarozasa
		showConnectionDialog();
		
		//ablak inicializacioja es beallitasai
		primaryStage.setTitle("Basic client!");
        primaryStage.setOnCloseRequest(e->{//kilepesi fugveny
			if(mConnection != null && mConnection.isOk()){
				mConnection.close();
			}
        });
        
        BorderPane base = new BorderPane();
        
        Scene scene = new Scene(base, 800, 600);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(800);
        
        scene.widthProperty().addListener(new ChangeListener<Number>() {//atmeretezesi figyelo
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number v) {
				if(list != null && listRooms != null && listClients != null){
					listRooms.setPrefWidth(v.intValue()*150/800);
					listClients.setPrefWidth(v.intValue()*150/800);
					list.setPrefWidth(v.intValue()*600/800);
				}
			}
        });
        
        if(mConnection.isOk()){//ha sikeres csatlakozas
        	createLoginPane(base);
        }else{//ha sikertelen csatlakozas
        	Text scenetitle = new Text("No connection with the server!");
            scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        	base.setCenter(scenetitle);
        }
	}
	
	//kapcsolodo dialogus
	public static void showConnectionDialog() {
		//dialogus inicializalasa
		final Stage dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.setTitle("Connect to?");
        dialog.initOwner(owner);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        //dialogus mezoinek inicializalasa
        Label ip = new Label("IP:");
        grid.add(ip, 0, 1);

        TextField ipBox = new TextField();//ipbox
        ipBox.setText("127.0.0.1");
        grid.add(ipBox, 1, 1);

        Label port = new Label("Port:");
        grid.add(port, 0, 2);

        TextField portBox = new TextField();//portbox
        portBox.setText("10013");
        grid.add(portBox, 1, 2);
        
        Button positive = new Button("Connect");//csatlakozas es ellenorzese
        positive.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(ipBox.getText().matches("[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+") && portBox.getText().matches("[0-9]+")){
					mConnection = new ConnectionManager(ipBox.getText(), Integer.parseInt(portBox.getText()));
					dialog.close();
				}else{
					ShowErrorDialog("Wrong format!", false);//ha hibas akkor hibadialog feldobasa
				}
			}
		});
        grid.add(positive, 0, 3);
        
        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {//bezarasi feltetel ha nem csatlakozott (bezar mindent)
			@Override
			public void handle(WindowEvent event) {
				owner.close();
			}
		});
        dialog.showAndWait();//a dialogus megmutatasa(es kozben a fo program var)
	}
	
	//belepesi panel elokeszitese
	private void createLoginPane(BorderPane base) {
		//dialogus inicializalasa
		final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        //Mezok inicializalasa
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();//felhasznalonev szovegdoboza
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        PasswordField pwBox = new PasswordField();//felhasznalonev szovegdoboza
        grid.add(pwBox, 1, 2);
        
        Button btn = new Button("Sign in");//bejelentkezesi gomb
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);
        
        Button btnReg = new Button("Register");//regisztracios gomb
        HBox hbBtnReg = new HBox(10);
        hbBtnReg.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtnReg.getChildren().add(btnReg);
        grid.add(hbBtnReg, 0, 4);
        
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        //bejelentkezeskori fogadas feldolgozasa
        WriteOutInterface write = new WriteOutInterface() {
			@Override
			public void write(String replay) {
				if(replay.equals("true")){//ha a szerver visszaigazol
					mConnection.setUser(userTextField.getText());
					isLogeedIn = true;
            		createChatPane(base);//csetszoba felepitese
            		dialog.close();
				}else{//ha nem jol igazol vissza
					actiontarget.setFill(Color.FIREBRICK);
	                actiontarget.setText("Wrong Username and/or Password!");
				}
			}
		};
        
        mConnection.connect(write);//feldolgozas hozzacsatolasa
        
        btn.setOnAction(new EventHandler<ActionEvent>() {//bejelentkezesi gomb esemenykezeloje
            @Override
            public void handle(ActionEvent e) {//uzenetet kuld a szervernek a bejelentkezesi szandekkal es adatokkal
            	mConnection.send(ConnectionManager.GLOBAL_CHANEL,ConnectionManager.TYPE_LOGIN,userTextField.getText()+":"+pwBox.getText(),false);
            }
        });
        
        btnReg.setOnAction(e->{//regisztralasi gomb esemenykezeloje
        	createRegisterPanel(write);//regisztralasi panel felepitese
        });
        
        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.setOnCloseRequest(e->{//bezaras kerese eseten bezarja a teljes programot
        	if(!isLogeedIn){
        		owner.close();
        	}
        });
        dialog.show();
	}
	//regisztracios panel
	private void createRegisterPanel(WriteOutInterface ondWrite) {
		//dialogus inicializalasa
		final Stage dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        //dialogus felepitese
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField();//felhasznalonev doboza
        grid.add(userTextField, 1, 1);

        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);

        TextField pwBox = new TextField();//jelszo doboza
        grid.add(pwBox, 1, 2);
        
        Button btn = new Button("Register in");//regisztracios gomb
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);
        
        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);
        
        mConnection.reConnect(new WriteOutInterface() {//atallitja a feldolgozas menetet
			@Override
			public void write(String replay) {
				System.out.println("MSG:"+replay);
				if(replay.equals("true")){
					isLogeedIn = true;
					mConnection.reConnect(ondWrite);//visszaallitja a menetes ha sikeres a regisztralas
            		dialog.close();
				}else{
					actiontarget.setFill(Color.FIREBRICK);
	                actiontarget.setText("Username in use!");
				}
			}
		});
        
        btn.setOnAction(new EventHandler<ActionEvent>() {//regisztralasi gomb esemenykezeloje
            @Override
            public void handle(ActionEvent e) {//ellenorzes
            	String user = userTextField.getText();
            	String password = pwBox.getText();
            	if(user.matches("[A-Za-z0-9]+") && password.matches("[A-Za-z0-9]+")){//ragisztralasi kerelem elkuldese a felhasznalonevvel es a jelszoval
            		mConnection.send(ConnectionManager.GLOBAL_CHANEL,ConnectionManager.TYPE_REGISTER,user+":"+password,false);
            	}else{//hibajelzes
            		actiontarget.setFill(Color.FIREBRICK);
	                actiontarget.setText("Only big and small characters and numbers are allowed!");
            	}
            }
        });
        
        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
	}

	//fo belepesi pont
	public static void main(String[] args) {
		launch(args);//fx elinditasa
	}
	
	//cshatablak letrehozasa
	private Pane createChatPane(BorderPane base){
		////////////////////////////////////////////////////bottom - sendArrea
        VBox textGetter = new VBox();
        textGetter.setPadding(new Insets(10));
        textGetter.setSpacing(8);
        
        TextArea ta = new TextArea();
        ta.setOnKeyPressed(new EventHandler<KeyEvent>() {//cshat doboz szoveggel kapcsolatos esemenykezeloje
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ENTER){
					if(event.isShiftDown()){//sift + enter az enterhez
						ta.appendText("\n");
					}else{//enterrel kuldi az uzenetet
						mConnection.send(null,ConnectionManager.TYPE_MSG, ta.getText(),true);
						ta.setText("");
						event.consume();
					}
				}
			}
		});
        
        HBox buttons = new HBox();
        Button btn = new Button();
        btn.setText("Send");
        btn.setOnAction(new EventHandler<ActionEvent>() {//kuldesgomb esemenykezeloje
            @Override
            public void handle(ActionEvent event) {
            	mConnection.send(null,ConnectionManager.TYPE_MSG, ta.getText(),true);
				ta.setText("");
				ta.requestFocus();
            }
        });
        
        Button fileBtn = new Button();
        fileBtn.setText("Send file");
        fileBtn.setOnAction(new EventHandler<ActionEvent>() { //fejlkuldes gomb esemenykezeloje
            @Override
            public void handle(ActionEvent event) {
            	FileChooser fileChooser = new FileChooser();
            	fileChooser.setTitle("Send file");
            	File file = fileChooser.showOpenDialog(owner);//fajl kivalasztasa
            	if(file != null){//ha van fajl
            		if(file.length() > 1024 * 1024 * 5){//ha nagyobb mint 5 mega
            			ShowErrorDialog("File too big!(Must be under 5MB)", false);
            		}else{//ha nem az
	            		byte[] contentsByte = null;
	            		try {//szoveg kiolvasasa belolle
	            			contentsByte = Files.readAllBytes(file.toPath());
						} catch (IOException e) {
							e.printStackTrace();
						}
	            		String contents = encode(contentsByte);//atalakitas kuldheto formatumba
	            		if(contents != null){
	            			mConnection.sendFile(contents, file.getName());//kuldes
	            		}
            		}
            	}else{
            		System.out.println("No file found!");
            	}
            }
        });
        
        buttons.getChildren().addAll(btn,fileBtn);
        
        textGetter.getChildren().addAll(ta,buttons);
        base.setBottom(textGetter);
        ////////////////////////////////////////////////////
        
        ////////////////////////////////////////////////////center - chatBoxArea
        list = new ListView<String>();
        uzenetek = new ArrayList<>();
        ObservableList<String> oUzenetek = FXCollections.observableArrayList(uzenetek);
        list.setItems(oUzenetek);
        list.setMaxWidth(600);
        list.setCellFactory(param -> new ListCell<String>() {//uzenetek kezelese a kepenyon, es ennek megvalositasa
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                Text text = new Text();
                text.wrappingWidthProperty().bind(list.widthProperty().subtract(15));
                text.textProperty().bind(itemProperty());

                setPrefWidth(0);
                setGraphic(text);
            }
        });
        
        mConnection.reConnect(new WriteOutInterface(){//feldolgozas veglegesitese es a csetablakra szabasa
			@Override
			public void write(String replay) {
				uzenetek.add(replay);
				ObservableList<String> oUzenetek = FXCollections.observableArrayList(uzenetek);
		        list.setItems(oUzenetek);
		        list.scrollTo(list.getItems().size()-1);//legorgetunk a csetablak vegebe uj uzenet eseten
		    }
		});
        
        base.setCenter(list);
        ////////////////////////////////////////////////////
        
		////////////////////////////////////////////////////left - chatRooms
        VBox left = new VBox();
        Label chatrooms = new Label("Chat rooms:");
		listRooms = new ListView<String>();
		rooms = new ArrayList<>();
		oRooms = FXCollections.observableArrayList(rooms);
		listRooms.setItems(oRooms);
		listRooms.setOnMouseClicked(new EventHandler<MouseEvent>() {//csetszobara kattintas esemenykezeloje
			@Override
			public void handle(MouseEvent event) {//belepunk a csetszobaba
				if(event.getClickCount() == 2){
					String room = listRooms.getSelectionModel().getSelectedItem();
					if(!room.equals(mConnection.getChanel())){
						mConnection.send(ConnectionManager.GLOBAL_CHANEL, ConnectionManager.TYPE_JOIN_ROOM, room,false);
					}
				}
			}
		});
		
		listRooms.setMaxWidth(150);
		
		Label newRoom = new Label("New room:");
		TextField createRoom = new TextField();
		createRoom.setOnKeyPressed(new EventHandler<KeyEvent>() {//esemenykezelo uj csetszoba letrehozasahoz
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode() == KeyCode.ENTER){//enter lenyomasara letrehozza, ha...
					String text = createRoom.getText();
					int f = text.indexOf('\n');
					if(f > 0){
						text = text.substring(0, f);
					}
					if(text.matches("[A-Za-z0-9]+")){//csak kis es nagybetuk vagy szamok szerepelnek benne
						if(!text.equals(mConnection.getChanel())){//csetszobaba csatlakozas keresenek kuldese
							mConnection.send(ConnectionManager.GLOBAL_CHANEL,ConnectionManager.TYPE_JOIN_ROOM, text,false);
							createRoom.setText("");
							event.consume();
						}else{
							createRoom.setText("");
							event.consume();
						}
					}else{//hiba eseten
						ShowErrorDialog("Only big and small characters and numers are allowed!", false);
					}
				}
			}
		});
		
		left.getChildren().addAll(chatrooms,listRooms,newRoom,createRoom);
		
		mConnection.send(null, ConnectionManager.TYPE_REQ_ROOMS, "mind1",false);//csatlakozas eseten lekerjuk a meglevo csetszobakat
		
		base.setLeft(left);
		////////////////////////////////////////////////////

		////////////////////////////////////////////////////right - activeClientsFromRoom
		VBox rigth = new VBox();
		Label activeclients = new Label("Clients");
		listClients = new ListView<String>();
		clients = new ArrayList<>();
		oClients = FXCollections.observableArrayList(clients);
		listClients.setItems(oClients);
		
		listClients.setMaxWidth(150);
		
		listClients.setOnMouseClicked(new EventHandler<MouseEvent>() {//kliensre kattintva kliensel kezdemenyezunk egy privat csetelest
			@Override
			public void handle(MouseEvent event) {
				if(event.getClickCount() == 2){
					String clientName = listClients.getSelectionModel().getSelectedItem();
					if(!clientName.equals(mConnection.getUser())){
						mConnection.send(ConnectionManager.GLOBAL_CHANEL, ConnectionManager.TYPE_REQ_CHAT_WITH_CLIENT, clientName,false);
					}
				}
			}
		});
		
		rigth.getChildren().addAll(activeclients,listClients);
		
		base.setRight(rigth);
		////////////////////////////////////////////////////
       
        return base;
	}
	
	//error dialogus felmutatasa
	private static void ShowErrorDialog(String msg, boolean modal) {
		final Stage dialog = new Stage();
		if(modal){//modalis?
			dialog.initModality(Modality.WINDOW_MODAL);
			dialog.setTitle("Fatal Error!");
		}else{
			dialog.initModality(Modality.NONE);
			dialog.setTitle("Error!");
		}
        dialog.initOwner(owner);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text sceneMsg = new Text(msg);//uzenet beallitasa
        sceneMsg.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        grid.add(sceneMsg, 0, 0, 3, 1);
        
        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();//megjelenites
	}
	
	//privatcseteles felkeresenek mutatasa
	public static void showRequestDialog(String title, String msg, Runnable positiveEvent, Runnable negativeEvent) {
		final Stage dialog = new Stage();
		dialog.initModality(Modality.NONE);
		dialog.setTitle(title);
        dialog.initOwner(owner);
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        Text sceneMsg = new Text(msg);//uzenet beallitasa
        sceneMsg.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        grid.add(sceneMsg, 0, 0, 3, 1);
        
        Button positive = new Button("Yes");//pozitiv valasz
        positive.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				positiveEvent.run();
				dialog.close();
			}
		});
        grid.add(positive, 0, 3);
        
        Button negative = new Button("No");//negativ valasz
        negative.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				negativeEvent.run();
				dialog.close();
			}
		});
        grid.add(negative, 3, 3);
        
        Scene dialogScene = new Scene(grid, 300, 200);
        dialog.setScene(dialogScene);
        dialog.setOnCloseRequest(new EventHandler<WindowEvent>() {//bezaras eseten negativ valasz
			@Override
			public void handle(WindowEvent event) {
				negativeEvent.run();
			}
		});
        dialog.show();
	}
	
	//fajl kuldhetove alakitasa
	public static String encode(byte[] contentsByte) {
		StringBuilder s = new StringBuilder();//string
		
		for(byte b: contentsByte){
			s.append(String.valueOf(b)+":");//minden byteot stringe alakitunk
		}
		String str = s.toString();
		return str.substring(0,str.length()-1);
	}
	
	//fajl kuldhetobol valo visszalakitasa
	public static byte[] decode(String encoded) {
		ArrayList<Byte> bArray = new ArrayList<>();
		String[] strs = encoded.split(":");//fajl felosztasa
		for(String s: strs){
			try{
				bArray.add(Byte.decode(s));//minden szam visszalakitasa byte-ba
			}catch(NumberFormatException e){
				System.err.println(s);
				e.printStackTrace();
			}
		}
		Byte[] bytesO = bArray.toArray(new Byte[bArray.size()]);//javas atalakitas objektumbol primitivbe
		byte[] bytes = new byte[bytesO.length];
		for(int i = 0; i < bytesO.length; ++i){
			bytes[i] = bytesO[i];
		}
		return bytes;
	}
	
}
