package hu.akarnokd.rxjava3;
import java.util.*;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.truth.Truth;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ZipSharedTest {


  @Test
  public void testConcurrentZip() {
    int elementCount = 10;
    int sourceCopies = 100;
    Flowable<Integer> source = Flowable.fromArray(IntStream.range(0, elementCount).boxed().toArray(Integer[]::new));

    io.reactivex.rxjava3.functions.Function<Object[], List<Object>> zipper = objects -> {
      Preconditions.checkArgument(Arrays.stream(objects).allMatch(o -> o.equals(objects[0])), "Elements should all be equal");
      return List.of(objects);
    };

    // A: independent sources, sequential
    {
      Iterable<List<Object>> results = Flowable.zip(Collections.nCopies(sourceCopies, source), zipper).blockingIterable();
      Truth.assertThat(results).hasSize(elementCount);
    }

    // B: multicast sources (c.f. share operator), sequential
    {
      Iterable<List<Object>> results = Flowable.zip(Collections.nCopies(sourceCopies, source.share()), zipper)
          .blockingIterable();
      Truth.assertThat(results).hasSize(elementCount);
    }

    // C: independent sources, parallel
    {
      Iterable<List<Object>> results = Flowable.zip(Collections.nCopies(sourceCopies, source.subscribeOn(Schedulers.io())), zipper)
          .blockingIterable();
      Truth.assertThat(results).hasSize(elementCount);
    }

    // D: multicast sources (c.f. share operator), parallel
    {
      Iterable<List<Object>> results = Flowable.zip(
              Collections.nCopies(sourceCopies, source.share().subscribeOn(Schedulers.io())), zipper, false, 1)
          .blockingIterable();
      Truth.assertThat(results).hasSize(elementCount);
    }
  }
}
