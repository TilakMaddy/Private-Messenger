package messenger;

//Imports
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Server extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private ServerSocket server;
	private Socket connection;	
	
	//Main method to start Server
	public static void main(String []args){
		ServerExample server = new ServerExample();
		server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		server.setLocationRelativeTo(null);
		server.setSize(400, 200);		
		server.startRunning();
	}
	
	//Constructor
	public ServerExample(){
		super("Instant Messenger");
		configureUserText();
		configureChatWindow();		
		setVisible(true);		
	}
	
	//Set up and Run Server
	public void startRunning(){
		try{
			server = new ServerSocket(7777, 100);
			while(true){
				try{
					waitForConnection();
					setupStreams();
					whileChatting();
				}catch(EOFException e){
					showMessage("\nServer ended the connection. ");
				}finally{
					close();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//Set up the text field
	public void configureUserText(){
		Border border = BorderFactory.createTitledBorder("Enter message");
		userText = new JTextField(""); 
		userText.setToolTipText("Enter message to send to the client.");
		userText.setBorder(border);
		userText.setEditable(false); 		
		userText.setFont(new Font(Font.SANS_SERIF, Font.BOLD , 15));
		userText.setForeground(Color.GREEN);
		userText.setBackground(Color.BLACK);
		userText.addActionListener(
				new ActionListener(){
					public void actionPerformed(ActionEvent event){
						sendMessage(event.getActionCommand());
						userText.setText("");
					}
				}
		);
		add(userText, BorderLayout.NORTH);
	}
	
	//Set up the Chat Window
	public void configureChatWindow(){
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		chatWindow.setBackground(Color.RED);
		chatWindow.setFont(new Font("Tahoma", Font.BOLD, 14));
		chatWindow.setForeground(Color.BLACK);		
		this.add(new JScrollPane(chatWindow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	}

	//Wait for connection and display IP information
	public void waitForConnection() throws IOException{
		showMessage("Waiting for Connections...\n");
		connection = server.accept();
		showMessage("Now Connected to " + connection.getInetAddress().getHostName().toString().concat("\n"));
		chatWindow.setBackground(Color.BLACK);
		chatWindow.setForeground(Color.CYAN);
		userText.setToolTipText("Enter message to send to " + connection.getInetAddress().getHostName().toString());
	}
	
	//Setup streams to receive  Data
	public void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n Streams are setup successfully.");
	}
	
	//While chat conversation
	public void whileChatting(){
		String message = "You are now connected to Maddy network. !";
		sendMessage(message);
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				showMessage("\nClient: " + message);
			}catch(ClassNotFoundException e){
				showMessage("\nMessage not readable.!");
			}catch(IOException ie){
				ie.printStackTrace();				
			}
		}while(!message.equals("END"));
	}
	
	//Close streams and sockets after chat
	public void close(){
		showMessage("\nClosing connections..\n");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//Send a message to Client
	public void sendMessage(String message){
				
		try{
			output.writeObject(message);
			output.flush();
			showMessage("\nServer - " + message);			
		}catch(IOException e){
			chatWindow.append("\nERROR sending message.\n");
		}
	}
	
	//Show message in chat Window
	public void showMessage(final String text){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						try{
							chatWindow.append(text);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
		);
	}
	
	//Give permission for user to type
	public void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						try{
							userText.setEditable(tof);
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
		);
	}
	
}
