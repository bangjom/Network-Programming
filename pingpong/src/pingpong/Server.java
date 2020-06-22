package pingpong;

import java.io.*;

import javax.net.ssl.*;

import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Server {
	private SSLServerSocket s;
	private SSLServerSocketFactory ssf;
	private SSLSocket c;
	private int numPlayers;
	private ServerSideConnection player1;
	private ServerSideConnection player2;
	private int turnsMade;
	private int maxTurns;
	private int[] values;
	private int player1ButtonNum;
	private int player2ButtonNum;
	
	public static void main(String[] args) {
		Server server = new Server();
		server.acceptConnections();
	}
	
	/*
	 *  사용할 변수들을 초기화하고 실행루트와 키스토어 설정해준다.
	 *  
	 */
	public Server() {
		System.out.println("-------Server--------");
		numPlayers = 0;
		turnsMade = 0;
		maxTurns = 8; //최대 turn의 수
		values = new int[4];
		
		for(int i=0;i<values.length;i++) {
			values[i] = (int)Math.ceil(Math.random() * 100); // 0~99.9999..
			System.out.println("Value #"+(i+1)+" is "+ values[i]+".");
		}
		
		try {
			final KeyStore ks;
			final KeyManagerFactory kmf;
			final SSLContext sc;
			final String runRoot = System.getProperty("user.dir") + System.getProperty("file.separator");
			
			String ksName = runRoot + ".keystore/ServerKey";
			char keyStorePass[] = "123456".toCharArray();
			char keyPass[] = "123456".toCharArray();
			
			ks=KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(ksName),keyStorePass);
			
			kmf = KeyManagerFactory.getInstance("SunX509");
			
			kmf.init(ks, keyPass);
			
			sc=SSLContext.getInstance("TLS");
			sc.init(kmf.getKeyManagers(),null,null);
			
			ssf = sc.getServerSocketFactory();
			/*
			 * 포트값을 8888로 하드코딩하였다.
			 */
			s = (SSLServerSocket) ssf.createServerSocket(8888);
			
		} catch(SSLException se) {
			System.out.println("SSL problem.");
		} catch(Exception e) {
			System.out.println("Exception from Server Constructor");
			System.out.println(e);
		} 
	}
	
	/*
	 * 클라이언트가 2명 연결될때까지 기달려준다.
	 */
	public void acceptConnections() {
		try {
			System.out.println("Waiting for connections...");
			ButtonImpl remObj = new ButtonImpl();
			java.rmi.Naming.rebind("rmi://localhost:1099/ButtonRemote", remObj);
			while(numPlayers < 2) {
				c=(SSLSocket)s.accept();
				numPlayers++;
				System.out.println("Player #"+numPlayers+" has connected.");
				ServerSideConnection ssc = new ServerSideConnection(c, numPlayers,remObj);
				if(numPlayers == 1) {
					player1 = ssc;
				} else {
					player2 = ssc;
				}
				Thread t = new Thread(ssc);
				t.start();
			}
			System.out.println("\nWe now have 2 players. No longer accepting connections.");
		} catch(IOException e) {
			System.out.println("IOException from acceptConnections()");
		}
	}
	

	private class ServerSideConnection implements Runnable {
		private SSLSocket socket;
		DataInputStream dataIn;
		DataOutputStream dataOut;
		private int playerID;
		private ButtonImpl remObj;
		
		/*
		 * rmi 구현클래스와 연결한 후 데이터스트림을 만든다.
		 */
		public ServerSideConnection(SSLSocket s, int id,ButtonImpl rem) {
			socket = s;
			playerID = id;
			try {
				remObj = rem;
				dataIn =new DataInputStream(socket.getInputStream());
				dataOut =new DataOutputStream(socket.getOutputStream());
			} catch(IOException e) {
				System.out.println("IOException from SSC constructor");
			}
		}
		
		/*
		 * 버튼 값들을 읽어준다.
		 */
		public void run() {
			try {
				dataOut.writeInt(playerID);
				dataOut.writeInt(maxTurns);
				dataOut.writeInt(values[0]);
				dataOut.writeInt(values[1]);
				dataOut.writeInt(values[2]);
				dataOut.writeInt(values[3]);
				dataOut.flush();
				
				while(true) {
					if(playerID == 1) {
						player1ButtonNum = dataIn.readInt();
						player2.remObj.sendButtonNum(player1ButtonNum,"Player #1");
						try {
							player2.dataOut.writeInt(player1ButtonNum);
							player2.dataOut.flush();
						} catch(IOException e) {
							System.out.println("IOException from sendButton() ssc");
						}
					}
					else {
						player2ButtonNum = dataIn.readInt();
						player1.remObj.sendButtonNum(player2ButtonNum,"Player #2");
						try {
							player1.dataOut.writeInt(player2ButtonNum);
							player1.dataOut.flush();
						} catch(IOException e) {
							System.out.println("IOException from sendButton() ssc");
						}
					}
					turnsMade++;
					if(turnsMade == maxTurns) {
						System.out.println("Max turns has been reached.");
						break;
					}
				}
				player1.closeConnection();
				player2.closeConnection();
			} catch(IOException e) {
				System.out.println("IOExcpetion from run() SSC");
			}
		}
		
		
		/*
		 * 서버 연결을 끊는다.
		 */
		public void closeConnection() {
			try {
				socket.close();
				System.out.println("Connection closed");
			}catch(IOException e) {
				System.out.println("IOException on closeConnect() SSC");
			}
		}
	}
}


