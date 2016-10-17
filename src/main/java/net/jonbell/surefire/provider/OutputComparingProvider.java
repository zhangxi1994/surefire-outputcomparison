package net.jonbell.surefire.provider;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.TestSetFailedException;

public class OutputComparingProvider extends AbstractProvider {
	private AbstractProvider[] providers;
	
	public OutputComparingProvider(ProviderParameters booterParameters)
	{
		String cps = booterParameters.getProviderProperties().get("cp");
		String[] cp = cps.split(",");
		providers = new AbstractProvider[cp.length];
		for(int i = 0; i< cp.length; i++)
		{
			cp[i] = cp[i].trim();
			providers[i] = new 
		}
		System.out.println(Arrays.toString(cp));
	}

	public Iterable<Class<?>> getSuites() {
		System.err.println("Get suites");
		return null;
	}

	public RunResult invoke(Object forkTestSet)
			throws TestSetFailedException, ReporterException, InvocationTargetException {
		System.err.println("Invoke");
		return null;
	}
}
