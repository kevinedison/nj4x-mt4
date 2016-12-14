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
//这个应该是客户端，是什么的客户端就不知道了
#pragma once

#ifndef CLIENT_H_
#define CLIENT_H_

#include "SocketConnection.h"
#include "csocket.h"

#include <string>
#include <string.h>
#include <exception>
#include <map>
#include <vector>
#include <memory>
#include "mql.h"
#include "../jvm/MyJvm.h"
#include "stdafx.h"
#include "Util.h"

using namespace std;

extern void debug(const char* fmt, const char* p1, const wchar_t* p2);
extern void debug(const char* fmt, ...);

class ConnectionInfo
{
	wchar_t info[1000];
public:
	ConnectionInfo(wchar_t const* symbol, int period, wchar_t const* strategy) throw (exception)
	{
		if (wcslen(symbol) + wcslen(strategy) + 7 > 999)
		{
			wchar_t info2[10000];
			swprintf_s(info2, 10000, L"%s.%d.%s", symbol, period, strategy);
			debug("too long connection info [%s] ...\n", info2);
			//fflush(debug);
			//
			throw exception("too long connection info");
		}
		swprintf_s(info, 1000, L"%s.%d.%s", symbol, period, strategy);
	}

	~ConnectionInfo()
	{
	}

	//
	const wchar_t* getKey()
	{
		return info;
	}
};

void copyStringToArray(char const* str, int* a);

class UDPSocketConnection
{
	friend class Client;
	//
	unsigned long id;
	char buf[BUF_SZ];
	int bufPos, bufLen;
	//
	UDPSocket* sock;
	int port;
	string sourceAddress; // Address of datagram source
	unsigned short sourcePort; // Port of datagram source
public:
	UDPSocketConnection(unsigned long _id, sockaddr_in _address) throw (int);

	~UDPSocketConnection()
	{
		delete this->sock;
	}

	void sendLine(string const& line) throw(int)
	{
		sendLine(line.c_str(), static_cast<size_t>(-1));
	}

	void sendLine(const char* buffer, size_t sz) throw(int)
	{
		size_t nBytes = sz == static_cast<size_t>(-1) ? strlen(buffer) : sz;
		sock->sendTo(buffer, nBytes, sourceAddress, sourcePort);
	}

	int receiveLine(char* buffer, int bufSize) throw(int)
	{
		try
		{
			int num_bytes_received = sock->recvFrom(buffer, bufSize, sourceAddress, sourcePort);
			buffer[num_bytes_received] = '\0';
			//
			return num_bytes_received;
		}
		catch (SocketException& e)
		{
			debug("ERROR in receiveLine [%s] ...\n", e.what());
			//fflush(debug);
			strcpy(buffer, "-2");
			//cerr << e.what() << endl;
			throw 1;
		}
	}

	void receiveNBytes(int nBytes, char* buffer) throw(int)
	{
		int num_bytes_received = 0;

		while (true)
		{
			int savedBufPos = bufPos;

			while (bufPos < bufLen && num_bytes_received < nBytes)
			{
				//
				buffer[num_bytes_received++] = buf[bufPos++];
				//
				if (num_bytes_received == nBytes)
				{
					// line is complete
					buffer[num_bytes_received] = '\0';
					//
					return;
				}
			}

			if (num_bytes_received >= nBytes)
			{
				bufPos = savedBufPos;
				break;
			}

			bufLen = sock->recvFrom(buf, BUF_SZ, sourceAddress, sourcePort);
			bufPos = 0;

			if (bufLen <= 0)
			{
				bufLen = bufPos = 0;
				throw 1;
			}
		}

		debug("UDP buffer overflow.\n");
		//fflush(debug);
		throw 2;
	}
};

class ByteBuffer
{
#define SIZE_OF_BOOLEAN 1	
#define SIZE_OF_SHORT 2	
#define SIZE_OF_INT 4	
#define SIZE_OF_LONG 8
#define RuntimeException 10000
#define IllegalArgumentException 10001
#define OutOfBufferException 10002
#define IndexOutOfBoundsException 10003
private:
	bool throwNoException, isOutOfBuffer;
	int position, capacity, limit;
	char* buffer;

