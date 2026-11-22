import java.util.*;
 
public class PlaylistBenchmark {
    static class Song {
        final int id;
        Song(int id) { this.id = id; }
    }
 
    static long time(Runnable task, int reps) {
        long start = System.nanoTime();
        for (int i = 0; i < reps; i++) task.run();
        return (System.nanoTime() - start) / reps;
    }
 
    static void run(int n) {
        List<Song> a = new ArrayList<>(), l = new LinkedList<>();
        for (int i = 0; i < n; i++) { a.add(new Song(i)); l.add(new Song(i)); }
        int reps = Math.max(100, 1_000_000 / n);
        System.out.println("N=" + n + " reps=" + reps);
        System.out.println("ArrayList get: " +
            time(() -> a.get(n / 2), reps) + " ns/op");
        System.out.println("LinkedList get: " +
            time(() -> l.get(n / 2), reps) + " ns/op");
        System.out.println("ArrayList iterate: " +
            time(() -> { long x=0; for (Song s:a) x+=s.id; }, 10) + " ns/run");
        System.out.println("LinkedList iterate: " +
            time(() -> { long x=0; for (Song s:l) x+=s.id; }, 10) + " ns/run");
        System.out.println("ArrayList front insert/remove: " +
            time(() -> { a.add(0, new Song(-1)); a.remove(0); }, reps) + " ns/pair");
        System.out.println("LinkedList front insert/remove: " +
            time(() -> { l.add(0, new Song(-1)); l.remove(0); }, reps) + " ns/pair");
        int mid = n / 2;
        System.out.println("ArrayList middle insert/remove: " +
            time(() -> { a.add(mid, new Song(-1)); a.remove(mid); }, reps) + " ns/pair");
        System.out.println("LinkedList middle insert/remove: " +
            time(() -> { l.add(mid, new Song(-1)); l.remove(mid); }, reps) + " ns/pair");
        System.out.println("ArrayList append: " +
            time(() -> a.add(new Song(-1)), reps) + " ns/op");
        System.out.println("LinkedList append: " +
            time(() -> l.add(new Song(-1)), reps) + " ns/op");
    }
 
    public static void main(String[] args) {
        run(1_000); run(10_000); run(100_000);
    }
}