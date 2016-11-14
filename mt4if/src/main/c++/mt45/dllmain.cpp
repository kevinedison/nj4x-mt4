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
// dllmain.cpp : Defines the entry point for the DLL application.
#include "stdafx.h"
#include "box_id.h"

#include <mutex>
#include <tchar.h>
#include <psapi.h>
#pragma comment(lib, "psapi")
#include <net/Util.h>

#define LOCKED 0xDEAD
#define UNLOCKED 0xBEAF

static FILE* fDebug = nullptr;
//HANDLE hDebug, hConnections, hJvm;
HANDLE volatile hTrialQuery = nullptr;
HANDLE volatile hThreads = nullptr;

mutex sStats;
mutex sDebug;
mutex sConnections;
mutex sJvm;
mutex sTrialQuery;
mutex sThreads;
mutex sDllInit;
__declspec(align(32)) unsigned int volatile sStatsXP = UNLOCKED;
__declspec(align(32)) unsigned int volatile sDebugXP = UNLOCKED;
__declspec(align(32)) unsigned int volatile sConnectionsXP = UNLOCKED;
__declspec(align(32)) unsigned int volatile sDllInitXP = UNLOCKED;
__declspec(align(32)) unsigned int volatile sJvmXP = UNLOCKED;
//__declspec(align(32)) unsigned int volatile sTrialQuery = UNLOCKED;
//__declspec(align(32)) unsigned int volatile sThreads = UNLOCKED;

static DWORD volatile PID = NULL;
static bool volatile hdrPrinted = false;
static long long volatile debug_rec_no = 0;


int volatile proto = 0;
int hwnd = 0;
int volatile IS_DLL_IN_DEBUG_MODE = DEBUG_DLL; // 0=default, 1=debug, 2=silent
char* NJ4X_UUID = "29a50980516c";

bool my_lock(volatile unsigned int* key, int tmoutMillis, bool busySpin)
{
	int i = 0;
	__int64 start = busySpin ? Util::currentTimeMillis() : 0;
	unsigned int v = 0;
	unsigned int TID = GetCurrentThreadId();
	while (UNLOCKED != (v = InterlockedCompareExchange(key, TID, UNLOCKED))
		&& TID != (v = InterlockedCompareExchange(key, TID, TID))
		&& (busySpin && (++i % 100 != 0 || Util::currentTimeMillis() - start < tmoutMillis) || !busySpin && ++i < tmoutMillis))
		Sleep(busySpin ? 0 : 1);
	return v != UNLOCKED && v != TID;
}

bool my_lock(volatile unsigned int* key)
{
	return my_lock(key, 60000, false);
}

void my_unlock(volatile unsigned int* key)
{
	unsigned int TID = GetCurrentThreadId();
	InterlockedCompareExchange(key, UNLOCKED, TID);
}

BOOL CALLBACK EnumChildProc(
	_In_  HWND hwnd,
	      _In_  LPARAM cmd
)
{
#if HWND_DEBUG
	printf("\nEnumChildProc hwnd=%#8p cmd=%u", hwnd, cmd);

#endif // HWND_DEBUG


	ShowWindow(hwnd, static_cast<int>(cmd));
	return TRUE; // continue enumeration
}

struct OwnerHwndAndAction
{
	HWND owner;
	int cmd;
};

void getWindowInfo(HWND hWnd, LPTSTR str, int max)
{
	int len = GetWindowText(hWnd, str, max);
	if (max - len > 2)
	{
		str[len++] = ' ';
		str[len] = NULL;
		GetClassName(hWnd, str, max - len);
	}
}

BOOL CALLBACK showHideOwnedWindows(
	__in  HWND hWnd,
	      __in  LPARAM lParam
)
{
	OwnerHwndAndAction* h = reinterpret_cast<OwnerHwndAndAction*>(lParam);

	if (GetWindow(hWnd, GW_OWNER) == h->owner)
	{
		wchar_t str[1024];
		GetClassNameW(hWnd, str, 1024);
#if HWND_DEBUG
		printf("\nshowHideOwnedWindows owner=%#8p owned=%#8p class=%ls", h->owner, hWnd, str);

#endif // HWND_DEBUG


		if (wcscmp(str, L"#32770") == 0)
		{
			ShowWindow(hWnd, h->cmd);
			return FALSE;
		}
	}

	return TRUE; // continue enumeration
}

