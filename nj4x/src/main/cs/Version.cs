using System;

namespace nj4x
{
    /// <summary>
    /// NJ4X API version registry. 
    /// It includes local API version, Terminal Server version, communication DLL version
    /// and MQL Expert Advisor version.
    /// </summary>
    public class Version
    {
        private static readonly String version = "2.6.2";

        /// <summary>
        /// Local NJ4X API version
        /// </summary>
        public static readonly String NJ4X = version;

        /// <summary>
        ///     NJ4X API build ID
        /// </summary>
        public static readonly String NJ4X_UUID = "29a50980516c";

        /// <summary>
        /// NJ4X communication DLL version.
        /// </summary>
        public String DLL;

        /// <summary>
        /// NJ4X EA version.
        /// </summary>
        public String MQL;

        /// <summary>
        /// NJ4X Terminal Server software version when connected using TS (see Strategy.connect method) or null otherwise.
        /// </summary>
        public String TS;

        internal Version(string ts, string dll, string mql)
        {
            TS = ts;
            DLL = dll;
            MQL = mql;
        }

        /// <summary>
        /// Checks if local API, Terminal Server, communication DLL
        /// and NJ4X MQL Expert Advisor are of the same version.
        /// </summary>
        /// <returns>True - if all API parts are of the same version.</returns>
        public bool IsConsistent()
        {
            return DLL != null && MQL != null
                   && NJ4X.Equals(DLL)
                   && DLL.Equals(MQL)
                   && (TS == null || MQL.Equals(TS));
        }

        /// <summary>
        /// Returns a <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </summary>
        /// <returns>
        /// A <see cref="T:System.String"/> that represents the current <see cref="T:System.Object"/>.
        /// </returns>
        /// <filterpriority>2</filterpriority>
        public override string ToString()
        {
            return "NJ4X API v." + NJ4X
                   + (TS == null ? "" : ", TS v." + TS)
                   + ", DLL v." + DLL
                   + ", MQL v." + MQL;
        }
    }
}