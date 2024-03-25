package com.lambdacoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class ScopedValuesDemo {
    public static class Task {
        final static ScopedValue<Integer> TASK_ID = ScopedValue.newInstance();

        public void execute() {
            System.out.println(STR."Running task in virtual thread with id: \{TASK_ID.get()}");
        }
    }

    public static void main(String[] args) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 5).forEach(taskId -> executor.submit(() -> {
                ScopedValue.runWhere(Task.TASK_ID, taskId, () -> new Task().execute());
            }));
        }
    }
}
