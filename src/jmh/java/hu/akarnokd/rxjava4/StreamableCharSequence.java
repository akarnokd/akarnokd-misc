package hu.akarnokd.rxjava4;

import java.util.concurrent.CompletionStage;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.StreamerCancellation;
import io.reactivex.rxjava4.operators.*;

public record StreamableCharSequence(CharSequence string) implements Streamable<Integer> {

    @Override
    public @NonNull Streamer<@NonNull Integer> stream(@NonNull StreamerCancellation cancellation) {
        return new CharSequenceStreamer(string);
    }

    static final class CharSequenceStreamer
    implements Streamer<Integer>, IndexableSource<Integer>, EnumerableSource<Integer> {

        final CharSequence string;

        int index;

        CharSequenceStreamer(CharSequence string) {
            this.string = string;
            this.index = -1;
        }

        @Override
        public @NonNull CompletionStage<Boolean> next() {
            if (++index >= string.length()) {
                return NEXT_FALSE;
            }
            return NEXT_TRUE;
        }

        @Override
        public @NonNull Integer current() {
            return (int)string.charAt(index);
        }

        @Override
        public @NonNull CompletionStage<Void> finish() {
            return FINISHED;
        }

        @Override
        public @NonNull Integer elementAt(long index) throws Throwable {
            return (int)string.charAt((int)index);
        }

        @Override
        public long limit() {
            return string.length();
        }

        @Override
        public boolean nextSync() throws Throwable {
            if (++index >= string.length()) {
                return false;
            }
            return true;
        }
    }
}
