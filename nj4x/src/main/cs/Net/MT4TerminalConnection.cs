// Copyright (c) 2008-2014 by Gerasimenko Roman
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 
// 1. Redistribution of source code must retain the above copyright
//     notice, this list of conditions and the following disclaimer.
// 
// 2. Redistribution in binary form must reproduce the above copyright
//     notice, this list of conditions and the following disclaimer in
//     the documentation and/or other materials provided with the
//     distribution.
// 
// 3. The name "NJ4X" must not be used to endorse or promote
//     products derived from this software without prior written
//     permission.
//     For written permission, please contact roman.gerasimenko@nj4x.com
// 
// 4. Products derived from this software may not be called "NJ4X",
//     nor may "NJ4X" appear in their name, without prior written
//     permission of Gerasimenko Roman.
// 
// THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
// BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
// LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
// USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
// OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.

using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Text;
using NLog;
using nj4x.Metatrader;

namespace nj4x.Net
{
    internal class MT4TerminalConnection : BasicStrategyRunner
    {
        private const String Rescmd = "R";
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private static long _udpNo;
        private readonly UTF8Encoding _encoding = new UTF8Encoding();
        private BufferedStream _bis;
        private long _commandNo;
        private bool _isDisconnected;
        private int _nextState;
        private bool _noMultiLineConnection;
        private long _rcvd;
        private long _sent;
        private Socket _socket;
        private UdpClient _udpSocket;

        public MT4TerminalConnection(string clientName)
        {
            SetFullClientName(clientName);
            IsNoUDPAllowed = IsOrdersProcessingChannel();
        }

        // ReSharper disable once ConvertToAutoProperty
        private Socket Socket
        {
            get { return _socket; }
            set
            {
//                if (Logger.IsDebugEnabled)
//                {
//                    Logger.Debug("MT4TC.Socket: set to " + value + " " + Environment.StackTrace);
//                }
                _socket = value;
            }
        }

        public override bool IsConnected
        {
            get
            {
                var isConnected = base.IsConnected && !_isDisconnected
                                   && (Socket == null || Socket.IsBound);
                if (!isConnected && Logger.IsDebugEnabled)
                {
                    Logger.Debug("MT4TC.IsConnected: _isDisconnected=" + _isDisconnected
                                 + " _socket=" + SocketInfo()
                                 + " IsBound=" + (Socket != null && Socket.IsBound)
                                 + " " + ClientName
                        );
                }
                return isConnected;
            }
        }

        internal int ID { get; set; }
        public bool IsNoUDPAllowed { get; private set; }

        public override string ToString()
        {
            return "MT4TerminalConnection [hash=" + GetHashCode() + "]: " + SocketInfo() + " " + ClientName;
        }

        public void SetSocket(Socket socket, BufferedStream stream)
        {
            Socket = socket;
            _bis = stream;
//            _bis.ReadTimeout
        }

        public void SetUDPPort(int udpPort)
        {
            if (!IsNoUDPAllowed)
            {
                var ipEndPoint = (Socket.RemoteEndPoint as IPEndPoint);
                if (ipEndPoint != null)
                {
                    _udpSocket = new UdpClient();
                    _udpSocket.Connect(ipEndPoint.Address, udpPort);
                }
            }
        }

        private static void DoNoException(Action m)
        {
            try
            {
                m.Invoke();
            }
// ReSharper disable EmptyGeneralCatchClause
            catch (Exception)
// ReSharper restore EmptyGeneralCatchClause
            {
            }
        }

        public override void Close()
        {
            if (Socket != null || _udpSocket != null)
            {
                if (Logger.IsDebugEnabled)
                    Logger.Debug("MT4TC.Close: " + SocketInfo() + " " + System.Environment.StackTrace);
                //
                if (Socket != null)
                {
                    DoNoException(Socket.Close);
                }
                if (_bis != null)
                {
                    DoNoException(_bis.Close);
                }
                if (_udpSocket != null)
                {
                    DoNoException(_udpSocket.Close);
                }
                _bis = null;
                Socket = null;
                _udpSocket = null;
                //
                base.Close();
            }
        }

