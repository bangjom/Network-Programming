package pingpong;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

	/*
	 * rmi 구현클래스
	 */
	public class ButtonImpl extends UnicastRemoteObject implements Button {
	private static final long serialVersionUID = 1L;
	
		public ButtonImpl() throws RemoteException{
			super();
		}
		public void sendButtonNum(int n,String s) {
			System.out.println(s+" clicked button #"+ n);
		}
}

