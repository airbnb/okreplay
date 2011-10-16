<!doctype html>
<html>
	<head>
		<meta name="layout" content="bootstrap"/>
		<title>Welcome to Grails</title>
	</head>

	<body>
		<div class="page-header">
			<h1>&beta;etamax <small>Example pages</small></h1>
		</div>

		<div class="row">
			<div class="span10">
				<h2>Welcome to &beta;etamax</h2>

				<p>This is a collection of example pages used for demonstrating the capabilities of &beta;etamax.</p>

				<h3>Available Pages:</h3>
				<ul>
					<g:each var="c" in="${grailsApplication.controllerClasses.sort { it.fullName } }">
						<li class="controller"><g:link controller="${c.logicalPropertyName}">${c.logicalPropertyName}</g:link></li>
					</g:each>
				</ul>
			</div>

			<div class="span4">
				<h3>Application Status</h3>
				<ul>
					<li>App version: <g:meta name="app.version"/></li>
					<li>Grails version: <g:meta name="app.grails.version"/></li>
					<li>Groovy version: ${org.codehaus.groovy.runtime.InvokerHelper.getVersion()}</li>
					<li>JVM version: ${System.getProperty('java.version')}</li>
					<li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
					<li>Domains: ${grailsApplication.domainClasses.size()}</li>
					<li>Services: ${grailsApplication.serviceClasses.size()}</li>
					<li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
				</ul>

				<h3>Installed Plugins</h3>
				<ul>
					<g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
						<li>${plugin.name} - ${plugin.version}</li>
					</g:each>
				</ul>
			</div>
		</div>
	</body>
</html>
