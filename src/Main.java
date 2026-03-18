public class Main {
    public static void main(String[] args) {
        int dim = 1000000000;

        ArrClass arrClass = new ArrClass(dim);
        int[] threadConfigs = {1, 2, 4, 6, 8};

        System.out.println("--- ПОЧАТОК ПОШУКУ (JAVA) ---");

        for (int threads : threadConfigs) {
            arrClass.resetForNewSearch(threads);

            long startTime = System.currentTimeMillis();
            arrClass.parallelMin();
            long endTime = System.currentTimeMillis();

            long duration = endTime - startTime;

            System.out.printf("Потоків: %d | Час: %4d мс | Знайдено мін: %d (індекс: %d)%n",
                    threads, duration, arrClass.getGlobalMin(), arrClass.getGlobalMinIndex());
        }
    }
}