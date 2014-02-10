package channels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPChannel implements Channel{
	private Socket s;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public TCPChannel (Socket s) throws IOException{
		this.s = s;
		this.out = new ObjectOutputStream(s.getOutputStream());
		out.flush();
		this.in = new ObjectInputStream(s.getInputStream());
	}
	
	public void sendObject(Object o) throws Exception{
		out.flush();
		out.writeObject(o);
	}
	
	public Object readObject() throws Exception{
		return in.readObject();
	}
	
	public void shutdown(){
		try {
			s.close();
		} catch (IOException e) {		}
	}
}
