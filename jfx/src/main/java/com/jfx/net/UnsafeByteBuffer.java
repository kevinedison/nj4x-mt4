package com.jfx.net;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

/**
 * User: roman
 * Date: 09.09.12
 * Time: 20:53
 */
@SuppressWarnings("UnusedDeclaration")
public class UnsafeByteBuffer {
    private static final Unsafe UNSAFE;

    static {
        try {
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    /**
     * Get a handle on the Unsafe instance, used for accessing low-level concurrency
     * and memory constructs.
     *
     * @return The Unsafe
     */
    public static Unsafe getUNSAFE() {
        return UNSAFE;
    }

    public static final int SIZE_OF_BOOLEAN = 1;
    public static final int SIZE_OF_SHORT = 2;
    public static final int SIZE_OF_INT = 4;
    public static final int SIZE_OF_LONG = 8;
    public static final byte[] ZERO_BYTES_ARRAY = new byte[0];

    private boolean throwNoException, isOutOfBuffer;
    private int position, capacity, limit;
    private final byte[] buffer;

    public UnsafeByteBuffer(final byte[] buffer) {
        if (null == buffer) {
            throw new NullPointerException("buffer cannot be null");
        }
        this.buffer = buffer;
        clear();
    }

    public UnsafeByteBuffer setThrowNoOverflowExceptions(boolean throwNoException) {
        this.throwNoException = throwNoException;
        return this;
    }

    public UnsafeByteBuffer clear() {
        this.isOutOfBuffer = false;
        this.position = 0;
        this.limit = this.capacity = buffer.length;
        return this;
    }

    public UnsafeByteBuffer position(int newPosition) {
        if ((newPosition > limit) || (newPosition < 0))
            throw new IllegalArgumentException();
        position = newPosition;
        return this;
    }

    public boolean isOutOfBuffer() {
        return isOutOfBuffer;
    }

    public final int position() {
        return position;
    }

    public final int limit() {
        return limit;
    }

    public final int capacity() {
        return capacity;
    }

    public final boolean hasRemaining() {
        return position < limit;
    }

    public final int remaining() {
        return limit - position;
    }

    public byte[] array() {
        return buffer;
    }

    private void checkBoundaries(long sizeOf) {
        checkBoundaries(position, sizeOf);
    }

    private void checkBoundaries(int position, long sizeOf) {
        if (!isOutOfBuffer && position + sizeOf > limit) {
            String message = "Out of buffer "
                    + "(pos=" + position
                    + ", bytes=" + sizeOf
                    + ", new_pos=" + (position + sizeOf)
                    + ", limit=" + limit
                    + ")";
            if (this.throwNoException) {
                isOutOfBuffer = true;
            } else {
                throw new RuntimeException(message);
            }
        }
    }

    private void checkReadBoundaries(long sizeOf) {
        checkBoundaries(position, sizeOf);
    }

    private void checkReadBoundaries(int position, long sizeOf) {
        if (position + sizeOf > limit) {
            String message = "Out of read buffer "
                    + "(pos=" + position
                    + ", bytes=" + sizeOf
                    + ", new_pos=" + (position + sizeOf)
                    + ", limit=" + limit
                    + ")";
            throw new RuntimeException(message);
        }
    }

    public UnsafeByteBuffer put(final byte value) {
        checkBoundaries(SIZE_OF_BOOLEAN);
        if (isOutOfBuffer) return this;
        UNSAFE.putByte(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position, value);
        position += SIZE_OF_BOOLEAN;
        return this;
    }

    public UnsafeByteBuffer put(int position, final byte value) {
        checkBoundaries(position, SIZE_OF_BOOLEAN);
        if (isOutOfBuffer) return this;
        UNSAFE.putByte(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position, value);
        return this;
    }

    public byte get() {
        checkReadBoundaries(SIZE_OF_BOOLEAN);
        byte value = UNSAFE.getByte(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position);
        position += SIZE_OF_BOOLEAN;
        return value;
    }

    public void putDouble(final double value) {
        checkBoundaries(SIZE_OF_LONG);
        if (isOutOfBuffer) return;
        //
        UNSAFE.putDouble(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position, value);
        position += SIZE_OF_LONG;
    }

    public double getDouble() {
        checkReadBoundaries(SIZE_OF_LONG);
        //
        double value = UNSAFE.getDouble(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position);
        position += SIZE_OF_LONG;

        return value;
    }

    public void putInt(final int value) {
        checkBoundaries(SIZE_OF_INT);
        if (isOutOfBuffer) return;
        //
        UNSAFE.putInt(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position, value);
        position += SIZE_OF_INT;
    }

    public int getInt() {
        checkReadBoundaries(SIZE_OF_INT);
        //
        int value = UNSAFE.getInt(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position);
        position += SIZE_OF_INT;

        return value;
    }

    public int getInt(int position) {
        checkReadBoundaries(position, SIZE_OF_INT);
        //
        return UNSAFE.getInt(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position);
    }

    public void putLong(final long value) {
        checkBoundaries(SIZE_OF_LONG);
        if (isOutOfBuffer) return;
        //
        UNSAFE.putLong(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position, value);
        position += SIZE_OF_LONG;
    }

    public long getLong() {
        checkReadBoundaries(SIZE_OF_LONG);
        //
        long value = UNSAFE.getLong(buffer, (long) ARRAY_BYTE_BASE_OFFSET + position);
        position += SIZE_OF_LONG;

        return value;
    }

    public void put(final byte[] values) {
        put(values, 0, values.length);
    }

    public void put(final byte[] values, final int offset, final int len) {
        checkBoundaries(len);
        if (isOutOfBuffer) return;
        //
        UNSAFE.copyMemory(values, ARRAY_BYTE_BASE_OFFSET + offset,
                buffer, ARRAY_BYTE_BASE_OFFSET + position,
                len);
        position += len;
    }

    public UnsafeByteBuffer get(byte[] dst) {
        return get(dst, 0, dst.length);
    }

    public UnsafeByteBuffer get(byte[] dst, int offset, int length) {
        checkBounds(offset, length, dst.length);
        checkReadBoundaries(length);
        copyArray(buffer, position, dst, offset, length);
        position += length;
        return this;
    }

    public static void copyArray(final byte[] src, final int srcOffset, final byte[] dst, final int dstOffset, final int len) {
        if (len > 0) {
            if (dstOffset + len > dst.length) {
                throw new RuntimeException("Out of buffer (len=" + dst.length
                        + ", pos=" + dstOffset
                        + ", sz=" + len
                        + ")");
            }
            //
            UNSAFE.copyMemory(src, ARRAY_BYTE_BASE_OFFSET + srcOffset,
                    dst, ARRAY_BYTE_BASE_OFFSET + dstOffset,
                    len);
        }
    }

    public int getByteArray(final byte[] values, final int offset) {
        int arraySize = getInt();
        checkReadBoundaries(arraySize);
        if (arraySize > values.length - offset) {
            position -= SIZE_OF_INT;
            throw new RuntimeException("Target array is too small");
        }
        UNSAFE.copyMemory(buffer, ARRAY_BYTE_BASE_OFFSET + position,
                values, ARRAY_BYTE_BASE_OFFSET + offset,
                arraySize);
        position += arraySize;
        return arraySize;
    }

    public byte[] getByteArray() {
        int max = getInt();
        if (max > 0) {
            if (max > ARRAY_SIZE) {
                throw new RuntimeException("Assuming getByteArray size < " + ARRAY_SIZE + ", got " + max);
            }
            byte[] array = new byte[max];
            get(array, 0, max);
            return array;
        } else if (max == -1) {
            return null;
        } else if (max < 0) {
            throw new RuntimeException("Assuming getByteArray size -1 or >0, got " + max);
        } else {
            return ZERO_BYTES_ARRAY;
        }
    }

    public UnsafeByteBuffer setPosition(int position) {
        this.position = position;
        return this;
    }

    public UnsafeByteBuffer limit(int newLimit) {
        if ((newLimit > capacity) || (newLimit < 0))
            throw new IllegalArgumentException();
        limit = newLimit;
        if (position > limit) position = limit;
        return this;
    }

    public UnsafeByteBuffer flip() {
        limit = position;
        position = 0;
        return this;
    }

    public static UnsafeByteBuffer allocate(int newSize) {
        return new UnsafeByteBuffer(new byte[newSize]);
    }

    public void put(UnsafeByteBuffer byteBuffer) {
        put(byteBuffer.buffer, byteBuffer.position, byteBuffer.remaining());
    }

    public String toString() {
        return "UnsafeByteBuffer "
                + "[cap=" + capacity()
                + ", pos=" + position()
                + ", lim=" + limit()
                + ']';
    }

    static void checkBounds(int off, int len, int size) {
        if ((off | len | (off + len) | (size - (off + len))) < 0)
            throw new IndexOutOfBoundsException();
    }

    // ----------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------
    // ----------------------------------------------------------------------------------------------------------
    public static int ARRAY_SIZE = 10000;
    public static int MATRIX_SIZE = 10000;
    public static int StringBuilder_SIZE = 10240; // 10KB
    public static final int MAX_SIZE = 1024 * 1024 * 64;//64MB
    public static final String TINY_ASCII_STR_MSG2 = "TinyASCIIString";
    public static final String TINY_BYTE_ARR_MSG = "TinyByteArray";
    public static final String TINY_BB_ARR_MSG = "TinyUnsafeByteBufferableArray";
    public static final String TINY_BB_AL_MSG = "TinyUnsafeByteBufferableArrayList";
    //
    private static final byte[] ZERO_BYTE_ARRAY = new byte[0];
    private static final StringBuilder EMPTY_STRING_BUILDER = new StringBuilder();
    private static final String EMPTY_STRING = "";

    public void putByteArray(byte[] array, int offset, int length) {
        int max;
        if (array != null && (max = length) > 0) {
            putInt(max);
            //expand(max);
            put(array, offset, length);
        } else if (array == null) {
            putInt(-1);
        } else {
            putInt(0);
        }
    }
}
