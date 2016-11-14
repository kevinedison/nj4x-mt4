package com.jfx;

/**
 * Parses strings of the format: [BeginChar data EndChar]+
 */
class DDParser {
    private int ixStart, ixEnd;
    private String src;
    private char start;
    private char end;
    private String v;

    public DDParser(String src, char start, char end) {
        this.src = src;
        this.start = start;
        this.end = end;
        ixStart = src.indexOf(start);
        ixEnd = src.indexOf(end, ixStart + 1);
        v = src.substring(ixStart + 1, ixEnd);
    }

    public String peek() {
        return v;
    }

    public String pop() {
        try {
            return v;
        } finally {
            if (ixEnd < src.length()) {
                ixStart = src.indexOf(start, ixEnd + 1);
                if (ixStart > 0) {
                    ixEnd = src.indexOf(end, ixStart + 1);
                    v = src.substring(ixStart + 1, ixEnd);
                }
            }
        }
    }

    public String tail() {
        return ixEnd + 1 < src.length() ? src.substring(ixEnd + 1) : null;
    }
}