bool show_window(HWND hwnd, int cmd)
{
#if HWND_DEBUG
	printf("\nshow_window hwnd=%#8p cmd=%u", hwnd, cmd);

#endif // HWND_DEBUG


	if (hwnd != nullptr)
	{
		ShowWindow(hwnd, cmd);

		OwnerHwndAndAction instructions;
		instructions.owner = hwnd;
		instructions.cmd = cmd;
		::EnumWindows(showHideOwnedWindows, reinterpret_cast<LPARAM>(&instructions));

		//if (cmd == SW_HIDE)
		//    EnumChildWindows(hwnd, EnumChildProc, (LPARAM)cmd);
	}
	//
	return hwnd != nullptr;
}

BOOL is_main_window(HWND handle)
{
	return GetWindow(handle, GW_OWNER) == static_cast<HWND>(nullptr)
		&& GetWindow(handle, GW_CHILD) != nullptr;
}

BOOL CALLBACK enumWindowsProc(
	__in  HWND hWnd,
	      __in  LPARAM lParam
)
{
	HWND* PID_in_HWND_out = reinterpret_cast<HWND*>(lParam);

	DWORD proccesID = 0;
	if (0 != GetWindowThreadProcessId(hWnd, &proccesID)
		&& is_main_window(hWnd)
		&& reinterpret_cast<DWORD>(PID_in_HWND_out[0]) == proccesID
	)
	{
#if HWND_DEBUG
		printf("\nenumWindowsProc pid=%u hwnd=%#8p isMainWnd=%u", proccesID, hWnd, is_main_window(hWnd));

#endif // HWND_DEBUG


		PID_in_HWND_out[1] = hWnd;
		return FALSE; // stop enumeration
	}

	return TRUE; // continue enumeration
}

HWND get_window_handle_by_PID(int pid)
{
#if HWND_DEBUG
	printf("\nget_window_handle_by_PID pid=%u", pid);
#endif // HWND_DEBUG


	HWND PID_in_HWND_out[2];
	PID_in_HWND_out[0] = reinterpret_cast<HWND>(pid);
	PID_in_HWND_out[1] = static_cast<HWND>(nullptr);
	::EnumWindows(enumWindowsProc, reinterpret_cast<LPARAM>(PID_in_HWND_out));
#if HWND_DEBUG
	printf("\nget_window_handle_by_PID pid=%u hwnd=%#8p", pid, PID_in_HWND_out[1]);
#endif // HWND_DEBUG


	return PID_in_HWND_out[1];
}


#define TA_FAILED 0
#define TA_SUCCESS_CLEAN 1
#define TA_SUCCESS_KILL 2
#define TA_SUCCESS_16 3

BOOL CALLBACK TerminateAppEnum2(HWND hwnd, LPARAM pid)
{
	DWORD dwID;

	GetWindowThreadProcessId(hwnd, &dwID);

	if (dwID == static_cast<DWORD>(pid) && GetWindow(hwnd, GW_OWNER) == static_cast<HWND>(nullptr))
	{
		// ReSharper disable once CppEntityNeverUsed
		BOOL res = PostMessage(hwnd, WM_CLOSE, 0, 0);
#if HWND_DEBUG
		printf("\nTerminateAppEnum: pid=%u, HWND=%#8p wm_close_res=%u last_err=%u", dwID, hwnd, res, GetLastError());
#endif
	}

	return TRUE;
}

