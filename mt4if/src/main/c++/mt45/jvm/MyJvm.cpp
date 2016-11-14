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
#include <mutex>
#ifdef USE_JVM

#include "stdafx.h"
#include "MyJvm.h"
#include <windows.h>
#include <tchar.h> 
#include <stdio.h>
#pragma comment(lib, "User32.lib")

char jvm_options[32767];
char classpath[32767];
JavaVM* vm = nullptr;
JavaVMInitArgs vm_args;
JavaVMOption options[100];
jclass cInprocessServer, cInprocessSocket;
jmethodID mConnect, jmShutdown, jmTest;
jmethodID mClose, mWrite, mRead;
jmethodID mWrite2, mRead2, mRead3;

static jint JNICALL my_vfprintf(FILE* fp, const char* format, va_list args)
{
	char buf[10240];
	vsnprintf(buf, sizeof(buf), format, args);
	//char tmp[10000];
	//sprintf_s(tmp, 10000, "th=%ul, msg=[%s]", GetCurrentThread(), buf);
	debug("[JVM] %s\n", buf);
	//
	return 0;
}


typedef jint (JNICALL *CreateJavaVM)(JavaVM** pvm, void** penv, void* args);
CreateJavaVM createJVM;

typedef jint (JNICALL *GetDefaultJavaVMInitArgs)(void* args);
GetDefaultJavaVMInitArgs getDefaultJavaVMInitArgs;


