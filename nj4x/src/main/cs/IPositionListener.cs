using System.Threading.Tasks;

namespace nj4x
{
    /// <summary>
    /// Trading events handler interface.
    /// </summary>
    public interface IPositionListener
    {
        /// <summary>
        /// Informs about initial trader's position.
        /// </summary>
        /// <param name="initialPositionInfo">initialPositionInfo active orders at listener startup.</param>
        void OnInit(IPositionInfo initialPositionInfo);

        /// <summary>
        /// It is invoked on Changes in trader's position.
        /// </summary>
        /// <param name="currentPositionInfo">current active orders</param>
        /// <param name="changes">position Changes: set of new/deleted/closed/modified orders</param>
        void OnChange(IPositionInfo currentPositionInfo, IPositionChangeInfo changes);
    }

    public class ChangedPositionHandlerInfo
    {
        public IPositionInfo CurrentPositionInfo { get; set; }
        public IPositionChangeInfo Changes { get; set; }
    }

    /// <summary>
    /// Informs about initial trader's position.
    /// </summary>
    /// <param name="initialPositionInfo">initialPositionInfo active orders at listener startup.</param>
    public delegate void InitializedPositionHandler(IPositionInfo initialPositionInfo);

    /// <summary>
    /// It is invoked on Changes in trader's position.
    /// </summary>
    /// <param name="currentPositionInfo">current active orders</param>
    /// <param name="changes">position Changes: set of new/deleted/closed/modified orders</param>
    public delegate Task ChangedPositionHandler(IPositionInfo currentPositionInfo, IPositionChangeInfo changes);
}