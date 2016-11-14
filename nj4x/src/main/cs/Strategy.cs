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
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Threading;
using nj4x.Metatrader;
using nj4x.Net;
using NLog;

namespace nj4x
{
    /// <summary>
    ///     Connect to the MT4 client terminal, do trading,
    ///     implement custom strategies in descendant classes.
    /// </summary>
    /// <example>
    ///     <code><![CDATA[
    ///   namespace Tests
    ///   {
    ///       using System;
    ///       using nj4x.Metatrader;
    ///       public class App
    ///       {
    ///           public static void Main(string[] args)
    ///           {
    ///               var mt4 = new nj4x.Strategy();
    ///               mt4.Connect("localhost", 7788,
    ///                           new Broker("Skyeast-Demo Accounts Server"), "90412100", "qm8cmeu");
    ///               using (mt4)
    ///               {
    ///                   Console.WriteLine(
    ///                       String.Format("Balance={0}, Bid={1}",
    ///                                     mt4.AccountBalance(),
    ///                                     mt4.Marketinfo("EURUSD", MarketInfo.MODE_BID)
    ///                           ));
    ///               }
    ///           }
    ///       }
    ///   }
    /// ]]></code>
    /// </example>
    public class Strategy : MT4, IDisposable
    {
        #region Delegates

        /// <summary>
        ///     Tick events handler delegate.
        /// </summary>
        /// <param name="symbol">A symbol the tick belongs to</param>
        /// <param name="tick">received tick information</param>
        /// <param name="connection">MT4 connection to be used for tick processing</param>
        public delegate void TickHandler(String symbol, TickInfo tick, MT4 connection);

        #endregion

        #region TerminalType enum

        /// <summary>
        ///     Enumeration of additional strategy terminal workers.
        /// </summary>
        public enum TerminalType
        {
            /// <summary>
            ///     Unspecified purpose worker - terminals of this type can be used for custom parallel processing under the MT4
            ///     account.
            /// </summary>
            FreeWorker,

            /// <summary>
            ///     Terminals of this type provide simultaneous OrderSend/Modify/Cancel/Delete operations - one can close two
            ///     different orders from two different Threads in parallel - Strategy.OrderClose method will not block if there
            ///     is enough ORDERS_WORKER terminals are opened for the strategy.
            /// </summary>
            OrdersWorker,

            /// <summary>
            ///     Terminals of this type are used to get different symbol's real-time ticks in parallel.
            /// </summary>
            TickWorker
        }

        #endregion

        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();
        private static readonly Hashtable RegisteredStrategies = new Hashtable();
        internal static readonly String SPCH = "SPCH";
        private readonly Stack<Terminal> _orderTerminals = new Stack<Terminal>();
        private readonly List<Terminal> _terminals = new List<Terminal>();
        private bool _asynchOrderOperations;
        private Chart _bulkTickListener;
        private List<Chart> _charts;
// ReSharper disable NotAccessedField.Local
        private volatile ArrayList _groups;
// ReSharper restore NotAccessedField.Local
        private volatile Hashtable _groupsMap;
        private String _id;
        private volatile List<Instrument> _instruments;
        private bool _isAlive;
        private bool _isOrderTerminalsPresent;
        private String _mt4Password;
        private Broker _mt4Server;
        private String _mt4User;
        private String _nj4xServerHost;
        private HistoryPeriod _historyPeriod;
        private bool _restart;
        private Chart _positionListener;
        private volatile List<String> _symbols;
        private Terminal _terminal;
        private String _termServerHost;
        private String _termServerHostName;
        private int _termServerPort;
        private String _termServerVersion;
        protected TerminalClient Cli;

        /// <summary>
        ///     Default Strategy constructor.
        /// </summary>
        public Strategy()
        {
            IsReconnect = true;
            Period = Timeframe.PERIOD_H4;
            _historyPeriod = HistoryPeriod.DEFAULT;
            _restart = false;
        }

        /// <summary>
        ///     List of MT4 terminal instruments (symbols)
        /// </summary>
        public List<Instrument> Instruments
        {
            get
            {
                InitSymbols();
                return _instruments;
            }
            private set { _instruments = value; }
        }

        /// <summary>
        ///     List of MT4 terminal symbols
        /// </summary>
        public List<String> Symbols
        {
            get
            {
                InitSymbols();
                return _symbols;
            }
//            private set { _symbols = value; }
        }

        /// <summary>
        ///     Strategy time frame
        /// </summary>
        public new Timeframe Period { get; set; }

        /// <summary>
        ///     Main strategy symbol
        /// </summary>
        public new String Symbol { get; private set; }

        /// <summary>
        ///     Flag of the automatic reconnection to MT4 terminal.
        /// </summary>
        public bool IsReconnect { get; set; }

        /// <summary>
        ///     Flag to indicate this Strategy instance has limited functionality due to trial NJ4X version.
        /// </summary>
        public bool IsLimitedFunctionality { get; internal set; }

        internal bool IsAlive
        {
            get { return _isAlive; }
            private set
            {
//                if (Logger.IsDebugEnabled)
//                {
//                    Logger.Debug("Strategy.IsAlive: set to " + value + " " + Environment.StackTrace);
//                }
                _isAlive = value;
            }
        }

        #region IDisposable Members

        /// <summary>
        ///     Disconnects from MT4 terminal.
        /// </summary>
        public virtual void Dispose()
        {
            Disconnect(true);
        }

        #endregion

        /// <summary>
        ///     Returns NJ4X API version.
        /// </summary>
        /// <returns>Returns NJ4X API version.</returns>
        public Version GetVersion()
        {
            return new Version(
                _termServerVersion,
                GetDLLVersion(),
                GetMQLVersion()
                );
        }

        private void InitSymbols()
        {
            lock (this)
            {
                var symbolsTotal = SymbolsTotal(false);
                if (_instruments != null && _instruments.Count == symbolsTotal)
                {
                    return;
                }

                var groupsMap = new Hashtable();
                //            var groups = new ArrayList();
                var symbols = new List<string>();
                var instruments = new List<Instrument>();
                //
                for (var i = 0; i < symbolsTotal; i++)
                {
                    var sym = SymbolName(i, false);
                    var grp = "all";

                    var gSymbols = (ArrayList)groupsMap[grp];
                    if (gSymbols == null)
                    {
                        groupsMap.Add(grp, gSymbols = new ArrayList());
                    }
                    gSymbols.Add(sym);
                    symbols.Add(sym);
                    var six = sym.Length >= 6 && !sym.Contains("#");
                    instruments.Add(new Instrument(sym, six ? sym.Substring(0, 3) : "", six ? sym.Substring(3, 3) : ""));
                }
                //
                _symbols = symbols;
                _instruments = instruments;
                _groupsMap = groupsMap;
            }
        }

