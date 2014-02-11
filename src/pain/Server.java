package pain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
	
	private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<ClientHandler>());


	private static ServerSocket servSock;


	public static void main(String[] args){
		try{
			// Create a server object
			servSock = new ServerSocket(20000); 
		}
		catch(IOException e){
			System.out.println("Unable to attach to port!");
			System.exit(1);
		}
		
		System.out.println("done!");
		while(true){
			run();
		}
	}
	
	private static void run() {
		Socket link = null;
		try
		{
			// Put the server into a waiting state
			link = servSock.accept();

			// print local host name
			String host = InetAddress.getLocalHost().getHostName();
			System.out.println("Client has estabished a connection to " + host);

			// Create a thread to handle this connection
			ClientHandler handler = new ClientHandler(link, clients);
			clients.add(handler);
			// start serving this connection
			handler.start(); 
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	
	
}

class ClientHandler extends Thread
{
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private String username;
	private List<ClientHandler> clientList;
	
	public ClientHandler(Socket s, List<ClientHandler> clients)
	{
		this.
		// set up the socket
		client = s;
		clientList = clients;
		try
		{
			// Set up input and output streams for socket
			in = new BufferedReader(new InputStreamReader(client.getInputStream())); 
			out = new PrintWriter(client.getOutputStream(),true); 
			
			// get username
			username = in.readLine();
			out.println(getBuddies());
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	// overwrite the method 'run' of the Runnable interface

	// this method is called automatically when a client thread starts.
	public void run()
	{

		
		System.out.println("test");
		// Receive and process the incoming data 
		try
		{
			String message = in.readLine();
			while (!message.equals("DONE"))
			{
				System.out.println(message);
				String[] splitMessage = message.split(";");
				for(ClientHandler client : clientList){ //check recipient against each user currently logged in
					System.out.println("comparing "+client.getUsername()+" to "+splitMessage[2]);
					if(client.getUsername().equals(splitMessage[2])){ //if match, send message
						System.out.println("printing to "+client.getUsername());
						client.out.println(splitMessage[0]+";"+splitMessage[1]);
					}else{
						System.out.println("not printing to "+client.getUsername());
					}
				}
				message = in.readLine();
			}
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try{
				System.out.println("!!!!! Closing connection... !!!!!" );
				client.close(); 
			}
			catch(IOException e){
				System.out.println("Unable to disconnect!");
				System.exit(1);
			}
		}
		
	}
	
	public String getUsername() {
		return username;
	}

	private String getBuddies(){
		String test = "Ixi;#Friends:Kroi;#Friends:Kayla;#Friends";
		return test;
	}
	
}