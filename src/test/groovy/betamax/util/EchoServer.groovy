package betamax.util

class EchoServer {

    private Thread t

    String start() {
        def host = InetAddress.localHost.hostAddress
        def port = 5000
        def serverProcess =  {
            def server = new ServerSocket(port)
            try {
                server.accept() { socket ->
                    println "got request"
                    socket.withStreams { input, output ->
                        output.withWriter { writer ->
                            writer << "HTTP/1.1 200 OK\n"
                            writer << "Content-Type: text/plain\n\n"
                            while (input.available()) {
                                writer << (char) input.read()
                            }
                        }
                    }
                }
            } catch(InterruptedException e) {
                println "interrupted"
            } finally {
                server.close()
            }
        }
        t = new Thread(serverProcess)
        t.setPriority(Thread.MIN_PRIORITY)
        t.start()

        "http://$host:$port/"
    }

    void stop() {
        t.interrupt()
    }

}