	//

	void checkBoundaries(long sizeOf)
	{
		checkBoundaries(position, sizeOf);
	}

	void checkBoundaries(int position, long sizeOf)
	{
		if (!isOutOfBuffer && position + sizeOf > limit)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "Out of buffer (pos=%d, bytes=%d, new_pos=%d, limit=%d)",
			          position, sizeOf, (position + sizeOf), limit
			);
			debug("%s\n", info2);
			if (this->throwNoException)
			{
				isOutOfBuffer = true;
			}
			else
			{
				throw RuntimeException;
			}
		}
	}

	void checkReadBoundaries(long sizeOf)
	{
		checkBoundaries(position, sizeOf);
	}

	void checkReadBoundaries(int position, long sizeOf)
	{
		if (!isOutOfBuffer && position + sizeOf > limit)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "Out of buffer (pos=%d, bytes=%d, new_pos=%d, limit=%d)",
			          position, sizeOf, (position + sizeOf), limit
			);
			debug("%s\n", info2);
			if (this->throwNoException)
			{
				isOutOfBuffer = true;
			}
			else
			{
				throw RuntimeException;
			}
		}
	}

	void checkBounds(int off, int len, int size)
	{
		if ((off | len | (off + len) | (size - (off + len))) < 0)
			throw IndexOutOfBoundsException;
	}

