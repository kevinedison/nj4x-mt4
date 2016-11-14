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
#include "stdafx.h"
#include "SocketConnection.h"
#include <winsock2.h>
#include <Ws2tcpip.h>
#include <stdio.h>

// Link with ws2_32.lib
#pragma comment(lib, "Ws2_32.lib")

extern void debug(const char* fmt, ...);
extern void debug(const char* fmt, const char* p1, const wchar_t* p2);
extern void debug(const char* fmt, const wchar_t* p1, const char* p2);
extern void debug(const char* fmt, const wchar_t* p1, const wchar_t* p2);

int tmoutInfoPrinted;

void SocketConnection::init()
{
	maxDebug = (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1);
	bufPos = bufLen = 0;
	connAttempt = 0;
	gettimeofday(&(this->rcvTime), nullptr);
	char* tmout = getenv("JFX_TERM_IDLE_TMOUT_SECONDS");
	if (tmout == nullptr)
	{
		idleTmoutSeconds = 3600 * 6;
		if (tmoutInfoPrinted != 1)
		{
			debug("Terminal idle timeout set to %lu sec (default, change by JFX_TERM_IDLE_TMOUT_SECONDS env. variable)\n", idleTmoutSeconds);
			tmoutInfoPrinted = 1;
		}
	}
	else
	{
		idleTmoutSeconds = atol(tmout);
	}
}

SocketConnection::SocketConnection()
{
	init();
}

SocketConnection::SocketConnection(LPWSTR pNodeName, int port)
{
	init();
#ifdef SMART_SOCKET
#ifdef _WINSOCK_H
	ADDRINFOW hints;
	ZeroMemory( &hints, sizeof(hints) );
	hints.ai_family = AF_UNSPEC ;
	hints.ai_socktype = SOCK_STREAM ;
	hints.ai_protocol = IPPROTO_TCP;

	ADDRINFOW* ptr = nullptr;
	DWORD rv = GetAddrInfoW(pNodeName, nullptr, &hints, &ptr);
	if (rv != 0)
	{
		// ReSharper disable once CppEntityNeverUsed
		int e = WSAGetLastError();
		debug("GetAddrInfoW error.\n");
		throw -1;
	}

	wchar_t ipstringbuffer[46];
	ZeroMemory( ipstringbuffer, sizeof(ipstringbuffer) );
	DWORD ipbufferlength = 46;
	INT iRetval;

	hints.ai_family = ptr->ai_family;
	switch (ptr->ai_family)
	{
	case AF_INET:
		LPSOCKADDR sockaddr_ip;
		sockaddr_ip = static_cast<LPSOCKADDR>(ptr->ai_addr);
		// The buffer length is changed by each call to WSAAddresstoString
		// So we need to set it for each iteration through the loop for safety
		iRetval = WSAAddressToString(
			sockaddr_ip, static_cast<DWORD>(ptr->ai_addrlen), nullptr,
			ipstringbuffer, &ipbufferlength
		);
		if (iRetval)
		{
			//wprintf(L"WSAAddressToString failed with %u\n", WSAGetLastError() );
			debug("WSAAddressToString failed.\n");
		}
		break;
	case AF_INET6:
		//wprintf(L"AF_INET6 (IPv6)\n");
		// the InetNtop function is available on Windows Vista and later
		// sockaddr_ipv6 = (struct sockaddr_in6 *) ptr->ai_addr;
		// printf("\tIPv6 address %s\n",
		//    InetNtop(AF_INET6, &sockaddr_ipv6->sin6_addr, ipstringbuffer, 46) );

		// We use WSAAddressToString since it is supported on Windows XP and later
		sockaddr_ip = static_cast<LPSOCKADDR>(ptr->ai_addr);
		// The buffer length is changed by each call to WSAAddresstoString
		// So we need to set it for each iteration through the loop for safety
		ipbufferlength = 46;
		iRetval = WSAAddressToString(sockaddr_ip, static_cast<DWORD>(ptr->ai_addrlen), nullptr,
		                             ipstringbuffer, &ipbufferlength);
		if (iRetval)
		{
			//wprintf(L"WSAAddressToString failed with %u\n", WSAGetLastError() );
			debug("WSAAddressToString failed.\n");
		}
		//
		break;
	default:
		debug("ai_family not INET*.\n");
		break;
	}

	FreeAddrInfoW(ptr);

	int len = sizeof(this->address);
	int temp = WSAStringToAddress(ipstringbuffer, hints.ai_family, nullptr, reinterpret_cast<sockaddr*>(&(this->address)), &len);
#else
	int temp = inet_pton(AF_INET, this->serverAddress, &(this->address.sin_addr));
#endif

	if (isSocketError(temp))
	{
		// ReSharper disable once CppEntityNeverUsed
		int e = WSAGetLastError();
		debug("WSAStringToAddress error.\n");
		throw -1;
	}

	/* Now the fun begins */
	this->socketId = socket(AF_INET, SOCK_STREAM, 0);

	// ReSharper disable once CppEntityNeverUsed
	int err = WSAGetLastError();

	if (!SocketConnection::isSocketOk(this->socketId))
	{
		debug("socket error.\n");
		throw -2;
	}

	this->address.sin_port = htons(port);
	if (!_connect())
	{
		debug("conn error.\n");
		throw -3;
	}
#endif
}

