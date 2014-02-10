package channels;

public abstract class ChannelDecorator implements Channel {
	protected Channel channel;
	
	public ChannelDecorator(Channel c){
		this.channel = c;
	}
	
	@Override
	public void sendObject(Object o) throws Exception {
		channel.sendObject(o);
	}
	
	@Override
	public Object readObject() throws Exception{
		return channel.readObject();
	}

	@Override
	public void shutdown() {
		channel.shutdown();
	}
	

}