        internal virtual void Init(string symbol, int period, BasicStrategyRunner basicStrategyRunner)
        {
            lock (this)
            {
                IsAlive = true;
                if (Cli != null)
                {
/*
                    var bindHost = _nj4xServerHost ??
                                   (NJ4XServer.Instance.BindHost.Equals("*")
                                        ? "127.0.0.1"
                                        : NJ4XServer.Instance.BindHost);
                    String cmd = "GETSYMBOLS:"
                                 //+ _mt4Server.val + '|' + _mt4User + "|||||";
                                 + _mt4Server.val + '|' + _mt4User +
                                 "||" + _id + '|' + bindHost
                                 + '|' + NJ4XServer.Instance.BindPort + ':' + GetTenant()
                                 + '|' + (Symbol ?? "");
                    String s = Cli.Ask(cmd); // S,G|S2,G2|...
                    //
                    var doc = new XmlDocument();
                    doc.Load(new MemoryStream(Encoding.UTF8.GetBytes(s)));
                    XmlNode eSymbols = doc.SelectSingleNode("symbols");
                    if (eSymbols != null)
                    {
//                    Element eSymbols = DOMUtil.findElement(doc, "symbols");
                        XmlNodeList nodeList = eSymbols.SelectNodes("symbol");
//                    Iterator i = DOMUtil.getTopElements(eSymbols, "symbol");
                        if (nodeList != null)
                        {
                            _groupsMap = new Hashtable();
                            _groups = new ArrayList();
                            Symbols = new List<string>();
                            Instruments = new List<Instrument>();
                            //
                            foreach (XmlNode node in nodeList)
                            {
                                var eSym = node as XmlElement;
                                if (eSym != null)
                                {
                                    String sym = eSym.GetAttribute("name");
                                    String grp = eSym.GetAttribute("group");

                                    var gSymbols = (ArrayList) _groupsMap[grp];
                                    if (gSymbols == null)
                                    {
                                        _groupsMap.Add(grp, gSymbols = new ArrayList());
                                    }
                                    gSymbols.Add(sym);
                                    Symbols.Add(sym);
                                    Instruments.Add(new Instrument(sym, eSym.GetAttribute("sym_1"),
                                                                   eSym.GetAttribute("sym_2")));
                                }
                            }
                        }
                    } //
*/
                    Cli.Close();
                    Cli = null;
                }
                //
                Symbol = symbol;
                Period = (Timeframe) period;
                //
                Init();
            }
        }

        /// <summary>
        ///     This method is called once, immediately after MT4 terminal connection establishement.
        /// </summary>
        /// <example>
        ///     <code><![CDATA[
        ///    namespace Tests
        ///    {
        ///        using nj4x.Metatrader;
        ///        public class OtherStrategy : nj4x.Strategy
        ///        {
        ///            public OtherStrategy()
        ///            {
        ///                Connect("localhost", 7788,
        ///                    new Broker("Skyeast-Demo Accounts Server"), "90385752", "7blx6tq");
        ///            }
        /// 
        ///            private List<MyOrder> _currentOrders;
        ///            public override void Init()
        ///            {
        ///                _currentOrders = new List<MyOrder>();
        ///                int count = OrdersTotal();
        ///                for (int i = 0; i < count; i++)
        ///                {
        ///                    if (OrderSelect(i, SelectionType.SELECT_BY_POS, SelectionPool.MODE_TRADES))
        ///                    {
        ///                        _currentOrders.Add(new MyOrder(this));
        ///                    }
        ///                }
        ///            }
        /// 
        ///            private class MyOrder
        ///            {
        ///                private int _ticket;
        ///                private int _magicNumber;
        ///                private double _profit;
        /// 
        ///                public MyOrder(nj4x.Strategy mt4)
        ///                {
        ///                    _ticket = mt4.OrderTicket();
        ///                    _magicNumber = mt4.OrderMagicNumber();
        ///                    _profit = mt4.OrderProfit();
        ///                }
        ///            }
        /// 
        ///        }
        ///    }
        ///  ]]></code>
        /// </example>
        public virtual void Init()
        {
        }

        internal virtual bool IsReconnectAllowed()
        {
            return true;
        }

        /// <summary>
        ///     This method is called once, after MT4 terminal disconnection.
        /// </summary>
        public virtual void Deinit()
        {
            IsAlive = false;
            while (IsReconnect && IsTerminalServerOk() && !(this is Chart))
            {
                try
                {
                    if (_strategyRunner != null)
                    {
                        _strategyRunner.Close();
                        _strategyRunner = null;
                    }
                    if (OrdersProcessingChannel != null)
                    {
                        OrdersProcessingChannel.Close();
                        OrdersProcessingChannel = null;
                    }
                    if (IsReconnectAllowed())
                    {
                        Connect(_termServerHost, _termServerPort, _mt4Server, _mt4User, _mt4Password, _symbol,
                            _asynchOrderOperations);
                        break;
                    }
                }
                catch (Exception)
                {
                    Thread.Sleep(1000);
                }
            }
        }

        /// <summary>
        ///     This method is used to initiate MT4 connection.
        /// </summary>
        /// <param name="terminalServerHost">Terminal Server host address</param>
        /// <param name="terminalServerPort">Terminal Server port</param>
        /// <param name="mt4Server">Broker configuration ID</param>
        /// <param name="mt4User">User ID</param>
        /// <param name="mt4Password">User Password</param>
        /// <example>
        ///     <code><![CDATA[
        ///     var mt4 = new nj4x.Strategy();
        ///     mt4.Connect("localhost", 7788,
        ///         new Broker("Skyeast-Demo Accounts Server"), "90412100", "qm8cmeu");
        /// ]]></code>
        /// </example>
        public void Connect(string terminalServerHost, int terminalServerPort, Broker mt4Server, string mt4User,
            string mt4Password)
        {
            Connect(terminalServerHost, terminalServerPort, mt4Server, mt4User, mt4Password, null, false);
        }

        /// <summary>
        ///     This method is used to initiate MT4 connection.
        /// </summary>
        /// <param name="terminalServerHost">Terminal Server host address</param>
        /// <param name="terminalServerPort">Terminal Server port</param>
        /// <param name="mt4Server">Broker configuration ID</param>
        /// <param name="mt4User">User ID</param>
        /// <param name="mt4Password">User Password</param>
        /// <param name="asynchOrderOperations">Pass TRUE if asynchronous Order* method calls are required</param>
        /// <example>
        ///     <code><![CDATA[
        ///     var mt4 = new nj4x.Strategy();
        ///     mt4.Connect("localhost", 7788,
        ///         new Broker("Skyeast-Demo Accounts Server"), "90412100", "qm8cmeu", true);
        /// ]]></code>
        /// </example>
        public void Connect(string terminalServerHost, int terminalServerPort, Broker mt4Server, string mt4User,
            string mt4Password, bool asynchOrderOperations)
        {
            Connect(terminalServerHost, terminalServerPort, mt4Server, mt4User, mt4Password, null, asynchOrderOperations);
        }

        /// <summary>
        ///     This method is used to initiate MT4 connection.
        /// </summary>
        /// <param name="terminalServerHost">Terminal Server host address</param>
        /// <param name="terminalServerPort">Terminal Server port</param>
        /// <param name="mt4Server">Broker configuration ID</param>
        /// <param name="mt4User">User ID</param>
        /// <param name="mt4Password">User Password</param>
        /// <param name="symbol">Default trading instrument</param>
        /// <example>
        ///     <code><![CDATA[
        ///     var mt4 = new nj4x.Strategy();
        ///     mt4.Connect("localhost", 7788,
        ///         new Broker("Skyeast-Demo Accounts Server"), "90412100", "qm8cmeu", "EURUSD");
        /// ]]></code>
        /// </example>
        public void Connect(string terminalServerHost, int terminalServerPort, Broker mt4Server, string mt4User,
            string mt4Password, String symbol)
        {
            Connect(terminalServerHost, terminalServerPort, mt4Server, mt4User, mt4Password, symbol, false);
        }

        /// <summary>
        ///     Used to setup all back address in case nj4x_server_host configuration parameter == "*"
        /// </summary>
        /// <param name="host">Local machine IP address</param>
        /// <returns></returns>
        public Strategy WithNJ4XServerHost(String host)
        {
            _nj4xServerHost = host;
            return this;
        }

