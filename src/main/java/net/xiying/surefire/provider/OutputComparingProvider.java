package net.xiying.surefire.provider;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

public class OutputComparingProvider extends AbstractProvider {
	private AbstractProvider[] providers;

	private final ProviderParameters providerParameters;
	private String[] classPaths = null;
	private Method chroniclerMainMethod = null;
	private String deployPath = null;
	private String replayPath = null;
	private List<String> failedTests = null;

	public OutputComparingProvider(ProviderParameters booterParameters) {
		providerParameters = booterParameters;
		failedTests = new LinkedList<String>();

		String cp = providerParameters.getProviderProperties().get("cp");
		classPaths = cp.split(",");
		for (int i = 0; i < classPaths.length; i++) {
			classPaths[i] = classPaths[i].trim();
		}

		deployPath = providerParameters.getProviderProperties().get("deploy");
		replayPath = providerParameters.getProviderProperties().get("replay");

		try {
			Class chronicler = providerParameters.getTestClassLoader()
					.loadClass("edu.columbia.cs.psl.chroniclerj.Main");
			chroniclerMainMethod = chronicler.getMethod("main", String[].class);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Iterable<Class<?>> getSuites() {
		return null;
	}

	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		// System.out.println("===========Invoke function start==========");

		for (String path : classPaths) {

			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();
			List<String> testCases = new LinkedList<String>();

			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					testCases.add(classPaths[0] + listOfFiles[i].getName());
				}
			}

			// for each test case run it twice
			for (String test : testCases) {
				System.out.println("");
				System.out.println("");
				System.out.println("=======================================================");
				instrument(test);
				File deployFolder = new File(deployPath);
				File[] listOfFilesDeploy = deployFolder.listFiles();

				if (listOfFilesDeploy[0].isFile()) {

					List<String> output1 = generate(listOfFilesDeploy[0].getName().split("\\.")[0]);
					List<String> output2 = runReplayerProcess();

					System.out.println("++++++++++Output1++++++++++");
					print(output1);
					System.out.println("++++++++++Output2++++++++++");
					print(output2);
					
					if(!compare(output1, output2)){
						failedTests.add(test);
					}
				}
				clear(deployPath);
				clear(replayPath);
			}
			System.out.println("=======================================================");
			System.out.println("T E S T   E N D");
			if(failedTests.size()==0) System.out.println("All tests passed.");
			else{
				System.out.println("Failed Tests:");
				for(String testsFailed : failedTests){
					System.out.println(testsFailed);
				}
			}
			System.out.println("=======================================================");
		}
		// System.out.println("===========Invoke function end===========");
		return null;
	}

	public void instrument(String test) {
		// System.out.println("===========Instrument function start==========");
		try {
			//System.out.println("Insturment: " + test);
			String[] args = new String[] { "-instrument", test, deployPath, replayPath };
			chroniclerMainMethod.invoke(null, (Object) args);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println("===========Instrument function end==========");
	}

	public List<String> generateTestsTemp(String testCase) {
		File folder = new File(deployPath + "/"); // class path for test cases
		File[] listOfFiles = folder.listFiles();
		List<String> res = generate(testCase);
		return res;

		// for (int i = 0; i < listOfFiles.length; i++) {
		// if (listOfFiles[i].isFile()) {
		// try {
		// URL url = folder.toURI().toURL();
		// URL[] urls = new URL[] { url };
		//
		// System.out.println(url);
		// ClassLoader cl = new URLClassLoader(urls,null);
		//
		// Class cls = cl.loadClass("RandomITCase");
		// String[] args = new String[] {};
		// cls.getMethod("main", String[].class).invoke(null,(Object) args);
		//
		// } catch (ClassNotFoundException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (SecurityException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (InvocationTargetException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (NoSuchMethodException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// }
	}

	public List<String> runReplayerProcess() {
		List<String> output = new LinkedList<String>();
		try {
			// System.out.println("============RunReplayer start============");

			File chroniclerJar = new File("target/");
			File[] matches_jar = chroniclerJar.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("ChroniclerJ-") && name.endsWith("-SNAPSHOT.jar");
				}
			});
			if (matches_jar.length == 0)
				return output;

			File log = new File(".");
			File[] matches_log = log.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("chroniclerj-crash-") && name.endsWith(".test");
				}
			});
			if (matches_log.length == 0)
				return output;

			ProcessBuilder pb = new ProcessBuilder("java", "-jar", "target/" + matches_jar[0].getName(), "-replay",
					matches_log[0].getName(), "target/replay/");

			pb.redirectErrorStream(true);

			Process p = pb.start();
			InputStream input = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(reader);
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith("Available logs"))
					continue;
				if (line.startsWith("java.") && line.endsWith("Exception"))
					break;
				output.add(line);
			}

			int code = p.waitFor();
			matches_log[0].delete();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}

	public List<String> runReplayer() {

		// System.out.println("===========Run replayer start==========");
		List<String> list = new LinkedList<String>();
		try {

			File dir = new File(".");
			File[] matches = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith("chroniclerj-crash-") && name.endsWith(".test");
				}
			});
			if (matches.length == 0)
				return list;

			String[] args = new String[] { "-replay", matches[0].getName(), replayPath + "/" };

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			PrintStream old = System.out;
			System.setOut(ps);

			chroniclerMainMethod.invoke(null, (Object) args);

			System.out.flush();
			System.setOut(old);
			matches[0].delete();
			for (String line : baos.toString().split("\\n")) {
				list.add(line);
			}
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("===========Run replayer end==========");
		return list;
	}

	public List<String> generate(String testCase) {
		List<String> output = new LinkedList<String>();
		try {
			String jarPath = "target/ChroniclerJ-0.42-SNAPSHOT.jar";
			String testCasePath = testCase;
			// System.out.println("============Generate start============");
			ProcessBuilder pb = new ProcessBuilder("java", "-cp", deployPath + ":" + jarPath, testCasePath);

			pb.redirectErrorStream(true);
			Process p = pb.start();

			InputStream input = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(input);
			BufferedReader br = new BufferedReader(reader);

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.equals("ChroniclerJ caught an exception"))
					break;
				output.add(line);
			}
			p.waitFor();
			// System.out.println("============Generate end============");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return output;
	}
	
	public boolean compare(List<String> output1, List<String> output2){
		if(output1.size()!=output2.size()) return false;
		Iterator<String> iter1 = output1.iterator();
		Iterator<String> iter2 = output2.iterator();
		
		while(iter1.hasNext()&&iter2.hasNext()){
			if(!iter1.next().equals(iter2.next())){
				return false;
			}
		}
		
		return true;
	}

	public void clear(String path) {
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (File file : files) {
			// System.out.println("delete : " + file.getName());
			if (!file.delete()) {
				System.out.println("Failed to delete " + file);
			}
		}
	}

	public void print(List<String> list) {
		Iterator<String> iter = list.iterator();
		while (iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
}
