package hu.akarnokd.rxjava2;

import java.io.*;

import org.junit.Test;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

public class LineByLine {

    @Test
    public void test() throws Exception {
        class State {
            DataInputStream inputStream;
            int count;
            int i;
       }

       BufferedReader bin = new BufferedReader(new InputStreamReader(System.in));
       InputStream is = new FileInputStream("files/ospd.txt");

       Flowable.generate(() -> {
           State s = new State();
           s.inputStream = new DataInputStream(is);
           try {
               /*
               if (s.inputStream.readInt() != SOME_CONSTANT) {
                   throw new IllegalArgumentException("illegal file format");
               }

               if (s.inputStream.readInt() != SOME_OTHER_CONSTANT) {
                   throw new IllegalArgumentException("illegal file format");
               }
               */
               s.count = s.inputStream.readInt();
           } catch (IOException ex) {
               s.inputStream.close();
               throw ex;
           }
           return s;
       }, (state, emitter) -> {
           if (state.i < state.count) {
               int v = state.inputStream.readByte();
               System.out.println("Read << " + v);
               emitter.onNext(v);
               state.i++;
           }
           if (state.i >= state.count) {
               emitter.onComplete();
           }
       }, state -> {
           state.inputStream.close();
       })
       .subscribeOn(Schedulers.io())
       .blockingSubscribe(b -> {
           System.out.println(b);
           bin.readLine();
       }, 1);
    }
}
