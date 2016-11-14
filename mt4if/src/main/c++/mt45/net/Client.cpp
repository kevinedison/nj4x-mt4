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
#include "Client.h"
#include "stdafx.h"
#include "box_id.h"
#include <stdlib.h>
#include <math.h>

void copyStringToArray(char const* str, int* a)
{
	int i = 0;
	for (; str[i] != '\0' && i < 499; i++) a[i + 1] = str[i];
	a[0] = i > 499 ? 499 : i;
	//
	//debug("a[0]=%d [%s]\n", a[0], str);
	////fflush(debug);
}

UDPSocketConnection::UDPSocketConnection(unsigned long _id, sockaddr_in _address)
{
	id = 0;
	buf[0] = NULL;
	//
	sourceAddress = ""; // Address of datagram source
	sourcePort = 0; // Port of datagram source
	//
	bufLen = bufPos = 0;
	port = 10000;
	while (1)
	{
		try
		{
			sock = new UDPSocket(port);
			this->id = _id;
			break;
		}
		catch (SocketException)
		{
			port++;
			if (port > 65000)
			{
				port = 10000;
			}
		}
	}
}

bool hasEnding(std::string const& fullString, std::string const& ending)
{
	unsigned int lastMatchPos = fullString.rfind(ending); // Find the last occurrence of ending
	bool isEnding = lastMatchPos != std::string::npos; // Make sure it's found at least once

	// If the string was found, make sure that any characters that follow it are the ones we're trying to ignore
	for (unsigned int i = lastMatchPos + ending.length(); (i < fullString.length()) && isEnding; i++)
	{
		if ((fullString[i] != '\n') &&
			(fullString[i] != '\r'))
		{
			isEnding = false;
		}
	}

	return isEnding;
}

volatile unsigned int Client::statsSync = 0;

Client::Client(wchar_t const* serverAddress, int port, wchar_t const* sym, int period, wchar_t const* stra)
throw(int)
	: serverAddress(new wstring(serverAddress)),
	  symbol(new wstring(sym)),
	  strategy(new wstring(stra)),
	  connInfo(new ConnectionInfo(sym, period, stra))
{
	init();
	//
	this->specialResultCode = 0;
	this->positionInit = false;
	this->maxDebug |= port == 17342;
	//
	bufPos = bufLen = 0;
	this->port = port;
	this->period = period;
	//
	memset(&(this->address), 0, sizeof(this->address));
	this->address.sin_family = AF_INET ;
	this->address.sin_port = htons(this->port);
	//
	this->udp = nullptr;
	//
	this->connect();
	//
	this->_statsTime = 0;
}

__declspec(align(32)) static volatile unsigned int statsSync;
Client::~Client()
{
	StatsDump();
	//((SocketConnection*) this)->~SocketConnection();
	if (this->udp != nullptr)
	{
		delete this->udp;
	}
#ifdef USE_JVM
	if (this->jvm != nullptr)
	{
		this->jvm->close();
		delete this->jvm;
	}
#endif
}