        /// <summary>
        ///     Used to setup history period client terminal will load historical orders of at startup.
        /// </summary>
        /// <param name="p">History period</param>
        /// <returns></returns>
        public Strategy WithHistoryPeriod(HistoryPeriod p)
        {
            _historyPeriod = p;
            return this;
        }

        /// <summary>
        ///     Used before Connect method call to instruct TS to restart client terminal if it is running.
        /// </summary>
        /// <returns></returns>
        public Strategy WithRestart()
        {
            _restart = true;
            return this;
        }

        /// <summary>
        ///     Time interval to load historical data of
        /// </summary>
        public enum HistoryPeriod
        {
            DEFAULT = 0, TODAY, LAST_3_DAYS, LAST_WEEK, LAST_MONTH, LAST_3_MONTHS, LAST_6_MONTHS, ALL_HISTORY
        }

        /// <summary>
        ///     This method is used to initiate MT4 connection and set default strategy Symbol.
        /// </summary>
        /// <param name="terminalServerHost">Terminal Server host address</param>
        /// <param name="terminalServerPort">Terminal Server port</param>
        /// <param name="mt4Server">Broker configuration ID</param>
        /// <param name="mt4User">User ID</param>
        /// <param name="mt4Password">User Password</param>
        /// <param name="symbol">Default trading instrument</param>
        /// <param name="asynchOrderOperations">Pass TRUE if asynchronous Order* method calls are required</param>
        /// <exception cref="Exception">In case of any connection errors</exception>
        /// <example>
        ///     <code><![CDATA[
        ///     var mt4 = new nj4x.Strategy();
        ///     mt4.Connect("localhost", 7788,
        ///         new Broker("Skyeast-Demo Accounts Server"), "90412100", "qm8cmeu", "AUDUSD", false);
        /// ]]></code>
        /// </example>
        public void Connect(string terminalServerHost, int terminalServerPort, Broker mt4Server, string mt4User,
            string mt4Password, String symbol, bool asynchOrderOperations)
        {
            lock (this)
            {
                _mt4Server = mt4Server;
                _mt4User = mt4User;
                _mt4Password = mt4Password;
                _asynchOrderOperations = asynchOrderOperations;
                _termServerHost = terminalServerHost;
                _termServerPort = terminalServerPort;
                _symbol = symbol;
                Symbol = symbol;
                if (_terminal != null)
                {
                    _terminal.InjectCharts();
                }
                //
                if (Logger.IsInfoEnabled)
                {
                    Logger.Info("Trying to connect to " + _mt4Server.val + " via " + terminalServerHost + ":" +
                                terminalServerPort);
                }
                if (Cli != null)
                {
                    try
                    {
                        Cli.Close();
                    }
                    catch (Exception)
                    {
                        // ignored
                    }
                }
                try
                {
                    var port = terminalServerPort;
                    Cli = new TerminalClient(mt4User + "@" + mt4Server.val + ":" + GetTenant(), terminalServerHost, port);
//                    Cli = new TerminalClient(Version.NJ4X_UUID + " " + GetTenant() + " " + mt4Server.val + " " + mt4User, terminalServerHost, port);
                    _termServerVersion = Cli.TSVersion;
                    _termServerHost = terminalServerHost;
                    _termServerPort = terminalServerPort;
                    //
                    _termServerHostName = Cli.GetTSName();
                    if (_termServerHostName == null || _termServerHostName.Equals("TerminalServer"))
                    {
                        Logger.Warn("Invalid TS hostname response: " + _termServerHostName);
                        _termServerHostName = _termServerHost;
                    }
                    //
                    var bindHost = _nj4xServerHost ??
                                   (NJ4XServer.Instance.BindHost.Equals("*")
                                       ? "127.0.0.1"
                                       : NJ4XServer.Instance.BindHost);
                    var cmd = "RUNTERM" + (asynchOrderOperations ? "" : ":") + ":" + _mt4Server.val + '|' + _mt4User 
                        + (_historyPeriod == HistoryPeriod.DEFAULT ? "" : ":" + ((int) _historyPeriod)) 
                                + '|' + _mt4Password + "|$id$|" + bindHost
                              + '|' + NJ4XServer.Instance.BindPort + ':' + GetTenant()
                              + '|' + (symbol != null && symbol.Trim().Length > 0 ? symbol.Trim() : "")
                            + (Period == Timeframe.PERIOD_DEFAULT ? "" : ":" + ((int) Period))
                              ;
                    if (_charts != null && _charts.Count > 0)
                    {
                        var csb = new StringBuilder();
                        foreach (var c in _charts)
                        {
                            csb.Append('|').Append(c.GetChartParams());//todo add Period
                        }
                        //
                        cmd = cmd + '|' + _charts.Count + csb;
                        //
                    }
                    if (_restart)
                    {
                        Cli.Ask(cmd.Replace("RUNTERM", "KILLTERM"));
                    }
                    //
                    if (_id == null)
                    {
                        _id = RegisterStrategyGetRegId(this);
                    }
                    else
                    {
                        RegisterStrategy(this);
                    }
                    cmd = cmd.Replace("$id$", _id);
                    var s = Cli.Ask(cmd);
                    //
                    if (s.StartsWith("OK") && s.EndsWith(" running"))
                    {
                        var strategy1 = GetStrategy(_id + '1');
                        var strategy2 = GetStrategy(_id + '2');
                        if (Logger.IsDebugEnabled)
                        {
                            Logger.Debug("Start waiting: Got [" + s + "], _id=[" + _id + "] " + _mt4User + " strategy1=" + strategy1 +
                                         " strategy2=" + strategy2);
                        }
                        for (var t = 0;
                            t < ((!ReferenceEquals(strategy1, this) || _asynchOrderOperations && !ReferenceEquals(strategy2, this)) ? 36 : 18) 
                            &&
                            (ReferenceEquals(strategy1, this) || ReferenceEquals(strategy2, this));
                            ++t)
                        {
                            if (Logger.IsDebugEnabled)
                            {
                                Logger.Debug("Do waiting t=" + t + ": Got [" + s + "], _id=[" + _id + "] "+_mt4User+" strategy1=" +
                                             strategy1 + " strategy2=" + strategy2);
                            }
                            Monitor.Wait(this, 500);
                            //
                            strategy1 = CheckStrategyRegistration();
//                            if (ReferenceEquals(strategy1, this))
//                            {
//                                continue;
//                            }
                            strategy2 = _asynchOrderOperations ? GetStrategy(_id + '2') : null;
                        }
                        if (Logger.IsDebugEnabled)
                        {
                            Logger.Debug("End waiting   : Got [" + s + "], _id=[" + _id + "] " + _mt4User + " strategy1=" + strategy1 +
                                         " strategy2=" + strategy2);
                        }
                        if (ReferenceEquals(strategy1, this) || ReferenceEquals(strategy2, this))
                        {
//                            Cli.Ask(cmd.Replace("RUNTERM", "KILLTERM"));
                            RegisterStrategy(this);
                            s = Cli.Ask("L" + cmd);
                        }
                    }
                    //
                    if (s.StartsWith("OK"))
                    {
                        cmd = cmd.Replace("RUNTERM", "CHKTERM");
                        var strategy1 = CheckStrategyRegistration();
                        var strategy2 = GetStrategy(_id + '2');
                        for (var t = 0; (strategy1 == this || strategy2 == this); ++t)
                        {
                            if (t%2 == 1)
                            {
                                s = Cli.Ask(cmd);
                                if (!s.StartsWith("OK"))
                                {
                                    ProcessFailConectionResp(s);
                                }
                            }
                            else
                            {
                                Monitor.Wait(this, 2000); //todo check if it is interrupted/signalled
                            }
                            //
                            strategy1 = CheckStrategyRegistration();
//                            if (ReferenceEquals(strategy1, this))
//                            {
//                                continue;
//                            }
                            strategy2 = _asynchOrderOperations ? GetStrategy(_id + '2') : null;
                        }
                        if (ReferenceEquals(strategy1, this) || ReferenceEquals(strategy2, this))
                        {
                            throw new Exception("Connection to " + _mt4Server.val +
                                                " has been timed out, check server/user/password");
                        }
                        else
                        {
                            StartCoordination();
                        }
                    }
                    else
                    {
                        ProcessFailConectionResp(s);
                    }
                }
                finally
                {
                    if (Cli != null)
                    {
                        try
                        {
                            Cli.Close();
                        }
                        finally
                        {
                            Cli = null;
                        }
                    }
                    RemoveStrategy(_id + '1');
                    RemoveStrategy(_id + '2');
                }
            }
        }

