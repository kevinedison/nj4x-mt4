//+------------------------------------------------------------------+
//|                                                          jfx.mq4 |
//|                                                                  |
//|                                                JFX Version 0.0.0 |
//+------------------------------------------------------------------+
#property copyright "Copyright (c) 2008-2015 by Gerasimenko Roman"
#property link 		"http://www.nj4x.com"
#property strict

#import "mt45if.dll"
string  jfxConnect(string, int, string, int, string);
void    jfxDisconnect(string&);
int     jfxGetCommand(string&, string& p1, string& p2, string& p3, string& p4, string& p5, string& p6, string& p7, string& p8, string& p9, string& p10, string& p11, string& p12, string& p13, string& p14, string& p15);
void    jfxSendResult(string&, string&);
void    jfxLog(string&);
void    jfxHWnd(int, bool);
void    jfxPositionInit(string&, int);
int     jfxPositionOrderInfo(string&, int, int, int, int, int, int, int, string&, string&, double, double, double, double, double, double, double, double);
string  jfxPositionRes(string&, int, int);
int  	jfxMqlRatesInit(string&);
int  	jfxMqlRatesAdd(string&, MqlRates& rates);
string  jfxMqlRatesRes(string&);
#import

extern string jfxHost = "127.0.0.1";
extern int jfxPort = 7777;
extern string strategy = "JFXExample";
extern datetime TEST_PERIOD_START = D'2008.1.1 00:00';
extern double param1 = 0;
extern double param2 = 0;
extern double param3 = 0;
extern double param4 = 0;
extern double param5 = 0;
extern double param6 = 0;
extern double param7 = 0;
extern double param8 = 0;
extern double param9 = 0;
extern double param10 = 0;
extern bool DEBUG_DLL = false;

bool isAppDriven;
bool listener;
bool auto_listener;
bool testing;
bool isMainChart;
bool isTickChart;
bool isErrorState;
static string version = "2.6.2";
static string NJ4X_UUID = "29a50980516c";
string conn;
bool maxDebug;
bool waitForDisconnect;
int chart = -1;
string args0="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args1="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args2="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args3="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args4="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args5="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args6="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args7="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args8="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args9="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args10="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args11="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args12="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args13="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
string args14="12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

string log = "";

int errorLast;
MqlTick listener_ticks[1000];
int listener_command;
bool IsStartPrinted;


void connect() {
    string symbol = Symbol();
    string s = "";
    if (isAppDriven == false) {
        string params = ":" + param1 +
            "," + param2 +
            "," + param3 +
            "," + param4 +
            "," + param5 +
            "," + param6 +
            "," + param7 +
            "," + param8 +
            "," + param9 +
            "," + param10;
        s = AccountNumber() + "(" + AccountCompany() + ")/" + chart + " MQLv" + version + "@" + strategy + params;
    } else {
        s = AccountNumber() + "(" + AccountCompany() + ") MQLv" + version + "@" + strategy;
    }
    //
    conn = jfxConnect(jfxHost, jfxPort, symbol, Period(), s);
}

int ticks;
int cmdcnt;
bool autoRefresh;

bool isTimer;
int monitorSleepPeriod;
int monitorSleepCount;
int maxSleepPeriod;
string sh;

int OnInit() {
    log = "jfxHost=["+jfxHost +"] jfxPort=["+jfxPort+"]"; jfxLog(log);
	//
	if (IsTesting()) {
		testing = true;
		log = "In tester mode.";
		jfxLog(log);
		Print(log);
	}
    conn = "";
    maxDebug = false;
    waitForDisconnect = false;
    IsStartPrinted = false;
	_ordersHistoryTotal = -1;
    listener_command = 10002;
    errorLast = 0;
    ticks = 0;
    cmdcnt = 0;
    autoRefresh = true;
    //
    isTimer = false;
    monitorSleepPeriod = -1;
    monitorSleepCount = 0;
    maxSleepPeriod = -1;
    sh = "";
    //
    if (chart == -1) {
      if (!GlobalVariableCheck("jfxChart")) GlobalVariableSet("jfxChart", 0);
      if (GlobalVariableGet("jfxChart") > 5000) GlobalVariableSet("jfxChart", 0);
      for (; chart < 10000; chart++) if (GlobalVariableSetOnCondition("jfxChart", chart + 1, chart) == true) break;
    }
    isAppDriven = isJavaDriven(strategy);
    isErrorState = false;
    listener = isAppDriven && param1 == 1;
    auto_listener = false;
    //
    if (!SymbolSelect(Symbol(), true)) {
        log = ("JFX: " + NJ4X_UUID + " " + ChartID() + " " + Symbol() + " skip init=" + strategy + ", acc=" + AccountNumber() + "(" + AccountCompany() + "), timeCurr=" + TimeCurrent() + ", timeLocal=" + TimeLocal() + ", w=" + chart); jfxLog(log);
		Print(log);
        return (INIT_SUCCEEDED);
    }
    //
    while (AccountNumber() == 0) {
        Sleep(10);
    }
    log = ("JFX: " + NJ4X_UUID + " " + ChartID() + " " + Symbol() + " init=" + strategy + ", acc=" + AccountNumber() + "(" + AccountCompany() + "), timeCurr=" + TimeCurrent() + ", timeLocal=" + TimeLocal() + ", w=" + chart); jfxLog(log);
	Print(log);
    //
    maxDebug = (jfxPort == 17342); // no DEBUG_DLL because of mass Print(...)
    //
    int err = 0;
    if (testing == false) {
        int isTradeAllowed = MarketInfo(Symbol(), MODE_TRADEALLOWED);
        if (1 != isTradeAllowed) {
            err = GetLastError();
            log = ("" + strategy + "> init(): " + Symbol() + " trade is not allowed ... err = " + err); jfxLog(log);
			Print(log);
            if (err == 0) {
                err = 133;
            }
        } else {
            log = ("" + strategy + "> init(): " + Symbol() + ", isTradeAllowed=" + isTradeAllowed); jfxLog(log);
			Print(log);
        }
        /*
        if (err == 0) {
            err = 132;
            datetime serverTime = TimeCurrent();
            while (serverTime == 0) {
                Sleep(100);
                serverTime = TimeCurrent();
            }
            for (int t = 0; t < 15; t++) {
                Sleep(100);
                if (serverTime != TimeCurrent()) {
                    log = ("" + strategy + "> init(): " + Symbol() + ", TimeCurrent()=" + TimeCurrent() + ", serverTime=" + serverTime + ", waited=" + (t + 1) / 2 + " sec"); jfxLog(log);
                    err = 0;
                    break;
                }
            }
        }
        */
    }
    //
    connect();
	//
    if (err == 132 || err == 136 || err == 133) {
		if (listener) {
			log = ("" + strategy + "> Market Closed, set timer return from init()"); jfxLog(log);
			Print(log);
//			isTimer = true;
//			EventSetMillisecondTimer(500);
		} else {
			log = ("" + strategy + "> Market Closed, return from init()"); jfxLog(log);
			Print(log);
//			start();
		}
    }
    //
    /*
    isTimer = true;
    EventSetTimer(1);
    while (!EventSetMillisecondTimer(100)) {
        log = ("" + strategy + "> set timer error: " + GetLastError()); jfxLog(log);
        Sleep(100);
    }
    */
    //
    log = ("" + strategy + "> return from init(): " + DayOfWeek() + "/" + Hour() + ":" + Minute()); jfxLog(log);
	Print(log);
    //
    if (!testing)  {
        start();
    }
    //
    return (INIT_SUCCEEDED);
}

bool isJavaDriven(string s) {
    isMainChart = false;
    int len = StringLen(s);
    if (len >= 33) {
        int i = 0;
        for (; i < len; i++) {
            int ch = StringGetChar(s, i);
            if ((ch < '0' || ch > '9') && (ch < 'A' || ch > 'F')) {
                break;
            }
        }
        string cs = StringSubstr(s,33);        
        isTickChart = (i == 32 && (StringFind(Symbol(), cs) >= 0 || StringSubstr(s,33) == "SPCH_T_LSNR" ));
        if (i == 33 || (i == 32 && (StringFind(Symbol(), cs) >= 0 || StringFind(StringSubstr(s,32), "|SPCH") == 0) )) {
			//log = ("IS_JAVA -> TRUE i=" + i + " s=" + s + " substr(>32)=" + StringSubstr(s,33) + " symbol=" + Symbol()); jfxLog(log);
            isMainChart = (i == 33 && StringGetChar(s, 32) == '1');
            if (isMainChart == true) {
                GlobalVariableSet("jfx-in-error-state", 0);
                isErrorState = false;
            }
            return (true);
        } 
        // else {
        //      log = ("IS_JAVA -> FALSE i=" + i + " s=" + s + " substr(>32)=" + StringSubstr(s,33) + " symbol=" + Symbol()); jfxLog(log);
        //}
    }
    for (int i = 0; i < len; i++) {
        int ch = StringGetChar(s, i);
        if (ch != '_' && (ch < '0' || ch > '9')) {
            return (false);
        }
    }
    return (true);
}

void OnDeinit(const int reason) {
	EventKillTimer();
    log = ("JFX: end=" + strategy + ", reason=" + reason + ", time=" + TimeCurrent() + ", w=" + chart); jfxLog(log);
    if (conn != "") jfxDisconnect(conn);
    log = ("" + strategy + "> return from deinit()"); jfxLog(log);
}

int p1[500];
int p2[500];
int p3[500];
int p4[500];
int p5[500];
int p6[500];
int p7[500];
int p8[500];
int p9[500];
int p10[500];
int p11[500];
int p12[500];
int p13[500];
int p14[500];
int p15[500];

int market_infos[28] = {
    MODE_LOW,
    MODE_HIGH,
    MODE_TIME,
    MODE_BID,
    MODE_ASK,
    MODE_POINT,
    MODE_DIGITS,
    MODE_SPREAD,
    MODE_STOPLEVEL,
    MODE_LOTSIZE,
    MODE_TICKVALUE,
    MODE_TICKSIZE,
    MODE_SWAPLONG,
    MODE_SWAPSHORT,
    MODE_STARTING,
    MODE_EXPIRATION,
    MODE_TRADEALLOWED,
    MODE_MINLOT,
    MODE_LOTSTEP,
    MODE_MAXLOT,
    MODE_SWAPTYPE,
    MODE_PROFITCALCMODE,
    MODE_MARGINCALCMODE,
    MODE_MARGININIT,
    MODE_MARGINMAINTENANCE,
    MODE_MARGINHEDGED,
    MODE_MARGINREQUIRED,
    MODE_FREEZELEVEL
};

int _ordersHistoryTotal;
int _ordersTotal;

