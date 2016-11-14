package com.jfx;

/**
 * NJ4X API version registry. 
 * It includes local API version, Terminal Server version, communication DLL version
 * and MQL Expert Advisor version.
 */
public class Version {
    private static final String version = "2.6.2";
    public static final String NJ4X = version;
    public static final String NJ4X_UUID = "29a50980516c";
    public String ts, dll, mql;

    public Version(String ts, String dll, String mql) {
        this.ts = ts;
        this.dll = dll;
        this.mql = mql;
    }

    /**
     * Checks if local API, Terminal Server, communication DLL
     * and NJ4X MQL Expert Advisor are of the same version.
     * @return true - if all API parts are of the same version.
     */
    public boolean isConsistent() {
        return dll != null && mql != null
                && NJ4X.substring(0, 5).equals(dll.substring(0, 5))
                && dll.substring(0, 5).equals(mql.substring(0, 5))
                && (ts == null || mql.substring(0, 5).equals(ts.substring(0, 5)));
    }

    @Override
    public String toString() {
        return "NJ4X API v." + NJ4X
                + (ts == null ? "" : ", TS v." + ts)
                + ", DLL v." + dll
                + ", MQL v." + mql;
    }
}
