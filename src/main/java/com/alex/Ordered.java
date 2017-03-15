package com.alex;

import org.openjdk.jmh.annotations.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@State(Scope.Benchmark)
@Fork(1)
public class Ordered {
    private List<String> stringList;

    @Setup(Level.Trial)
    public void main() {
        Random r = new Random();
        Supplier<String> strSupplier = () -> new BigInteger(64, r).toString();
        stringList = Stream.generate(strSupplier).limit(1_000_000).collect(toList());
    }

    @Benchmark
    public long unordered() {
        return stringList.parallelStream()
                .unordered()
                .distinct()
                .count();
    }

    @Benchmark
    public long ordered() {
        return stringList.parallelStream()
                .distinct()
                .count();
    }
}