void Client::connect() throw(int)
{
	char b[10000];
	if (port == 0 && serverAddress->size() > 0)
	{
#ifdef USE_JVM
		this->jvm = new MyJvm();
		this->jvm->connect(tombs(connInfo->getKey(), b, 10000));
#endif
		this->udp = nullptr;
		this->socketId = INVALID_SOCKET;
	}
	else
	{
#ifdef USE_JVM
		this->jvm = nullptr;
#endif
		_disconnect();
		//
#ifdef _WINSOCK_H
		//int len = sizeof(this->address);
		int temp = 0;//WSAStringToAddress(this->serverAddress, AF_INET, NULL, (sockaddr*) (&address), &(len));
		this->address.sin_addr.s_addr = inet_addr(tombs(this->serverAddress->c_str(), b, 10000));
#else
		int temp = inet_pton(AF_INET, this->serverAddress, &(this->address.sin_addr));
#endif

		if (isSocketError(temp))
		{
			debug("inet_pton error.\n");
			//fflush(debug);
			return;
		}

		/* Now the fun begins */
		this->socketId = socket(AF_INET, SOCK_STREAM, 0);
		//this->socketId = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

		// ReSharper disable once CppEntityNeverUsed
		int err = WSAGetLastError();

		if (!SocketConnection::isSocketOk(this->socketId))
		{
			debug("socket error.\n");
			//fflush(debug);
			return;
		}

		if (!_connect())
		{
			if (this->connAttempt < 4)
			{
				debug("connection error: %s:%d\n", tombs(this->serverAddress->c_str(), b, 10000), this->port);
			}
			else
			{
				debug(".");
			}
			//
			throw - 1;
			//return;
		}
	}

	//	if (port > 0) {

	char tmp[1000];
	char hello[1000];
	char* version = "2.6.2";

	//sprintf_s(hello, 1000, "HELLO%s\x01%s\n", connInfo->getKey(), version);

	mywcstombs(connInfo->getKey(), b, 10000);
	sprintf_s(hello, 1000, "\x01HELLO%s\x01%s\x02", b/*tombs(connInfo->getKey(), b, 10000)*/, version);
	sendLine(hello, strlen(hello));
	// ReSharper disable once CppInitializedValueIsAlwaysRewritten
	int len = receiveLine(tmp, 1000);
	unsigned long id = 0;

	if (strlen(tmp) > 5)
	{
		sscanf_s(tmp, "%s", hello, 1000);
		sscanf_s(tmp + strlen(hello), "%u %d", &id, &proto);
		strcpy(tmp, hello);
	}
	//
	if (strcmp("HELLO", tmp) != 0)
	{
		debug("Handshake error: %s\n", tmp);
		//fflush(debug);
		return;
	}
	else if (maxDebug)
	{
		sprintf_s(hello, 1000, "id=%d proto=%d", id, proto);
		debug("Handshake ok: %s\n", hello);
	}

	//if (maxDebug) {
	//	sprintf_s(hello, 1000, "boxid=%lu", boxid());
	//    debug("%s\n", hello);
	//}

	//sprintf_s(hello, 1000, "BOX%lu\n", boxid());
	sprintf_s(hello, 1000, "\x01%s%lu\x02", "BOX", 45/*boxid()*/);
	//
	sendLine(hello, strlen(hello));
	//
	int pos = 0;
	sprintf_s(hello + (pos++), 10, "\x01");
	sprintf_s(hello + (pos++), 10, "X");
	//
	receiveLine(tmp, 1000);
	//
	sprintf_s(hello + (pos++), 10, "O");
	unsigned long actKey = static_cast<unsigned long>(_atoi64(tmp));
	sprintf_s(hello + (pos++), 10, "B");
	int ibi = isboxid(actKey);
	//
	if (id != 0)
	{
		this->udp = new UDPSocketConnection(id, this->address);
	}
	else
	{
		this->udp = nullptr;
	}
	//
	delay = (0x1fff1fff1fff1fffL & ibi);
	for (int i = 0; i < 4; ++i)
	{
		long x = ((delay >> (16 * i)) & 0xffffL) ^ 0x1fffL;
		if (x == 0)
		{
			delay = 8000;
			break;
		}
	}
	//
	if (ibi != 0 && is_in_trial() / 25 != 13)
	{
		long bb = boxid();
		sprintf_s(hello + pos, 996, "NOK%lu\x02", bb);
		//
		char info2[1000];
		sprintf_s(info2, 1000, "box=%lu ak=%lu", bb, actKey);
		delay = 2000;
		debug("------ Invalid activation key, %s\n", info2);
		//fflush(debug);
	}
	else
	{
#if DEBUG_DLL
		if (ibi != 0)
			debug("TRIAL PERIOD\n");
#endif
		if (this->udp == nullptr)
		{
			//sprintf_s(hello+(pos++), 996, "OK\n");
			sprintf_s(hello + pos, 996, "OK\x02");
		}
		else
		{
			//sprintf_s(hello+(pos++), 996, "OK%d\n", this->udp->port);
			sprintf_s(hello + pos, 996, "OK%d\x02", this->udp->port);
		}
		delay = 0;
	}

	sendLine(hello, strlen(hello));
	//	}

	char token[1000];
	len = receiveLine(token, 900);
	while (len > 0 && strcmp("START", token) != 0)
	{
		len = receiveLine(token, 900);
	}

	if (maxDebug)
		debug("Server handshake is ok, start sending client requests\n");
	//fflush(debug);

	commandNo = 0;
}

