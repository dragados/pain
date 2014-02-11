/**Current TO-DO:
 * 1: Database to store user/buddy list information
 * 		a: table of user/pw 
 * 		b: table of pal uname+group for each user
 * 2: hook into database for actual log in
 * 3: encryption of messages
 * 4: logging utility
 * 5: cloud logs?
 */

package pain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.*;
import javafx.scene.control.TextArea;
import javafx.util.Callback;

public class Client extends Application{

	private HashMap<String, messageWindow> windows = new HashMap<String, messageWindow>();
	private PrintWriter out = null;
	private String loginID = null;

	@Override
	public void start(Stage primaryStage) {

		new loginWindow();


	}


	private class loginWindow{
		private loginWindow(){
			StackPane root = new StackPane();
			final Stage loginWindow = new Stage();
			
			

			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			Text scenetitle = new Text("Login");
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(scenetitle, 0, 0);


			final TextField usernameInput = new TextField();
			
			EventHandler<ActionEvent> loginHandler = new EventHandler<ActionEvent>(){
				public void handle(ActionEvent event){
					if(usernameInput.getText().length()>0){	
						loginAttempt(usernameInput.getText());
						loginWindow.hide();
					}
				}
			};
			
			usernameInput.setOnAction(loginHandler);
			grid.add(usernameInput, 0, 1);


			Button newWindow = new Button("Login");
			grid.add(newWindow, 0, 4);
			newWindow.setOnAction(loginHandler);


			root.getChildren().add(grid);
			loginWindow.setTitle("PAIN");
			loginWindow.setScene(new Scene(root, 450, 350));
			usernameInput.requestFocus();
			loginWindow.show();
		}

		boolean loginAttempt(String username){
			loginID = username;
			new buddyList();
			Connection conn = null;
			conn = DriverManager.getConnection("jdbc:mysql://162.217.176.77:6275/pain");
			return false;
		}
	}


	private class buddyList{
		private buddyList(){
			final Stage primaryStage = new Stage();
			Socket link = null;
			BufferedReader in = null;

			try {
				link = new Socket(InetAddress.getByName("162.217.176.77"),20000);
			} catch (UnknownHostException e) {
				System.out.println("oh dear, this is bad. UnknownHostException!");
				System.exit(1);
			} catch (IOException e) {
				System.out.println("oh dear, this is bad. IOException!");
				System.exit(1);
			} 

			try {
				in = new BufferedReader(new InputStreamReader(link.getInputStream()));
				out = new PrintWriter(link.getOutputStream(),true);
			} catch (IOException e) {
				System.out.println("problem creating in/out streams");
				System.exit(1);
			} 

			out.println(loginID);
			final ChatListener rcv = new ChatListener(in);
			primaryStage.setTitle("PAIN");
			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(0, 0, 0, 0));
			StackPane root = new StackPane();

			Text scenetitle = new Text("Patte's Amateur Instant Note-sender");
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(scenetitle, 0, 0);


			TreeItem<String> rootItem = new TreeItem<String>("#Buddies");
			rootItem.setExpanded(true);


			String[] buddies=null;
			try {
				buddies = in.readLine().split(":");
			} catch (IOException e) {
				System.out.println("Problem read");
			}
			for (String s : buddies){
				String[] split = s.split(";");
				TreeItem<String> buddyLeaf = new TreeItem<String>(split[0]);
				boolean found = false;
				for(TreeItem<String> groupBranch : rootItem.getChildren()){
					if (groupBranch.getValue().equals(split[1])){
						groupBranch.getChildren().add(buddyLeaf);
						found = true;
						break;
					}
				}
				if(!found){
					TreeItem<String> groupBranch = new TreeItem<String>(split[1]);
					rootItem.getChildren().add(groupBranch);
					groupBranch.getChildren().add(buddyLeaf);
					groupBranch.setExpanded(true);
				}
			}


			final TreeView<String> tree = new TreeView<String>(rootItem);
			tree.setMaxHeight(200);
			tree.setEditable(true); // enables handler for double clicks
			tree.setCellFactory(new Callback<TreeView<String>,TreeCell<String>>(){
				@Override
				public TreeCell<String> call(TreeView<String> arg0) {
					return new TextFieldTreeCellImpl();
				}
			});
			grid.add(tree, 0, 1);


			Label inputLabel = new Label("New Recipient");
			grid.add(inputLabel, 0, 2);


