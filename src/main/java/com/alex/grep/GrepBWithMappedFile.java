package com.alex.grep;

import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@State(Scope.Benchmark)
@Fork(1)
public class GrepBWithMappedFile {
    @Param({"10000", "100000", "1000000"})
    public int N;

    private MappedByteBuffer bb;
    private Pattern patt;

    @Setup(Level.Trial)
    public void setUp() throws IOException {
        PrintWriter p = new PrintWriter(new FileWriter("/tmp/bigfile"));
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < N; i++) {
            int wordCount = r.nextInt(20);
            for (int j = 0; j < wordCount; j++) {
                sb.append(new BigInteger(r.nextInt(32) + 3, r));
                sb.append(' ');
            }
            p.println(sb);
            sb.setLength(0);
        }
        p.close();
        Path start = new File("/tmp/bigfile").toPath();
        FileChannel fc = FileChannel.open(start);
        bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        patt = Pattern.compile("12345*");
    }

    @Benchmark
    public List<DispLine> seqStream() throws IOException, InterruptedException {
        Spliterator<DispLine> ls = new LineSpliterator(bb, 0, bb.limit() - 1);
        return StreamSupport.stream(ls, false)
                .filter(l -> patt.matcher(l.line).find())
                .collect(toList());
    }

    @Benchmark
    public List<DispLine> parStream() throws IOException, InterruptedException {
        Spliterator<DispLine> ls = new LineSpliterator(bb, 0, bb.limit() - 1);
        return StreamSupport.stream(ls, true)
                .filter(l -> patt.matcher(l.line).find())
                .collect(toList());
    }

    @Benchmark
    public List<DispLine> sequential() throws Exception {
        List<DispLine> disps = new ArrayList<>();
        int nlIndex = 0, currentIndex = 0;
        int disp = 0;
        StringBuilder sb = new StringBuilder();
        while (currentIndex < bb.capacity()) {
            while (nlIndex < bb.capacity() && bb.get(nlIndex) != '\n') {
                sb.append(bb.get(nlIndex));
                nlIndex++;
            }
            String line = sb.toString();
            sb.setLength(0);
            if (patt.matcher(line).find()) disps.add(new DispLine(disp, line));
            disp += nlIndex + 1 - currentIndex;
            currentIndex += nlIndex + 1 - currentIndex;
            nlIndex = currentIndex + 1;
        }
        return disps;
    }
}