static void create_vm() throw(int)
{
#ifndef USE_MT4_THREADS
	if (my_lock(&sJvmXP) /*WaitForSingleObject(hJvm, 60000) == WAIT_TIMEOUT*/)
	{
		debug("create_vm: can not lock Jvm mutex.\n");
		throw -1;
	}
#else
	unique_lock<mutex> lock(sJvm);
#endif

	if (vm != nullptr)
	{
#ifndef USE_MT4_THREADS
		my_unlock(&sJvmXP) /*ReleaseMutex(hJvm)*/;
#endif
		return;
	}

	HINSTANCE handle = LoadLibrary(_T("MQL4\\Libraries\\mt45if.dll"));
	if (handle == nullptr)
	{
		debug("create_vm: can not pin mt45if.dll.\n");
		throw -1;
	}

	//	char path[MAX_PATH];
	//	int path_len = strcpy_s(path, MAX_PATH, getenv("PATH"));
	//	path[path_len] = '\0';

	//char* path = "\"C:\\Program Files (x86)\\Java\\jre7\\bin\";\"C:\\Program Files (x86)\\Java\\jre7\\bin\\client\";MQL4\\Libraries";

	int p = 0;
	char* cp = getenv("CLASSPATH");
	if (cp == nullptr)
	{
		cp = ".\\MQL4\\Experts;.\\MQL4\\Libraries";
		debug("create_vm: NO CLASSPATH environment variable detected, using %s\n", cp);
	}
	strcpy_s(classpath, sizeof(classpath), "-Djava.class.path=");
	strcpy_s(classpath + strlen("-Djava.class.path="), sizeof(classpath) - strlen("-Djava.class.path="), cp);
	classpath[strlen("-Djava.class.path=") + strlen(cp)] = '\0';

	HANDLE hFile;
	WIN32_FIND_DATAW FindFileData;
	hFile = FindFirstFileW(L".\\MQL4\\Libraries\\*.jar", &FindFileData);

	if (INVALID_HANDLE_VALUE != hFile)
	{
		do
		{
			//Skip directories
			if (FILE_ATTRIBUTE_DIRECTORY & FindFileData.dwFileAttributes)
				continue;

			//debug("%s> create_vm: %s\n", Util::getCurrentTime().c_str(), "JAR for CLASSPATH found");
			int cpsz = strlen(classpath);
			strcpy_s(classpath + cpsz, sizeof(classpath) - cpsz, ";MQL4\\Libraries\\");
			cpsz += strlen(";MQL4\\Libraries\\");
			int bytes = wcstombs(
				classpath + cpsz,
				FindFileData.cFileName,
				sizeof(classpath) - cpsz - 1
			);
			classpath[cpsz + bytes] = '\0';

			//debug("%s> create_vm: CP=[%s]\n", Util::getCurrentTime().c_str(), classpath);
		}
		while (FindNextFileW(hFile, &FindFileData));
		//
		FindClose(hFile);
	}

	debug("create_vm: CLASSPATH=[%s]\n", classpath);

	options[p++].optionString = classpath;
	options[p].optionString = "vfprintf";
	options[p++].extraInfo = my_vfprintf;
	//options[p++].optionString = "-verbose:jni";	
	char* jo = getenv("JFX_JVM_OPTIONS");
	if (jo == nullptr)
	{
		debug("create_vm: NO JFX_JVM_OPTIONS environment variable detected\n");
	}
	else
	{
		strcpy_s(jvm_options, sizeof(jvm_options), jo);
		debug("create_vm: JFX_JVM_OPTIONS=[%s]\n", jvm_options);
		int start = 0;
		while (jvm_options[start] != 0 && (jvm_options[start] == ' ' || jvm_options[start] == '\t')) start++;
		string o(jvm_options + start);
		options[p++].optionString = jvm_options + start;
		int ix1 = o.find(" -D");
		int ix2 = o.find(" -X");
		int ix = ix1 < 0 ? ix2 : (ix2 < 0 ? ix1 : (ix1 > ix2 ? ix2 : ix1));
		while (ix > 0 && p < 30)
		{
			jvm_options[start + ix] = 0;
			debug("JFX_JVM_OPTION [%s]\n", options[p - 1].optionString);
			options[p++].optionString = jvm_options + (start + ix + 1);
			//
			ix1 = o.find(" -D", ix + 1);
			ix2 = o.find(" -X", ix + 1);
			ix = ix1 < 0 ? ix2 : (ix2 < 0 ? ix1 : (ix1 > ix2 ? ix2 : ix1));
		}
		debug("JFX_JVM_OPTION [%s]\n", options[p - 1].optionString);
	}

#ifndef STATIC_JVM
	char jre_home[MAX_PATH];
	char* jh = getenv("JFX_JRE_HOME");
	if (jh == nullptr)
	{
		debug("create_vm: NO JFX_JRE_HOME environment variable detected\n");
		strcpy_s(jre_home, sizeof(jre_home), "C:\\jdk1.7\\jre\\bin\\client\\jvm.dll");
	}
	else
	{
		strcpy_s(jre_home, sizeof(jre_home), jh);
		int jh_len = strlen(jre_home);
		if (jre_home[0] == '"' && jre_home[jh_len - 1] == '"')
		{
			memcpy(jre_home, jre_home + 1, jh_len - 2);
			jre_home[jh_len - 2] = '\0';
			jh_len = strlen(jre_home);
		}
		debug("create_vm: JFX_JRE_HOME=[%s]\n", jre_home);
		char* dll = "\\bin\\client\\jvm.dll";
		strcpy_s(jre_home + jh_len, sizeof(jre_home) - jh_len, dll);
		jre_home[jh_len + strlen(dll)] = '\0';
		debug("create_vm: DLL=[%s]\n", jre_home);
	}

	WCHAR dll_path[MAX_PATH];
	int chars = mbstowcs(
		dll_path,
		jre_home,
		MAX_PATH
	);
	dll_path[chars] = 0;
	//
	handle = LoadLibraryW(dll_path);
	//handle = LoadLibraryEx(_T("jvm.dll"), 0, DONT_RESOLVE_DLL_REFERENCES);
	if (handle == nullptr)
	{
		int err = GetLastError();
		char dll_dir[MAX_PATH];
		int len = GetDllDirectoryA(MAX_PATH - 1, dll_dir);
		if (len < MAX_PATH) dll_dir[len] = '\0';
		char info2[1000];
		sprintf_s(info2, 1000, "Error loading 32bit jvm.dll err=%d (not in the %s).\n", err, dll_dir);
		debug("jvm.dll not loaded\n");
		debug("%s\n", info2);
#ifndef USE_MT4_THREADS
		my_unlock(&sJvmXP) /*ReleaseMutex(hJvm)*/;
#endif
		throw -2;
	}
	debug("jvm.dll loaded\n");
	//get the function pointer to JNI_CreateJVM
	createJVM = reinterpret_cast<CreateJavaVM>(GetProcAddress(handle, "JNI_CreateJavaVM"));
	getDefaultJavaVMInitArgs = reinterpret_cast<GetDefaultJavaVMInitArgs>(GetProcAddress(handle, "JNI_GetDefaultJavaVMInitArgs"));
#endif

#ifdef STATIC_JVM
	JNI_GetDefaultJavaVMInitArgs(&vm_args);
#else
	getDefaultJavaVMInitArgs(&vm_args);
#endif

	vm_args.version = JNI_VERSION_1_4;
	vm_args.options = options;
	vm_args.nOptions = p;
	vm_args.ignoreUnrecognized = JNI_FALSE;
	//
	vm = nullptr;
	JNIEnv* env;

	debug("Creating JVM\n");
#ifdef STATIC_JVM
	jint res = JNI_CreateJavaVM(&(vm), (void **)&(env), &(vm_args));
#else
	jint res = createJVM(&vm, reinterpret_cast<void **>(&env), &vm_args);
#endif

	if (res < 0)
	{
		debug("Error creating JVM.\n");
#ifndef USE_MT4_THREADS		
		my_unlock(&sJvmXP) /*ReleaseMutex(hJvm)*/;
#endif
		throw -2;
	}

	//locate the class
	cInprocessServer = env->FindClass("com/jfx/net/InprocessServer");
	if (cInprocessServer == nullptr)
	{
		debug("java.lang.NoClassDefFoundError: com/jfx/net/InprocessServer\n");
		throw -1;
	}
	cInprocessServer = static_cast<jclass>(env->NewGlobalRef(cInprocessServer));
	//
	cInprocessSocket = env->FindClass("com/jfx/net/InprocessSocket");
	if (cInprocessSocket == nullptr)
	{
		debug("java.lang.NoClassDefFoundError: com/jfx/net/InprocessSocket\n");
		throw -1;
	}
	cInprocessServer = static_cast<jclass>(env->NewGlobalRef(cInprocessServer));
	//
	mConnect = env->GetStaticMethodID(cInprocessServer, "connect", "(Ljava/lang/String;)Lcom/jfx/net/InprocessSocket;");
	if (mConnect == nullptr)
	{
		debug("java.lang.NoSuchMethodError: connect\n");
		throw -2;
	}
	//
	mClose = env->GetMethodID(cInprocessSocket, "close", "()V");
	if (mClose == nullptr)
	{
		debug("java.lang.NoSuchMethodError: close\n");
		throw -3;
	}
	mWrite = env->GetMethodID(cInprocessSocket, "write", "(Ljava/lang/String;)V");
	if (mWrite == nullptr)
	{
		debug("java.lang.NoSuchMethodError: write\n");
		throw -4;
	}
	mWrite2 = env->GetMethodID(cInprocessSocket, "write2", "([CI)V");
	if (mWrite2 == nullptr)
	{
		debug("java.lang.NoSuchMethodError: write2\n");
		throw -5;
	}
	mRead = env->GetMethodID(cInprocessSocket, "read", "()Ljava/lang/String;");
	if (mRead == nullptr)
	{
		debug("java.lang.NoSuchMethodError: read\n");
		throw -6;
	}
	mRead2 = env->GetMethodID(cInprocessSocket, "read2", "([CI)I");
	if (mRead2 == nullptr)
	{
		debug("java.lang.NoSuchMethodError: read2\n");
		throw -6;
	}
	mRead3 = env->GetMethodID(cInprocessSocket, "read3", "([BI)I");
	if (mRead3 == nullptr)
	{
		debug("java.lang.NoSuchMethodError: read3\n");
		throw -6;
	}
	//
	debug("JVM created.\n");
	//
#ifndef USE_MT4_THREADS
	my_unlock(&sJvmXP) /*ReleaseMutex(hJvm)*/;
#endif
	//
}

