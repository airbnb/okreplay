<h3>Tweets by Client</h3>
<table id="twitter-clients" class="zebra-striped">
	<thead>
		<tr>
			<th scope="col">Client</th>
			<th scope="col">Tweets</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${clients}">
			<tr>
				<td>${it.key}</td>
				<td>${it.value}</td>
			</tr>
		</g:each>
	</tbody>
</table>
