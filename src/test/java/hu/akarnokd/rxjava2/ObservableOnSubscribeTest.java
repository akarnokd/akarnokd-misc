package hu.akarnokd.rxjava2;

    import io.reactivex.*;
    
    public class ObservableOnSubscribeTest {
        public static void main(String[] args) {
            Observable<String> observable = Observable.create(
                    new ObservableOnSubscribe<String>(){
                        @Override
                        public void subscribe(ObservableEmitter<String> sub){
                            sub.onNext("New Datas");
                            sub.onComplete(); 
                        }
                    }
                );
        }
    }
