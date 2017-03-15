package com.alex.grep;

public final class DispLine {
    final int disp;
    final String line;

    DispLine(int d, String l) {
        disp = d;
        line = l;
    }

    public String toString() {
        return disp + ":" + line;
    }
}
