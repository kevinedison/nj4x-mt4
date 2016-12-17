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


//这个MQ的操作类
#pragma once
#ifndef Mql_H
#define Mql_H
#include "net/Util.h"


//const int BUF_SZ = 51200;
//const int RES_BUF_SZ = 4096;
const int BUF_SZ = 10240;
const int RES_BUF_SZ = 4096;

extern void debug(const char* fmt, const char* p1, const wchar_t* p2);
extern void debug(const char* fmt, ...);
extern void printStats(const char* msg, bool init);

extern size_t mywcstombs(const wchar_t* res, char* b, size_t csz);

struct MqlStr {
    int  len;
    char *str;
};

struct MqlRates
{
	int		time;         // Period start time
	double  open;         // Open price
	double  high;         // The highest price of the period
	double  low;          // The lowest price of the period
	double  close;        // Close price
	long    tick_volume;  // Tick volume
	int     spread;       // Spread
	long    real_volume;  // Trade volume
};

#define MAX_COMMENT_SZ 300
#define MAX_SYMBOL_SZ 40

#define MASK_TYPE		2
#define MASK_OTIME		4
#define MASK_CTIME		8
#define MASK_EXP		16
#define MASK_LOTS		32
#define MASK_OPRICE		64
#define MASK_CPRICE		128
#define MASK_SL			256
#define MASK_TP			512
#define MASK_PROFIT		1024
#define MASK_COMMISSION	2048
#define MASK_SWAP		4096

enum StatPeriod
{
	SECOND,
	MINUTE,
	MINUTES_5,
	MINUTES_15,
	MINUTES_30,
	TOTAL
};

struct PeriodStats
{
	static __int64 toStatPeriod(__int64 millis, StatPeriod period)
	{
		switch (period)
		{
		case StatPeriod::SECOND:
			return millis / 1000 * 1000;
		case StatPeriod::MINUTE:
			return millis / 60000 * 60000;
		case StatPeriod::MINUTES_5:
			return millis / 300000 * 300000;
		case StatPeriod::MINUTES_15:
			return millis / 900000 * 900000;
		case StatPeriod::MINUTES_30:
			return millis / 1800000 * 1800000;
		case TOTAL: break;
		default: break;
		}
		return millis;
	}

	StatPeriod period;
	__int64 startTime, time, numRequests, execTime;
	__int64 _startTime;

	//
	PeriodStats()
	{
		_startTime = startTime = time = numRequests = execTime = 0;
		period = StatPeriod::TOTAL;
	}


	//
	int PeriodSeconds()
	{
		switch (period)
		{
		case StatPeriod::SECOND:
			return 1;
		case StatPeriod::MINUTE:
			return 60;
		case StatPeriod::MINUTES_5:
			return 300;
		case StatPeriod::MINUTES_15:
			return 900;
		case StatPeriod::MINUTES_30:
			return 1800;
		case StatPeriod::TOTAL:
			__int64 t = (time - startTime) / 1000;
			return t == 0 ? 1 : t;
		}
		return 1;
	}

	int TimeBeginSeconds()
	{
		if (period == StatPeriod::TOTAL)
		{
			return (startTime) / 1000;
		}
		return time / 1000;
	}

	int TimeEndSeconds()
	{
		if (period == StatPeriod::TOTAL)
		{
			return (time) / 1000;
		}
		return TimeBeginSeconds() + PeriodSeconds();
	}

	void Init(__int64 millis, StatPeriod p)
	{
		period = p;
		time = toStatPeriod(startTime = millis, p);
	}

	void Begin(__int64 startTime)
	{
		_startTime = startTime;
	}

	//
	void End(__int64 endTime)
	{
		numRequests++;
		execTime += (endTime - _startTime);
		time = toStatPeriod(endTime, period);
	}

	//
	void AddMax(PeriodStats s)
	{
		if (time != s.time)
		{
			time = s.time;
			execTime = numRequests > s.numRequests ? execTime : s.execTime;
			numRequests = numRequests > s.numRequests ? numRequests : s.numRequests;
		}
	}