bool wmclose()
{
	DWORD pid = GetCurrentProcessId();

	HANDLE hProc;
	// ReSharper disable once CppEntityAssignedButNoRead
	DWORD dwRet;
	// If we can't open the process with PROCESS_TERMINATE rights,
	// then we give up immediately.
	hProc = OpenProcess(SYNCHRONIZE | PROCESS_TERMINATE, FALSE, pid);

	if (hProc == nullptr)
	{
#if HWND_DEBUG
		printf("\nTerminateApp: hProc == NULL pid=%u", pid);
#endif
		return FALSE;
	}

#if HWND_DEBUG
	printf("\nTerminateApp: hProc==%p pid=%u", hProc, pid);
#endif

	//
	HWND hwnd = get_window_handle_by_PID(pid);
	//BOOL pmRes = PostMessage(hwnd, WM_CLOSE, 0, 0);
	// ReSharper disable once CppEntityNeverUsed
	BOOL pmRes = PostMessage(hwnd, WM_SYSCOMMAND, 0xF060, 0);
#if HWND_DEBUG
	printf("\PostMessage: pid=%u, window_handle_by_PID wm_close_res=%u last_err=%u", pid, pmRes, GetLastError());
#endif
	// Wait on the handle. If it signals, great. If it times out,
	// then you kill it.
	int res = WaitForSingleObject(hProc, 10000);
	if (res != WAIT_OBJECT_0)
	{
#if HWND_DEBUG
		printf("\nTerminateApp: WaitForSingleObject timed out 1 pid=%u res=%d last_err=%u", pid, res, GetLastError());
#endif
		// TerminateAppEnum() posts WM_CLOSE to all windows whose PID
		// matches your process's.
		EnumWindows(static_cast<WNDENUMPROC>(TerminateAppEnum2), static_cast<LPARAM>(pid));

		res = WaitForSingleObject(hProc, 5000);
		if (res != WAIT_OBJECT_0)
		{
#if HWND_DEBUG
			printf("\nTerminateApp: WaitForSingleObject timed out 2 pid=%u res=%d last_err=%u", pid, res, GetLastError());
#endif
			int exit = TerminateProcess(hProc, 0);
#if HWND_DEBUG
			printf("\nTerminateApp: kill pid=%u exit=%u", pid, exit);
#endif
			// ReSharper disable once CppAssignedValueIsNeverUsed
			dwRet = (exit ? TA_SUCCESS_KILL : TA_FAILED);
		}
		else
		{
#if HWND_DEBUG
			printf("\nTerminateApp: WM_CLOSE succeeded, pid=%u", pid);
#endif
			// ReSharper disable once CppAssignedValueIsNeverUsed
			dwRet = TA_SUCCESS_CLEAN;
		}
	}
	else
	{
#if HWND_DEBUG
		printf("\nTerminateApp: WM_CLOSE succeeded, pid=%u", pid);
#endif
		// ReSharper disable once CppAssignedValueIsNeverUsed
		dwRet = TA_SUCCESS_CLEAN;
	}

	CloseHandle(hProc);
	return false;
}

bool swhide()
{
	return show_window(get_window_handle_by_PID(GetCurrentProcessId()), SW_HIDE);
}

bool swshow()
{
	return show_window(get_window_handle_by_PID(GetCurrentProcessId()), SW_SHOW);
}

DWORD wcsToUtf8(size_t* sz, char* buf, size_t bufSz, const wchar_t* res, size_t maxCountInBytes)
{

	//	err = wcstombs_s(&i, b + (prefix ? plen : 0), csz - (prefix ? plen + 1 : 0), res, _TRUNCATE);

	SetLastError(0);
	//	int WideCharToMultiByte(
	//		_In_      UINT    CodePage,
	//		_In_      DWORD   dwFlags,
	//		_In_      LPCWSTR lpWideCharStr,
	//		_In_      int     cchWideChar,
	//		_Out_opt_ LPSTR   lpMultiByteStr,
	//		_In_      int     cbMultiByte,
	//		_In_opt_  LPCSTR  lpDefaultChar,
	//		_Out_opt_ LPBOOL  lpUsedDefaultChar
	//	);
	*sz = WideCharToMultiByte(
		CP_UTF8/*CodePage*/,
		0/*dwFlags*/,
		res/*lpWideCharStr*/,
		-1/*cchWideChar*/,
		buf /*lpMultiByteStr*/,
		bufSz /*cbMultiByte */,
		nullptr/*lpDefaultChar*/,
		nullptr/*lpUsedDefaultChar*/
	);
	return GetLastError();
}

const char* tombs(const wchar_t* wc, char* c)
{
	return tombs(wc, c, 1000);
}

const char* tombs(const wchar_t* wc, char* c, size_t csz)
{
	// ReSharper disable once CppEntityNeverUsed
//	size_t n = wcstombs(c, wc, csz - 2);
//	c[csz - 1] = '\0';
//	return c;
	size_t n = WideCharToMultiByte(
		CP_UTF8/*CodePage*/,
		0/*dwFlags*/,
		wc/*lpWideCharStr*/,
		-1/*cchWideChar*/,
		c /*lpMultiByteStr*/,
		csz - 2 /*cbMultiByte */,
		nullptr/*lpDefaultChar*/,
		nullptr/*lpUsedDefaultChar*/
	);
	c[csz - 1] = '\0';
	return c;
}

size_t mywcstombs(const wchar_t* res, char* b, size_t csz)
{
	return mywcstombs(res, b, csz, nullptr, 0, nullptr, '\0');
}

size_t mywcstombs(const wchar_t* res, WCSTOMBSPROC proc, LPARAM param, MCHAR* prefix, MCHAR suffix)
{
	return mywcstombs(res, nullptr, 0, proc, param, prefix, suffix);
}

