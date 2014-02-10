package channels;

public interface Channel {
	public void sendObject(Object o) throws Exception;
	public Object readObject() throws Exception;
	public void shutdown();
}