public:
	ByteBuffer& Clear()
	{
		isOutOfBuffer = false;
		position = 0;
		limit = capacity;
		return (*this);
	}

	ByteBuffer(char* b, int size)
	{
		this->buffer = b;
		this->capacity = size;
		Clear();
	}

	ByteBuffer& Flip()
	{
		limit = position;
		position = 0;
		return (*this);
	}

	ByteBuffer& Position(int newPosition)
	{
		if ((newPosition > limit) || (newPosition < 0))
			throw IllegalArgumentException;
		position = newPosition;
		return (*this);
	}

	int Position()
	{
		return position;
	}

	ByteBuffer& Limit(int newLimit)
	{
		if ((newLimit > capacity) || (newLimit < 0))
			throw IllegalArgumentException;
		limit = newLimit;
		return (*this);
	}

	int Limit()
	{
		return limit;
	}

	int Capacity()
	{
		return capacity;
	}

	bool HasRemaining()
	{
		return position < limit;
	}

	int Remaining()
	{
		return limit - position;
	}

	char* Array()
	{
		return buffer;
	}

	bool IsOutOfBuffer()
	{
		return isOutOfBuffer;
	}

	ByteBuffer& Put(const char value)
	{
		checkBoundaries(SIZE_OF_BOOLEAN);
		if (isOutOfBuffer) return (*this);
		buffer[position] = value;
		position += SIZE_OF_BOOLEAN ;
		return (*this);
	}

	char Get()
	{
		return Get(position);
	}

	char Get(int& position)
	{
		checkReadBoundaries(SIZE_OF_BOOLEAN);
		char value = buffer[position];
		position += SIZE_OF_BOOLEAN ;
		return value;
	}

	ByteBuffer& PutInt(const int value)
	{
		checkBoundaries(SIZE_OF_INT);
		if (isOutOfBuffer) return (*this);
		//
		union int_and_buffer
		{
			int i;
			unsigned char byte_buff[ sizeof(int) ];
		} iab;
		iab.i = value;
		for (int i = 0; i < sizeof(iab.byte_buff); ++i)
		{
			buffer[position + i] = iab.byte_buff[i];
		}
		position += SIZE_OF_INT ;
		//
		return (*this);
	}

	int GetInt()
	{
		return GetInt(position);
	}

	int GetInt(int& position)
	{
		checkReadBoundaries(SIZE_OF_INT);
		//
		union int_and_buffer
		{
			int i;
			unsigned char byte_buff[ sizeof(int) ];
		} iab;
		//
		for (int i = 0; i < sizeof(iab.byte_buff); ++i)
		{
			iab.byte_buff[i] = buffer[position + i];
		}

		position += SIZE_OF_INT ;

		return iab.i;
	}

	ByteBuffer& PutLong(const long value)
	{
		checkBoundaries(SIZE_OF_LONG);
		if (isOutOfBuffer) return (*this);
		//
		union long_and_buffer
		{
			long l;
			unsigned char byte_buff[ sizeof(long) ];
		} lab;
		lab.l = value;
		for (int i = 0; i < sizeof(lab.byte_buff); ++i)
		{
			buffer[position + i] = lab.byte_buff[i];
		}
		position += SIZE_OF_LONG;
		//
		return (*this);
	}

	long GetLong()
	{
		return GetLong(position);
	}

	long GetLong(int& position)
	{
		checkReadBoundaries(SIZE_OF_LONG);
		//
		union long_and_buffer
		{
			long l;
			unsigned char byte_buff[ sizeof(double) ];
		} lab;
		//
		for (int i = 0; i < sizeof(lab.byte_buff); ++i)
		{
			lab.byte_buff[i] = buffer[position + i];
		}

		position += SIZE_OF_LONG;

		return lab.l;
	}

	ByteBuffer& PutDouble(const double value)
	{
		checkBoundaries(SIZE_OF_LONG);
		if (isOutOfBuffer) return (*this);
		//
		union double_and_buffer
		{
			double d;
			unsigned char byte_buff[ sizeof(double) ];
		} dab;
		dab.d = value;
		for (int i = 0; i < sizeof(dab.byte_buff); ++i)
		{
			buffer[position + i] = dab.byte_buff[i];
		}
		position += SIZE_OF_LONG; // todo assertion SIZE_OF_LONG == sizeof(dab.byte_buff)
		//
		return (*this);
	}

	double GetDouble()
	{
		checkReadBoundaries(SIZE_OF_LONG);
		//
		union double_and_buffer
		{
			double d;
			unsigned char byte_buff[ sizeof(double) ];
		} dab;
		//
		for (int i = 0; i < sizeof(dab.byte_buff); ++i)
		{
			dab.byte_buff[i] = buffer[position + i];
		}

		position += SIZE_OF_LONG;

		return dab.d;
	}

	ByteBuffer& Put(const char* values, const int len)
	{
		checkBoundaries(len);
		if (isOutOfBuffer) return (*this);
		//
		memcpy(buffer + position, values, len);
		//
		position += len;
		//
		return (*this);
	}

	ByteBuffer& Get(char* dst, int length)
	{
		checkReadBoundaries(length);
		memcpy(dst, buffer + position, length);
		position += length;
		return (*this);
	}

	ByteBuffer& Get(wchar_t* dst, int length)
	{
		checkReadBoundaries(length);
		char c = buffer[position + length];
		buffer[position + length] = '\0';
		mbstowcs(dst, buffer + position, 500);
		buffer[position + length] = c;
		position += length;
		return (*this);
	}
};

extern volatile unsigned int statsSync;
class Client : SocketConnection
{
	auto_ptr<wstring> serverAddress;
	int port;
	//
	auto_ptr<wstring> symbol, strategy;
	int period;
	//
	long commandNo;
	char msg[BUF_SZ];
	//char const *args[100];
	//
	//bool isSocketOk();
	long delay;
	//
	UDPSocketConnection* udp;
	//
	bool positionInit;
	int specialResultCode, tCount, hCount;
	map<int, auto_ptr<Order>> LiveOrders;
	vector<auto_ptr<Order>> HistoricalOrders;
	//
	Order _orderInfo;
	//
	int _commandId;
	map<int, auto_ptr<CmdStats>> stats;
	//
	vector<auto_ptr<Rate>> Rates;
	//
	void _sendRes(char* res, size_t nBytes);
	//
public:
	auto_ptr<ConnectionInfo> connInfo;
#ifdef USE_JVM
	MyJvm* jvm;
#endif

public:
	Client(wchar_t const* serverAddress, int port, wchar_t const* symbol, int period, wchar_t const* strategy) throw(int);
	~Client();
	char* getCmd();
	int getCmdAsArray(wchar_t** args);