size_t mywcstombs(const wchar_t* res, char* b, size_t csz, WCSTOMBSPROC proc, LPARAM param, MCHAR* prefix, MCHAR suffix)
{
	char _b[RES_BUF_SZ];
	if (b == nullptr)
	{
		b = _b;
		csz = RES_BUF_SZ;
	}
	char* buffer = nullptr;
	wchar_t* _res = nullptr;
	size_t len = 0;
	size_t plen = prefix ? strlen(prefix) : 0;
	errno_t err;
	try
	{
		size_t i;
		err = wcsToUtf8(&i, b + (prefix ? plen : 0), csz - (prefix ? plen + 1 : 0), res, _TRUNCATE);
		if (err == 0)
		{
			len = i == 0 ? 0 : i - 1;
			if (proc != nullptr)
			{
				if (prefix) {
					memcpy(b, prefix, plen);
					b[len + plen] = suffix;
					b[len + plen + 1] = '\0';
					len += plen + 1;
				}
				proc(b, len, param);
			}
		}
		else
		{
			if (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1)
			{
				wchar_t info[RES_BUF_SZ];
				swprintf_s(info, RES_BUF_SZ, L"wcstombs_s err=%d", err);
				debug("mywcstombs: %s\n", info);
				//debug("%s> mywcstombs: res=[%s]\n", Util::getCurrentTime().c_str(), res);
			}
			if (err == EILSEQ)
			{
				size_t wlen = wcslen(res);
				size_t wsz = (wlen + 1) * sizeof(wchar_t);
				if (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1)
				{
					wchar_t info[RES_BUF_SZ];
					swprintf_s(info, RES_BUF_SZ, L"wlen=%d wsz=%d", wlen, wsz);
					debug("mywcstombs: %s\n", info);
				}
				_res = static_cast<wchar_t*>(malloc(wsz));
				//
				wchar_t wc[2];
				char mbc[6];
				wc[1] = NULL;
				for (int c = 0; c < wlen; ++c)
				{
					wc[0] = res[c];
					size_t cres = wcstombs(mbc, wc, 4);
					if (cres != static_cast<size_t>(1))
					{
						if (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1)
						{
							errno_t err1;
							_get_errno(&err1);
							wchar_t info[RES_BUF_SZ];
							swprintf_s(info, RES_BUF_SZ, L"mywcstombs, mbc conv error: s=%s res=%d err=%d", mbc, cres, err1);
							debug("%s\n", info);
						}
						//
						_res[c] = L'\'';
					}
					else
					{
						_res[c] = res[c];
					}
				}
				_res[wlen] = NULL;
				//
				if (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1)
				{
					debug("mywcstombs: _res=[%s]\n", _res);
				}
				res = _res;
			}
			size_t nBytes;
			err = wcsToUtf8(&nBytes, static_cast<char*>(nullptr), 0, res, 0);
			if (IS_DLL_IN_DEBUG_MODE == 0 && DEBUG_DLL || IS_DLL_IN_DEBUG_MODE == 1)
			{
				wchar_t info[RES_BUF_SZ];
				swprintf_s(info, RES_BUF_SZ, L"buf_sz=%d need_sz=%d err=%d", RES_BUF_SZ, nBytes + (prefix ? plen + 1 : 0), err);
				debug("mywcstombs: %s\n", info);
			}
			if (err == 0)
			{
				if (nBytes < csz - (prefix ? plen + 1 : 0))
				{
					//err = wcstombs_s(&i, b, csz, res, csz);
					err = wcsToUtf8(&i, b + (prefix ? plen : 0), csz - (prefix ? plen + 1 : 0), res, csz - (prefix ? plen + 1 : 0));
					if (err == 0)
					{
						len = i == 0 ? 0 : i - 1;
						if (proc != nullptr)
						{
							if (prefix) {
								memcpy(b, prefix, plen);
								b[len + plen] = suffix;
								b[len + plen + 1] = '\0';
								len += plen + 1;
							}
							proc(b, len, param);
						}
					}
					else
					{
						wchar_t info[RES_BUF_SZ];
						swprintf_s(info, RES_BUF_SZ, L"err=%d res=[%s]", err, res);
						debug("mywcstombs wcstombs_s error(1.1): %s\n", info);
						if (proc != nullptr)
						{
							if (prefix)
							{
								char errm[1000];
								sprintf_s(errm, 1000, "%swcstombs_s error 1.1 ", prefix);
								size_t _len = strlen(errm);
								errm[_len - 1] = suffix;
								proc(errm, _len, param);
							}
							else
							{
								char *errm = "wcstombs_s error 1.1";
								proc(errm, strlen(errm), param);
							}
						}
					}
				}
				else
				{
					b = buffer = static_cast<char*>(malloc(csz = nBytes + 1 + (prefix ? plen + 1 : 0)));
					err = wcsToUtf8(&i, b + (prefix ? plen : 0), csz - (prefix ? plen + 1 : 0), res, csz - (prefix ? plen + 1 : 0));
					//err = wcstombs_s(&i, buffer, nBytes, res, nBytes);
					if (err == 0)
					{
						len = i == 0 ? 0 : i - 1;
						if (proc != nullptr)
						{
							if (prefix) {
								memcpy(b, prefix, plen);
								b[len + plen] = suffix;
								b[len + plen + 1] = '\0';
								len += plen + 1;
							}
							proc(b, len, param);
						}
					}
					else
					{
						wchar_t info[RES_BUF_SZ];
						swprintf_s(info, RES_BUF_SZ, L"err=%d res=[%s]", err, res);
						debug("mywcstombs wcstombs_s error(1.2): %s\n", info);
						if (prefix)
						{
							char errm[1000];
							sprintf_s(errm, 1000, "%swcstombs_s error 1.2 ", prefix);
							size_t _len = strlen(errm);
							errm[_len - 1] = suffix;
							proc(errm, _len, param);
						}
						else
						{
							char *errm = "wcstombs_s error 1.2";
							proc(errm, strlen(errm), param);
						}
					}
				}
			}
			else
			{
				wchar_t info[RES_BUF_SZ];
				swprintf_s(info, RES_BUF_SZ, L"err=%d res=[%s]", err, res);
				debug("mywcstombs wcstombs_s error(2): %s\n", info);
				if (proc != nullptr)
				{
					if (prefix)
					{
						char errm[1000];
						sprintf_s(errm, 1000, "%swcstombs_s error 2 ", prefix);
						size_t _len = strlen(errm);
						errm[_len - 1] = suffix;
						proc(errm, _len, param);
					}
					else
					{
						char *errm = "wcstombs_s error 2";
						proc(errm, strlen(errm), param);
					}
				}
			}
		}
	}
	catch (...)
	{
		debug("Error in mywcstombs str=[%s]\n", res);
		len = 0;
	}
	if (_res != nullptr)
	{
		free(_res);
	}
	if (buffer != nullptr)
	{
		free(buffer);
	}
	return len;
}

