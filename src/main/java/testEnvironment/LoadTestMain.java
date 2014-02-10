package testEnvironment;

import proxy.Proxy;
import server.FileServerMain1;
import server.FileServerMain2;
import util.Config;
import util.ShellThread;
import cli.Shell;

public class LoadTestMain {

	public static void main(String args[]){
		//Proxy.main(args);
		//FileServerMain1.main(args);
		//FileServerMain2.main(args);
    	ShellThread st = new ShellThread(new Shell("loadTestShell", System.out, System.in));
	    LoadTestLogic logic = new LoadTestLogic(new Config("loadtest"), st);
	    System.out.println(logic);
	    logic.startTesting();
	}
}