	int getCmdAsArray3(MqlStr* args);
	//void getCmd(Uni& msg);

	int getCmdAsArray4(wchar_t** args);
	void getCmd(ByteBuffer& msg);

	void receiveNBytes(int nBytes, char* buffer) throw(int) override;
	void sendRes(char* res, size_t nBytes);
	void sendLine(const char* buffer, size_t nBytes) throw(int) override;
	int receiveLine(char* buffer, int bufSize) throw(int) override;

	void fillClientRes(char* res, size_t nBytes)
	{
		int len;
		int bufSz;
		char* buffer = nullptr;
		map<int, auto_ptr<Order>>::iterator it;
		vector<auto_ptr<Order>>::iterator vi;
		vector<auto_ptr<Rate>>::iterator ri;
		int loCnt;
		int hoCnt;
		//
		switch (specialResultCode)
		{
		case 10003:
			char b[RES_BUF_SZ];
			bufSz = RES_BUF_SZ;
			//
			//
			len = sprintf_s(b, bufSz, "\x01R");
			len += _orderInfo.ToString(b + 2, bufSz - 2);
			//len = strlen(b);
			//
			len += sprintf_s(b + len, bufSz - len, "%s\x02", res);
			//nBytes += len + 1;
			nBytes = len;
			res = b;
			break;
		case 10004:
			loCnt = LiveOrders.size();
			hoCnt = HistoricalOrders.size();
			//
			bufSz = 4 + nBytes + 1024 * (1 + loCnt + hoCnt);
			if (maxDebug)
			{
				debug("INFO: PositionRes: malloc bufSz=%d loCnt=%d hoCnt=%d\n", bufSz, loCnt, hoCnt);
			}
			buffer = static_cast<char*>(malloc(bufSz));
			if (maxDebug)
			{
				debug("INFO: PositionRes: malloc done\n");
			}
			//
			//strcpy(buffer, res);
			//bufSz -= len + 1;
			//
			sprintf_s(buffer, bufSz, "\x01R\x01%d|%d\x02", tCount, hCount);
			len = strlen(buffer);
			//
			sprintf_s(buffer + len, bufSz - len, "\x01%d\x02", loCnt);
			len = strlen(buffer);
			for (it = LiveOrders.begin(); it != LiveOrders.end(); ++it)
			{
				len += it->second->ToString(buffer + len, bufSz - len);
				//len = strlen(buffer);
			}
			//
			len += sprintf_s(buffer + len, bufSz - len, "\x01%d\x02", hoCnt);
			//len = strlen(buffer);
			for (vi = HistoricalOrders.begin(); vi != HistoricalOrders.end(); ++vi)
			{
				len += (*vi)->ToString(buffer + len, bufSz - len);
				//len = strlen(buffer);
			}
			//
			len += sprintf_s(buffer + len, bufSz - len, "%s\x02", res);
			//
			res = buffer;
			//nBytes += len + 1;
			nBytes = len;
			break;
		case 10006:
			hoCnt = Rates.size();
			//
			bufSz = 4 + nBytes + 96 * (1 + hoCnt);
			if (maxDebug)
			{
				char info2[1000];
				sprintf_s(info2, 1000, "malloc bufSz=%d hoCnt=%d (res=%s nBytes=%d)", bufSz, hoCnt, res, nBytes);
				debug("INFO: MqlRatesRes: %s\n", info2);
			}
			buffer = static_cast<char*>(malloc(bufSz));
			if (maxDebug)
			{
				debug("INFO: MqlRatesRes: malloc done\n");
			}
			//
			sprintf_s(buffer, bufSz, "\x01R");
			len = strlen(buffer);
			//
			len += sprintf_s(buffer + len, bufSz - len, "\x01%d\x02", hoCnt);
			//len = strlen(buffer);
			for (ri = Rates.begin(); ri != Rates.end(); ++ri)
			{
				len += (*ri)->ToString(buffer + len, bufSz - len);
			}
			//
			len += sprintf_s(buffer + len, bufSz - len, "%s\x02", res);
			//
			res = buffer;
			nBytes = len;
			break;
		default:
			char* c = res;
			for (int i = 0; c[i] != 0 && i < nBytes; ++i)
			{
				c[i] = c[i] == '\x01' || c[i] == '\x02' ? ' ' : c[i];
			}
			break;
		}
		//
		try
		{
			if (maxDebug)
			{
				char info2[1000];
				sprintf_s(info2, 1000, "before _sendRes, nBytes=%d", nBytes);
				debug("INFO: fillClientRes: %s\n", info2);
			}
			_sendRes(res, nBytes);
			if (specialResultCode == 10004)
			{
				for (it = LiveOrders.begin(); it != LiveOrders.end(); ++it)
				{
					it->second->MarkSent();
				}
				//
				for (vi = HistoricalOrders.begin(); vi != HistoricalOrders.end(); ++vi)
				{
					(*vi)->MarkSent();
				}
			}
		}
		catch (...)
		{
			res[nBytes] = 0;
			debug("_sendRes Error: [%s]\n", res);
		}
		//
		specialResultCode = 0;
		if (buffer != nullptr)
		{
			free(buffer);
		}
	}

