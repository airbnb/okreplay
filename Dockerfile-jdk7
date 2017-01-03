FROM openjdk:7u111-jdk
ADD betamax.pem /
RUN keytool -importcert -keystore $JAVA_HOME/jre/lib/security/cacerts -file betamax.pem -storepass changeit -noprompt
RUN sed -i -e 's/securerandom.source=file:\/dev\/random/securerandom.source=file:\/dev\/urandom/' $JAVA_HOME/jre/lib/security/java.security