			final TextField inputBox = new TextField();
			inputBox.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent event){
					new messageWindow(inputBox.getText());
				}
			});
			inputBox.setMinHeight(25);
			grid.add(inputBox, 0, 3);

			Button newWindow = new Button("Create Window");
			grid.add(newWindow, 0, 4);
			newWindow.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent event){
					new messageWindow(inputBox.getText());
				}
			});
			root.getChildren().add(grid);
			primaryStage.setScene(new Scene(root, 350, 400));
			primaryStage.show();
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
				public void handle(WindowEvent arg0) {
					for(messageWindow m : windows.values()){
						m.getWindow().hide();
					}
					rcv.interrupt();
					out.println("DONE");
				}

			});
			rcv.start();

		}
	}



	private class messageWindow{

		private Stage window = null;
		private TextArea chatlog = null;


		private messageWindow(final String username){
			StackPane root = new StackPane();
			window = new Stage();

			GridPane grid = new GridPane();
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));

			Text scenetitle = new Text("Chat session with "+username);
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(scenetitle, 0, 0, 1, 1);


			chatlog = new TextArea();
			windows.put(username, this);
			chatlog.setEditable(false);
			window.setOnCloseRequest(new EventHandler<WindowEvent>(){
				public void handle(WindowEvent arg0) {
					windows.remove(username);
				}
			});
			grid.add(chatlog, 0, 1);


			final TextField inputBox = new TextField();
			inputBox.setOnAction(new EventHandler<ActionEvent>(){
				public void handle(ActionEvent event){
					String timestamp = DateFormat.getTimeInstance().format(new Date());
					chatlog.setText(chatlog.getText()+"["+timestamp+"] "+loginID+": "+inputBox.getText()+"\n");
					out.println(loginID+";"+inputBox.getText()+";"+username);
					inputBox.setText("");
					inputBox.requestFocus();
				}
			});
			grid.add(inputBox, 0, 2);


			root.getChildren().add(grid);
			window.setTitle(username+" - PAIN Chat Session");
			window.setScene(new Scene(root, 450, 350));
			inputBox.requestFocus();
			window.show();
		}

		public Stage getWindow() {
			return window;
		}

		public TextArea getChatlog() {
			return chatlog;
		}

	}


	//handles editing groupnames/creating windows from buddy list
	private final class TextFieldTreeCellImpl extends TreeCell<String> {
		private TextField textField;

		public TextFieldTreeCellImpl(){
		}

		public void startEdit(){
			if(getString().charAt(0)=='#'){
				super.startEdit();
				if (textField == null){
					createTextField();
				}
				setText(null);
				setGraphic(textField);
				textField.selectAll();
			}else{
				new messageWindow(getString());
			}
		}

		public void cancelEdit(){
			super.cancelEdit();
			setText((String) getItem());
			setGraphic(getTreeItem().getGraphic());
		}

		public void updateItem(String item, boolean empty){
			super.updateItem(item, empty);

			if(empty){
				setText(null);
				setGraphic(null);
			}else{
				if(isEditing()){
					if(textField!=null){
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				}else{
					setText(getString());
				}
			}
		}

		private void createTextField(){
			textField = new TextField(getString());
			textField.setOnKeyReleased(new EventHandler<KeyEvent>(){

				@Override
				public void handle(KeyEvent arg0) {
					if(arg0.getCode()==KeyCode.ENTER){
						String temp = textField.getText();
						if(temp.length()==0){
							cancelEdit();
						}else{
							if(temp.charAt(0)!='#'){
								temp = "#"+temp;
							}
							commitEdit(temp);
						}
					}else if(arg0.getCode()==KeyCode.ESCAPE){
						cancelEdit();

					}	
				}
			});
		}

		private String getString(){
			if(getItem()==null){
				return "";
			}else{
				return getItem().toString();
			}
		}

	}


	//receives all messages and passes to message windows
	private class ChatListener extends Thread{
		BufferedReader in;

		public ChatListener(BufferedReader r){
			in=r;
		}

		public void run(){
			while(true){ 
				try {
					String msg = in.readLine();
					if(this.isInterrupted()){
						break;
					}
					final String[] splitMessage = msg.split(";");
					if(!windows.containsKey(splitMessage[0])){
						Platform.runLater(new Runnable(){
							public void run(){
								new messageWindow(splitMessage[0]);
							}
						});
					}
					while(!windows.containsKey(splitMessage[0])){
						//wait for window to be created
					}
					TextArea msgLog = windows.get(splitMessage[0]).getChatlog();
					String timestamp = DateFormat.getTimeInstance().format(new Date());
					msgLog.setText(msgLog.getText()+"["+timestamp+"] "+splitMessage[0]+": "+splitMessage[1]+"\n");

					System.out.println(msg);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args){
		launch(args);
	}
}