        private static void ProcessFailConectionResp(string s)
        {
            if (s.StartsWith("No connection to server: "))
            {
                throw new NJ4XNoConnectionToServerException(s);
            }
            if (s.StartsWith("Reached max number of terminals: "))
            {
                throw new NJ4XMaxNumberOfTerminalsExceededException(s);
            }
            if (s.StartsWith("Invalid user name or password: "))
            {
                throw new NJ4XInvalidUserNameOrPasswordException(s);
            }
            throw new Exception(s);
        }

        private void StartCoordination()
        {
            var sr = ((BasicStrategyRunner) StrategyRunner);
            sr.PreInit();
            sr.StartCoordination();
            if (_charts != null && _charts.Count > 0)
            {
                foreach (var c in _charts)
                {
                    sr = ((BasicStrategyRunner) c.StrategyRunner);
                    sr.PreInit();
                    sr.StartCoordination();
                }
            }
        }

        private Strategy CheckStrategyRegistration()
        {
            var strategy1 = GetStrategy(_id + '1');
            if (strategy1 == null && _charts != null && _charts.Any(c => GetStrategy(_id + '|' + c.Symbol) != null))
            {
                strategy1 = this;
            }
            return strategy1;
        }

        internal static Strategy GetStrategy(string id)
        {
            lock (RegisteredStrategies)
            {
                return (Strategy) RegisteredStrategies[id];
            }
        }

        internal static void RemoveStrategy(string id)
        {
            lock (RegisteredStrategies)
            {
                RegisteredStrategies.Remove(id);
            }
        }

        private static void RegisterStrategy(Strategy s)
        {
            lock (RegisteredStrategies)
            {
                if (RegisteredStrategies.Contains(s._id + '1') && RegisteredStrategies[s._id + '1'] != s
                    || RegisteredStrategies.Contains(s._id + '2') && RegisteredStrategies[s._id + '2'] != s)
                {
                    throw new MT4Exception(65536 + 1, s._mt4User + " is already connecting to " + s._mt4Server.val);
                }
                RegisteredStrategies[s._id + '1'] = s;
                if (s._asynchOrderOperations)
                {
                    RegisteredStrategies[s._id + '2'] = s;
                }
                else
                {
                    RegisteredStrategies.Remove(s._id + '2');
                }
                //
                var charts = s.GetCharts();
                if (charts != null && charts.Count > 0)
                {
                    foreach (var c in charts)
                    {
                        RegisteredStrategies[s._id + '|' + c.Symbol] = c;
                    }
                }
            }
        }

//        private static long CurrentTimeMillis()
//        {
//            return DateTime.UtcNow.Ticks/10000L;
//        }

        private static string MD5(string input)
        {
            var x = new MD5CryptoServiceProvider();
            var bs = Encoding.UTF8.GetBytes(input);
            bs = x.ComputeHash(bs);
            var s = new StringBuilder();
            foreach (var b in bs)
            {
                s.Append(b.ToString("x2").ToUpper());
            }
            var password = s.ToString();
            return password;
        }

        protected virtual String GetTenant()
        {
            if (_charts == null || _charts.Count == 0 /* || _terminal != null*/)
            {
                return "";
            }
            var sb = new StringBuilder();
            foreach (var c in _charts)
            {
                if (sb.Length > 0)
                {
                    sb.Append('_');
                }
                sb.Append(c.ChartId.StartsWith(SPCH) ? c.ChartId.Substring(SPCH.Length) : c.ChartId);
            }
            //
            if (sb.Length < 32)
            {
                return sb.ToString();
            }
            return /*"" + _charts.Count + " charts " + */MD5(sb.ToString());
        }

        private static string RegisterStrategyGetRegId(Strategy s)
        {
            String id;
            lock (RegisteredStrategies)
            {
//                id = s._mt4User + '_' + _strategyID;
                id = MD5(s._termServerHostName + s._termServerPort + s.Symbol + s._mt4User
                         + s._mt4Server.val + s._mt4Password + s.GetTenant());
                if (RegisteredStrategies.Contains(id + '1') || RegisteredStrategies.Contains(id + '2'))
                {
                    throw new MT4Exception(65536 + 1, s._mt4User + " is already connecting to " + s._mt4Server.val);
                }
                RegisteredStrategies[id + '1'] = s;
                if (s._asynchOrderOperations)
                {
                    RegisteredStrategies[id + '2'] = s;
                }
                else
                {
                    RegisteredStrategies.Remove(id + '2');
                }
                //
                var charts = s.GetCharts();
                if (charts != null && charts.Count > 0)
                {
                    foreach (var c in charts)
                    {
                        RegisteredStrategies[id + '|' + c.Symbol] = c;
                    }
                }
            }
            return id;
        }

        /// <summary>
        ///     Disconnects from MT4 terminal.
        /// </summary>
        public void Disconnect()
        {
            Disconnect(false);
        }

        /// <summary>
        ///     Disconnects from MT4 terminal.
        /// </summary>
        /// <param name="immediately">
        ///     pass true if immediate mt4 terminal process termination is required, otherwise
        ///     JFX_TERM_IDLE_TMOUT_SECONDS env. variable will be used by MT4 terminal to exit at idle timeout.
        /// </param>
        public void Disconnect(bool immediately)
        {
            Terminate(immediately);
        }

        private readonly Object _synch = new object();
        private string _symbol;