SocketConnection::SocketConnection(SOCKET socket, sockaddr_in addr)
{
	init();
	socketId = socket;
	address = addr;
	_connect();
}

bool SocketConnection::isHealthy()
{
	return isSocketOk(socketId);
}

void SocketConnection::_disconnect()
{
	//
	if (isSocketOk(this->socketId))
	{
		try
		{
			//debug("%s> Closing Socket ...\n", Util::getCurrentTime().c_str(), "");
			//fflush(debug);
			closesocket(this->socketId);
		}
		catch (...)
		{
			debug("ALERT: Closing Socket Error ...\n");
			//fflush(debug);
		}
#ifdef _WINSOCK_H
		this->socketId = INVALID_SOCKET;
#else
		this->socketId = -1;
#endif
	}
}

SocketConnection::~SocketConnection()
{
	//debug("%s> in SocketConnection::~SocketConnection() ...\n", Util::getCurrentTime().c_str(), "");
	_disconnect();
}

void SocketConnection::receiveNBytes(int nBytes, char* buffer) throw(int)
{
	int num_bytes_received = 0;
	//	int argStarts = 0;

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
				if (maxDebug)
				{
					debug("Rcvd [%s]\n", buffer);
					//fflush(debug);
				}
				//
				gettimeofday(&(this->rcvTime), nullptr);
				//
				return;
			}
		}

		if (num_bytes_received >= nBytes)
		{
			bufPos = savedBufPos;
			break;
		}

		bufLen = recv(this->socketId, buf, BUF_SZ, 0);
		bufPos = 0;

		if (bufLen <= 0)
		{
			bufLen = bufPos = 0;
			if (maxDebug)
			{
				debug("recv error.\n");
			}
			//fflush(debug);
			throw 1;
		}
	}

	debug("buffer overflow.\n");
	//fflush(debug);
	throw 2;
}

void SocketConnection::sendNBytes(int nBytes, const char* buffer) throw(int)
{
	int num_bytes_sent = 0;
	while (num_bytes_sent < nBytes)
	{
		int n = send(this->socketId, buffer + num_bytes_sent, nBytes - num_bytes_sent, 0);

		if (n <= 0)
		{
			debug("send error.\n");
			//fflush(debug);
			throw 1;
		}

		num_bytes_sent += n;

		if (num_bytes_sent < nBytes)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "%d bytes (should be %d)", num_bytes_sent, nBytes);
			debug("Sent %s\n", info2);
			//fflush(debug);
		}
	}
}

void SocketConnection::sendLine(string const& line) throw(int)
{
	sendLine(line.c_str(), line.length());
}

void SocketConnection::sendLine(const char* buffer, size_t nBytes) throw(int)
{
	//size_t nBytes = strlen(buffer);
	sendNBytes(nBytes, buffer);
	if (maxDebug)
	{
		debug("Sent [%s]\n", buffer);
		//fflush(debug);
	}
	//sendNBytes(1, "\n");
}

