using System;

namespace nj4x.Metatrader
{
    /// <summary>Not existent global variable name was used</summary>
    /// 
    [Serializable] //
    public class ErrGlobalVariableNotFound : MT4Exception
    {
        internal ErrGlobalVariableNotFound(string message)
            : base(4058, message + " -> Not existent global variable name was used")
        {
        }
    }
}