void destroy_vm()
{
#ifndef USE_MT4_THREADS
	if (my_lock(&sJvmXP) /* WaitForSingleObject(hJvm, 10000) == WAIT_TIMEOUT */)
	{
		debug("destroy_vm: can not lock Jvm mutex.\n");
		throw -1;
	}
#else
	unique_lock<mutex> lock(sJvm);
#endif;

	if (vm != nullptr)
	{
		debug("Calling vm->DestroyJavaVM()...\n");
		vm->DestroyJavaVM();
		vm = nullptr;
		debug("JVM destroyed.\n");
	}

#ifndef USE_MT4_THREADS
	my_unlock(&sJvmXP) /*ReleaseMutex(hJvm)*/;
#endif
}

MyJvm::MyJvm() throw(int)
{
	create_vm();
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	w = static_cast<jcharArray>(env->NewGlobalRef(env->NewCharArray(JCHAR_RWA_SZ)));
	r = static_cast<jcharArray>(env->NewGlobalRef(env->NewCharArray(JCHAR_RWA_SZ)));
	byte_r = static_cast<jbyteArray>(env->NewGlobalRef(env->NewByteArray(sizeof(ra))));
	bufPos = bufLen = 0;

	socket = nullptr;
	ra[0] = NULL;
	wa[0] = NULL;
}