bool dumpRes;

int Client::getCmdAsArray(wchar_t** args)
{
	char* command = getCmd();
	char* copy = command;
	long _commandNo;
	_commandId = -1;
	//
#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		sscanf_s(this->msg, "%ld %d", &_commandNo, &_commandId);
#ifdef USE_JVM
	}
	else
	{
		sscanf_s(this->msg, "%d", &_commandId);
	}
#endif
	//
	bool dumpCommand = false
		/*_commandId == 78 // OrderClose
				    || _commandId == 94 // OrderSend
				    || _commandId == 10002 // NewTick
					*/;
	dumpRes = dumpCommand;
	//
	int count = 0;
//	int ix = 0;
//	int ix2 = 0;
	while (true)
	{
		char* begin = strstr(command, "\x01");
		if (begin == nullptr)
		{
			break;
		}
		char* end = strstr(command, "\x02");
		end[0] = '\0';
		command = end + 1;

		//mbstowcs(args[count], begin + 1, 500);
		int cvtSz = MultiByteToWideChar(
			CP_UTF8,
			0,
			begin + 1,
			-1,
			args[count],
			500
		);
		//todo Error processing

		count++;

		if (dumpCommand)
		{
			begin[0] = '[';
			end[0] = ']';
		}
	}
	//
	if (dumpCommand)
	{
		debug("%s\n", copy);
	}
	//
	DEB(StatsBegin())
	//
	return _commandId;
}

int Client::getCmdAsArray4(wchar_t** args)
{
	char copy[10000];
	//
	ByteBuffer command(this->msg, BUF_SZ);
	getCmd(command);
	//
	int _commandId = command.GetInt();
	// ReSharper disable once CppEntityNeverUsed
	long _commandNo = command.GetLong();
	int intargs_size = command.Get();
	int longargs_size = command.Get();
	int doubleargs_size = command.Get();
	int strargs_size = command.Get();
	//sprintf_s(copy, 10000, "cmd_no=%ld cmd_id=%d", _commandNo, _commandId);
	//
	bool dumpCommand = false /*_commandId == 78 // OrderClose
				    || _commandId == 94 // OrderSend
				    || _commandId == 10002 // NewTick
					*/;
	dumpRes = dumpCommand;
	if (dumpCommand)
	{
		char info2[1000];
		sprintf_s(info2, 1000, "ints=%d longs=%d doubles=%d strings=%ld",
		          intargs_size,
		          longargs_size,
		          doubleargs_size,
		          strargs_size
		);
		debug("%s\n", info2);
	}
	//
//	int argNo = 0;
	for (int i = 0; i < intargs_size; ++i)
	{
		int map = command.Get();
		int value = command.GetInt();
		swprintf_s(args[map], 500, L"%d", value);
		if (dumpCommand)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "int[%d]=arg[%d]=%d",
			          i,
			          map,
			          value
			);
			debug("%s\n", info2);
		}
	}
	for (int i = 0; i < longargs_size; ++i)
	{
		int map = command.Get();
		long value = command.GetLong();
		swprintf_s(args[map], 500, L"%ld", value);
		if (dumpCommand)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "long[%d]=arg[%d]=%ld",
			          i,
			          map,
			          value
			);
			debug("%s\n", info2);
		}
	}
	for (int i = 0; i < doubleargs_size; ++i)
	{
		int map = command.Get();
		double value = command.GetDouble();
		swprintf_s(args[map], 500, L"%.32g", value);
		if (dumpCommand)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "double[%d]=arg[%d]=%g | %e | %.32g ",
			          i,
			          map,
			          value, value, value
			);
			debug("%s\n", info2);
		}
	}
	for (int i = 0; i < strargs_size; ++i)
	{
		int ix = command.Get();
		int sz = command.GetInt();
		if (sz > 0)
		{
			command.Get(args[ix], sz);
		}
		else
		{
			args[ix][0] = L'\0';
		}
		if (dumpCommand)
		{
			wchar_t info2[1000];
			swprintf_s(info2, 1000, L"str[%d]=arg[%d]=%s sz=%d",
			           i,
			           ix,
			           args[ix],
			           sz
			);
			debug("%s\n", info2);
		}
	}
	//
	if (dumpCommand)
	{
		debug("%s\n", copy);
	}
	//
	return _commandId;
}

