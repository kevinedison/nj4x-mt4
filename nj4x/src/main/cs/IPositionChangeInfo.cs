using System.Collections.Generic;

namespace nj4x
{
    /// <summary>
    /// Changes in trader's position: new, modified, deleted or closed orders.
    /// </summary>
    public interface IPositionChangeInfo
    {
        /// <summary>
        /// List of deleted Stop/Limit orders.
        /// </summary>
        /// <returns></returns>
        List<IOrderInfo> GetDeletedOrders();

        /// <summary>
        /// List of closed orders.
        /// </summary>
        /// <returns></returns>
        List<IOrderInfo> GetClosedOrders();

        /// <summary>
        /// List of new trading orders.
        /// </summary>
        /// <returns></returns>
        List<IOrderInfo> GetNewOrders();

        /// <summary>
        /// List of modified trading orders.
        /// </summary>
        /// <returns></returns>
        List<IOrderInfo> GetModifiedOrders();
    }
}