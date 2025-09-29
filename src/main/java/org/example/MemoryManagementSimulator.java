package org.example;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Memory Management Simulator
 *
 * Assumptions (based on the assignment statement and example):
 *  - Total physical memory: 16 GB
 *  - Page size in PHYSICAL memory: 160 MB
 *  - Number of pages: 100
 *  - A process size is determined by a random number 1..30 where EACH number represents 80 MB.
 *    So: processSizeMB = rand(1..30) * 80
 *  - To store a process, we need ceil(processSizeMB / 160) MEMORY PAGES.
 *  - Allocation is contiguous, first-fit from low to high memory (starting at array index 0).
 *  - Starting memory address (for reporting) begins at 2000 and grows by 160 per page allocated.
 *  - We keep allocating new processes until all 100 pages are filled.
 *
 * Report columns (match the example wording/ordering):
 *  Process Id (MG) | Starting Memory Address | Size of the Process MB | Unused Space MG
 *

 */
public class MemoryManagementSimulator {

    static final int TOTAL_PAGES = 100;
    static final int PAGE_SIZE_MB = 160;
    static final int TOTAL_MEMORY_MB = TOTAL_PAGES * PAGE_SIZE_MB; // 16,000 MB (≈ 16 GB; using decimal MB as in prompt)
    static final int PROCESS_SIZE_UNIT_MB = 80;                     // From the prompt's example
    static final int MIN_UNITS = 1;
    static final int MAX_UNITS = 30;
    static final int START_ADDRESS = 2000;                          // Address units treated as MB for reporting

    static class Allocation {
        int pid;
        int startAddress;     // in "MB" units as per the prompt
        int processSizeMB;
        int pagesAllocated;
        int unusedMB;

        Allocation(int pid, int startAddress, int processSizeMB, int pagesAllocated, int unusedMB) {
            this.pid = pid;
            this.startAddress = startAddress;
            this.processSizeMB = processSizeMB;
            this.pagesAllocated = pagesAllocated;
            this.unusedMB = unusedMB;
        }
    }

    private final int[] pageTable = new int[TOTAL_PAGES]; // 0 = free, >0 = pid
    private final List<Allocation> allocations = new ArrayList<>();
    private final Random rnd;

    public MemoryManagementSimulator(long seed) {
        this.rnd = new Random(seed);
    }

    /**
     * Generate a process size in MB according to the assignment rule:
     * random units 1..30, each unit = 80MB.
     */
    private int generateProcessSizeMB() {
        int units = rnd.nextInt(MAX_UNITS - MIN_UNITS + 1) + MIN_UNITS; // 1..30
        return units * PROCESS_SIZE_UNIT_MB;
    }

    /**
     * Compute how many memory pages (size 160MB) are required to store processSizeMB.
     * pages = ceil(processSizeMB / 160)
     */
    private int pagesNeeded(int processSizeMB) {
        return (processSizeMB + PAGE_SIZE_MB - 1) / PAGE_SIZE_MB;
    }

    /**
     * Allocate contiguous pages from the next free index until we fill all 100 pages.
     * If a randomly generated process doesn't fit in the remaining pages, generate a new one
     * until one fits. This guarantees we finish with 0 free pages.
     */
    public void userMemoryAllocation() {
        int nextFreeIndex = 0;
        int nextAddress = START_ADDRESS;
        int pid = 1;

        while (nextFreeIndex < TOTAL_PAGES) {
            int remainingPages = TOTAL_PAGES - nextFreeIndex;

            int sizeMB;
            int needPages;

            // Keep generating a process until it fits the remaining pages.
            do {
                sizeMB = generateProcessSizeMB();
                needPages = pagesNeeded(sizeMB);
            } while (needPages > remainingPages);

            // Allocate [nextFreeIndex, nextFreeIndex + needPages)
            for (int i = 0; i < needPages; i++) {
                pageTable[nextFreeIndex + i] = pid;
            }

            int unusedMB = (needPages * PAGE_SIZE_MB) - sizeMB;

            allocations.add(new Allocation(pid, nextAddress, sizeMB, needPages, unusedMB));

            // Advance pointers
            nextFreeIndex += needPages;
            nextAddress += needPages * PAGE_SIZE_MB;
            pid++;
        }
    }

    private static String left(String s, int w) {
        if (s.length() >= w) return s.substring(0, w);
        StringBuilder b = new StringBuilder(s);
        while (b.length() < w) b.append(' ');
        return b.toString();
    }

    private static String right(String s, int w) {
        if (s.length() >= w) return s.substring(s.length() - w);
        StringBuilder b = new StringBuilder();
        while (b.length() + s.length() < w) b.append(' ');
        b.append(s);
        return b.toString();
    }

    public void printReport() {
        System.out.println("Summary Report Format Example:\n");
        System.out.println(left("Process Id MG", 16)
                + left("Starting Memory Address", 26)
                + left("Size of the Process MB", 25)
                + "Unused Space MG");
        for (Allocation a : allocations) {
            System.out.println(
                    right(String.valueOf(a.pid), 7) + "        " +
                            right(String.valueOf(a.startAddress), 7) + "                 " +
                            right(String.valueOf(a.processSizeMB), 7) + "                 " +
                            right(String.valueOf(a.unusedMB), 5)
            );
        }

        System.out.println("\nMethod:  userMemoryAllocation\n");

        System.out.println("Memory constants:");
        System.out.println(" - Total memory: " + TOTAL_MEMORY_MB + " MB (100 pages x 160 MB)");
        System.out.println(" - Process size unit: 80 MB (random 1..30 units)");
        System.out.println(" - Starting address: " + START_ADDRESS + "\n");

        // Also print a compact memory map (page -> pid)
        System.out.println("Memory Page Table (index:pid):");
        for (int i = 0; i < TOTAL_PAGES; i++) {
            System.out.print(i + ":" + pageTable[i]);
            if (i < TOTAL_PAGES - 1) System.out.print(" | ");
            if ((i + 1) % 20 == 0) System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        if (args.length == 1) {
            try {
                seed = Long.parseLong(args[0]);
            } catch (NumberFormatException ignored) {}
        }
        MemoryManagementSimulator sim = new MemoryManagementSimulator(seed);
        sim.userMemoryAllocation();
        sim.printReport();
    }
}