	int OrderInfo(int pool, int _ticket, int _type, int _openTime, int _closeTime, int _magic, int _expiration, wchar_t const* _symbol, wchar_t const* _comment, double _lots, double _openPrice, double _closePrice, double _sl, double _tp, double _profit, double _commission, double _swap)
	{
		switch (pool)
		{
		case 0: //trades
			if (positionInit)
			{
				if (maxDebug)
				{
					char info2[1000];
					sprintf_s(info2, 1000, "%d total=%d", _ticket, LiveOrders.size());
					debug("INFO: live order in positionInit: %s\n", info2);
				}
				//
				LiveOrders[_ticket] = auto_ptr<Order>(new Order(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap));
				return 1;
			}
			else
			{
				Order o(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap);
				return processLiveOrder(o);
			}
		case 1: //history
			if (positionInit)
			{
				if (maxDebug)
				{
					debug("INFO: historical order in positionInit: %d total=%d\n", _ticket, HistoricalOrders.size());
				}
				HistoricalOrders.push_back(auto_ptr<Order>(new Order(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap)));
			}
			else
			{
				Order o(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap);
				processHistoricalOrder(o);
			}
			break;
		case 2: //orderGet
			if (maxDebug)
			{
				wchar_t info2[1000];
				swprintf_s(info2, 1000, L"%d", _ticket);
				debug("INFO: OrderGet: %s\n", info2);
				//
				swprintf_s(info2, 1000, L"%d|%d|%d|%d|%d|%d|%d|'%s'|'%s'|%f|%f|%f|%f|%f|%f|%f|%f",
				           pool, _ticket, _type, _openTime, _closeTime, _magic, _expiration,
				           _symbol, _comment,
				           _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap
				);
				debug("INFO: order info: %s\n", info2);
			}
			_orderInfo.reset(_ticket, _type, _openTime, _closeTime, _magic, _expiration, _symbol, _comment, _lots, _openPrice, _closePrice, _sl, _tp, _profit, _commission, _swap);
			specialResultCode = 10003;
			break;
		default:
			char nBuff[100];
			debug("OrderInfo: invalid pool: %d.\n", _itoa(pool, nBuff, 10));
		}
		return 0;
	}

	//
	// Position
	//
	void PositionInit(int mode)
	{
		if (mode == 0)
		{
			positionInit = true;
			LiveOrders.clear();
		}
		HistoricalOrders.clear();
	}

	void PositionRes(int _tCount, int _hCount)
	{
		positionInit = false;
		specialResultCode = 10004;
		tCount = _tCount;
		hCount = _hCount;
	}

