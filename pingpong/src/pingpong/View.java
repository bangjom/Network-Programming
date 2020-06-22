package pingpong;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.*;

public class View extends JFrame {

	private static final long serialVersionUID = 1L;
	private int width=500;
	private int height=100;
	private Container contentPane;
	JTextArea message;
	private JButton b1;
	private JButton b2;
	private JButton b3;
	private JButton b4;
	private Client client;
	
	
	/*
	 * gui게임창을 만든다.
	 * 
	 */
	public View(Client client) {
		this.client = client;
		this.setSize(width, height);
		this.setTitle("Player #"+client.playerID+": PINGPONG");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = this.getContentPane();
		message = new JTextArea();
		contentPane.setLayout(new GridLayout(1,5));
		contentPane.add(message);
		message.setText("Creating a simple pingpong game in JAVA.");
		message.setWrapStyleWord(true);
		message.setLineWrap(true);
		message.setEditable(false);
		b1= new JButton("1");
		b2= new JButton("2");
		b3= new JButton("3");
		b4= new JButton("4");
		contentPane.add(b1);
		contentPane.add(b2);
		contentPane.add(b3);
		contentPane.add(b4);
		
		if(client.playerID == 1) {
			message.setText("You are Player #1. You go first.");
			client.otherPlayer = 2;
			client.buttonsEnabled = true;
		}
		else {
			message.setText("You are Player #2. Wait for your turn.");
			client.otherPlayer = 1;
			client.buttonsEnabled = false;
			Thread t = new Thread(new Runnable() {
				public void run() {
					updateTurn();
				}
			});
			t.start();
		}
		
		toggleButtons();
		setUpButtons();
		this.setVisible(true);
	} 
	
	/*
	 * 내 차례일때만 버튼을 활성화해준다.
	 */
	public void setUpButtons() {
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				JButton b = (JButton) ae.getSource();
				int bNum = Integer.parseInt(b.getText());
				
				message.setText("You Clicked button #"+bNum+". Now wait for player #"+client.otherPlayer);
				client.turnsMade++;
				System.out.println("Turns made : "+client.turnsMade);
				
				client.buttonsEnabled = false;
				toggleButtons();
				
				client.myPoints += client.values[bNum-1];
				System.out.println("My points : "+client.myPoints);
				try {
					client.csc.remoteObj.sendButtonNum(bNum,"Player");
					try {
						client.csc.dataOut.writeInt(bNum);
						client.csc.dataOut.flush();
					} catch(IOException e) {
						System.out.println("IOException from sendButton() ssc");
					}
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(client.playerID == 2 && client.turnsMade == client.maxTurns) { //player 2가 항상 마지막
					checkWinner();
				}
				else {
					Thread t = new Thread(new Runnable() {
						public void run() {
							updateTurn();
						}
					});
					t.start();
				}
			}
		};
		
		b1.addActionListener(al);
		b2.addActionListener(al);
		b3.addActionListener(al);
		b4.addActionListener(al);
	}
	
	/*
	 * 버튼을 활성화해준다.
	 */
	public void toggleButtons() {
		b1.setEnabled(client.buttonsEnabled);
		b2.setEnabled(client.buttonsEnabled);
		b3.setEnabled(client.buttonsEnabled);
		b4.setEnabled(client.buttonsEnabled);
	}
	
	/*
	 * 게임 마지막에 승리한 사람을 알려준다.
	 */
	void checkWinner() {
		client.buttonsEnabled = false;
		if(client.myPoints > client.enemyPoints) {
			message.setText("You Won!\n"+"YOU : "+ client.myPoints+"\n"+"ENEMY : "+client.enemyPoints);
		}
		else if(client.myPoints < client.enemyPoints) {
			message.setText("You LOST!\n"+"YOU : "+ client.myPoints+"\n"+"ENEMY : "+client.enemyPoints);
		}
		else {
			message.setText("You DRAW!\n"+"YOU : "+ client.myPoints+"\n"+"ENEMY : "+client.enemyPoints);
		}
		
		client.csc.closeConnection();
	}
	/*
	 * 버튼이 눌린 후 턴을 넘겨준다.
	 */
	public void updateTurn() {
		int n= client.csc.receiveButtonNum();
		message.setText("Your enemy clicked button #"+n+". Your turn.");
		System.out.println(n);
		client.enemyPoints += client.values[n-1];
		client.buttonsEnabled = true;
		if(client.playerID == 1 && client.turnsMade == client.maxTurns) {   //player1은 항상 player2의 값을 받아오기를 기다려야한다
			checkWinner();
		}
		else {
			client.buttonsEnabled = true;
		}
		toggleButtons();
	}
}