	//
	void AddMin(PeriodStats s)
	{
		if (time != s.time)
		{
			time = s.time;
			execTime = numRequests != 0 && numRequests < s.numRequests ? execTime : s.execTime;
			numRequests = numRequests != 0 && numRequests < s.numRequests ? numRequests : s.numRequests;
		}
	}

	//
	int ToStringHeader(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, "\t\t%s\t%s\t%s\t%s\t%s\t%s",
			"Period", "NumReq", "ExTime", "TPS", "TimeFrom", "TimeTo    "
			);
		return strlen(buf);
	}

	//
	int ToString(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, "\t\t%u\t%llu\t%llu\t%.4g\t%u\t%u",
			PeriodSeconds(), numRequests, execTime, (static_cast<double>(numRequests) / PeriodSeconds()), TimeBeginSeconds(), TimeEndSeconds()
			);
		return strlen(buf);
	}

	//
	int ToCSV(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, ",%u,%llu,%llu,%.4g,%u,%u",
			PeriodSeconds(), numRequests, execTime, (static_cast<double>(numRequests) / PeriodSeconds()), TimeBeginSeconds(), TimeEndSeconds()
			);
		return strlen(buf);
	}
};

class CmdStats
{
public:
	int cmd;
	__int64 startTime;
	PeriodStats total;
	PeriodStats current[StatPeriod::TOTAL];
	//PeriodStats minimums[StatPeriod::TOTAL];
	PeriodStats maximums[StatPeriod::TOTAL];

	CmdStats(int _cmd)
	{
		cmd = _cmd;
		startTime = Util::currentTimeMillis();
		//
		total.Init(startTime, StatPeriod::TOTAL);
		//
		current[StatPeriod::SECOND].Init(startTime, StatPeriod::SECOND);
		current[StatPeriod::MINUTE].Init(startTime, StatPeriod::MINUTE);
		current[StatPeriod::MINUTES_5].Init(startTime, StatPeriod::MINUTES_5);
		current[StatPeriod::MINUTES_15].Init(startTime, StatPeriod::MINUTES_15);
		current[StatPeriod::MINUTES_30].Init(startTime, StatPeriod::MINUTES_30);
		//
		//		minimums[StatPeriod::SECOND].Init(startTime, StatPeriod::SECOND);
		//		minimums[StatPeriod::MINUTE].Init(startTime, StatPeriod::MINUTE);
		//		minimums[StatPeriod::MINUTES_5].Init(startTime, StatPeriod::MINUTES_5);
		//		minimums[StatPeriod::MINUTES_15].Init(startTime, StatPeriod::MINUTES_15);
		//		minimums[StatPeriod::MINUTES_30].Init(startTime, StatPeriod::MINUTES_30);
		//
		maximums[StatPeriod::SECOND].Init(startTime, StatPeriod::SECOND);
		maximums[StatPeriod::MINUTE].Init(startTime, StatPeriod::MINUTE);
		maximums[StatPeriod::MINUTES_5].Init(startTime, StatPeriod::MINUTES_5);
		maximums[StatPeriod::MINUTES_15].Init(startTime, StatPeriod::MINUTES_15);
		maximums[StatPeriod::MINUTES_30].Init(startTime, StatPeriod::MINUTES_30);
	}

	void Begin()
	{
		__int64 now = Util::currentTimeMillis();
		total.Begin(now);
		for (int i = StatPeriod::SECOND; i < StatPeriod::TOTAL; ++i)
		{
			current[i].Begin(now);
		}
	}

	void End(__int64 now)
	{
		total.End(now);
		for (int i = StatPeriod::SECOND; i < StatPeriod::TOTAL; ++i)
		{
			__int64 t = current[i].time;
			current[i].End(now);
			//			minimums[i].AddMin(current[i]);
			maximums[i].AddMax(current[i]);
			if (t != current[i].time)
			{
				current[i].numRequests = 0;
				current[i].execTime = 0;
			}
		}
	}