void debug(const char* fmt, const wchar_t* p1)
{
	char b1[10000];
	char b2[10000];
	debug(fmt, tombs(p1, b1));
}

#define SOCKET_LOG "logs\\socket.log"
#define SOCKET_LOG_BAK "logs\\socket.-1.log"
//#define SOCKET_LOG "socket.log"
//#define SOCKET_LOG_BAK "socket.-1.log"
#define SOCKET_LOG_MAX_SZ 10 * 1024 * 1024

//mutex _debug_mutex;

void debug(const char* fmt, ...)
{
#if DEBUG_DLL
	auto start = Util::currentTimeMillis();
	__int64 p1 = 0, p2 = 0;
#endif
#ifndef USE_MT4_THREADS
	if (my_lock(&sDebugXP))
	{
		return;
	}
#else
	unique_lock<mutex> lock(sDebug); // does not work on XP and win2003 server
#endif
	try
	{
		debug_rec_no++;
		unsigned int TID = GetCurrentThreadId();
		//
		if (fDebug == nullptr)
		{
			//errno_t err = fopen_s(&fDebug, SOCKET_LOG, "a+");// not sharable
			fDebug = _fsopen(SOCKET_LOG, "a+", _SH_DENYWR);
			if (!fDebug)
			{
				fDebug == nullptr;
				return;
			}
		}
		//
		if (debug_rec_no % 100 == 0)
		{
			SetLastError(NO_ERROR);
			fseek(fDebug, 0L, SEEK_END);
			if (GetLastError() == NO_ERROR)
			{
				long fsz = ftell(fDebug);
				if (fsz > SOCKET_LOG_MAX_SZ)
				{
					fprintf(fDebug, "\n\n%s size=%d, resetting (max sz=%d)\n\n", SOCKET_LOG, fsz, SOCKET_LOG_MAX_SZ);
					fflush(fDebug);
					if (fclose(fDebug) == 0) fDebug = nullptr;
					//
					DeleteFileA(SOCKET_LOG_BAK);
					MoveFileA(SOCKET_LOG, SOCKET_LOG_BAK);
					//
					fDebug = _fsopen(SOCKET_LOG, "a+", _SH_DENYWR);
					if (!fDebug)
					{
						fDebug == nullptr;
						return;
					}
				}
			}
			else
			{
				fprintf(fDebug, "\n\nGetFileSize error=%d fsz=%ld\n\n", GetLastError(), ftell(fDebug));
			}
		}
#if DEBUG_DLL
		p1 = Util::currentTimeMillis();
#endif
		//
		if (strcmp(fmt, "."))
		{
			fprintf(fDebug, hdrPrinted ? "%06d %05d | %s> " : "\n\n\n%06d %05d | %s> ", PID, TID, Util::getCurrentTime().c_str());
		}
		//		fprintf(fDebug, fmt, p1, p2);
		//		fflush(fDebug);
		va_list ap;
		va_start(ap, fmt);
		vfprintf(fDebug, fmt, ap);
		va_end(ap);
		fflush(fDebug);
		if (fclose(fDebug) == 0) fDebug = nullptr;
#if DEBUG_DLL
		p2 = Util::currentTimeMillis();
#endif
		//
		hdrPrinted = true;
	}
	catch (...)
	{
	}
	//
	if (fDebug != nullptr && fclose(fDebug) == 0)
	{
		fDebug = nullptr;
	}
	//
#ifndef USE_MT4_THREADS
	my_unlock(&sDebugXP);
#endif
#if DEBUG_DLL
	auto finish = Util::currentTimeMillis();
	long long time = finish - start;
	if (time > 1)
	{
		long long t1 = p1 - start;
		long long t2 = p2 - p1;
		long long t3 = finish - p2;
		char info[1024];
		sprintf_s(info, 1000, "too long debug session: %lld (%lld + %lld + %lld)", time, t1, t2, t3);
		printStats(info, false); 
	}
#endif
}

