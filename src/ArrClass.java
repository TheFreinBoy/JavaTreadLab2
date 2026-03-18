import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ArrClass {
    private final int dim;
    private final int[] arr;

    private int globalMin;
    private int globalMinIndex;

    private final Lock lockerForMin = new ReentrantLock();
    private final Lock lockerForCount = new ReentrantLock();
    private final Condition countCondition = lockerForCount.newCondition();

    private int threadCount;
    private int currentThreadNum;

    public ArrClass(int dim) {
        this.dim = dim;
        this.arr = new int[dim];
        initArr();
    }

    private void initArr() {
        System.out.println("Генерація масиву з " + dim + " елементів... (зачекайте)");
        Random rnd = new Random();

        for (int i = 0; i < dim; i++) {
            arr[i] = rnd.nextInt(100) + 1;
        }

        int randomIndex = rnd.nextInt(dim);
        int randomNegative = -(rnd.nextInt(1000) + 1);
        arr[randomIndex] = randomNegative;

        System.out.println("[ЗГЕНЕРОВАНО] Мінусовий елемент: " + randomNegative + ", з індексом: " + randomIndex + "\n");
    }

    public void resetForNewSearch(int threadNum) {
        this.globalMin = Integer.MAX_VALUE;
        this.globalMinIndex = -1;
        this.threadCount = 0;
        this.currentThreadNum = threadNum;
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
            while (threadCount < currentThreadNum) {
                countCondition.await();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lockerForCount.unlock();
        }
    }

    public void parallelMin() {
        int chunkSize = dim / currentThreadNum;
        int remainder = dim % currentThreadNum;
        int currentStart = 0;

        for (int i = 0; i < currentThreadNum; i++) {
            int currentFinish = currentStart + chunkSize + (i < remainder ? 1 : 0);

            ThreadMin worker = new ThreadMin(currentStart, currentFinish, this);
            Thread t = new Thread(worker);
            t.start();

            currentStart = currentFinish;
        }

        waitAllThreads();
    }

    public int getGlobalMin() { return globalMin; }
    public int getGlobalMinIndex() { return globalMinIndex; }
}