	void ToStringHeader(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, "%s",
			"CMD"
			);
		int len = strlen(buf);
		len += total.ToStringHeader(buf + len, sz - len);
		for (int i = StatPeriod::SECOND; i < StatPeriod::TOTAL; ++i)
		{
			len += maximums[i].ToStringHeader(buf + len, sz - len);
		}
	}

	void ToString(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, "%05d",
			cmd
			);
		int len = strlen(buf);
		len += total.ToString(buf + len, sz - len);
		for (int i = StatPeriod::SECOND; i < StatPeriod::TOTAL; ++i)
		{
			len += maximums[i].ToString(buf + len, sz - len);
		}
	}

	void ToCSV(char* buf, size_t sz)
	{
		sprintf_s(buf, sz, "%05d",
			cmd
			);
		int len = strlen(buf);
		len += total.ToCSV(buf + len, sz - len);
		for (int i = StatPeriod::SECOND; i < StatPeriod::TOTAL; ++i)
		{
			len += maximums[i].ToCSV(buf + len, sz - len);
		}
	}
};

class Rate
{
	int time; // Period start time
	double open; // Open price
	double high; // The highest price of the period
	double low; // The lowest price of the period
	double close; // Close price
	long tick_volume; // Tick volume
	int spread; // Spread
	long real_volume; // Trade volume
public:
	Rate(MqlRates* rates)
	{
		time = rates->time;
		open = rates->open;
		high = rates->high;
		low = rates->low;
		close = rates->close;
		tick_volume = rates->tick_volume;
		spread = rates->spread;
		real_volume = rates->real_volume;
	}

	int ToString(char* info, int sz)
	{
		int charsWritten = sprintf_s(info, sz, "\x01%d|%.8g|%.8g|%.8g|%.8g|%ld|%d|%ld\x02"
			, time, open, high, low, close, tick_volume, spread, real_volume
			);
		return charsWritten;
	}

	int GetStringLength()
	{
		int charsWritten = _snprintf(nullptr, 0, "\x01%d|%.8g|%.8g|%.8g|%.8g|%ld|%d|%ld\x02"
			, time, open, high, low, close, tick_volume, spread, real_volume
			);
		return charsWritten;
	}
};

class Order
{
	long diffBitMap;
	bool _sent;

public:
	int ticket, type, openTime, closeTime, magic, expiration;
	double lots, openPrice, closePrice, sl, tp, profit, commission, swap;
	wchar_t symbol[MAX_SYMBOL_SZ];
	wchar_t comment[MAX_COMMENT_SZ];

	Order()
	{
		ticket = type = openTime = closeTime = magic = expiration = 0;
		diffBitMap = 0;
		lots = openPrice = closePrice = sl = tp = profit = commission = swap = 0;
		symbol[0] = NULL;
		comment[0] = NULL;
		_sent = false;
	}

	Order(int _ticket, int _type, int _openTime, int _closeTime, int _magic, int _expiration, wchar_t const* _symbol, wchar_t const* _comment, double _lots, double _openPrice, double _closePrice, double _sl, double _tp, double _profit, double _commission, double _swap)
	{
		_sent = false;
		diffBitMap = 0;
		reset(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap);
	}

	Order(Order& o)
	{
		_sent = o._sent;
		diffBitMap = 0;
		reset(o.ticket, o.type, o.openTime, o.closeTime, o.magic, o.expiration, o.symbol, o.comment, o.lots, o.openPrice, o.closePrice, o.sl, o.tp, o.profit, o.commission, o.swap);
	}

