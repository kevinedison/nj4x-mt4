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
using System.Configuration;
using System.Net;
using System.Net.Sockets;
using System.Reflection;
using System.Threading;
using NLog;

namespace nj4x.Net
{
    /// <summary>
    /// Passive-mode NJ4X server. Listens for incoming MT4 terminal connections.
    /// </summary>
    /// <remarks>To allow your application work with a remote Terminal Server
    /// App.config file shall contain nj4x_server_host set to the .Net application IP address (which is 127.0.0.1 by default)
    /// <code><![CDATA[
    /// <!-- this is your_application.exe.config -->
    /// <configuration>
    ///   <appSettings>
    ///       <add key="nj4x_server_host" value="dot.net.app.addr"/>
    ///       <add key="nj4x_server_port" value="7777"/>
    ///       <!-- ... -->
    ///   </appSettings>
    ///   <!-- ... -->
    /// </configuration>
    /// ]]></code></remarks>
    public class NJ4XServer
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private static readonly bool UdpTrial = false;//AppSetting("nj4x_use_udp", "false").Equals("true");
        internal static Timer WarnTimer;
        private static volatile NJ4XServer _instance;

        /// <summary>
        /// IP address server must bind at.
        /// See also <c>nj4x_server_host</c> App.config appSettings key.
        /// </summary>
        public readonly String BindHost;

        /// <summary>
        /// NJ4X server port.
        /// See also <c>nj4x_server_port</c> App.config appSettings key.
        /// </summary>
        public readonly int BindPort;

        private readonly UdpClient _datagramSocket;
        private readonly TcpListener _tcpListener;
        private int _incomingConnectionId = 777777;
        private bool _isDead;

/*
        static NJ4XServer()
        {
            Instance = Instance ?? new NJ4XServer(
                                       AppSetting("nj4x_server_host", "127.0.0.1"),
                                       int.Parse(AppSetting("nj4x_server_port", "7777"))
                                       );
        }
*/

        private NJ4XServer(String host, int port)
        {
            BindHost = host;
            BindPort = port;
            //
            var bindToNJ4XHostIPOnly = AppSetting("nj4x_server_host_only", "false").Equals("true");
            if (host.Equals("*") || !bindToNJ4XHostIPOnly)
            {
                _datagramSocket = new UdpClient(BindPort);
                _tcpListener = new TcpListener(BindPort);
            }
            else
            {
                IPAddress ipAddress = IPAddress.Parse(BindHost);
                _datagramSocket = new UdpClient(new IPEndPoint(ipAddress, BindPort));
                _tcpListener = new TcpListener(ipAddress, BindPort);
            }
            new Thread(
                delegate()
                    {
                        _tcpListener.Start();
                        while (!_isDead)
                        {
                            try
                            {
                                Socket socket = _tcpListener.AcceptSocket();
                                var receiveTimeout = ConfigurationManager.AppSettings["nj4x_socket_read_timeout_millis"];
                                socket.ReceiveTimeout = receiveTimeout == null ? -1 : int.Parse(receiveTimeout);
                                socket.SendTimeout = 5000;
                                socket.NoDelay = true;
                                //
/*
                                ThreadPool.QueueUserWorkItem(
                                    new Greeter(socket, UdpTrial ? GetNextIncomingConnectionID() : 0).Run
                                );
*/
                                new Thread(new Greeter(socket, UdpTrial ? GetNextIncomingConnectionID() : 0).Run)
                                {Name = "Greeter " + socket.RemoteEndPoint, IsBackground = true}.Start();
                            }
                            catch (Exception e)
                            {
                                Logger.Error("", e);
                            }
                        }
                    }) {IsBackground = true, Name = "NJ4XServer ListenerThread", Priority = ThreadPriority.Highest}.Start();
            //
            Thread.Sleep(5000);
        }

        /// <summary>
        /// Passive-mode NJ4X server instance accessor.
        /// </summary>
        public static NJ4XServer Instance
        {
            get
            {
                if (_instance == null)
                {
                    lock (typeof (NJ4XServer))
                    {
                        if (_instance == null)
                        {
                            var nj4XServer = new NJ4XServer(AppSetting("nj4x_server_host", "127.0.0.1"), int.Parse(AppSetting("nj4x_server_port", "7777")));
                            _instance = nj4XServer;
                            if (Logger.IsDebugEnabled)
                                Logger.Debug("NJ4XServer started: tcp=" + nj4XServer._tcpListener.LocalEndpoint);
                        }
                    }
                }
                return _instance;
            }
            private set { _instance = value; }
        }

        internal static string AppSetting(string name, string dflt)
        {
            if (ConfigurationManager.AppSettings.Get(name) == null)
            {
                return dflt;
            }
            return ConfigurationManager.AppSettings[name];
        }

        /// <summary>
        /// Stops TCP/IP listener and disposes the resources.
        /// </summary>
        public static void Stop()
        {
            try
            {
                if (Logger.IsDebugEnabled)
                    Logger.Debug("NJ4XServer stopped: tcp=" + Instance._tcpListener.LocalEndpoint);
                Instance._isDead = true;
                if (WarnTimer != null)
                {
                    WarnTimer.Dispose();
                    WarnTimer = null;
                }
                Instance._tcpListener.Stop();
                Instance._datagramSocket.Close();
            }
// ReSharper disable EmptyGeneralCatchClause
            catch (Exception)
// ReSharper restore EmptyGeneralCatchClause
            {
            }
            finally
            {
                Instance = null;
            }
        }

        internal int GetNextIncomingConnectionID()
        {
            lock (this)
            {
                return ++_incomingConnectionId;
            }
        }
    }
}