int Client::getCmdAsArray3(MqlStr* args)
{
	return 0;
	/*	char copy[10000];
	//
	Uni command;
    getCmd(command);
	//
	long _commandNo = command.commandno();
	int _commandId = command.id();
	//sprintf_s(copy, 10000, "cmd_no=%ld cmd_id=%d", _commandNo, _commandId);
    //
	bool dumpCommand = false 
					//_commandId == 78 // OrderClose
				    //|| _commandId == 94 // OrderSend
				    //|| _commandId == 10002 // NewTick
					;
	dumpRes = dumpCommand;
	if (dumpCommand) {
			char info2[1000];
			sprintf_s(info2, 1000, "ints=%d longs=%d doubles=%d strings=%ld, maps=%d", 
				command.intargs_size(),
				command.longargs_size(),
				command.doubleargs_size(),
				command.strargs_size(),
				command.map_size()
			);
			debug("%s\n", info2);
	}
	int argNo = 0;
	int sz = command.intargs_size();
	for (int i = 0; i < sz; ++i) {
		sprintf_s(args[command.map().Get(argNo++)].str, 500, "%d", command.intargs().Get(i));
		if (dumpCommand) {
			char info2[1000];
			sprintf_s(info2, 1000, "int[%d]=arg[%d]=%d", 
				i,
				command.map().Get(argNo-1),
				command.intargs().Get(i)
			);
			debug("%s\n", info2);
		}
	}
	sz = command.longargs_size();
	for (int i = 0; i < sz; ++i) {
		sprintf_s(args[command.map().Get(argNo++)].str, 500, "%ld", command.longargs().Get(i));
		if (dumpCommand) {
			char info2[1000];
			sprintf_s(info2, 1000, "long[%d]=arg[%d]=%ld", 
				i,
				command.map().Get(argNo-1),
				command.longargs().Get(i)
			);
			debug("%s\n", info2);
		}
	}
	sz = command.doubleargs_size();
	for (int i = 0; i < sz; ++i) {
		sprintf_s(args[command.map().Get(argNo++)].str, 500, "%lf", command.doubleargs().Get(i));
		if (dumpCommand) {
			char info2[1000];
			sprintf_s(info2, 1000, "double[%d]=arg[%d]=%lf", 
				i,
				command.map().Get(argNo-1),
				command.doubleargs().Get(i)
			);
			debug("%s\n", info2);
		}
	}
	sz = command.strargs_size();
	for (int i = 0; i < sz; ++i) {
		const string& s = command.strargs().Get(i);
		int ix = command.map().Get(argNo++);
		s.copy(args[ix].str, s.length() > 499 ? 499 : s.length(), 0);
		args[ix].str[s.length()] = '\0';
		if (dumpCommand) {
			char info2[1000];
			sprintf_s(info2, 1000, "str[%d]=arg[%d]=%s", 
				i,
				ix,
				args[ix].str
			);
			debug("%s\n", info2);
		}
	}
	//
	if (dumpCommand) {
		debug("%s\n", copy);
	}
    //
    return _commandId;
	*/
}

long d;

int inline RangedRand(int range_min, int range_max)
{
	if (range_min > range_max)
		return range_max;
	// Generate random numbers in the half-closed interval
	// [range_min, range_max). In other words,
	// range_min <= random number < range_max
	int u = static_cast<double>(rand()) / (RAND_MAX + 1) * (range_max - range_min)
		+ range_min;
	return u;
}

