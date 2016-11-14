#pragma once
/*
* Copyright (c) 2000-2006 Joachim Henke
*
* For conditions of distribution and use, see copyright notice in base91.c
*/

#ifndef BASE91_H
#define BASE91_H 1

struct BasE91 {
	BasE91() : queue(0), nbits(0), val(-1) {}
	unsigned long queue;
	unsigned int nbits;
	long val;
	//
	size_t encode(const char* ib, size_t len, char* ob);
	size_t decode(const char* ib, size_t len, char* ob);
};

size_t basE91_encode(struct BasE91 *, const void *, size_t, void *);

size_t basE91_encode_end(struct BasE91 *, void *);

size_t basE91_decode(struct BasE91 *, const void *, size_t, void *);

size_t basE91_decode_end(struct BasE91 *, void *);

#endif	/* base91.h */
