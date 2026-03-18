public class Main {
    public static void main(String[] args) {
        int dim = 10000000;
        int threadNum = 4;

        ArrClass arrClass = new ArrClass(dim, threadNum);
        arrClass.parallelMin();
    }
}