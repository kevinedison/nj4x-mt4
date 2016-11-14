/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx.ts.io;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to read .property files of the extended syntax.
 * It has functionality of java.util.Properties()
 */
@SuppressWarnings({"unchecked"})
public class ResourceReader extends HashMap {

    private static final HashMap objProps = new HashMap();
    public static final String GLOBAL_RESOURCE_DIR = "global_resource_dir";
    public static final String VARIABLE_PREFIX = "${";
    private static final String PROPERTIES = ".properties";
    private static final String RESOURCES = "resources/";
    private static final String _RESOURCES_ = "/resources/";
    private static final String _8859_1 = "8859_1";
    private static final String EQ = "=";
    private static final String ENCODING = "encoding";
    private static final String EMPTY_STRING = "";
    private static final String _FALSE = "false";
    private static final String ZERO = "0";
    private static final String STR_BR = "}";
    private static final String _1 = ".1";

    public static ResourceReader getObjectResourceReader(Object o) {
        return getObjectResourceReader(o, false);
    }

    public static ResourceReader getObjectResourceReader(Object o, boolean reload) {
        Class clazz = o.getClass();
        return getClassResourceReader(clazz, reload);
    }

    public static ResourceReader getClassResourceReader(Class clazz) {
        return getClassResourceReader(clazz, false);
    }

    public static ResourceReader getClassResourceReader(Class clazz, boolean reload) {
        String clazzName = clazz.getName();
        clazzName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
        String resourceName = clazzName + PROPERTIES;
        return getClassResourceReader(clazz, resourceName, reload);
    }

    public static ResourceReader getClassResourceReader(Class clazz, String resourceName) {
        return getClassResourceReader(clazz, resourceName, false);
    }

