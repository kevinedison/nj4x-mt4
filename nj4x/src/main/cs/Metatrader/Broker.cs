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

namespace nj4x.Metatrader
{
    /// <summary>Default brokers set</summary>
    /// 
    public class Broker
    {
        #region ProxyType enum

        /// <summary>
        /// Proxy server type
        /// </summary>
        public enum ProxyType
        {
            /// <summary>
            /// HTTP 
            /// </summary>
            HTTP,

            /// <summary>
            /// SOCKS4
            /// </summary>
            SOCKS4,

            /// <summary>
            /// SOCKS5
            /// </summary>
            SOCKS5
        }

        #endregion

        /// 
        /// 
        public static readonly Broker AlpariNZ_Demo = new Broker("Alpari-Demo");

        /// 
        /// 
        public static readonly Broker AlpariUK_Demo = new Broker("AlpariUK-Demo");

        /// 
        /// 
        public static readonly Broker AlpariUK_Live = new Broker("AlpariUK-Live");

        /// 
        /// 
        public static readonly Broker AlpariUS_Demo = new Broker("AlpariUS-Demo");

        /// 
        /// 
        public static readonly Broker AlpariUS_Live = new Broker("AlpariUS-Live");

        /// 
        /// 
        public static readonly Broker Apex_Real = new Broker("Apex-Real");

        /// 
        /// 
        public static readonly Broker ATCBrokers_Demo_Pro_EST = new Broker("ATCBrokers-Demo-Pro-EST");

        /// 
        /// 
        public static readonly Broker CrownForexSA_Server = new Broker("CrownForexSA-Server");

        /// 
        /// 
        public static readonly Broker EuroForex_Server = new Broker("EuroForex-Server");

        /// 
        /// 
        public static readonly Broker EuroOrient_Demo = new Broker("EuroOrient-Demo");

        /// 
        /// 
        public static readonly Broker EuroOrient_Real1 = new Broker("EuroOrient-Real1");

        /// 
        /// 
        public static readonly Broker EuroOrient_Real2 = new Broker("EuroOrient-Real2");

        /// 
        /// 
        public static readonly Broker FIBO_Demo = new Broker("FIBO-Demo");

        /// 
        /// 
        public static readonly Broker FinMarket_Server = new Broker("FinMarket-Server");

        /// 
        /// 
        public static readonly Broker Forex_Server = new Broker("Forex-Server");

        /// 
        /// 
        public static readonly Broker Forex_com_Demo = new Broker("Forex.com-Demo");

        /// 
        /// 
        public static readonly Broker Forex_com_Live = new Broker("Forex.com-Live");

        /// 
        /// 
        public static readonly Broker Forex_com_Server = new Broker("Forex.com-Server");

        /// 
        /// 
        public static readonly Broker ForexBest_Demo = new Broker("ForexBest-Demo");

        /// 
        /// 
        public static readonly Broker ForexBest_Real = new Broker("ForexBest-Real");

        /// 
        /// 
        public static readonly Broker FXDD_MT4_DEMO_Server = new Broker("FXDD-MT4 DEMO Server");

        /// 
        /// 
        public static readonly Broker FXDD_MT4_Live_Server = new Broker("FXDD-MT4 Live Server");

        /// 
        /// 
        public static readonly Broker FXDirectDealer_MT4_DEMO_Server = new Broker("FXDirectDealer-MT4 DEMO Server");

        /// 
        /// 
        public static readonly Broker FXDirectDealer_MT4_Live_Server = new Broker("FXDirectDealer-MT4 Live Server");

        /// 
        /// 
        public static readonly Broker FxPro_Server = new Broker("FxPro-Server");

        /// 
        /// 
        public static readonly Broker Gimex_NEXTT = new Broker("Gimex-NEXTT");

        /// 
        /// 
        public static readonly Broker IntegralBank_Server = new Broker("IntegralBank-Server");

        /// 
        /// 
        public static readonly Broker InterbankFX_Demo_Accounts = new Broker("InterbankFX-Demo Accounts");

        /// 
        /// 
        public static readonly Broker InterbankFX_Live_Accounts = new Broker("InterbankFX-Live Accounts");

        /// 
        /// 
        public static readonly Broker MBTrading_Demo = new Broker("MBTrading-Demo Server");