	void reset(int _ticket, int _type, int _openTime, int _closeTime, int _magic, int _expiration, wchar_t const* _symbol, wchar_t const* _comment, double _lots, double _openPrice, double _closePrice, double _sl, double _tp, double _profit, double _commission, double _swap)
	{
		this->ticket = _ticket;
		this->type = _type;
		this->openTime = _openTime;
		this->closeTime = _closeTime;
		this->magic = _magic;
		this->expiration = _expiration;
		this->lots = _lots;
		this->openPrice = _openPrice;
		this->closePrice = _closePrice;
		this->sl = _sl;
		this->tp = _tp;
		this->profit = _profit;
		this->commission = _commission;
		this->swap = _swap;
		//
		wcscpy_s(this->symbol, MAX_SYMBOL_SZ, _symbol);
		wcscpy_s(this->comment, MAX_COMMENT_SZ, _comment);
	}

	int storeDifference(Order& o)
	{
		diffBitMap = 0;
		if (type != o.type)
		{
			type = o.type;
			diffBitMap |= MASK_TYPE;
		}
		if (openTime != o.openTime)
		{
			openTime = o.openTime;
			diffBitMap |= MASK_OTIME;
		}
		if (closeTime != o.closeTime)
		{
			closeTime = o.closeTime;
			wcscpy_s(comment, MAX_COMMENT_SZ, o.comment);
			diffBitMap |= MASK_CTIME;
			diffBitMap |= MASK_CPRICE;
			diffBitMap |= MASK_PROFIT;
		}
		if (expiration != o.expiration)
		{
			expiration = o.expiration;
			diffBitMap |= MASK_EXP;
		}
		if (lots != o.lots)
		{
			lots = o.lots;
			diffBitMap |= MASK_LOTS;
		}
		if (openPrice != o.openPrice)
		{
			openPrice = o.openPrice;
			diffBitMap |= MASK_OPRICE;
		}
		if (closePrice != o.closePrice)
		{
			closePrice = o.closePrice;
			if (o.closeTime != 0) diffBitMap |= MASK_CPRICE;
			diffBitMap |= MASK_PROFIT;
		}
		if (sl != o.sl)
		{
			sl = o.sl;
			diffBitMap |= MASK_SL;
		}
		if (tp != o.tp)
		{
			tp = o.tp;
			diffBitMap |= MASK_TP;
		}
		if (profit != o.profit)
		{
			profit = o.profit;
			diffBitMap |= MASK_PROFIT;
		}
		if (swap != o.swap)
		{
			swap = o.swap;
			diffBitMap |= MASK_SWAP;
		}
		if (commission != o.commission)
		{
			commission = o.commission;
			diffBitMap |= MASK_COMMISSION;
		}
		//
		if (diffBitMap == 0 || diffBitMap == MASK_PROFIT)
		{
			diffBitMap = -1;
			//commentmedebug("store diff(%ld) #%d(%d) -> 0\n", diffBitMap, ticket, _sent);
			return 0;
		}
		else
		{
			//commentmedebug("store diff(%ld) #%d(%d) -> %d\n", diffBitMap, ticket, _sent, _sent);
			return _sent ? 1 : 0;
		}
	}

	const char* tombs(const wchar_t* wc, char* c)
	{
		size_t n = mywcstombs(wc, c, 999);
		//size_t n = wcstombs(c, wc, 999);
		if (n >= 999)
		{
			c[999] = '\0';
		}
		for (int i = 0; i < n; ++i)
		{
			c[i] = c[i] == '\x01' || c[i] == '\x02' ? ' ' : (c[i] == '|' ? '~' : c[i]);
		}
		return c;
	}

