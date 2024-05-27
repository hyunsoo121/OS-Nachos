package nachos.threads;
import nachos.machine.*;
import java.util.LinkedList;

public class Condition2 {
    public Condition2(Lock conditionLock) {
        this.conditionLock = conditionLock;
        waitQueue = new LinkedList<KThread>();
    }

    public void sleep() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        boolean intStatus = Machine.interrupt().disable();

        waitQueue.add(KThread.currentThread());
        System.out.println(KThread.currentThread().getName() + " sleep");
        conditionLock.release();
        KThread.sleep();
        conditionLock.acquire();
        System.out.println(KThread.currentThread().getName() + " wake");

        Machine.interrupt().restore(intStatus);
    }

    public void wake() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        boolean intStatus = Machine.interrupt().disable();

        if (!waitQueue.isEmpty()) {
            KThread thread = waitQueue.removeFirst();
            if (thread != null) {
                System.out.println(thread.getName() + " wake");
                thread.ready();
            }
        }
        Machine.interrupt().restore(intStatus);
    }

    public void wakeAll() {
        Lib.assertTrue(conditionLock.isHeldByCurrentThread());
        boolean intStatus = Machine.interrupt().disable();

        while (!waitQueue.isEmpty()) {
            wake();
        }
        Machine.interrupt().restore(intStatus);
    }

    private Lock conditionLock;
    private LinkedList<KThread> waitQueue;

    private static class InterlockTest {
        private static Lock lock;
        private static Condition2 cv;

        public InterlockTest() {
            lock = new Lock();
            cv = new Condition2(lock);
        }

        public void runTest() {
            KThread ping = new KThread(new Interlocker());
            ping.setName("ping");
            KThread pong = new KThread(new Interlocker());
            pong.setName("pong");
            ping.fork();
            pong.fork();
            ping.join();
        }

        private static class Interlocker implements Runnable {
            public void run() {
                lock.acquire();
                for (int i = 0; i < 10; i++) {
                    System.out.println(KThread.currentThread().getName());
                    cv.wake();   // signal
                    cv.sleep();  // wait
                }
                lock.release();
            }
        }
    }

    public static void selfTest() {
        InterlockTest test = new InterlockTest();
        test.runTest();
    }
}