int SocketConnection::receiveLine(char* buffer, int bufSize) throw(int)
{
	int nBytes = bufSize - 1;
	int num_bytes_received = 0;
	int argStarts = 0;
	char firstChar = 0;

	while (true)
	{
		int savedBufPos = bufPos;

		while (bufPos < bufLen && num_bytes_received < nBytes)
		{
			char c = buf[bufPos++];
			if (firstChar == 0)
			{
				firstChar = c;
			}
			//            if (c == '\n') {
			if (c == '\x02' && argStarts == 1 || firstChar != '\x01' && c == '\n')
			{
				// line is complete
				buffer[num_bytes_received] = '\0';
				//
				if (maxDebug)
				{
					debug("Rcvd [%s]\n", buffer);
					//fflush(debug);
				}
				//
				gettimeofday(&(this->rcvTime), nullptr);
				//
				return num_bytes_received;
			}
			else
			{
				if (argStarts > 0 || firstChar != '\x01')
				{
					buffer[num_bytes_received++] = c;
				}
				//
				if (c == '\x01')
				{
					argStarts++;
				}
				else if (c == '\x02')
				{
					argStarts--;
				}
			}
		}

		if (num_bytes_received >= nBytes)
		{
			bufPos = savedBufPos;
			break;
		}

		bufLen = recv(this->socketId, buf, BUF_SZ, 0);
		bufPos = 0;

		if (bufLen <= 0)
		{
			bufLen = bufPos = 0;
			if (maxDebug)
			{
				debug("recv error.\n");
				//} else {
				//	debug("(");
			}
			//fflush(debug);
			throw 1;
		}
	}

	debug("buffer overflow.\n");
	//fflush(debug);
	throw 2;
}

#ifdef _WINSOCK_H
bool SocketConnection::isSocketOk(SOCKET socket)
{
	return socket != INVALID_SOCKET;
}
#else
bool SocketConnection::isSocketOk(int socket) {
	return socket < 0;
}
#endif

bool SocketConnection::isSocketError(int result)
{
#ifdef _WINSOCK_H
	return result == SOCKET_ERROR;
#else
		return result < 0;
#endif
}

extern bool wmclose();
extern int hwnd;

bool SocketConnection::_connect()
{
	this->connAttempt++;
	int temp;
#ifdef _WINSOCK_H
	temp = connect(socketId, reinterpret_cast<sockaddr*>(&address), sizeof(address));
#else
#ifdef __CYGWIN__
			temp = connect(this->socketId, (sockaddr*)&(this->address), sizeof(this->address));
#else
			temp = connect(this->socketId, &(this->address), sizeof(this->address));
#endif
#endif

	if (isSocketError(temp))
	{
		//
		timeval time;
		gettimeofday(&time, nullptr);
		if (this->connAttempt > 10 && (time.tv_sec - this->rcvTime.tv_sec) > this->idleTmoutSeconds)
		{
			char info2[1000];
			sprintf_s(info2, 1000, "ABORT: Can not connect more than %lu sec (see JFX_TERM_IDLE_TMOUT_SECONDS env. variable), closing with hwnd=%d", idleTmoutSeconds, hwnd);
			debug("\n%s\n", info2);
			if (!wmclose())
			{
				debug("WM_CLOSE failed\n");
			}
		}
		//
		return FALSE;
		//
	}
	else
	{
		//debug("\n%s> [+]\n", Util::getCurrentTime().c_str(), "");
		debug("\n");
		debug("DLL: %s [+]\n", NJ4X_UUID);
		this->connAttempt = 0;
	}

	gettimeofday(&(this->rcvTime), nullptr);
	return TRUE;
}

void SocketConnection::_bind()
{
	/*  Bind our server socket to a local IPv4 address and associated port*/
	int temp;
#ifdef _WINSOCK_H
	temp = bind(socketId, reinterpret_cast<sockaddr*>(&address), sizeof(address));
#else
#ifdef __CYGWIN__
			temp = bind (this->socketId, (sockaddr*) &address, sizeof(address));
#else
			temp = bind (this->socketId, &address, sizeof(struct sockaddr_in));
#endif
#endif

	if (isSocketError(temp))
	{
		debug("unable to bind address\n");
		//fflush(debug);
		throw temp;
	}
}


int gettimeofday(struct timeval* tv, struct timezone* tz)
{
	FILETIME ft;
	unsigned __int64 tmpres = 0;
	static int tzflag;

	if (NULL != tv)
	{
		GetSystemTimeAsFileTime(&ft);

		tmpres |= ft.dwHighDateTime;
		tmpres <<= 32;
		tmpres |= ft.dwLowDateTime;

		/*converting file time to unix epoch*/
		tmpres -= DELTA_EPOCH_IN_MICROSECS;
		tmpres /= 10; /*convert into microseconds*/
		tv->tv_sec = static_cast<long>(tmpres / 1000000UL);
		tv->tv_usec = static_cast<long>(tmpres % 1000000UL);
	}

	if (NULL != tz)
	{
		if (!tzflag)
		{
			_tzset();
			tzflag++;
		}
		long tzsecs;
		_get_timezone(&tzsecs);
		tz->tz_minuteswest = tzsecs / 60;
		int dl;
		_get_daylight(&dl);
		tz->tz_dsttime = dl;
	}

	return 0;
}
