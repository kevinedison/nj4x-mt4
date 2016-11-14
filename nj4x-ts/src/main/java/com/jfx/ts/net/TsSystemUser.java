package com.jfx.ts.net;

import com.jfx.ts.io.ExternalProcess;

import java.io.IOException;

/**
* todo: comments
* User: roman
* Date: 05/08/2014
* Time: 22:02
*/
public class TsSystemUser {
    public static final String NJ4X_HOST = "NJ4X_HOST_";
    private TS ts;
    int id;
    String name, password;
    Session session;

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name/* + "/" + password*/;
    }

    TsSystemUser(TS ts, String name) {
        this.ts = ts;
        if (name.startsWith(NJ4X_HOST)) {
            this.id = Integer.parseInt(name.substring(NJ4X_HOST.length()));
            this.name = name;
            setPassword();
        } else {
            throw new RuntimeException("Invalid TS user name: " + name);
        }
    }

    TsSystemUser(TS ts) {
        this.name = System.getProperty("user.name");
        this.ts = ts;
        this.id = -1;
        this.password = "";
    }

    TsSystemUser(TS ts, int id) {
        this.ts = ts;
        this.id = id;
        this.name = id == 0 ? "NJ4X" : (NJ4X_HOST + id);
        setPassword();
    }

    private void setPassword() {
        String userName = this.name;
        this.password = getPassword(userName);
    }

    private static String getPassword(String userName) {
        StringBuilder p = new StringBuilder();
        for (int i = 0; i < userName.length(); ++i) {
            char c = userName.charAt(i);
            c = (c == '_' ? ',' : c);
            if (i % 2 == 0) {
                p.append(c);
            } else {
                p.append(Character.toLowerCase(c));
            }
            if (i % 2 == 1) {
                p.append(i / 2);
            }
        }
        while (p.length() < 7) p.append(p.toString());
        return p.toString().substring(p.length() > 14 ? p.length() - 14 : 0);
    }

    public static void main(String[] args) throws IOException {
        for (String user : args) {
            System.out.println(user + " : " + getPassword(user));
        }
        System.in.read();
    }
    public boolean exists() {
        try {
            return 0 == new ExternalProcess(TS.NET, "user", name).run(30000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check RDP session,
     * Start RDP session if needed.
     */
    public TsSystemUser getReady() throws IOException {
        if (session == null && id > 0) {
            ts.sessionManager.startSession(this);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TsSystemUser tsSystemUser = (TsSystemUser) o;

        //noinspection RedundantIfStatement
        if (id != tsSystemUser.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public String getName() {
        return name;
    }
}
