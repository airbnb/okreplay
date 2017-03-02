package walkman
/**
 * Doing some concurrent execution against walkman trying to flush out why things don't always behave the way I
 * expected them to.
 *
 * Turns out there needs to be some kind of warning for the Content-Length header. If it's not precise,
 * The Apache HttpAsyncClient will continue to wait for the remainder of the content, blocking littleproxy from
 * Serving any other requests, because it has no other content to deliver. Oops!
 */
//@Unroll
//class ConcurrencyTest extends Specification {
//
//  @Shared
//  def tapeRoot = new File(ConcurrencyTest.class.getResource("/walkman/tapes/").toURI())
//
//  /**
//   * http://meteatamel.wordpress.com/2012/03/21/deadlock-detection-in-java/
//   * handy
//   */
//  def countDeadlockedThreads() {
//    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean()
//    long[] threadIds = threadBean.findDeadlockedThreads()
//    int deadlockedThreads = threadIds != null ? threadIds.length : 0
//    println("Number of deadlocked threads: $deadlockedThreads")
//  }
//
//  void "#amount Concurrent accesses to a #tapeMode tape works"() {
//    given:
//    def proxyConfig = Configuration.builder()
//        .tapeRoot(tapeRoot)
//        .defaultMode(tapeMode)
//        .defaultMatchRule(new PostingMatchRule())
//        .build()
//
//    def recorder = new Recorder(proxyConfig)
//    recorder.start("concurrentTape")
//
//    def builder = HttpAsyncClients.custom()
//    builder.setProxy(new HttpHost("127.0.0.1", 5555))
//
//    def httpClient = builder.build()
//
//    httpClient.start()
//    def requestCount = amount
//    println("okHttpClient started, request count ${requestCount}")
//
//    List<HttpPost> requests = []
//
//    print("\t")
//    requestCount.times { num ->
//      def post = new HttpPost("http://httpbin.org/post")
//      post.setEntity(new StringEntity(num.toString(), ContentType.TEXT_PLAIN))
//      print(".")
//      requests.add post
//    }
//    println()
//
//    when:
//    //Map the request body to the result body, they should match
//    def resultsMap = new ConcurrentHashMap<String, String>()
//
//    def latch = new CountDownLatch(requestCount)
//    println("Executing $requestCount requests concurrently")
//    requests.each { request ->
//      httpClient.execute(request, new FutureCallback<HttpResponse>() {
//        @Override
//        void completed(HttpResponse result) {
//          //HTTPBin will return what we post, so we'll just make sure that we save
//          // the request's body and the response body together for this result
//          // that way we can ensure that we're not clobbering stuff, since each request will have a different
//          // request body
//          //Using this to get the "data" attribute out
//          //Doing this first, because it might actually bomb, heh
//          latch.countDown()
//          try {
//            def json = new JsonSlurper().parseText(result.entity.content.text)
//
//            resultsMap.put(request.entity.content.text, json.data)
//          } catch (e) {
//            resultsMap.put(request.entity.content.text, "FAILED: ${e.message}")
//          }
//        }
//
//        @Override
//        void failed(Exception ex) {
//          resultsMap.put(request.entity.content.text, "HTTP_FAILED: ${ex.message}")
//          latch.countDown()
//        }
//
//        @Override
//        void cancelled() {
//          resultsMap.put(request.entity.content.text, "CANCELLED!")
//          latch.countDown()
//        }
//      })
//    }
//
//    then:
//    //This should happen fast
//    println("Awaiting request completion with latch")
//    latch.await(5, TimeUnit.SECONDS)
//
//    while (resultsMap.size() != requestCount) {
//      //Wait until the map lines up with the countdown latch
//      Thread.sleep(10)
//    }
//
//    //We'll verify it just for sanity
//    resultsMap.size() == requestCount
//
//    println("Verifying results map")
//    resultsMap.each { pair ->
//      //This assertion doesn't seem to work?
//      assert (pair.key == pair.value)
//    }
//
//    cleanup:
//    println("counting deadlocked threads")
//    countDeadlockedThreads()
//
//    println("stopping recorder")
//    recorder?.stop()
//    println("Closing httpclient")
//    httpClient?.close()
//
//    where:
//    tapeMode << [READ_ONLY, READ_WRITE]
//    amount << [10, 8]
//  }
//}
