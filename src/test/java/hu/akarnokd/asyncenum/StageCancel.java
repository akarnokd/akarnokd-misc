package hu.akarnokd.asyncenum;

import java.util.concurrent.CompletableFuture;

public final class StageCancel {
    private StageCancel() { }
    public static void main(String[] args) throws Exception {
        CompletableFuture<Integer> f = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        });

        CompletableFuture<Integer> g = f.whenComplete((v, e) -> { System.out.println(v); });

        g.cancel(true);

        System.out.println("+" + f.get());
    }
}