        private string SocketInfo()
        {
            try
            {
                if (Socket == null)
                {
                    return "<no socket>";
                }
                var lep = Socket.LocalEndPoint.ToString();
                return lep.Substring(lep.IndexOf(':') + 1) + "/" + Socket.RemoteEndPoint;

//                return Socket == null ? "<no socket>" : "" + Socket.LocalEndPoint + "/" + Socket.RemoteEndPoint;
            }
            catch (ObjectDisposedException e)
            {
                return "<disposed>";
            }
        }

        private void Flush()
        {
            if (!_isDisconnected && Socket.Connected)
            {
                _bis.Flush();
            }
        }

        private void SendToClient(String line)
        {
            if (!_isDisconnected && Socket.Connected)
            {
//                byte[] lineBytes = _encoding.GetBytes(line + '\n');
                var lineBytes = _noMultiLineConnection
                    ? _encoding.GetBytes(line + '\n')
                    : _encoding.GetBytes(MT4.ArgStartC + line + MT4.ArgEndC);
                var length = lineBytes.Length;
                _bis.Write(lineBytes, 0, length);
                _sent += length;
/*
                int cnt = 0;
                var length = lineBytes.Length;
                while (cnt < length)
                {
                    cnt += Socket.Send(lineBytes, cnt, length - cnt, SocketFlags.None);
                }
                _sent += cnt;
*/
                //Console.WriteLine("----Sent(no m/line=" + _noMultiLineConnection + "): " + lineBytes.Length + " bytes ----> " + line);
                //
                if (Logger.IsDebugEnabled)
                {
                    Logger.Debug("[" + SocketInfo() + "]: SENT " + line + " to " + ClientName);
                }
            }
            else
            {
                throw new IOException("Socket not connected: " + Socket);
            }
        }

        private String GetCommand(StringBuilder command)
        {
            try
            {
                return "" + _commandNo + " " + command;
            }
            finally
            {
                _commandNo++;
            }
        }

