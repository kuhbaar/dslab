package testEnvironment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.TimerTask;

import client.ClientThread;

public class UploadTimerTask extends TimerTask{

	private LoadTestClientThread parent;
	private ClientThread cThread;
	private double overwriteRatio;
	
	public UploadTimerTask(LoadTestClientThread parent)	{
		this.parent = parent;
		cThread = parent.getcLogic().getClientThread();
		overwriteRatio = parent.gettLogic().getOverwriteRatio();
	}
	
	private String createRandomBytes(int len)	{
		String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random rnd = new Random();
		StringBuilder sb = new StringBuilder( len );
		   for( int i = 0; i < len; i++ ) 
		      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		   return sb.toString();
	}
	
	@Override
	public void run() {
		File tmpFile = new File(parent.getcLogic().getDDir());
		try {
			tmpFile = File.createTempFile("tmpfile", ".txt", tmpFile);
			BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
			bw.write(createRandomBytes(parent.gettLogic().getFileSizeKB()*1000));
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			

        try {
        	System.out.println("Calculation " + parent.getUploadOverwrites()+ "/"  + parent.getUploadsTotal()+">="+overwriteRatio);
        	if(parent.getUploadsTotal() == 0)	{
        		//
        		System.out.println("First File: "+cThread.upload(tmpFile.getName()));
        		parent.setLastFile(tmpFile);
        	}
        	else if(((double)parent.getUploadOverwrites() / (double)parent.getUploadsTotal())>=overwriteRatio)	{
	        	
				
				System.out.println("New File: "+cThread.upload(tmpFile.getName()));
				if(parent.getLastFile() != null)
					parent.getLastFile().delete();
				parent.setLastFile(tmpFile);
        	}
        	else	{
        		System.out.println("Existing File: "+cThread.upload(parent.getLastFile().getName()));
        		parent.incUploadOverwrites();
        		tmpFile.delete();
        	}
			
        	parent.incUploadsTotal();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
		
		
	}

}
