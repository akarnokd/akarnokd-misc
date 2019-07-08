package hu.akarnokd.rxjava;

import static java.lang.String.format;
import static rx.Observable.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.subjects.*;

/**
 * Date: 3/13/17.
 */
public class TransactionSubjectPlaypen {
  enum ExceptionType { OBSERVABLE_EXCEPTION, THROW_EXCEPTION}
  private static ExceptionType exceptionType;
  static final int COUNTER_START = 1;
  static final int ATTEMPTS = 5;
  static final int ORIGINAL_DELAY_IN_SECONDS = 10;
  private Subject<TestTransaction, TestTransaction> transactionSubject;

  public TransactionSubjectPlaypen() {
    this.transactionSubject = PublishSubject.create();
  }

  public static void main(String[] args) {
    try {
      exceptionType = ExceptionType.THROW_EXCEPTION;
      final TransactionSubjectPlaypen transactionSubjectPlaypen = new TransactionSubjectPlaypen();
      transactionSubjectPlaypen.subscribe(new TestTransactionObserver());
      transactionSubjectPlaypen.start();
      Thread.sleep(120000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public Subscription subscribe(Observer<TestTransaction> transactionObserver) {
    return transactionSubject.subscribe(transactionObserver);
  }

  public void start() {
    startPeriodicTransactionRetrieval();
  }

  // Emits Transaction

  private void startPeriodicTransactionRetrieval() {
    System.out.println("TransactionSubjectPlaypen.startPeriodicTransactionRetrieval");
    TransactionSubjectPlaypen.exceptionType = ExceptionType.OBSERVABLE_EXCEPTION;
    Observable.fromCallable(() -> pollRemoteForTransactions(1))
      .flatMap(listObservable -> {
       return listObservable;
      }).flatMapIterable(testTransactions -> {
        return testTransactions;
      })
      .retryWhen(errors -> {
          System.out.println("retryWhen");
          return errors.flatMap(error -> {
              System.out.println("retryWhen: " + error);
            return Observable.just(null);
          });
        })
        .repeatWhen(observable -> {
          System.out.println("repeatWhen");
          return observable.concatMap(v -> timer(4, TimeUnit.SECONDS));
        })
      .subscribe(transactionSubject);
  }

  // This is a bit superfluous however it is included to gave a more complete understanding of the problem.
  private Observable<List<TestTransaction>> pollRemoteForTransactions(Integer i) {
    System.out.println("TransactionSubjectPlaypen.pollRemoteForTransactions");
    return remoteServiceClientSimulation(i);
  }

  private Observable<List<TestTransaction>> remoteServiceClientSimulation(Integer i) {
    System.out.println("TransactionSubjectPlaypen.remoteServiceClientSimulation");
    try {
      randomNetworkDelaySimulation();
      return getTransactions(i);
    } catch (InterruptedException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void randomNetworkDelaySimulation() throws InterruptedException {
    System.out.println("TransactionSubjectPlaypen.randomNetworkDelaySimulation");
    final int MAX_DELAY = 8000;
    Thread.sleep(new Random().nextInt(MAX_DELAY));
  }

  private Observable<List<TestTransaction>> getTransactions(Integer requestedOption) {
    System.out.println("TransactionSubjectPlaypen.getTransactions");
    final List<TestTransaction> transactions = new ArrayList<>();
    int ZERO_RETURN_TRANSACTIONS = 0, ONE_RETURN_EXCEPTION = 1;

    int MAX_OPTIONS = 3;
    final int i = (requestedOption != null ? requestedOption : new Random().nextInt(MAX_OPTIONS));
    if (i == ZERO_RETURN_TRANSACTIONS) {

      transactions.add(new TestTransaction("addVendor"));
      transactions.add(new TestTransaction("addPersonAccount"));
      transactions.add(new TestTransaction("addSystemAccount"));
      transactions.add(new TestTransaction("addDeploymentContext"));
      transactions.add(new TestTransaction("addProperty"));

      System.out.println(format("Return %d transaction", transactions.size()));

      return just(transactions);

    } else if (i == ONE_RETURN_EXCEPTION) {

      if (exceptionType == ExceptionType.THROW_EXCEPTION) {
        System.out.println("Return exception");
        throw new RuntimeException();
        //return error(new RuntimeException());
      } else {
        System.out.println(format("Returning %d transaction", transactions.size()));
        return Observable.error(new RuntimeException());
      }
    } else {

      System.out.println(format("Returning %d transaction", transactions.size()));
      return just(transactions);

    }

  }

  public static class TestTransactionObserver implements Observer<TestTransaction> {

    @Override
    public void onCompleted() {
      System.out.println("TestTransactionObserver.onCompleted");
    }

    @Override
    public void onError(Throwable e) {
      System.out.println("TestTransactionObserver.onError");
      System.out.println(e);
      e.printStackTrace();
    }

    @Override
    public void onNext(TestTransaction testTransaction) {
      System.out.println("TestTransactionObserver.onNext");
      System.out.println("Value = " + testTransaction);
    }
  }

  public static class TestTransaction {
    private String name;

    public TestTransaction() {
    }

    public TestTransaction(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "TestTransaction{" +
        "name='" + name + '\'' +
        '}';
    }
  }

}