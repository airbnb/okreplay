$(document).ready(function() {

	// Google code prettify
	$('pre').addClass('prettyprint');
	prettyPrint();

	// wrap each tab in a .tab-pane
	$('#maven, #gradle, #grails, #junit, #spock').each(function() {
		$(this).nextUntil('h1, h2, h3').andSelf().wrapAll('<div class="tab-pane"></div>');
	});

	// move ids from tab headings to containing .tab-pane
	$('.tab-pane h3').each(function() {
		var id = $(this).attr('id');
		$(this).removeAttr('id').parent().attr('id', id);
	});

	// wrap all contiguous .tab-pane elements in a .tab-content
	$(':not(.tab-pane) + .tab-pane').each(function() {
		$(this).nextUntil(':not(.tab-pane)').andSelf().wrapAll('<div class="tab-content"></div>');
	});

	// create nav pills for each tab container
	$('.tab-content').each(function() {
		var tabs = $('<ul class="nav nav-pills"></ul>');
		$(this).children().each(function() {
			var tab = $('<li></li>');
			tab.append($('<a></a>', {
				href: '#' + $(this).attr('id'),
				text: $(this).find('h3').text(),
				click: function(e) {
					e.preventDefault();
					$(this).tab('show');
				}
			}).data('toggle', 'tab'));
			tabs.append(tab);
		});
		tabs.insertBefore(this)
			.find('li:first-child a').tab('show');
	});

	// FOUC prevention
	$(window).load(function() {
		// affix nav
		$('#doc-index nav').affix({
			offset: $('header.jumbotron').outerHeight(true) + $('.social-bar').outerHeight(true) - 15
		});
		$('body').addClass('ready');
		// force mobile URL bar out of view if we're at the top of the page
		if ($(window).scrollTop() == 0) {
			window.scrollTo(0, 1);
		}
	});

});
