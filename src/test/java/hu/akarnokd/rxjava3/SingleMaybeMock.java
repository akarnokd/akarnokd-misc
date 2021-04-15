package hu.akarnokd.rxjava3;

import org.junit.Test;

import com.google.gson.*;

import io.reactivex.*;

public class SingleMaybeMock {
    @Test
    public void testGenerateValue() {
        Handler handler = new Handler();

        handler.generateValue(new Request("testVal"))
                .subscribe();
    }

    class Handler {
        MockValueService valueService = new MockValueService();
        MockRepositoryService repositoryService = new MockRepositoryService();

    public Single<Result> generateValue(Request request) {
        return valueService.create(request.getArg())
                .flatMapSingleElement(value -> repositoryService.persist(value))
                .map(obj -> new Result(obj.toJson()))
                .switchIfEmpty(Maybe.fromCallable(() -> new Result(new JsonObject())))
                .toSingle()
                ;
    }
    }

    class MockValueService {
        public Maybe<Value> create(String arg) {
            return Maybe.empty();
        }
    }

    class MockRepositoryService {
        public Single<Value> persist(Value value) {
            return Single.just(value);
        }
    }

    class Result {
        private JsonElement json;

        public Result(JsonElement json) {
            this.json = json;
        }

        public JsonElement getJson() {
            return json;
        }
    }

    class Request {
        private String arg;

        public Request(String arg) {
            this.arg = arg;
        }

        public String getArg() {
            return arg;
        }
    }

    class Value {
        private JsonObject original;

        public Value(JsonObject original) {
            this.original = original;
        }

        public JsonObject toJson() {
            return original;
        }
    }
}