        /// <summary>
        ///     Kills MT4 Client Terminal (remote) process
        /// </summary>
        /// <param name="immediately">
        ///     pass true if immediate mt4 terminal process termination is required, otherwise
        ///     JFX_TERM_IDLE_TMOUT_SECONDS env. variable will be used by MT4 terminal to exit at idle timeout.
        /// </param>
        /// <remarks>
        ///     This method is called automatically by
        ///     <see>
        ///         <cref>Disconnect</cref>
        ///     </see>
        ///     method
        ///     and frees resources associated with this mt4 strategy object.
        /// </remarks>
        /// >
        internal override void Terminate(bool immediately)
        {
            lock (_synch)
            {
                if (_strategyRunner == null)
                {
                    if (Logger.IsInfoEnabled)
                    {
                        Logger.Info("Close method called: " + _id + ", skip - _strategyRunner == null");
                    }
                }
                else
                {
                    if (Logger.IsInfoEnabled)
                    {
                        Logger.Info("Close method called: " + _id);
                    }
                }
                //
                IsReconnect = false;
                //
                CloseTerminalGracefully(immediately);
                //
                if (_strategyRunner != null)
                {
                    _strategyRunner.Close();
                    _strategyRunner = null;
                }
                if (OrdersProcessingChannel != null) {
                    OrdersProcessingChannel.Close();
                    OrdersProcessingChannel = null;
                }
                //
                StopCharts();
                //
                TerminalClient cli = null;
                try
                {
                    if (_termServerHost != null && _termServerPort != 0)
                    {
                        cli = new TerminalClient(_id, _termServerHost, _termServerPort);
                        var bindHost = GetBindHost();
                        var cmd = (immediately ? "KILLTERM:" : "STOPTERM:")
                                  + _mt4Server.val + '|' + _mt4User +
                                  "||" + _id + '|' + bindHost
                                  + '|' + NJ4XServer.Instance.BindPort + ':' + GetTenant()
                                  + '|' + (Symbol ?? "");
                        cli.Ask(cmd);
                        lock (_terminals)
                        {
                            foreach (var t in _terminals)
                            {
                                var mt4Connection = (Strategy) t.GetMt4Connection();
                                if (mt4Connection != null)
                                {
                                    mt4Connection.IsReconnect = false;
                                    mt4Connection.CloseTerminalGracefully(immediately);
                                    if (mt4Connection._strategyRunner != null)
                                    {
                                        mt4Connection._strategyRunner.Close();
                                        mt4Connection._strategyRunner = null;
                                    }
                                    mt4Connection.StopCharts();
                                }
                                cmd = (immediately ? "KILLTERM:" : "STOPTERM:")
                                      + _mt4Server.val + '|' + _mt4User +
                                      "||" + _id + '|' + bindHost
                                      + '|' + NJ4XServer.Instance.BindPort + ':' +
                                      (mt4Connection == null ? "" : mt4Connection.GetTenant())
                                      + '|' + (mt4Connection == null ? "" : (mt4Connection.Symbol ?? ""));
                                cli.Ask(cmd);
                            }

                            _terminals.Clear();
                        }
                    }
                }
                finally
                {
                    if (cli != null)
                    {
                        cli.Close();
                    }
                    base.Terminate(immediately);
                }
            }
        }

        private string GetBindHost()
        {
            return _nj4xServerHost ??
                   (NJ4XServer.Instance.BindHost.Equals("*") ? "127.0.0.1" : NJ4XServer.Instance.BindHost);
        }

        private void CloseTerminalGracefully(bool immediately)
        {
            if (_strategyRunner != null)
            {
                ((BasicStrategyRunner)_strategyRunner).IsPendingClose = true;
            }
            if (OrdersProcessingChannel != null)
            {
                ((BasicStrategyRunner)OrdersProcessingChannel).IsPendingClose = true;
            }
            //
            if (_charts != null && _charts.Count > 0)
            {
                foreach (var c in _charts)
                {
                    if (c._strategyRunner != null)
                    {
                        ((BasicStrategyRunner) c.StrategyRunner).IsPendingClose = true;
                    }
                }
            }
//            if (!immediately)
//            {
//                try
//                {
//                    TerminalClose(0);
//                }
//                catch (Exception)
//                {
//                    // ignored
//                }
//            }
        }

        private void StopCharts()
        {
            if (_charts != null && _charts.Count > 0)
            {
                foreach (var c in _charts)
                {
                    c.IsReconnect = false;
                    if (c._strategyRunner != null)
                    {
                        c._strategyRunner.Close();
                        c._strategyRunner = null;
                    }
                }
            }
        }

        private bool IsTerminalServerOk()
        {
            return _termServerHost != null &&
                   _mt4Server != null &&
                   _mt4User != null &&
                   _mt4Password != null;
        }

        /// <summary>
        ///     Coordinate() method calls frequency in milliseconds. It is 1 second by default.
        /// </summary>
        /// <remarks>
        ///     Descendant classes can override it to instruct NJ4X framework for the custom coordination frequency period.
        /// </remarks>
        public virtual int CoordinationIntervalMillis()
        {
            return 10000;
        }

        /// <summary>
        ///     Descendant classes can implement their MQ4/EA-like trading logic here.
        /// </summary>
        /// <example>
        ///     <code><![CDATA[
        ///      namespace Tests
        ///      {
        ///          using System;
        ///          using nj4x.Metatrader;
        ///          public class MyStrategy : nj4x.Strategy
        ///          {
        ///              public MyStrategy()
        ///              {
        ///                  Connect("localhost", 7788, 
        ///                      new Broker("Skyeast-Demo Accounts Server"), "90385752", "7blx6tq");
        ///              }
        /// 
        ///              public override void Coordinate()
        ///              {
        ///                  Console.WriteLine(
        ///                      String.Format("Balance={0}, Bid={1}",
        ///                                    AccountBalance(),
        ///                                    Marketinfo("EURUSD", MarketInfo.MODE_BID)
        ///                          ));
        ///              }
        ///          }
        ///      }
        ///  ]]></code>
        /// </example>
        public virtual void Coordinate()
        {
        }

        protected override MT4 GetOrdersManipulationConnection()
        {
            if (_isOrderTerminalsPresent)
            {
                Terminal pop;
                while (true)
                {
                    lock (_orderTerminals)
                    {
                        if (_orderTerminals.Count > 0)
                        {
                            pop = _orderTerminals.Pop();
                            break;
                        }
                    }
                    Thread.SpinWait(1000);
                }
                return pop.GetMt4Connection();
            }
            return this;
        }

        protected override void ReturnOrdersManipulationConnection(MT4 conn)
        {
            if (conn != this)
            {
                _orderTerminals.Push(((TerminalStrategy) conn).MyTerminal);
            }
        }

        /// <summary>
        ///     Creates another terminal instance to get specified symbol's ticks in real-time.
        /// </summary>
        /// <param name="symbol">symbol to subscribe ticks listener for</param>
        /// <param name="lsnr">listener to process symbol's tick events</param>
        /// <returns></returns>
        public Terminal AddTerminal(String symbol, ITickListener lsnr)
        {
            var t = new Terminal(this, symbol, lsnr);
            return t;
        }

        /// <summary>
        ///     Creates another terminal instance and registers timer event handler for it.
        /// </summary>
        /// <param name="lsnr">timer listener to perform regular mt4 account jobs under.</param>
        /// <param name="timerIntervalMillis">timer interval in millis</param>
        /// <returns></returns>
        public Terminal AddTerminal(ITimerListener lsnr, int timerIntervalMillis)
        {
            var t = new Terminal(this, lsnr, timerIntervalMillis);
            return t;
        }

        /// <summary>
        ///     Creates another terminal instance integrated with this strategy object.
        /// </summary>
        /// <param name="type">
        ///     TerminalType.ORDERS_WORKER or TerminalType.FREE_WORKER or TerminalType.TICK_WORKER (this requires
        ///     subsequent Terminal.AddTickListener call.)
        /// </param>
        /// <returns></returns>
        public Terminal AddTerminal(TerminalType type)
        {
            var t = new Terminal(this, type);
            return t;
        }

        protected void AddChart(Chart c)
        {
            _charts = _charts ?? new List<Chart>();
            _charts.Add(c);
        }

        private List<Chart> GetCharts()
        {
            return _charts;
        }

        /// <summary>
        ///     Installs user defined PositionListener for the Strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="lsnr">user defined position listener</param>
        /// <returns>Strategy object itself</returns>
        /// <exception cref="Exception">PositionListener is already set exception for connected strategy object.</exception>
        public Strategy SetPositionListener(IPositionListener lsnr)
        {
            return SetPositionListener(lsnr, 50, 500);
        }