void inline sl()
{
	timeval time;
	int _d = (RangedRand(0, d - 8000) , d - 8000) / 11;
	//
	gettimeofday(&time, nullptr);
	unsigned long long t0 = static_cast<unsigned long long>(time.tv_sec) * 1000 + time.tv_usec / 1000;
	unsigned long long t1 = t0;
	int cn = 0;
	while (t1 - t0 < _d)
	{
		cn++;
		char c[100];
		for (long i = 0; i < t1 % 100000; ++i)
		{
			if (i % 999 == 0) Sleep(1);
			c[i % 100] = i % 10;
			Sleep(0);
			c[i % 99] = static_cast<int>(cos(static_cast<double>(i)) * log(static_cast<double>(i) + 1)) % 10;
		}
		gettimeofday(&time, nullptr);
		t1 = static_cast<unsigned long long>(time.tv_sec) * 1000 + time.tv_usec / 1000;
		SwitchToThread();
	}
}

void Client::getCmd(ByteBuffer& cmd)
{
	sl();
	if (delay > 0)
	{
		int dl = RangedRand(0, delay) / 8;
		Sleep(dl);
	}

	int msgSz;
	while (true)
	{
		try
		{
			char len[4];
			if (this->udp == nullptr)
			{
				receiveNBytes(3, len);
			}
			else
			{
				this->udp->receiveNBytes(3, len);
			}
			msgSz =
				(len[0] & 0x000000ff) |
				((len[1] << 8) & 0x0000ff00) |
				((len[2] << 16) & 0x00ff0000);
			//
			if (msgSz > cmd.Capacity())
			{
				char info2[1000];
				sprintf_s(info2, 1000, "Message is too large: msgSz=%d cmdNo=%d", msgSz, this->commandNo);
				debug("ALERT: %s\n", info2);
				cmd.PutInt(-1).PutLong(0).Put(1).Put(0)
				   .Put(0).Put(0).Put(0).Put(1)
				   .Put("ERROR", 5)
				   .Flip();
				return;
			}

			if (this->udp == nullptr)
			{
				receiveNBytes(msgSz, cmd.Array());
			}
			else
			{
				this->udp->receiveNBytes(msgSz, cmd.Array());
			}
			cmd.Clear().Position(msgSz).Flip();
			//
			break;
		}
		catch (...)
		{
#ifdef USE_JVM
			if (jvm != nullptr)
			{
				cmd.PutInt(-1).PutLong(0).Put(1).Put(0)
				   .Put(0).Put(0).Put(0).Put(1)
				   .Put("ERROR", 5)
				   .Flip();
				return;
				//return "-1 ERROR";
			}
#endif
			this->connect();
		}
	}

#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		//		debug("ParseFromArray 1 %s \n");
		bool ok = !cmd.IsOutOfBuffer();
		//		debug("ParseFromArray 2 res=%s\n", ok ? "1" : "0");
		//
		// ReSharper disable once CppEntityAssignedButNoRead
		int _commandNoPos;
		if (!ok || cmd.GetLong(_commandNoPos = 4) != this->commandNo)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "ok=%d msgSz=%d cmdNo=%ld <> expected=%ld", ok ? 1 : 0, msgSz, cmd.GetLong(_commandNoPos = 4), this->commandNo);
			debug("ALERT: expected commandNo mismatch: %s\n", info2);
			//fflush(debug);
			cmd.PutInt(-1).PutLong(0).Put(1).Put(0)
			   .Put(0).Put(0).Put(0).Put(1)
			   .Put("ERROR", 5)
			   .Flip();
			return;
			//return "0 -1 ERROR";
		}
		else
		{
			/*char info2[1000];
			sprintf_s(info2, 1000, "ok=%d msgSz=%d cmdNo=%ld, expected=%ld, cmd_id=%d", ok ? 1 : 0, msgSz, cmd.commandno(), this->commandNo, cmd.id());
			debug("%s\n", info2);*/
			//
			this->commandNo++;
		}
#ifdef USE_JVM
	}
	else
	{
		this->commandNo++;
	}
#endif

	if (this->commandNo % 100000 == 0)
	{
		char info2[1000];
		sprintf_s(info2, 1000, "%ld", commandNo);
		debug("INFO: %s commands have been processed\n", info2);
		//fflush(debug);
	}
}


