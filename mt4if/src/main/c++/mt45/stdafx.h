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

#include "targetver.h"
#include "net/Client.h"
#include "jvm/MyJvm.h"
#include "Mql.h"

//#define USE_MT4_THREADS

#define WIN32_LEAN_AND_MEAN             // Exclude rarely-used stuff from Windows headers
// Windows Header Files:
#include <windows.h>
#include <memory>

#define DEBUG_DLL 0
#define HWND_DEBUG DEBUG_DLL
#define USE_WCS 0

#if USE_WCS
#define MCHAR wchar_t
#else
#define MCHAR char
#endif


namespace std{
	class mutex;
}

extern int volatile IS_DLL_IN_DEBUG_MODE;

extern char* NJ4X_UUID;

#ifndef _T
#define _T(x) L##x
#endif // !_T


#ifdef _DEBUG
#define DEB(x) x;
#else
#if DEBUG_DLL
#define DEB(x) x;
#else
#define DEB(x) 
#endif // DEBUG_DLL
#endif // _DEBUG

#define BOXUTILS

EXTERN_C int WINAPI GetModuleFullName(
	__in  HMODULE hModule,
	__out LPWSTR pszBuffer,
	__in  int nMaxChars);

#define MT4_EXPFUNC __declspec(dllexport)
//确定了让外部调用的函数
extern "C" MT4_EXPFUNC wchar_t const* __stdcall jfxConnect(wchar_t const* symbol, int period, wchar_t const* strategy);
extern "C" MT4_EXPFUNC void __stdcall jfxDisconnect(wchar_t const* sessID);
extern "C" MT4_EXPFUNC int __stdcall jfxGetCommand(wchar_t const* sessionID, wchar_t* p1, wchar_t* p2, wchar_t* p3, wchar_t* p4, wchar_t* p5, wchar_t* p6, wchar_t* p7, wchar_t* p8, wchar_t* p9, wchar_t* p10, wchar_t* p11, wchar_t* p12, wchar_t* p13, wchar_t* p14, wchar_t* p15);
extern "C" MT4_EXPFUNC void __stdcall jfxSendResult(wchar_t const* sessID, wchar_t const* res);

extern "C" MT4_EXPFUNC void __stdcall jfxLog(wchar_t const* info);

extern "C" MT4_EXPFUNC void __stdcall jfxPositionInit(wchar_t const* sessID, int mode);
extern "C" MT4_EXPFUNC int __stdcall jfxPositionOrderInfo(wchar_t const* sessID, int is_history, int ticket, int type, int openTime, int closeTime, int magic, int expiration, wchar_t const* symbol, wchar_t const* comment, double lots, double openPrice, double closePrice, double sl, double tp, double profit, double commission, double swap);
extern "C" MT4_EXPFUNC wchar_t const* __stdcall jfxPositionRes(wchar_t const* sessID, int tCount, int hCount);

extern "C" MT4_EXPFUNC int __stdcall jfxMqlRatesInit(wchar_t const* sessID);
extern "C" MT4_EXPFUNC int __stdcall jfxMqlRatesAdd(wchar_t const* sessID, MqlRates* rates);
extern "C" MT4_EXPFUNC wchar_t const* __stdcall jfxMqlRatesRes(wchar_t const* sessID);

extern HANDLE volatile hThreads;

extern mutex sDebug, sConnections, sJvm, sThreads;
extern __declspec(align(32)) unsigned int volatile sStatsXP;
extern __declspec(align(32)) unsigned int volatile sDebugXP;
extern __declspec(align(32)) unsigned int volatile sConnectionsXP;
extern __declspec(align(32)) unsigned int volatile sDllInitXP;
extern __declspec(align(32)) unsigned int volatile sJvmXP;

extern bool my_lock(volatile unsigned int* key, int tmoutMillis, bool busySpin);
extern bool my_lock(volatile unsigned int* key);
extern void my_unlock(volatile unsigned int* key);

extern int volatile proto;

extern const char* tombs(const wchar_t* wc, char* c);
extern const char* tombs(const wchar_t* wc, char* c, size_t csz);

extern void debug(const char* fmt, const wchar_t* p1);
extern void debug(const char* fmt, ...);

typedef void (CALLBACK* WCSTOMBSPROC)(char*, size_t, LPARAM);
extern size_t mywcstombs(const wchar_t* res, char* b, size_t csz);
extern size_t mywcstombs(const wchar_t* res, WCSTOMBSPROC proc, LPARAM param, MCHAR *prefix, MCHAR suffix);
extern size_t mywcstombs(const wchar_t* res, char* b, size_t csz, WCSTOMBSPROC proc, LPARAM param, MCHAR *prefix, MCHAR suffix);
