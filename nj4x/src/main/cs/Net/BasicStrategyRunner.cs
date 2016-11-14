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
using System.Globalization;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using NLog;

namespace nj4x.Net
{
    internal class BasicStrategyRunner : IStrategyRunner
    {
        private static readonly Logger Logger = LogManager.GetCurrentClassLogger();

        internal static readonly StringBuilder IDLE = new StringBuilder("-1 ");
        protected static long CallsCount;
        protected readonly Object CoordinationSynch = new Object();

        protected internal String ClientId;
        public string DllVersion, MQLVersion;
        protected internal Strategy RunningStrategy;
        private String _command;
        private Timer _coordinationTimer;
        private bool _isRunning;
        private bool _isTesting;
        private bool _isTestingEverTested;
        internal bool IsPendingClose = false;
        private int _period;
        private String _result;
        private String _symbol;
        private int delay;
        private int executionFrequencyPeriod;
        public string ClientName { get; private set; }

        #region IStrategyRunner Members

        public bool Start(string clientName, bool isLimited, Greeter greeter)
        {
            lock (this)
            {
                SetFullClientName(clientName);
                String id = ClientId;
                //
                RunningStrategy = null;
                RunningStrategy = Strategy.GetStrategy(id);

                if (RunningStrategy == null)
                {
                    if (Regex.IsMatch(id, "\\d+_\\d+"))
                    {
                        throw new Exception("No such strategy waiting for connection: id=" + id);
                    }
                    else if (id.Length == 33 && Regex.IsMatch(id, "[\\dABCDEF]+") ||
                             id.Length > 33 && id.IndexOf('|') == 32 &&
                             Regex.IsMatch(id.Substring(0, 32), "[\\dABCDEF]+"))
                    {
                        return false;
                    }
                    if (Logger.IsDebugEnabled)
                    {
                        Logger.Debug(String.Format("ID {0} is accepted as manual strategy name", id));
                    }
                    //
                    int paramsDelimiter = id.IndexOf(':');
                    String className;
                    var args = new ArrayList();
                    if (paramsDelimiter < 0)
                    {
                        className = id;
                    }
                    else
                    {
                        className = id.Substring(0, paramsDelimiter);
                        string[] _params = id.Substring(paramsDelimiter + 1).Split(',');
                        foreach (string p in _params)
                        {
                            args.Add(double.Parse(p, CultureInfo.InvariantCulture));
                        }
                    }
                    //
                    Type cStrategyOrFactory = Type.GetType(className);
                    if (cStrategyOrFactory == null)
                    {
                        throw new Exception(String.Format(
                            "Type '{0}' does not exist, please check class and assembly name", className));
                    }
                    if (!typeof (Strategy).IsAssignableFrom(cStrategyOrFactory))
                    {
                        throw new Exception(String.Format(
                            "Type '{0}' does not extend njfx.Strategy.Strategy class", className));
                    }
                    var okConstructors = new ArrayList();
                    ConstructorInfo[] constructors = cStrategyOrFactory.GetConstructors();
                    foreach (ConstructorInfo c in constructors)
                    {
                        ParameterInfo[] parameters = c.GetParameters();
                        // ReSharper disable OperatorIsCanBeUsed
                        bool ok = parameters.All(parameterInfo => parameterInfo.ParameterType == typeof (Double));
                        // ReSharper restore OperatorIsCanBeUsed
                        if (ok) okConstructors.Add(c);
                    }

                    okConstructors.Sort(new ConstructorComparer());
                    var ci = (ConstructorInfo) okConstructors[0];
                    RunningStrategy = (Strategy) Activator.CreateInstance(
                        cStrategyOrFactory, args.GetRange(0, ci.GetParameters().Length).ToArray()
                                              );
                    //
                    if (RunningStrategy != null)
                    {
                        RunningStrategy.StrategyRunner = this;
                        RunningStrategy.IsLimitedFunctionality = isLimited;
                        greeter.SendToClient("START");
                        PreInit();
                        StartCoordination();
                    }
                    else
                    {
                        throw new Exception("Can not initialize strategy: " + id);
                    }
                }
                else
                {
                    RunningStrategy.IsLimitedFunctionality = isLimited;
                    if (id.EndsWith("1") ||
                        id.Length > 33 && id.IndexOf('|') == 32 && Regex.IsMatch(id.Substring(0, 32), "[\\dABCDEF]+"))
                    {
                        RunningStrategy.StrategyRunner = this;
                    }
                    else
                    {
                        RunningStrategy.OrdersProcessingChannel = this;
                    }
                    lock (RunningStrategy)
                    {
                        Strategy.RemoveStrategy(id);
                        greeter.SendToClient("START");
                        Monitor.Pulse(RunningStrategy);
                    }
                }
            }
            //
            return true;
        }

        private bool _isAlive;
        public bool IsAlive
        {
            get { return _isAlive; }
            private set
            {
//                if (Logger.IsDebugEnabled)
//                {
//                    Logger.Debug("BSR.IsAlive: set to " + value + " " + System.Environment.StackTrace);
//                }
                _isAlive = value;
            }
        }

        public virtual bool IsConnected
        {
            get
            {
                if (Logger.IsDebugEnabled)
                {
                    Logger.Debug("BSR.IsConnected: IsAlive=" + IsAlive + " " + ClientName);
                }
                return IsAlive;
            }
        }

        public virtual void Close()
        {
            IsAlive = false;
            try
            {
                if (RunningStrategy != null && RunningStrategy.OrdersProcessingChannel != this)
                {
                    RunningStrategy.Deinit();
                    RunningStrategy = null;
                }
            }
            catch (Exception e)
            {
                Logger.Error("Initialization error (" + Thread.CurrentThread.Name + ")", e);
            }
        }