    public static ResourceReader getClassResourceReader(Class clazz, String resourceName, boolean reload) {
        String clazzName = clazz.getName();
        clazzName = clazzName.substring(clazzName.lastIndexOf('.') + 1);
        String resourceFileName = (resourceName == null ? clazzName + PROPERTIES : resourceName);
        resourceName = RESOURCES + resourceFileName;
        ResourceReader rr = ((ResourceReader) objProps.get(clazzName + resourceName));
        if (rr == null || reload) {
            synchronized (objProps) {
                rr = ((ResourceReader) objProps.get(clazzName + resourceName));
                if (rr == null || reload) {
                    try {
                        rr = new ResourceReader();
                        String globalResDir = System.getProperty(GLOBAL_RESOURCE_DIR);
                        rr.load(
                                globalResDir == null || !new File(globalResDir + '/' + resourceFileName).exists()
                                        ? clazz.getResourceAsStream(resourceName)
                                        : new BufferedInputStream(new FileInputStream(globalResDir + '/' + resourceFileName))
                        );
                        objProps.put(clazzName + resourceName, rr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        rr = null;
                    }
                }
            }
        }
        return rr;
    }

    public static ResourceReader getPackageResourceReader(String packageName, String resourceFileName) {
        return getPackageResourceReader(packageName, resourceFileName, false);
    }

    public static ResourceReader getPackageResourceReader(String packageName, String resourceFileName, boolean reload) {
        String resourceLocation = packageName.replace('.', '/') + _RESOURCES_ + resourceFileName;
        ResourceReader rr = ((ResourceReader) objProps.get(resourceLocation));
        if (rr == null || reload) {
            synchronized (objProps) {
                rr = ((ResourceReader) objProps.get(resourceLocation));
                if (rr == null || reload) {
                    try {
                        rr = new ResourceReader();
                        rr.load(getPackageResourceStream(packageName, resourceFileName));
                        objProps.put(resourceLocation, rr);
                    } catch (IOException ex) {
                        rr = null;
                    }
                }
            }
        }
        return rr;
    }

    public static InputStream getPackageResourceStream(String packageName, String resourceFileName) {
        String resourceLocation = packageName.replace('.', '/') + _RESOURCES_ + resourceFileName;
        try {
            String globalResDir = System.getProperty(GLOBAL_RESOURCE_DIR);
            return globalResDir == null || !new File(globalResDir + '/' + resourceFileName).exists()
                    ? Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation)
                    : new BufferedInputStream(new FileInputStream(globalResDir + '/' + resourceFileName));
        } catch (FileNotFoundException e) {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceLocation);
        }
    }

    public static String getObjectProperty(Object o, String propertyName) {
        return getObjectResourceReader(o).getProperty(propertyName);
    }

    protected ResourceReader defaults;

    public ResourceReader() {
        this(null);
    }

    public ResourceReader(ResourceReader defaults) {
        this.defaults = defaults;
    }

    private static final String KEY_VALUE_SEPARATORS = "=: \t\r\n\f";
    private static final String STRICT_KEY_VALUE_SEP = "=:";
    private static final String WHITE_SPACE_CHARS = " \t\r\n\f";

    /**
     * Reads a property list (key and element pairs) from the input stream.
     * By default, the stream is assumed to be using the ISO 8859-1 character encoding.
     * If the stream is encoded differently, one should use "encoding=yourCharSet" definition
     * at the very first property in a stream to identify true encoding:
     * <pre>
     * encoding = Windows-1251
     * p1   p1Value
     * p2 = [p2
     * Multiline
     * Value]
     * p4 p4 \
     * split \
     * value
     * p4 : p4-Value
     * </pre>
     * <p/>
     * Every property occupies one line of the input stream.
     * Extended syntax allows to setup multi-line properties, using "[","]" separators.
     * Each line is terminated by a line terminator (<code>\n</code> or <code>\r</code>
     * or <code>\r\n</code>). Lines from the input stream are processed until
     * end of file is reached on the input stream.
     * <p/>
     * A line that contains only whitespace or whose first non-whitespace
     * character is an <code>#</code> or <code>!</code> is ignored
     * (thus, <code>#</code> or <code>!</code> indicate comment lines).
     * <p/>
     * Every line other than a blank line or a comment line describes one
     * property to be added to the table.
     * Any whitespace after the key is skipped; if the first non-whitespace
     * character after the key is <code>=</code> or <code>:</code>, then it
     * is ignored and any whitespace characters after it are also skipped.
     * All remaining characters on the line become part of the associated
     * element string. Within the element string, the ASCII
     * escape sequences <code>\t</code>, <code>\n</code>,
     * <code>\r</code>, <code>\\</code>, <code>\"</code>, <code>\'</code>,
     * <code>\ &#32;</code> &#32;(a backslash and a space), and
     * <code>&#92;u</code><i>xxxx</i> are recognized and converted to single
     * characters. Moreover, if the first character on the line is "[",
     * then the value of a property can occupy multiple lines until the close bracet symbol ("]")
     * is encountered.
     * <p/>
     * As an example, each of the following four lines specifies the key
     * <code>"Truth"</code> and the associated element value
     * <code>"Beauty"</code>:
     * <p/>
     * <pre>
     * Truth = Beauty
     * 	Truth:Beauty
     * Truth			:Beauty
     * </pre>
     * As another example, the following three lines specify a single
     * property:
     * <p/>
     * <pre>
     * fruits = [apple, banana, pear,
     * cantaloupe, watermelon,
     * kiwi, mango]
     * </pre>
     * The key is <code>"fruits"</code> and the associated element is:
     * <p/>
     * <pre>"apple, banana, pear,\n cantaloupe, watermelon,\n kiwi, mango"</pre>
     * <p/>
     * As a third example, the line:
     * <p/>
     * <pre>cheeses
     * </pre>
     * specifies that the key is <code>"cheeses"</code> and the associated
     * element is the empty string.<p>
     *
     * @param inStream the input stream.
     * @throws java.io.IOException if an error occurred when reading from the
     *                             input stream.
     */
    public void load(InputStream inStream) throws IOException {
        if (inStream != null) {
            InputStreamManager inputStreamManager = new InputStreamManager(inStream);
            byte[] data = inputStreamManager.getBytes();
            //
            // determine encoding
            //
            int newLineIndex = -1;
            boolean dataFound = false;
            for (int i = 0; i < data.length; ++i) {
                if (!dataFound && WHITE_SPACE_CHARS.indexOf((char) data[i]) == -1) {
                    dataFound = true;
                }
                if (dataFound && (data[i] == (byte) '\n' || data[i] == (byte) '\r')) {
                    newLineIndex = i;
                    break;
                }
            }
            String encoding = _8859_1;
            if (newLineIndex > 0) {
                String s = new String(data, 0, newLineIndex, encoding);
                String[] firstLine = s.split(EQ);//new Tokenizer(s, EQ).getTokens();
                if (firstLine.length == 2 && firstLine[0].trim().equalsIgnoreCase(ENCODING)) {
                    // encoding has been determined
                    encoding = firstLine[1].trim();
                }
            }
            load(new ByteArrayInputStream(data), encoding);
        }
    }

    public static void main(String[] args) throws IOException {
        ResourceReader p;
        (p = new ResourceReader()).load(new FileInputStream("c:/res.properties"));
        System.out.println(EMPTY_STRING + p);
    }

    public void load(InputStream inStream, String charsetName) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inStream, charsetName));
        while (true) {
            // Get next line
            String line = in.readLine();
            if (line == null) {
                return;
            }
            if (line.length() > 0) {
                // Continue lines that end in slashes if they are not comments
                char firstChar = line.charAt(0);
                if ((firstChar != '#') && (firstChar != '!')) {
                    while (continueLine(line)) {
                        String nextLine = in.readLine();
                        if (nextLine == null)
                            nextLine = EMPTY_STRING;
                        String loppedLine = line.substring(0, line.length() - 1);
                        // Advance beyond whitespace on new line
                        int startIndex;
                        for (startIndex = 0; startIndex < nextLine.length(); startIndex++)
                            if (WHITE_SPACE_CHARS.indexOf(nextLine.charAt(startIndex)) == -1)
                                break;
                        nextLine = nextLine.substring(startIndex, nextLine.length());
                        line = loppedLine + nextLine;
                    }

                    // Find start of key
                    int len = line.length();
                    int keyStart;
                    for (keyStart = 0; keyStart < len; keyStart++) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(keyStart)) == -1)
                            break;
                    }

                    // Blank lines are ignored
                    if (keyStart == len)
                        continue;

                    // Find separation between key and value
                    int separatorIndex;
                    for (separatorIndex = keyStart; separatorIndex < len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if (currentChar == '\\')
                            separatorIndex++;
                        else if (KEY_VALUE_SEPARATORS.indexOf(currentChar) != -1)
                            break;
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex = separatorIndex; valueIndex < len; valueIndex++)
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1)
                            break;

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len)
                        if (STRICT_KEY_VALUE_SEP.indexOf(line.charAt(valueIndex)) != -1)
                            valueIndex++;

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (WHITE_SPACE_CHARS.indexOf(line.charAt(valueIndex)) == -1)
                            break;
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : EMPTY_STRING;

                    boolean multiline;
                    multiline = value.length() > 0 && value.charAt(0) == '[';
                    if (multiline) {
                        StringBuilder mlb = value.length() == 1 ? new StringBuilder() : new StringBuilder(value.substring(1));
                        while (true) {
                            String nextLine = in.readLine();
//                            if ((nextLine == null) || (nextLine.length() == 0))
//                                break;
                            mlb.append(System.getProperty("line.separator")).append(nextLine);
                            if (nextLine.length() == 1 && nextLine.charAt(0) == ']') {
                                mlb.delete(mlb.length() - 1, mlb.length());
                                value = mlb.toString();
                                break;
                            }
                        }
                    }

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);
                    put(key, value);

                    // ---------------------------------------------------------------

