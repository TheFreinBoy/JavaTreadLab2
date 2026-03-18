
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArrClass {
    private final int dim;
    private final int threadNum;
    private final int[] arr;

    private int globalMin = Integer.MAX_VALUE;
    private int globalMinIndex = -1;

    private final Lock lockerForMin = new ReentrantLock();
    private final Lock lockerForCount = new ReentrantLock();
    private final Condition countCondition = lockerForCount.newCondition();

    private int threadCount = 0;

    public ArrClass(int dim, int threadNum) {
        this.dim = dim;
        this.threadNum = threadNum;
        this.arr = new int[dim];
        initArr();
    }

    private void initArr() {
        Random rnd = new Random();
        for (int i = 0; i < dim; i++) {
            arr[i] = i;
        }

        int randomIndex = rnd.nextInt(dim);
        arr[randomIndex] = -99999;

        System.out.println("[Генерація] Від'ємне число (-99999) розміщено під індексом: " + randomIndex);
    }

    public int[] findPartMin(int startIndex, int finishIndex) {
        int localMin = Integer.MAX_VALUE;
        int localMinIndex = -1;

        for (int i = startIndex; i < finishIndex; i++) {
            if (arr[i] < localMin) {
                localMin = arr[i];
                localMinIndex = i;
            }
        }

        return new int[]{localMin, localMinIndex};
    }

    public void collectMin(int localMin, int localMinIndex) {
        lockerForMin.lock();
        try {
            if (localMin < globalMin) {
                globalMin = localMin;
                globalMinIndex = localMinIndex;
            }
        } finally {
            lockerForMin.unlock();
        }
    }

    public void incThreadCount() {
        lockerForCount.lock();
        try {
            threadCount++;
            countCondition.signalAll();
        } finally {
            lockerForCount.unlock();
        }
    }

    private void waitAllThreads() {
        lockerForCount.lock();
        try {
            while (threadCount < threadNum) {
                countCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockerForCount.unlock();
        }
    }

    public void parallelMin() {
        int chunkSize = dim / threadNum;
        int remainder = dim % threadNum;
        int currentStart = 0;

        for (int i = 0; i < threadNum; i++) {
            int currentFinish = currentStart + chunkSize + (i < remainder ? 1 : 0);

            ThreadMin worker = new ThreadMin(currentStart, currentFinish, this);
            Thread t = new Thread(worker);
            t.start();

            currentStart = currentFinish;
        }

        waitAllThreads();

        System.out.println("\n[Результат] Мінімальний елемент: " + globalMin);
        System.out.println("[Результат] Індекс: " + globalMinIndex);
    }
}