	void MqlRatesRes()
	{
		specialResultCode = 10006;
	}

	void MqlRatesInit()
	{
		Rates.clear();
	}

	void MqlRatesAdd(MqlRates* rates)
	{
		Rates.push_back(auto_ptr<Rate>(new Rate(rates)));
	}
	//
	// Statistics
	//
	__int64 _statsTime, _statsCnt = 0, _printedStatsCnt = 0;

	void StatsBegin()
	{
		if (!stats.count(_commandId))
		{
			stats[_commandId] = auto_ptr<CmdStats>(new CmdStats(_commandId));
		}
		stats[_commandId]->Begin();
	}

	__declspec(align(32)) static volatile unsigned int statsSync;

	void StatsDump()
	{
		if (stats.size() > 0 && _statsCnt != _printedStatsCnt)
		{
			_printedStatsCnt = _statsCnt;
			//
			char info2[100000];
			bool hdr = false;
			//
			map<int, auto_ptr<CmdStats>>::iterator it;
			for (it = stats.begin(); it != stats.end(); ++it)
			{
				if (it->second->total.numRequests > 0)
				{
					if (!hdr)
					{
						int c = 0;
						while (0 != InterlockedCompareExchange(&statsSync, 1, 0) && c++ < 1000) Sleep(1);

						if (c >= 1000)
						{
							debug("Failed to use statsSync");
							return;
						}

						it->second->ToStringHeader(info2, 100000);
						debug("STAT %s\n", info2);
						hdr = true;
					}
					it->second->ToString(info2, 100000);
					debug("STAT %s\n", info2);
				}
			}
			//
			if (hdr)
			{
				InterlockedCompareExchange(&statsSync, 0, 1);
			}
			//
			stats.clear();
		}
	}

	void StatsCSV()
	{
#if DEBUG_DLL
		if (stats.size() > 0)
		{
			char info2[100000];
			//
			map<int, auto_ptr<CmdStats>>::iterator it;
			for (it = stats.begin(); it != stats.end(); ++it)
			{
				if (it->second->total.numRequests > 0)
				{
					it->second->ToCSV(info2, 100000);
					printStats(info2, false);
				}
			}
		}
#endif
	}

	void StatsEnd()
	{
		_statsCnt++;
		__int64 now = Util::currentTimeMillis();

		if (_commandId != -1)
		{
			stats[_commandId]->End(now);
		}
		_commandId = -1;

		if (_statsTime == 0)
		{
			_statsTime = now;
		}
		else if (now - _statsTime > 1800000LL)
		{
			StatsDump();
			_statsTime = now;
		}
	}

private:
	void connect();

	int processLiveOrder(Order& o)
	{
		if (LiveOrders.count(o.ticket))
		{
			// check modification
			return LiveOrders[o.ticket]->storeDifference(o);
		}
		else
		{
			if (maxDebug)
			{
				char info2[1000];
				sprintf_s(info2, 1000, "%d", o.ticket);
				debug("INFO: new live order: %s\n", info2);
			}
			LiveOrders[o.ticket] = auto_ptr<Order>(new Order(o));
			return 1;
		}
	}

	void processHistoricalOrder(Order& o)
	{
		if (LiveOrders.count(o.ticket))
		{
			if (maxDebug)
			{
				debug("INFO: closed/deleted live order: %d %f\n", o.ticket, o.profit);
			}
			// check modification
			auto_ptr<Order> existingOrder = LiveOrders[o.ticket];
			LiveOrders.erase(o.ticket);
			existingOrder->storeDifference(o);
			HistoricalOrders.push_back(auto_ptr<Order>(existingOrder.get()));
			existingOrder.release();
		}
		else
		{
			if (maxDebug)
			{
				debug("WARN: unknown closed/deleted order: %d %f\n", o.ticket, o.profit);
			}
			HistoricalOrders.push_back(auto_ptr<Order>(new Order(o)));
		}
	}
};


#endif /*CLIENT_H_*/
