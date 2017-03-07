package hu.akarnokd.rxjava2;

import java.io.*;
import java.util.concurrent.*;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;

import io.reactivex.*;

public class ObservableFromFuture {
    public static void main(String[] args) {
        ExecutorService myExecutor = Executors.newCachedThreadPool();
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory();

        try {
            HttpRequest httpRequest = httpRequestFactory.buildGetRequest(new GenericUrl("https://api.github.com/users/etiago"));

            Maybe<String> maybeString =
                    Single
                            .fromFuture(httpRequest.executeAsync(myExecutor))
                            .map(httpResponse -> {
                                InputStreamReader inputStreamReader =
                                        new InputStreamReader(httpResponse.getContent());
                                BufferedReader bufferedReader =
                                        new BufferedReader(inputStreamReader);

                                return Observable
                                        .fromIterable(bufferedReader.lines()::iterator)
                                        .reduce(String::concat).map(s -> {
                                            bufferedReader.close();
                                            inputStreamReader.close();
                                            return s;
                                        });
                            })
                            .blockingGet();

            System.out.println(maybeString.blockingGet());
            myExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}