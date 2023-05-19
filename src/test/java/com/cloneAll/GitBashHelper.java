package com.cloneAll;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GitBashHelper {

    public static void cloneTheProject(String sshLink, String windowsDirectory, String linuxDirectory) throws IOException {

        System.out.println(sshLink);
        Process process = getProcess(sshLink, windowsDirectory, linuxDirectory);

        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        Future<?> future = executorService.submit(new StreamGobbler(process.getInputStream(), System.out::println));

        int exitCode = 0;

        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        assertDoesNotThrow(() -> future.get(10, TimeUnit.SECONDS));

        System.out.println("git clone exit code :" + exitCode);
        assertEquals(0, exitCode);
    }

    private static Process getProcess(String sshLink, String windowsDirectory, String linuxDirectory) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();

        if (isWindows) {
//            builder.command("cmd.exe", "/c", "dir");
            builder.command("cmd.exe", "/c", "git clone " + sshLink);
            builder.directory(new File(windowsDirectory));
        } else {
//            builder.command("sh", "-c", "ls");
            builder.command("sh", "-c", "git clone " + sshLink);
            builder.directory(new File(linuxDirectory));
        }

        return builder.start();
    }

    private static class StreamGobbler implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
        }
    }
}
