package co.freeside.betamax

interface HttpInterceptor {

	boolean isRunning()

	void start()

	void stop()

	String getHost()

	int getPort()

}