        /// <summary>
        ///     Installs user defined PositionListener for the Strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="lsnr">user defined position listener</param>
        /// <param name="minDelayMillis">minimum new orders check interval millis</param>
        /// <param name="maxDelayMillis">maximum new orders check interval millis</param>
        /// <returns>Strategy object itself</returns>
        /// <exception cref="Exception">PositionListener is already set exception for connected strategy object.</exception>
        public Strategy SetPositionListener(IPositionListener lsnr, int minDelayMillis, int maxDelayMillis)
        {
            if (_positionListener != null)
            {
                if (IsAlive)
                {
                    throw new Exception("PositionListener is already set");
                }
                _charts.Remove(_positionListener);
            }
            else
            {
                if (IsAlive)
                {
                    throw new Exception("PositionListener must be set before connection.");
                }
            }
            _positionListener = new PositionListenerChart(lsnr, minDelayMillis, maxDelayMillis);
            AddChart(_positionListener);
            return this;
        }

        /// <summary>
        ///     Installs user defined PositionListener for the Strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="initialPositionHandler">Informed about initial trader's position at connect.</param>
        /// <param name="changedPositionHandler">It is invoked on Changes in trader's position.</param>
        /// <returns></returns>
        public Strategy SetPositionListener(InitializedPositionHandler initialPositionHandler,
            ChangedPositionHandler changedPositionHandler)
        {
            return SetPositionListener(initialPositionHandler, changedPositionHandler, 50, 500);
        }

        /// <summary>
        ///     Installs user defined PositionListener for the Strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="initialPositionHandler">Informed about initial trader's position at connect.</param>
        /// <param name="changedPositionHandler">It is invoked on Changes in trader's position.</param>
        /// <param name="minDelayMillis">minimum new orders check interval millis</param>
        /// <param name="maxDelayMillis">maximum new orders check interval millis</param>
        /// <returns></returns>
        public Strategy SetPositionListener(InitializedPositionHandler initialPositionHandler,
            ChangedPositionHandler changedPositionHandler, int minDelayMillis,
            int maxDelayMillis)
        {
            if (_positionListener != null)
            {
                if (IsAlive)
                {
                    throw new Exception("PositionListener is already set");
                }
                _charts.Remove(_positionListener);
            }
            else
            {
                if (IsAlive)
                {
                    throw new Exception("PositionListener must be set before connection.");
                }
            }
            _positionListener = new PositionListenerChart(initialPositionHandler, changedPositionHandler, minDelayMillis,
                maxDelayMillis);
            AddChart(_positionListener);
            return this;
        }

        /// <summary>
        ///     Sets BulkTickListener for the strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="bulkTickListener">multiple symbols ticks' listener.</param>
        /// <param name="checkIntervalMillis">Interval in milliseconds to check for new ticks (1 millisecond by default)</param>
        /// <returns>this strategy object reference</returns>
        public Strategy SetBulkTickListener(IBulkTickListener bulkTickListener, int checkIntervalMillis = 1)
        {
            return SetBulkTickListener(bulkTickListener.OnTicks, checkIntervalMillis);
        }

        /// <summary>
        ///     Sets BulkTickListener for the strategy. Must be called before Connect(...) method.
        /// </summary>
        /// <param name="bulkTickHandler">multiple symbols ticks' handler.</param>
        /// <param name="checkIntervalMillis">Interval in milliseconds to check for new ticks (1 millisecond by default)</param>
        /// <returns>this strategy object reference</returns>
        public Strategy SetBulkTickListener(BulkTickHandler bulkTickHandler, int checkIntervalMillis = 1)
        {
            if (_bulkTickListener != null)
            {
                if (IsAlive)
                {
                    throw new Exception("BulkTickListener is already set");
                }
                _charts.Remove(_bulkTickListener);
            }
            else
            {
                if (IsAlive)
                {
                    throw new Exception("BulkTickListener must be set before connection.");
                }
            }
            _bulkTickListener = new TickListenerChart(SPCH + "_T_LSNR", bulkTickHandler, checkIntervalMillis);
            AddChart(_bulkTickListener);
            return this;
        }

        /// <summary>
        ///     Adds TickListener to the strategy. Must be used before connection.
        /// </summary>
        /// <param name="symbol">symbol to listen for the ticks of</param>
        /// <param name="tickHandler">ticks handler</param>
        /// <returns>this strategy object</returns>
        public Strategy AddTickListener(String symbol, TickHandler tickHandler)
        {
            return AddTickListener(symbol, new TickHandlerWrapper(tickHandler));
        }

        /// <summary>
        ///     Adds TickListener to the strategy. Must be used before connection.
        /// </summary>
        /// <param name="symbol">symbol to listen for the ticks of</param>
        /// <param name="tickListener">ticks listener</param>
        /// <returns>this strategy object</returns>
        public Strategy AddTickListener(String symbol, ITickListener tickListener)
        {
            lock (this)
            {
                if (_terminal == null)
                {
                    _terminal = new Terminal(this, TerminalType.TickWorker);
                }
                _terminal.AddTickListener(symbol, tickListener);
                return this;
            }
        }

        /// <summary>
        ///     Internal method designating TickListener strategy.
        /// </summary>
        /// <returns></returns>
        public virtual bool IsTickListenerStrategy()
        {
            return false;
        }

        #region Nested type: Chart

        protected internal abstract class Chart : Strategy
        {
            public String ChartId;

            protected Chart(String symbol)
            {
                ChartId = Symbol = symbol;
            }

            public virtual String GetChartParams()
            {
                return Symbol;
            }
        }

        #endregion

        #region Nested type: ITimerListener

        /// <summary>
        ///     Regular time interval events listener.
        /// </summary>
        public interface ITimerListener
        {
            /// <summary>
            ///     It is called on regular time intervals
            /// </summary>
            /// <param name="connection">MT4 connection to be used for timer logic implementation</param>
            void OnTimer(MT4 connection);
        }

        #endregion

        #region Nested type: Instrument

        /// <summary>
        ///     Represents Forex/MT4 financial instrument.
        /// </summary>
        public class Instrument
        {
            internal Instrument(string name, string symbol1, string symbol2)
            {
                Name = name;
                Symbol1 = symbol1;
                Symbol2 = symbol2;
            }

            /// <summary>
            ///     Full instrument ID.
            /// </summary>
            public String Name { get; private set; }

            /// <summary>
            ///     First currency in the pair
            /// </summary>
            public String Symbol1 { get; private set; }

            /// <summary>
            ///     Second currency in the pair
            /// </summary>
            public String Symbol2 { get; private set; }

            /// <summary>
            ///     Returns a <see cref="T:System.String" /> that represents the current <see cref="T:System.Object" />.
            /// </summary>
            /// <returns>
            ///     A <see cref="T:System.String" /> that represents the current <see cref="T:System.Object" />.
            /// </returns>
            /// <filterpriority>2</filterpriority>
            public override string ToString()
            {
                return "Instrument [name=" + Name + "] [sym_1=" + Symbol1 + "] [sym_2=" + Symbol2 + ']';
            }
        }

        #endregion

        #region Nested type: PositionListenerChart

        internal class PositionListenerChart : Chart
        {
            private readonly IPositionListener _lsnr;
            private readonly int _maxDelayMillis;
            private readonly int _minDelayMillis;
            private IPositionInfoEnabled _positionInfo;

            internal PositionListenerChart(InitializedPositionHandler initialPositionHandler,
                ChangedPositionHandler changedPositionHandler, int minDelayMillis,
                int maxDelayMillis)
                : base(SPCH + "_P_LSNR")
            {
                _minDelayMillis = minDelayMillis;
                _maxDelayMillis = maxDelayMillis;
                _lsnr = null;
                InitializedPositionHandler = initialPositionHandler;
                ChangedPositionHandler = changedPositionHandler;
            }

            internal PositionListenerChart(IPositionListener listener, int minDelayMillis, int maxDelayMillis)
                : base(SPCH + "_P_LSNR")
            {
                _lsnr = listener;
                _minDelayMillis = minDelayMillis;
                _maxDelayMillis = maxDelayMillis;
            }

