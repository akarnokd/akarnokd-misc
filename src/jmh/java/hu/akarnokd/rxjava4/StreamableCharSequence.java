package hu.akarnokd.rxjava4;

import java.util.concurrent.CompletionStage;

import io.reactivex.rxjava4.annotations.NonNull;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.disposables.StreamerCancellation;

public record StreamableCharSequence(CharSequence string) implements Streamable<Integer> {

    @Override
    public @NonNull Streamer<@NonNull Integer> stream(@NonNull StreamerCancellation cancellation) {
        return new CharSequenceStreamer(string);
    }

    static final class CharSequenceStreamer implements Streamer<Integer> {

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
        
    }
}
