package pingpong;


/*
 * rmi 인터페이스
 */
public interface Button extends java.rmi.Remote  {
	public void sendButtonNum(int n,String s) throws java.rmi.RemoteException;
		
}
