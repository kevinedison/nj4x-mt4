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
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Reflection;
using System.Text;
using System.Threading;
using NLog;

namespace nj4x.Net
{
    /// <summary>
    ///     TCP/IP client to the Terminal Server.
    /// </summary>
    public class TerminalClient
    {
        public const string MinimumTsVersion = "2.4.0";
        // Create a logger for use in this class
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private readonly String _myName;
        private readonly String _terminalHost;
        private readonly int _terminalPort;
        private ConnectionWorker _connectionWorker;

        /// <summary>
        ///     TS version.
        /// </summary>
        public string TSVersion;

        /// <summary>
        ///     Terminal client constructor.
        /// </summary>
        /// <param name="myName">client name</param>
        /// <param name="terminalHost">TS host IP address</param>
        /// <param name="terminalPort">TS port number</param>
        public TerminalClient(string myName, string terminalHost, int terminalPort)
        {
            _myName = myName;
            _terminalHost = terminalHost;
            _terminalPort = terminalPort;
            Connect();
        }

        /// <summary>
        ///     Connects to the Terminal Server and returns a list of available SRV configuration files.
        /// </summary>
        /// <param name="terminalHost">TS host IP address</param>
        /// <param name="terminalPort">TS port number</param>
        /// <returns>list of SRV file names</returns>
        public static List<String> GetAvailableSrvFiles(string terminalHost, int terminalPort)
        {
            var cli = new TerminalClient("SRV_GETTER", terminalHost, terminalPort);
            try
            {
                return cli.GetAvailableSrvFiles();
            }
            finally
            {
                cli.Close();
            }
        }

        /// <summary>
        ///     Connects to the Terminal Server and returns its box ID.
        /// </summary>
        /// <param name="terminalHost">TS host IP address</param>
        /// <param name="terminalPort">TS port number</param>
        /// <returns>Terminal Server's BOXID</returns>
        public static ulong GetBoxID(string terminalHost, int terminalPort)
        {
            var cli = new TerminalClient("SRV_GETTER", terminalHost, terminalPort);
            try
            {
                return cli.GetBoxID();
            }
            finally
            {
                cli.Close();
            }
        }

        /// <summary>
        ///     Connects to the Terminal Server and KILLS all terminals.
        /// </summary>
        /// <param name="tsHost">TS host IP address</param>
        /// <param name="tsPort">TS port number</param>
        /// <returns>>true - success, all terminals have been killed; false - failure</returns>
        public static bool KillTerminals(string tsHost, int tsPort)
        {
            var cli = new TerminalClient("TERMS_KILLER", tsHost, tsPort);
            try
            {
                return cli.KillTerminals();
            }
            finally
            {
                cli.Close();
            }
        }

        ~TerminalClient()
        {
            Close();
        }

        private void Connect()
        {
            try
            {
                var socket = new TcpClient(_terminalHost, _terminalPort) {NoDelay = true};
                //
                _connectionWorker = new ConnectionWorker(socket);
//                ThreadPool.QueueUserWorkItem(_connectionWorkerThread.Run);
//                var t = new Thread(_connectionWorkerThread.Run) {IsBackground = true};
//                t.Start();
                //
                const int receiveTimeout = 60000;
                socket.ReceiveTimeout = receiveTimeout;
                var s = Ask("HELLO" + _myName + "\u0001" + Version.NJ4X + "\u0001" + Version.NJ4X_UUID);
                socket.ReceiveTimeout = 0;
                if (s == null || s.Equals("NONE"))
                {
                    // peer disconnected
                    throw new Exception("Socket read timeout: " + receiveTimeout);
                }
                var ix = s.LastIndexOf('\u0001');
                TSVersion = ix > 0 ? s.Substring(ix + 1) : "1.7.1";
                _connectionWorker.PeerName = s.Substring(5, ix > 0 ? ix - 5 : s.Length - 5);
                if (s.StartsWith("HELLO"))
                {
                    if (String.Compare(TSVersion, MinimumTsVersion, StringComparison.Ordinal) < 0)
                    {
                        throw new Exception("Unsupported Terminal Server version: " + TSVersion);
                    }
                }
                else
                {
                    throw new Exception("Unsupported NJ4X API version (" + Version.NJ4X + ") by Terminal Server (v" +
                                        TSVersion + ")");
                }
            }
            catch (Exception e)
            {
                Close();
                Logger.Fatal("Could not establish connection to terminal server: " + _terminalHost + ':' +
                             _terminalPort, e);
                throw;
            }
        }

        /// <summary>
        ///     Terminal Server host name.
        /// </summary>
        /// <returns>Terminal Server host name.</returns>
        public String GetTSName()
        {
            return _connectionWorker == null ? null : _connectionWorker.PeerName;
        }

        /// <summary>
        ///     Send request to TS.
        /// </summary>
        /// <param name="msg">message to be sent to TS</param>
        /// <returns></returns>
        public String Ask(String msg)
        {
            return _connectionWorker.Ask(msg);
        }

