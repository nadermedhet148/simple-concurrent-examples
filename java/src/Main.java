import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

  public static int numberOfTask = 1_000;

  public static void main(String[] args) {
    seqIncreaseCounter();
    System.out.println("=====================================");
    multiThreadIncreaseCounter();
    System.out.println("=====================================");
    safeMultiThreadIncreaseCounter();
  }

  public static class Counter {

    private int value = 0;

    public void increment() {
      value++;
    }

    public int get() {
      return value;
    }
  }


  public static void seqIncreaseCounter() {
    long startTime = System.currentTimeMillis();
    var counter = new Counter();
    var random = new Random();

    for (int i = 1; i <= numberOfTask; i++) {
      counter.increment();
      try {
        int waitTime = random.nextInt(10); // Random wait time between 0 and 9 ms
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    long endTime = System.currentTimeMillis();

    System.out.println("Final counter value: " + counter.get());
    System.out.println("Total execution time: " + (endTime - startTime) + " ms");

  }


  public static void multiThreadIncreaseCounter() {
    long startTime = System.currentTimeMillis();
    var counter = new Counter();
    var random = new Random();
    var executor = Executors.newFixedThreadPool(10);

    for (int i = 1; i <= numberOfTask * 50; i++) {
      executor.submit(
          () -> {
            counter.increment();
            try {
              int waitTime = random.nextInt(10); // Random wait time between 0 and 9 ms
              Thread.sleep(waitTime);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
      );
    }

    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    long endTime = System.currentTimeMillis();

    System.out.println("Final counter value: " + counter.get());
    System.out.println("Total execution time: " + (endTime - startTime) + " ms");

  }

  public static void safeMultiThreadIncreaseCounter() {
    long startTime = System.currentTimeMillis();
    var counter = new Counter();
    var random = new Random();
    var executor = Executors.newFixedThreadPool(10);

    for (int i = 1; i <= numberOfTask * 50; i++) {
      executor.submit(
          () -> {
            synchronized (counter) {
              counter.increment();
            }
            try {
              int waitTime = random.nextInt(10); // Random wait time between 0 and 9 ms
              Thread.sleep(waitTime);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          }
      );
    }

    executor.shutdown();
    try {
      executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    long endTime = System.currentTimeMillis();

    System.out.println("Final counter value: " + counter.get());
    System.out.println("Total execution time: " + (endTime - startTime) + " ms");

  }




}