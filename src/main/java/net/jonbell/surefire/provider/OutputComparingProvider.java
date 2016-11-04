package net.jonbell.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.maven.surefire.junitcore.JUnitCoreProvider;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

public class OutputComparingProvider extends AbstractProvider {
	private AbstractProvider[] providers;
	private JUnitCoreProvider baseProvider;
	public OutputComparingProvider(ProviderParameters booterParameters)
	{
		baseProvider = new JUnitCoreProvider(booterParameters);
		String cps = booterParameters.getProviderProperties().get("cp");
		String[] cp = cps.split(",");
		providers = new AbstractProvider[cp.length];
		for(int i = 0; i< cp.length; i++)
		{
			cp[i] = cp[i].trim();
//			providers[i] = new 
		}
		System.out.println("OCP");
		System.out.println(Arrays.toString(cp));
	}

	public Iterable<Class<?>> getSuites() {
		Iterable<Class<?>> ret = baseProvider.getSuites();
		System.out.println("Suites: " + ret);
		return ret;
//		return null;
	}

	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		System.out.println("RunResult invoke");
		System.out.println(getSuites());
		System.err.println("Invoke");
		System.out.println(forkTestSet);
		return null;
	}
}
