package net.xiying.surefire.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.surefire.junitcore.JUnitCoreProvider;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;

public class BackUp {
	private AbstractProvider[] providers;
	private JUnitCoreProvider baseProvider;

	private TestsToRun testsToRun;

	// private ProcessBuilder pb = null;

	public BackUp(ProviderParameters booterParameters) {
		baseProvider = new JUnitCoreProvider(booterParameters);
		String cps = booterParameters.getProviderProperties().get("cp");
		String[] cp = cps.split(",");
		providers = new AbstractProvider[cp.length];
		for (int i = 0; i < cp.length; i++) {
			cp[i] = cp[i].trim(); 
		}

	}

	public Iterable<Class<?>> getSuites() {
		Iterable<Class<?>> ret = baseProvider.getSuites();
		return ret;
	}

	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		// System.out.println("============Test Xi============");
		String sourceLocation = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/src/test/java/edu/columbia/cs/psl/test/chroniclerj/";
		String targetLocation = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/";

		compilation(sourceLocation, targetLocation, "Solution.java");
		instrument();
		List<String> output1 = generate();
		List<String> output2 = runReplayer();

		System.out.println();

		System.out.println("============Output1============");
		print(output1);
		System.out.println("============Output2============");
		print(output2);

		System.out.println("============Test Runner Output============");
		String p = null;
//		if (OutputComparer.compare(output1, output2)) {
//			p = "Output are the same";
//		} else
//			p = "Output aren't the same";
//		System.out.println(p);

		return baseProvider.invoke(forkTestSet);
	}

	public void print(List<String> list) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}

	public void compilation(String sourceLocation, String targetLocation, String fileName) {
		try {
			System.out.println("============ChroniclerJ compile start============");
			// InputStream input = new BufferedInputStream(new
			// FileInputStream(sourceLocation+fileName));
			// byte[] buffer = new byte[8192];
			//
			// try {
			// for (int length = 0; (length = input.read(buffer)) != -1;) {
			// System.out.write(buffer, 0, length);
			// }
			// } finally {
			// input.close();
			// }

			ProcessBuilder pb = new ProcessBuilder("javac", "-d", targetLocation, sourceLocation + fileName);
			Process p = pb.start();
			p.waitFor();
		} catch (Exception e) {
			System.out.println("============Compilation Exception==============");
			e.printStackTrace();
		}

	}

	public List<String> runReplayer() {
		List<String> output = new LinkedList<String>();
		try {
			remove();
			String jarPath = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/ChroniclerJ-0.42-SNAPSHOT.jar";
			String logPath = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/chroniclerj-crash.test";
			System.out.println("============ChroniclerJ runReplayer start============");

			ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath, "-replay", logPath, "target/replay/");
			pb.redirectErrorStream(true);
			Process p = pb.start();

			InputStream input = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(reader);

			String line = null;

			while ((line = br.readLine()) != null) {
				output.add(line);
			}

			int code = p.waitFor();

		} catch (Exception e) {
			System.out.println("============RunPlayer Exception==============");
			e.printStackTrace();
		}
		return output;
	}

	public void instrument() {
		try {
			String jarPath = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/ChroniclerJ-0.42-SNAPSHOT.jar";
			String testCasePath = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/Solution.class";
			System.out.println("============Chronicler J Instrument start============");
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarPath, "-instrument", testCasePath,
					"target/deploy", "target/replay");
			Process p = pb.start();
			p.waitFor();
		} catch (Exception e) {
			System.out.println("============Instrument Exception===========");
			e.printStackTrace();
		}
	}

	public List<String> generate() {
		List<String> output = new LinkedList<String>();
		try {
			String jarPath = "/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/ChroniclerJ-0.42-SNAPSHOT.jar";
			String testCasePath = "Solution";
			System.out.println("============ChroniclerJ Generate start============");
			ProcessBuilder pb = new ProcessBuilder("java", "-cp",
					"/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/deploy:" + jarPath, testCasePath);

			pb.redirectErrorStream(true);
			Process p = pb.start();

			InputStream input = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(reader);

			String line = null;

			while ((line = br.readLine()) != null) {
				output.add(line);
			}

			p.waitFor();
			move();
		} catch (Exception e) {
			System.out.println("============Generate Exception===========");
			e.printStackTrace();
		}
		return output;
	}

	public void move() {
		try {
			// System.out.println("=======ChroniclerJ move start========");
			File dir = new File("/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/");
			File[] matches = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("chroniclerj-crash-") && name.endsWith(".test");
				}
			});
			// for(File f : matches){
			// System.out.println("==========" + f.getName()+"==========");
			// }

			ProcessBuilder pb = new ProcessBuilder("mv",
					"/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/" + matches[0].getName(),
					"/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/replay/chroniclerj-crash.test");

			// for(String str : pb.command()){
			// System.out.println("===========Command:"+str+"============");
			// }

			Process p = pb.start();
			int errCode = p.waitFor();
			// System.out.println("============return code:" +
			// errCode+"==============");
		} catch (Exception e) {
			System.out.println("============Move Exception===========");
			e.printStackTrace();
		}

	}

	public void remove() {
		try {
			ProcessBuilder pb = new ProcessBuilder("rm",
					"/Users/XI/Documents/chroniclerj/Code/ChroniclerJ/target/Solution.class");
			Process p = pb.start();
			p.waitFor();
		} catch (Exception e) {

		}
	}
}