	int ToString(char* info, int sz)
	{
		if (ticket == 0)
		{
			return sprintf_s(info, sz, "\x01%s|%s\x02", "C0", "0");
		}
		if (/*diffBitMap == 0 || */!_sent)
		{
			char b1[1000];
			char b2[1000];
			// new order - dump everything
			int charsWritten = sprintf_s(info, sz, "\x01%d|%d|%d|%d|%d|%d|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%s|%s\x02"
				, ticket, type, openTime, closeTime, magic, expiration
				, lots, openPrice, closePrice, sl, tp, profit, commission, swap
				, tombs(symbol, b1), tombs(comment, b2)
				);
			return charsWritten;
		}
		else if (diffBitMap > 0)
		{
			char b2[1000];
			// changes
			int cnt = sprintf_s(info, sz, "\x01%s%ld|%d|%d|%d|%d|%d|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%s\x02"
				, "C", diffBitMap, ticket
				, (diffBitMap & MASK_TYPE) == 0 ? 0 : type
				, (diffBitMap & MASK_OTIME) == 0 ? 0 : openTime
				, (diffBitMap & MASK_CTIME) == 0 ? 0 : closeTime
				, (diffBitMap & MASK_EXP) == 0 ? 0 : expiration
				, (diffBitMap & MASK_LOTS) == 0 ? 0 : lots
				, (diffBitMap & MASK_OPRICE) == 0 ? 0 : openPrice
				, (diffBitMap & MASK_CPRICE) == 0 ? 0 : closePrice
				, (diffBitMap & MASK_SL) == 0 ? 0 : sl
				, (diffBitMap & MASK_TP) == 0 ? 0 : tp
				, (diffBitMap & MASK_PROFIT) == 0 ? 0 : profit
				, (diffBitMap & MASK_COMMISSION) == 0 ? 0 : commission
				, (diffBitMap & MASK_SWAP) == 0 ? 0 : swap
				, (diffBitMap & MASK_CTIME) == 0 ? "" : tombs(comment, b2)
				);
			return cnt;
		}
		else
		{
			// no changes
			return sprintf_s(info, sz, "\x01%s|%d\x02", "C0", ticket);
		}
	}

	size_t GetStringLength()
	{
		if (ticket == 0)
		{
			return _snprintf(nullptr, 0, "\x01%s|%s\x02", "C0", "0");
		}
		if (/*diffBitMap == 0 || */!_sent)
		{
			char b1[1000];
			char b2[1000];
			// new order - dump everything
			int charsWritten = _snprintf(nullptr, 0, "\x01%d|%d|%d|%d|%d|%d|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%s|%s\x02"
				, ticket, type, openTime, closeTime, magic, expiration
				, lots, openPrice, closePrice, sl, tp, profit, commission, swap
				, tombs(symbol, b1), tombs(comment, b2)
				);
			return charsWritten;
		}
		else if (diffBitMap > 0)
		{
			char b2[1000];
			// changes
			int cnt = _snprintf(nullptr, 0, "\x01%s%ld|%d|%d|%d|%d|%d|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%.8g|%s\x02"
				, "C", diffBitMap, ticket
				, (diffBitMap & MASK_TYPE) == 0 ? 0 : type
				, (diffBitMap & MASK_OTIME) == 0 ? 0 : openTime
				, (diffBitMap & MASK_CTIME) == 0 ? 0 : closeTime
				, (diffBitMap & MASK_EXP) == 0 ? 0 : expiration
				, (diffBitMap & MASK_LOTS) == 0 ? 0 : lots
				, (diffBitMap & MASK_OPRICE) == 0 ? 0 : openPrice
				, (diffBitMap & MASK_CPRICE) == 0 ? 0 : closePrice
				, (diffBitMap & MASK_SL) == 0 ? 0 : sl
				, (diffBitMap & MASK_TP) == 0 ? 0 : tp
				, (diffBitMap & MASK_PROFIT) == 0 ? 0 : profit
				, (diffBitMap & MASK_COMMISSION) == 0 ? 0 : commission
				, (diffBitMap & MASK_SWAP) == 0 ? 0 : swap
				, (diffBitMap & MASK_CTIME) == 0 ? "" : tombs(comment, b2)
				);
			return cnt;
		}
		else
		{
			// no changes
			return _snprintf(nullptr, 0, "\x01%s|%d\x02", "C0", ticket);
		}
	}

	void MarkSent() { 
		_sent = true; 
		//commentmedebug("MarkSent #%d(%d)\n", ticket, _sent);
	}
};

#endif // !Mql_H
