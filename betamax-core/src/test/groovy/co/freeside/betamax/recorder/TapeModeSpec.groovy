package co.freeside.betamax.recorder

import co.freeside.betamax.Recorder
import co.freeside.betamax.handler.*
import co.freeside.betamax.message.Request
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.message.BasicRequest
import co.freeside.betamax.util.server.EchoHandler
import spock.lang.*
import static co.freeside.betamax.TapeMode.*
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK

@Unroll
class TapeModeSpec extends Specification {

	@Shared @AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Shared Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()
	HttpHandler handler = new DefaultHandlerChain(recorder)
	Request request = new BasicRequest('GET', endpoint.url)

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	void cleanup() {
		recorder.ejectTape()
	}

	void 'in #mode mode the proxy rejects a request if no recorded interaction exists'() {
		given: 'a read-only tape is inserted'
		recorder.insertTape('read only tape', [mode: mode])

		when: 'a request is made that does not match anything recorded on the tape'
		handler.handle(request)

		then: 'the proxy rejects the request'
		thrown NonWritableTapeException

		where:
		mode << [READ_ONLY, READ_SEQUENTIAL]
		request = new BasicRequest('GET', endpoint.url)
	}

	void 'in #mode mode a new interaction recorded'() {
		given: 'an empty write-only tape is inserted'
		new File(tapeRoot, 'blank_tape_' + mode + '.yaml').delete()
		recorder.insertTape('blank tape ' + mode, [mode: mode])
		def tape = recorder.tape

		when: 'a request is made'
		handler.handle(request)

		then: 'the interaction is recorded'
		tape.size() == old(tape.size()) + 1

		cleanup:
		recorder.ejectTape()

		where:
		mode << [READ_WRITE, WRITE_ONLY, WRITE_SEQUENTIAL]
	}

	void 'in write-only mode the proxy overwrites an existing matching interaction'() {
		given: 'an existing tape file is inserted in write-only mode'
		def tapeFile = new File(tapeRoot, 'write_only_tape.yaml')
		tapeFile.text = """\
!tape
name: write only tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
		recorder.insertTape('write only tape', [mode: WRITE_ONLY])
		def tape = recorder.tape

		when: 'a request is made that matches a request already recorded on the tape'
		handler.handle(request)

		then: 'the previously recorded request is overwritten'
		tape.size() == old(tape.size())
		tape.interactions[-1].response.status == HTTP_OK
		tape.interactions[-1].response.body
	}

	@Issue('https://github.com/robfletcher/betamax/issues/7')
	@Issue('https://github.com/robfletcher/betamax/pull/70')
	void 'in write-sequential mode the proxy records additional interactions'() {
		given: 'an existing tape file is inserted in write-sequential mode'
		def tapeFile = new File(tapeRoot, 'write_sequential_tape.yaml')
		tapeFile.text = """\
!tape
name: write sequential tape
interactions:
- recorded: 2011-08-26T21:46:52.000Z
  request:
    method: GET
    uri: $endpoint.url
    headers: {}
  response:
    status: 202
    headers: {}
    body: Previous response made when endpoint was down.
"""
		recorder.insertTape('write sequential tape', [mode: WRITE_SEQUENTIAL])
		def tape = recorder.tape

		when: 'a request is made that matches a request already recorded on the tape'
		handler.handle(request)

		then: 'the previously recorded request is overwritten'
		tape.size() == old(tape.size()) + 1
		tape.interactions[-1].response.status == HTTP_OK
		tape.interactions[-1].response.body
	}

}