#define STATS_LOG "logs\\stats.log"
#define STATS_LOG_BAK "logs\\stats.-1.log"
#define STATS_LOG_MAX_SZ 1024000

static FILE* volatile fStats = nullptr;

void printStats(const char* msg, bool init)
{
	try
	{
#ifndef USE_MT4_THREADS
		if (my_lock(&sStatsXP))
		{
			return;
		}
#else
		unique_lock<mutex> lock(sStats);
#endif
		//
		unsigned int TID = GetCurrentThreadId();
		//
		if (fStats == nullptr)
		{
			fStats = fopen(STATS_LOG, "a+");
			if (fStats == nullptr)
			{
				fStats = stderr;
			}
			else if (init)
			{
				SetLastError(NO_ERROR);
				fseek(fStats, 0L, SEEK_END);
				if (GetLastError() == NO_ERROR)
				{
					long fsz = ftell(fStats);
					if (fsz > STATS_LOG_MAX_SZ)
					{
						fprintf(fStats, "\n\n%s size=%d, resetting (max sz=%d)\n\n", STATS_LOG, fsz, STATS_LOG_MAX_SZ);
						fflush(fStats);
						fclose(fStats);
						fStats = nullptr;
						//
						DeleteFileA(STATS_LOG_BAK);
						MoveFileA(STATS_LOG, STATS_LOG_BAK);
						fStats = fopen(STATS_LOG, "a+");
						if (fStats == nullptr)
						{
							fStats = stderr;
						}
					}
				}
				else
				{
					fprintf(fStats, "\n\nGetFileSize error=%d fsz=%ld\n\n", GetLastError(), ftell(fStats));
				}
			}
		}
		//
		fprintf(fStats, init ? "\n\n\n%06d %05d | %s> %s\n" : "%06d %05d | %s> %s\n", PID, TID, Util::getCurrentTime().c_str(), msg);
		fflush(fStats);
		//
		if (fStats != stderr)
		{
			fclose(fStats);
		}
		fStats = nullptr;
	}
	catch (...)
	{
		if (fStats != nullptr && fStats != stderr)
		{
			fclose(fStats);
		}
		fStats = nullptr;
	}
	//
#ifndef USE_MT4_THREADS
	my_unlock(&sStatsXP); 
#endif
}

extern void dumpStatistics();

#ifndef BOXUTILS
#include "net/ty/MgmtHandlerThread.h"
#include "net/ty/sockets/SocketHandler.h"
#include "net/ty/sockets/StdoutLog.h"
#include "net/ty/EA.h"

static MgmtHandlerThread* handler_thread = nullptr;
static MTResultThread* mtres_thread = nullptr;
static StdoutLog* socket_handler_log = nullptr;
static SocketHandler* socket_handler = nullptr;

#endif // !BOXUTILS


static HMODULE phModule;