int saveOrderInfo(bool is_history, bool position) {
    int closeTime = OrderCloseTime();
    if (!is_history && closeTime > 0) {
        return (1); // false live order
    }
	int t = 0;
	while (is_history && closeTime == 0) {
		Sleep(10);
	    closeTime = OrderCloseTime();
		t++;
		if (t > 100) {
		    break;
		}
	}
    if (is_history && closeTime == 0) {
        return (1); // false historical order
    }
	//
    int ticket = OrderTicket();
    int type = OrderType();
    string symbol = OrderSymbol();
    string comment = OrderComment();            
    double lots = OrderLots();
    int magic = OrderMagicNumber();
    double openPrice = OrderOpenPrice();
    int openTime = OrderOpenTime();
    double sl = OrderStopLoss();
    double tp = OrderTakeProfit();
    //
    double commission = 0;
    double swap = 0;
    double profit = 0;
    int expiration = 0;
    double closePrice = 0;
    closePrice = OrderClosePrice();
    if (type == OP_BUY || type == OP_SELL || type > 5) {
        profit = OrderProfit();
        swap = OrderSwap();
        commission = OrderCommission(); 
    } else {
        expiration = OrderExpiration();
    }
    //
    int pool = 2; // orderGet
    if (position) pool = is_history;
    //
    return (jfxPositionOrderInfo(conn, pool, ticket, type, openTime, closeTime, magic, expiration, symbol, comment, lots, openPrice, closePrice, sl, tp, profit, commission, swap));
}

void initPosition() 
{
    while (true) 
    {
        jfxPositionInit(conn, 0);
        _ordersHistoryTotal = OrdersHistoryTotal();
        _ordersTotal = OrdersTotal();
        for (int i = 0; i < _ordersTotal; i++)
        {
            if (OrderSelect(i, SELECT_BY_POS, MODE_TRADES))
            {
                saveOrderInfo(false, true);
            }
        }
        if (_ordersHistoryTotal == OrdersHistoryTotal() && _ordersTotal == OrdersTotal()) 
        {
            break;
        }
    }
    //
    int hStartPos = MathMax(0, _ordersHistoryTotal - 50);
    for (int i = hStartPos; i < _ordersHistoryTotal; i++)
    {
        if (OrderSelect(i, SELECT_BY_POS, MODE_HISTORY))
        {
            saveOrderInfo(true, true);
        }
    }
}

void monitorPosition() {
    if (param2 > 0 && monitorSleepPeriod < 0) {
        monitorSleepPeriod = param2;
    }
    if (param3 > 0 && maxSleepPeriod < 0) {
        maxSleepPeriod = param3;
    }
    if (monitorSleepPeriod < 0) {
        monitorSleepPeriod = 50;
    }
    if (maxSleepPeriod < 0) {
        maxSleepPeriod = 500;
         log = ("MonitorPosition " 
                 + " p1=" + param1
                 + " p2=" + param2
                 + " p3=" + param3
                 + " monitorSleepPeriod=" + monitorSleepPeriod
                 + " maxSleepPeriod=" + maxSleepPeriod
         ); jfxLog(log);
    }
    //
    jfxPositionInit(conn, 1);
    int ordersHistoryTotal = OrdersHistoryTotal();
    int ordersTotal = OrdersTotal();
	bool noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
    while (_ordersHistoryTotal == ordersHistoryTotal && _ordersTotal == ordersTotal && noErrors) 
    {
        int changes = 0;
        for (int m = 0; m < ordersTotal; m++)
        {
            if (OrderSelect(m, SELECT_BY_POS, MODE_TRADES))
            {
                changes = changes + saveOrderInfo(false, true);
            }
        }
        if (changes > 0) {
            monitorSleepCount = 0;
            monitorSleepPeriod = 50;
			ResetLastError();
            return; 
        } else {
            monitorSleepCount++;
            if (monitorSleepCount % 100 == 0) {//5sec,
                monitorSleepPeriod += monitorSleepPeriod;
                monitorSleepPeriod = MathMin(maxSleepPeriod, monitorSleepPeriod);
            }
            Sleep(monitorSleepPeriod);
        }
		RefreshRates();
        ordersHistoryTotal = OrdersHistoryTotal();
        ordersTotal = OrdersTotal();
		noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
    }
	//
	if (noErrors) {
		for (int i = _ordersHistoryTotal; i < ordersHistoryTotal; i++)
		{
			if (OrderSelect(i, SELECT_BY_POS, MODE_HISTORY))
			{
				saveOrderInfo(true, true);
			}
		}
		for (int i = 0; i < ordersTotal; i++)
		{
			if (OrderSelect(i, SELECT_BY_POS, MODE_TRADES))
			{
				saveOrderInfo(false, true);
			}
		}
		//
		_ordersHistoryTotal = ordersHistoryTotal;
		_ordersTotal = ordersTotal;
		//
		ResetLastError();
	}
}

void update_hwnd() {
    if (sh != "" || testing) 
        return;
    ResetLastError();
    int h = WindowHandle(Symbol(), Period());
    if (GetLastError() == 0 && h != 0) {
        string sh = "" + h;
        //
        log = ("hwnd=" + sh); jfxLog(log);
        //
        int f = FileOpen("wnd", FILE_WRITE);
        if (GetLastError() == 0 && f != INVALID_HANDLE) {
            FileWrite(f, sh);
            FileClose(f);
        }
    }
}

void OnTimer() {
    log = ("" + strategy + "> OnTimer(): " + Symbol() + ", TimeCurrent()=" + TimeCurrent() + " isTimer=" + isTimer); jfxLog(log);
	if (isTimer) {
		start();
        ///*
		isTimer = true;
        double serverTime = TimeCurrent();
        for (int t = 0; t < 15; t++) {
            Sleep(500);
            if (serverTime != TimeCurrent()) {
                isTimer = false;
                log = ("" + strategy + "> OnTimer(): " + Symbol() + ", TimeCurrent()=" + TimeCurrent() + ", serverTime=" + serverTime + ", waited=" + (t + 1) / 2 + " sec"); jfxLog(log);
                break;
            }
        }
        //*/
	} else {
	    EventKillTimer();
	}
}

enum ENUM_SYMBOL_CALC_MODE {
    SYMBOL_CALC_MODE_FOREX = 0,
    SYMBOL_CALC_MODE_FUTURES = 1,
    SYMBOL_CALC_MODE_CFD = 2,
    SYMBOL_CALC_MODE_CFDINDEX = 3,
    SYMBOL_CALC_MODE_CFDLEVERAGE = 4,
    SYMBOL_CALC_MODE_EXCH_STOCKS = 32,
    SYMBOL_CALC_MODE_EXCH_FUTURES = 33,
    SYMBOL_CALC_MODE_EXCH_FUTURES_FORTS = 34
};

enum ENUM_SYMBOL_SWAP_MODE {
	SYMBOL_SWAP_MODE_DISABLED = 0,
	SYMBOL_SWAP_MODE_POINTS = 1,
	SYMBOL_SWAP_MODE_CURRENCY_SYMBOL = 2,
	SYMBOL_SWAP_MODE_CURRENCY_MARGIN = 3,
	SYMBOL_SWAP_MODE_CURRENCY_DEPOSIT = 4,
	SYMBOL_SWAP_MODE_INTEREST_CURRENT = 5,
	SYMBOL_SWAP_MODE_INTEREST_OPEN = 6,
	SYMBOL_SWAP_MODE_REOPEN_CURRENT = 7,
	SYMBOL_SWAP_MODE_REOPEN_BID = 8
};

int CalcModeOrderNum(int m) {
    switch (m) {
        case SYMBOL_CALC_MODE_FOREX: return 0;
        case SYMBOL_CALC_MODE_FUTURES: return 1;
        case SYMBOL_CALC_MODE_CFD: return 2;
        case SYMBOL_CALC_MODE_CFDINDEX: return 3;
        case SYMBOL_CALC_MODE_CFDLEVERAGE: return 4;
        case SYMBOL_CALC_MODE_EXCH_STOCKS: return 5;
        case SYMBOL_CALC_MODE_EXCH_FUTURES: return 6;
        case SYMBOL_CALC_MODE_EXCH_FUTURES_FORTS: return 7;
    }
    return -1;
}

int SwapModeOrderNum(int m) {
    switch (m) {
        case SYMBOL_SWAP_MODE_DISABLED: return 0;
        case SYMBOL_SWAP_MODE_POINTS: return 1;
        case SYMBOL_SWAP_MODE_CURRENCY_SYMBOL: return 2;
        case SYMBOL_SWAP_MODE_CURRENCY_MARGIN: return 3;
        case SYMBOL_SWAP_MODE_CURRENCY_DEPOSIT: return 4;
        case SYMBOL_SWAP_MODE_INTEREST_CURRENT: return 5;
        case SYMBOL_SWAP_MODE_INTEREST_OPEN: return 6;
        case SYMBOL_SWAP_MODE_REOPEN_CURRENT: return 7;
        case SYMBOL_SWAP_MODE_REOPEN_BID: return 8;
    }
    return -1;
}

int TradeModeOrderNum(int m) {
    switch (m) {
        case SYMBOL_TRADE_MODE_DISABLED: return 0;
        case SYMBOL_TRADE_MODE_LONGONLY: return 1;
        case SYMBOL_TRADE_MODE_SHORTONLY: return 2;
        case SYMBOL_TRADE_MODE_CLOSEONLY: return 3;
        case SYMBOL_TRADE_MODE_FULL: return 4;
    }
    return -1;
}

int TradeExecutionModeOrderNum(int m) {
    switch (m) {
        case SYMBOL_TRADE_EXECUTION_REQUEST: return 0;
        case SYMBOL_TRADE_EXECUTION_INSTANT: return 1;
        case SYMBOL_TRADE_EXECUTION_MARKET: return 2;
        case SYMBOL_TRADE_EXECUTION_EXCHANGE: return 3;
    }
    return -1;
}

int AccountTradeMode(int m) {
    switch (m) {
        case ACCOUNT_TRADE_MODE_DEMO: return 0;
        case ACCOUNT_TRADE_MODE_CONTEST: return 1;
        case ACCOUNT_TRADE_MODE_REAL: return 2;
    }
    return -1;
}

int AccountStopOutMode(int m) {
    switch (m) {
        case ACCOUNT_STOPOUT_MODE_PERCENT: return 0;
        case ACCOUNT_STOPOUT_MODE_MONEY: return 1;
    }
    return -1;
}

