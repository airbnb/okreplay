<!doctype html>
<html>
	<head>
		<g:if test="${!request.xhr}"><meta name="layout" content="bootstrap"></g:if>
		<title>Twitter Search Results</title>
	</head>

	<body>

		<div class="page-header">
			<h1>Twitter Search Results <small><em>${q}</em></small></h1>
		</div>

		<div class="row">
			<div class="span10">
				<g:include action="tweets" params="[q: q]"/>
			</div>
			<div class="span4">
				<g:include action="clients" params="[q: q]"/>
			</div>
		</div>

	</body>
</html>