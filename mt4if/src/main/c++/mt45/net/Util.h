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
//”¶”√¿‡
#pragma once

#ifndef UTIL_H_
#define UTIL_H_

#include <time.h>
#include <windows.h>
#include <stdio.h>
#include <string>

using namespace std;

#if defined(_MSC_VER) || defined(_MSC_EXTENSIONS)
#define DELTA_EPOCH_IN_MILLIS		11644473600000Ui64
#define DELTA_EPOCH_IN_MICROSECS	11644473600000000Ui64
#else
	#define DELTA_EPOCH_IN_MILLIS		11644473600000ULL
	#define DELTA_EPOCH_IN_MICROSECS	11644473600000000ULL
#endif

struct timezone
{
	int tz_minuteswest; /* minutes W of Greenwich */
	int tz_dsttime; /* type of dst correction */
};

int gettimeofday(struct timeval* tv, struct timezone* tz);

static bool volatile hdrTimePrinted = false;

class Util
{
public:
	static string getCurrentTime()
	{
		char res[40];
		if (hdrTimePrinted)
		{
			SYSTEMTIME lt;
			//FILETIME ft_now;
			GetLocalTime(&lt);
			//GetSystemTimeAsFileTime(&ft_now);
			//LONGLONG ll_now = ((LONGLONG)ft_now.dwLowDateTime + ((LONGLONG)(ft_now.dwHighDateTime) << 32LL)) / 10000LL;
			//int millis = ((int) ll_now - (ll_now / 1000LL) * 1000LL);
			sprintf_s(res, 40, "%02d %02d:%02d:%02d.%03d", lt.wDay, lt.wHour, lt.wMinute, lt.wSecond, lt.wMilliseconds);
		}
		else
		{
			hdrTimePrinted = true;
			time_t rawtime;
			time(&rawtime);
			ctime_s(res, 27, &rawtime);
			res[strlen(res) - 1] = NULL; // remove \n at the end
		}
		//int l = (int) strlen(res);
		//if (l > 1) res[strlen(res) - 1] = '\0';
		return string(res);
	}

	static __int64 currentTimeMillis()
	{
		FILETIME ft;
		GetSystemTimeAsFileTime(&ft);
		LONGLONG ll_now = (static_cast<LONGLONG>(ft.dwLowDateTime) + (static_cast<LONGLONG>(ft.dwHighDateTime) << 32LL)) / 10000LL;
		return ll_now - DELTA_EPOCH_IN_MILLIS;
	}

	static void sleep(int sec)
	{
#if (defined _WINDOWS_H || defined _WINDOWS_)
		Sleep(1000 * sec);
#else
		//sleep(1*sec);
#endif
	}
};

#endif /*UTIL_H_*/