//
// GetModuleFullName:
//
//    Gets the full path and file name of the specified module and returns the length on success,
//    (which does not include the terminating NUL character) 0 otherwise.  Use GetLastError() to
//    get extended error information.
//
//       hModule              [in] Handle to a module loaded by the calling process, or NULL to
//                            use the current process module handle.  This function does not 
//                            retrieve the name for modules that were loaded using LoadLibraryEx
//                            with the LOAD_LIBRARY_AS_DATAFILE flag. For more information, see 
//                            LoadLibraryEx.
//
//       pszBuffer            [out] Pointer to the buffer which receives the module full name.
//                            This paramater may be NULL, in which case the function returns the
//                            size of the buffer in characters required to contain the full name,
//                            including a NUL terminating character.
//
//       nMaxChars            [in] Specifies the size of the buffer in characters.  This must be
//                            0 when pszBuffer is NULL, otherwise the function fails.
//
//       ppszFileName         [out] On return, the referenced pointer is assigned a position in
//                            the buffer to the module's file name only.  This parameter may be
//                            NULL if the file name is not required.
//       
EXTERN_C int WINAPI GetModuleFullName(
	__in  HMODULE hModule,
	__out LPWSTR pszBuffer,
	      __in  int nMaxChars)
{
	DWORD dwStatus = NO_ERROR;

	//
	// Validate parameters:
	//
	if (dwStatus == NO_ERROR)
	{
		if (pszBuffer == nullptr && (nMaxChars != 0)) {
			debug("GetModuleFullName: ERROR_INVALID_PARAMETER: pszBuffer is null");
			dwStatus = ERROR_INVALID_PARAMETER ;
		}

		else if (pszBuffer != nullptr && nMaxChars < 1) {
			debug("GetModuleFullName: ERROR_INVALID_PARAMETER: nMaxChars < 1");
			dwStatus = ERROR_INVALID_PARAMETER;
		}
	}

	//
	// Determine required buffer size when requested:
	//
	int nLength = 0;

	if (dwStatus == NO_ERROR)
	{
		if (pszBuffer == nullptr)
		{
			HANDLE hHeap = ::GetProcessHeap();

			WCHAR cwBuffer[2048] = { 0 };
			LPWSTR psz_buffer = cwBuffer;
			DWORD dwMaxChars = _countof(cwBuffer);
			DWORD dwLength = 0;

			for (;;)
			{
				//
				// Try to get the module's full path and file name:
				//
				dwLength = ::GetModuleFileNameW(hModule, psz_buffer, dwMaxChars);
				if (dwLength == 0)
				{
					dwStatus = ::GetLastError();
					break;
				}

				//
				// If succeeded, return buffer size requirement:
				//    o  Adds one for the terminating NUL character.
				//
				if (dwLength < dwMaxChars)
				{
					nLength = static_cast<int>(dwLength) + 1;
					break;
				}

				//
				// Check the maximum supported full name length:
				//    o  Assumes support for HPFS, NTFS, or VTFS of ~32K.
				//
				if (dwMaxChars >= 32768U)
				{
					dwStatus = ERROR_BUFFER_OVERFLOW;
					break;
				}

				//
				// Double the size of our buffer and try again:
				//
				dwMaxChars *= 2;

				SIZE_T nSize = static_cast<SIZE_T>(dwMaxChars) * sizeof(WCHAR);
				LPWSTR pszNew = (psz_buffer == cwBuffer ? NULL : psz_buffer);
				if (pszNew == nullptr)
					pszNew = static_cast<LPWSTR>(::HeapAlloc(hHeap, 0, nSize));
				else
					pszNew = static_cast<LPWSTR>(::HeapReAlloc(hHeap, 0, pszNew, nSize));
				if (pszNew == nullptr)
				{
					dwStatus = ERROR_OUTOFMEMORY;
					break;
				}

				psz_buffer = pszNew;
			}

			//
			// Free the temporary buffer if allocated:
			//
			if (psz_buffer != cwBuffer)
			{
				if (!::HeapFree(hHeap, 0, psz_buffer))
					dwStatus = ::GetLastError();
			}
		}
	}

	//
	// Get the module's full name and pointer to file name when requested:
	//
	if (dwStatus == NO_ERROR)
	{
		if (pszBuffer != nullptr)
		{
			nLength = static_cast<int>(::GetModuleFileNameW(hModule, pszBuffer, nMaxChars));
			if (nLength <= 0 || nLength == nMaxChars) {
				dwStatus = ::GetLastError();
				debug("GetModuleFullName: GetModuleFileNameW error: %d, nLength=%d, nMaxChars=%d", dwStatus, nLength, nMaxChars);
			}
		}
	}

	//
	// Return full name length or 0 on error:
	//
	if (dwStatus != NO_ERROR)
	{
		nLength = 0;

		::SetLastError(dwStatus);
	}

	return nLength;
}

