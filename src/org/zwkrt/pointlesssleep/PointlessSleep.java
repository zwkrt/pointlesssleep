package org.zwkrt.pointlesssleep;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class PointlessSleep {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Pointless!");
        System.out.println("type in numbers and I'll wait that many seconds!");

        SleepManager manager = new SleepManager();
        Thread managerThread = new Thread(manager::manage);
        managerThread.start();

        int taskId = 0;
        Scanner intScanner = new Scanner(System.in);
        while (true) {
            int delay = intScanner.nextInt();

            System.out.println(String.format("Task %d waiting %d seconds", ++taskId, delay));
            SleepTask task = new SleepTask(taskId, delay);
            manager.addTask(task);

        }
    }

    public static class SleepManager {

        private char executorThreadName = 'a';

        private final ArrayBlockingQueue<SleepTask> jobQueue = new ArrayBlockingQueue<>(30);
        private final ExecutorService executorService = new ScheduledThreadPoolExecutor(4, runnable -> {
            return new Thread(runnable, String.valueOf(executorThreadName++));  // cheeky way of naming threads 'a', 'b', 'c', ...
        });

        public void addTask(SleepTask task) throws InterruptedException {
            log("> enqueueing task %d", task.id);
            this.jobQueue.put(task);
        }

        public void manage() {
            while (true) {
                try {
                    SleepTask task = jobQueue.take();
                    log("> dequeueing task %d", task.id);
                    this.executorService.submit(task);
                } catch (InterruptedException ignored) { }
            }
        }
    }

    public static class SleepTask implements Runnable {

        private final int id;
        private final int delay;

        public SleepTask(int id, int delay) {
            this.id = id;
            this.delay = delay;
        }

        @Override
        public void run() {
            try {
                log("> running task %d on thread %s", id, Thread.currentThread().getName());
                Thread.sleep(1000 * delay);
                log("Task %d slept %d seconds, freeing thread %s", id , delay, Thread.currentThread().getName());
            } catch (InterruptedException ignored) { }
        }
    }

    private static void log(String stmt, Object... args) {
        System.out.println(String.format(stmt, args));
    }
}
