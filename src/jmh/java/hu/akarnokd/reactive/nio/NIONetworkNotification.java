package hu.akarnokd.reactive.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;

/**
 * Example benchmark. Run from command line as
 * <br>
 * gradle jmh -Pjmh='IxPerf'
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class NIONetworkNotification {

    ExecutorService exec;

    ByteBuffer sendBuffer;

    ByteBuffer receiveBuffer;

    CyclicBarrier awaitConnection;
    CyclicBarrier awaitClient;
    CyclicBarrier awaitComplete;

    ServerSocketChannel server;

    SocketChannel client;

    @Setup(Level.Iteration)
    public void setup() throws Exception {
        exec = Executors.newCachedThreadPool();
        awaitConnection = new CyclicBarrier(2);
        awaitClient = new CyclicBarrier(2);
        awaitComplete = new CyclicBarrier(2);

        sendBuffer = ByteBuffer.allocateDirect(1);

        receiveBuffer = ByteBuffer.allocateDirect(1);

        server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress(24554));

        exec.execute(this::serverLoop);

        client = SocketChannel.open(new InetSocketAddress(24554));
        client.configureBlocking(false);

        awaitConnection.await();
    }


    @TearDown(Level.Iteration)
    public void teardown() throws Exception {
        exec.shutdown();

        server.close();
    }

    void serverLoop() {
        try {
            SocketChannel sc = server.accept();

            awaitConnection.await();

            ByteBuffer b = receiveBuffer;

            awaitClient.await();

            for (;;) {
                b.clear();
                if (sc.read(b) < 0) {
                    break;
                }
            }

            awaitComplete.await();
        } catch (Throwable ex) {

        }
    }

    @Benchmark
    public void send() throws Exception {
        awaitClient.await();

        ByteBuffer b = sendBuffer;
        // todo
        for (int i = 0; i < 1_000_000; i++) {
            b.clear();
            b.put((byte)1);

            b.flip();

            while (client.write(b) != 1) {
                ;
            }
        }

        client.close();

        awaitComplete.await();
    }
}
