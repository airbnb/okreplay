grails.servlet.version = '2.5'
grails.project.work.dir = 'target'
grails.project.class.dir = 'target/classes'
grails.project.test.class.dir = 'target/test-classes'
grails.project.test.reports.dir = 'target/test-reports'
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

    inherits 'global'
	log 'error'
    checksums true

    repositories {
        inherits true

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
		mavenRepo 'http://oss.sonatype.org/content/groups/public/'
    }

    dependencies {
		test 'co.freeside:betamax:1.1-SNAPSHOT'
    }

    plugins {
        build ":tomcat:$grailsVersion"
		compile ':rest:0.7'
		test ':spock:0.6'
    }

}
