package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

import io.reactivex.Single;

public class Covariance {

    @Test
    public void test() {
        Single<ArrayList<Integer>> list = null;
        
        //m1(list);
        m2(list);
    }
    
    public void m1(Single<List<Integer>> p) {
        
    }

    
    public void m2(Single<? extends List<? extends Integer>> p) {
        
    }
}
