//+------------------------------------------------------------------+
//|                                                         wnds.mq4 |
//|                                                Roman Gerasimenko |
//|                                              http://www.nj4x.com |
//+------------------------------------------------------------------+
#property copyright "Roman Gerasimenko"
#property link      "http://www.nj4x.com"
#property version   "1.00"
#property strict

#import "mt45if.dll"
void    jfxLog(string&);
#import

//+------------------------------------------------------------------+
//| Script program start function                                    |
//+------------------------------------------------------------------+
string FindChartSymbol(string cs) {
    string log = "";
    string s = SymbolName(0, true);
    int st = SymbolsTotal(false);
    log = ("wnds: FindChartSymbol: [" + cs + "] s0=" + s + " sTotal=" + st); jfxLog(log);
    for (int i = 0; i < st; i++) {
        string si = SymbolName(i, false);
        if (StringFind(si, cs) >= 0 && SymbolSelect(si, true)) {
            s = si;
            log = ("wnds: FoundChartSymbol: [" + cs + "] s=[" + s + "]"); jfxLog(log);
            return s;
        }
    }
    log = ("wnds: NotFoundChartSymbol: [" + cs + "] returns default -> [" + s + "]"); jfxLog(log);
    return s;
}

void OnStart() {
    string log = "";
    string cs = "";
    ENUM_TIMEFRAMES cp = PERIOD_H4;
    ///*
    int st = SymbolsTotal(false);
    while (st == 0 || AccountNumber() == 0) {
        Sleep(100);
        st = SymbolsTotal(false);
    }
    long chartID = ChartFirst();
    log = ("wnds: " + st + " s0=" + SymbolName(0, false) + " ChartFirst=" + chartID); jfxLog(log);
    while (chartID != -1) {
        //
        cs = ChartSymbol(chartID);
        cp = ChartPeriod(chartID);
        //
        if (StringLen(cs) == 0) {
            chartID = ChartNext(chartID);
            cs = "";
            log = ("wnds: chart symbol hidden, ChartNext=" + chartID); jfxLog(log);
            continue;
        }
        log = ("wnds: Chart: " + chartID + " symbol: [" + cs + "]"); jfxLog(log);
        if (cs != "NJ4X" && !SymbolSelect(cs, true)) {
            log = ("wnds: can not select symbol [" + cs + "]"); jfxLog(log);
            string s = FindChartSymbol(cs);
            if (ChartSetSymbolPeriod(chartID, s, cp)) {
                log = ("wnds: set new symbol [" + s + "] for the chart " + chartID); jfxLog(log);
                chartID = ChartNext(chartID);
                cs = "";
                log = ("wnds: ChartNext=" + chartID); jfxLog(log);
            } else {
                log = ("wnds: set new symbol [" + s + "] for the chart " + chartID + " error: " + GetLastError()); jfxLog(log);
                Sleep(100);
            }
            //chartID = ChartFirst();
            //log = ("wnds: reset first chart ChartFirst=" + chartID); jfxLog(log);
        } else {
            chartID = ChartNext(chartID);
            cs = "";
            log = ("wnds: ChartNext=" + chartID); jfxLog(log);
        }
    }
    log = ("wnds: exit, closing script's chart"); jfxLog(log);
    //*/
    ChartClose(0);
}
//+------------------------------------------------------------------+
