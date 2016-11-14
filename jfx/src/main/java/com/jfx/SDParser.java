package com.jfx;

/**
 * Parses strings of the format: "data"['delimiter'"data"]+
 */
class SDParser {
    private int ix;
    private String src;
    private char delimiter;
    private String v;

    public SDParser(String src, char delimiter) {
        this.src = src;
        this.delimiter = delimiter;
        ix = src.indexOf(delimiter);
        ix = ix < 0 ? src.length() : ix;
        v = src.substring(0, ix);
    }

    public String peek() {
        return v;
    }

    public boolean popBoolean() {
        String result = pop();
        return result.length() > 1 ? Boolean.valueOf(result).booleanValue() : (Integer.parseInt(result) == 0 ? false : true);
    }

    public int popInt() {
        String result = pop();
        return Integer.parseInt(result);
    }

    public long popLong() {
        String result = pop();
        return Long.parseLong(result);
    }

    public double popDouble() {
        String result = pop();
        return Double.parseDouble(result);
    }

    public String pop() {
        try {
            return v;
        } finally {
            if (ix < src.length()) {
                int ix2 = src.indexOf(delimiter, ix + 1);
                ix2 = ix2 < 0 ? src.length() : ix2;
                v = src.substring(ix + 1, ix2);
                ix = ix2;
            } else {
                v = null;
            }
        }
    }
}
