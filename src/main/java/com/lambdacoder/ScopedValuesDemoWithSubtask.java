package com.lambdacoder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.IntStream;

public class ScopedValuesDemoWithSubtask {
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    final static ScopedValue<Integer> TASK_ID = ScopedValue.newInstance();

    public static class Task {
        public void execute() {
            Integer id = TASK_ID.get();
            if (id == 100) {
                EXECUTOR.submit(() -> new SubTask().execute());
            } else if (id == 101) {
                EXECUTOR.submit(() ->
                        ScopedValue.runWhere(TASK_ID, 201, () -> new SubTask().execute()));
            } else if (id == 102) {
                ScopedValue.runWhere(TASK_ID, 202,
                        () -> EXECUTOR.submit(() -> new SubTask().execute()));
            } else if (id == 103) {
                try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                    scope.fork(() -> new SubTask().execute());
                    scope.fork(() -> new SubTask().execute());

                    scope.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println(STR."Running Task in virtual thread with id: \{id }");
        }
    }

    public static class SubTask {
        public Integer execute() {
            Integer id = TASK_ID.orElse(999999);
            System.out.println(STR."Running SubTask in virtual thread with id: \{id }");
            return 1;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        IntStream.range(100, 105).forEach(taskId -> EXECUTOR.submit(() -> {
            ScopedValue.runWhere(TASK_ID, taskId, () -> new Task().execute());
        }));

        Thread.sleep(100L);
        EXECUTOR.close();
    }
}