MyJvm::~MyJvm()
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	env->DeleteGlobalRef(socket);
	env->DeleteGlobalRef(w);
	env->DeleteGlobalRef(r);
	env->DeleteGlobalRef(byte_r);
	//
	debug("in ~MyJvm()\n");
}

void MyJvm::connect(const char* s)
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	debug("in MyJvm::connect().\n");
	//
	//invoke the main method with no parameters
	jstring js = env->NewStringUTF(s);
	this->socket = env->CallStaticObjectMethod(cInprocessServer, mConnect, js);
	//
	if (env->ExceptionCheck())
	{
		env->DeleteLocalRef(js);
		env->ExceptionDescribe();
		throw -1;
	}
	env->DeleteLocalRef(js);
	this->socket = env->NewGlobalRef(this->socket);

	char tmp[1000];
	sprintf_s(tmp, 1000, "socket=%ul", this->socket);
	debug("out MyJvm::connect() -> %s.\n", tmp);
	//vm->DetachCurrentThread();
}

void MyJvm::send(const char* s)
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	DEB(debug("%s> in MyJvm::send(%s).\n", Util::getCurrentTime().c_str(), s))
	//
	//if (env->MonitorEnter(socket) != JNI_OK) {
	//	debug("%s> MyJvm::send/MonitorEnter error (%s).\n", Util::getCurrentTime().c_str(), s);
	//	throw -1;
	//}
	env->ExceptionClear();
	jstring js = env->NewStringUTF(s);
	if (env->ExceptionCheck())
	{
		debug("error 1 MyJvm::send(%s).\n", s);
		env->DeleteLocalRef(js);
		env->ExceptionDescribe();
		throw - 2;
	}
	env->CallVoidMethod(socket, mWrite, js);
	if (env->ExceptionCheck())
	{
		debug("error 2 MyJvm::send(%s).\n", s);
		env->DeleteLocalRef(js);
		env->ExceptionDescribe();
		//env->MonitorExit(socket);
		//		vm->DetachCurrentThread();
		throw -2;
	}
	env->DeleteLocalRef(js);
	//if (env->MonitorExit(socket) != JNI_OK) {
	//	debug("%s> MyJvm::send/MonitorExit error (%s).\n", Util::getCurrentTime().c_str(), s);
	//}
	//
	//debug("%s> out MyJvm::send(%s).\n", Util::getCurrentTime().c_str(), s);
	//	vm->DetachCurrentThread();
}