int start() {
    if (!IsStartPrinted || maxDebug) {log = ("" + strategy + "> enter to start(), isTimer=" + isTimer); jfxLog(log);}
    IsStartPrinted = true;
    //
	isTimer = false;
	if (waitForDisconnect) {
        log = ("" + strategy + "> exit from start() 1"); jfxLog(log);
        return (0);
    }
    //
    //
    while (conn == "" && IsStopped() == false) {
        Sleep(1000);
        connect();
    }
    //
    datetime start = TimeCurrent();
    //
    if (testing == true && (start < TEST_PERIOD_START)) {
        log = ("" + strategy + "> exit from start() 2"); jfxLog(log);
        return (0);
    }
    //
    ticks++;
    if (ticks % 10000 == 0 && listener) {
        if (maxDebug) {log = ("" + ticks + " have been processed"); jfxLog(log);}
    }
	int iteration = 0;
    while(IsStopped() == false && (isAppDriven || iteration == 0)) {
		iteration++;
        int x = 0;
        if (listener && auto_listener) {
            bool noErrors = !isErrorState;//(GlobalVariableGet("jfx-in-error-state") == 0);
            if (noErrors) {
                x = listener_command;
            } else {
                auto_listener = false;
                x = jfxGetCommand(conn, args0, args1, args2, args3, args4, args5, args6, args7, args8, args9, args10, args11, args12, args13, args14);
                //log = ("(2) Got Command: " + x); jfxLog(log);
            }
        } else {
            x = jfxGetCommand(conn, args0, args1, args2, args3, args4, args5, args6, args7, args8, args9, args10, args11, args12, args13, args14);
            //log = ("(1) Got Command: " + x); jfxLog(log);
        }
        datetime commandStart = TimeCurrent();

        if (args0 == "ERROR") {
            if (/*isMainChart && */!isErrorState) {
                 GlobalVariableSet("jfx-in-error-state", 1);
                 isErrorState = true;
            }
            Sleep(500);
            continue;
        }
        
        if (isErrorState) {
            GlobalVariableSet("jfx-in-error-state", 0);
            isErrorState = false;
        }

        if (x < 0) {
            //jfxSendResult(conn, "");
            if (listener || testing) {
                if (listener && isTickChart) {
                    auto_listener = true;
                }
				if (!testing && auto_listener && listener_command == 10012) {
				    continue;
				}
                if (maxDebug) {log = ("" + strategy + "> exit from start() 3"); jfxLog(log);}
                return (0);
            } else if (IsStopped()) {
                log = ("! IS_STOPPED !"); jfxLog(log);
                log = ("" + strategy + "> exit from start() 4"); jfxLog(log);
                return (0);
            } else {
               //Sleep(1500);
               continue;
            }
        }

        string res = "";
        int error = 0;
        int errorCopy = -1;
        bool debug = maxDebug;
        int retries = 0;
        while (retries < 60) {
            if (!testing && autoRefresh) {
                //if (maxDebug) {log = ("RefreshRates"); jfxLog(log);}
                RefreshRates();
            }
			ResetLastError();
            switch (x) {
                case 0:
                    res = IntegerToString(iBars(args0,StrToInteger(args1)));
                    if (maxDebug) Print("iBars", ", ", "symbol=", args0,", ", "timeframe=", args1);
                    break;
                case 1:
                    res = IntegerToString( iBarShift(args0,StrToInteger(args1),StrToTime(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iBarShift", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "time=", args2,", ", "exact=", args3);
                    break;
                case 2:
                    res = DoubleToString( iClose(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iClose", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 3:
                    res = DoubleToString( iHigh(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iHigh", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 4:
                    res = DoubleToString( iLow(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iLow", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 5:
                    res = DoubleToString( iOpen(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iOpen", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 6:
                    res = IntegerToString( iVolume(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iVolume", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 7:
                    res = IntegerToString ((int) iTime(args0,StrToInteger(args1),StrToInteger(args2)));
                    if (maxDebug) Print("iTime", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 8:
                    res = IntegerToString( iLowest(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iLowest", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "type=", args2,", ", "count=", args3,", ", "start=", args4);
                    break;
                case 9:
                    res = IntegerToString( iHighest(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iHighest", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "type=", args2,", ", "count=", args3,", ", "start=", args4);
                    break;
                case 10:
                    res = DoubleToString( AccountBalance() );
                    if (maxDebug) Print("AccountBalance", "");
                    break;
                case 11:
                    res = DoubleToString( AccountCredit() );
                    if (maxDebug) Print("AccountCredit", "");
                    break;
                case 12:
                    res = AccountCompany();
                    if (maxDebug) Print("AccountCompany", "");
                    break;
                case 13:
                    res = AccountCurrency();
                    if (maxDebug) Print("AccountCurrency", "");
                    break;
                case 14:
                    res = DoubleToString( AccountEquity() );
                    if (maxDebug) Print("AccountEquity", "");
                    break;
                case 15:
                    res = DoubleToString( AccountFreeMargin() );
                    if (maxDebug) Print("AccountFreeMargin", "");
                    break;
                case 16:
                    res = DoubleToString( AccountMargin() );
                    if (maxDebug) Print("AccountMargin", "");
                    break;
                case 17:
                    res = AccountName();
                    if (maxDebug) Print("AccountName", "");
                    break;
                case 18:
                    res = IntegerToString( AccountNumber() );
                    if (maxDebug) Print("AccountNumber", "");
                    break;
                case 19:
                    res = DoubleToString( AccountProfit() );
                    if (maxDebug) Print("AccountProfit", "");
                    break;
                case 20:
                    res = IntegerToString(errorLast);
                    if (maxDebug) Print("GetLastError", "");
                    break;
                case 21:
                    res = IntegerToString( IsConnected() );
                    if (maxDebug) Print("IsConnected", "");
                    break;
                case 22:
                    res = IntegerToString( IsDemo() );
                    if (maxDebug) Print("IsDemo", "");
                    break;
                case 23:
                    res = IntegerToString( testing );
                    if (maxDebug) Print("IsTesting", "");
                    break;
                case 24:
                    res = IntegerToString( IsVisualMode() );
                    if (maxDebug) Print("IsVisualMode", "");
                    break;
                case 25:
                    res = IntegerToString( GetTickCount() );
                    if (maxDebug) Print("GetTickCount", "");
                    break;
                case 26:
                    Comment(args0);
                    if (maxDebug) Print("Comment", ", ", "comments=", args0);
                    break;
                case 27:
                    res = DoubleToString( MarketInfo(args0,StrToInteger(args1)) );
                    if (maxDebug) Print("MarketInfo", ", ", "symbol=", args0,", ", "type=", args1);
                    break;
                case 28:
                    Print(args0);
                    if (maxDebug) Print("Print", ", ", "comments=", args0);
                    if (args0 == "SW_HIDE" || args0 == "SW_SHOW") {
                        ResetLastError();
                        int h = ((int)ChartGetInteger(ChartID(), CHART_WINDOW_HANDLE));
                        if (GetLastError() == 0 && h != 0) {
                            if (args0 == "SW_HIDE") {
                                jfxHWnd(h, true);
                            } else {
                                jfxHWnd(h, false);
                            }
                        } else {
                            log = (args0 + ": h=" + h + ", lastErr=" + GetLastError()); jfxLog(log);
                        }
                    }
                    if (args0 == "debug=0" || args0 == "debug=1") {
                        log = (args0); jfxLog(log);
                    }
					if (args0 == "rates") {
						MqlRates rates[];
						int copied = CopyRates(NULL, 0, 0, 100, rates);
						if (copied <= 0)
							Print("Error copying price data ",GetLastError());
					    else {
							if (jfxMqlRatesInit(conn) == 1) {
								int sz = ArraySize(rates);
								for (int i = 0; i < sz; i++) {
									jfxMqlRatesAdd(conn, rates[i]);
								}
								res = jfxMqlRatesRes(conn);
							}
						}
					}
                    break;
                case 29:
                    res = IntegerToString( Day() );
                    if (maxDebug) Print("Day", "");
                    break;
                case 30:
                    res = IntegerToString( DayOfWeek() );
                    if (maxDebug) Print("DayOfWeek", "");
                    break;
                case 31:
                    res = IntegerToString( DayOfYear() );
                    if (maxDebug) Print("DayOfYear", "");
                    break;
                case 32:
                    res = IntegerToString( Hour() );
                    if (maxDebug) Print("Hour", "");
                    break;
                case 33:
                    res = IntegerToString( Minute() );
                    if (maxDebug) Print("Minute", "");
                    break;
                case 34:
                    res = IntegerToString( Month() );
                    if (maxDebug) Print("Month", "");
                    break;
                case 35:
                    res = IntegerToString( Seconds() );
                    if (maxDebug) Print("Seconds", "");
                    break;
                case 36:
                    res = IntegerToString((int) TimeCurrent());
                    if (maxDebug) Print("TimeCurrent", "");
                    break;
                case 37:
                    res = IntegerToString( Year() );
                    if (maxDebug) Print("Year", "");
                    break;
                case 38:
                    res = IntegerToString( ObjectCreate(args0,StrToInteger(args1),StrToInteger(args2),StrToTime(args3),StrToDouble(args4),StrToTime(args5),StrToDouble(args6),StrToTime(args7),StrToDouble(args8)) );
                    if (maxDebug) Print("ObjectCreate", ", ", "name=", args0,", ", "type=", args1,", ", "window=", args2,", ", "time1=", args3,", ", "price1=", args4,", ", "time2=", args5,", ", "price2=", args6,", ", "time3=", args7,", ", "price3=", args8);
                    break;
                case 39:
                    res = IntegerToString( ObjectCreate(args0,StrToInteger(args1),StrToInteger(args2),StrToTime(args3),StrToDouble(args4)) );
                    if (maxDebug) Print("ObjectCreate", ", ", "name=", args0,", ", "type=", args1,", ", "window=", args2,", ", "time1=", args3,", ", "price1=", args4);
                    break;
                case 40:
                    res = IntegerToString( ObjectCreate(args0,StrToInteger(args1),StrToInteger(args2),StrToTime(args3),StrToDouble(args4),StrToTime(args5),StrToDouble(args6)) );
                    if (maxDebug) Print("ObjectCreate", ", ", "name=", args0,", ", "type=", args1,", ", "window=", args2,", ", "time1=", args3,", ", "price1=", args4,", ", "time2=", args5,", ", "price2=", args6);
                    break;
                case 41:
                    res = IntegerToString( ObjectDelete(args0) );
                    if (maxDebug) Print("ObjectDelete", ", ", "name=", args0);
                    break;
                case 42:
                    res = DoubleToString( ObjectGet(args0,StrToInteger(args1)) );
                    if (maxDebug) Print("ObjectGet", ", ", "name=", args0,", ", "index=", args1);
                    break;
                case 43:
                    res = IntegerToString( ObjectSet(args0,StrToInteger(args1),StrToDouble(args2)) );
                    if (maxDebug) Print("ObjectSet", ", ", "name=", args0,", ", "index=", args1,", ", "value=", args2);
                    break;
                case 44:
                    res = ObjectGetFiboDescription(args0,StrToInteger(args1));
                    if (maxDebug) Print("ObjectGetFiboDescription", ", ", "name=", args0,", ", "index=", args1);
                    break;
                case 45:
                    res = IntegerToString( ObjectSetFiboDescription(args0,StrToInteger(args1),args2) );
                    if (maxDebug) Print("ObjectSetFiboDescription", ", ", "name=", args0,", ", "index=", args1,", ", "text=", args2);
                    break;
                case 46:
                    res = IntegerToString( ObjectSetText(args0,args1,StrToInteger(args2),args3,StrToInteger(args4)) );
                    if (maxDebug) Print("ObjectSetText", ", ", "name=", args0,", ", "text=", args1,", ", "font_size=", args2,", ", "font=", args3,", ", "text_color=", args4);
                    break;
                case 47:
                    res = IntegerToString( ObjectsTotal(StrToInteger(args0)) );
                    if (maxDebug) Print("ObjectsTotal", ", ", "type=", args0);
                    break;
                case 48:
                    res = IntegerToString( ObjectType(args0) );
                    if (maxDebug) Print("ObjectType", ", ", "name=", args0);
                    break;
                case 49:
                    res = DoubleToString( iAC(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iAC", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 50:
                    res = DoubleToString( iAD(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iAD", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 51:
                    res = DoubleToString( iAlligator(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11)) );
                    if (maxDebug) Print("iAlligator", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "jawPeriod=", args2,", ", "jawShift=", args3,", ", "teethPeriod=", args4,", ", "teethShift=", args5,", ", "lipsPeriod=", args6,", ", "lipsShift=", args7,", ", "maMethod=", args8,", ", "appliedPrice=", args9,", ", "mode=", args10,", ", "shift=", args11);
                    break;
                case 52:
                    res = DoubleToString( iADX(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5)) );
                    if (maxDebug) Print("iADX", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "mode=", args4,", ", "shift=", args5);
                    break;
                case 53:
                    res = DoubleToString( iATR(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iATR", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "shift=", args3);
                    break;
                case 54:
                    res = DoubleToString( iAO(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iAO", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 55:
                    res = DoubleToString( iBearsPower(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iBearsPower", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "shift=", args4);
                    break;
                case 56:
                    res = DoubleToString( iBands(args0,StrToInteger(args1),StrToInteger(args2),StrToDouble(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6),StrToInteger(args7)) );
                    if (maxDebug) Print("iBands", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "deviation=", args3,", ", "bandsShift=", args4,", ", "appliedPrice=", args5,", ", "mode=", args6,", ", "shift=", args7);
                    break;
                case 57:
                    res = DoubleToString( iBullsPower(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iBullsPower", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "shift=", args4);
                    break;
                case 58:
                    res = DoubleToString( iCCI(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iCCI", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "shift=", args4);
                    break;
                case 59:
                    if (args5 != "" && StrToInteger(StringSubstr(args5,1)) > 0) {
                        string type = StringSubstr(args5,0,1);
                        int pCount = StrToInteger(StringSubstr(args5,1));
                        if (type == "i") {
                            switch (pCount) {
                                case 1:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                    );
                                    break;
                                case 2:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                    );
                                    break;
                                case 3:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                    );
                                    break;
                                case 4:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                    );
                                    break;
                                case 5:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                    );
                                    break;
                                case 6:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                    );
                                    break;
                                case 7:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11),StrToInteger(args12), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                    );
                                    break;
                                case 8:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11),StrToInteger(args12),StrToInteger(args13), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                    );
                                    break;
                                default:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11),StrToInteger(args12),StrToInteger(args13),StrToInteger(args14), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                        , ", p9=", args14
                                    );
                                    break;
                            }
                        } else if (type == "d") {
                            switch (pCount) {
                                case 1:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                    );
                                    break;
                                case 2:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                    );
                                    break;
                                case 3:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                    );
                                    break;
                                case 4:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                    );
                                    break;
                                case 5:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9),StrToDouble(args10), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                    );
                                    break;
                                case 6:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9),StrToDouble(args10),StrToDouble(args11), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                    );
                                    break;
                                case 7:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9),StrToDouble(args10),StrToDouble(args11),StrToDouble(args12), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                    );
                                    break;
                                case 8:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9),StrToDouble(args10),StrToDouble(args11),StrToDouble(args12),StrToDouble(args13), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                    );
                                    break;
                                default:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, StrToDouble(args6),StrToDouble(args7),StrToDouble(args8),StrToDouble(args9),StrToDouble(args10),StrToDouble(args11),StrToDouble(args12),StrToDouble(args13),StrToDouble(args14), StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                        , ", p9=", args14
                                    );
                                    break;
                            }
                        } else {
                            switch (pCount) {
                                case 1:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                    );
                                    break;
                                case 2:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                    );
                                    break;
                                case 3:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                    );
                                    break;
                                case 4:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                    );
                                    break;
                                case 5:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9,args10, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                    );
                                    break;
                                case 6:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9,args10,args11, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                    );
                                    break;
                                case 7:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9,args10,args11,args12, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                    );
                                    break;
                                case 8:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9,args10,args11,args12,args13, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                    );
                                    break;
                                default:
                                    res = DoubleToString( iCustom(args0,StrToInteger(args1),args2, args6,args7,args8,args9,args10,args11,args12,args13,args14, StrToInteger(args3),StrToInteger(args4)) );
                                    if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4
                                        , ", p1=", args6
                                        , ", p2=", args7
                                        , ", p3=", args8
                                        , ", p4=", args9
                                        , ", p5=", args10
                                        , ", p6=", args11
                                        , ", p7=", args12
                                        , ", p8=", args13
                                        , ", p9=", args14
                                    );
                                    break;
                            }
                        }
                    } else {
                        res = DoubleToString( iCustom(args0,StrToInteger(args1),args2,StrToInteger(args3),StrToInteger(args4)) );
                        if (maxDebug) Print("iCustom", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "name=", args2,", ", "mode=", args3,", ", "shift=", args4);
                    }
                    break;
                case 60:
                    res = DoubleToString( iDeMarker(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iDeMarker", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "shift=", args3);
                    break;
                case 61:
                    res = DoubleToString( iEnvelopes(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToDouble(args6),StrToInteger(args7),StrToInteger(args8)) );
                    if (maxDebug) Print("iEnvelopes", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "maPeriod=", args2,", ", "maMethod=", args3,", ", "maShitf=", args4,", ", "appliedPrice=", args5,", ", "deviation=", args6,", ", "mode=", args7,", ", "shift=", args8);
                    break;
                case 62:
                    res = DoubleToString( iForce(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5)) );
                    if (maxDebug) Print("iForce", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "maMethod=", args3,", ", "appliedPrice=", args4,", ", "shift=", args5);
                    break;
                case 63:
                    res = DoubleToString( iFractals(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iFractals", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "mode=", args2,", ", "shift=", args3);
                    break;
                case 64:
                    res = DoubleToString( iGator(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6),StrToInteger(args7),StrToInteger(args8),StrToInteger(args9),StrToInteger(args10),StrToInteger(args11)) );
                    if (maxDebug) Print("iGator", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "jawPeriod=", args2,", ", "jawShift=", args3,", ", "teethPeriod=", args4,", ", "teethShift=", args5,", ", "lipsPeriod=", args6,", ", "lipsShift=", args7,", ", "maMethod=", args8,", ", "appliedPrice=", args9,", ", "mode=", args10,", ", "shift=", args11);
                    break;
                case 65:
                    res = DoubleToString( iBWMFI(args0,StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("iBWMFI", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "shift=", args2);
                    break;
                case 66:
                    res = DoubleToString( iMomentum(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iMomentum", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "shift=", args4);
                    break;
                case 67:
                    res = DoubleToString( iMFI(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iMFI", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "shift=", args3);
                    break;
                case 68:
                    res = DoubleToString( iMA(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6)) );
                    if (maxDebug) Print("iMA", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "maShift=", args3,", ", "maMethod=", args4,", ", "appliedPrice=", args5,", ", "shift=", args6);
                    break;
                case 69:
                    res = DoubleToString( iOsMA(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6)) );
                    if (maxDebug) Print("iOsMA", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "fastEMAPeriod=", args2,", ", "slowEMAPeriod=", args3,", ", "signalPeriod=", args4,", ", "appliedPrice=", args5,", ", "shift=", args6);
                    break;
                case 70:
                    res = DoubleToString( iMACD(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6),StrToInteger(args7)) );
                    if (maxDebug) Print("iMACD", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "fastEMAPeriod=", args2,", ", "slowEMAPeriod=", args3,", ", "signalPeriod=", args4,", ", "appliedPrice=", args5,", ", "mode=", args6,", ", "shift=", args7);
                    break;
                case 71:
                    res = DoubleToString( iOBV(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iOBV", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "appliedPrice=", args2,", ", "shift=", args3);
                    break;
                case 72:
                    res = DoubleToString( iSAR(args0,StrToInteger(args1),StrToDouble(args2),StrToDouble(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iSAR", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "step=", args2,", ", "maximum=", args3,", ", "shift=", args4);
                    break;
                case 73:
                    res = DoubleToString( iRSI(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iRSI", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "appliedPrice=", args3,", ", "shift=", args4);
                    break;
                case 74:
                    res = DoubleToString( iRVI(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4)) );
                    if (maxDebug) Print("iRVI", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "mode=", args3,", ", "shift=", args4);
                    break;
                case 75:
                    res = DoubleToString( iStdDev(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6)) );
                    if (maxDebug) Print("iStdDev", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "maPeriod=", args2,", ", "maShift=", args3,", ", "maMethod=", args4,", ", "appliedPrice=", args5,", ", "shift=", args6);
                    break;
                case 76:
                    res = DoubleToString( iStochastic(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6),StrToInteger(args7),StrToInteger(args8)) );
                    if (maxDebug) Print("iStochastic", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "kPeriod=", args2,", ", "dPeriod=", args3,", ", "slowing=", args4,", ", "maMethod=", args5,", ", "priceField=", args6,", ", "mode=", args7,", ", "shift=", args8);
                    break;
                case 77:
                    res = DoubleToString( iWPR(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3)) );
                    if (maxDebug) Print("iWPR", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "period=", args2,", ", "shift=", args3);
                    break;
                case 78: {
						int orderCloseTicket = StrToInteger(args0);
						double orderClosePrice = StrToDouble(args2);
						double orderCloseSlippage = StrToInteger(args3);
						   double orderClosePoint = 0;
						   double orderCloseTerminalPrice = 0;
						double orderClosePriceDistance = 0;
						int orderCloseSlippageSign = 1;
						//
						if (orderCloseSlippage >= 0 && OrderSelect(orderCloseTicket, SELECT_BY_TICKET, MODE_TRADES)) {
							string orderCloseSymbol = OrderSymbol();                        
							int orderCloseTradeOp = OrderType();
							
							orderClosePoint = MarketInfo(orderCloseSymbol, MODE_POINT);
							orderCloseTerminalPrice = 0;
							switch (orderCloseTradeOp) {
								case OP_BUY: 
									orderCloseTerminalPrice = MarketInfo(orderCloseSymbol, MODE_BID); // close by opposite price
									if (orderCloseTerminalPrice < orderClosePrice) {
										orderCloseSlippageSign = -1; // worse price
									}
									break;
								case OP_SELL:
									orderCloseTerminalPrice = MarketInfo(orderCloseSymbol, MODE_ASK);
									if (orderCloseTerminalPrice > orderClosePrice) {
										orderCloseSlippageSign = -1; // worse price
									}
									break;
							}
							//
							//orderClosePriceDistance = (MathAbs(orderCloseTerminalPrice - orderClosePrice) / orderClosePoint);
							//if (orderClosePriceDistance <= orderCloseSlippage) {
							//    orderClosePrice = orderCloseTerminalPrice;
							//    orderCloseSlippage = (orderCloseSlippage - orderClosePriceDistance);
							//}

							orderClosePriceDistance = orderCloseTerminalPrice - orderClosePrice;
							/*jfxLog("Closing order #" + args0 
									+ " p=" + orderClosePrice 
									+ " t=" + orderCloseTerminalPrice 
									+ " point=" + orderClosePoint
									+ " dist=" + orderClosePriceDistance
							);*/
							if (orderClosePriceDistance < 0.0) {
								orderClosePriceDistance = -orderClosePriceDistance;
							}
							if (orderClosePriceDistance > 0.0 && (orderClosePriceDistance <= orderCloseSlippage * orderClosePoint || orderCloseSlippageSign > 0)) {
								orderClosePrice = orderCloseTerminalPrice;
								orderCloseSlippage = (orderCloseSlippage + orderCloseSlippageSign * orderClosePriceDistance / orderClosePoint);
							}
						}
						//
						res = IntegerToString( OrderClose(orderCloseTicket, StrToDouble(args1), orderClosePrice, orderCloseSlippage, StrToInteger(args4)) );
						errorCopy = GetLastError();

						if (true || maxDebug || errorCopy != 0) {
							if (maxDebug) Print("OrderClose", ", ", "ticket=", orderCloseTicket, ", ", "lots=", args1,", ", "price=", orderClosePrice,", ", "slippage=", args3,", ", "arrowColor=", args4);
							if (errorCopy == 0 && orderClosePoint != 0 && OrderSelect(orderCloseTicket, SELECT_BY_TICKET, MODE_HISTORY)) 
							{
								int order_close_exec_slip = (MathAbs(OrderClosePrice() - orderClosePrice) / orderClosePoint);
								log = ("ORDER_CLOSE " + OrderComment() + " " + args0 
										+ " res=" + res
										+ " err=" + errorCopy
										+ " price: ask=" + args2 + " terminal:" + orderCloseTerminalPrice
										+ " (diff=" + orderClosePriceDistance + ")"
										+ " price: used=" + orderClosePrice  + " executed:" + OrderClosePrice()
										+ " (exec_slip=" + order_close_exec_slip + " max_slip=" + orderCloseSlippage + ")"
										+ " slip: in=" + args3 + " used=" + orderCloseSlippage
										+ " point=" + orderClosePoint
								); jfxLog(log);
							} else {
								log = ("ORDER_CLOSE " + args0 
										+ " res=" + res
										+ " err=" + errorCopy
										+ " price: ask=" + args2 + " used=" + orderClosePrice + " terminal:" + orderCloseTerminalPrice
										+ " (diff=" + orderClosePriceDistance + " <== " + orderCloseTerminalPrice + "-" + args2  + ")"
										+ " slip=" + args3 + " -> " + orderCloseSlippage
								); jfxLog(log);
							}
						}
					}
                    break;
                case 79:
                    res = "" + OrderCloseBy(StrToInteger(args0),StrToInteger(args1),StrToInteger(args2));
                    if (maxDebug) Print("OrderCloseBy", ", ", "ticket=", args0,", ", "opposite=", args1,", ", "arrowColor=", args2);
                    break;
                case 80:
                    res = DoubleToString( OrderClosePrice() );
                    if (maxDebug) Print("OrderClosePrice", "");
                    break;
                case 81:
                    res = IntegerToString((int) OrderCloseTime());
                    if (maxDebug) Print("OrderCloseTime", "");
                    break;
                case 82:
                    res = OrderComment();
                    if (maxDebug) Print("OrderComment", "");
                    break;
                case 83:
                    res = DoubleToString( OrderCommission() );
                    if (maxDebug) Print("OrderCommission", "");
                    break;
                case 84:
                    res = IntegerToString( OrderDelete(StrToInteger(args0),StrToInteger(args1)) );
                    if (maxDebug) Print("OrderDelete", ", ", "ticket=", args0,", ", "arrowColor=", args1);
                    break;
                case 85:
                    res = IntegerToString( OrderExpiration() );
                    if (maxDebug) Print("OrderExpiration", "");
                    break;
                case 86:
                    res = DoubleToString( OrderLots() );
                    if (maxDebug) Print("OrderLots", "");
                    break;
                case 87:
                    res = IntegerToString( OrderMagicNumber() );
                    if (maxDebug) Print("OrderMagicNumber", "");
                    break;
                case 88:
                    res = IntegerToString( OrderModify(StrToInteger(args0),StrToDouble(args1),StrToDouble(args2),StrToDouble(args3),StrToTime(args4),StrToInteger(args5)) );
                    if (maxDebug) Print("OrderModify", ", ", "ticket=", args0,", ", "price=", args1,", ", "stoploss=", args2,", ", "takeprofit=", args3,", ", "expiration=", args4,", ", "arrowColor=", args5);
                    break;
                case 89:
                    res = DoubleToString( OrderOpenPrice() );
                    if (maxDebug) Print("OrderOpenPrice", "");
                    break;
                case 90:
                    res = IntegerToString((int) OrderOpenTime());
                    if (maxDebug) Print("OrderOpenTime", "");
                    break;
                case 91:
                    OrderPrint();
                    if (maxDebug) Print("OrderPrint", "");
                    break;
                case 92:
                    res = DoubleToString( OrderProfit() );
                    if (maxDebug) Print("OrderProfit", "");
                    break;
                case 93:
                    res = IntegerToString( OrderSelect(StrToInteger(args0),StrToInteger(args1),StrToInteger(args2)) );
                    if (maxDebug) Print("OrderSelect", ", ", "index=", args0,", ", "select=", args1,", ", "pool=", args2);
                    break;
                case 94: {
						int orderSendTradeOp = StrToInteger(args1);
						double orderSendPrice = StrToDouble(args3);
						double orderSlippage = StrToDouble(args4);
						bool canModifySlippage = orderSlippage < 0;
						orderSlippage = canModifySlippage ? -orderSlippage : orderSlippage;
						double orderPriceDistance = 0;
						double orderTerminalPrice = 0;
						double orderPoint = 0;
						int orderSendSlippageSign = 1;
						if (canModifySlippage && (orderSendTradeOp == OP_BUY || orderSendTradeOp == OP_SELL)) {
							orderPoint = MarketInfo(args0, MODE_POINT);
							switch (orderSendTradeOp) {
								case OP_BUY: 
									orderTerminalPrice = MarketInfo(args0, MODE_ASK);
									if (orderTerminalPrice > orderSendPrice) {
										orderSendSlippageSign = -1; // worse price
									}
									break;
								case OP_SELL:
									orderTerminalPrice = MarketInfo(args0, MODE_BID);
									if (orderTerminalPrice < orderSendPrice) {
										orderSendSlippageSign = -1; // worse price
									}
									break;
							}
							//
							//orderPriceDistance = (MathAbs(orderTerminalPrice - orderSendPrice) / orderPoint);
							orderPriceDistance = orderTerminalPrice - orderSendPrice;
							if (orderPriceDistance < 0.0) {
								orderPriceDistance = -orderPriceDistance;
							}
							if (orderPriceDistance > 0.0 && (orderPriceDistance <= orderSlippage * orderPoint || orderSendSlippageSign > 0)) {
								orderSendPrice = orderTerminalPrice;
								orderSlippage = (orderSlippage + orderSendSlippageSign * orderPriceDistance / orderPoint);
							}
							if (orderSlippage < 0) {
								orderSlippage = 0;
							}
						}
						res = IntegerToString( OrderSend(args0, orderSendTradeOp, StrToDouble(args2), orderSendPrice, orderSlippage, StrToDouble(args5),StrToDouble(args6),args7,StrToInteger(args8),StrToTime(args9),StrToInteger(args10)) );
						errorCopy = GetLastError();
						if (true || maxDebug || errorCopy != 0) {
							if (maxDebug) Print("OrderSend", ", ", "symbol=", args0,", ", "cmd=", args1,", ", "volume=", args2,", ", "price=", args3,", ", "slippage=", args4,", ", "stoploss=", args5,", ", "takeprofit=", args6,", ", "comment=", args7,", ", "magic=", args8,", ", "expiration=", args9,", ", "arrowColor=", args10);
							if (errorCopy == 0 && orderPoint != 0 && OrderSelect(StrToInteger(res), SELECT_BY_TICKET, MODE_TRADES)) 
							{
								int order_send_exec_slip = (MathAbs(OrderOpenPrice() - orderSendPrice) / orderPoint);
								log = ("ORDER_SEND " + args7 + " " + args0 
										+ " t=" + res
										+ " err=" + errorCopy
										+ " price: ask=" + args3 + " terminal:" + orderTerminalPrice
										+ " (diff=" + orderPriceDistance + ")"
										+ " price: used=" + orderSendPrice  + " executed:" + OrderOpenPrice()
										+ " (exec_slip=" + order_send_exec_slip + " max_slip=" + orderSlippage + ")"
										+ " slip: in=" + args4 + " used=" + orderSlippage
										+ " point=" + orderPoint
								); jfxLog(log);
							} else {
								log = ("ORDER_SEND " + args7 + " " + args0 
										+ " t=" + res
										+ " err=" + errorCopy
										+ " price: ask=" + args3 + " used=" + orderSendPrice + " terminal:" + orderTerminalPrice
										+ " (diff=" + orderPriceDistance + " <== " + orderTerminalPrice + "-" + args3  + ")"
										+ " slip=" + args4 + " -> " + orderSlippage
								); jfxLog(log);
							}
						}
					}
                    break;
                case 95:
                    res = IntegerToString( OrdersHistoryTotal() );
                    if (maxDebug) Print("OrdersHistoryTotal", "");
                    break;
                case 96:
                    res = DoubleToString( OrderStopLoss() );
                    if (maxDebug) Print("OrderStopLoss", "");
                    break;
                case 97:
                    res = IntegerToString( OrdersTotal() );
                    if (maxDebug) Print("OrdersTotal", "");
                    break;
                case 98:
                    res = DoubleToString( OrderSwap() );
                    if (maxDebug) Print("OrderSwap", "");
                    break;
                case 99:
                    res = OrderSymbol();
                    if (maxDebug) Print("OrderSymbol", "");
                    break;
                case 100:
                    res = DoubleToString( OrderTakeProfit() );
                    if (maxDebug) Print("OrderTakeProfit", "");
                    break;
                case 101:
                    res = IntegerToString( OrderTicket() );
                    if (maxDebug) Print("OrderTicket", "");
                    break;
                case 102:
                    res = IntegerToString( OrderType() );
                    if (maxDebug) Print("OrderType", "");
                    break;
                case 103:
                    res = IntegerToString( IsTradeContextBusy() );
                    if (maxDebug) Print("IsTradeContextBusy", "");
                    break;
                case 104:
                    res = IntegerToString( RefreshRates() );
                    if (maxDebug) Print("RefreshRates", "");
                    break;
                case 105:
                    res = IntegerToString( AccountStopoutLevel() );
                    if (maxDebug) Print("AccountStopoutLevel", "");
                    break;
                case 106:
                    res = IntegerToString( AccountStopoutMode() );
                    if (maxDebug) Print("AccountStopoutMode", "");
                    break;
                case 107:
                    res = "" + MessageBox(args0,args1,StrToInteger(args2));
                    if (maxDebug) Print("MessageBox", ", ", "text=", args0,", ", "caption=", args1,", ", "flags=", args2);
                    break;
                case 108:
                    res = IntegerToString( UninitializeReason() );
                    if (maxDebug) Print("UninitializeReason", "");
                    break;
                case 109:
                    res = IntegerToString( IsTradeAllowed() );
                    if (maxDebug) Print("IsTradeAllowed", "");
                    break;
                case 110:
                    res = IntegerToString( IsStopped() );
                    if (maxDebug) Print("IsStopped", "");
                    break;
                case 111:
                    res = IntegerToString( IsOptimization() );
                    if (maxDebug) Print("IsOptimization", "");
                    break;
                case 112:
                    res = IntegerToString( IsLibrariesAllowed() );
                    if (maxDebug) Print("IsLibrariesAllowed", "");
                    break;
                case 113:
                    res = IntegerToString( IsDllsAllowed() );
                    if (maxDebug) Print("IsDllsAllowed", "");
                    break;
                case 114:
                    res = IntegerToString( IsExpertEnabled() );
                    if (maxDebug) Print("IsExpertEnabled", "");
                    break;
                case 115:
                    res = DoubleToString( AccountFreeMarginCheck(args0,StrToInteger(args1),StrToDouble(args2)) );
                    if (maxDebug) Print("AccountFreeMarginCheck", ", ", "symbol=", args0,", ", "cmd=", args1,", ", "volume=", args2);
                    break;
                case 116:
                    res = IntegerToString( AccountFreeMarginMode() );
                    if (maxDebug) Print("AccountFreeMarginMode", "");
                    break;
                case 117:
                    res = IntegerToString( AccountLeverage() );
                    if (maxDebug) Print("AccountLeverage", "");
                    break;
                case 118:
                    res = AccountServer();
                    if (maxDebug) Print("AccountServer", "");
                    break;
                case 119:
                    res = TerminalCompany();
                    if (maxDebug) Print("TerminalCompany", "");
                    break;
                case 120:
                    res = TerminalName();
                    if (maxDebug) Print("TerminalName", "");
                    break;
                case 121:
                    res = TerminalPath();
                    if (maxDebug) Print("TerminalPath", "");
                    break;
                case 122:
                    Alert(args0);
                    if (maxDebug) Print("Alert", ", ", "arg=", args0);
                    break;
                case 123:
                    PlaySound(args0);
                    if (maxDebug) Print("PlaySound", ", ", "filename=", args0);
                    break;
                case 124:
                    res = ObjectDescription(args0);
                    if (maxDebug) Print("ObjectDescription", ", ", "name=", args0);
                    break;
                case 125:
                    res = IntegerToString( ObjectFind(args0) );
                    if (maxDebug) Print("ObjectFind", ", ", "name=", args0);
                    break;
                case 126:
                    res = IntegerToString( ObjectGetShiftByValue(args0,StrToDouble(args1)) );
                    if (maxDebug) Print("ObjectGetShiftByValue", ", ", "name=", args0,", ", "value=", args1);
                    break;
                case 127:
                    res = DoubleToString( ObjectGetValueByShift(args0,StrToInteger(args1)) );
                    if (maxDebug) Print("ObjectGetValueByShift", ", ", "name=", args0,", ", "shift=", args1);
                    break;
                case 128:
                    res = IntegerToString( ObjectMove(args0,StrToInteger(args1),StrToTime(args2),StrToDouble(args3)) );
                    if (maxDebug) Print("ObjectMove", ", ", "name=", args0,", ", "point=", args1,", ", "time1=", args2,", ", "price1=", args3);
                    break;
                case 129:
                    res = ObjectName(StrToInteger(args0));
                    if (maxDebug) Print("ObjectName", ", ", "index=", args0);
                    break;
                case 130:
                    res = IntegerToString( ObjectsDeleteAll(StrToInteger(args0),StrToInteger(args1)) );
                    if (maxDebug) Print("ObjectsDeleteAll", ", ", "window=", args0,", ", "type=", args1);
                    break;
                case 131:
                    res = DoubleToString( iIchimoku(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5),StrToInteger(args6)) );
                    if (maxDebug) Print("iIchimoku", ", ", "symbol=", args0,", ", "timeframe=", args1,", ", "tenkan_sen=", args2,", ", "kijun_sen=", args3,", ", "senkou_span_b=", args4,", ", "mode=", args5,", ", "shift=", args6);
                    break;
                case 132:
                    HideTestIndicators(StrToInteger(args0));
                    if (maxDebug) Print("HideTestIndicators", ", ", "shift=", args0);
                    break;
                case 133:
                    res = IntegerToString( Period() );
                    if (maxDebug) Print("Period", "");
                    break;
                case 134:
                    res = Symbol();
                    if (maxDebug) Print("Symbol", "");
                    break;
                case 135:
                    res = IntegerToString( WindowBarsPerChart() );
                    if (maxDebug) Print("WindowBarsPerChart", "");
                    break;
                case 136:
                    res = IntegerToString( WindowFirstVisibleBar() );
                    if (maxDebug) Print("WindowFirstVisibleBar", "");
                    break;
                case 137:
                    res = WindowExpertName();
                    if (maxDebug) Print("WindowExpertName", "");
                    break;
                case 138:
                    res = IntegerToString( WindowFind(args0) );
                    if (maxDebug) Print("WindowFind", ", ", "name=", args0);
                    break;
                case 139:
                    res = IntegerToString( WindowIsVisible(StrToInteger(args0)) );
                    if (maxDebug) Print("WindowIsVisible", ", ", "index=", args0);
                    break;
                case 140:
                    res = DoubleToString( WindowPriceMax(StrToInteger(args0)) );
                    if (maxDebug) Print("WindowPriceMax", ", ", "index=", args0);
                    break;
                case 141:
                    res = IntegerToString( WindowPriceMin(StrToInteger(args0)) );
                    if (maxDebug) Print("WindowPriceMin", ", ", "index=", args0);
                    break;
                case 142:
                    res = IntegerToString( WindowOnDropped() );
                    if (maxDebug) Print("WindowOnDropped", "");
                    break;
                case 143:
                    res = IntegerToString( WindowXOnDropped() );
                    if (maxDebug) Print("WindowXOnDropped", "");
                    break;
                case 144:
                    res = IntegerToString( WindowYOnDropped() );
                    if (maxDebug) Print("WindowYOnDropped", "");
                    break;
                case 145:
                    res = DoubleToString( WindowPriceOnDropped() );
                    if (maxDebug) Print("WindowPriceOnDropped", "");
                    break;
                case 146:
                    res = IntegerToString((int) WindowTimeOnDropped());
                    if (maxDebug) Print("WindowTimeOnDropped", "");
                    break;
                case 147:
                    res = IntegerToString( WindowsTotal() );
                    if (maxDebug) Print("WindowsTotal", "");
                    break;
                case 148:
                    WindowRedraw();
                    if (maxDebug) Print("WindowRedraw", "");
                    break;
                case 149:
                    res = IntegerToString( WindowScreenShot(args0,StrToInteger(args1),StrToInteger(args2),StrToInteger(args3),StrToInteger(args4),StrToInteger(args5)) );
                    if (maxDebug) Print("WindowScreenShot", ", ", "filename=", args0,", ", "sizeX=", args1,", ", "sizeY=", args2,", ", "startBar=", args3,", ", "chartScale=", args4,", ", "chartMode=", args5);
                    break;
                case 150:
                    res = IntegerToString( WindowHandle(args0,StrToInteger(args1)) );
                    if (maxDebug) Print("WindowHandle", ", ", "symbol=", args0,", ", "timeframe=", args1);
                    break;
                case 151:
                    res = IntegerToString( GlobalVariableCheck(args0) );
                    if (maxDebug) Print("GlobalVariableCheck", ", ", "name=", args0);
                    break;
                case 152:
                    res = IntegerToString( GlobalVariableDel(args0) );
                    if (maxDebug) Print("GlobalVariableDel", ", ", "name=", args0);
                    break;
                case 153:
                    res = DoubleToString( GlobalVariableGet(args0) );
                    if (maxDebug) Print("GlobalVariableGet", ", ", "name=", args0);
                    break;
                case 154:
                    res = GlobalVariableName(StrToInteger(args0));
                    if (maxDebug) Print("GlobalVariableName", ", ", "index=", args0);
                    break;
                case 155:
                    res = IntegerToString( GlobalVariableSet(args0,StrToDouble(args1)) );
                    if (maxDebug) Print("GlobalVariableSet", ", ", "name=", args0,", ", "value=", args1);
                    break;
                case 156:
                    res = IntegerToString( GlobalVariableSetOnCondition(args0,StrToDouble(args1),StrToDouble(args2)) );
                    if (maxDebug) Print("GlobalVariableSetOnCondition", ", ", "name=", args0,", ", "value=", args1,", ", "check_value=", args2);
                    break;
                case 157:
                    res = IntegerToString( GlobalVariablesDeleteAll(args0) );
                    if (maxDebug) Print("GlobalVariablesDeleteAll", ", ", "prefix=", args0);
                    break;
                case 158:
                    res = IntegerToString( GlobalVariablesTotal() );
                    if (maxDebug) Print("GlobalVariablesTotal", "");
                    break;
                case 159:
                    res = IntegerToString(SymbolsTotal(StringToInteger(args0)));
                    if (maxDebug) Print("SymbolsTotal", ", ", "selected=", args0);
					break;
                case 160:
                    res = SymbolName(StringToInteger(args0), StringToInteger(args1));
                    if (maxDebug) Print("SymbolName", ", ", "pos=", args0, ", ", "selected=", args1);
                    break;
                case 161:
                    res = IntegerToString(SymbolSelect(args0, StringToInteger(args1)));
                    if (maxDebug) Print("SymbolSelect", ", ", "symbol=", args0, ", ", "select=", args1);
					errorCopy = GetLastError();
					if (errorCopy == 4220) {//ERR_MARKET_SELECT_ERROR
					    errorCopy = 0;
					}
                    if (errorCopy == 4106) {//ERR_UNKNOWN_SYMBOL
                        errorCopy = 0;
                    }
                    break;
                case 162:
                    if (maxDebug) Print("TerminalClose", ", ", "exit_code=", args0);
					res = IntegerToString(TerminalClose(StringToInteger(args0)));
                    break;
                case 163: {
                    if (maxDebug) Print("SymbolInfo", ", ", "symbol=", args0);
					res = "";
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SELECT))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SPREAD_FLOAT))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SESSION_DEALS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SESSION_BUY_ORDERS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SESSION_SELL_ORDERS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_VOLUME))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_VOLUMEHIGH))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_VOLUMELOW))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_DIGITS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SPREAD))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_TRADE_STOPS_LEVEL))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_TRADE_FREEZE_LEVEL))); StringAdd(res, "|");
					//
					StringAdd(res, IntegerToString(CalcModeOrderNum(SymbolInfoInteger(args0, SYMBOL_TRADE_CALC_MODE)))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(TradeModeOrderNum(SymbolInfoInteger(args0, SYMBOL_TRADE_MODE)))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SwapModeOrderNum(SymbolInfoInteger(args0, SYMBOL_SWAP_MODE)))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_SWAP_ROLLOVER3DAYS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(TradeExecutionModeOrderNum(SymbolInfoInteger(args0, SYMBOL_TRADE_EXEMODE)))); StringAdd(res, "|");
					//
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_TIME))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_START_TIME))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_EXPIRATION_TIME))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_EXPIRATION_MODE))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_FILLING_MODE))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(SymbolInfoInteger(args0, SYMBOL_ORDER_MODE))); StringAdd(res, "|");
					//
					StringAdd(res, SymbolInfoString(args0, SYMBOL_CURRENCY_BASE)); StringAdd(res, "|");
					StringAdd(res, SymbolInfoString(args0, SYMBOL_CURRENCY_PROFIT)); StringAdd(res, "|");
					StringAdd(res, SymbolInfoString(args0, SYMBOL_CURRENCY_MARGIN)); StringAdd(res, "|");
					string tmp = SymbolInfoString(args0, SYMBOL_DESCRIPTION);
					StringReplace(tmp, "|", "/");
					StringAdd(res, tmp); StringAdd(res, "|");
					//StringAdd(res, SymbolInfoString(args0, SYMBOL_PATH)); StringAdd(res, "|");
					tmp = SymbolInfoString(args0, SYMBOL_PATH);
					StringReplace(tmp, "|", "/");
					StringAdd(res, tmp); StringAdd(res, "|");
					//
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_BID))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_BIDHIGH))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_BIDLOW))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_ASK))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_ASKHIGH))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_ASKLOW))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_LAST))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_LASTHIGH))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_LASTLOW))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_POINT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_TRADE_TICK_VALUE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_TRADE_TICK_VALUE_PROFIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_TRADE_TICK_VALUE_LOSS))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_TRADE_TICK_SIZE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_TRADE_CONTRACT_SIZE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_VOLUME_MIN))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_VOLUME_MAX))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_VOLUME_STEP))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_VOLUME_LIMIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SWAP_LONG))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SWAP_SHORT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_INITIAL))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_MAINTENANCE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_LONG))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_SHORT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_LIMIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_STOP))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_MARGIN_STOPLIMIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_VOLUME))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_TURNOVER))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_INTEREST))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_BUY_ORDERS_VOLUME))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_SELL_ORDERS_VOLUME))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_OPEN))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_CLOSE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_AW))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_PRICE_SETTLEMENT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_PRICE_LIMIT_MIN))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(SymbolInfoDouble(args0, SYMBOL_SESSION_PRICE_LIMIT_MAX))); StringAdd(res, "|");
					//
					ResetLastError();
					//
                    } break;
                case 164:
                    if (maxDebug) Print("AccountInfo");
					res = "";
					StringAdd(res, IntegerToString(AccountInfoInteger(ACCOUNT_LOGIN))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountTradeMode(AccountInfoInteger(ACCOUNT_TRADE_MODE)))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountInfoInteger(ACCOUNT_LEVERAGE))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountInfoInteger(ACCOUNT_LIMIT_ORDERS))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountStopOutMode(AccountInfoInteger(ACCOUNT_MARGIN_SO_MODE)))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountInfoInteger(ACCOUNT_TRADE_ALLOWED))); StringAdd(res, "|");
					StringAdd(res, IntegerToString(AccountInfoInteger(ACCOUNT_TRADE_EXPERT))); StringAdd(res, "|");
					//
					StringAdd(res, AccountInfoString(ACCOUNT_NAME)); StringAdd(res, "|");
					StringAdd(res, AccountInfoString(ACCOUNT_SERVER)); StringAdd(res, "|");
					StringAdd(res, AccountInfoString(ACCOUNT_CURRENCY)); StringAdd(res, "|");
					StringAdd(res, AccountInfoString(ACCOUNT_COMPANY)); StringAdd(res, "|");
					//
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_BALANCE))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_CREDIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_PROFIT))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_EQUITY))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_FREEMARGIN))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN_LEVEL))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN_SO_CALL))); StringAdd(res, "|");
					StringAdd(res, DoubleToString(AccountInfoDouble(ACCOUNT_MARGIN_SO_SO))); StringAdd(res, "|");
					//
					ResetLastError();
					//
                    break;
				case 165: {
						if (maxDebug) Print("serverTimeGMTOffset");
						string gmtOffsetFile = "server_gmt_offset.csv";
                        res = "";
						//
						datetime t1 = TimeCurrent();
						for (int i = 0; i < 200; i++) {
							if (t1 != TimeCurrent()) {
								break;
							}
							Sleep(10);
						}
						if (t1 == TimeCurrent()) { // seems market is closed (or no connection)
							int filehandle = FileOpen(gmtOffsetFile, FILE_READ|FILE_CSV); 
							if (filehandle != INVALID_HANDLE) 
							{
								ResetLastError();
								datetime t = FileReadDatetime(filehandle); 
								int diff = (TimeLocal() - t);
								if (GetLastError() == 0 && diff / 60.0 / 60.0 / 24.0 <= 7 /*days*/) {
									res = FileReadString(filehandle); 
									log = ("serverTimeGMTOffset: " + res + " read from db, last updated at " + t); jfxLog(log);
								}
								FileClose(filehandle); 
							}
							if (res == "") errorCopy = 132;
							break;
						}
						//
						int srvrToUTC = TimeGMT() /*GMT time of local computer*/ - TimeCurrent() /*last known server time*/;
						double nearest15minutes = MathRound(srvrToUTC / 900.);
						double Srvr_To_UTC_Hours = nearest15minutes / 4.;
						res = IntegerToString(((int) (Srvr_To_UTC_Hours * 3600.)));
						errorCopy = GetLastError();
						int filehandle = FileOpen(gmtOffsetFile, FILE_WRITE|FILE_CSV); 
						if(filehandle != INVALID_HANDLE) 
						{ 
							FileWrite(filehandle, TimeLocal(), res); 
							FileClose(filehandle); 
							log = ("serverTimeGMTOffset: " + res + ", db file updated"); jfxLog(log);
						}
						ResetLastError();
					}
					break;
                case 166:
					res = IntegerToString( IsTradeAllowed(args0, StrToTime(args1)) );
                    if (maxDebug) Print("IsTradeAllowed("+args0+", "+StrToTime(args1)+")="+res);
                    break;
                case 167:  // sba = SymbolsMarketInfo(selected)
                    {
                        res = "";
                        int sba_selected = StringToInteger(args0);
                        int sba_max = SymbolsTotal(sba_selected);
                        StringAdd(res, IntegerToString(sba_max)); StringAdd(res, "|");
                        for (int sba_i = 0; sba_i < sba_max; sba_i++) {
                            string sba_symbol = SymbolName(sba_i, sba_selected);
                            StringAdd(res, sba_symbol); StringAdd(res, "|");
//                            StringAdd(res, DoubleToString(SymbolInfoDouble(sba_symbol, SYMBOL_BID))); StringAdd(res, "|");
//                            StringAdd(res, DoubleToString(SymbolInfoDouble(sba_symbol, SYMBOL_ASK))); StringAdd(res, "|");
                            StringAdd(res, DoubleToString(MarketInfo(sba_symbol, MODE_BID))); StringAdd(res, "|");
                            StringAdd(res, DoubleToString(MarketInfo(sba_symbol, MODE_ASK))); StringAdd(res, "|");
                        }
                        if (maxDebug) Print("SymbolsMarketInfo", ", ", "selected=", args0);
                        errorCopy = GetLastError();
                        if (errorCopy == 4106) {//ERR_UNKNOWN_SYMBOL
                            errorCopy = 0;
                        }
                    }
                    break;
                // -----------------------------------------------------------------------------------------------
                // Chart*
                // -----------------------------------------------------------------------------------------------
                case 170:
					res = IntegerToString(ChartApplyTemplate(StringToInteger(args0), args1));
                    if (maxDebug) Print("ChartApplyTemplate", "chart_id=", args0, "filename=", args1, "=", res);
                    break;
                case 171:
					res = IntegerToString(ChartSaveTemplate(StringToInteger(args0), args1));
                    if (maxDebug) Print("ChartSaveTemplate", "chart_id=", args0, "filename=", args1, "=", res);
                    break;
                case 172:
					res = IntegerToString(ChartOpen(args0, StringToInteger(args1)));
                    if (maxDebug) Print("ChartOpen", "symbol=", args0, "period=", args1, "=", res);
                    break;
                case 173:
					res = IntegerToString(ChartClose(StringToInteger(args0)));
                    if (maxDebug) Print("ChartClose", "chart_id=", args0, "=", res);
                    break;
                case 174: {
						long _ch = StringToInteger(args0);
						bool _i = ChartSetSymbolPeriod(_ch, args1, StringToInteger(args2));
						res = IntegerToString(_i);
						if (maxDebug) Print("ChartSetSymbolPeriod", "chart_id=", args0, " ", args1, " ", args2, " res=", res);
					}
                    break;
                // -----------------------------------------------------------------------------------------------
                case 1000:
                    res = "0";
                    break;
                case 10000:
                    autoRefresh = (StrToInteger(args0) != 0);
                    if (maxDebug) Print("SetAutoRefresh=" + autoRefresh);
                    res = "1";
                    break;
                case 10001:
                    res = "";
                    for (int mi = 0; mi < 28; mi++) {
                        res = res + MarketInfo(args0, market_infos[mi]) + "|";
                    }
                    if (maxDebug) Print("MarketInfo", ", ", "symbol=", args0,", ", "type=ALL");
                    break;
                case 10002: {
                    string _symbol = Symbol();
                    double _time = 0;
                    double _bid  = 0;
                    double _ask  = 0;
                    if (listener) {
						listener_command = 10002;
                        auto_listener = false;
                    } else {
                        _symbol = args0;
                        _time = StrToDouble(args1);
                        _bid  = StrToDouble(args2);
                        _ask  = StrToDouble(args3);
                    }
                    //
                    // if (maxDebug) Print("tNow" + MarketInfo(_symbol, MODE_TIME) + ", tWas=" + _time);
                    //
                    double _timeNew = MarketInfo(_symbol, MODE_TIME);
                    double _bidNew  = MarketInfo(_symbol, MODE_BID);
                    double _askNew  = MarketInfo(_symbol, MODE_ASK);
                    bool noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
                    while (_timeNew == _time
                        && _bidNew == _bid
                        && _askNew == _ask
                        && noErrors
                    ) {
                        Sleep(1);
                        //RefreshRates();
                        _timeNew = MarketInfo(_symbol, MODE_TIME);
                        _bidNew  = MarketInfo(_symbol, MODE_BID);
                        _askNew  = MarketInfo(_symbol, MODE_ASK);
                        noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
                    }
                    //
                    res = "" + DoubleToStr(_timeNew, 0)
                       + "|" + _bidNew 
                       + "|" + _askNew;
                    //
                    if (maxDebug) Print("NewTick", ", ", "symbol=", _symbol, res);
                    //
                    int _t = OrdersTotal();
                    string _orders = "";
                    int _ordersCount = 0;
                    for (int _o = 0; _o < _t; _o++) {
                        if (OrderSelect(_o, SELECT_BY_POS) == false) continue;
                        if (OrderSymbol() == _symbol) {
                            int _ot = OrderType();
                            if (_ot != OP_BUY && _ot != OP_SELL) continue;
                            _orders = _orders + OrderTicket() + "|" + DoubleToStr(OrderProfit(), 2) + "|";
                            _ordersCount++;
                        }
                    }
                    res = res + "|" + _ordersCount + "|" + _orders;
                    //
					}
                    break;
                case 10012: // lsnr_all_
					{
						if (listener) {
							listener_command = 10012;
							auto_listener = false;
						}
						//
						int lsnr_all_symbolsTotal = SymbolsTotal(true);
						lsnr_all_symbolsTotal = MathMin(lsnr_all_symbolsTotal, ArraySize(listener_ticks));
						string lsnr_all_symbol;
						res = "";
						MqlTick lsnr_all_last_tick;
                        bool noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
						while (noErrors) {
							bool lsnr_all_found = false;
							for (int i = 0; i < lsnr_all_symbolsTotal; i++) {
								lsnr_all_symbol = SymbolName(i, true);
								if (SymbolInfoTick(lsnr_all_symbol, lsnr_all_last_tick)) {
									MqlTick lsnr_all_prev_tick = listener_ticks[i];
									if (lsnr_all_last_tick.time != lsnr_all_prev_tick.time
											|| lsnr_all_last_tick.bid != lsnr_all_prev_tick.bid
											|| lsnr_all_last_tick.ask != lsnr_all_prev_tick.ask
									) {
										listener_ticks[i] = lsnr_all_last_tick;
										if (lsnr_all_found) {
											StringAdd(res, "|");
										}
										lsnr_all_found = true;
										StringAdd(res, lsnr_all_symbol);
										StringAdd(res, "|");
										StringAdd(res, IntegerToString((int) lsnr_all_last_tick.time));
										StringAdd(res, "|");
										StringAdd(res, DoubleToString(lsnr_all_last_tick.bid));
										StringAdd(res, "|");
										StringAdd(res, DoubleToString(lsnr_all_last_tick.ask));
									}
								} else {
									int lsnr_all_err = GetLastError();
									if (maxDebug) Print("AllTicksListener", " error=", lsnr_all_err, " symbol=", lsnr_all_symbol);
								}
							}
							if (lsnr_all_found) {
								break;
							}
							Sleep(1);
                            noErrors = (GlobalVariableGet("jfx-in-error-state") == 0);
						}
						errorCopy = GetLastError();
						if (errorCopy == 4024) {
							errorCopy = 0;
						}
					}
                    break;
                case 10003: {
                        long ticket = StrToInteger(args0);
						int og_pool = StrToInteger(args2);
						int og_mode = StrToInteger(args1);
                        bool selected = false;
                        if (og_mode == SELECT_BY_TICKET) {
                            for (int i = 0; i < 10; ++i) {
                                selected = OrderSelect(ticket, og_mode, og_pool);
                                if (selected) {
                                    break;
                                }
                                log = ("OrderGet: ticket=" + ticket + " not selected i=" + i + " err=" + GetLastError()); jfxLog(log);
                                Sleep(100);
                            }
                        }
                        else 
                        {
                            selected = OrderSelect(ticket, og_mode, og_pool);
                        }
						if (selected) {
							if (og_mode == SELECT_BY_TICKET && og_pool == MODE_TRADES && OrderCloseTime() > 0) {
								og_pool = MODE_HISTORY;
							}
							if (og_mode == SELECT_BY_TICKET && og_pool == MODE_HISTORY && OrderCloseTime() == 0) {
								og_pool = MODE_TRADES;
							}
							saveOrderInfo(og_pool == MODE_HISTORY, false);
							res = "1";
						} else {
							res = "0";
						}
						if (maxDebug) Print("OrderGet", ", ", "index=", args0,", ", "select=", args1,", ", "pool=", args2);
					}
                    break;
                case 10004: {
						int _tCount = StrToInteger(args0);
						int _hCount = StrToInteger(args1);
						//
						if (_ordersHistoryTotal == -1 || _tCount == -1 && _hCount == -1) {
							// init
							initPosition();
						} else {
							// 
							if (_tCount != _ordersTotal) {
								_ordersTotal = -1; // reinitialize live orders
							}
							if (_hCount < _ordersHistoryTotal) {
								_ordersHistoryTotal = _hCount; // scan more historical orders
							}
							monitorPosition();
						}
						//
						res = jfxPositionRes(conn, _ordersTotal, _ordersHistoryTotal);
						//
					}
                    break;
				case 10005: {
						int mode = StrToInteger(args0); // MODE_TRADES
                        datetime df = 0;
                        datetime dt = 0;
                        if (StringLen(args1) > 0) {
                            df = StrToTime(args1);
                        }
                        if (StringLen(args2) > 0) {
                            dt = StrToTime(args2);
                        }
						int ordersTotal = 0;
						int ordersHistoryTotal = 0;
						int total = 0;
						bool is_history = false;
						//
						jfxPositionInit(conn, 0);
						if (mode == MODE_TRADES) {
							ordersTotal = OrdersTotal();
							total = ordersTotal;
						} else {
							ordersHistoryTotal = OrdersHistoryTotal();
							total = ordersHistoryTotal;
							is_history = true;
						}
						for (int i = 0; i < total; i++)
						{
							if (OrderSelect(i, SELECT_BY_POS, mode))
							{
                                if (df != 0 || dt != 0) {
                                    datetime t = 0;
                                    if (is_history && OrderType() != 7 /* OP_CREDIT */) {
                                        t = OrderCloseTime();
                                    } else {
                                        t = OrderOpenTime();
                                    }
                                    if ((df == 0 || df <= t) 
                                        && (dt == 0 || t <= dt)) {
                                        //
                                        saveOrderInfo(is_history, true);
                                    }
                                } else {
                                    saveOrderInfo(is_history, true);
                                }
							}
						}
						res = jfxPositionRes(conn, ordersTotal, ordersHistoryTotal);
					}
					break;
				case 10006: {
						int start = StrToInteger(args2);
						int count = StrToInteger(args3);
						datetime df = 0;
						datetime dt = 0;
                        if (StringLen(args4) > 0) {
                            df = StrToTime(args4);
                        }
                        if (StringLen(args5) > 0) {
                            dt = StrToTime(args5);
                        }
						MqlRates rates[];
						int copied;
						if (count > 0) {
							if (((int) df) > 0) {
								copied = CopyRates(args0, StrToInteger(args1), df, count, rates);
							} else {
								copied = CopyRates(args0, StrToInteger(args1), start, count, rates);
							}
						} else {
							copied = CopyRates(args0, StrToInteger(args1), df, dt, rates);
						}
						if (copied > 0) {
							if (jfxMqlRatesInit(conn) == 1) {
								int sz = ArraySize(rates);
								for (int i = 0; i < sz; i++) {
									jfxMqlRatesAdd(conn, rates[i]);
								}
							}
						} else {
							jfxMqlRatesInit(conn);
						}
						jfxMqlRatesRes(conn);
						//
						res = "";
						StringAdd(res, DoubleToString(MarketInfo(args0, MODE_BID))); StringAdd(res, "|");
						StringAdd(res, DoubleToString(MarketInfo(args0, MODE_ASK)));
					}
					break;
                default:
                    res = "not-implemented";
            }
            //
            if (errorCopy >= 0) {
                error = errorCopy;
            } else {
                error = GetLastError();
            }
            //
            if (x >= 0 && x < 10 && x != 1 && (/*error == 4051 || */error == 0 && StrToDouble(res) == 0)) {
                error = 4066;
            }
            if (error != 4073 && error != 4066 && error != 4054) {
                if (retries > 0) {
                    if (StrToDouble(res) != 0 || x <= 1 || x >= 10) {
                        log = ("" + x + " [" + args0 + "] Error=" + error + ", 4066 has been cleared, Result=" + res); jfxLog(log);
                        break;
                    }
                } else {
                    break;
                }
            } else if (error == 4054 && retries > 5) {
                log = ("" + x + " [" + args0 + "] Error=" + error + ", Result=" + res + " retries=" + retries); jfxLog(log);
                error = 0;
                break;
            }
            retries++;
            if (retries >= 15) {
                log = ("" + x + " [" + args0 + "] Error=" + error + ", Result=" + res + " retries=" + retries); jfxLog(log);
                error = 0;
                break;
            }
            maxDebug = true;
            log = ("" + x + " [" + args0 + "] Error=" + error + ", Result=" + res + ", sleeping for 1 sec" + " retry_no=" + retries); jfxLog(log);
            Sleep(1000);
        }
        //
        maxDebug = debug;
		errorLast = error;
		if (x == 93 || x == 10003) { // clear error for OrderSelect or OrderGet
		    error = 0;
		}
        string resFinal = res + "@" + error;
        if (maxDebug) Print("Result=", resFinal);
        jfxSendResult(conn, resFinal);
 		if (x == 162) {
			waitForDisconnect = true;
			break;
		}
    }
    //
    if (IsStopped() == true) {
        log = ("! IS_STOPPED (1) !"); jfxLog(log);
    }
    //
    if (maxDebug) {log = ("" + strategy + "> exit from start() 5, waitForDisconnect=" + waitForDisconnect); jfxLog(log);}
    //
    return(0);
}