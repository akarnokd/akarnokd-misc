package hu.akarnokd.rxjava;

import rx.Observable;
import rx.schedulers.Schedulers;

public class ZipRxJava {

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        ZipRxJava z = new ZipRxJava();
        Observable<CartPlanResponse> o1 = Observable.<CartPlanResponse>create(sub -> sub.onNext(createPlanResponse(z))).subscribeOn(Schedulers.io());
        Observable<CartFeatureResponse> o2 = Observable.<CartFeatureResponse>create(sub -> sub.onNext(createFeatureResponse(z))).subscribeOn(Schedulers.io());
        Observable<CartAccessoriesResponse> o3 = Observable.<CartAccessoriesResponse>create(sub -> sub.onNext(createAccessoriesResponse(z))).subscribeOn(Schedulers.io());
        Observable.zip(o1, o2, o3, (p1, p2, p3) -> {
            System.out.println("Inside Transformer $$$$$$$$$$$››››" + Thread.currentThread().getName());
            Response res = z.new Response();
            res.setPlanResponse(p1);
            res.setFeatureResponse(p2);
            res.setAccesoriesResponse(p3);
            return res;
        }).subscribe(r1 -> System.out.println("&&&&&&&&&&&"+ Thread.currentThread().getName() + "*******" +  r1.getPlanResponse().getPlanId() + " " + r1.getFeatureResponse().getFeatureId() + " " +
            r1.getAccesoriesResponse().getAccessoryId()), e1 -> System.out.println("Error"));
        System.out.println("Main Method ********** " + Thread.currentThread().getName());
        sleep();
    }


    private static CartPlanResponse createPlanResponse(ZipRxJava z) {
        System.out.println("Plan ********** " + Thread.currentThread().getName());
        CartPlanResponse res = z.new CartPlanResponse();
        res.setPlanId("123");
        System.out.println("Before Return Plan ********** " + Thread.currentThread().getName());
        return res;
    }

    private static CartFeatureResponse createFeatureResponse(ZipRxJava z) {
        System.out.println("Feature ********** " + Thread.currentThread().getName());
        //sleep();
//        int y =0;
//        for (int i =0 ; i <100000000; i++) {
//            y +=i;
//        }
        CartFeatureResponse res = z.new CartFeatureResponse();
        res.setFeatureId("345");
        System.out.println("Before Return Feature ********** " + Thread.currentThread().getName());
        return res;
    }

    private static CartAccessoriesResponse createAccessoriesResponse(ZipRxJava z) {
        System.out.println("Accessories ********** " + Thread.currentThread().getName());
        CartAccessoriesResponse res = z.new CartAccessoriesResponse();
        res.setAccessoryId("567");
        System.out.println("Before Return Accessories ********** " + Thread.currentThread().getName());
        return res;
    }

    private static void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private class CartPlanResponse {
        String planId;

        public String getPlanId() {
            return planId;
        }

        public void setPlanId(String planId) {
            this.planId = planId;
        }
    }

    private class CartFeatureResponse {
        private String featureId;

        public String getFeatureId() {
            return featureId;
        }

        public void setFeatureId(String featureId) {
            this.featureId = featureId;
        }
    }

    private class CartAccessoriesResponse {
        private String accessoryId;

        public String getAccessoryId() {
            return accessoryId;
        }

        public void setAccessoryId(String accessoryId) {
            this.accessoryId = accessoryId;
        }
    }

    private class Response {
        private CartPlanResponse planResponse;
        private CartFeatureResponse featureResponse;
        private CartAccessoriesResponse accesoriesResponse;
        public CartPlanResponse getPlanResponse() {
            return planResponse;
        }
        public void setPlanResponse(CartPlanResponse planResponse) {
            this.planResponse = planResponse;
        }
        public CartFeatureResponse getFeatureResponse() {
            return featureResponse;
        }
        public void setFeatureResponse(CartFeatureResponse featureResponse) {
            this.featureResponse = featureResponse;
        }
        public CartAccessoriesResponse getAccesoriesResponse() {
            return accesoriesResponse;
        }
        public void setAccesoriesResponse(CartAccessoriesResponse accesoriesResponse) {
            this.accesoriesResponse = accesoriesResponse;
        }
    }
}