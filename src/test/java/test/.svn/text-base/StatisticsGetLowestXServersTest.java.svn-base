package test;

import model.FileserverInfoPersist;

import org.junit.Before;
import org.junit.Test;

import proxy.Statistics;
import util.CliComponent;

import java.util.*;

public class StatisticsGetLowestXServersTest {

	// Communication
	Map<String, CliComponent> componentMap = new HashMap<String, CliComponent>();
	CliComponent component;


	@Before
	public void setUp() throws Exception {
	}


	@Test
	public void test() throws Throwable {
		Statistics stats = new Statistics();
		stats.fileserverPut("Test1 100");
		stats.fileserverPut("Test2 200");
		stats.fileserverPut("Test3 300");
		stats.fileserverPut("Test4 400");
		stats.fileserverPut("Test5 500");
		stats.calculateReadWriteQuorum();
		System.out.println(
				stats.getNr() +":"+
				stats.getNw()
		);
		List<FileserverInfoPersist> tmp = stats.getLowestXFileserversAddrs(1);
		for(FileserverInfoPersist fs: tmp)	
			System.out.println(fs.getAddr() + " : "+ fs.getUsage());
	}

}
