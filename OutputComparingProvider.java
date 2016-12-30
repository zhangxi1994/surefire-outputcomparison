package net.jonbell.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.maven.surefire.junitcore.JUnitCoreProvider;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.ScanResult;

public class OutputComparingProvider extends AbstractProvider {
	private AbstractProvider[] providers;

	private final ProviderParameters providerParameters;
	//private JUnitCoreProvider baseProvider;

	public OutputComparingProvider(ProviderParameters booterParameters) {
		//baseProvider = new JUnitCoreProvider(booterParameters);
		providerParameters = booterParameters;
		String cps = booterParameters.getProviderProperties().get("cp");
		String[] cp = cps.split(",");
		providers = new AbstractProvider[cp.length];
		for (int i = 0; i < cp.length; i++) {
			cp[i] = cp[i].trim();
			// providers[i] = new
		}
//		System.out.println("OCP");
//		System.out.println(Arrays.toString(cp));
	}

	public Iterable<Class<?>> getSuites() {
//		Iterable<Class<?>> ret = baseProvider.getSuites();
//		System.out.println("Suites: " + ret);
		return null;
	}

	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		System.out.println("===========invoke start==========");
		
		String cps = providerParameters.getProviderProperties().get("cp");
		String[] cp = cps.split(",");
		for(String str : cp){
			System.out.println(str);
		}
		
		
		System.out.println("===========invoke end==========");
		return null;
	}
}
