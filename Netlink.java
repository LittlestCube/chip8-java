import java.net.Socket;
import java.net.ServerSocket;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.PrintWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.EOFException;

public class Netlink extends Thread
{
	static int connected = 0;			// 0 for no connection, 1 for we're the server, 2 for we're the client
	public static final int SERVER = 1;
	public static final int CLIENT = 2;
	
	String host;
	
	final int PORT = 6969;
	
	ServerSocket server;
	
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	boolean clientKeepRunning = false;
	
	static int sendTrueKeyValue = -1;
	static int sendFalseKeyValue = -1;
	static int sendSoundValue = -1;
	static boolean[] sendGfx = null;
	
	static int receiveTrueKeyValue = -1;
	static int receiveFalseKeyValue = -1;
	static int receiveSoundValue = -1;
	static boolean[] receiveGfx = null;
	
	public void run()
	{
		Socket connectedClient = null;
		Socket client = null;
		
		Object objectReceived;
		
		if (connected == SERVER)
		{
			try
			{
				server = new ServerSocket(PORT);
				
				connectedClient = server.accept();
				
				oos = new ObjectOutputStream(connectedClient.getOutputStream());
				ois = new ObjectInputStream(connectedClient.getInputStream());
			}
			
			catch (Exception e)
			{
				System.err.println("E: Couldn't connect to client...\n\n");
				e.printStackTrace();
			}
			
			System.out.println("Client found! Beginning process...");
			
			while (true)
			{
				try
				{
					while (!checkIfShouldSend()) {}
					
					try
					{
						System.out.println(objectReceived = ois.readObject());
					}
					
					catch (EOFException e)
					{
						System.out.println("Client disconnected! Cleaning up...");
						ois.close();
						oos.close();
						
						connected = 0;
						System.out.println("Done!");
						break;
					}
					
					process(objectReceived);
				}
				
				catch (Exception e)
				{
					System.err.println("E: Couldn't read Object...\n\n");
					e.printStackTrace();
				}
			}
		}
		
		else if (connected == CLIENT)
		{
			try
			{
				client = new Socket(host, PORT);
				
				oos = new ObjectOutputStream(client.getOutputStream());
				ois = new ObjectInputStream(client.getInputStream());
			}
			
			catch (Exception e)
			{
				System.err.println("E: Couldn't connect to server...\n\n");
				e.printStackTrace();
			}
			
			while (true)
			{
				System.out.println("asdf");
				
				try
				{
					while (!checkIfShouldSend()) {}
					
					process(ois.readObject());
				}
				
				catch (Exception e)
				{
					System.err.println("E: Couldn't read Object...\n\n");
					e.printStackTrace();
				}
			}
		}
	}
	
	public void initAsServer()
	{
		try
		{
			System.out.println("I: Cool! We're the server.");
			
			System.out.println("I: Success! Server initialized at port " + PORT + ".");
			
			connected = SERVER;
			
			this.start();
		}
		
		catch (Exception e)
		{
			System.err.println("E: Couldn't initialize ServerSocket...\n\n");
			e.printStackTrace();
		}
	}
	
	public void initAsClient(String hostname)
	{
		try
		{
			System.out.println("I: Cool! We're the client.");
			
			System.out.println("I: Success! Client initialized at port " + PORT + ".");
			
			host = hostname;
			connected = CLIENT;
			clientKeepRunning = true;
			
			this.start();
		}
		
		catch (Exception e)
		{
			System.err.println("E: Couldn't initialize Socket...\n\n");
			e.printStackTrace();
		}
	}
	
	public void sendObject(Object output) throws Exception
	{
		oos.writeObject(output);
	}
	
	public void process(Object input)
	{
		System.out.println("input: ");
		System.out.println(input);
		
		try
		{
			System.out.println(input);
			
			if (input == null)
			{
				return;
			}
			
			if (input instanceof String)
			{
				String inputString = (String) input;
				
				if (inputString.startsWith("KT:"))
				{
					receiveTrueKeyValue = Integer.parseInt(inputString.substring(inputString.indexOf(':') + 2, inputString.length()));
				}
				
				else if (inputString.startsWith("KF:"))
				{
					receiveFalseKeyValue = Integer.parseInt(inputString.substring(inputString.indexOf(':') + 2, inputString.length()));
				}
				
				else if (inputString.startsWith("S:"))
				{
					receiveSoundValue = Integer.parseInt(inputString.substring(inputString.indexOf(':') + 2, inputString.length()));
				}
				
				else
				{
					System.out.println(inputString);
				}
			}
			
			if (input instanceof boolean[])
			{
				receiveGfx = (boolean[]) input;
				System.out.println(input);
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean checkIfShouldSend()
	{
		Object output = new Object();
		
		try
		{
			if (sendTrueKeyValue != -1)
			{
				output = new String("KT: " + sendTrueKeyValue);
				sendObject(output);
				
				sendTrueKeyValue = -1;
				return true;
			}
			
			if (sendFalseKeyValue != -1)
			{
				output = new String("KF: " + sendFalseKeyValue);
				sendObject(output);
				
				sendFalseKeyValue = -1;
				return true;
			}
			
			if (connected == SERVER)
			{
				if (sendSoundValue != -1)
				{
					output = new String("S: " + sendSoundValue);
					sendObject(output);
					
					sendSoundValue = -1;
					return true;
				}
				
				if (sendGfx != null)
				{
					output = sendGfx;
					sendObject(output);
					
					sendGfx = null;
					return true;
				}
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public void setGfx(boolean[] gfx)
	{
		sendGfx = gfx;
	}
	
	public boolean[] getGfx()
	{
		return receiveGfx;
	}
}