        /// <summary>
        ///     Returns a list of available SRV configuration files.
        /// </summary>
        /// <returns>list of SRV file names</returns>
        public List<String> GetAvailableSrvFiles()
        {
            var res = new List<string>();
            var ask = Ask("GETSRV:");
            var split = ask.Split('|');
            res.AddRange(split);
            return res;
        }

        /// <summary>
        ///     Returns BoxID.
        /// </summary>
        /// <returns>nj4x box identifier</returns>
        public ulong GetBoxID()
        {
            var res = Ask("GETBOXID:");
            return ulong.Parse(res);
        }

        /// <summary>
        ///     Kills all terminals visible to the Terminal Server.
        /// </summary>
        /// <returns>true - success, false - failure</returns>
        public bool KillTerminals()
        {
            var res = Ask("KILLTERMS:");
            return res.StartsWith("OK");
        }

        /// <summary>
        ///     Close tcp/ip connection to TS.
        /// </summary>
        public void Close()
        {
            if (_connectionWorker != null && _connectionWorker.Socket != null)
            {
                _connectionWorker.Socket.Close();
            }
        }

        #region Nested type: ConnectionWorkerThread

        private class ConnectionWorker
        {
            private const String None = "NONE";
            private const int Timeout = 300000;
            private readonly UTF8Encoding _encoding = new UTF8Encoding();
            private readonly Stream _networkStream;
            internal readonly TcpClient Socket;
            private String _ret;
            internal String PeerName;

            public ConnectionWorker(TcpClient socket)
            {
                Socket = socket;
                PeerName = socket.Client.RemoteEndPoint.ToString();
                _networkStream = Socket.GetStream();
                _networkStream.WriteTimeout = 1000;
                _networkStream.ReadTimeout = Timeout;
            }

            public void Run(object state)
            {
                var sb = new StringBuilder();
                while (Socket.Connected)
                {
                    int b;
                    try
                    {
                        b = _networkStream.ReadByte();
                    }
                    catch (Exception)
                    {
                        if (Logger.IsDebugEnabled)
                            Logger.Debug("Peer " + PeerName + " disconnected");
                        return;
                    }
                    if (b < 0)
                    {
                        if (Logger.IsDebugEnabled)
                            Logger.Debug("Peer " + PeerName + " disconnected");
                        return;
                    }
                    if (b == '\n' || b == '\r')
                    {
                        var line = sb.ToString();
                        sb.Length = 0;
                        if (!line.Equals("ARE_YOU_STILL_THERE"))
                            ProcessClientRequest(line);
                    }
                    else
                    {
                        sb.Append((char) b);
                    }
                }
            }

            private void ProcessClientRequest(String line)
            {
                lock (this)
                {
                    if (Logger.IsDebugEnabled)
                        Logger.Debug("GOT FROM " + PeerName + ": " + line);
                    _ret = line;
                    Monitor.Pulse(this);
                }
            }

            internal String Ask(String line)
            {
                lock (this)
                {
                    var lineBytes = _encoding.GetBytes(line + '\n');
                    _networkStream.Write(lineBytes, 0, lineBytes.Length);
                    _networkStream.Flush();

                    if (Logger.IsDebugEnabled)
                        Logger.Debug("SENT TO " + PeerName + ": " + line);

                    _ret = None;
                    var sb = new StringBuilder();
                    while (Socket.Connected)
                    {
                        int b;
                        try
                        {
                            b = _networkStream.ReadByte();
                        }
                        catch (Exception e)
                        {
                            if (Logger.IsDebugEnabled)
                                Logger.Debug("Peer " + PeerName + " disconnected", e);
                            return None;
                        }
                        if (b < 0)
                        {
                            if (Logger.IsDebugEnabled)
                                Logger.Debug("Peer " + PeerName + " disconnected");
                            return None;
                        }
                        if (b == '\n' || b == '\r')
                        {
                            _ret = sb.ToString();
                            sb.Length = 0;
                            if (Logger.IsDebugEnabled)
                                Logger.Debug("GOT FROM " + PeerName + ": " + _ret);
                            if (!_ret.Equals("ARE_YOU_STILL_THERE"))
                                return _ret;
                        }
                        else
                        {
                            sb.Append((char) b);
                        }
                    }
                    return _ret;
                }
            }

            internal String Ask_old(String line)
            {
                lock (this)
                {
                    var lineBytes = _encoding.GetBytes(line + '\n');
                    _networkStream.Write(lineBytes, 0, lineBytes.Length);

                    if (Logger.IsDebugEnabled)
                        Logger.Debug("SENT TO " + PeerName + ": " + line);

                    var start = CurrentTimeMillis();
                    _ret = None;
                    //noinspection StringEquality
                    while (_ret == None && Thread.CurrentThread.IsAlive)
                    {
                        Monitor.Wait(this, 1000);
                        if (CurrentTimeMillis() - start > Timeout)
                        {
                            throw new Exception("Request timed out (" + Timeout + " millis)");
                        }
                    }
                    return _ret;
                }
            }

            private static long CurrentTimeMillis()
            {
                return DateTime.UtcNow.Ticks/10000L;
            }
        }

        #endregion
    }
}