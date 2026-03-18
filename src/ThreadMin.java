
public class ThreadMin implements Runnable {
    private final int startIndex;
    private final int finishIndex;
    private final ArrClass arrClass;

    public ThreadMin(int startIndex, int finishIndex, ArrClass arrClass) {
        this.startIndex = startIndex;
        this.finishIndex = finishIndex;
        this.arrClass = arrClass;
    }

    @Override
    public void run() {
        int[] result = arrClass.findPartMin(startIndex, finishIndex);

        arrClass.collectMin(result[0], result[1]);

        arrClass.incThreadCount();
    }
}