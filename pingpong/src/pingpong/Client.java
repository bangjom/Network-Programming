package pingpong;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;


public class Client {

	/*
	 * ip를 설정해주세요~
	 */
	 String server="localhost";
	 int playerID;
	 int otherPlayer;
	 int[] values;
	 int maxTurns;
	 int turnsMade;
	 int myPoints;
	 int enemyPoints;
	 boolean buttonsEnabled;

	 ClientSideConnection csc;
	
	BufferedReader in;
	PrintStream out;
	
	SSLSocketFactory f;
	SSLSocket c;
	
	static BufferedWriter w;
	static BufferedReader r;
	
	/* 
	 * 클라이언트 생성자를 실행한다.
	 * 서버와 연결해준다.
	 * gui를 만들어준다.
	 */
	public static void main(String[] args) {
		Client p = new Client();
		p.connectToServer();
		View view= new View(p);
		
	}

	public Client() {
		
		values = new int[4];
		turnsMade = 0;
		myPoints=0;
		enemyPoints=0;
		
	}
	
	
	/*
	 * 서버에서 리바인딩한 rmi를 가져와서 인터페이스 변수에 할당해준다.
	 * 클라이언트사이드커넥션을 생성한다.
	 */
	public void connectToServer() {
			Button remoteObj;
			try {
				remoteObj = (Button) Naming.lookup("rmi://"+server+":1099/ButtonRemote");
				csc = new ClientSideConnection(remoteObj);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (NotBoundException e) {
				e.printStackTrace();
			}
		
	}
	
	
	class ClientSideConnection{
		
		private SSLSocket socket;
		DataInputStream dataIn;
		DataOutputStream dataOut;
		Button remoteObj;
		
		/*
		 * 키스토어에서 키를 받아서 소켓을 생성한다.(보안인증) 
		 */
		public ClientSideConnection(Button remoteObj) {
			this.remoteObj=remoteObj;
			System.out.println("-----Client-----");
			try {
				System.setProperty("javax.net.ssl.trustStore","trustedcerts");
				System.setProperty("javax.net.ssl.trustStorePassword", "123456");
				
				f=(SSLSocketFactory) SSLSocketFactory.getDefault();
				System.out.println(f);
				socket=(SSLSocket) f.createSocket(server,8888); 
				System.out.println(socket);

				String[] supported = socket.getSupportedCipherSuites();
				socket.setEnabledCipherSuites(supported);
				
				socket.startHandshake();
				dataIn =new DataInputStream(socket.getInputStream());
				dataOut =new DataOutputStream(socket.getOutputStream());
				playerID = dataIn.readInt();
				System.out.println("Connected to server as Player #"+playerID+".");
				maxTurns = dataIn.readInt() / 2;
				values[0]=dataIn.readInt();
				values[1]=dataIn.readInt();
				values[2]=dataIn.readInt();
				values[3]=dataIn.readInt(); //server에서 발생하는 random values를 받음
				System.out.println("maxTurns: "+ maxTurns);
				
			} catch(IOException e) {
				System.out.println("IOException from ClientSideConnection constructor");
			}
		}
		
		/*
		 * 서버에서 버튼값을 받아준다.
		 */
		public int receiveButtonNum() {
			int n = -1;
			try {
				n = (Integer) dataIn.readInt();
				System.out.println("Player #"+otherPlayer+" clicked button #"+n);
			} catch(IOException e) {
				System.out.println("IOException from receiveButtonNum() CSC");
			}
			return n;
		}
		
		/*
		 *	서버가 끝나면 소켓을 닫아준다. 
		 */
		public void closeConnection() {
			try {
				socket.close();
				System.out.println("----CONNECTION CLOSED----");
				System.out.println("Value # 1 is "+values[0]);
				System.out.println("Value # 2 is "+values[1]);
				System.out.println("Value # 3 is "+values[2]);
				System.out.println("Value # 4 is "+values[3]);
			} catch(IOException e) {
				System.out.println("IOException on closeConnection() CSC");
			}
		}
	}
}