/*
                    int equals = line.indexOf('=');
                    if (equals == -1)
                        continue;
                    equals++;
                    while(equals < line.length() && line.charAt(equals) == ' ') {
                        equals++;
                    }

                    boolean multiline;
                    if (equals >= line.length()) {
                        multiline = false;
                    } else {
                        if (line.charAt(equals) == '[') {
                            line = line.substring(0, equals);
                            multiline = true;
                        } else {
                            multiline = false;
                        }
                    }
                    if (multiline) {
                        while (true) {
                            String nextLine = in.readLine();
                            if ((nextLine == null) || (nextLine.length() == 0))
                                break;
                            line = new String(line+'\n'+nextLine);
                            if (line.charAt(line.length()-1) == ']') {
                                line = line.substring(0, line.length()-1);
                                break;
                            }
                        }
                    }
                    // Find start of key
                    int len = line.length();
                    int keyStart;
                    for(keyStart=0; keyStart<len; keyStart++) {
                        if(whiteSpaceChars.indexOf(line.charAt(keyStart)) == -1)
                            break;
                    }
                    // Find separation between key and value
                    int separatorIndex;
                    for(separatorIndex=keyStart; separatorIndex<len; separatorIndex++) {
                        char currentChar = line.charAt(separatorIndex);
                        if(keyValueSeparators.indexOf(currentChar) != -1)
                            break;
                    }

                    // Skip over whitespace after key if any
                    int valueIndex;
                    for (valueIndex=separatorIndex; valueIndex<len; valueIndex++)
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;

                    // Skip over one non whitespace key value separators if any
                    if (valueIndex < len)
                        if (strictKeyValueSeparators.indexOf(line.charAt(valueIndex)) != -1)
                            valueIndex++;

                    // Skip over white space after other separators if any
                    while (valueIndex < len) {
                        if (whiteSpaceChars.indexOf(line.charAt(valueIndex)) == -1)
                            break;
                        valueIndex++;
                    }
                    String key = line.substring(keyStart, separatorIndex);
                    String value = (separatorIndex < len) ? line.substring(valueIndex, len) : "";

                    // Convert then store key and value
                    key = loadConvert(key);
                    value = loadConvert(value);
                    put(key, value);
*/
                }
            }
        }
    }

    /*
     * Returns true if the given line is a line that must
     * be appended to the next line
     */
    private boolean continueLine(String line) {
        int slashCount = 0;
        int index = line.length() - 1;
        while ((index >= 0) && (line.charAt(index--) == '\\'))
            slashCount++;
        return (slashCount % 2 == 1);
    }

    /*
     * Converts encoded &#92;uxxxx to unicode chars
     * and changes special saved chars to their original forms
     */
    private String loadConvert(String theString) {
        char aChar;
        int len = theString.length();
        StringBuilder outBuffer = new StringBuilder(len);

        for (int x = 0; x < len; ) {
            aChar = theString.charAt(x++);
            if (aChar == '\\') {
                aChar = theString.charAt(x++);
                if (aChar == 'u') {
                    // Read the xxxx
                    int value = 0;
                    for (int i = 0; i < 4; i++) {
                        aChar = theString.charAt(x++);
                        switch (aChar) {
                            case '0':
                            case '1':
                            case '2':
                            case '3':
                            case '4':
                            case '5':
                            case '6':
                            case '7':
                            case '8':
                            case '9':
                                value = (value << 4) + aChar - '0';
                                break;
                            case 'a':
                            case 'b':
                            case 'c':
                            case 'd':
                            case 'e':
                            case 'f':
                                value = (value << 4) + 10 + aChar - 'a';
                                break;
                            case 'A':
                            case 'B':
                            case 'C':
                            case 'D':
                            case 'E':
                            case 'F':
                                value = (value << 4) + 10 + aChar - 'A';
                                break;
                            default:
                                throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
                        }
                    }
                    outBuffer.append((char) value);
                } else {
                    if (aChar == 't')
                        aChar = '\t';
                    else if (aChar == 'r')
                        aChar = '\r';
                    else if (aChar == 'n')
                        aChar = '\n';
                    else if (aChar == 'f') aChar = '\f';
                    outBuffer.append(aChar);
                }
            } else
                outBuffer.append(aChar);
        }
        return outBuffer.toString();
    }

    private boolean noVariableTranslation;

    public ResourceReader setNoVariableTranslation(boolean noVariableTranslation) {
        this.noVariableTranslation = noVariableTranslation;
        return this;
    }

    public String getProperty(String key) {
        String sval = key != null && key.length() > 0 ? System.getProperty(key) : null;
        sval = sval == null ? (String) super.get(key) : sval;
        //noinspection ConstantConditions
        return sval == null || noVariableTranslation || sval.indexOf(VARIABLE_PREFIX) < 0
                ? sval
                : processString(key, ((sval == null) && (defaults != null)) ? defaults.getProperty(key) : sval);
    }

    public boolean getPropertyAsBoolean(String key) {
        return Boolean.valueOf(getProperty(key, _FALSE));
    }

    public int getPropertyAsInt(String key) {
        return Integer.parseInt(getProperty(key, ZERO));
    }

    public int getPropertyAsInt(String key, int dflt) {
        return Integer.parseInt(getProperty(key, String.valueOf(dflt)));
    }

    private HashMap recursive = new HashMap();

    public synchronized String processString(String key, String value) {
        if (recursive.containsKey(key)) {
            throw new RuntimeException("ResourceReader: recursive reference: " + key);
        }
        if (value == null) {
            return null;
        }
        try {
            recursive.put(key, key);
            while (true) {
                int ix = (value == null) ? (-1) : (value.indexOf(VARIABLE_PREFIX));
                if (ix >= 0) {
                    ix += 2;
                    assert value != null;
                    int ix2 = value.indexOf(STR_BR, ix);
                    if (ix2 < 0) break;
                    String nm = value.substring(ix, ix2);
                    //
                    String value2 = getProperty(nm);
                    //
                    value = value2 == null
                            ? (ix == 2 && ix2 == value.length() - 2 ? null : value.substring(0, ix - 2) + value.substring(ix2 + 1))
                            : value.substring(0, ix - 2) + value2 + value.substring(ix2 + 1);
                    continue;
                }
                break;
            }
            value = value != null && value.length() == 0 ? null : value;
        } finally {
            recursive.remove(key);
        }
        return value;
    }

    public String getProperty(String key, String defaultValue) {
        String val = getProperty(key);
        return (val == null) ? defaultValue : val;
    }

    public ArrayList fillPropertyGroup(String prefix) {
        String id = getProperty(prefix);
        if (id != null) {
            ArrayList ids = new ArrayList();
            ids.add(id);
            return ids;
        } else {
            id = getProperty(prefix + _1);
            if (id != null) {
                ArrayList ids = new ArrayList();
                for (int i = 2; id != null; id = getProperty(prefix + '.' + (i++))) {
                    ids.add(id);
                }
                return ids;
            } else {
                return null;
            }
        }
    }

    /**
     * Calls the <tt>Hashtable</tt> method <code>put</code>. Provided for
     * parallelism with the <tt>getProperty</tt> method. Enforces use of
     * strings for property keys and values. The value returned is the
     * result of the <tt>Hashtable</tt> call to <code>put</code>.
     *
     * @param key   the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @return the previous value of the specified key in this property
     *         list, or <code>null</code> if it did not have one.
     * @see #getProperty
     * @since 1.2
     */
    public Object setProperty(String key, String value) {
        return put(key, value);
    }
}