        public virtual string SendCommandGetResult(StringBuilder command)
        {
            lock (this)
            {
                if (_command != null)
                {
                    throw new Exception("Active command processing error.");
                }
                //
                _command = command.ToString();
                _result = null;
                //
                Monitor.Pulse(this);
                Monitor.Wait(this);
                //
                if (_result == null)
                {
                    throw new Exception("No result recieved for command: " + _command);
                }
                return _result;
            }
        }

        #endregion

        protected bool IsRunning()
        {
            lock (CoordinationSynch)
            {
                if (_isRunning && RunningStrategy != null)
                {
                    return true;
                }
                else
                {
                    try
                    {
                        return false;
                    }
                    finally
                    {
                        _isRunning = RunningStrategy != null;
                    }
                }
                //return _isRunning;
            }
        }

        protected bool IsOrdersProcessingChannel()
        {
            return Strategy.GetStrategy(ClientId) != null && !ClientId.EndsWith("1");
        }

        public void PreInit()
        {
            lock (RunningStrategy)
            {
                Monitor.Pulse(RunningStrategy);
            }
            //
            try
            {
                RunningStrategy.Init(_symbol, _period, this);
            }
            catch (Exception e)
            {
                Logger.Error("Initialization error (" + Thread.CurrentThread.Name + ")", e);
                IsAlive = false;
            }
        }

        protected void SetFullClientName(String clientName)
        {
            int u1Ix = clientName.LastIndexOf('\u0001');
            if (u1Ix > 0)
            {
                DllVersion = clientName.Substring(u1Ix + 1);
                clientName = clientName.Substring(0, u1Ix);
            }
            else
            {
                DllVersion = "<1.7.2";
            }
            ClientName = clientName;
            IsAlive = true;
            //
            // GBPUSD..60.strategy
            //
            int startIndex = 0;
            int dotIx = clientName.IndexOf('.');
            while (clientName[dotIx + 1] == '.') dotIx++;
            while (true)
            {
                try
                {
                    _symbol = clientName.Substring(0, dotIx);
                    startIndex = _symbol.Length + 1;
                    _period = int.Parse(clientName.Substring(
                        startIndex, clientName.IndexOf('.', startIndex) - startIndex
                                            ));
                    break;
                }
                catch (FormatException)
                {
                    // GBPUSD.arm.60.strategy
                    dotIx = clientName.IndexOf('.', dotIx + 1);
                }
            }
            //
            ClientId = clientName.Substring(clientName.IndexOf('.', startIndex) + 1);
            int atIx = ClientId.IndexOf('@');
            if (atIx >= 0)
            {
                int mqlvIx = ClientId.IndexOf(" MQLv", StringComparison.InvariantCulture);
                if (mqlvIx > 0)
                {
                    MQLVersion = ClientId.Substring(mqlvIx + 5, atIx - mqlvIx - 5);
                }
                else
                {
                    MQLVersion = "<1.7.2";
                }
                ClientId = ClientId.Substring(atIx + 1);
            }
        }

        public void StartCoordination()
        {
            delay = Math.Max(1, Math.Min(RunningStrategy.CoordinationIntervalMillis(), 1000));
            executionFrequencyPeriod = RunningStrategy.CoordinationIntervalMillis();
            _coordinationTimer = new Timer(
                delegate
                    {
                        if (!IsAlive)
                        {
                            _coordinationTimer.Dispose();
                        }
                        else
                        {
                            Run();
                        }
                    },
                null,
                delay,
                executionFrequencyPeriod
                );
        }

        public void Run()
        {
            if (IsRunning() || RunningStrategy == null)
            {
                return;
            }
            //
            try
            {
                do
                {
                    CallsCount++;
                    if (CallsCount%5000 == 0)
                    {
                        Logger.Debug("" + CallsCount + " calls have been processed");
                    }
                    //
                    try
                    {
                        if (!_isTestingEverTested && RunningStrategy != null)
                        {
                            _isTesting = RunningStrategy.IsTesting();
                            _isTestingEverTested = true;
                        }
                        if (RunningStrategy != null && RunningStrategy.IsConnectedToTerminal())
                        {
                            RunningStrategy.Coordinate();
                        }
                    }
                    catch (Exception e)
                    {
                        if (RunningStrategy != null && RunningStrategy.IsAlive)
                        {
                            Console.WriteLine(Thread.CurrentThread.Name + "> Uncought coordination exception: " + e);
                            Logger.Error("Coordination error (" + Thread.CurrentThread.Name + ")", e);
                        }
                    }
                    //
                    try
                    {
                        SendCommandGetResult(IDLE);
                        if (RunningStrategy != null && RunningStrategy.OrdersProcessingChannel != null)
                        {
//                            Strategy.OrdersProcessingChannel.SendCommandGetResult(IDLE);
                            RunningStrategy.OrdersProcessingChannel.SendCommandGetResult(new StringBuilder("134 ")); // Symbol
                        }
                    }
                    catch (Exception)
                    {
                        Close();
                        break;
                    }
                } while (_isTesting);
            }
            finally
            {
                lock (CoordinationSynch)
                {
                    _isRunning = false;
                }
            }
        }

        #region Nested type: ConstructorComparer

        private class ConstructorComparer : IComparer
        {
            #region IComparer Members

            public int Compare(object x, object y)
            {
                var c1 = (ConstructorInfo) x;
                var c2 = (ConstructorInfo) y;
                return c2.GetParameters().Length - c1.GetParameters().Length;
            }

            #endregion
        }

        #endregion
    }
}