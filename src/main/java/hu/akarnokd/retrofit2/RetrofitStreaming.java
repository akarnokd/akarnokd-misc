package hu.akarnokd.retrofit2;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.List;

import com.google.common.base.Charsets;
import com.sun.net.httpserver.HttpServer;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import rx.Observable;

public final class RetrofitStreaming {

    private RetrofitStreaming() { }

    public interface Ints {
        @GET("/")
        Observable<List<Integer>> getInts();

        @GET("/")
        Observable<Integer> getIntsStreaming();
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8181), 0);
        server.setExecutor(null);
        server.createContext("/", e -> {

            e.getResponseHeaders().add("Content-type", "application/json");
            e.sendResponseHeaders(200, 0);

            try (OutputStream out = e.getResponseBody();

                PrintWriter pout = new PrintWriter(new OutputStreamWriter(out, Charsets.UTF_8))) {

                pout.println("[");

                for (int i = 0; i < 100; i++) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    if (i != 0) {
                        pout.println(", ");
                    }
                    pout.print("  1");
                    pout.flush();
                }

                pout.println("]");

            }
        });

        server.start();

        try {
//            URL u = new URL("http://localhost:8181/");
//            URLConnection uc = u.openConnection();
//            uc.setDoInput(true);
//            InputStream in = uc.getInputStream();
//            int i = 0;
//            while ((i = in.read()) >= 0) {
//                System.out.print((char)i);
//            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://localhost:8181")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();

            Ints service = retrofit.create(Ints.class);

            service.getInts().toBlocking()
            .subscribe(System.out::println, Throwable::printStackTrace);

        } finally {
            server.stop(1000);
        }
    }
}