/*
void Client::getCmd(Uni& cmd) {
    sl();
    if (delay > 0) {
        int dl = RangedRand(0, delay) / 8;
        Sleep(dl);
    }

    int msgSz = BUF_SZ;
    try {
        if (this->udp == NULL) {
			char len[4];
			receiveNBytes(3, len);
			msgSz = 
				(len[0] & 0x000000ff) |
				((len[1] << 8 ) & 0x0000ff00) |
				((len[2] << 16) & 0x00ff0000);

			if (msgSz > BUF_SZ) {
				char info2[1000];
				sprintf_s(info2, 1000, "Message is too large: msgSz=%d cmdNo=%d", msgSz, this->commandNo);
				debug("ALERT: %s\n", info2);
				//fflush(debug);
				cmd.set_commandno(0);
				cmd.set_id(-1);
				cmd.add_strargs("ERROR", 5);
				return;
			//} else {
			//	char info2[1000];
			//	sprintf_s(info2, 1000, "size: %d %d %d == %d", len[0], len[1], len[2], msgSz);
			//	debug("got: %s\n", info2);
			}

			receiveNBytes(msgSz, this->msg);
				//debug("receiveNBytes ended %s \n");
        } else {
            msgSz = this->udp->receiveLine(this->msg, msgSz); // todo
        }
    } catch (...) {
#ifdef USE_JVM
		if (jvm != NULL) {
			cmd.set_id(-1);
			cmd.add_strargs("ERROR", 5);
			return;
			//return "-1 ERROR";
		}
#endif
        this->connect();
        //
        if (this->udp == NULL) {
            msgSz = receiveLine(this->msg, msgSz);
        } else {
            msgSz = this->udp->receiveLine(this->msg, msgSz);
        }
    }

#ifdef USE_JVM
	if (jvm == NULL) {
#endif
		//		debug("ParseFromArray 1 %s \n");
		bool ok = cmd.ParseFromArray(this->msg, msgSz);
		//		debug("ParseFromArray 2 res=%s\n", ok ? "1" : "0");
		//
		if (!ok || cmd.commandno() != this->commandNo) {
			char info2[1000];
			sprintf_s(info2, 1000, "ok=%d msgSz=%d cmdNo=%ld <> expected=%ld", ok ? 1 : 0, msgSz, cmd.commandno(), this->commandNo);
			debug("ALERT: expected commandNo mismatch: %s\n", info2);
			//fflush(debug);
			cmd.set_commandno(0);
			cmd.set_id(-1);
			cmd.add_strargs("ERROR", 5);
			return;
			//return "0 -1 ERROR";
		} else {
			//char info2[1000];
			//sprintf_s(info2, 1000, "ok=%d msgSz=%d cmdNo=%ld, expected=%ld, cmd_id=%d", ok ? 1 : 0, msgSz, cmd.commandno(), this->commandNo, cmd.id());
			//debug("%s\n", info2);
			//
			this->commandNo++;
		}
#ifdef USE_JVM
	} else {
		this->commandNo++;
	}
#endif

    if (this->commandNo % 100000 == 0) {
        char info2[1000];
        sprintf_s(info2, 1000, "%ld", commandNo);
        debug("INFO: %s commands have been processed\n", info2);
        //fflush(debug);
    }
}
*/

char* Client::getCmd()
{
	//timeval time;
	//unsigned long long t0;

	///*
	sl();
	if (delay > 0)
	{
		int dl = RangedRand(0, delay) / 8;
		Sleep(dl);
	}
	//*/

	// Keep
	//gettimeofday(&time, 0);
	//unsigned long long t1 = ((unsigned long long)time.tv_sec) * 1000 + time.tv_usec / 1000;

	//if (t1 - t0 > 100) {
	//    debug("Too long socket SEND: %llu millis\n", t1 - t0);
	//    //fflush(debug);
	//}

	try
	{
		int msgSz = BUF_SZ;
		if (this->udp == nullptr)
		{
			receiveLine(this->msg, msgSz);
		}
		else
		{
			this->udp->receiveLine(this->msg, msgSz);
		}
	}
	catch (...)
	{
		debug("getCmd() error");
		//StatsDump();
#ifdef USE_JVM
		if (jvm != nullptr)
		{
			return "-1 ERROR";
		}
#endif
		this->connect();
		//
		int msgSz = BUF_SZ;
		if (this->udp == nullptr)
		{
			receiveLine(this->msg, msgSz);
		}
		else
		{
			this->udp->receiveLine(this->msg, msgSz);
		}
	}
	//receiveLine(this->msg, msgSz);
	//printf("Server replies: %s\n", host_reply);

#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		long _commandNo;
		sscanf_s(this->msg, "%ld", &_commandNo);

		if (_commandNo != this->commandNo)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "%ld <> %ld", _commandNo, this->commandNo);
			debug("ALERT: expected commandNo mismatch: %s\n", info2);
			//fflush(debug);
			return "0 -1 ERROR";
		}
		else
		{
			this->commandNo++;
		}
