<!doctype html>
<html lang="en">
	<head>
		<meta charset="utf-8">
		<title><g:layoutTitle/></title>
		<meta name="description" content="">
		<meta name="author" content="">

		<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
		<!--[if lt IE 9]>
      	<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    	<![endif]-->

		<!-- Le styles -->
		<link rel="stylesheet/less" href="${resource(dir: 'css', file: 'bootstrap.less')}">
		<script src="${resource(dir: 'js', file: 'less.js')}"></script>
		<style type="text/css">
			/* Override some defaults */
			html, body {
				background-color: #eee;
			}

			body {
				padding-top: 40px; /* 40px to make the container go all the way to the bottom of the topbar */
			}

			.container > footer p {
				text-align: center; /* center align it with the container */
			}

			.container {
				width: 820px; /* downsize our container to make the content feel a bit tighter and more cohesive. NOTE: this removes two full columns from the grid, meaning you only go to 14 columns and not 16. */
			}

			/* The white background content wrapper */
			.content {
				background-color: #fff;
				padding: 20px;
				margin: 0 -20px; /* negative indent the amount of the padding to maintain the grid system */
				-webkit-border-radius: 0 0 6px 6px;
				-moz-border-radius: 0 0 6px 6px;
				border-radius: 0 0 6px 6px;
				-webkit-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
				-moz-box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
				box-shadow: 0 1px 2px rgba(0, 0, 0, .15);
			}

			/* Page header tweaks */
			.page-header {
				background-color: #f5f5f5;
				padding: 20px 20px 10px;
				margin: -20px -20px 20px;
			}

			/* Styles you shouldn't keep as they are for displaying this base example only */
			.content .span10,
			.content .span4 {
				min-height: 500px;
			}

			/* Give a quick and non-cross-browser friendly divider */
			.content .span4 {
				margin-left: 0;
				padding-left: 19px;
				border-left: 1px solid #eee;
			}

			.topbar .btn {
				border: 0;
			}
		</style>

		<!-- Le fav and touch icons -->
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="72x72" href="${resource(dir: 'images', file: 'apple-touch-icon-72x72.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-114x114.png')}">

		<g:layoutHead/>
	</head>

	<body>

		<div class="topbar">
			<div class="fill">
				<div class="container">
					<a class="brand" href="${resource(file: '/')}">&beta;etamax</a>
				</div>
			</div>
		</div>

		<div class="container">

			<div class="content">
				<g:layoutBody/>
			</div>

			<footer>
				<p>&copy; Company 2011</p>
			</footer>

		</div>

	</body>
</html>