void MyJvm::send2(const char* s, size_t len)
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	//	debug("%s> in MyJvm::send(%s).\n", Util::getCurrentTime().c_str(), s);
	//
	//if (env->MonitorEnter(socket) != JNI_OK) {
	//	debug("%s> MyJvm::send/MonitorEnter error (%s).\n", Util::getCurrentTime().c_str(), s);
	//	throw -1;
	//}
	//size_t len = strlen(s);

	jchar* buffer = wa;
	jcharArray jca = w;

	try
	{
		if (len > JCHAR_RWA_SZ)
		{
			buffer = static_cast<jchar*>(malloc(len * sizeof(jchar)));
			jca = static_cast<jcharArray>(env->NewGlobalRef(env->NewCharArray(len)));
		}
	}
	catch (...)
	{
		debug("Error allocating jchar[]\n");
		len = JCHAR_RWA_SZ;
	}

	for (int i = 0; i < len; ++i) buffer[i] = s[i];
	env->SetCharArrayRegion(jca, 0, len, buffer);
	env->CallVoidMethod(socket, mWrite2, jca, len);

	if (buffer != nullptr && buffer != wa)
	{
		free(buffer);
		env->DeleteGlobalRef(jca);
	}

	if (env->ExceptionCheck())
	{
		debug("Error MyJvm::send2(%s).\n", s);
		env->ExceptionDescribe();
		//env->MonitorExit(socket);
		throw -2;
	}
	//if (env->MonitorExit(socket) != JNI_OK) {
	//	debug("%s> MyJvm::send/MonitorExit error (%s).\n", Util::getCurrentTime().c_str(), s);
	//}
	//
	//	debug("%s> out MyJvm::send(%s).\n", Util::getCurrentTime().c_str(), s);
	//	vm->DetachCurrentThread();
}

int MyJvm::recv(char* buffer, int bufSize)
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	//debug("%s> in MyJvm::recv.\n", Util::getCurrentTime().c_str(), "");
	//
	env->ExceptionClear();
	jstring s = static_cast<jstring>(env->CallObjectMethod(socket, mRead));
	if (env->ExceptionCheck())
	{
		debug("error 1 MyJvm::recv\n");
		env->ExceptionDescribe();
		//		vm->DetachCurrentThread();
		env->DeleteLocalRef(s);
		throw -1;
	}
	//
	int len = env->GetStringUTFLength(s);
	const char* jutf8 = env->GetStringUTFChars(s, nullptr);
	if (env->ExceptionCheck())
	{
		debug("error 2 MyJvm::recv\n");
		env->ExceptionDescribe();
		env->DeleteLocalRef(s);
		throw - 1;
	}
	int sz = bufSize - 1 < len ? bufSize - 1 : len;
	memcpy_s(buffer, sz, jutf8, sz);
	env->ReleaseStringUTFChars(s, jutf8);
	if (env->ExceptionCheck())
	{
		debug("error 3 MyJvm::recv\n");
		env->ExceptionDescribe();
		env->DeleteLocalRef(s);
		throw - 1;
	}
	//env->GetStringUTFRegion(s, 0, sz, buffer);
	if (sz > 1 && buffer[0] == '\x01' && buffer[sz - 1] == '\x02')
	{
		for (int i = 0; i < sz - 1; ++i) buffer[i] = buffer[i + 1];
		buffer[sz - 2] = buffer[sz - 1] = buffer[sz] = '\0';
	} else
	{
		buffer[sz] = '\0';
	}
	//
	env->DeleteLocalRef(s);
	//
	//char tmp[1000];
	//sprintf_s(tmp, 1000, "len=%d, bufSize=%d, sz=%d, s=(%s)", len, bufSize, sz, buffer);
	//debug("%s> out MyJvm::recv (%s)\n", Util::getCurrentTime().c_str(), tmp);
	//
	//	vm->DetachCurrentThread();
	//

	return sz;
}

int MyJvm::recv2(char* buffer, int bufSize)
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	//debug("%s> in MyJvm::recv2.\n", Util::getCurrentTime().c_str(), "");
	//
	jint sz = reinterpret_cast<jint>(env->CallObjectMethod(socket, mRead2, r, bufSize - 1));
	if (env->ExceptionCheck())
	{
		debug("error MyJvm::recv2\n");
		env->ExceptionDescribe();
		throw -1;
	}
	//
	env->GetCharArrayRegion(r, 0, sz, ra);
	int copied = 0;
	//	int argStarts = 0;
	//	char firstChar = 0;
	for (int i = 0; i < sz; ++i)
	{
		char c = ra[i];
		if (i > 0 && i < sz - 1)
			buffer[copied++] = c;
	}
	buffer[copied] = '\0';
	//
	//char tmp[1000];
	//sprintf_s(tmp, 1000, "len=%d, bufSize=%d, sz=%d, s=(%s)", copied, bufSize, sz, buffer);
	//debug("%s> out MyJvm::recv2 (%s)\n", Util::getCurrentTime().c_str(), tmp);
	//
	return sz;
}