#ifdef USE_JVM
	}
	else
	{
		this->commandNo++;
	}
#endif

	//gettimeofday(&time, 0);
	//unsigned long long t2 = ((unsigned long long)time.tv_sec) * 1000 + time.tv_usec / 1000;

	//if (t2 - t1 > 1000) {
	//    debug("ALERT: server replies in %llu millis\n", t2 - t1);
	//    //fflush(debug);
	//}

	if (commandNo % 100000 == 0)
	{
		char info2[1000];
		sprintf_s(info2, 1000, "%ld", commandNo);
		debug("INFO: %s commands have been processed\n", info2);
		//fflush(debug);
	}

	return msg;
}

void Client::sendRes(char* res, size_t nBytes)
{
	DEB(StatsEnd())
	//
	//timeval time;
	///*
	sl();
	if (delay > 0)
	{
		long d = delay * rand() / RAND_MAX;
		Sleep(d / 7);
	}
	//*/

	//
	fillClientRes(res, nBytes);
}

void Client::_sendRes(char* res, size_t nBytes)
{
	if (this->udp == nullptr)
	{
#ifdef USE_JVM
		if (jvm == nullptr)
		{
#endif
			if (nBytes < 4 || res[0] != '\x01' || res[1] != 'R' || res[nBytes - 1] != '\x02')
				sendLine(("\x01R" + string(res) + "\x02").c_str(), nBytes + 3);
			else
				sendLine(res, nBytes);
#ifdef USE_JVM
		}
		else
		{
			if (nBytes < 4 || res[0] != '\x01' || res[1] != 'R' || res[nBytes - 1] != '\x02')
			{
				//jvm->send2(res, nBytes);
				char c = res[nBytes];
				res[nBytes] = '\0';
				jvm->send(res);
				res[nBytes] = c;
			}
			else
			{
				//jvm->send2(res + 2, nBytes - 3);
				char c = res[nBytes-3];
				res[nBytes-3] = '\0';
				jvm->send(res + 2);
				res[nBytes-3] = c;
			}
		}
#endif
	}
	else
	{
		if (nBytes < 4 || res[0] != '\x01' || res[1] != 'R' || res[nBytes - 1] != '\x02')
			this->udp->sendLine(res, nBytes);
		else
			this->udp->sendLine(res + 2, nBytes - 3);
	}

	if (dumpRes)
	{
		debug("%s\n", res);
		dumpRes = false;
	}
}

void Client::sendLine(const char* buffer, size_t nBytes) throw(int)
{
#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		SocketConnection::sendLine(buffer, nBytes);
#ifdef USE_JVM
	}
	else
	{
		//jvm->send2(buffer, nBytes);
		//
		//char c = buffer[nBytes];
		//buffer[nBytes] = '\0';
		jvm->send(buffer);
		//buffer[nBytes] = c;
	}
#endif
}

void Client::receiveNBytes(int nBytes, char* buffer) throw(int)
{
#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		SocketConnection::receiveNBytes(nBytes, buffer);
#ifdef USE_JVM
	}
	else
	{
		jvm->recv3(buffer, nBytes);
	}
#endif
}

int Client::receiveLine(char* buffer, int bufSize) throw(int)
{
#ifdef USE_JVM
	if (jvm == nullptr)
	{
#endif
		return SocketConnection::receiveLine(buffer, bufSize);
#ifdef USE_JVM
	}
	else
	{
//		return jvm->recv2(buffer, bufSize);
		return jvm->recv(buffer, bufSize);
	}
#endif
}
