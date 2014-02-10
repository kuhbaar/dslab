package testEnvironment;

import cli.Shell;
import util.Config;
import util.ShellThread;

public class LoadTestLogic {

	private Shell s;
	private int clients;
	private int uploadsPerMin;
	private int downloadsPerMin;
	private int fileSizeKB;
	private double overwriteRatio;
	
	public LoadTestLogic(Config c, ShellThread st)	{
	    this.s = st.getShell();
	    try{
	      this.clients = c.getInt("clients");
	      this.uploadsPerMin = c.getInt("uploadsPerMin");
	      this.downloadsPerMin = c.getInt("downloadsPerMin");
	      this.fileSizeKB= c.getInt("fileSizeKB");
	      this.overwriteRatio= Double.valueOf(c.getString("overwriteRatio"));
	      
	    } catch (Exception e) {
	      System.err.println("parameters in config file missing or invalid");
	      e.printStackTrace();
	      System.exit(0); // no threads has been started yet
	    }
	}

	public void startTesting()	{
		for(int i=0;i<clients;++i)	{
			LoadTestClientThread thread = new LoadTestClientThread(this);
			thread.run(true);
		}
		LoadTestClientThread thread = new LoadTestClientThread(this);
		thread.run();
		thread.subscribe();
	}
	
	
	
	public int getUploadsPerMin() {
		return uploadsPerMin;
	}

	public void setUploadsPerMin(int uploadsPerMin) {
		this.uploadsPerMin = uploadsPerMin;
	}

	public int getDownloadsPerMin() {
		return downloadsPerMin;
	}

	public void setDownloadsPerMin(int downloadsPerMin) {
		this.downloadsPerMin = downloadsPerMin;
	}

	public int getFileSizeKB() {
		return fileSizeKB;
	}

	public void setFileSizeKB(int fileSizeKB) {
		this.fileSizeKB = fileSizeKB;
	}

	public double getOverwriteRatio() {
		return overwriteRatio;
	}

	public void setOverwriteRatio(double overwriteRatio) {
		this.overwriteRatio = overwriteRatio;
	}

	@Override
	public String toString() {
		return "LoadTestLogic [clients=" + clients
				+ ", uploadsPerMin=" + uploadsPerMin + ", downloadsPerMin="
				+ downloadsPerMin + ", fileSizeKB=" + fileSizeKB
				+ ", overwriteRatio=" + overwriteRatio + "]";
	}
	
	
}
