<h2>Tweets about <em>${q}</em></h2>
<ul id="tweets" class="unstyled">
	<g:each in="${tweets}">
		<li>
			<blockquote>
				<p>${it.text}</p>
				<small><a href="http://m.twitter.com/${it.user}" rel="external">@${it.user}</a></small>
			</blockquote>
		</li>
	</g:each>
</ul>