            public InitializedPositionHandler InitializedPositionHandler { get; set; }
            public ChangedPositionHandler ChangedPositionHandler { get; set; }

            public override void Init()
            {
                _positionInfo = NewPosition(null) as IPositionInfoEnabled;
                if (InitializedPositionHandler != null)
                {
                    InitializedPositionHandler(_positionInfo);
                }
                if (_lsnr != null)
                {
                    _lsnr.OnInit(_positionInfo);
                }
            }

            public override void Coordinate()
            {
                while (IsAlive)
                {
                    var newPositionInfo = NewPosition(_positionInfo);
                    if (_lsnr != null)
                    {
                        _lsnr.OnChange(_positionInfo, _positionInfo.MergePosition(newPositionInfo));
                    }
                    if (ChangedPositionHandler != null)
                    {
                        ChangedPositionHandler(_positionInfo, _positionInfo.MergePosition(newPositionInfo));
                    }
                }
            }

            public override int CoordinationIntervalMillis()
            {
                return 1;
            }

            public override string GetChartParams()
            {
                return Symbol + ":" + _minDelayMillis + ":" + _maxDelayMillis;
            }
        }

        #endregion

        #region Nested type: Terminal

        /// <summary>
        ///     An additional Terminal using Strategy's credentials to connect to mt4 broker in parallel.
        /// </summary>
        public class Terminal
        {
            private readonly Strategy _s;
            private String _id;
            private bool _isInjected;
            private MT4 _mt4Connection;
            internal Hashtable TickListeners = new Hashtable();
            internal int TimerIntervalMillis;
            internal ITimerListener TimerListener;
            internal TerminalType Type;

            internal Terminal(Strategy parent, TerminalType type)
            {
                _s = parent;

//                if (type != TerminalType.FreeWorker && type != TerminalType.OrdersWorker)
//                {
//                    throw new Exception("Only FREE_WORKER or ORDERS_WORKER terminals can be created this way.");
//                }
                Type = type;
                if (type != TerminalType.TickWorker)
                {
                    CalcId();
                }
            }

            internal Terminal(Strategy parent, String symbol, ITickListener tickListener)
            {
                if (string.IsNullOrEmpty(symbol))
                {
                    throw new Exception("Tick worker's symbol is empty.");
                }
                if (tickListener == null)
                {
                    throw new Exception("Tick worker's listener must be provided.");
                }
                //
                _s = parent;
                Type = TerminalType.TickWorker;
                TickListeners = TickListeners ?? new Hashtable();
                TickListeners[symbol] = tickListener;
            }

            internal Terminal(Strategy parent, ITimerListener timerListener, int timerIntervalMillis)
            {
                _s = parent;
                Type = TerminalType.FreeWorker;
                TimerListener = timerListener;
                TimerIntervalMillis = timerIntervalMillis;
                if (timerIntervalMillis <= 0)
                {
                    throw new Exception("timerIntervalMillis must be greater than zero.");
                }
                if (timerListener == null)
                {
                    throw new Exception("Timer listener must be provided.");
                }
                CalcId();
            }

            /// <summary>
            ///     Add (or replace if already exist) symbol's tick listener to this terminal.
            /// </summary>
            /// <param name="symbol">Symbol to listen real-time ticks of</param>
            /// <param name="tickHandler">Ticks handler implementation</param>
            /// <exception cref="Exception">Terminal type must be TickWorker, symbol not null/empty and tick listener must be provided</exception>
            public Terminal AddTickListener(String symbol, TickHandler tickHandler)
            {
                return AddTickListener(symbol, new TickHandlerWrapper(tickHandler));
            }

            /// <summary>
            ///     Add (or replace if already exist) symbol's tick listener to this terminal.
            /// </summary>
            /// <param name="symbol">Symbol to listen real-time ticks of</param>
            /// <param name="tickListener">Ticks Listener implementation</param>
            /// <exception cref="Exception">Terminal type must be TickWorker, symbol not null/empty and tick listener must be provided</exception>
            public Terminal AddTickListener(String symbol, ITickListener tickListener)
            {
                if (Type != TerminalType.TickWorker)
                {
                    throw new Exception("Terminal must be of " + TerminalType.TickWorker + " type.");
                }
                TickListeners = TickListeners ?? new Hashtable();
                if (string.IsNullOrEmpty(symbol))
                {
                    throw new Exception("Tick worker's symbol is empty.");
                }
                if (tickListener == null)
                {
                    throw new Exception("Tick worker's listener must be provided.");
                }
                //
                TickListeners[symbol] = tickListener;
                //
                return this;
            }

            /// <summary>
            ///     Connect to the terminal
            /// </summary>
            public Terminal Connect()
            {
                if (_mt4Connection == null)
                {
                    _mt4Connection = new TerminalStrategy(_s, this);
                    lock (_s._terminals)
                    {
                        if (Type == TerminalType.OrdersWorker)
                        {
                            _s._orderTerminals.Push(this);
                            _s._isOrderTerminalsPresent = true;
                        }
                    }
                }
                return this;
            }

            /// <summary>
            ///     Used to kill mt4 terminal process (makes this Terminal unusable)
            /// </summary>
            /// <exception cref="IOException">in case of Terminal Server errors.</exception>
            public void Close()
            {
                if (Type == TerminalType.OrdersWorker)
                {
                    if (_s._orderTerminals.Contains(this))
                    {
                        var tmp = new List<Terminal>();
                        while (_s._orderTerminals.Count > 0)
                        {
                            var t = _s._orderTerminals.Pop();
                            if (t != this)
                            {
                                tmp.Add(t);
                            }
                            else
                            {
                                break;
                            }
                        }
                        tmp.Reverse();
                        foreach (var terminal in tmp)
                        {
                            _s._orderTerminals.Push(terminal);
                        }
                    }
                    else
                    {
                        throw new IOException("Terminal is currently used.");
                    }
                }
                lock (_s._terminals)
                {
                    _s._terminals.Remove(this);
                }
                _mt4Connection.Terminate(true);
            }

            /// <summary>
            ///     Terminal's type: FREE, ORDERS or TICK WORKER.
            /// </summary>
            /// <returns>returns type of the terminal.</returns>
            public TerminalType GetTerminalType()
            {
                return Type;
            }

            /// <summary>
            ///     MT4 connection associated with this terminal.
            /// </summary>
            /// <returns>MT4 connection to be used elsewhere.</returns>
            public MT4 GetMt4Connection()
            {
                return _mt4Connection;
            }

            /// <summary>
            ///     Returns terminal identifier composed of type and (optionally) ordinal number.
            /// </summary>
            /// <returns>terminal identifier</returns>
            public String GetId()
            {
                if (_id == null)
                {
                    CalcId();
                }
                return _id;
            }

            private void CalcId()
            {
                lock (_s._terminals)
                {
                    switch (Type)
                    {
                        case TerminalType.TickWorker:
                            if (TickListeners == null || TickListeners.Count == 0)
                            {
                                throw new Exception("TickListener must be added for this terminal type.");
                            }
                            var symbols =
                                TickListeners.Keys.Cast<object>().Cast<string>().OrderBy(s => s).ToList();
                            if (_id != null)
                            {
                                _s._terminals.Remove(this);
                            }
                            var sb = new StringBuilder();
                            foreach (var s in symbols)
                            {
                                sb.Append(' ').Append(s);
                            }
                            if (sb.Length < 64)
                            {
                                _id = "" + Type + sb;
                            }
                            else
                            {
                                _id = "" + Type + " " + symbols.Count + " symbols " + MD5(sb.ToString());
                            }
                            break;
                        default:
                            var cnt = _s._terminals.Sum(t => (t.Type == Type ? 1 : 0));
                            _id = (TimerListener == null ? "" + Type : "TIMER")
                                  + " " + String.Format("{0:D3}", cnt + 1);
                            break;
                    }
                    _s._terminals.Add(this);
                }
            }

