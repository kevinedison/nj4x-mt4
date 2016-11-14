using System;
using System.Collections;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Globalization;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using nj4x.Net;

namespace nj4x.Metatrader
{
    /// <summary>
    ///     Collection of MQ4-reflected methods, base class for nj4x.Strategy
    /// </summary>
// ReSharper disable InconsistentNaming
    [SuppressMessage("ReSharper", "InconsistentNaming")]
    public abstract class MT4AsyncBase : MT4
// ReSharper restore InconsistentNaming
    {
        private int _gmtOffset;
        private DateTime _lastCacheTime = DateTime.MinValue;

        internal MT4AsyncBase()
        {
        }

        /// <summary>
        ///     Defines asyncronous command interface.
        /// </summary>
        internal abstract IAsyncStrategyRunner AsyncStrategyRunner
        {
            get; 
        }


        /// <summary>
        ///     Cancels reservation for mt4 connection used for orders manipulation.
        /// </summary>
        /// <returns></returns>
        protected virtual void ReturnOrdersManipulationConnection(MT4AsyncBase conn)
        {
        }

        /// <summary>
        ///     Returns collection of all market information variables for specified symbol.
        /// </summary>
        /// <param name='symbol'>a symbol to get market info about</param>
        /// <example>
        ///     <code><![CDATA[
        /// var mt4 = new nj4x.Strategy();
        /// mt4.Connect(...);
        /// MarketInformation marketInformation = mt4.Marketinfo("EURUSD");
        /// double bid = marketInformation.BID;
        /// DateTime time = marketInformation.TIME;
        /// double spread = marketInformation.SPREAD;
        /// ]]>
        /// </code>
        /// </example>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        public async Task<MarketInformation> MarketinfoAsync(
            string symbol, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("10001 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Marketinfo(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", type=").Append("ALL");
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var mi = result.Split('|');
            var p = 0;
            var res = new MarketInformation(
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                ToDate(double.Parse(mi[p++], CultureInfo.InvariantCulture)),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p], CultureInfo.InvariantCulture)
                );
            //
            return res;
        }

        /// <summary>
        ///     The system calls RefreshRates() automatically before each MT4 method, - you can change this behaviour by
        ///     setting autoRefresh to false.
        /// </summary>
        /// <param name='autoRefreshRates'>true - enable auto refresh, false - disable auto refresh.</param>
        public async Task SetAutoRefreshRatesAsync(
            bool autoRefreshRates, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("10000 ");
            //
            command.Append(ArgStartC).Append(autoRefreshRates ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SetAutoRefreshRates(");
                signature.Append("autoRefreshRates=").Append(autoRefreshRates);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Returns the number of bars on the specified chart</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <remarks>For the current chart, the information about the amount of bars is in the predefined variable named Bars.</remarks>
        /// <returns>The number of bars on the specified chart</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<int> iBarsAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("0 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBars(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Search for bar by open time</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='time'>value to find (bar's open time).</param>
        /// <param name='exact'>Return mode when bar not found. false - iBarShift returns nearest. true - iBarShift returns -1.</param>
        /// <remarks>
        ///     The function returns bar shift with the open time specified. If the bar having the specified open time is
        ///     missing, the function will return -1 or the nearest bar shift depending on the exact.
        /// </remarks>
        /// <exception cref="ErrIncorrectSeriesarrayUsing">Incorrect series array using..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
// ReSharper disable InconsistentNaming
        public async Task<int> iBarShiftAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            DateTime time,
            bool exact, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("1 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(time.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(exact ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBarShift(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", time=").Append(time.ToString(YyyyMmDdHhMmSs));
                signature.Append(", exact=").Append(exact ? 1 : 0);
                signature.Append(')');
                switch (error)
                {
                    case 4054:
                        throw new ErrIncorrectSeriesarrayUsing(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns Close value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0.For the current chart, the information about close
        ///     prices is in the predefined array named Close[].
        /// </remarks>
        /// <returns>Close value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iCloseAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("2 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iClose(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns High value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0. For the current chart, the information about high
        ///     prices is in the predefined array named High[].
        /// </remarks>
        /// <returns>High value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iHighAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("3 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iHigh(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns Low value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0. For the current chart, the information about low
        ///     prices is in the predefined array named Low[].
        /// </remarks>
        /// <returns>Low value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iLowAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("4 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iLow(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns Open value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0. For the current chart, the information about open
        ///     prices is in the predefined array named Open[].
        /// </remarks>
        /// <returns>Open value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iOpenAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("5 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iOpen(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns Tick Volume value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0. For the current chart, the information about bars
        ///     tick volumes is in the predefined array named Volume[].
        /// </remarks>
        /// <returns>Tick Volume value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iVolumeAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("6 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iVolume(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns Time value for the bar of indicated symbol with timeframe and shift</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     If local history is empty (not loaded), function returns 0. For the current chart, the information about bars
        ///     open times is in the predefined array named Time[].
        /// </remarks>
        /// <returns>Time value for the bar of indicated symbol with timeframe and shift</returns>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<DateTime> iTimeAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("7 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iTime(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>Returns the shift of the least value over a specific number of periods depending on type</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='type'>Series array identifier. It can be any of Series array identifier enumeration values.</param>
        /// <param name='count'>
        ///     Number of periods (in direction from the start bar to the back one) on which the calculation is
        ///     carried out.
        /// </param>
        /// <param name='start'>Shift showing the bar, relative to the current bar, that the data should be taken from.</param>
        /// <returns>The shift of the least value over a specific number of periods depending on type</returns>
        /// <exception cref="ErrIncorrectSeriesarrayUsing">Incorrect series array using..</exception>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<int> iLowestAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            Series type,
            int count,
            int start, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("8 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            command.Append(ArgStartC).Append(count).Append(ArgEndC);
            command.Append(ArgStartC).Append(start).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iLowest(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", type=").Append((int) type);
                signature.Append(", count=").Append(count);
                signature.Append(", start=").Append(start);
                signature.Append(')');
                switch (error)
                {
                    case 4054:
                        throw new ErrIncorrectSeriesarrayUsing(signature.ToString());
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the shift of the maximum value over a specific number of periods depending on type</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='type'>Series array identifier. It can be any of Series array identifier enumeration values.</param>
        /// <param name='count'>
        ///     Number of periods (in direction from the start bar to the back one) on which the calculation is
        ///     carried out.
        /// </param>
        /// <param name='start'>Shift showing the bar, relative to the current bar, that the data should be taken from.</param>
        /// <returns>The shift of the maximum value over a specific number of periods depending on type</returns>
        /// <exception cref="ErrIncorrectSeriesarrayUsing">Incorrect series array using..</exception>
        /// <exception cref="ErrHistoryWillUpdated">Requested history data in updating state..</exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<int> iHighestAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            Series type,
            int count,
            int start, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("9 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            command.Append(ArgStartC).Append(count).Append(ArgEndC);
            command.Append(ArgStartC).Append(start).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iHighest(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", type=").Append((int) type);
                signature.Append(", count=").Append(count);
                signature.Append(", start=").Append(start);
                signature.Append(')');
                switch (error)
                {
                    case 4054:
                        throw new ErrIncorrectSeriesarrayUsing(signature.ToString());
                    case 4066:
                        throw new ErrHistoryWillUpdated(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns balance value of the current account (the amount of money on the account)</summary>
        /// <returns>Balance value of the current account (the amount of money on the account)</returns>
        public async Task<double> AccountBalanceAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("10 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountBalance(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns credit value of the current account</summary>
        /// <returns>Credit value of the current account</returns>
        public async Task<double> AccountCreditAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("11 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountCredit(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns the brokerage company name where the current account was registered</summary>
        /// <returns>The brokerage company name where the current account was registered</returns>
        public async Task<string> AccountCompanyAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("12 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountCompany(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns currency name of the current account</summary>
        /// <returns>Currency name of the current account</returns>
        public async Task<string> AccountCurrencyAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("13 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountCurrency(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns equity value of the current account</summary>
        /// <remarks>Equity calculation depends on trading server settings.</remarks>
        /// <returns>Equity value of the current account</returns>
        public async Task<double> AccountEquityAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("14 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountEquity(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns free margin value of the current account</summary>
        /// <returns>Free margin value of the current account</returns>
        public async Task<double> AccountFreeMarginAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("15 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountFreeMargin(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns margin value of the current account</summary>
        /// <returns>Margin value of the current account</returns>
        public async Task<double> AccountMarginAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("16 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountMargin(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns the current account name</summary>
        /// <returns>The current account name</returns>
        public async Task<string> AccountNameAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("17 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountName(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns the number of the current account</summary>
        /// <returns>The number of the current account</returns>
        public async Task<int> AccountNumberAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("18 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountNumber(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns profit value of the current account</summary>
        /// <returns>Profit value of the current account</returns>
        public async Task<double> AccountProfitAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("19 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountProfit(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>
        ///     The function returns the last occurred error, then the value of special last_error variable where the last
        ///     error code is stored will be zeroized
        /// </summary>
        /// <remarks>So, the next call for GetLastError() will return 0. </remarks>
        public async Task<int> GetLastErrorAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("20 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GetLastError(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     The function returns the status of the main connection between client terminal and server that performs data
        ///     pumping
        /// </summary>
        /// <remarks>It returns TRUE if connection to the server was successfully established, otherwise, it returns FALSE.</remarks>
        public async Task<bool> IsConnectedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("21 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsConnected(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if the expert runs on a demo account, otherwise returns FALSE</summary>
        /// <returns>TRUE if the expert runs on a demo account, otherwise returns FALSE</returns>
        public async Task<bool> IsDemoAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("22 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsDemo(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if expert runs in the testing mode, otherwise returns FALSE</summary>
        /// <returns>TRUE if expert runs in the testing mode, otherwise returns FALSE</returns>
        public async Task<bool> IsTestingAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("23 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsTesting(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            //
            return res;
        }

        /// <summary>Returns TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE</summary>
        /// <returns>TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE</returns>
        public async Task<bool> IsVisualModeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("24 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsVisualMode(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     The GetTickCount() function retrieves the number of milliseconds that have elapsed since the system was
        ///     started
        /// </summary>
        /// <remarks>It is limited to the resolution of the system timer. </remarks>
        public async Task<uint> GetTickCountAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("25 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GetTickCount(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = uint.Parse(result);
            return res;
        }

        /// <summary>The function outputs the comment defined by the user in the left top corner of the chart</summary>
        /// <param name='comments'>User defined comment.</param>
        public async Task CommentAsync(
            string comments, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("26 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(comments)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Comment(");
                signature.Append("comments=").Append(comments);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <param name='symbol'></param>
        /// <param name='type'></param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        public async Task<double> MarketinfoAsync(
            string symbol,
            MarketInfo type, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("27 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Marketinfo(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", type=").Append((int) type);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Prints a message to the experts log</summary>
        /// <param name='comments'>User defined message.</param>
        public async Task PrintAsync(
            string comments, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("28 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(comments)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Print(");
                signature.Append("comments=").Append(comments);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Returns the current day of the month, i.e., the day of month of the last known server time</summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>The current day of the month, i.e., the day of month of the last known server time</returns>
        public async Task<int> DayAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("29 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Day(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time</summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>The current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time</returns>
        public async Task<int> DayOfWeekAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("30 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("DayOfWeek(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of
        ///     the last known server time
        /// </summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>
        ///     The current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of the last
        ///     known server time
        /// </returns>
        public async Task<int> DayOfYearAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("31 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("DayOfYear(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the hour (0,1,2,..23) of the last known server time by the moment of the program start (this value
        ///     will not change within the time of the program execution)
        /// </summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>
        ///     The hour (0,1,2,..23) of the last known server time by the moment of the program start (this value will not
        ///     change within the time of the program execution)
        /// </returns>
        public async Task<int> HourAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("32 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Hour(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the current minute (0,1,2,..59) of the last known server time by the moment of the program start (this
        ///     value will not change within the time of the program execution)
        /// </summary>
        /// <returns>
        ///     The current minute (0,1,2,..59) of the last known server time by the moment of the program start (this value
        ///     will not change within the time of the program execution)
        /// </returns>
        public async Task<int> MinuteAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("33 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Minute(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the
        ///     last known server time
        /// </summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>
        ///     The current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the last known
        ///     server time
        /// </returns>
        public async Task<int> MonthAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("34 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Month(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the amount of seconds elapsed from the beginning of the current minute of the last known server time
        ///     by the moment of the program start (this value will not change within the time of the program execution)
        /// </summary>
        /// <returns>
        ///     The amount of seconds elapsed from the beginning of the current minute of the last known server time by the
        ///     moment of the program start (this value will not change within the time of the program execution)
        /// </returns>
        public async Task<int> SecondsAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("35 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Seconds(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the last known server time (time of incoming of the latest quote) as number of seconds elapsed from
        ///     00:00 January 1, 1970
        /// </summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>
        ///     The last known server time (time of incoming of the latest quote) as number of seconds elapsed from 00:00
        ///     January 1, 1970
        /// </returns>
        public async Task<DateTime> TimeCurrentAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("36 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("TimeCurrent(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>Returns the current year, i.e., the year of the last known server time</summary>
        /// <remarks>Note: At the testing, the last known server time is modelled. </remarks>
        /// <returns>The current year, i.e., the year of the last known server time</returns>
        public async Task<int> YearAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("37 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Year(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Creation of an object with the specified name, type and initial coordinates in the specified window</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='type'>Object type. It can be any of the Object type enumeration values.</param>
        /// <param name='window'>
        ///     Index of the window where the object will be added. Window index must exceed or equal to 0 and be
        ///     less than WindowsTotal().
        /// </param>
        /// <param name='time1'>Time part of the first point.</param>
        /// <param name='price1'>Price part of the first point.</param>
        /// <param name='time2'>Time part of the second point.</param>
        /// <param name='price2'>Price part of the second point.</param>
        /// <param name='time3'>Time part of the third point.</param>
        /// <param name='price3'>Price part of the third point.</param>
        /// <remarks>
        ///     Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function
        ///     succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one
        ///     has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of
        ///     ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if
        ///     there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always
        ///     exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object
        ///     needs only time, but price (any value) must be passed, as well.
        /// </remarks>
        public async Task<bool> ObjectCreateAsync(
            string name,
            ObjectType type,
            int window,
            DateTime time1,
            double price1,
            DateTime time2,
            double price2,
            DateTime time3,
            double price3,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("38 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            command.Append(ArgStartC).Append(window).Append(ArgEndC);
            command.Append(ArgStartC).Append(time1.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price1.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(time2.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price2.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(time3.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price3.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectCreate(");
                signature.Append("name=").Append(name);
                signature.Append(", type=").Append((int) type);
                signature.Append(", window=").Append(window);
                signature.Append(", time1=").Append(time1.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price1=").Append(price1.ToString(CultureInfo.InvariantCulture));
                signature.Append(", time2=").Append(time2.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price2=").Append(price2.ToString(CultureInfo.InvariantCulture));
                signature.Append(", time3=").Append(time3.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price3=").Append(price3.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Creation of an object with the specified name, type and initial coordinates in the specified window</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='type'>Object type. It can be any of the Object type enumeration values.</param>
        /// <param name='window'>
        ///     Index of the window where the object will be added. Window index must exceed or equal to 0 and be
        ///     less than WindowsTotal().
        /// </param>
        /// <param name='time1'>Time part of the first point.</param>
        /// <param name='price1'>Price part of the first point.</param>
        /// <remarks>
        ///     Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function
        ///     succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one
        ///     has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of
        ///     ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if
        ///     there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always
        ///     exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object
        ///     needs only time, but price (any value) must be passed, as well.
        /// </remarks>
        public async Task<bool> ObjectCreateAsync(
            string name,
            ObjectType type,
            int window,
            DateTime time1,
            double price1,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("39 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            command.Append(ArgStartC).Append(window).Append(ArgEndC);
            command.Append(ArgStartC).Append(time1.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price1.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectCreate(");
                signature.Append("name=").Append(name);
                signature.Append(", type=").Append((int) type);
                signature.Append(", window=").Append(window);
                signature.Append(", time1=").Append(time1.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price1=").Append(price1.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Creation of an object with the specified name, type and initial coordinates in the specified window</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='type'>Object type. It can be any of the Object type enumeration values.</param>
        /// <param name='window'>
        ///     Index of the window where the object will be added. Window index must exceed or equal to 0 and be
        ///     less than WindowsTotal().
        /// </param>
        /// <param name='time1'>Time part of the first point.</param>
        /// <param name='price1'>Price part of the first point.</param>
        /// <param name='time2'>Time part of the second point.</param>
        /// <param name='price2'>Price part of the second point.</param>
        /// <remarks>
        ///     Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function
        ///     succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one
        ///     has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of
        ///     ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if
        ///     there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always
        ///     exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object
        ///     needs only time, but price (any value) must be passed, as well.
        /// </remarks>
        public async Task<bool> ObjectCreateAsync(
            string name,
            ObjectType type,
            int window,
            DateTime time1,
            double price1,
            DateTime time2,
            double price2,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("40 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            command.Append(ArgStartC).Append(window).Append(ArgEndC);
            command.Append(ArgStartC).Append(time1.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price1.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(time2.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price2.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectCreate(");
                signature.Append("name=").Append(name);
                signature.Append(", type=").Append((int) type);
                signature.Append(", window=").Append(window);
                signature.Append(", time1=").Append(time1.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price1=").Append(price1.ToString(CultureInfo.InvariantCulture));
                signature.Append(", time2=").Append(time2.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price2=").Append(price2.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Deletes object having the specified name</summary>
        /// <param name='name'>Object unique name.</param>
        /// <remarks>
        ///     If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed
        ///     error information, one has to call the GetLastError() function.
        /// </remarks>
        public async Task<bool> ObjectDeleteAsync(
            string name, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("41 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectDelete(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>The function returns the value of the specified object property</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='index'>Object property index. It can be any of the Object properties enumeration values.</param>
        /// <remarks>To check errors, one has to call the GetLastError() function. See also ObjectSet() function.</remarks>
        public async Task<double> ObjectGetAsync(
            string name,
            ObjectProperty index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("42 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectGet(");
                signature.Append("name=").Append(name);
                signature.Append(", index=").Append((int) index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Changes the value of the specified object property</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='index'>Object property index. It can be any of the Object properties enumeration values.</param>
        /// <param name='value'>New value of the given property.</param>
        /// <remarks>
        ///     If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed
        ///     error information, one has to call the GetLastError() function. See also ObjectGet() function.
        /// </remarks>
        public async Task<bool> ObjectSetAsync(
            string name,
            ObjectProperty index,
            double value,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("43 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) index).Append(ArgEndC);
            command.Append(ArgStartC).Append(value.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectSet(");
                signature.Append("name=").Append(name);
                signature.Append(", index=").Append((int) index);
                signature.Append(", value=").Append(value.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>The function returns the level description of a Fibonacci object</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='index'>Index of the Fibonacci level (0-31).</param>
        /// <remarks>
        ///     The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To
        ///     get the detailed error information, one has to call the GetLastError() function. See also
        ///     ObjectSetFiboDescription() function.
        /// </remarks>
        public async Task<string> ObjectGetFiboDescriptionAsync(
            string name,
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("44 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectGetFiboDescription(");
                signature.Append("name=").Append(name);
                signature.Append(", index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>The function assigns a new description to a level of a Fibonacci object</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='index'>Index of the Fibonacci level (0-31).</param>
        /// <param name='text'>New description of the level.</param>
        /// <remarks>
        ///     The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To
        ///     get the detailed error information, one has to call the GetLastError() function.
        /// </remarks>
        public async Task<bool> ObjectSetFiboDescriptionAsync(
            string name,
            int index,
            string text,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("45 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            command.Append(ArgStartC).Append(RemoveSpecialChars(text)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectSetFiboDescription(");
                signature.Append("name=").Append(name);
                signature.Append(", index=").Append(index);
                signature.Append(", text=").Append(text);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Changes the object description</summary>
        /// <param name='name'>Object unique name.</param>
        /// <param name='text'>A text describing the object.</param>
        /// <param name='font_size'>Font size in points.</param>
        /// <param name='font'>Font name (NULL).</param>
        /// <param name='text_color'>Text color (CLR_NONE). </param>
        /// <remarks>
        ///     For objects of OBJ_TEXT and OBJ_LABEL, this description is shown as a text line in the chart. If the function
        ///     succeeds, the returned value will be TRUE. Otherwise, it is FALSE. To get the detailed error information, one has
        ///     to call the GetLastError() function. Parameters of font_size, font_name and text_color are used for objects of
        ///     OBJ_TEXT and OBJ_LABEL only. For objects of other types, these parameters are ignored. See also ObjectDescription()
        ///     function.
        /// </remarks>
        public async Task<bool> ObjectSetTextAsync(
            string name,
            string text,
// ReSharper disable InconsistentNaming
            int font_size,
// ReSharper restore InconsistentNaming
            string font,
// ReSharper disable InconsistentNaming
            Color text_color,
// ReSharper restore InconsistentNaming
int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("46 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(RemoveSpecialChars(text)).Append(ArgEndC);
            command.Append(ArgStartC).Append(font_size).Append(ArgEndC);
            command.Append(ArgStartC).Append(font).Append(ArgEndC);
            command.Append(ArgStartC).Append((long) text_color).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectSetText(");
                signature.Append("name=").Append(name);
                signature.Append(", text=").Append(text);
                signature.Append(", font_size=").Append(font_size);
                signature.Append(", font=").Append(font);
                signature.Append(", text_color=").Append((long) text_color);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns total amount of objects of the specified type in the chart</summary>
        /// <param name='type'>
        ///     Optional parameter. An object type to be counted. It can be any of the Object type enumeration
        ///     values or EMPTY constant to count all objects with any types.
        /// </param>
        /// <returns>Total amount of objects of the specified type in the chart</returns>
        public async Task<int> ObjectsTotalAsync(
            ObjectType type,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("47 ");
            //
            command.Append(ArgStartC).Append((int) type).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectsTotal(");
                signature.Append("type=").Append((int) type);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The function returns the object type value</summary>
        /// <param name='name'>Object unique name.</param>
        /// <remarks>To get the detailed error information, one has to call the GetLastError() function. </remarks>
        public async Task<ObjectType> ObjectTypeAsync(
            string name, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("48 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectType(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = (ObjectType) int.Parse(result);
            return res;
        }

        /// <summary>Calculates the Bill Williams' Accelerator/Decelerator oscillator</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iACAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("49 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iAC(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Accumulation/Distribution indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iADAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("50 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iAD(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Bill Williams' Alligator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='jawPeriod'>Blue line averaging period (Alligator's Jaw).</param>
        /// <param name='jawShift'>Blue line shift relative to the chart.</param>
        /// <param name='teethPeriod'>Red line averaging period (Alligator's Teeth).</param>
        /// <param name='teethShift'>Red line shift relative to the chart.</param>
        /// <param name='lipsPeriod'>Green line averaging period (Alligator's Lips).</param>
        /// <param name='lipsShift'>Green line shift relative to the chart.</param>
        /// <param name='maMethod'>MA method. It can be any of Moving Average methods.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='mode'>
        ///     Data source, identifier of a line of the indicator. It can be any of the following values:
        ///     MODE_GATORJAW - Gator Jaw (blue) balance line, MODE_GATORTEETH - Gator Teeth (red) balance line, MODE_GATORLIPS -
        ///     Gator Lips (green) balance line.
        /// </param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iAlligatorAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int jawPeriod,
            int jawShift,
            int teethPeriod,
            int teethShift,
            int lipsPeriod,
            int lipsShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            GatorMode mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("51 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(jawPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(jawShift).Append(ArgEndC);
            command.Append(ArgStartC).Append(teethPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(teethShift).Append(ArgEndC);
            command.Append(ArgStartC).Append(lipsPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(lipsShift).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iAlligator(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", jawPeriod=").Append(jawPeriod);
                signature.Append(", jawShift=").Append(jawShift);
                signature.Append(", teethPeriod=").Append(teethPeriod);
                signature.Append(", teethShift=").Append(teethShift);
                signature.Append(", lipsPeriod=").Append(lipsPeriod);
                signature.Append(", lipsShift=").Append(lipsShift);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Movement directional index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iADXAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            ADXIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("52 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iADX(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Indicator of the average true range and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iATRAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("53 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iATR(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Bill Williams' Awesome oscillator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iAOAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("54 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iAO(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Bears Power indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iBearsPowerAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("55 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBearsPower(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Movement directional index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='deviation'>Deviation from the main line.</param>
        /// <param name='bandsShift'>The indicator shift relative to the chart.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iBandsAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            double deviation,
            int bandsShift,
            AppliedPrice appliedPrice,
            BandsIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("56 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(deviation.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(bandsShift).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBands(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", deviation=").Append(deviation.ToString(CultureInfo.InvariantCulture));
                signature.Append(", bandsShift=").Append(bandsShift);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Bulls Power indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iBullsPowerAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("57 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBullsPower(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Commodity channel index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iCCIAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("58 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iCCI(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

// ReSharper disable InconsistentNaming
        private async Task<double> iCustomAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            string name,
            int mode,
            int shift,
            CustomParams p,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            p?.CheckParams();
            //
            var command = new StringBuilder("59 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            p?.AppendParams(command);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iCustom(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", name=").Append(name);
                signature.Append(", mode=").Append(mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the specified custom indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='name'>Custom indicator compiled program name.</param>
        /// <param name='mode'>
        ///     Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer
        ///     functions.
        /// </param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators
        ///     directory.
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public Task<double> iCustomAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            string name,
            int mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            return iCustomAsync(symbol, timeframe, name, mode, shift, null, timeoutMillis: timeoutMillis, ct: ct);
        }

        /// <summary>Calculates the specified custom indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='name'>Custom indicator compiled program name.</param>
        /// <param name='mode'>
        ///     Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer
        ///     functions.
        /// </param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <param name='p'>
        ///     Parameters set (if necessary). The passed parameters and their order must correspond with the
        ///     desclaration order and the type of extern variables of the custom indicator.
        /// </param>
        /// <remarks>
        ///     The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators
        ///     directory.
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public Task<double> iCustomAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            string name,
            int[] p,
            int mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            return iCustomAsync(symbol, timeframe, name, mode, shift, new CustomParams(p), timeoutMillis, ct);
        }

        /// <summary>Calculates the specified custom indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='name'>Custom indicator compiled program name.</param>
        /// <param name='mode'>
        ///     Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer
        ///     functions.
        /// </param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <param name='p'>
        ///     Parameters set (if necessary). The passed parameters and their order must correspond with the
        ///     desclaration order and the type of extern variables of the custom indicator.
        /// </param>
        /// <remarks>
        ///     The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators
        ///     directory.
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public Task<double> iCustomAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            string name,
            double[] p,
            int mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            return iCustomAsync(symbol, timeframe, name, mode, shift, new CustomParams(p), timeoutMillis, ct);
        }

        /// <summary>Calculates the specified custom indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='name'>Custom indicator compiled program name.</param>
        /// <param name='mode'>
        ///     Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer
        ///     functions.
        /// </param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <param name='p'>
        ///     Parameters set (if necessary). The passed parameters and their order must correspond with the
        ///     desclaration order and the type of extern variables of the custom indicator.
        /// </param>
        /// <remarks>
        ///     The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators
        ///     directory.
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public Task<double> iCustomAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            string name,
            string[] p,
            int mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            return iCustomAsync(symbol, timeframe, name, mode, shift, new CustomParams(p), timeoutMillis, ct);
        }

        /// <summary>Calculates the DeMarker indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iDeMarkerAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("60 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iDeMarker(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Envelopes indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='maPeriod'>Averaging period for calculation of the main line.</param>
        /// <param name='maMethod'>MA method. It can be any of Moving Average methods.</param>
        /// <param name='maShitf'>MA shift. Indicator line offset relate to the chart by timeframe.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='deviation'>Percent deviation from the main line.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iEnvelopesAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int maPeriod,
            MovingAverageMethod maMethod,
            int maShitf,
            AppliedPrice appliedPrice,
            double deviation,
            BandsIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("61 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(maPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append(maShitf).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(deviation.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iEnvelopes(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", maPeriod=").Append(maPeriod);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", maShitf=").Append(maShitf);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", deviation=").Append(deviation.ToString(CultureInfo.InvariantCulture));
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Force index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='maMethod'>MA method. It can be any of Moving Average methods.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iForceAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("62 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iForce(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Fractals and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iFractalsAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            BandsIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("63 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iFractals(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Gator oscillator calculation</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='jawPeriod'>Blue line averaging period (Alligator's Jaw).</param>
        /// <param name='jawShift'>Blue line shift relative to the chart.</param>
        /// <param name='teethPeriod'>Red line averaging period (Alligator's Teeth).</param>
        /// <param name='teethShift'>Red line shift relative to the chart.</param>
        /// <param name='lipsPeriod'>Green line averaging period (Alligator's Lips).</param>
        /// <param name='lipsShift'>Green line shift relative to the chart.</param>
        /// <param name='maMethod'>MA method. It can be any of Moving Average methods.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='mode'>Indicator line index. It can be any of Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     The oscillator displays the difference between the Alligator red and blue lines (the upper histogram) and that
        ///     between red and green lines (the lower histogram).
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iGatorAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int jawPeriod,
            int jawShift,
            int teethPeriod,
            int teethShift,
            int lipsPeriod,
            int lipsShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            BandsIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("64 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(jawPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(jawShift).Append(ArgEndC);
            command.Append(ArgStartC).Append(teethPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(teethShift).Append(ArgEndC);
            command.Append(ArgStartC).Append(lipsPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(lipsShift).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iGator(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", jawPeriod=").Append(jawPeriod);
                signature.Append(", jawShift=").Append(jawShift);
                signature.Append(", teethPeriod=").Append(teethPeriod);
                signature.Append(", teethShift=").Append(teethShift);
                signature.Append(", lipsPeriod=").Append(lipsPeriod);
                signature.Append(", lipsShift=").Append(lipsShift);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Bill Williams Market Facilitation index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iBWMFIAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("65 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iBWMFI(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Momentum indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iMomentumAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("66 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iMomentum(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Money flow index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iMFIAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("67 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iMFI(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Moving average indicator and returns its value</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='maShift'>MA shift. Indicators line offset relate to the chart by timeframe.</param>
        /// <param name='maMethod'>MA method. It can be any of the Moving Average method enumeration value.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iMAAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            int maShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("68 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(maShift).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iMA(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", maShift=").Append(maShift);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Moving Average of Oscillator and returns its value</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='fastEMAPeriod'>Number of periods for fast moving average calculation.</param>
        /// <param name='slowEMAPeriod'>Number of periods for slow moving average calculation.</param>
        /// <param name='signalPeriod'>Number of periods for signal moving average calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>Sometimes called MACD Histogram in some systems. </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iOsMAAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
// ReSharper disable InconsistentNaming
            int fastEMAPeriod,
            int slowEMAPeriod,
// ReSharper restore InconsistentNaming
            int signalPeriod,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("69 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(fastEMAPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(slowEMAPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(signalPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iOsMA(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", fastEMAPeriod=").Append(fastEMAPeriod);
                signature.Append(", slowEMAPeriod=").Append(slowEMAPeriod);
                signature.Append(", signalPeriod=").Append(signalPeriod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Moving averages convergence/divergence and returns its value</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='fastEMAPeriod'>Number of periods for fast moving average calculation.</param>
        /// <param name='slowEMAPeriod'>Number of periods for slow moving average calculation.</param>
        /// <param name='signalPeriod'>Number of periods for signal moving average calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <remarks>
        ///     In the systems where OsMA is called MACD Histogram, this indicator is displayed as two lines. In the Client
        ///     Terminal, the Moving Average Convergence/Divergence is drawn as a histogram.
        /// </remarks>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iMACDAsync(
            string symbol,
            Timeframe timeframe,
            int fastEMAPeriod,
            int slowEMAPeriod,
// ReSharper restore InconsistentNaming
            int signalPeriod,
            AppliedPrice appliedPrice,
            MACDIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("70 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(fastEMAPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(slowEMAPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(signalPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iMACD(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", fastEMAPeriod=").Append(fastEMAPeriod);
                signature.Append(", slowEMAPeriod=").Append(slowEMAPeriod);
                signature.Append(", signalPeriod=").Append(signalPeriod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the On Balance Volume indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iOBVAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("71 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iOBV(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Parabolic Stop and Reverse system and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='step'>Increment, usually 0.02.</param>
        /// <param name='maximum'>Maximum value, usually 0.2.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iSARAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            double step,
            double maximum,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("72 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(step.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(maximum.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iSAR(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", step=").Append(step.ToString(CultureInfo.InvariantCulture));
                signature.Append(", maximum=").Append(maximum.ToString(CultureInfo.InvariantCulture));
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Relative strength index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Number of periods for calculation.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iRSIAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("73 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iRSI(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Relative Vigor index and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Number of periods for calculation.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iRVIAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            MACDIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("74 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iRVI(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Standard Deviation indicator and returns its value</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='maPeriod'>MA period</param>
        /// <param name='maShift'>MA shift. Indicators line offset relate to the chart by timeframe.</param>
        /// <param name='maMethod'>MA method. It can be any of the Moving Average method enumeration value.</param>
        /// <param name='appliedPrice'>Applied price. It can be any of Applied price enumeration values.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iStdDevAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int maPeriod,
            int maShift,
            MovingAverageMethod maMethod,
            AppliedPrice appliedPrice,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("75 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(maPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(maShift).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) appliedPrice).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iStdDev(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", maPeriod=").Append(maPeriod);
                signature.Append(", maShift=").Append(maShift);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", appliedPrice=").Append((int) appliedPrice);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Stochastic oscillator and returns its value</summary>
        /// <param name='symbol'>Symbol the data of which should be used to calculate indicator; NULL means the current symbol.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='kPeriod'>%K line period.</param>
        /// <param name='dPeriod'>%D line period.</param>
        /// <param name='slowing'>Slowing value.</param>
        /// <param name='maMethod'>MA method. It can be any of the Moving Average method enumeration value.</param>
        /// <param name='priceField'>Price field parameter. Can be one of this values: 0 - Low/High or 1 - Close/Close.</param>
        /// <param name='mode'>Indicator line index. It can be any of the Indicators line identifiers enumeration value.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iStochasticAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int kPeriod,
            int dPeriod,
            int slowing,
            MovingAverageMethod maMethod,
            int priceField,
            MACDIndicatorLines mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("76 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(kPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(dPeriod).Append(ArgEndC);
            command.Append(ArgStartC).Append(slowing).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) maMethod).Append(ArgEndC);
            command.Append(ArgStartC).Append(priceField).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iStochastic(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", kPeriod=").Append(kPeriod);
                signature.Append(", dPeriod=").Append(dPeriod);
                signature.Append(", slowing=").Append(slowing);
                signature.Append(", maMethod=").Append((int) maMethod);
                signature.Append(", priceField=").Append(priceField);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculates the Larry William's percent range indicator and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='period'>Averaging period for calculation.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iWPRAsync(
// ReSharper restore InconsistentNaming
            string symbol,
            Timeframe timeframe,
            int period,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("77 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(period).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iWPR(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", period=").Append(period);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Closes opened order</summary>
        /// <param name='ticket'>Unique number of the order ticket.</param>
        /// <param name='lots'>Number of lots.</param>
        /// <param name='price'>Preferred closing price.</param>
        /// <param name='slippage'>Value of the maximum price slippage in points.</param>
        /// <param name='arrowColor'>
        ///     Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value
        ///     closing arrow will not be drawn on the chart.
        /// </param>
        /// <remarks>
        ///     If the function succeeds, the return value is true. If the function fails, the return value is false. To get
        ///     the detailed error information, call GetLastError().
        /// </remarks>
        /// <exception cref="ErrCustomIndicatorError"></exception>
        /// <exception cref="ErrIntegerParameterExpected"></exception>
        /// <exception cref="ErrInvalidFunctionParamvalue"></exception>
        /// <exception cref="ErrInvalidPriceParam"></exception>
        /// <exception cref="ErrInvalidTicket"></exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrTradeNotAllowed"></exception>
        /// <exception cref="ErrCommonError">Common error</exception>
        /// <exception cref="ErrInvalidTradeParameters">
        ///     Invalid parameters were passed to the trading function, for example, wrong
        ///     symbol, unknown trade operation, negative slippage, non-existing ticket number, etc
        /// </exception>
        /// <exception cref="ErrServerBusy">Trade server is busy</exception>
        /// <exception cref="ErrOldVersion">Old version of the client terminal</exception>
        /// <exception cref="ErrNoConnection">No connection to the trade server</exception>
        /// <exception cref="ErrTooFrequentRequests">Requests are too frequent</exception>
        /// <exception cref="ErrAccountDisabled">The account was disabled</exception>
        /// <exception cref="ErrInvalidAccount">The account number is invalid</exception>
        /// <exception cref="ErrTradeTimeout">Timeout for the trade has been reached</exception>
        /// <exception cref="ErrInvalidPrice">Invalid bid or ask price, perhaps, unnormalized price</exception>
        /// <exception cref="ErrInvalidStops">
        ///     Stops are too close, or prices are ill-calculated or unnormalized (or in the open
        ///     price of a pending order)
        /// </exception>
        /// <exception cref="ErrInvalidTradeVolume">Invalid trade volume, error in the volume granularity</exception>
        /// <exception cref="ErrMarketClosed">Market is closed</exception>
        /// <exception cref="ErrTradeDisabled">Trade is disabled</exception>
        /// <exception cref="ErrNotEnoughMoney">Not enough money to make an operation</exception>
        /// <exception cref="ErrPriceChanged">The price has changed</exception>
        /// <exception cref="ErrOffQuotes">No quotes</exception>
        /// <exception cref="ErrRequote">The requested price has become out of date or bid and ask prices have been mixed up</exception>
        /// <exception cref="ErrOrderLocked">The order has been locked and under processing</exception>
        /// <exception cref="ErrLongPositionsOnlyAllowed">Only buying operation is allowed</exception>
        /// <exception cref="ErrTooManyRequests">Too many requests</exception>
        /// <exception cref="ErrTradeTimeout2">The order has been enqueued</exception>
        /// <exception cref="ErrTradeTimeout3">The order was accepted by the dealer for execution</exception>
        /// <exception cref="ErrTradeTimeout4">The order was discarded by the client during manual confirmation</exception>
        /// <exception cref="ErrTradeModifyDenied">
        ///     Modifying has been denied since the order is too close to market and locked for
        ///     possible soon execution
        /// </exception>
        /// <exception cref="ErrTradeContextBusy">The trade thread is busy</exception>
        /// <exception cref="ErrTradeExpirationDenied">The use of pending order expiration date has been denied by the broker</exception>
        /// <exception cref="ErrTradeTooManyOrders">The amount of open and pending orders has reached the limit set by the broker</exception>
        public async Task<bool> OrderCloseAsync(
            long ticket,
            double lots,
            double price,
            int slippage,
            Color arrowColor,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("78 ");
            //
            command.Append(ArgStartC).Append(ticket).Append(ArgEndC);
            command.Append(ArgStartC).Append(lots.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(slippage).Append(ArgEndC);
            command.Append(ArgStartC).Append((long) arrowColor).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderClose(");
                signature.Append("ticket=").Append(ticket);
                signature.Append(", lots=").Append(lots.ToString(CultureInfo.InvariantCulture));
                signature.Append(", price=").Append(price.ToString(CultureInfo.InvariantCulture));
                signature.Append(", slippage=").Append(slippage);
                signature.Append(", arrowColor=").Append((long) arrowColor);
                signature.Append(')');
                switch (error)
                {
                    case 4055:
                        throw new ErrCustomIndicatorError(signature.ToString());
                    case 4063:
                        throw new ErrIntegerParameterExpected(signature.ToString());
                    case 4051:
                        throw new ErrInvalidFunctionParamvalue(signature.ToString());
                    case 4107:
                        throw new ErrInvalidPriceParam(signature.ToString());
                    case 4108:
                        throw new ErrInvalidTicket(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4109:
                        throw new ErrTradeNotAllowed(signature.ToString());
                    case 0:
                        break;
                    case 2:
                        throw new ErrCommonError(signature.ToString());
                    case 3:
                        throw new ErrInvalidTradeParameters(signature.ToString());
                    case 4:
                        throw new ErrServerBusy(signature.ToString());
                    case 5:
                        throw new ErrOldVersion(signature.ToString());
                    case 6:
                        throw new ErrNoConnection(signature.ToString());
                    case 8:
                        throw new ErrTooFrequentRequests(signature.ToString());
                    case 64:
                        throw new ErrAccountDisabled(signature.ToString());
                    case 65:
                        throw new ErrInvalidAccount(signature.ToString());
                    case 128:
                        throw new ErrTradeTimeout(signature.ToString());
                    case 129:
                        throw new ErrInvalidPrice(signature.ToString());
                    case 130:
                        throw new ErrInvalidStops(signature.ToString());
                    case 131:
                        throw new ErrInvalidTradeVolume(signature.ToString());
                    case 132:
                        throw new ErrMarketClosed(signature.ToString());
                    case 133:
                        throw new ErrTradeDisabled(signature.ToString());
                    case 134:
                        throw new ErrNotEnoughMoney(signature.ToString());
                    case 135:
                        throw new ErrPriceChanged(signature.ToString());
                    case 136:
                        throw new ErrOffQuotes(signature.ToString());
                    case 138:
                        throw new ErrRequote(signature.ToString());
                    case 139:
                        throw new ErrOrderLocked(signature.ToString());
                    case 140:
                        throw new ErrLongPositionsOnlyAllowed(signature.ToString());
                    case 141:
                        throw new ErrTooManyRequests(signature.ToString());
                    case 142:
                        throw new ErrTradeTimeout2(signature.ToString());
                    case 143:
                        throw new ErrTradeTimeout3(signature.ToString());
                    case 144:
                        throw new ErrTradeTimeout4(signature.ToString());
                    case 145:
                        throw new ErrTradeModifyDenied(signature.ToString());
                    case 146:
                        throw new ErrTradeContextBusy(signature.ToString());
                    case 147:
                        throw new ErrTradeExpirationDenied(signature.ToString());
                    case 148:
                        throw new ErrTradeTooManyOrders(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            long t;
            if (long.TryParse(result, out t))
            {
                return t > 0;
            }
            return bool.Parse(result);
        }

        /// <summary>Closes an opened order by another opposite opened order</summary>
        /// <param name='ticket'>Unique number of the order ticket.</param>
        /// <param name='opposite'>Unique number of the opposite order ticket.</param>
        /// <param name='arrowColor'>
        ///     Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value
        ///     closing arrow will not be drawn on the chart.
        /// </param>
        /// <remarks>
        ///     If the function succeeds, the return value is true. If the function fails, the return value is false. To get
        ///     the detailed error information, call GetLastError().
        /// </remarks>
        /// <exception cref="ErrCustomIndicatorError"></exception>
        /// <exception cref="ErrIntegerParameterExpected"></exception>
        /// <exception cref="ErrInvalidFunctionParamvalue"></exception>
        /// <exception cref="ErrInvalidTicket"></exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrTradeNotAllowed"></exception>
        /// <exception cref="ErrCommonError">Common error</exception>
        /// <exception cref="ErrInvalidTradeParameters">
        ///     Invalid parameters were passed to the trading function, for example, wrong
        ///     symbol, unknown trade operation, negative slippage, non-existing ticket number, etc
        /// </exception>
        /// <exception cref="ErrServerBusy">Trade server is busy</exception>
        /// <exception cref="ErrOldVersion">Old version of the client terminal</exception>
        /// <exception cref="ErrNoConnection">No connection to the trade server</exception>
        /// <exception cref="ErrTooFrequentRequests">Requests are too frequent</exception>
        /// <exception cref="ErrAccountDisabled">The account was disabled</exception>
        /// <exception cref="ErrInvalidAccount">The account number is invalid</exception>
        /// <exception cref="ErrTradeTimeout">Timeout for the trade has been reached</exception>
        /// <exception cref="ErrInvalidPrice">Invalid bid or ask price, perhaps, unnormalized price</exception>
        /// <exception cref="ErrInvalidStops">
        ///     Stops are too close, or prices are ill-calculated or unnormalized (or in the open
        ///     price of a pending order)
        /// </exception>
        /// <exception cref="ErrInvalidTradeVolume">Invalid trade volume, error in the volume granularity</exception>
        /// <exception cref="ErrMarketClosed">Market is closed</exception>
        /// <exception cref="ErrTradeDisabled">Trade is disabled</exception>
        /// <exception cref="ErrNotEnoughMoney">Not enough money to make an operation</exception>
        /// <exception cref="ErrPriceChanged">The price has changed</exception>
        /// <exception cref="ErrOffQuotes">No quotes</exception>
        /// <exception cref="ErrRequote">The requested price has become out of date or bid and ask prices have been mixed up</exception>
        /// <exception cref="ErrOrderLocked">The order has been locked and under processing</exception>
        /// <exception cref="ErrLongPositionsOnlyAllowed">Only buying operation is allowed</exception>
        /// <exception cref="ErrTooManyRequests">Too many requests</exception>
        /// <exception cref="ErrTradeTimeout2">The order has been enqueued</exception>
        /// <exception cref="ErrTradeTimeout3">The order was accepted by the dealer for execution</exception>
        /// <exception cref="ErrTradeTimeout4">The order was discarded by the client during manual confirmation</exception>
        /// <exception cref="ErrTradeModifyDenied">
        ///     Modifying has been denied since the order is too close to market and locked for
        ///     possible soon execution
        /// </exception>
        /// <exception cref="ErrTradeContextBusy">The trade thread is busy</exception>
        /// <exception cref="ErrTradeExpirationDenied">The use of pending order expiration date has been denied by the broker</exception>
        /// <exception cref="ErrTradeTooManyOrders">The amount of open and pending orders has reached the limit set by the broker</exception>
        public async Task<bool> OrderCloseByAsync(
            long ticket,
            long opposite,
            Color arrowColor,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("79 ");
            //
            command.Append(ArgStartC).Append(ticket).Append(ArgEndC);
            command.Append(ArgStartC).Append(opposite).Append(ArgEndC);
            command.Append(ArgStartC).Append((long) arrowColor).Append(ArgEndC);
            //
            // String result = (OrdersProcessingChannel ?? StrategyRunner).SendCommandGetResult(command);
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderCloseBy(");
                signature.Append("ticket=").Append(ticket);
                signature.Append(", opposite=").Append(opposite);
                signature.Append(", arrowColor=").Append((long) arrowColor);
                signature.Append(')');
                switch (error)
                {
                    case 4055:
                        throw new ErrCustomIndicatorError(signature.ToString());
                    case 4063:
                        throw new ErrIntegerParameterExpected(signature.ToString());
                    case 4051:
                        throw new ErrInvalidFunctionParamvalue(signature.ToString());
                    case 4108:
                        throw new ErrInvalidTicket(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4109:
                        throw new ErrTradeNotAllowed(signature.ToString());
                    case 0:
                        break;
                    case 2:
                        throw new ErrCommonError(signature.ToString());
                    case 3:
                        throw new ErrInvalidTradeParameters(signature.ToString());
                    case 4:
                        throw new ErrServerBusy(signature.ToString());
                    case 5:
                        throw new ErrOldVersion(signature.ToString());
                    case 6:
                        throw new ErrNoConnection(signature.ToString());
                    case 8:
                        throw new ErrTooFrequentRequests(signature.ToString());
                    case 64:
                        throw new ErrAccountDisabled(signature.ToString());
                    case 65:
                        throw new ErrInvalidAccount(signature.ToString());
                    case 128:
                        throw new ErrTradeTimeout(signature.ToString());
                    case 129:
                        throw new ErrInvalidPrice(signature.ToString());
                    case 130:
                        throw new ErrInvalidStops(signature.ToString());
                    case 131:
                        throw new ErrInvalidTradeVolume(signature.ToString());
                    case 132:
                        throw new ErrMarketClosed(signature.ToString());
                    case 133:
                        throw new ErrTradeDisabled(signature.ToString());
                    case 134:
                        throw new ErrNotEnoughMoney(signature.ToString());
                    case 135:
                        throw new ErrPriceChanged(signature.ToString());
                    case 136:
                        throw new ErrOffQuotes(signature.ToString());
                    case 138:
                        throw new ErrRequote(signature.ToString());
                    case 139:
                        throw new ErrOrderLocked(signature.ToString());
                    case 140:
                        throw new ErrLongPositionsOnlyAllowed(signature.ToString());
                    case 141:
                        throw new ErrTooManyRequests(signature.ToString());
                    case 142:
                        throw new ErrTradeTimeout2(signature.ToString());
                    case 143:
                        throw new ErrTradeTimeout3(signature.ToString());
                    case 144:
                        throw new ErrTradeTimeout4(signature.ToString());
                    case 145:
                        throw new ErrTradeModifyDenied(signature.ToString());
                    case 146:
                        throw new ErrTradeContextBusy(signature.ToString());
                    case 147:
                        throw new ErrTradeExpirationDenied(signature.ToString());
                    case 148:
                        throw new ErrTradeTooManyOrders(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            long t;
            if (long.TryParse(result, out t))
            {
                return t > 0;
            }
            return bool.Parse(result);
        }

        /// <summary>Returns close price for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Close price for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderClosePriceAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("80 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderClosePrice(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns close time for the currently selected order</summary>
        /// <remarks>
        ///     If order close time is not 0 then the order selected and has been closed and retrieved from the account
        ///     history. Open and pending orders close time is equal to 0. Note: The order must be previously selected by the
        ///     OrderSelect() function.
        /// </remarks>
        /// <returns>Close time for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<DateTime> OrderCloseTimeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("81 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderCloseTime(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>Returns comment for the selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Comment for the selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<string> OrderCommentAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("82 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderComment(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns calculated commission for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Calculated commission for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderCommissionAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("83 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderCommission(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Deletes previously opened pending order</summary>
        /// <param name='ticket'>Unique number of the order ticket.</param>
        /// <param name='arrowColor'>
        ///     Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing
        ///     arrow will not be drawn on the chart.
        /// </param>
        /// <remarks>
        ///     If the function succeeds, the return value is true. If the function fails, the return value is false. To get
        ///     the detailed error information, call GetLastError().
        /// </remarks>
        /// <exception cref="ErrCustomIndicatorError"></exception>
        /// <exception cref="ErrInvalidFunctionParamvalue"></exception>
        /// <exception cref="ErrInvalidTicket"></exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrTradeNotAllowed"></exception>
        /// <exception cref="ErrCommonError">Common error</exception>
        /// <exception cref="ErrInvalidTradeParameters">
        ///     Invalid parameters were passed to the trading function, for example, wrong
        ///     symbol, unknown trade operation, negative slippage, non-existing ticket number, etc
        /// </exception>
        /// <exception cref="ErrServerBusy">Trade server is busy</exception>
        /// <exception cref="ErrOldVersion">Old version of the client terminal</exception>
        /// <exception cref="ErrNoConnection">No connection to the trade server</exception>
        /// <exception cref="ErrTooFrequentRequests">Requests are too frequent</exception>
        /// <exception cref="ErrAccountDisabled">The account was disabled</exception>
        /// <exception cref="ErrInvalidAccount">The account number is invalid</exception>
        /// <exception cref="ErrTradeTimeout">Timeout for the trade has been reached</exception>
        /// <exception cref="ErrInvalidPrice">Invalid bid or ask price, perhaps, unnormalized price</exception>
        /// <exception cref="ErrInvalidStops">
        ///     Stops are too close, or prices are ill-calculated or unnormalized (or in the open
        ///     price of a pending order)
        /// </exception>
        /// <exception cref="ErrInvalidTradeVolume">Invalid trade volume, error in the volume granularity</exception>
        /// <exception cref="ErrMarketClosed">Market is closed</exception>
        /// <exception cref="ErrTradeDisabled">Trade is disabled</exception>
        /// <exception cref="ErrNotEnoughMoney">Not enough money to make an operation</exception>
        /// <exception cref="ErrPriceChanged">The price has changed</exception>
        /// <exception cref="ErrOffQuotes">No quotes</exception>
        /// <exception cref="ErrRequote">The requested price has become out of date or bid and ask prices have been mixed up</exception>
        /// <exception cref="ErrOrderLocked">The order has been locked and under processing</exception>
        /// <exception cref="ErrLongPositionsOnlyAllowed">Only buying operation is allowed</exception>
        /// <exception cref="ErrTooManyRequests">Too many requests</exception>
        /// <exception cref="ErrTradeTimeout2">The order has been enqueued</exception>
        /// <exception cref="ErrTradeTimeout3">The order was accepted by the dealer for execution</exception>
        /// <exception cref="ErrTradeTimeout4">The order was discarded by the client during manual confirmation</exception>
        /// <exception cref="ErrTradeModifyDenied">
        ///     Modifying has been denied since the order is too close to market and locked for
        ///     possible soon execution
        /// </exception>
        /// <exception cref="ErrTradeContextBusy">The trade thread is busy</exception>
        /// <exception cref="ErrTradeExpirationDenied">The use of pending order expiration date has been denied by the broker</exception>
        /// <exception cref="ErrTradeTooManyOrders">The amount of open and pending orders has reached the limit set by the broker</exception>
        public async Task<bool> OrderDeleteAsync(
            long ticket,
            Color arrowColor,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("84 ");
            //
            command.Append(ArgStartC).Append(ticket).Append(ArgEndC);
            command.Append(ArgStartC).Append((long) arrowColor).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderDelete(");
                signature.Append("ticket=").Append(ticket);
                signature.Append(", arrowColor=").Append((long) arrowColor);
                signature.Append(')');
                switch (error)
                {
                    case 4055:
                        throw new ErrCustomIndicatorError(signature.ToString());
                    case 4051:
                        throw new ErrInvalidFunctionParamvalue(signature.ToString());
                    case 4108:
                        throw new ErrInvalidTicket(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4109:
                        throw new ErrTradeNotAllowed(signature.ToString());
                    case 0:
                        break;
                    case 2:
                        throw new ErrCommonError(signature.ToString());
                    case 3:
                        throw new ErrInvalidTradeParameters(signature.ToString());
                    case 4:
                        throw new ErrServerBusy(signature.ToString());
                    case 5:
                        throw new ErrOldVersion(signature.ToString());
                    case 6:
                        throw new ErrNoConnection(signature.ToString());
                    case 8:
                        throw new ErrTooFrequentRequests(signature.ToString());
                    case 64:
                        throw new ErrAccountDisabled(signature.ToString());
                    case 65:
                        throw new ErrInvalidAccount(signature.ToString());
                    case 128:
                        throw new ErrTradeTimeout(signature.ToString());
                    case 129:
                        throw new ErrInvalidPrice(signature.ToString());
                    case 130:
                        throw new ErrInvalidStops(signature.ToString());
                    case 131:
                        throw new ErrInvalidTradeVolume(signature.ToString());
                    case 132:
                        throw new ErrMarketClosed(signature.ToString());
                    case 133:
                        throw new ErrTradeDisabled(signature.ToString());
                    case 134:
                        throw new ErrNotEnoughMoney(signature.ToString());
                    case 135:
                        throw new ErrPriceChanged(signature.ToString());
                    case 136:
                        throw new ErrOffQuotes(signature.ToString());
                    case 138:
                        throw new ErrRequote(signature.ToString());
                    case 139:
                        throw new ErrOrderLocked(signature.ToString());
                    case 140:
                        throw new ErrLongPositionsOnlyAllowed(signature.ToString());
                    case 141:
                        throw new ErrTooManyRequests(signature.ToString());
                    case 142:
                        throw new ErrTradeTimeout2(signature.ToString());
                    case 143:
                        throw new ErrTradeTimeout3(signature.ToString());
                    case 144:
                        throw new ErrTradeTimeout4(signature.ToString());
                    case 145:
                        throw new ErrTradeModifyDenied(signature.ToString());
                    case 146:
                        throw new ErrTradeContextBusy(signature.ToString());
                    case 147:
                        throw new ErrTradeExpirationDenied(signature.ToString());
                    case 148:
                        throw new ErrTradeTooManyOrders(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns expiration date for the selected pending order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Expiration date for the selected pending order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<DateTime> OrderExpirationAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("85 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderExpiration(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>Returns amount of lots for the selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Amount of lots for the selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderLotsAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("86 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderLots(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns an identifying (magic) number for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function.</remarks>
        /// <returns>An identifying (magic) number for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<int> OrderMagicNumberAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("87 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderMagicNumber(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Modification of characteristics for the previously opened position or pending orders</summary>
        /// <param name='ticket'>Unique number of the order ticket.</param>
        /// <param name='price'>New open price of the pending order.</param>
        /// <param name='stoploss'>New StopLoss level.</param>
        /// <param name='takeprofit'>New TakeProfit level.</param>
        /// <param name='expiration'>Pending order expiration time.</param>
        /// <param name='arrowColor'>
        ///     Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing
        ///     arrow will not be drawn on the chart.
        /// </param>
        /// <remarks>
        ///     If the function succeeds, the returned value will be TRUE. If the function fails, the returned value will be
        ///     FALSE. To get the detailed error information, call GetLastError() function. Notes: Open price and expiration time
        ///     can be changed only for pending orders. If unchanged values are passed as the function parameters, the error 1
        ///     (ERR_NO_RESULT) will be generated. Pending order expiration time can be disabled in some trade servers. In this
        ///     case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED)
        ///     will be generated.
        /// </remarks>
        /// <exception cref="ErrCustomIndicatorError"></exception>
        /// <exception cref="ErrIntegerParameterExpected"></exception>
        /// <exception cref="ErrInvalidFunctionParamvalue"></exception>
        /// <exception cref="ErrInvalidPriceParam"></exception>
        /// <exception cref="ErrInvalidTicket"></exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrTradeNotAllowed"></exception>
        /// <exception cref="ErrNoResult">OrderModify attempts to replace the values already set with the same values</exception>
        /// <exception cref="ErrCommonError">Common error</exception>
        /// <exception cref="ErrInvalidTradeParameters">
        ///     Invalid parameters were passed to the trading function, for example, wrong
        ///     symbol, unknown trade operation, negative slippage, non-existing ticket number, etc
        /// </exception>
        /// <exception cref="ErrServerBusy">Trade server is busy</exception>
        /// <exception cref="ErrOldVersion">Old version of the client terminal</exception>
        /// <exception cref="ErrNoConnection">No connection to the trade server</exception>
        /// <exception cref="ErrTooFrequentRequests">Requests are too frequent</exception>
        /// <exception cref="ErrAccountDisabled">The account was disabled</exception>
        /// <exception cref="ErrInvalidAccount">The account number is invalid</exception>
        /// <exception cref="ErrTradeTimeout">Timeout for the trade has been reached</exception>
        /// <exception cref="ErrInvalidPrice">Invalid bid or ask price, perhaps, unnormalized price</exception>
        /// <exception cref="ErrInvalidStops">
        ///     Stops are too close, or prices are ill-calculated or unnormalized (or in the open
        ///     price of a pending order)
        /// </exception>
        /// <exception cref="ErrInvalidTradeVolume">Invalid trade volume, error in the volume granularity</exception>
        /// <exception cref="ErrMarketClosed">Market is closed</exception>
        /// <exception cref="ErrTradeDisabled">Trade is disabled</exception>
        /// <exception cref="ErrNotEnoughMoney">Not enough money to make an operation</exception>
        /// <exception cref="ErrPriceChanged">The price has changed</exception>
        /// <exception cref="ErrOffQuotes">No quotes</exception>
        /// <exception cref="ErrRequote">The requested price has become out of date or bid and ask prices have been mixed up</exception>
        /// <exception cref="ErrOrderLocked">The order has been locked and under processing</exception>
        /// <exception cref="ErrLongPositionsOnlyAllowed">Only buying operation is allowed</exception>
        /// <exception cref="ErrTooManyRequests">Too many requests</exception>
        /// <exception cref="ErrTradeTimeout2">The order has been enqueued</exception>
        /// <exception cref="ErrTradeTimeout3">The order was accepted by the dealer for execution</exception>
        /// <exception cref="ErrTradeTimeout4">The order was discarded by the client during manual confirmation</exception>
        /// <exception cref="ErrTradeModifyDenied">
        ///     Modifying has been denied since the order is too close to market and locked for
        ///     possible soon execution
        /// </exception>
        /// <exception cref="ErrTradeContextBusy">The trade thread is busy</exception>
        /// <exception cref="ErrTradeExpirationDenied">The use of pending order expiration date has been denied by the broker</exception>
        /// <exception cref="ErrTradeTooManyOrders">The amount of open and pending orders has reached the limit set by the broker</exception>
        public async Task<bool> OrderModifyAsync(
            long ticket,
            double price,
            double stoploss,
            double takeprofit,
            DateTime expiration,
            Color arrowColor,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("88 ");
            //
            command.Append(ArgStartC).Append(ticket).Append(ArgEndC);
            command.Append(ArgStartC).Append(price.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(stoploss.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(takeprofit.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (expiration == DateTime.MaxValue ? NoExpiration : expiration).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append((long) arrowColor).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderModify(");
                signature.Append("ticket=").Append(ticket);
                signature.Append(", price=").Append(price.ToString(CultureInfo.InvariantCulture));
                signature.Append(", stoploss=").Append(stoploss.ToString(CultureInfo.InvariantCulture));
                signature.Append(", takeprofit=").Append(takeprofit.ToString(CultureInfo.InvariantCulture));
                signature.Append(", expiration=").Append(expiration.ToString(YyyyMmDdHhMmSs));
                signature.Append(", arrowColor=").Append((long) arrowColor);
                signature.Append(')');
                switch (error)
                {
                    case 4055:
                        throw new ErrCustomIndicatorError(signature.ToString());
                    case 4063:
                        throw new ErrIntegerParameterExpected(signature.ToString());
                    case 4051:
                        throw new ErrInvalidFunctionParamvalue(signature.ToString());
                    case 4107:
                        throw new ErrInvalidPriceParam(signature.ToString());
                    case 4108:
                        throw new ErrInvalidTicket(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4109:
                        throw new ErrTradeNotAllowed(signature.ToString());
                    case 1:
                        throw new ErrNoResult(signature.ToString());
                    case 0:
                        break;
                    case 2:
                        throw new ErrCommonError(signature.ToString());
                    case 3:
                        throw new ErrInvalidTradeParameters(signature.ToString());
                    case 4:
                        throw new ErrServerBusy(signature.ToString());
                    case 5:
                        throw new ErrOldVersion(signature.ToString());
                    case 6:
                        throw new ErrNoConnection(signature.ToString());
                    case 8:
                        throw new ErrTooFrequentRequests(signature.ToString());
                    case 64:
                        throw new ErrAccountDisabled(signature.ToString());
                    case 65:
                        throw new ErrInvalidAccount(signature.ToString());
                    case 128:
                        throw new ErrTradeTimeout(signature.ToString());
                    case 129:
                        throw new ErrInvalidPrice(signature.ToString());
                    case 130:
                        throw new ErrInvalidStops(signature.ToString());
                    case 131:
                        throw new ErrInvalidTradeVolume(signature.ToString());
                    case 132:
                        throw new ErrMarketClosed(signature.ToString());
                    case 133:
                        throw new ErrTradeDisabled(signature.ToString());
                    case 134:
                        throw new ErrNotEnoughMoney(signature.ToString());
                    case 135:
                        throw new ErrPriceChanged(signature.ToString());
                    case 136:
                        throw new ErrOffQuotes(signature.ToString());
                    case 138:
                        throw new ErrRequote(signature.ToString());
                    case 139:
                        throw new ErrOrderLocked(signature.ToString());
                    case 140:
                        throw new ErrLongPositionsOnlyAllowed(signature.ToString());
                    case 141:
                        throw new ErrTooManyRequests(signature.ToString());
                    case 142:
                        throw new ErrTradeTimeout2(signature.ToString());
                    case 143:
                        throw new ErrTradeTimeout3(signature.ToString());
                    case 144:
                        throw new ErrTradeTimeout4(signature.ToString());
                    case 145:
                        throw new ErrTradeModifyDenied(signature.ToString());
                    case 146:
                        throw new ErrTradeContextBusy(signature.ToString());
                    case 147:
                        throw new ErrTradeExpirationDenied(signature.ToString());
                    case 148:
                        throw new ErrTradeTooManyOrders(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns open price for the currently selected order</summary>
        /// <remarks>Order must be first selected by the OrderSelect() function. </remarks>
        /// <returns>Open price for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderOpenPriceAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("89 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderOpenPrice(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns open time for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Open time for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<DateTime> OrderOpenTimeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("90 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderOpenTime(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>
        ///     Prints information about the selected order in the log in the following format: ticket number; open time;
        ///     trade operation; amount of lots; open price; Stop Loss; Take Profit; close time; close price; commission; swap;
        ///     profit; comment; magic number; pending order expiration date
        /// </summary>
        /// <remarks>Order must be selected by the OrderSelect() function. </remarks>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task OrderPrintAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("91 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderPrint(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Returns the net profit value (without swaps or commissions) for the selected order</summary>
        /// <remarks>
        ///     For open positions, it is the current unrealized profit. For closed orders, it is the fixed profit. Returns
        ///     profit for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
        /// </remarks>
        /// <returns>The net profit value (without swaps or commissions) for the selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderProfitAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("92 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderProfit(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>The function selects an order for further processing</summary>
        /// <param name='ticket'>Order index or order ticket depending on the second parameter.</param>
        /// <param name='select'>
        ///     Selecting flags. It can be any of the following values: SELECT_BY_POS - index in the order pool,
        ///     SELECT_BY_TICKET - index is order ticket.
        /// </param>
        /// <param name='pool'>
        ///     Optional order pool index. Used when the selected parameter is SELECT_BY_POS. It can be any of the
        ///     following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY
        ///     - order selected from history pool (closed and canceled order).
        /// </param>
        /// <remarks>
        ///     It returns TRUE if the function succeeds. It returns FALSE if the function fails. To get the error
        ///     information, one has to call the GetLastError() function. The pool parameter is ignored if the order is selected by
        ///     the ticket number. The ticket number is a unique order identifier. To find out from what list the order has been
        ///     selected, its close time must be analyzed. If the order close time equals to 0, the order is open or pending and
        ///     taken from the terminal open positions list. One can distinguish an open position from a pending order by the order
        ///     type. If the order close time does not equal to 0, the order is a closed order or a deleted pending order and was
        ///     selected from the terminal history. They also differ from each other by their order types.
        /// </remarks>
        public virtual async Task<bool> OrderSelectAsync(
            long ticket,
            SelectionType select,
            SelectionPool pool, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("93 ");
            //
            command.Append(ArgStartC).Append(ticket).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) @select).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) pool).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderSelect(");
                signature.Append("index=").Append(ticket);
                signature.Append(", select=").Append((int) @select);
                signature.Append(", pool=").Append((int) pool);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>The main function used to open a position or place a pending order</summary>
        /// <param name='symbol'>Symbol for trading.</param>
        /// <param name='cmd'>Operation type. It can be any of the Trade operation enumeration.</param>
        /// <param name='volume'>Number of lots.</param>
        /// <param name='price'>Preferred price of the trade.</param>
        /// <param name='slippage'>Maximum price slippage for buy or sell orders in points.</param>
        /// <param name='stoploss'>StopLoss level.</param>
        /// <param name='takeprofit'>TakeProfit level.</param>
        /// <param name='comment'>Order comment text. Last part of the comment may be changed by server.</param>
        /// <param name='magic'>Order magic number. May be used as user defined identifier.</param>
        /// <param name='expiration'>Order expiration time (for pending orders only).</param>
        /// <remarks>
        ///     Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional
        ///     error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or
        ///     OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is
        ///     performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or
        ///     MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot
        ///     be applied. If there has not been the requested open price in the price thread or it has not been normalized
        ///     according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the
        ///     requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the
        ///     slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened
        ///     at the current price and only if the current price lies within the range of price+-slippage. StopLoss and
        ///     TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained
        ///     using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop
        ///     levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot
        ///     be too close to the market. The minimal distance of the pending price from the current market one in points can be
        ///     obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a
        ///     pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can
        ///     be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter,
        ///     the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and
        ///     pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order
        ///     will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
        /// </remarks>
        /// <returns>Number of the ticket assigned to the order by the trade server or -1 if it fails</returns>
        /// <exception cref="ErrInvalidFunctionParamvalue"></exception>
        /// <exception cref="ErrCustomIndicatorError"></exception>
        /// <exception cref="ErrStringParameterExpected"></exception>
        /// <exception cref="ErrIntegerParameterExpected"></exception>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
        /// <exception cref="ErrInvalidPriceParam"></exception>
        /// <exception cref="ErrTradeNotAllowed"></exception>
        /// <exception cref="ErrLongsNotAllowed"></exception>
        /// <exception cref="ErrShortsNotAllowed"></exception>
        /// <exception cref="ErrCommonError">Common error</exception>
        /// <exception cref="ErrInvalidTradeParameters">
        ///     Invalid parameters were passed to the trading function, for example, wrong
        ///     symbol, unknown trade operation, negative slippage, non-existing ticket number, etc
        /// </exception>
        /// <exception cref="ErrServerBusy">Trade server is busy</exception>
        /// <exception cref="ErrOldVersion">Old version of the client terminal</exception>
        /// <exception cref="ErrNoConnection">No connection to the trade server</exception>
        /// <exception cref="ErrTooFrequentRequests">Requests are too frequent</exception>
        /// <exception cref="ErrAccountDisabled">The account was disabled</exception>
        /// <exception cref="ErrInvalidAccount">The account number is invalid</exception>
        /// <exception cref="ErrTradeTimeout">Timeout for the trade has been reached</exception>
        /// <exception cref="ErrInvalidPrice">Invalid bid or ask price, perhaps, unnormalized price</exception>
        /// <exception cref="ErrInvalidStops">
        ///     Stops are too close, or prices are ill-calculated or unnormalized (or in the open
        ///     price of a pending order)
        /// </exception>
        /// <exception cref="ErrInvalidTradeVolume">Invalid trade volume, error in the volume granularity</exception>
        /// <exception cref="ErrMarketClosed">Market is closed</exception>
        /// <exception cref="ErrTradeDisabled">Trade is disabled</exception>
        /// <exception cref="ErrNotEnoughMoney">Not enough money to make an operation</exception>
        /// <exception cref="ErrPriceChanged">The price has changed</exception>
        /// <exception cref="ErrOffQuotes">No quotes</exception>
        /// <exception cref="ErrRequote">The requested price has become out of date or bid and ask prices have been mixed up</exception>
        /// <exception cref="ErrOrderLocked">The order has been locked and under processing</exception>
        /// <exception cref="ErrLongPositionsOnlyAllowed">Only buying operation is allowed</exception>
        /// <exception cref="ErrTooManyRequests">Too many requests</exception>
        /// <exception cref="ErrTradeTimeout2">The order has been enqueued</exception>
        /// <exception cref="ErrTradeTimeout3">The order was accepted by the dealer for execution</exception>
        /// <exception cref="ErrTradeTimeout4">The order was discarded by the client during manual confirmation</exception>
        /// <exception cref="ErrTradeModifyDenied">
        ///     Modifying has been denied since the order is too close to market and locked for
        ///     possible soon execution
        /// </exception>
        /// <exception cref="ErrTradeContextBusy">The trade thread is busy</exception>
        /// <exception cref="ErrTradeExpirationDenied">The use of pending order expiration date has been denied by the broker</exception>
        /// <exception cref="ErrTradeTooManyOrders">The amount of open and pending orders has reached the limit set by the broker</exception>
        public async Task<long> OrderSendAsync(
            string symbol,
            TradeOperation cmd,
            double volume,
            double price,
            int slippage,
            double stoploss,
            double takeprofit,
            string comment,
            int magic,
            DateTime expiration,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("94 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) cmd).Append(ArgEndC);
            command.Append(ArgStartC).Append(volume.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(slippage).Append(ArgEndC);
            command.Append(ArgStartC).Append(stoploss.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(takeprofit.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(RemoveSpecialChars(comment)).Append(ArgEndC);
            command.Append(ArgStartC).Append(magic).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (expiration == DateTime.MaxValue ? NoExpiration : expiration).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(timeoutMillis).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderSend(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", cmd=").Append((int) cmd);
                signature.Append(", volume=").Append(volume.ToString(CultureInfo.InvariantCulture));
                signature.Append(", price=").Append(price.ToString(CultureInfo.InvariantCulture));
                signature.Append(", slippage=").Append(slippage);
                signature.Append(", stoploss=").Append(stoploss.ToString(CultureInfo.InvariantCulture));
                signature.Append(", takeprofit=").Append(takeprofit.ToString(CultureInfo.InvariantCulture));
                signature.Append(", comment=").Append(comment);
                signature.Append(", magic=").Append(magic);
                signature.Append(", expiration=").Append(expiration.ToString(YyyyMmDdHhMmSs));
                //signature.Append(", arrowColor=").Append((long) arrowColor);
                signature.Append(')');
                switch (error)
                {
                    case 4051:
                        throw new ErrInvalidFunctionParamvalue(signature.ToString());
                    case 4055:
                        throw new ErrCustomIndicatorError(signature.ToString());
                    case 4062:
                        throw new ErrStringParameterExpected(signature.ToString());
                    case 4063:
                        throw new ErrIntegerParameterExpected(signature.ToString());
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    case 4107:
                        throw new ErrInvalidPriceParam(signature.ToString());
                    case 4109:
                        throw new ErrTradeNotAllowed(signature.ToString());
                    case 4110:
                        throw new ErrLongsNotAllowed(signature.ToString());
                    case 4111:
                        throw new ErrShortsNotAllowed(signature.ToString());
                    case 0:
                        break;
                    case 2:
                        throw new ErrCommonError(signature.ToString());
                    case 3:
                        throw new ErrInvalidTradeParameters(signature.ToString());
                    case 4:
                        throw new ErrServerBusy(signature.ToString());
                    case 5:
                        throw new ErrOldVersion(signature.ToString());
                    case 6:
                        throw new ErrNoConnection(signature.ToString());
                    case 8:
                        throw new ErrTooFrequentRequests(signature.ToString());
                    case 64:
                        throw new ErrAccountDisabled(signature.ToString());
                    case 65:
                        throw new ErrInvalidAccount(signature.ToString());
                    case 128:
                        throw new ErrTradeTimeout(signature.ToString());
                    case 129:
                        throw new ErrInvalidPrice(signature.ToString());
                    case 130:
                        throw new ErrInvalidStops(signature.ToString());
                    case 131:
                        throw new ErrInvalidTradeVolume(signature.ToString());
                    case 132:
                        throw new ErrMarketClosed(signature.ToString());
                    case 133:
                        throw new ErrTradeDisabled(signature.ToString());
                    case 134:
                        throw new ErrNotEnoughMoney(signature.ToString());
                    case 135:
                        throw new ErrPriceChanged(signature.ToString());
                    case 136:
                        throw new ErrOffQuotes(signature.ToString());
                    case 138:
                        throw new ErrRequote(signature.ToString());
                    case 139:
                        throw new ErrOrderLocked(signature.ToString());
                    case 140:
                        throw new ErrLongPositionsOnlyAllowed(signature.ToString());
                    case 141:
                        throw new ErrTooManyRequests(signature.ToString());
                    case 142:
                        throw new ErrTradeTimeout2(signature.ToString());
                    case 143:
                        throw new ErrTradeTimeout3(signature.ToString());
                    case 144:
                        throw new ErrTradeTimeout4(signature.ToString());
                    case 145:
                        throw new ErrTradeModifyDenied(signature.ToString());
                    case 146:
                        throw new ErrTradeContextBusy(signature.ToString());
                    case 147:
                        throw new ErrTradeExpirationDenied(signature.ToString());
                    case 148:
                        throw new ErrTradeTooManyOrders(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = long.Parse(result);
            return res;
        }

        /// <summary>Returns the number of closed orders in the account history loaded into the terminal</summary>
        /// <remarks>The history list size depends on the current settings of the Account history tab of the terminal. </remarks>
        /// <returns>The number of closed orders in the account history loaded into the terminal</returns>
        public async Task<int> OrdersHistoryTotalAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("95 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrdersHistoryTotal(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns stop loss value for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Stop loss value for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderStopLossAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("96 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderStopLoss(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns market and pending orders count</summary>
        /// <returns>Market and pending orders count</returns>
        public async Task<int> OrdersTotalAsync(int magic = 0, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("97 ");
            //
            command.Append(ArgStartC).Append(magic).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrdersTotal(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns swap value for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Swap value for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderSwapAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("98 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderSwap(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns the order symbol value for selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>The order symbol value for selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<string> OrderSymbolAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("99 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderSymbol(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns take profit value for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Take profit value for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<double> OrderTakeProfitAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("100 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderTakeProfit(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns ticket number for the currently selected order</summary>
        /// <remarks>Note: The order must be previously selected by the OrderSelect() function. </remarks>
        /// <returns>Ticket number for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<long> OrderTicketAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("101 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderTicket(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = long.Parse(result);
            return res;
        }

        /// <summary>Returns order operation type for the currently selected order</summary>
        /// <remarks>
        ///     It can be any of the following values: OP_BUY - buying position, OP_SELL - selling position, OP_BUYLIMIT - buy
        ///     limit pending position, OP_BUYSTOP - buy stop pending position, OP_SELLLIMIT - sell limit pending position,
        ///     OP_SELLSTOP - sell stop pending position. Note: order must be selected by OrderSelect() function.
        /// </remarks>
        /// <returns>Order operation type for the currently selected order</returns>
        /// <exception cref="ErrNoOrderSelected"></exception>
        public virtual async Task<TradeOperation> OrderTypeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("102 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderType(");

                signature.Append(')');
                switch (error)
                {
                    case 4105:
                        throw new ErrNoOrderSelected(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = (TradeOperation) int.Parse(result);
            return res;
        }

        /// <summary>Returns TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE</summary>
        /// <returns>TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE</returns>
        public async Task<bool> IsTradeContextBusyAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("103 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsTradeContextBusy(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns the value of the Stop Out level</summary>
        /// <returns>The value of the Stop Out level</returns>
        public async Task<int> AccountStopoutLevelAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("105 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountStopoutLevel(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the calculation mode for the Stop Out level</summary>
        /// <remarks>
        ///     Calculation mode can take the following values: 0 - calculation of percentage ratio between margin and equity;
        ///     1 - comparison of the free margin level to the absolute value.
        /// </remarks>
        /// <returns>The calculation mode for the Stop Out level</returns>
        public async Task<int> AccountStopoutModeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("106 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountStopoutMode(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The MessageBox function creates, displays, and operates message box</summary>
        /// <param name='text'>Text that contains the message to be displayed.</param>
        /// <param name='caption'>
        ///     Text to be displayed in the header of the dialog box. If this parameter is NULL, the expert name
        ///     will be displayed in the header.
        /// </param>
        /// <param name='flags'>
        ///     Flags that determine the type and behavior of the dialog box (see enumeration class
        ///     MessageBoxFlag). They can represent a conbination of flags from the following groups.
        /// </param>
        /// <remarks>
        ///     The message box contains an application-defined message and header, as well as a random combination of
        ///     predefined icons and push buttons. If the function succeeds, the returned value is one of the MessageBox return
        ///     code values.The function cannot be called from custom indicators since they are executed within interface thread
        ///     and may not decelerate it.
        /// </remarks>
        public async Task<int> MessageBoxAsync(
            string text,
            string caption,
            int flags,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("107 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(text)).Append(ArgEndC);
            command.Append(ArgStartC).Append(RemoveSpecialChars(caption)).Append(ArgEndC);
            command.Append(ArgStartC).Append(flags).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("MessageBox(");
                signature.Append("text=").Append(text);
                signature.Append(", caption=").Append(caption);
                signature.Append(", flags=").Append(flags);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the code of the uninitialization reason for the experts, custom indicators, and scripts</summary>
        /// <remarks>
        ///     The returned values can be ones of Uninitialize reason codes. This function can also be called in function
        ///     init() to analyze the reasons for deinitialization of the previous launch.
        /// </remarks>
        /// <returns>The code of the uninitialization reason for the experts, custom indicators, and scripts</returns>
        public async Task<int> UninitializeReasonAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("108 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("UninitializeReason(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns
        ///     FALSE
        /// </summary>
        /// <returns>TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns FALSE</returns>
        public async Task<bool> IsTradeAllowedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("109 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsTradeAllowed(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Checks trade status for the specified symbol at the specified time</summary>
        /// <returns>Returns TRUE if the expert was allowed to trade the symbol at the specified time, otherwise returns FALSE.</returns>
        public async Task<bool> IsTradeAllowedAsync(
            string symbol,
            DateTime testedTime,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("166 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (testedTime == DateTime.MaxValue ? NoExpiration : testedTime).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsTradeAllowed(");
                signature.Append(", symbol=").Append(symbol);
                signature.Append(", testedTime=").Append(testedTime);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Returns all Market Watch symbols market info (currently bid/ask prices).
        /// </summary>
        /// <returns>In case of failure returns empty Dictionary.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<Dictionary<string, SymbolMarketInfo>> SymbolsMarketInfoAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("167 ");
            //
            // ReSharper disable once UnreachableCode
            command.Append(ArgStartC).Append(true /*selected*/ ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SymbolsInfo(");
                signature.Append("selected=").Append(true /*selected*/);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var p = new SDParser(result, '|');
            var count = p.popInt();
            var res = new Dictionary<string, SymbolMarketInfo>(count);
            for (var i = 0; i < count; i++)
            {
                var symbol = p.pop();
                res.Add(symbol, new SymbolMarketInfo(p));
            }
            return res;
        }

        /// <summary>
        ///     Applies a specific template from a specified file to the chart. The command is added to chart message queue
        ///     and executed only after all previous commands have been processed.
        /// </summary>
        /// <param name="chartId">Chart ID. 0 means the current chart.</param>
        /// <param name="fileName">The name of the file containing the template.</param>
        /// <returns>Returns true if the command has been added to chart queue, otherwise false.</returns>
        /// <remarks>
        ///     The Expert Advisor will be unloaded and will not be able to continue operating in case of successful loading
        ///     of a new template to the chart it is attached to.
        /// </remarks>
        /// <remarks>More details here - http://docs.mql4.com/chart_operations/chartapplytemplate</remarks>
        public async Task<bool> ChartApplyTemplateAsync(
            long chartId,
            string fileName,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("170 ");
            //
            command.Append(ArgStartC).Append(chartId).Append(ArgEndC);
            command.Append(ArgStartC).Append(fileName).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ChartApplyTemplate(");
                signature.Append(", chartId=").Append(chartId);
                signature.Append(", fileName=").Append(fileName);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Saves current chart settings in a template with a specified name. The command is added to chart message queue
        ///     and executed only after all previous commands have been processed.
        /// </summary>
        /// <param name="chartId">Chart ID. 0 means the current chart.</param>
        /// <param name="fileName">
        ///     The filename to save the template. The ".tpl" extension will be added to the filename
        ///     automatically, there is no need to specify it. The template is saved in terminal_directory\Profiles\Templates\ and
        ///     can be used for manual application in the terminal. If a template with the same filename already exists, the
        ///     contents of this file will be overwritten.
        /// </param>
        /// <returns>Returns true if the command has been added to chart queue, otherwise false.</returns>
        /// <remarks>
        ///     Using templates, you can save chart settings with all applied indicators and graphical objects, to then apply
        ///     it to another chart.
        /// </remarks>
        /// <remarks>More details here - http://docs.mql4.com/chart_operations/chartsavetemplate</remarks>
        public async Task<bool> ChartSaveTemplateAsync(
            long chartId,
            string fileName,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("171 ");
            //
            command.Append(ArgStartC).Append(chartId).Append(ArgEndC);
            command.Append(ArgStartC).Append(fileName).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ChartSaveTemplate(");
                signature.Append(", chartId=").Append(chartId);
                signature.Append(", fileName=").Append(fileName);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Opens a new chart with the specified symbol and period. The command is added to chart message queue and
        ///     executed only after all previous commands have been processed.
        /// </summary>
        /// <param name="symbol">Chart symbol.</param>
        /// <param name="period">Chart period (timeframe).</param>
        /// <returns>If successful, it returns the opened chart ID. Otherwise returns 0.</returns>
        /// <remarks>More details here - http://docs.mql4.com/chart_operations/chartopen</remarks>
        public async Task<long> ChartOpenAsync(
            string symbol,
            Timeframe period,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("172 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) period).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ChartOpen(");
                signature.Append(", symbol=").Append(symbol);
                signature.Append(", period=").Append((int) period);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = long.Parse(result);
            return res;
        }

        /// <summary>Closes the specified chart.</summary>
        /// <param name="chartId">Chart ID. 0 means the current chart.</param>
        /// <returns>If successful, returns true, otherwise false.</returns>
        /// <remarks>More details here - http://docs.mql4.com/chart_operations/chartclose</remarks>
        public async Task<bool> ChartCloseAsync(
            long chartId,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("173 ");
            //
            command.Append(ArgStartC).Append(chartId).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ChartClose(");
                signature.Append(", chartId=").Append(chartId);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }


        /// <summary>
        ///     Changes the symbol and period of the specified chart. The function is asynchronous, i.e. it sends the command
        ///     and does not wait for its execution completion. The command is added to chart message queue and executed only after
        ///     all previous commands have been processed.
        /// </summary>
        /// <param name="chartId">Chart ID. 0 means the current chart.</param>
        /// <param name="symbol">Chart symbol</param>
        /// <param name="period">Chart period (timeframe)</param>
        /// <returns>If successful, returns true, otherwise false.</returns>
        public async Task<bool> ChartSetSymbolPeriodAsync(
            long chartId,
            string symbol,
            Timeframe period,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("174 ");
            //
            command.Append(ArgStartC).Append(chartId).Append(ArgEndC);
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) period).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ChartSetSymbolPeriod(");
                signature.Append(", chartId=").Append(chartId);
                signature.Append(", symbol=").Append(symbol);
                signature.Append(", period=").Append(period);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE</summary>
        /// <returns>TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE</returns>
        public async Task<bool> IsOptimizationAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("111 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsOptimization(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if the expert can call library function, otherwise returns FALSE</summary>
        /// <remarks>See also IsDllsAllowed(), IsTradeAllowed(). </remarks>
        /// <returns>TRUE if the expert can call library function, otherwise returns FALSE</returns>
        public async Task<bool> IsLibrariesAllowedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("112 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsLibrariesAllowed(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE</summary>
        /// <returns>TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE</returns>
        public async Task<bool> IsDllsAllowedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("113 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsDllsAllowed(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns TRUE if expert advisors are enabled for running, otherwise returns FALSE</summary>
        /// <returns>TRUE if expert advisors are enabled for running, otherwise returns FALSE</returns>
        public async Task<bool> IsExpertEnabledAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("114 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("IsExpertEnabled(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Returns free margin that remains after the specified position has been opened at the current price on the
        ///     current account
        /// </summary>
        /// <param name='symbol'>Symbol for trading operation.</param>
        /// <param name='cmd'>Operation type. It can be either OP_BUY or OP_SELL.</param>
        /// <param name='volume'>Number of lots.</param>
        /// <remarks>If the free margin is insufficient, an error 134 (ERR_NOT_ENOUGH_MONEY) will be generated. </remarks>
        /// <returns>
        ///     Free margin that remains after the specified position has been opened at the current price on the current
        ///     account
        /// </returns>
        public async Task<double> AccountFreeMarginCheckAsync(
            string symbol,
            TradeOperation cmd,
            double volume,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("115 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) cmd).Append(ArgEndC);
            command.Append(ArgStartC).Append(volume.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountFreeMarginCheck(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", cmd=").Append((int) cmd);
                signature.Append(", volume=").Append(volume.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Calculation mode of free margin allowed to open positions on the current account</summary>
        /// <remarks>
        ///     The calculation mode can take the following values: 0 - floating profit/loss is not used for calculation; 1 -
        ///     both floating profit and loss on open positions on the current account are used for free margin calculation; 2 -
        ///     only profit value is used for calculation, the current loss on open positions is not considered; 3 - only loss
        ///     value is used for calculation, the current loss on open positions is not considered.
        /// </remarks>
        public async Task<double> AccountFreeMarginModeAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("116 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountFreeMarginMode(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns leverage of the current account</summary>
        /// <returns>Leverage of the current account</returns>
        public async Task<int> AccountLeverageAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("117 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountLeverage(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the connected server name</summary>
        /// <returns>The connected server name</returns>
        public async Task<string> AccountServerAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("118 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountServer(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns the name of company owning the client terminal</summary>
        /// <returns>The name of company owning the client terminal</returns>
        public async Task<string> TerminalCompanyAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("119 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("TerminalCompany(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns client terminal name</summary>
        /// <remarks> </remarks>
        /// <returns>Client terminal name</returns>
        public async Task<string> TerminalNameAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("120 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("TerminalName(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Returns the directory, from which the client terminal was launched</summary>
        /// <returns>The directory, from which the client terminal was launched</returns>
        public async Task<string> TerminalPathAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("121 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("TerminalPath(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Displays a dialog box containing the user-defined data</summary>
        /// <param name='arg'>Any value.</param>
        public async Task AlertAsync(
            string arg,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("122 ");
            //
            command.Append(ArgStartC).Append(arg).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Alert(");
                signature.Append("arg=").Append(arg);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Function plays a sound file</summary>
        /// <param name='filename'>Path to the sound file.</param>
        /// <remarks>The file must be located in the terminal_dir\sounds directory (see TerminalPath()) or in its subdirectory. </remarks>
        public async Task PlaySoundAsync(
            string filename,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("123 ");
            //
            command.Append(ArgStartC).Append(filename).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("PlaySound(");
                signature.Append("filename=").Append(filename);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Return object description</summary>
        /// <param name='name'>Object name.</param>
        /// <remarks>For objects of OBJ_TEXT and OBJ_LABEL types, the text drawn by these objects will be returned.</remarks>
        public async Task<string> ObjectDescriptionAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("124 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectDescription(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Search for an object having the specified name</summary>
        /// <param name='name'>Object name to search for.</param>
        /// <remarks>
        ///     The function returns index of the windows that contains the object to be found. If it fails, the returned
        ///     value will be -1. To get the detailed error information, one has to call the GetLastError() function. The chart
        ///     sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main
        ///     window always exists and has the 0 index.
        /// </remarks>
        public async Task<int> ObjectFindAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("125 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectFind(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The function calculates and returns bar index (shift related to the current bar) for the given price</summary>
        /// <param name='name'>Object name.</param>
        /// <param name='value'>Price value.</param>
        /// <remarks>
        ///     The bar index is calculated by the first and second coordinates using a linear equation. Applied to trendlines
        ///     and similar objects. To get the detailed error information, one has to call the GetLastError() function.
        /// </remarks>
        public async Task<int> ObjectGetShiftByValueAsync(
            string name,
            double value,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("126 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(value.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectGetShiftByValue(");
                signature.Append("name=").Append(name);
                signature.Append(", value=").Append(value.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The function calculates and returns the price value for the specified bar (shift related to the current bar)</summary>
        /// <param name='name'>Object name.</param>
        /// <param name='shift'>Bar index.</param>
        /// <remarks>
        ///     The price value is calculated by the first and second coordinates using a linear equation. Applied to
        ///     trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
        /// </remarks>
        public async Task<double> ObjectGetValueByShiftAsync(
            string name,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("127 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectGetValueByShift(");
                signature.Append("name=").Append(name);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>The function moves an object coordinate in the chart</summary>
        /// <param name='name'>Object name.</param>
        /// <param name='point'>Coordinate index (0-2).</param>
        /// <param name='time1'>New time value.</param>
        /// <param name='price1'>New price value.</param>
        /// <remarks>
        ///     Objects can have from one to three coordinates depending on their types. If the function succeeds, the
        ///     returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call
        ///     the GetLastError() function. The object coordinates are numbered starting from 0.
        /// </remarks>
        public async Task<bool> ObjectMoveAsync(
            string name,
            int point,
            DateTime time1,
            double price1,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("128 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(point).Append(ArgEndC);
            command.Append(ArgStartC).Append(time1.ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(price1.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectMove(");
                signature.Append("name=").Append(name);
                signature.Append(", point=").Append(point);
                signature.Append(", time1=").Append(time1.ToString(YyyyMmDdHhMmSs));
                signature.Append(", price1=").Append(price1.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>The function returns the object name by its index in the objects list</summary>
        /// <param name='index'>
        ///     Object index in the objects list. Object index must exceed or equal to 0 and be less than
        ///     ObjectsTotal().
        /// </param>
        /// <remarks>To get the detailed error information, one has to call the GetLastError() function.</remarks>
        public async Task<string> ObjectNameAsync(
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("129 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectName(");
                signature.Append("index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Removes all objects of the specified type and in the specified sub-window of the chart</summary>
        /// <param name='window'>
        ///     Index of the window in which the objects will be deleted. Must exceed or equal to -1 (EMPTY, the
        ///     default value) and be less than WindowsTotal().
        /// </param>
        /// <param name='type'>
        ///     An object type to be deleted. It can be any of the Object type enumeration values or EMPTY constant
        ///     to delete all objects with any types.
        /// </param>
        /// <remarks>
        ///     The function returns the count of removed objects. To get the detailed error information, one has to call the
        ///     GetLastError() function. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are
        ///     numbered starting from 1. The chart main window always exists and has the 0 index. If the window index is missing
        ///     or it has the value of -1, the objects will be removed from the entire chart. If the type value equals to -1 or
        ///     this parameter is missing, all objects will be removed from the specified sub-window.
        /// </remarks>
        public async Task<int> ObjectsDeleteAllAsync(
            int window,
            int type,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("130 ");
            //
            command.Append(ArgStartC).Append(window).Append(ArgEndC);
            command.Append(ArgStartC).Append(type).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ObjectsDeleteAll(");
                signature.Append("window=").Append(window);
                signature.Append(", type=").Append(type);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Calculates the Ichimoku Kinko Hyo and returns its value</summary>
        /// <param name='symbol'>
        ///     Symbol name of the security on the data of which the indicator will be calculated. NULL means the
        ///     current symbol.
        /// </param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <param name='tenkan_sen'>Tenkan Sen averaging period.</param>
        /// <param name='kijun_sen'>Kijun Sen averaging period.</param>
        /// <param name='senkou_span_b'>Senkou SpanB averaging period.</param>
        /// <param name='mode'>Source of data. It can be one of the Ichimoku Kinko Hyo mode enumeration.</param>
        /// <param name='shift'>
        ///     Index of the value taken from the indicator buffer (shift relative to the current bar the given
        ///     amount of periods ago).
        /// </param>
        /// <exception cref="ErrUnknownSymbol">Unknown symbol..</exception>
// ReSharper disable InconsistentNaming
        public async Task<double> iIchimokuAsync(
            string symbol,
            Timeframe timeframe,
            int tenkan_sen,
            int kijun_sen,
            int senkou_span_b,
// ReSharper restore InconsistentNaming
            IchimokuSource mode,
            int shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("131 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(tenkan_sen).Append(ArgEndC);
            command.Append(ArgStartC).Append(kijun_sen).Append(ArgEndC);
            command.Append(ArgStartC).Append(senkou_span_b).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) mode).Append(ArgEndC);
            command.Append(ArgStartC).Append(shift).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("iIchimoku(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(", tenkan_sen=").Append(tenkan_sen);
                signature.Append(", kijun_sen=").Append(kijun_sen);
                signature.Append(", senkou_span_b=").Append(senkou_span_b);
                signature.Append(", mode=").Append((int) mode);
                signature.Append(", shift=").Append(shift);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>The function sets a flag hiding indicators called by the Expert Advisor</summary>
        /// <param name='shift'>TRUE, if there is a need to hide indicators, or else FALSE.</param>
        /// <remarks>
        ///     After the expert has been tested and the appropriate chart opened, the flagged indicators will not be drawn in
        ///     the testing chart. Every indicator called will first be flagged with the current hiding flag. It must be noted that
        ///     only those indicators can be drawn in the testing chart that are directly called from the expert under test.
        /// </remarks>
        public async Task HideTestIndicatorsAsync(
            bool shift,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("132 ");
            //
            command.Append(ArgStartC).Append(shift ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("HideTestIndicators(");
                signature.Append("shift=").Append(shift ? 1 : 0);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Returns the amount of minutes determining the used period (chart timeframe)</summary>
        /// <returns>The amount of minutes determining the used period (chart timeframe)</returns>
        public async Task<Timeframe> PeriodAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("133 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Period(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = (Timeframe) int.Parse(result);
            return res;
        }

        /// <summary>Returns a text string with the name of the current financial instrument</summary>
        /// <returns>A text string with the name of the current financial instrument</returns>
        public async Task<string> SymbolAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("134 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("Symbol(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Function returns the amount of bars visible on the chart</summary>
        /// <remarks> </remarks>
        public async Task<int> WindowBarsPerChartAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("135 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowBarsPerChart(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The function returns the first visible bar number in the current chart window</summary>
        /// <remarks>
        ///     It must be taken into consideration that price bars are numbered in the reverse order, from the last to the
        ///     first one. The current bar, the latest in the price array, is indexed as 0. The oldest bar is indexed as Bars-1. If
        ///     the first visible bar number is 2 or more bars less than the amount of visible bars in the chart, it means that the
        ///     chart window has not been fully filled out and there is a space to the left.
        /// </remarks>
        public async Task<int> WindowFirstVisibleBarAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("136 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowFirstVisibleBar(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from
        ///     which this function has been called
        /// </summary>
        /// <returns>
        ///     Name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from which
        ///     this function has been called
        /// </returns>
        public async Task<string> WindowExpertNameAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("137 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowExpertName(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>
        ///     If indicator with name was found, the function returns the window index containing this specified indicator,
        ///     otherwise it returns -1
        /// </summary>
        /// <param name='name'>Indicator short name.</param>
        /// <remarks>Note: WindowFind() returns -1 if custom indicator searches itself when init() function works. </remarks>
        public async Task<int> WindowFindAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("138 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowFind(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns TRUE if the chart subwindow is visible, otherwise returns FALSE</summary>
        /// <param name='index'>Chart subwindow index.</param>
        /// <remarks>The chart subwindow can be hidden due to the visibility properties of the indicator placed in it. </remarks>
        /// <returns>TRUE if the chart subwindow is visible, otherwise returns FALSE</returns>
        public async Task<bool> WindowIsVisibleAsync(
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("139 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowIsVisible(");
                signature.Append("index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Returns maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart
        ///     window, the indicators' subwindows are numbered starting from 1)
        /// </summary>
        /// <param name='index'>Chart subwindow index (0 - main chart window).</param>
        /// <remarks>
        ///     If the subwindow index has not been specified, the maximal value of the price scale of the main chart window
        ///     is returned.
        /// </remarks>
        /// <returns>
        ///     Maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the
        ///     indicators' subwindows are numbered starting from 1)
        /// </returns>
        public async Task<double> WindowPriceMaxAsync(
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("140 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowPriceMax(");
                signature.Append("index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>
        ///     Returns minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart
        ///     window, the indicators' subwindows are numbered starting from 1)
        /// </summary>
        /// <param name='index'>Chart subwindow index (0 - main chart window).</param>
        /// <remarks>
        ///     If the subwindow index has not been specified, the minimal value of the price scale of the main chart window
        ///     is returned.
        /// </remarks>
        /// <returns>
        ///     Minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the
        ///     indicators' subwindows are numbered starting from 1)
        /// </returns>
        public async Task<double> WindowPriceMinAsync(
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("141 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowPriceMin(");
                signature.Append("index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns window index where expert, custom indicator or script was dropped</summary>
        /// <remarks>
        ///     This value is valid if the expert, custom indicator or script was dropped by mouse. Note: For custom
        ///     indicators being initialized (call from the init() function), this index is not defined. The returned index is the
        ///     number of window (0-chart main menu, subwindows of indicators are numbered starting from 1) where the custom
        ///     indicator is working. A custom indicator can create its own new subwindow during its work, and the number of this
        ///     subwindow will differ from that of the window where the indicator was really dropped in.
        /// </remarks>
        /// <returns>Window index where expert, custom indicator or script was dropped</returns>
        public async Task<int> WindowOnDroppedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("142 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowOnDropped(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the value at X axis in pixels for the chart window client area point at which the expert or script was
        ///     dropped
        /// </summary>
        /// <remarks>The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.</remarks>
        /// <returns>The value at X axis in pixels for the chart window client area point at which the expert or script was dropped</returns>
        public async Task<int> WindowXOnDroppedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("143 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowXOnDropped(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the value at Y axis in pixels for the chart window client area point at which the expert or script was
        ///     dropped
        /// </summary>
        /// <remarks>The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.</remarks>
        /// <returns>The value at Y axis in pixels for the chart window client area point at which the expert or script was dropped</returns>
        public async Task<int> WindowYOnDroppedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("144 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowYOnDropped(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns the price part of the chart point where expert or script was dropped</summary>
        /// <remarks>
        ///     This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value
        ///     is undefined.
        /// </remarks>
        /// <returns>The price part of the chart point where expert or script was dropped</returns>
        public async Task<double> WindowPriceOnDroppedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("145 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowPriceOnDropped(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>Returns the time part of the chart point where expert or script was dropped</summary>
        /// <remarks>
        ///     This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value
        ///     is undefined.
        /// </remarks>
        /// <returns>The time part of the chart point where expert or script was dropped</returns>
        public async Task<DateTime> WindowTimeOnDroppedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("146 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowTimeOnDropped(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>Returns count of indicator windows on the chart (including main chart)</summary>
        /// <returns>Count of indicator windows on the chart (including main chart)</returns>
        public async Task<int> WindowsTotalAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("147 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowsTotal(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Redraws the current chart forcedly</summary>
        /// <remarks>It is normally used after the objects properties have been changed. </remarks>
        public async Task WindowRedrawAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("148 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowRedraw(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
        }

        /// <summary>Saves current chart screen shot as a GIF file</summary>
        /// <param name='filename'>Screen shot file name.</param>
        /// <param name='sizeX'>Screen shot width in pixels.</param>
        /// <param name='sizeY'>Screen shot height in pixels.</param>
        /// <param name='startBar'>
        ///     Index of the first visible bar in the screen shot. If 0 value is set, the current first visible
        ///     bar will be shot. If no value or negative value has been set, the end-of-chart screen shot will be produced, indent
        ///     being taken into consideration.
        /// </param>
        /// <param name='chartScale'>
        ///     Horizontal chart scale for screen shot. Can be in the range from 0 to 5. If no value or
        ///     negative value has been set, the current chart scale will be used.
        /// </param>
        /// <param name='chartMode'>
        ///     Chart displaying mode. It can take the following values: CHART_BAR (0 is a sequence of bars),
        ///     CHART_CANDLE (1 is a sequence of candlesticks), CHART_LINE (2 is a close prices line). If no value or negative
        ///     value has been set, the chart will be shown in its current mode.
        /// </param>
        /// <remarks>
        ///     Returns FALSE if it fails. To get the error code, one has to use the GetLastError() function. The screen shot
        ///     is saved in the terminal_dir\experts\files (terminal_dir\tester\files in case of testing) directory or its
        ///     subdirectories.
        /// </remarks>
        /// <returns>FALSE if it fails</returns>
        public async Task<bool> WindowScreenShotAsync(
            string filename,
            int sizeX,
            int sizeY,
            int startBar,
            int chartScale,
            int chartMode,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("149 ");
            //
            command.Append(ArgStartC).Append(filename).Append(ArgEndC);
            command.Append(ArgStartC).Append(sizeX).Append(ArgEndC);
            command.Append(ArgStartC).Append(sizeY).Append(ArgEndC);
            command.Append(ArgStartC).Append(startBar).Append(ArgEndC);
            command.Append(ArgStartC).Append(chartScale).Append(ArgEndC);
            command.Append(ArgStartC).Append(chartMode).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowScreenShot(");
                signature.Append("filename=").Append(filename);
                signature.Append(", sizeX=").Append(sizeX);
                signature.Append(", sizeY=").Append(sizeY);
                signature.Append(", startBar=").Append(startBar);
                signature.Append(", chartScale=").Append(chartScale);
                signature.Append(", chartMode=").Append(chartMode);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns the system window handler containing the given chart</summary>
        /// <param name='symbol'>symbol name.</param>
        /// <param name='timeframe'>Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.</param>
        /// <remarks>
        ///     If the chart of symbol and timeframe has not been opened by the moment of function calling, 0 will be
        ///     returned.
        /// </remarks>
        /// <returns>The system window handler containing the given chart</returns>
        public async Task<int> WindowHandleAsync(
            string symbol,
            Timeframe timeframe,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("150 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("WindowHandle(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append((int) timeframe);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>Returns TRUE if the global variable exists, otherwise, returns FALSE</summary>
        /// <param name='name'>Global variable name.</param>
        /// <returns>TRUE if the global variable exists, otherwise, returns FALSE</returns>
        public async Task<bool> GlobalVariableCheckAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("151 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableCheck(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Deletes the global variable</summary>
        /// <param name='name'>Global variable name.</param>
        /// <remarks>If the function succeeds, the returned value will be TRUE, otherwise, it will be FALSE.</remarks>
        public async Task<bool> GlobalVariableDelAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("152 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableDel(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Returns the value of an existing global variable or 0 if an error occurs</summary>
        /// <param name='name'>Global variable name.</param>
        /// <returns>The value of an existing global variable or 0 if an error occurs</returns>
        public async Task<double> GlobalVariableGetAsync(
            string name,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("153 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableGet(");
                signature.Append("name=").Append(name);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = double.Parse(result, CultureInfo.InvariantCulture);
            return res;
        }

        /// <summary>The function returns the name of a global variable by its index in the list of global variables</summary>
        /// <param name='index'>
        ///     Index in the list of global variables. It must exceed or be equal to 0 and be less than
        ///     GlobalVariablesTotal().
        /// </param>
        public async Task<string> GlobalVariableNameAsync(
            int index,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("154 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableName(");
                signature.Append("index=").Append(index);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result;
            return res;
        }

        /// <summary>Sets a new value of the global variable</summary>
        /// <param name='name'>Global variable name.</param>
        /// <param name='value'>The new numeric value.</param>
        /// <remarks>
        ///     If it does not exist, the system creates a new gloabl variable. If the function succeeds, the returned value
        ///     will be the last access time. Otherwise, the returned value will be 0.
        /// </remarks>
        public async Task<DateTime> GlobalVariableSetAsync(
            string name,
            double value,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("155 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(value.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableSet(");
                signature.Append("name=").Append(name);
                signature.Append(", value=").Append(value.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = ToDate(double.Parse(result, CultureInfo.InvariantCulture));
            return res;
        }

        /// <summary>
        ///     Sets the new value of the existing global variable if the current value equals to the third parameter
        ///     check_value
        /// </summary>
        /// <param name='name'>Global variable name.</param>
        /// <param name='value'>The new numeric value.</param>
        /// <param name='checkValue'>Value to be compared to the current global variable value.</param>
        /// <remarks>When successfully executed, the function returns TRUE, otherwise, it returns FALSE.</remarks>
        /// <exception cref="ErrGlobalVariableNotFound">Not existent global variable name was used..</exception>
        public async Task<bool> GlobalVariableSetOnConditionAsync(
            string name,
            double value,
            double checkValue,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("156 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(name)).Append(ArgEndC);
            command.Append(ArgStartC).Append(value.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            command.Append(ArgStartC).Append(checkValue.ToString(CultureInfo.InvariantCulture)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariableSetOnCondition(");
                signature.Append("name=").Append(name);
                signature.Append(", value=").Append(value.ToString(CultureInfo.InvariantCulture));
                signature.Append(", check_value=").Append(checkValue.ToString(CultureInfo.InvariantCulture));
                signature.Append(')');
                switch (error)
                {
                    case 4058:
                        throw new ErrGlobalVariableNotFound(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>Deletes global variables</summary>
        /// <param name='prefix'>Name prefix of the global variables to be deleted.</param>
        /// <remarks>
        ///     If the name prefix is not specified, all global variables will be deleted. Otherwise, only those variables
        ///     will be deleted, the names of which begin with the specified prefix. The function returns the count of deleted
        ///     variables.
        /// </remarks>
        public async Task<int> GlobalVariablesDeleteAllAsync(
            string prefix,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("157 ");
            //
            command.Append(ArgStartC).Append(RemoveSpecialChars(prefix)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariablesDeleteAll(");
                signature.Append("prefix=").Append(prefix);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>The function returns the total count of global variables</summary>
        public async Task<int> GlobalVariablesTotalAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("158 ");
            //

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GlobalVariablesTotal(");

                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the number of available (selected in Market Watch or all) symbols.
        /// </summary>
        /// <param name="selected">Request mode. Can be true or false: true - return number of symbols in MarketWatch</param>
        /// <returns>
        ///     If the 'selected' parameter is true, the function returns the number of symbols selected in MarketWatch. If
        ///     the value is false, it returns the total number of all symbols.
        /// </returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<int> SymbolsTotalAsync(
            bool selected,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("159 ");
            //
            command.Append(ArgStartC).Append(selected ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SymbolsTotal(");
                signature.Append("selected=").Append(selected);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Returns the name of a symbol.
        /// </summary>
        /// <param name="pos">Order number of a symbol.</param>
        /// <param name="selected">
        ///     Request mode. If the value is true, the symbol is taken from the list of symbols selected in
        ///     MarketWatch. If the value is false, the symbol is taken from the general list.
        /// </param>
        /// <returns>Value of string type with the symbol name.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<string> SymbolNameAsync(
            int pos,
            bool selected,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("160 ");
            //
            command.Append(ArgStartC).Append(pos).Append(ArgEndC);
            command.Append(ArgStartC).Append(selected ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SymbolName(");
                signature.Append("pos=").Append(pos);
                signature.Append("selected=").Append(selected);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            return result;
        }

        /// <summary>
        ///     Selects a symbol in the Market Watch window or removes a symbol from the window.
        /// </summary>
        /// <param name="symbol">Symbol name.</param>
        /// <param name="select">
        ///     Switch. If the value is false, a symbol should be removed from MarketWatch, otherwise a symbol
        ///     should be selected in this window. A symbol can't be removed if the symbol chart is open, or there are open orders
        ///     for this symbol.
        /// </param>
        /// <returns>In case of failure returns false.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<bool> SymbolSelectAsync(
            string symbol,
            bool @select,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("161 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append(@select ? 1 : 0).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SymbolSelect(");
                signature.Append("symbol=").Append(symbol);
                signature.Append("selected=").Append(@select);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Passes stop request to the terminal application.
        /// </summary>
        /// <param name="exitCode">application exit code.</param>
        /// <returns>In case of failure returns false.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<bool> TerminalCloseAsync(int exitCode, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("162 ");
            //
            command.Append(ArgStartC).Append(exitCode).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("TerminalClose(");
                signature.Append("exitCode=").Append(exitCode);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var res = result.Length > 1 ? bool.Parse(result) : int.Parse(result) != 0;
            return res;
        }

        /// <summary>
        ///     Obtains current market information for the symbol
        /// </summary>
        /// <param name="symbol">symbol to obtain market information for</param>
        /// <returns>current market information for the specified symbol.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<SymbolInfo> SymbolInfoAsync(string symbol, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("163 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("SymbolInfo(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            return new SymbolInfo(this, ix >= 0 ? result.Substring(0, ix) : result);
        }

        /// <summary>
        ///     Obtains information about connected account, e.g. name, server currency, company, balance, credit, profit, equity,
        ///     margin, freemargin, trade_mode, leverage, margin_so_mode
        /// </summary>
        /// <returns>connected account information</returns>
        /// <exception cref="MT4Exception">common error</exception>
        public async Task<AccountInfo> AccountInfoAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("164 ");
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("AccountInfo(");
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            return new AccountInfo(this, ix >= 0 ? result.Substring(0, ix) : result);
        }

        /// <summary>
        ///     Offset in seconds between current server time and GMT time, so {server time} + {offset} = {gmt time}
        /// </summary>
        /// <returns>seconds between current server time and GMT time.</returns>
        /// <exception cref="MT4Exception">common error</exception>
        // ReSharper disable once InconsistentNaming
        public async Task<int> ServerTimeGMTOffsetAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("165 ");
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("ServerTimeGMTOffset(");
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = int.Parse(result);
            return res;
        }

        /// <summary>
        ///     Converts server time to default time zone.
        /// </summary>
        /// <param name="serverTime">time at mt4 server, e.g TimeCurrent()</param>
        /// <returns>Date representing local time for a specified server time.</returns>
        public async Task<DateTime> ToLocalTimeAsync(DateTime serverTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return
                new DateTimeOffset(DateTime.SpecifyKind(serverTime, DateTimeKind.Unspecified), TimeSpan.FromSeconds(-await ServerTimeGMTOffsetCachedAsync(timeoutMillis, ct).ConfigureAwait(false))).ToLocalTime()
                    .DateTime;
        }

        /// <summary>
        ///     Converts server time to GMT time zone.
        /// </summary>
        /// <param name="serverTime">time at mt4 server, e.g TimeCurrent()</param>
        /// <returns>Date representing UTC time for a specified server time.</returns>
        public async Task<DateTime> ToUniversalTimeAsync(DateTime serverTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return DateTime.SpecifyKind(
                new DateTimeOffset(DateTime.SpecifyKind(serverTime, DateTimeKind.Unspecified), TimeSpan.FromSeconds(-await ServerTimeGMTOffsetCachedAsync(timeoutMillis, ct).ConfigureAwait(false)))
                .ToUniversalTime()
                    .DateTime, DateTimeKind.Utc);
        }

        /// <summary>
        ///     Converts GMT time to server time.
        /// </summary>
        /// <param name="utcTime">UTC time</param>
        /// <returns>Date representing server time.</returns>
        public async Task<DateTime> FromUniversalTimeAsync(DateTime utcTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return
                new DateTimeOffset(DateTime.SpecifyKind(utcTime, DateTimeKind.Unspecified), TimeSpan.Zero).ToOffset(TimeSpan.FromSeconds(-await ServerTimeGMTOffsetCachedAsync(timeoutMillis, ct).ConfigureAwait(false)))
                    .DateTime;
        }

        /// <summary>
        ///     Converts server time to default time zone.
        /// </summary>
        /// <param name="localTime">time at mt4 server, e.g TimeCurrent()</param>
        /// <returns>Date representing local time for a specified server time.</returns>
        public async Task<DateTime> ToServerTimeAsync(DateTime localTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return new DateTimeOffset(localTime).ToOffset(TimeSpan.FromSeconds(-await ServerTimeGMTOffsetCachedAsync(timeoutMillis, ct).ConfigureAwait(false))).DateTime;
        }

        /// <summary>
        ///     Cached version of <see cref="ServerTimeGMTOffsetAsync" />, updated once per hour
        /// </summary>
        /// <returns></returns>
        public async Task<int> ServerTimeGMTOffsetCachedAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var now = DateTime.Now;
            if ((now - _lastCacheTime).TotalMinutes > 3600)
            {
                _gmtOffset = await ServerTimeGMTOffsetAsync(timeoutMillis, ct).ConfigureAwait(false);
                _lastCacheTime = now;
            }
            return _gmtOffset;
        }

        /// <summary>
        ///     Returns new tick information (time,bid,ask,orders P/L) for the specified symbol.
        /// </summary>
        /// <param name="symbol"></param>
        /// <param name="lastTick"></param>
        /// <returns></returns>
        /// <exception cref="ErrUnknownSymbol"></exception>
        /// <exception cref="MT4Exception"></exception>
        public async Task<TickInfo> NewTickAsync(
            string symbol,
            TickInfo lastTick,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("10002 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                lastTick == null || lastTick.Time == DateTime.MinValue
                    ? 0
                    : (lastTick.Time.Ticks - D1970Ticks)/10000000L
                ).Append(ArgEndC);
            command.Append(ArgStartC)
                .Append((lastTick?.Bid ?? 0).ToString(CultureInfo.InvariantCulture))
                .Append(ArgEndC);
            command.Append(ArgStartC)
                .Append((lastTick?.Ask ?? 0).ToString(CultureInfo.InvariantCulture))
                .Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("NewTick(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(')');
                switch (error)
                {
                    case 4106:
                    case 54301:
                        throw new ErrUnknownSymbol(signature.ToString());
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var mi = result.Split('|');
            var p = 0;
            var orderPlMap = new Hashtable();
            var res = new TickInfo(
                ToDate(double.Parse(mi[p++], CultureInfo.InvariantCulture)),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                double.Parse(mi[p++], CultureInfo.InvariantCulture),
                orderPlMap
                );
            var max = int.Parse(mi[p++], CultureInfo.InvariantCulture);
            for (var i = 0; i < max; ++i)
            {
                orderPlMap.Add(
                    long.Parse(mi[p++]),
                    double.Parse(mi[p++], CultureInfo.InvariantCulture)
                    );
            }
            //
            return res;
        }

        /// <summary>
        ///     Returns available ticks for market watch selected symbols.
        /// </summary>
        /// <returns>new ticks for the symbols from MarketWatch</returns>
        /// <exception cref="MT4Exception"></exception>
        /// <seealso cref="SymbolSelectAsync" />
        public async Task<List<Tick>> GetTicksAsync(int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("10012 ");
            //
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("GetTicks(");
                signature.Append(')');
                throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                              CheckHttpDocsMql4ComRuntimeErrorsForDetails);
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            var mi = result.Split('|');
            var r = new List<Tick>();
            for (var i = 0; i < mi.Length;)
            {
                var t = new Tick(mi[i++], ToDate(double.Parse(mi[i++], CultureInfo.InvariantCulture)),
                    double.Parse(mi[i++], CultureInfo.InvariantCulture),
                    double.Parse(mi[i++], CultureInfo.InvariantCulture));
                r.Add(t);
            }
            //
            return r;
        }

        /// <summary>The function selects available order information.</summary>
        /// <param name='index'>Order index or order ticket depending on the second parameter.</param>
        /// <param name='select'>
        ///     Selecting flags. It can be any of the following values: SELECT_BY_POS - index in the order pool,
        ///     SELECT_BY_TICKET - index is order ticket.
        /// </param>
        /// <param name='pool'>
        ///     Optional order pool index. Used when the selected parameter is SELECT_BY_POS. It can be any of the
        ///     following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY
        ///     - order selected from history pool (closed and canceled order).
        /// </param>
        /// <remarks>It returns order information if the function succeeds or <b>null</b> if the function fails. </remarks>
        public async Task<IOrderInfo> OrderGetAsync(
            long index,
            SelectionType select,
            SelectionPool pool,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("10003 ");
            //
            command.Append(ArgStartC).Append(index).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) @select).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) pool).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderGet(");
                signature.Append("index=").Append(index);
                signature.Append(", select=").Append((int) @select);
                signature.Append(", pool=").Append((int) pool);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            if (result.Length <= 1)
            {
                // "0" unsuccessful orderSelect
                return null;
            }
            //
            return new OrderImpl(new DDParser(result, ArgStartC, ArgEndC).pop(), this);
        }

        /// <summary>
        ///     This function returns information about created, modified, closed or deleted orders. It blocks until such
        ///     information arrives.
        /// </summary>
        /// <param name="prevPositionInfo">position information to base modifications calculatation at</param>
        /// <returns>New information about created, modified, closed or deleted orders.</returns>
        public async Task<IPositionInfo> NewPositionAsync(
            IPositionInfo prevPositionInfo,
            int timeoutMillis = -1, CancellationToken ct = default(CancellationToken)
            )
        {
            var command = new StringBuilder("10004 ");
            //
            command.Append(ArgStartC).Append(((PositionImpl) prevPositionInfo)?.GetTCount() ?? -1).Append(ArgEndC);
            command.Append(ArgStartC).Append(((PositionImpl) prevPositionInfo)?.GetHCount() ?? -1).Append(ArgEndC);

            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("NewPosition(");
                signature.Append("prevPosition=").Append(prevPositionInfo);
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = new PositionImpl(result, this);
            //
            return res;
        }

        /// <summary>The function provides information about all orders from the selected pool.</summary>
        /// <param name='pool'>
        ///     order pool index, it can be any of the following values: MODE_TRADES (default)- order selected from
        ///     trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled
        ///     order).
        /// </param>
        /// <remarks>It returns information about all orders from the selected pool.</remarks>
        public Task<Dictionary<long, IOrderInfo>> OrderGetAllAsync(SelectionPool pool, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return OrderGetAllAsync(pool, NotDefined, NotDefined, timeoutMillis, ct);
        }

        /// <summary>The function provides information about all orders from the selected pool.</summary>
        /// <param name='pool'>
        ///     order pool index, it can be any of the following values: MODE_TRADES (default)- order selected from
        ///     trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled
        ///     order).
        /// </param>
        /// <param name="dateFrom">
        ///     filter orders by open date (for live orders) or close date (for historical orders).
        ///     <see cref="MT4.NotDefined" /> may be used to weak this filter parameter.
        /// </param>
        /// <param name="dateTo">
        ///     filter orders by open date (for live orders) or close date (for historical orders).
        ///     <see cref="MT4.NotDefined" /> may be used to weak this filter parameter.
        /// </param>
        /// <remarks>It returns information about all orders from the selected pool.</remarks>
        public async Task<Dictionary<long, IOrderInfo>> OrderGetAllAsync(SelectionPool pool, DateTime dateFrom, DateTime dateTo, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("10005 ");
            //
            command.Append(ArgStartC).Append((int) pool).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (dateFrom == DateTime.MaxValue ? NoExpiration : dateFrom).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (dateTo == DateTime.MaxValue ? NoExpiration : dateTo).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("OrderGetAll(");
                signature.Append("pool=").Append((int) pool);
                signature.Append(", dateFrom=").Append(dateFrom.ToString(YyyyMmDdHhMmSs));
                signature.Append(", dateTo=").Append(dateTo.ToString(YyyyMmDdHhMmSs));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            var res = new PositionImpl(result, this);
            //
            return pool == SelectionPool.MODE_TRADES ? res.LiveOrders : res.HistoricalOrders;
        }

        /// <summary>
        ///     Gets history data of <see cref="Rate" /> structure of a specified symbol-period
        ///     in specified quantity. The elements ordering of the copied data is from present to the past,
        ///     i.e., starting position of 0 means the current bar.
        /// </summary>
        /// <param name="symbol">Symbol name</param>
        /// <param name="timeframe">Period</param>
        /// <param name="startPos">The start position for the first element to copy.</param>
        /// <param name="count">Data count to copy.</param>
        /// <param name="startTime">Bar time for the first element to copy.</param>
        /// <param name="stopTime">Bar time, corresponding to the last element to copy.</param>
        /// <remarks>
        ///     History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest
        ///     (last element)
        /// </remarks>
        private async Task<List<Rate>> CopyRatesAsync(string symbol, Timeframe timeframe, int startPos, int count, DateTime startTime,
            DateTime stopTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var command = new StringBuilder("10006 ");
            //
            command.Append(ArgStartC).Append(symbol).Append(ArgEndC);
            command.Append(ArgStartC).Append((int) timeframe).Append(ArgEndC);
            command.Append(ArgStartC).Append(startPos).Append(ArgEndC);
            command.Append(ArgStartC).Append(count).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (startTime == DateTime.MaxValue ? NoExpiration : startTime).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            command.Append(ArgStartC).Append(
                (stopTime == DateTime.MaxValue ? NoExpiration : stopTime).ToString(YyyyMmDdHhMmSs)).Append(ArgEndC);
            //
            var result = await AsyncStrategyRunner.SendCommandGetResultAsync(command, timeoutMillis, ct).ConfigureAwait(false);
            var ix = result.LastIndexOf('@');
            var error = ix >= 0 ? int.Parse(result.Substring(ix + 1)) : 0;
            if (error != 0)
            {
                var signature = new StringBuilder("CopyRates(");
                signature.Append("symbol=").Append(symbol);
                signature.Append(", timeframe=").Append(timeframe);
                signature.Append(", startPos=").Append(startPos);
                signature.Append(", count=").Append(count);
                signature.Append(", dateFrom=").Append(startTime.ToString(YyyyMmDdHhMmSs));
                signature.Append(", dateTo=").Append(stopTime.ToString(YyyyMmDdHhMmSs));
                signature.Append(')');
                switch (error)
                {
                    default:
                        if (error != 0)
                        {
                            throw new MT4Exception(error, signature + " -> " + UnexpectedErrorOccurred + error +
                                                          CheckHttpDocsMql4ComRuntimeErrorsForDetails);
                        }
                        break;
                }
            }
            //
            result = ix >= 0 ? result.Substring(0, ix) : result;
            //
            // decompose result
            //
            double bid;
            double ask;
            var res = Rate.Decode(result, this, out bid, out ask);
            //
            return res;
        }

        /// <summary>
        ///     Gets history data of <see cref="Rate" /> structure of a specified symbol-period
        ///     in specified quantity. The elements ordering of the copied data is from present to the past,
        ///     i.e., starting position of 0 means the current bar.
        /// </summary>
        /// <param name="symbol">Symbol name</param>
        /// <param name="timeframe">Period</param>
        /// <param name="startPos">The start position for the first element to copy.</param>
        /// <param name="count">Data count to copy.</param>
        /// <returns>
        ///     History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest
        ///     (last element)
        /// </returns>
        public Task<List<Rate>> CopyRatesAsync(string symbol, Timeframe timeframe, int startPos, int count, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return CopyRatesAsync(symbol, timeframe, startPos, count, NotDefined, NotDefined, timeoutMillis, ct);
        }

        /// <summary>
        ///     Gets history data of <see cref="Rate" /> structure of a specified symbol-period
        ///     in specified quantity. The elements ordering of the copied data is from present to the past,
        ///     i.e., starting position of 0 means the current bar.
        /// </summary>
        /// <param name="symbol">Symbol name</param>
        /// <param name="timeframe">Period</param>
        /// <param name="startTime">Bar time for the first element to copy.</param>
        /// <param name="count">Data count to copy.</param>
        /// <returns>
        ///     History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest
        ///     (last element)
        /// </returns>
        public Task<List<Rate>> CopyRatesAsync(string symbol, Timeframe timeframe, DateTime startTime, int count, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return CopyRatesAsync(symbol, timeframe, 0, count, startTime, NotDefined, timeoutMillis, ct);
        }

        /// <summary>
        ///     Gets history data of <see cref="Rate" /> structure of a specified symbol-period
        ///     in specified quantity. The elements ordering of the copied data is from present to the past,
        ///     i.e., starting position of 0 means the current bar.
        /// </summary>
        /// <param name="symbol">Symbol name</param>
        /// <param name="timeframe">Period</param>
        /// <param name="startTime">Bar time for the first element to copy.</param>
        /// <param name="stopTime">Bar time, corresponding to the last element to copy.</param>
        /// <returns>
        ///     History data of a specified symbol-period. Elements are ordered from the oldest (first element) to the newest
        ///     (last element)
        /// </returns>
        public Task<List<Rate>> CopyRatesAsync(string symbol, Timeframe timeframe, DateTime startTime, DateTime stopTime, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            return CopyRatesAsync(symbol, timeframe, 0, 0, startTime, stopTime, timeoutMillis, ct);
        }

        /// <summary>
        ///     Gets current <see cref="Bar" /> of a specified symbol-period.
        /// </summary>
        /// <param name="symbol">Symbol name</param>
        /// <param name="timeframe">Period</param>
        /// <returns>Current <see cref="Bar" /></returns>
        public async Task<Bar> GetBarAsync(string symbol, Timeframe timeframe, int timeoutMillis = -1, CancellationToken ct = default(CancellationToken))
        {
            var copyRates = await CopyRatesAsync(symbol, timeframe, 0, 1, NotDefined, NotDefined, timeoutMillis, ct).ConfigureAwait(false);
            var bid = await MarketinfoAsync(symbol, MarketInfo.MODE_BID, timeoutMillis, ct).ConfigureAwait(false);
            var ask = await MarketinfoAsync(symbol, MarketInfo.MODE_ASK, timeoutMillis, ct).ConfigureAwait(false);
            return copyRates.Count > 0 ? new Bar(copyRates[0], bid, ask) : new Bar(new Rate(bid), bid, ask);
        }

    }
}