void pinModule(HMODULE hModule)
{
	BOOL w = false;
#ifdef SMART_SOCKET
	HANDLE hProcess;
	hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, GetCurrentProcessId());
	if (hProcess)
	{
		TCHAR lpBaseName[1000];
		DWORD cnt = GetModuleBaseName(hProcess, hModule, lpBaseName, 1000);
		if (cnt > 0)
		{
			lpBaseName[cnt] = '\0';
		}
		else
		{
			debug("Can not copy module base name, err=%d\n", GetLastError());
		}
		CloseHandle(hProcess);
	}
	else
	{
		debug("Can not open process, err=%d\n", GetLastError());
	}
#endif
	if (!w)
	{
		w = GetModuleHandleExW(GET_MODULE_HANDLE_EX_FLAG_PIN, L"mt45if.dll", &phModule);
	}
}

void CALLBACK Test(HWND hwnd, HINSTANCE hinst, LPSTR lpszCmdLine, int nCmdShow)
{
//	lock_guard<mutex> lock(_debug_mutex);
	debug("Test: pid=%d cmd=%s", GetCurrentProcessId(), lpszCmdLine);
}

BOOL APIENTRY DllMain(HMODULE hModule,
                      DWORD ul_reason_for_call,
                      LPVOID lpReserved
)
{
	//wchar_t mtxInfo[100];
	switch (ul_reason_for_call)
	{
	case DLL_PROCESS_ATTACH: {
#ifndef USE_MT4_THREADS
		if (my_lock(&sDllInitXP))
		{
			break;
		}
#else
		unique_lock<mutex> lock(sDllInit);
#endif
		if (hTrialQuery == nullptr)
		{
			debug("\n");
			//
#if DEBUG_DLL
#else
			SetErrorMode(SEM_FAILCRITICALERRORS | SEM_NOGPFAULTERRORBOX);
#endif
			//
			PID = GetCurrentProcessId();
			pinModule(hModule);
			//
#ifndef BOXUTILS
			//
			if (handler_thread == nullptr)
			{
				//
				mtres_thread = new MTResultThread();
				mtres_thread->Start();
				//				Mutex* mutex = new Mutex();
				socket_handler = new SocketHandler(/**mutex, */socket_handler_log = new StdoutLog());
				handler_thread = new MgmtHandlerThread(*socket_handler);
				handler_thread->SetDeleteOnExit();
				handler_thread->Start();
			}
#endif // !BOXUTILS
#ifdef USE_JVM
			jvm_options[0] = 0;
#endif
			hTrialQuery = CreateMutex(nullptr, FALSE, _T("Global\\nj4x_trial_req"));
			//swprintf_s(mtxInfo, L"nj4x-debug-%d", PID);
			//		hDebug = CreateMutex(NULL, FALSE, NULL);
			//swprintf_s(mtxInfo, L"nj4x-conns-%d", PID);
			//		hConnections = CreateMutex(NULL, FALSE, NULL);
			//swprintf_s(mtxInfo, L"nj4x-jvm-%d", PID);
			//		hJvm = CreateMutex(NULL, FALSE, NULL);
			//swprintf_s(mtxInfo, L"nj4x-threads-%d", PID);
			hThreads = CreateMutex(nullptr, FALSE, nullptr);
			//
		}
		//
#ifndef USE_MT4_THREADS
		my_unlock(&sDllInitXP);
#endif
	}
		break;
	case DLL_PROCESS_DETACH: {
#ifndef USE_MT4_THREADS
		if (my_lock(&sDllInitXP))
		{
			break;
		}
#else
		unique_lock<mutex> lock(sDllInit);
#endif
		if (hTrialQuery != nullptr)
		{
			//commentme debug("\nDLL_PROCESS_DETACH\n");
			CloseHandle(hTrialQuery);
			CloseHandle(hThreads);
			hTrialQuery = nullptr;
		}
#ifndef BOXUTILS
		if (handler_thread)
		{
			handler_thread->Stop();
			handler_thread = nullptr;
			delete socket_handler;
			delete socket_handler_log;
		}
#endif // !BOXUTILS

#ifndef USE_MT4_THREADS
		my_unlock(&sDllInitXP);
#endif
	}
		//		CloseHandle(hDebug);
		//		CloseHandle(hConnections);
		//		CloseHandle(hJvm);
		//		CloseHandle(hThreads);
		break;
	case DLL_THREAD_ATTACH:
		//debug("\nDLL_THREAD_ATTACH\n");
		break;
	case DLL_THREAD_DETACH:
		//debug("\nDLL_THREAD_DETACH\n");
		break;
	}
	return TRUE;
}