            internal void InjectCharts()
            {
                if (TickListeners != null && !_isInjected)
                {
                    _isInjected = true;
                    foreach (DictionaryEntry tle in TickListeners)
                    {
                        _s.AddChart(new TickListenerChart((string) tle.Key, (ITickListener) tle.Value));
                    }
                }
            }
        }

        #endregion

        #region Nested type: TerminalStrategy

        protected internal class TerminalStrategy : Strategy
        {
            private readonly Strategy _strategyThis;
            internal readonly Terminal MyTerminal;

            internal TerminalStrategy(Strategy parent, Terminal t)
            {
                Period = parent.Period;
                //
                _strategyThis = parent;
                IsReconnect = true;
                MyTerminal = t;
                //
                if (t.TickListeners != null)
                {
                    foreach (DictionaryEntry tle in t.TickListeners)
                    {
                        AddChart(new TickListenerChart((string) tle.Key, (ITickListener) tle.Value));
                    }
                }
                //
                var mt4User = _strategyThis._mt4User;
                Connect(
                    _strategyThis._termServerHost,
                    _strategyThis._termServerPort,
                    _strategyThis._mt4Server,
                    mt4User.IndexOf('@') > 0 
                        ? mt4User.Substring(0, mt4User.IndexOf('@')) : mt4User,
                    _strategyThis._mt4Password
                    );
            }

            internal override bool IsReconnectAllowed()
            {
                Thread.Sleep(500);
                return _strategyThis.IsAlive && _strategyThis.IsConnectedToTerminal() || !_strategyThis.IsReconnect;
            }

            public Terminal GetTerminal()
            {
                return MyTerminal;
            }

            internal override void Init(string symbol, int period, BasicStrategyRunner strategyRunner)
            {
                IsAlive = true;
                Symbol = symbol;
                Period = (Timeframe) period;
                if (Cli != null)
                {
                    try
                    {
                        Cli.Close();
                    }
                    finally
                    {
                        Cli = null;
                    }
                }
            }

            public override string ToString()
            {
                return _strategyThis._mt4User
                       + "/" + _strategyThis._mt4Server
                       + "/" + MyTerminal.GetId()
                    ;
            }

            public override int CoordinationIntervalMillis()
            {
                return (MyTerminal.TimerIntervalMillis <= 0
                    ? 2000
                    : MyTerminal.TimerIntervalMillis);
            }

            public override void Coordinate()
            {
                if (MyTerminal.TimerListener != null)
                {
                    MyTerminal.TimerListener.OnTimer(this);
                }
            }

            protected override String GetTenant()
            {
                return MyTerminal.GetId() + "\u0002" + _strategyThis.GetTenant();
            }
        }

        #endregion

        #region Nested type: TickHandlerWrapper

        internal class TickHandlerWrapper : ITickListener
        {
            private readonly TickHandler _tickHandler;

            /// <summary>
            ///     Initializes a new instance of the TickHandlerListenerWrapper class.
            /// </summary>
            public TickHandlerWrapper(TickHandler tickHandler)
            {
                _tickHandler = tickHandler;
            }

            #region ITickListener Members

            public void OnTick(String symbol, TickInfo tick, MT4 connection)
            {
                _tickHandler.Invoke(symbol, tick, connection);
            }

            #endregion
        }

        #endregion

        #region Nested type: TickListenerChart

        internal class TickListenerChart : Chart
        {
            private readonly BulkTickHandler _bulkTickHandler;
            private readonly ITickListener _tickListener;
            private TickInfo _lastTick;
            private int _interval;

            internal TickListenerChart(string symbol, ITickListener listener, int interval = 1)
                : base(symbol)
            {
                _tickListener = listener;
                _interval = interval;
                _bulkTickHandler = null;
                _lastTick = new TickInfo();
            }

            internal TickListenerChart(string symbol, BulkTickHandler bHdlr, int interval = 1)
                : base(symbol)
            {
                _tickListener = null;
                _bulkTickHandler = bHdlr;
                _interval = interval;
                _lastTick = new TickInfo();
            }

            public override void Coordinate()
            {
                try
                {
                    if (_tickListener == null)
                    {
                        _bulkTickHandler.Invoke(GetTicks(), this);
                    }
                    else
                    {
                        var tick = NewTick(Symbol, _lastTick);
                        _lastTick = tick;
                        Chart c = this;
                        _tickListener.OnTick(Symbol, tick, c);
                    }
                }
                catch (ErrUnknownSymbol errUnknownSymbol)
                {
                    Logger.Error("", errUnknownSymbol);
                }
            }

            public override int CoordinationIntervalMillis()
            {
                return _interval;
            }

            public override bool IsTickListenerStrategy()
            {
                return true;
            }
        }

        #endregion

        #region Nested type: ITickListener

        /// <summary>
        ///     Tick events handler interface.
        /// </summary>
        public interface ITickListener
        {
            /// <summary>
            ///     Handle new tick info.
            /// </summary>
            /// <param name="symbol">A symbol the tick belongs to</param>
            /// <param name="tick">received tick information</param>
            /// <param name="connection">MT4 connection to be used for tick processing</param>
            void OnTick(String symbol, TickInfo tick, MT4 connection);
        }

        /// <summary>
        ///     Multiple symbols tick events handler interface.
        /// </summary>
        public interface IBulkTickListener
        {
            /// <summary>
            ///     Handle new tick info.
            /// </summary>
            /// <param name="ticks">received ticks information</param>
            /// <param name="connection">MT4 connection to be used for tick processing</param>
            void OnTicks(List<Tick> ticks, MT4 connection);
        }

        /// <summary>
        ///     Multiple symbols tick events handler.
        /// </summary>
        /// <param name="ticks">received ticks information</param>
        /// <param name="connection">MT4 connection to be used for tick processing</param>
        public delegate void BulkTickHandler(List<Tick> ticks, MT4 connection);

        #endregion
    }

    /// <summary>
    ///     It can be thrown by Strategy.Connect when Terminal Server can not establish socket connection to the mt4 broker.
    /// </summary>
    public class NJ4XNoConnectionToServerException : MT4Exception
    {
        /// <summary>
        ///     Exception constructor
        /// </summary>
        /// <param name="s">exception detailed message</param>
        public NJ4XNoConnectionToServerException(string s)
            : base(2, s)
        {
        }
    }

    /// <summary>
    ///     It can be thrown by Strategy.Connect when Terminal Server can not create mt4 terminal process.
    /// </summary>
    public class NJ4XMaxNumberOfTerminalsExceededException : Exception
    {
        /// <summary>
        ///     Exception constructor
        /// </summary>
        /// <param name="s">exception detailed message</param>
        public NJ4XMaxNumberOfTerminalsExceededException(string s)
            : base(s)
        {
        }
    }

    /// <summary>
    ///     It can be thrown by Strategy.connect when mt4 terminal can not establish socket connection to the mt4 broker or
    ///     connection times out.
    /// </summary>
    public class NJ4XInvalidUserNameOrPasswordException : MT4Exception
    {
        /// <summary>
        ///     Exception constructor
        /// </summary>
        /// <param name="s">exception detailed message</param>
        public NJ4XInvalidUserNameOrPasswordException(string s)
            : base(2, s)
        {
        }
    }
}