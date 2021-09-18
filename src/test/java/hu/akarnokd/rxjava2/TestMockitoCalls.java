package hu.akarnokd.rxjava2;

    import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.Test;

import io.reactivex.subjects.CompletableSubject;

    public class TestMockitoCalls {

        @Test
        public void test() {
            @SuppressWarnings("unchecked")
            List<Integer> list = mock(List.class);

            CompletableSubject source = CompletableSubject.create();

            source.doOnSubscribe(v -> list.add(1))
            .doOnError(e -> list.remove(1))
            .doOnComplete(() -> list.remove(1))
            .subscribe();

            source.onComplete();

            verify(list).add(1);
            verify(list).remove(1);
            verifyNoMoreInteractions(list);
        }
    }
