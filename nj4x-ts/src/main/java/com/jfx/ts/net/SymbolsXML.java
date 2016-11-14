package com.jfx.ts.net;

import com.jfx.ts.xml.DOMUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;

/**
* User: roman
* Date: 06/08/2014
* Time: 10:06
*/
public class SymbolsXML {
    private ClientWorker clientWorker;
    private TerminalParams tp;
    private String dir;
    private String sym1;
    private String sym2;

    public SymbolsXML(ClientWorker clientWorker, TerminalParams tp, String dir) throws IOException {
        this.clientWorker = clientWorker;
        this.tp = tp;
        this.dir = dir;
        String symbol = tp.guessSymbol();
        sym1 = sym2 = (symbol == null ? tp.getSymbolFromSrvConfig() : symbol);
        //
    }

    public String getSym1() {
        return sym1 == null ? (clientWorker.ts.dir2symbols.get(dir) == null ? "EURUSD" : clientWorker.ts.dir2symbols.get(dir)[0]) : sym1; // todo
    }

    public String getSym2() {
        return sym2 == null ? (clientWorker.ts.dir2symbols.get(dir) == null ? "GBPUSD" : clientWorker.ts.dir2symbols.get(dir)[1]) : sym2; // todo
    }

    public boolean needUpdate() throws IOException {
        if (tp.isMT5
                || tp.isTesterTerminal()
                || tp.isCustomTerminal()
                || sym1 != null
                /* || clientApiVersion.substring(0, 6).compareTo("2.1.5") >= 0*/) {
            return false;
        }
        String symFileName = clientWorker.getSymbolsXMLFileName(tp);
        try {
            synchronized (TS.symFiles) {
                while (TS.symFiles.contains(symFileName)) {
                    try {
                        TS.symFiles.wait(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                TS.symFiles.add(symFileName);
            }
            File symFile = new File(symFileName);
//                    pause("Before symFile.exists(): " + symFile.getAbsolutePath());
            if (symFile.exists()) {
//                        pause("symFile.exists()");
                //
                Calendar cNow = Calendar.getInstance();
                Calendar cFile = Calendar.getInstance();
                cFile.setTimeInMillis(symFile.lastModified());
                if (cNow.get(Calendar.DAY_OF_YEAR) != cFile.get(Calendar.DAY_OF_YEAR)
                        || cNow.get(Calendar.YEAR) != cFile.get(Calendar.YEAR)
                        ) {
                    return tp.tenant == null || tp.tenant.length() == 0 || sym1 == null || sym2 == null; // true
                }
                //
                Document doc;
                Element eSymbols;
                doc = DOMUtil.getDocument(symFileName);
                eSymbols = DOMUtil.findElement(doc, "symbols");
                Iterator i = DOMUtil.getTopElements(eSymbols, "symbol");
                String p1 = null, p2 = null;
                while (i.hasNext()) {
                    Element e = (Element) i.next();
                    String s = e.getAttribute("name");
                    if (sym1 == null && s.length() < 8 && s.contains("EUR") && s.contains("USD")) {
                        sym1 = s;
                    }
                    if (sym2 == null && s.length() < 8 && s.contains("GBP") && s.contains("USD")) {
                        sym2 = s;
                    }
                    if (p1 == null) {
                        p1 = s;
                    } else if (p2 == null) {
                        p2 = s;
                    }
                }
                sym1 = sym1 == null ? p1 : sym1;
                sym2 = sym2 == null ? p2 : sym2;
                //
                clientWorker.ts.dir2symbols.put(dir, new String[]{sym1, sym2});
                //
                return sym1 == null || sym2 == null;
            } else {
//                        pause("!symFile.exists(), tp.tenant: [" + tp.tenant + "]");
                return tp.tenant == null || tp.tenant.length() == 0 || sym1 == null || sym2 == null;
            }
        } finally {
            synchronized (TS.symFiles) {
                TS.symFiles.remove(symFileName);
            }
        }
    }

    public SymbolsXML synchSymbols() throws IOException {
        String symFileName = clientWorker.getSymbolsXMLFileName(tp);
        try {
            synchronized (TS.symFiles) {
                while (TS.symFiles.contains(symFileName)) {
                    try {
                        TS.symFiles.wait(200);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
                TS.symFiles.add(symFileName);
            }
            File symFile = new File(symFileName);
            Document doc;
            Element eSymbols;
            boolean isChanged = false;
            if (symFile.exists()) {
                doc = DOMUtil.getDocument(symFileName);
                eSymbols = DOMUtil.findElement(doc, "symbols");
            } else {
                isChanged = true;
                doc = DOMUtil.createDocument();
                eSymbols = doc.createElement("symbols");
                doc.appendChild(eSymbols);
            }
            //
            sym1 = null;
            sym2 = null;
            String p1 = null, p2 = null;
            HashSet<String> _s = new HashSet<String>();
            ArrayList<String> symbols = ClientWorker.loadSymbols(dir); //symbol,group
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < symbols.size(); i++) {
                String s = symbols.get(i);
                String sym = s.substring(0, s.indexOf(','));
                _s.add(sym);
                String grp = s.substring(s.indexOf(',') + 1);
                if (sym1 == null && sym.length() < 8 && sym.contains("EUR") && sym.contains("USD")) {
                    sym1 = sym;
                }
                if (sym2 == null && sym.length() < 8 && sym.contains("GBP") && sym.contains("USD")) {
                    sym2 = sym;
                }
                if (p1 == null) {
                    p1 = sym;
                } else if (p2 == null) {
                    p2 = sym;
                }
                //
                Element eSymbol = DOMUtil.findElement(eSymbols, "symbol", "name", sym);
                if (eSymbol == null) {
                    isChanged = true;
                    eSymbol = doc.createElement("symbol");
                    eSymbol.setAttribute("name", sym);
                    eSymbol.setAttribute("group", grp);
                    if (sym.length() >= 6 && !sym.contains("#")) {
                        eSymbol.setAttribute("sym_1", sym.substring(0, 3));
                        eSymbol.setAttribute("sym_2", sym.substring(3, 6));
                    }
                    eSymbols.appendChild(eSymbol);
                }
            }
            sym1 = sym1 == null ? p1 : sym1;
            sym2 = sym2 == null ? p2 : sym2;
            //
            clientWorker.ts.dir2symbols.put(dir, new String[]{sym1, sym2});
            //
            Iterator chk = DOMUtil.getTopElements(eSymbols, "symbol");
            ArrayList<Element> r = new ArrayList<Element>();
            while (chk.hasNext()) {
                Element eSym = (Element) chk.next();
                if (!_s.contains(eSym.getAttribute("name"))) {
                    r.add(eSym);
                }
            }
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < r.size(); i++) {
                Element element = r.get(i);
                eSymbols.removeChild(element);
                isChanged = true;
            }
            //
            if (isChanged) {
                DOMUtil.serializeDocument(doc, new FileOutputStream(symFileName));
            }
            return this;
        } finally {
            synchronized (TS.symFiles) {
                TS.symFiles.remove(symFileName);
            }
        }
    }
}
