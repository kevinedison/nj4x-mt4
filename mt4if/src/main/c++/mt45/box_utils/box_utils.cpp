// box_utils.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "box_utils.h"
#include "box_id.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif
#include <windef.h>
#include <winbase.h>

long d;

JNIEXPORT jlong JNICALL Java_com_jfx_ts_io_BoxUtils_boxid(JNIEnv *env, jclass c) {
	return boxid();
}

JNIEXPORT jlong JNICALL Java_com_jfx_ts_io_BoxUtils_getBinaryType(JNIEnv *env, jclass c, jstring _path) 
{
	const jchar *path = env->GetStringChars(_path, nullptr);
//	size_t iLenP = wcslen(LPWSTR(path));
	//
	DWORD binaryType;
	if (!GetBinaryTypeW(LPWSTR(path), static_cast<LPDWORD>(&binaryType))) {
		DWORD err = GetLastError();
		if (err == ERROR_BAD_EXE_FORMAT) {
			binaryType = -1;
		} else {
			binaryType = -2;
		}
	}
	//
	env->ReleaseStringChars(_path, path);
	//
	return binaryType;
}
