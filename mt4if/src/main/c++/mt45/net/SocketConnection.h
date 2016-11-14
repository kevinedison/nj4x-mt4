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
#ifndef SERVER_SOCKET_LISTENER_H_
#define SERVER_SOCKET_LISTENER_H_

#ifdef _WIN32
#include <winsock2.h>
#define _WINSOCK_H
#pragma warning( disable : 4290 )
#else
    #include <arpa/inet.h>
    #include <errno.h>
    #include <fcntl.h>
    #include <netinet/in.h>
    #include <signal.h>
    #include <sys/socket.h>
    #include <unistd.h>
#endif

#include <string>
#include "Mql.h"

#define LISTENQ 5

class SocketConnection
{
public:
	void init();
#ifdef _WINSOCK_H
	SOCKET socketId;
#else
    int socketId;
#endif
	struct sockaddr_in address;
	//char desc[128];
	//
	char buf[BUF_SZ];
	int bufPos, bufLen;
	bool maxDebug;
	timeval rcvTime;
	int connAttempt;
	long idleTmoutSeconds;

public:
	void sendNBytes(int nBytes, const char* buffer) throw(int);
	virtual void receiveNBytes(int nBytes, char* buffer) throw(int);
	//
	void sendLine(std::string const& line) throw(int);
	virtual void sendLine(const char* buffer, size_t nBytes) throw(int);
	virtual int receiveLine(char* buffer, int bufSize) throw(int);
	//
protected:
	SocketConnection();
	//
#ifdef _WINSOCK_H
	bool isSocketOk(SOCKET socket);
#else
    bool isSocketOk(int socket);
#endif
	bool isSocketError(int result);
	bool _connect();
	void _disconnect();
	void _bind();
public:
	SocketConnection(SOCKET socket, sockaddr_in addr);
	SocketConnection(LPWSTR serverAddress, int port);
	virtual ~SocketConnection();
	//
	bool isHealthy();
	//virtual void run() = 0;
};

#endif /*SERVER_SOCKET_LISTENER_H_*/
