package com.alex.grep;

import java.nio.ByteBuffer;
import java.util.Spliterator;
import java.util.function.Consumer;

public final class LineSpliterator implements Spliterator<DispLine> {
    private static final int AVG_LINE_LENGTH = 40;
    private ByteBuffer bb;
    private int lo, hi;

    LineSpliterator(ByteBuffer bb, int lo, int hi) {
        this.bb = bb;
        this.lo = lo;
        this.hi = hi;
    }

    public boolean tryAdvance(Consumer<? super DispLine> action) {
        int index = lo;
        StringBuilder sb = new StringBuilder();
        do {
            sb.append((char) bb.get(index));
        } while (bb.get(index++) != '\n');
        action.accept(new DispLine(lo, sb.toString()));
        lo = lo + sb.length();
        return lo <= hi;
    }

    @Override
    public Spliterator<DispLine> trySplit() {
        int index = (lo + hi) >>> 1;
        while (bb.get(index) != '\n') index++;
        LineSpliterator newSpliterator = null;
        if (index != hi) {
            newSpliterator = new LineSpliterator(bb, lo, index);
            lo = index + 1;
        }
        return newSpliterator;
    }

    @Override
    public long estimateSize() {
        return (hi - lo + 1) / AVG_LINE_LENGTH;
    }

    @Override
    public int characteristics() {
        return ORDERED | IMMUTABLE | NONNULL;
    }
}
