
import java.util.ArrayList;
import java.util.List;

class OddEvenThreads {

    private static class NumberPrinter {
        private final Object lock = new Object();
        private final List<Integer> numbers = new ArrayList<>();

        private int current = 1;
        private final int max;

        NumberPrinter(int max) {
            this.max = max;
        }

        void printNumbers(boolean oddThread) {
            while (true) {
                synchronized (lock) {

                    // Wait until it is our turn.
                    while (current <= max
                            && (current % 2 == 1) != oddThread) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            lock.notifyAll();
                            return;
                        }
                    }

                    // Stop when every number has been processed.
                    if (current > max) {
                        lock.notifyAll();
                        return;
                    }

                    // Record the number in the shared list.
                    numbers.add(current);

                    System.out.println(
                        Thread.currentThread().getName()
                        + " -> " + current
                    );

                    current++;

                    // Give the other thread a chance to proceed.
                    lock.notifyAll();
                }
            }
        }

        List<Integer> getNumbers() {
            synchronized (lock) {
                return new ArrayList<>(numbers);
            }
        }
    }

    public static void main(String[] args)
            throws InterruptedException {

        NumberPrinter printer = new NumberPrinter(10);

        Thread odd = new Thread(
            () -> printer.printNumbers(true),
            "Odd"
        );

        Thread even = new Thread(
            () -> printer.printNumbers(false),
            "Even"
        );

        odd.start();
        even.start();

        odd.join();
        even.join();

        System.out.println("Final list: " + printer.getNumbers());
    }
}