int MyJvm::recv3(char* buffer, int nBytes)
{
	int num_bytes_received = 0;
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);

	//debug("%s> in MyJvm::recv3.\n", Util::getCurrentTime().c_str(), "");
	while (true)
	{
		int savedBufPos = bufPos;

		if (bufPos < bufLen && num_bytes_received < nBytes)
		{
			int sz = bufLen - bufPos >= nBytes ? nBytes : bufLen - bufPos;
			env->GetByteArrayRegion(byte_r, bufPos, sz, reinterpret_cast<jbyte*>(buffer));
			if (env->ExceptionCheck())
			{
				debug("error MyJvm::recv3\n");
				env->ExceptionDescribe();
				throw -1;
			}
			num_bytes_received += sz;
			bufPos += sz;
			if (num_bytes_received == nBytes)
			{
				// line is complete
				buffer[num_bytes_received] = '\0';
				//
				//char tmp[1000];
				//sprintf_s(tmp, 1000, "nBytes=%d, read=%d, s=(%s)", nBytes, sz, buffer);
				//debug("%s> out MyJvm::recv3 (%s)\n", Util::getCurrentTime().c_str(), tmp);
				//
				return num_bytes_received;
			}
		}

		if (num_bytes_received >= nBytes)
		{
			bufPos = savedBufPos;
			break;
		}
		// -----------------------------------------------------------------------
		bufLen = reinterpret_cast<int>(env->CallObjectMethod(socket, mRead3, byte_r, static_cast<int>(BUF_SZ)));
		bufPos = 0;
		if (env->ExceptionCheck())
		{
			debug("error MyJvm::recv3\n");
			env->ExceptionDescribe();
			throw -1;
		}
		//
		//char tmp[1000];
		//sprintf_s(tmp, 1000, "needed=%d, got=%d bytes", nBytes, bufLen);
		//debug("%s> MyJvm::recv3 (%s)\n", Util::getCurrentTime().c_str(), tmp);
		// -----------------------------------------------------------------------
		if (bufLen <= 0)
		{
			bufLen = bufPos = 0;
			throw 1;
		}
	}

	debug("buffer overflow.\n");
	//fflush(debug);
	throw 2;
}

/*int recv3(char* buffer, int bufSize)
{
	JNIEnv *env;
	vm->AttachCurrentThreadAsDaemon((void **)&(env), NULL);
	//
debug("%s> in MyJvm::recv3.\n", Util::getCurrentTime().c_str(), "");
	//
	jint sz = (jint) env->CallObjectMethod(socket, mRead3, byte_r, bufSize);
	if(env->ExceptionCheck()) {
		debug("%s> error MyJvm::recv3\n", Util::getCurrentTime().c_str(), "");
		env->ExceptionDescribe();
		throw -1;
	}
	//
	env->GetByteArrayRegion(byte_r, 0, sz, (jbyte*) buffer);
	//
	char tmp[1000];
    sprintf_s(tmp, 1000, "bufSize=%d, sz=%d, s=(%s)", bufSize, sz, buffer);
	debug("%s> out MyJvm::recv3 (%s)\n", Util::getCurrentTime().c_str(), tmp);
	//
	return sz;
}*/

void MyJvm::close()
{
	JNIEnv* env;
	vm->AttachCurrentThreadAsDaemon(reinterpret_cast<void **>(&(env)), nullptr);
	//
	debug("in MyJvm::close()\n");
	//
	env->CallVoidMethod(socket, mClose);
	if (env->ExceptionCheck())
	{
		debug("error MyJvm::close()\n");
		env->ExceptionDescribe();
		//vm->DetachCurrentThread();
		throw -1;
	}
	//
	debug("out MyJvm::close()\n");
	//vm->DetachCurrentThread();
}

#endif /*USE_JVM*/
