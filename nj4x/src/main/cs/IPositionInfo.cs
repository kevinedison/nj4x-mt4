using System.Collections;
using System.Collections.Generic;

namespace nj4x
{
    /// <summary>Trader's position.</summary>
    public interface IPositionInfo
    {
        /// <summary>
        /// Hash table of currently active trades.
        /// </summary>
        /// <returns>(Long -> IOrderInfo) map of active trades.</returns>
        Hashtable GetLiveOrders();

        /// <summary>
        /// Dictionary of currently active trades.
        /// </summary>
        /// <returns>(Long -> IOrderInfo) map of active trades.</returns>
        Dictionary<long, IOrderInfo> LiveOrders { get; }

        /// <summary>
        /// Hash table of latest historical orders.
        /// </summary>
        /// <returns>(Long -> IOrderInfo) map of historical orders.</returns>
        Hashtable GetHistoricalOrders();

        /// <summary>
        /// Dictionary of latest historical orders.
        /// </summary>
        /// <returns>(Long -> IOrderInfo) map of historical orders.</returns>
        Dictionary<long, IOrderInfo> HistoricalOrders { get; }
    }

    /// <summary>
    /// for internal use only.
    /// </summary>
    public interface IPositionInfoEnabled : IPositionInfo
    {
        /// <summary>
        /// Merges current and available new position information.
        /// </summary>
        /// <param name="positionInfo">information about position to merge with.</param>
        /// <returns>Position Changes information</returns>
        IPositionChangeInfo MergePosition(IPositionInfo positionInfo);
    }
}