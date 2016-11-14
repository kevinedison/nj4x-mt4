// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently

#pragma once

#include "box_id.h"
#include "targetver.h"

extern void debug(const char* fmt, const wchar_t* p1);
extern void debug(const char* fmt, ...);

#define BOXUTILS 1
