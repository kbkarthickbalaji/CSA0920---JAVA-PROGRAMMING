
import java.util.*;
 
public class TicketBenchmark {
    static String[] fields(int count) {
        String[] f = new String[count];
        for (int i = 0; i < count; i++)
            f[i] = "FIELD-" + i + "-VALUE";
        return f;
    }
 
    static String usingString(String[] fields) {
        String ticket = "";
        for (String field : fields) ticket = ticket + "|" + field;
        return ticket;
    }
 
    static String usingBuilder(String[] fields) {
        StringBuilder b = new StringBuilder();
        for (String field : fields) b.append('|').append(field);
        return b.toString();
    }
 
    static String usingBuffer(String[] fields) {
        StringBuffer b = new StringBuffer();
        for (String field : fields) b.append('|').append(field);
        return b.toString();
    }
 
    interface Work { long run(); }
 
    static long measure(Work work, int warmups, int repetitions) {
        for (int i = 0; i < warmups; i++) work.run();
        long total = 0;
        for (int i = 0; i < repetitions; i++) {
            long start = System.nanoTime();
            work.run();
            total += System.nanoTime() - start;
        }
        return total / repetitions;
    }
 
    static void test(String label, int tickets, int fieldCount) {
        String[] data = fields(fieldCount);
        int warmups = 5, repetitions = 10;
        long s = measure(() -> {
            long chars = 0;
            for (int i = 0; i < tickets; i++) chars += usingString(data).length();
            return chars;
        }, warmups, repetitions);
        long b = measure(() -> {
            long chars = 0;
            for (int i = 0; i < tickets; i++) chars += usingBuilder(data).length();
            return chars;
        }, warmups, repetitions);
        long sb = measure(() -> {
            long chars = 0;
            for (int i = 0; i < tickets; i++) chars += usingBuffer(data).length();
            return chars;
        }, warmups, repetitions);
 
        System.out.printf("%s | tickets=%d fields=%d | String=%d ns | Builder=%d ns | Buffer=%d ns%n",
                label, tickets, fieldCount, s, b, sb);
        System.out.println("Estimated String-loop intermediate results per ticket: "
                + fieldCount + " (excluding implementation-specific temporaries).");
    }
 
    public static void main(String[] args) {
        test("A-short", 1, 6);
        test("B-high-load", 10_000, 50);
        test("C-large-record", 1_000, 500);
    }
}