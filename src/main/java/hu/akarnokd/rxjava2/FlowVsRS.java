package hu.akarnokd.rxjava2;

public class FlowVsRS {

    interface FlowPublisher {

    }

    interface RSPublisher extends FlowPublisher {

    }

    public static void flowAPI(FlowPublisher src) {

    }

    public static void rsAPI(RSPublisher src) {

    }

    public static void main(String[] args) {
        flowAPI(new FlowPublisher() { });
    }
}
