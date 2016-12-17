#pragma once
/*
* Copyright (c) 2000-2006 Joachim Henke
*
* For conditions of distribution and use, see copyright notice in base91.c
* 
*/
//basE91 is an advanced method for encoding binary data as ASCII characters. It is similar to UUencode or base64, but is more efficient. The overhead produced by basE91 depends on the input data. It amounts at most to 23% (versus 33% for base64) and can range down to 14%, which typically occurs on 0-byte blocks. This makes basE91 very useful for transferring larger files over binary unsafe connections like e-mail or terminal lines.
//http://base91.sourceforge.net/

//这是一个压缩算法
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
