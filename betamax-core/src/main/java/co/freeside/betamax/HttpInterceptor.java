package co.freeside.betamax;

public interface HttpInterceptor {

	boolean isRunning();

	void start();

	void stop();

	String getHost();

	int getPort();

}