        /// 
        /// 
        public static readonly Broker MBTrading_Live = new Broker("MBTrading-Live Server");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Demo_Accounts_2 = new Broker("InterbankFX-MT4 Demo Accounts 2");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Mini_Accounts_2 = new Broker("InterbankFX-MT4 Mini Accounts 2");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Mini_Accounts_3 = new Broker("InterbankFX-MT4 Mini Accounts 3");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Mini_Accounts_4 = new Broker("InterbankFX-MT4 Mini Accounts 4");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Mini_Accounts = new Broker("InterbankFX-MT4 Mini Accounts");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_MM_Mini = new Broker("InterbankFX-MT4 MM Mini");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Standard_Accounts_2 =
            new Broker("InterbankFX-MT4 Standard Accounts 2");

        /// 
        /// 
        public static readonly Broker InterbankFX_MT4_Standard_Accounts = new Broker("InterbankFX-MT4 Standard Accounts");

        /// 
        /// 
        public static readonly Broker MIG_Real = new Broker("MIG-Real");

        /// 
        /// 
        public static readonly Broker MIG_Real2 = new Broker("MIG-Real2");

        /// 
        /// 
        public static readonly Broker NorthFinance_Demo = new Broker("NorthFinance-Demo");

        /// 
        /// 
        public static readonly Broker ODL_MT4_Demo = new Broker("ODL-MT4 Demo");

        /// 
        /// 
        public static readonly Broker Omnivest_Omnivest_Trading_System = new Broker("Omnivest-Omnivest Trading System");

        /// 
        /// 
        public static readonly Broker Orion_DEMO = new Broker("Orion-DEMO");

        /// 
        /// 
        public static readonly Broker RCG_Demo = new Broker("RCG-Demo");

        /// 
        /// 
        public static readonly Broker RCG_Pro = new Broker("RCG-Pro");

        /// 
        /// 
        public static readonly Broker RCG_Server1 = new Broker("RCG-Server1");

        /// 
        /// 
        public static readonly Broker RealTrade_Demo = new Broker("RealTrade-Demo");

        /// 
        /// 
        public static readonly Broker RealTrade_Real = new Broker("RealTrade-Real");

        /// 
        /// 
        public static readonly Broker RFXT_Server = new Broker("RFXT-Server");

        /// 
        /// 
        public static readonly Broker SIG_Lite_com = new Broker("SIG-Lite.com");

        /// 
        /// 
        public static readonly Broker SpotTrader_Real = new Broker("SpotTrader-Real");

        /// 
        /// 
        public static readonly Broker TeleTrade_Demo = new Broker("TeleTrade-Demo");

        /// 
        /// 
        public static readonly Broker TeleTrade_Server = new Broker("TeleTrade-Server");

        /// 
        /// 
        public static readonly Broker UGMFX_Live = new Broker("UGMFX-Live");

        /// 
        /// 
        public static readonly Broker Ukrsotsbank_MT4 = new Broker("Ukrsotsbank-MT4");

        /// 
        /// 
        public static readonly Broker XTB_Demo = new Broker("XTB-Demo");

        /// 
        /// 
        public static readonly Broker XTB_Real = new Broker("XTB-Real");

        /// <summary>Broker configuration ID</summary>
        public String val;

        /// <summary>Broker enumeration constructor</summary>
        /// <param name='val'>Broker configuration ID</param>
        public Broker(String val)
        {
            this.val = val;
        }

        public override string ToString()
        {
            return val;
        }

        /// <summary>Broker enumeration constructor</summary>
        /// <param name='val'>Broker configuration ID</param>
        /// <param name="proxyServer">proxy server address, e.g. proxy.company.com:3128</param>
        /// <param name="proxyType">proxy server type</param>
        public Broker(String val, String proxyServer, ProxyType proxyType)
            : this(val, proxyServer, proxyType, null, null)
        {
        }

        /// <summary>Broker enumeration constructor</summary>
        /// <param name='val'>Broker configuration ID</param>
        /// <param name="proxyServer">proxy server address, e.g. proxy.company.com:3128</param>
        /// <param name="proxyType">proxy server type</param>
        /// <param name="proxyLogin">login to be authorized on proxy server</param>
        /// <param name="proxyPassword">password to access to proxy server</param>
        public Broker(String val, String proxyServer, ProxyType proxyType, String proxyLogin, String proxyPassword)
        {
            this.val = val;
            if (val.IndexOf('@') < 0 && proxyServer != null)
            {
                this.val += "@" + proxyServer
                            + "\u0001" + proxyType
                            + "\u0001" + (proxyLogin ?? "")
                            + "\u0001" + (proxyPassword ?? "")
                            + "\u0001"
                    ;
            }
        }
    }
}