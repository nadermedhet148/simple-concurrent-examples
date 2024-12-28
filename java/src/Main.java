import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class Main {

    public static int numberOfTask = 1_000;

    public static void main(String[] args) {
//    seqIncreaseCounter();
//    System.out.println("=====================================");
//    multiThreadIncreaseCounter();
//    System.out.println("=====================================");
    safeMultiThreadIncreaseCounter();
//
////        deadlockExample();
////        deadlockExampleV2();
//
//        concurrentDs();
    }

    public static void concurrentDs() {
        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            final int index = i;
            executor.submit(() -> {
                map.put("key" + index, index);
                System.out.println("Added key" + index + " with value " + index);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Final map size: " + map.size());

    }


    public static void deadlockExampleV2() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();

        Thread thread1 = new Thread(() -> {
            try {
                if (lock1.tryLock(50, TimeUnit.MILLISECONDS)) {
                    System.out.println("Thread 1: Holding lock 1...");
                    try {
                        Thread.sleep(10);
                        if (lock2.tryLock(50, TimeUnit.MILLISECONDS)) {
                            try {
                                System.out.println("Thread 1: Holding lock 1 & 2...");
                            } finally {
                                lock2.unlock();
                            }
                        } else {
                            System.out.println("Thread 1: Could not acquire lock 2");
                        }
                    } finally {
                        lock1.unlock();
                    }
                } else {
                    System.out.println("Thread 1: Could not acquire lock 1");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread thread2 = new Thread(() -> {
            try {
                if (lock2.tryLock(50, TimeUnit.MILLISECONDS)) {
                    System.out.println("Thread 2: Holding lock 2...");
                    try {
                        Thread.sleep(10);
                        if (lock1.tryLock(50, TimeUnit.MILLISECONDS)) {
                            try {
                                System.out.println("Thread 2: Holding lock 1 & 2...");
                            } finally {
                                lock1.unlock();
                            }
                        } else {
                            System.out.println("Thread 2: Could not acquire lock 1");
                        }
                    } finally {
                        lock2.unlock();
                    }
                } else {
                    System.out.println("Thread 2: Could not acquire lock 2");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void deadlockExample() {
        final Object lock1 = new Object();
        final Object lock2 = new Object();

        Thread thread1 = new Thread(() -> {
            synchronized (lock1) {
                System.out.println("Thread 1: Holding lock 1...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread 1: Waiting for lock 2...");
                synchronized (lock2) {
                    System.out.println("Thread 1: Holding lock 1 & 2...");
                }
            }
        });

        Thread thread2 = new Thread(() -> {
            synchronized (lock2) {
                System.out.println("Thread 2: Holding lock 2...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread 2: Waiting for lock 1...");
                synchronized (lock1) {
                    System.out.println("Thread 2: Holding lock 1 & 2...");
                }
            }
        });

        thread1.start();
        thread2.start();
        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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

        for (int i = 1; i <= numberOfTask * 50; i++) {
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
        var executor = Executors.newVirtualThreadPerTaskExecutor();

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