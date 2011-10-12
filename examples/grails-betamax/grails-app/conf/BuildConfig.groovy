grails.project.work.dir = "target"
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.dependency.resolution = {
	inherits("global") {
		excludes "slf4j-api", "slf4j-log4j12", "jul-to-slf4j", "jcl-over-slf4j"
	}
	log "warn"
	repositories {
		grailsPlugins()
		grailsHome()
		grailsCentral()
		mavenLocal()
		mavenCentral()
		mavenRepo "http://oss.sonatype.org/content/groups/public/"
	}
	dependencies {
		// workaround for https://github.com/robfletcher/betamax/issues/39
		def slf4jVersion = "1.6.2"
		compile "org.slf4j:slf4j-api:$slf4jVersion"
		runtime "org.slf4j:slf4j-log4j12:$slf4jVersion", "org.slf4j:jul-to-slf4j:$slf4jVersion", "org.slf4j:jcl-over-slf4j:$slf4jVersion"

		compile("org.codehaus.groovy.modules.http-builder:http-builder:0.5.1") {
			excludes "groovy", "xml-apis"
		}
		test "com.github.robfletcher:betamax:1.1-SNAPSHOT"
	}
	plugins {
		compile ":hibernate:$grailsVersion"
		compile ":jquery:1.6.1.1"
		compile ":resources:1.0.2"
		build ":tomcat:$grailsVersion"
		test ":spock:0.5-groovy-1.7"
	}
}
