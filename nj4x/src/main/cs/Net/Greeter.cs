using System;
using System.Collections.Generic;
using System.IO;
using System.Net.Sockets;
using System.Reflection;
using System.Text;
using System.Windows.Forms;
using NLog;
using nj4x.Metatrader;
using Timer = System.Threading.Timer;

namespace nj4x.Net
{
    internal class Greeter
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private static bool _warnMsgShown;

        private static readonly Dictionary<string, Greeter> PendingConnections = new Dictionary<string, Greeter>();
        private readonly UTF8Encoding _encoding = new UTF8Encoding();
        private readonly int _id;
        private readonly Socket _socket;
        private readonly BufferedStream _socketStream;
        private string _boxId;
        private string _clientName;
        private MT4TerminalConnection _strategyRunner;

        public Greeter(Socket socket, int id)
        {
            if (Logger.IsDebugEnabled)
                Logger.Debug("Greeting " + socket.RemoteEndPoint + " at " + socket.LocalEndPoint);
            _socket = socket;
            _id = id;
            _socketStream = new BufferedStream(new NetworkStream(_socket));
//            _socketStream = new NetworkStream(_socket);
        }

        public void Run(object x)
        {
            var sb = new StringBuilder();
            var resStarted = false;
            var firstChar = (char) 0;
            while (true)
            {
                try
                {
                    var b = _socketStream.ReadByte();
                    if (b < 0)
                    {
                        if (Logger.IsInfoEnabled)
                        {
                            Logger.Info("Client " + _clientName + " disconnected");
                        }
                        return;
                    }
                    if (firstChar == (char) 0)
                    {
                        firstChar = (char) b;
                    }
                    //if (b == '\n' || b == '\r')
                    if (b == MT4.ArgEndC || firstChar != MT4.ArgStartC)
                    {
                        resStarted = false;
                        var line = sb.ToString();
                        sb.Length = 0;
                        if (Logger.IsDebugEnabled)
                        {
                            Logger.Debug("GOT FROM " + _clientName + ": " + line);
                        }
                        //
                        if (line.StartsWith("HELLO"))
                        {
                            _clientName = line.Substring(5);
                            _strategyRunner = new MT4TerminalConnection(_clientName);
                            _strategyRunner.SetSocket(_socket, _socketStream);
                            _strategyRunner.SetNoMultiLineConnection(firstChar != MT4.ArgStartC);
                            if (_id != 0 && !_strategyRunner.IsNoUDPAllowed)
                            {
                                SendToClient("HELLO " + _id + " 0"); // 0 - proto
                                try
                                {
                                    _strategyRunner.ID = _id;
                                }
                                catch (Exception e)
                                {
                                    Logger.Error("", e);
                                    _socket.Close();
                                    return;
                                }
                            }
                            else
                            {
                                SendToClient("HELLO");
                            }
                            //
                        }
                        else if (line.StartsWith("BOX"))
                        {
                            _boxId = line.Substring(3);
                            //
                            var activationKey = _boxId.Equals("5")
                                ? NJ4XServer.AppSetting("nj4x_mt5_activation_key", null)
                                : NJ4XServer.AppSetting("nj4x_activation_key",
                                    NJ4XServer.AppSetting("nj4x_mt4_activation_key", null));
                            if (activationKey != null)
                            {
                                SendToClient(activationKey + " ");
                            }
                            else
                            {
                                SendToClient("943289279" + " ");
                            }
                            //
                        }
                        else if (line.StartsWith("XOB"))
                        {
                            var res = line.Substring(3);
                            //
                            var isLimited = !res.StartsWith("OK");
                            //
                            lock (typeof (NJ4XServer))
                            {
                                if (isLimited && NJ4XServer.WarnTimer == null)
                                {
                                    Logger.Info("Setting WarnTimer: " + line);
                                    NJ4XServer.WarnTimer = new Timer(
                                        delegate
                                        {
                                            if (!_warnMsgShown)
                                            {
                                                _warnMsgShown = true;
                                                if (Environment.UserInteractive)
                                                    MessageBox.Show(WarnMsg());
                                            }
                                            //
                                            if (Logger.IsWarnEnabled)
                                            {
                                                Logger.Warn("");
                                                Logger.Warn(
                                                    "*************************************************************************");
                                                Logger.Warn(WarnMsg());
                                                Logger.Warn(
                                                    "*************************************************************************");
                                                Logger.Warn("");
                                            }
                                            else
                                            {
                                                Console.WriteLine("");
                                                Console.WriteLine(
                                                    "*************************************************************************");
                                                Console.WriteLine(WarnMsg());
                                                Console.WriteLine(
                                                    "*************************************************************************");
                                                Console.WriteLine("");
                                            }
                                        },
                                        null, 5000, 60000
                                        );
                                }
                            }
                            //
                            try
                            {
                                var udpPort = 0;
                                if (!isLimited && res.Length > 2)
                                {
                                    udpPort = int.Parse(res.Substring(2));
                                }
                                else if (res.StartsWith("NOK") && res.Length > 3)
                                {
                                    _boxId = res.Substring(3);
                                    //udpPort = Int32.Parse(res.Substring(3));
                                }
                                if (udpPort > 0)
                                {
                                    _strategyRunner.SetUDPPort(udpPort);
                                }
                                //
                                if (_strategyRunner.Start(_clientName, isLimited, this))
                                {
                                    if (Logger.IsInfoEnabled)
                                    {
                                        Logger.Info("Client " + _clientName + " connected, ClientId=" + _strategyRunner.ClientId);
                                    }
                                }
                                else
                                {
                                    lock (PendingConnections)
                                    {
                                        PendingConnections[_clientName] = this;
                                    }
                                    var thisGreeter = this;
                                    Timer t = null;
                                    t = new Timer(state =>
                                    {
                                        Greeter greeter; // = PendingConnections[_clientName];
                                        lock (PendingConnections)
                                        {
                                            PendingConnections.TryGetValue(_clientName, out greeter);
                                        }
                                        if (ReferenceEquals(thisGreeter, greeter))
                                        {
                                            try
                                            {
                                                SendToClient("WAIT");
                                                if (_strategyRunner.Start(_clientName, isLimited, this))
                                                {
                                                    if (Logger.IsInfoEnabled)
                                                    {
                                                        Logger.Info("Client " + _clientName + " connected, ClientId=" + _strategyRunner.ClientId);
                                                    }
                                                }
                                                else
                                                {
                                                    return; // wait more
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                if (Logger.IsInfoEnabled)
                                                {
                                                    Logger.Info("Client " + _clientName + " disconnected (" + e +
                                                                ")");
                                                }
                                            }
                                            //
                                            lock (PendingConnections)
                                            {
                                                PendingConnections.Remove(_clientName);
                                            }
                                        }
                                        else
                                        {
                                            // ReSharper disable once PossibleNullReferenceException
                                            // ReSharper disable once AccessToModifiedClosure
                                            if (t != null)
                                            {
                                                // ReSharper disable once AccessToModifiedClosure
                                                t.Dispose();
                                            }
                                        }
                                    },
//                                        delegate
//                                        {
//                                            lock (PendingConnections)
//                                            {
//                                                if (PendingConnections.TryGetValue(_clientName, out greeter))
//                                                {
//                                                    if (ReferenceEquals(thisGreeter, greeter))
//                                                    {
//                                                        try
//                                                        {
//                                                            SendToClient("WAIT");
//                                                            if (_strategyRunner.Start(_clientName, isLimited, this))
//                                                            {
//                                                                if (Logger.IsInfoEnabled)
//                                                                {
//                                                                    Logger.Info("Client " + _clientName + " connected.");
//                                                                }
//                                                            }
//                                                            else
//                                                            {
//                                                                return; // wait more
//                                                            }
//                                                        }
//                                                        catch (Exception e)
//                                                        {
//                                                            if (Logger.IsInfoEnabled)
//                                                            {
//                                                                Logger.Info("Client " + _clientName + " disconnected (" + e + ")");
//                                                            }
//                                                        }
//                                                        //
//                                                        PendingConnections.Remove(_clientName);
//                                                    }
//                                                }
//                                                // ReSharper disable once PossibleNullReferenceException
//                                                // ReSharper disable once AccessToModifiedClosure
//                                                t[0].Dispose();
//                                            }
//                                        },
                                        null, 10, 2000
                                        );
                                }
                                //
                                return;
                            }
                            catch (Exception e)
                            {
                                Logger.Error("", e);
                                _socket.Close();
                            }
                        }
                        else
                        {
                            Logger.Error("Unrecognized client (" + _clientName + ") request: [" + line + "]");
                        }
                    }
                    else
                    {
                        if (resStarted || firstChar != MT4.ArgStartC)
                        {
                            sb.Append((char) b);
                        }
                        else
                        {
                            resStarted = (b == MT4.ArgStartC);
                        }
                    }
                }
                catch (Exception e)
                {
                    if (Logger.IsInfoEnabled)
                    {
                        Logger.Info("Client " + _clientName + " disconnected (" + e + ")");
                    }
                    break;
                }
            }
        }

        private string WarnMsg()
        {
            return "Trial period for your BOXID (" + _boxId + ") has ended."
                   + "\nIn order to continue using NJ4X API you need to buy NJ4X Personal license"
                   + "\n(http://www.nj4x.com/pricing), register BOXID and get NJ4X activation key"
                   + "\n-------------------------------------------------------------------------"
                   + "\n   API functionality is limited: all methods will be randomly delayed"
                   + "\n-------------------------------------------------------------------------"
                ;
            /*return "Ask for 'nj4x_activation_key', your BOXID=" +
                   _boxId
                   + "\nNJ4X functionality is limited: API methods will be randomly delayed."
                   + "\nRegister your BOXID (" + _boxId + ") for free 30-days trial period at"
                   + "\n    http://www.nj4x.com/downloads."
                ;*/
        }

        internal void SendToClient(string line)
        {
            //byte[] lineBytes = _encoding.GetBytes(line + '\n');
            var lineBytes = _encoding.GetBytes(MT4.ArgStartC + line + MT4.ArgEndC);
            var cnt = 0;
            var length = lineBytes.Length;
            while (cnt < length)
            {
                cnt += _socket.Send(lineBytes, cnt, length - cnt, SocketFlags.None);
            }
/*
            _socketStream.Write(lineBytes, 0, lineBytes.Length);
            _socketStream.Flush();
*/
            //
            if (Logger.IsDebugEnabled)
            {
                Logger.Debug("SENT TO " + _clientName + ": " + line);
            }
        }
    }
}