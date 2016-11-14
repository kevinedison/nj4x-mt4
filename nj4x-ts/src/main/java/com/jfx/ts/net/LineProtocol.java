package com.jfx.ts.net;

import com.jfx.net.Config;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

class LineProtocol extends ByteToMessageCodec<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String s, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(s.getBytes(Config.CHARSET));
        byteBuf.writeByte('\n');
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        int fromIndex = in.readerIndex();
        int toIndex = in.writerIndex();
        int ix = in.indexOf(fromIndex, toIndex, (byte) '\n');
        ix -= fromIndex;
        if (ix > 0) {
            byte[] bytes = new byte[ix];
            in.readBytes(bytes);
            in.readByte();
            list.add(new String(bytes));
        } else if (ix == 0) {
            list.add("");
            in.readByte();
        }
    }
}