        public override string SendCommandGetResult(StringBuilder command)
        {
            lock (this)
            {
                //var sb = new StringBuilder();
                var bab = new MemoryStream();
                var state = _nextState; // 0 = GETCMD awaited, 1 = RESCMD awaited
                if (command == IDLE && RunningStrategy.IsTickListenerStrategy())
                {
                    _nextState = 1;
                }
                var resCount = 0;
                while (true)
                {
                    try
                    {
                        int b;
                        //it is udp client 
                        if (_udpSocket != null)
                        {
                            if (_isDisconnected)
                            {
                                //Console.WriteLine("----Exception(MT4TerminalConnection ----");
                                if (IsPendingClose)
                                {
                                    return "R0@2";
                                }
                                throw new Exception("MT4TerminalConnection: Client " + ClientName + " [i/o=" + _rcvd +
                                                    "/" + _sent + "] disconnected");
                            }
                            //
                            var p = new UDPClientPacket(this);
                            if (state == 0)
                            {
//                                Console.WriteLine("----Sending: " + command + "----");
                                p.Send(GetCommand(command));
                            }
                            if (command == IDLE)
                            {
//                                Console.WriteLine("----command == IDLE, ret null----");
                                return null;
                            }
                            //
                            if (_nextState == 1)
                            {
                                _nextState = 0;
                            }
                            return p.Recieve();
                        }
                        //
                        if (state == 1)
                        {
//                            Console.WriteLine("----state == 1----");
                            b = _bis.ReadByte();
                            if (b < 0)
                            {
                                if (Logger.IsInfoEnabled)
                                {
                                    Logger.Info("MT4TerminalConnection: Client " + ClientName + " [i/o=" + _rcvd + "/" +
                                                _sent + "] disconnected");
                                }
                                if (IsPendingClose)
                                {
                                    return "R0@2";
                                }
                                throw new Exception("MT4TerminalConnection: Client " + ClientName + " [i/o=" + _rcvd +
                                                    "/" + _sent + "] disconnected");
                            }
                        }
                        else
                        {
//                            Console.WriteLine("----Sending: " + command + "----");
                            SendToClient(GetCommand(command));
                            if (command == IDLE)
                            {
//                                Console.WriteLine("----command == IDLE, ret null----");
                                Flush();
                                return null;
                            }
                            state = 1;
                            continue;
                        }
                        //
//                        if (b == '\n' || b == '\r')
                        if (b == MT4.ArgEndC && resCount == 1 || _noMultiLineConnection && (b == '\n' || b == '\r'))
                        {
                            //                            var line = sb.ToString();
                            //                            sb.Length = 0;
                            //
                            var line = Encoding.UTF8.GetString(bab.GetBuffer(), 0, (int) bab.Length);
                            bab.SetLength(0);
                            //
                            _rcvd += line.Length;
                            //
                            if (Logger.IsDebugEnabled)
                            {
                                Logger.Debug("[" + SocketInfo() + "]: GOT " + line + " from " + ClientName);
                            }
                            //
                            if (line.StartsWith(Rescmd))
                            {
                                if (_nextState == 1)
                                {
                                    _nextState = 0;
                                }
                                return line.Substring(Rescmd.Length);
                            }
                            Logger.Error("MT4TerminalConnection: Unrecognized client (" + ClientName +
                                         ") request: [" + line + "]");
                            SendToClient(GetCommand(new StringBuilder("-2 UNRECOGNIZED REQ: " + line)));
                            state = 1;
                        }
                        else if (b != 0)
                        {
                            if (resCount > 0 || _noMultiLineConnection)
                            {
//                                sb.Append((char) b);
                                bab.WriteByte((byte) b);
                            }
                            if (b == MT4.ArgStartC)
                            {
                                resCount++;
                            }
                            else if (b == MT4.ArgEndC)
                            {
                                resCount--;
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        _nextState = 0;
                        //                        Console.WriteLine("----_isDisconnected ----");
                        _isDisconnected = true;
                        if (Logger.IsInfoEnabled)
                        {
                            Logger.Info("MT4TerminalConnection: Client " + ClientName + " [i/o=" + _rcvd + "/" + _sent +
                                        "] disconnected", e);
                        }
                        if (IsPendingClose)
                        {
                            return "R0@2";
                        }
                        throw new Exception("MT4TerminalConnection: Client " + ClientName + " [i/o=" + _rcvd + "/" +
                                            _sent +
                                            "] disconnected (" + e.Message + ")", e);
                    }
                }
            }
        }

        internal void SetNoMultiLineConnection(bool b)
        {
            _noMultiLineConnection = b;
        }

        #region Nested type: UDPClientPacket

        public class UDPClientPacket
        {
            private readonly MT4TerminalConnection _c;
            private readonly long _no;

            public UDPClientPacket(MT4TerminalConnection c)
            {
                _c = c;
                lock (typeof (MT4TerminalConnection))
                {
                    _no = _udpNo++;
                }
            }

            public void Send(String cmd)
            {
                var cmdBytes = _c._encoding.GetBytes(cmd);
                //
                //if (Logger.IsDebugEnabled)
                //{
                //    Logger.Debug("" + _no + "->UDP [id=" + _c.ID + "] [" + cmd + "] ...");
                //}
                _c._udpSocket.Send(cmdBytes, cmdBytes.Length);
                _c._sent += cmdBytes.Length;
            }

            public String Recieve()
            {
                IPEndPoint ep = null;
                var bytes = _c._udpSocket.Receive(ref ep);
                var s = _c._encoding.GetString(bytes);
                _c._rcvd += bytes.Length;
                //if (Logger.IsDebugEnabled)
                //{
                //    Logger.Debug("" + _no + "<-UDP [id=" + _c.ID + "] [" + s + "]");
                //}
                return s;
            }
        }

        #endregion
    }
}