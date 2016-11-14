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
#pragma once
#ifdef USE_JVM

#ifndef MYJVM_H_
#define MYJVM_H_

#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers

#include "stdafx.h"

#include "jni.h"
#ifdef STATIC_JVM
#pragma comment(lib, _T(STATIC_JVM))
#endif

#define CLASS_NAME "com/jfx/net/InprocessServer"
#define JCHAR_RWA_SZ 10240

extern char jvm_options[];
extern JavaVM* vm;
extern JavaVMInitArgs vm_args;
extern JavaVMOption options[];
extern JNIEnv* env;

void destroy_vm();

class MyJvm
{
	jobject socket;
	jcharArray r, w;
	jchar ra[JCHAR_RWA_SZ];
	jchar wa[JCHAR_RWA_SZ];

	jbyteArray byte_r;
	int bufPos, bufLen;

public:
	MyJvm() throw(int);
	~MyJvm();
	void connect(const char* s);
	void send(const char* s);
	int recv(char* buffer, int bufSize);
	void send2(const char* s, size_t nBytes);
	int recv2(char* buffer, int bufSize);
	int recv3(char* buffer, int bufSize);
	void close();
};

#endif /*MYJVM_H_*/
#endif /* USE_JVM */
