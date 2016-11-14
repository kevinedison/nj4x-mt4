/*
 * Copyright (c) 2008-2014 by Gerasimenko Roman.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistribution of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistribution in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 * 3. The name "JFX" must not be used to endorse or promote
 *     products derived from this software without prior written
 *     permission.
 *     For written permission, please contact roman.gerasimenko@gmail.com
 *
 * 4. Products derived from this software may not be called "JFX",
 *     nor may "JFX" appear in their name, without prior written
 *     permission of Gerasimenko Roman.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE JFX CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 */

package com.jfx.jboss;

import com.jfx.*;

import java.io.IOException;

/**
 * JBoss service interface.
 */
public interface JFXService {
    /**
     * Establishes the connection to MT4 server and returns registered sessionID
     * to be used as a mandatory parameter to other methods.
     *
     * @param termServerHost JFX Terminal Server IP address
     * @param termServerPort JFX Terminal Server port
     * @param mt4Server      MT4 Broker
     * @param mt4User        MT4 user id
     * @param mt4Password    password
     * @return connected session ID
     * @throws IOException in case of any connection trouble
     */
    public int connect(String termServerHost, int termServerPort, Broker mt4Server, String mt4User, String mt4Password) throws IOException;

    /**
     * Can be used to check existing MT4-connection session
     *
     * @param mt4Server MT4 Broker
     * @param mt4User   MT4 user id
     * @return null if there is no mt4Server/mt4User connection, sessionID otherwise
     */
    public Integer getSessionID(Broker mt4Server, String mt4User);

    /**
     * Disconnects existing MT4 session
     *
     * @param mt4Server MT4 Broker
     * @param mt4User   MT4 user id
     * @throws IOException
     */
    public void disconnect(Broker mt4Server, String mt4User) throws IOException;

    /**
     * Disconnects existing MT4 session
     *
     * @param session MT4 session ID given by connect(...) method
     * @throws IOException
     */
    public void disconnect(int session) throws IOException;

    /**
     * Returns the The last incoming tick time (last known server time).
     *
     * @return Returns the last The last incoming tick time (last known server time).
     */
    public java.util.Date marketInfo_MODE_TIME(int sessionID,
                                               String symbol
    ) throws ErrUnknownSymbol;


    /**
     * Returns the number of bars on the specified chart. For the current chart, the information about the amount of bars is in the predefined variable named Bars.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @return Returns the number of bars on the specified chart. For the current chart, the information about the amount of bars is in the predefined variable named Bars.
     */
    public int iBars(int sessionID,
                     String symbol,
                     Timeframe timeframe
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Search for bar by open time. The function returns bar shift with the open time specified. If the bar having the specified open time is missing, the function will return -1 or the nearest bar shift depending on the exact.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param time      value to find (bar's open time).
     * @param exact     Return mode when bar not found. false - iBarShift returns nearest. true - iBarShift returns -1.
     * @return Search for bar by open time. The function returns bar shift with the open time specified. If the bar having the specified open time is missing, the function will return -1 or the nearest bar shift depending on the exact.
     */
    public int iBarShift(int sessionID,
                         String symbol,
                         Timeframe timeframe,
                         java.util.Date time,
                         boolean exact
    ) throws ErrIncorrectSeriesarrayUsing, ErrUnknownSymbol, ErrHistoryWillUpdated;


    /**
     * Returns Close value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0.For the current chart, the information about close prices is in the predefined array named Close[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Close value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0.For the current chart, the information about close prices is in the predefined array named Close[].
     */
    public double iClose(int sessionID,
                         String symbol,
                         Timeframe timeframe,
                         int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns High value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about high prices is in the predefined array named High[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns High value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about high prices is in the predefined array named High[].
     */
    public double iHigh(int sessionID,
                        String symbol,
                        Timeframe timeframe,
                        int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns Low value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about low prices is in the predefined array named Low[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Low value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about low prices is in the predefined array named Low[].
     */
    public double iLow(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns Open value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about open prices is in the predefined array named Open[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Open value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about open prices is in the predefined array named Open[].
     */
    public double iOpen(int sessionID,
                        String symbol,
                        Timeframe timeframe,
                        int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns Tick Volume value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars tick volumes is in the predefined array named Volume[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Tick Volume value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars tick volumes is in the predefined array named Volume[].
     */
    public double iVolume(int sessionID,
                          String symbol,
                          Timeframe timeframe,
                          int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns Time value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars open times is in the predefined array named Time[].
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Returns Time value for the bar of indicated symbol with timeframe and shift. If local history is empty (not loaded), function returns 0. For the current chart, the information about bars open times is in the predefined array named Time[].
     */
    public java.util.Date iTime(int sessionID,
                                String symbol,
                                Timeframe timeframe,
                                int shift
    ) throws ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns the shift of the least value over a specific number of periods depending on type.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param type      Series array identifier. It can be any of Series array identifier enumeration values.
     * @param count     Number of periods (in direction from the start bar to the back one) on which the calculation is carried out.
     * @param start     Shift showing the bar, relative to the current bar, that the data should be taken from.
     * @return Returns the shift of the least value over a specific number of periods depending on type.
     */
    public int iLowest(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       Series type,
                       int count,
                       int start
    ) throws ErrIncorrectSeriesarrayUsing, ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns the shift of the maximum value over a specific number of periods depending on type.
     *
     * @param symbol    Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param type      Series array identifier. It can be any of Series array identifier enumeration values.
     * @param count     Number of periods (in direction from the start bar to the back one) on which the calculation is carried out.
     * @param start     Shift showing the bar, relative to the current bar, that the data should be taken from.
     * @return Returns the shift of the maximum value over a specific number of periods depending on type.
     */
    public int iHighest(int sessionID,
                        String symbol,
                        Timeframe timeframe,
                        Series type,
                        int count,
                        int start
    ) throws ErrIncorrectSeriesarrayUsing, ErrHistoryWillUpdated, ErrUnknownSymbol;


    /**
     * Returns balance value of the current account (the amount of money on the account).
     *
     * @return Returns balance value of the current account (the amount of money on the account).
     */
    public double accountBalance(int sessionID);


    /**
     * Returns credit value of the current account.
     *
     * @return Returns credit value of the current account.
     */
    public double accountCredit(int sessionID);


    /**
     * Returns the brokerage company name where the current account was registered.
     *
     * @return Returns the brokerage company name where the current account was registered.
     */
    public String accountCompany(int sessionID);


    /**
     * Returns currency name of the current account.
     *
     * @return Returns currency name of the current account.
     */
    public String accountCurrency(int sessionID);


    /**
     * Returns equity value of the current account. Equity calculation depends on trading server settings.
     *
     * @return Returns equity value of the current account. Equity calculation depends on trading server settings.
     */
    public double accountEquity(int sessionID);


    /**
     * Returns free margin value of the current account.
     *
     * @return Returns free margin value of the current account.
     */
    public double accountFreeMargin(int sessionID);


    /**
     * Returns margin value of the current account.
     *
     * @return Returns margin value of the current account.
     */
    public double accountMargin(int sessionID);


    /**
     * Returns the current account name.
     *
     * @return Returns the current account name.
     */
    public String accountName(int sessionID);


    /**
     * Returns the number of the current account
     *
     * @return Returns the number of the current account
     */
    public int accountNumber(int sessionID);


    /**
     * Returns profit value of the current account.
     *
     * @return Returns profit value of the current account.
     */
    public double accountProfit(int sessionID);


    /**
     * The function returns the last occurred error, then the value of special last_error variable where the last error code is stored will be zeroized. So, the next call for GetLastError() will return 0.
     *
     * @return The function returns the last occurred error, then the value of special last_error variable where the last error code is stored will be zeroized. So, the next call for GetLastError() will return 0.
     */
    public int getLastError(int sessionID);


    /**
     * The function returns the status of the main connection between client terminal and server that performs data pumping. It returns TRUE if connection to the server was successfully established, otherwise, it returns FALSE.
     *
     * @return The function returns the status of the main connection between client terminal and server that performs data pumping. It returns TRUE if connection to the server was successfully established, otherwise, it returns FALSE.
     */
    public boolean isConnected(int sessionID);


    /**
     * Returns TRUE if the expert runs on a demo account, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert runs on a demo account, otherwise returns FALSE.
     */
    public boolean isDemo(int sessionID);


    /**
     * Returns TRUE if expert runs in the testing mode, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert runs in the testing mode, otherwise returns FALSE.
     */
    public boolean isTesting(int sessionID);


    /**
     * Returns TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert is tested with checked 'Visual Mode' button, otherwise returns FALSE.
     */
    public boolean isVisualMode(int sessionID);


    /**
     * The GetTickCount() function retrieves the number of milliseconds that have elapsed since the system was started. It is limited to the resolution of the system timer.
     *
     * @return The GetTickCount() function retrieves the number of milliseconds that have elapsed since the system was started. It is limited to the resolution of the system timer.
     */
    public int getTickCount(int sessionID);


    /**
     * The function outputs the comment defined by the user in the left top corner of the chart.
     *
     * @param comments User defined comment.
     * @return The function outputs the comment defined by the user in the left top corner of the chart.
     */
    public void comment(int sessionID,
                        String comments
    );


    /**
     * @param symbol
     * @param type
     * @return
     */
    public double marketInfo(int sessionID,
                             String symbol,
                             MarketInfo type
    ) throws ErrUnknownSymbol;


    /**
     * Prints a message to the experts log
     *
     * @param comments User defined message.
     * @return Prints a message to the experts log
     */
    public void print(int sessionID,
                      String comments
    );


    /**
     * Returns the current day of the month, i.e., the day of month of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current day of the month, i.e., the day of month of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int day(int sessionID);


    /**
     * Returns the current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current zero-based day of the week (0-Sunday,1,2,3,4,5,6) of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int dayOfWeek(int sessionID);


    /**
     * Returns the current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current day of the year (1 means 1 January,..,365(6) does 31 December), i.e., the day of year of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int dayOfYear(int sessionID);


    /**
     * Returns the hour (0,1,2,..23) of the last known server time by the moment of the program start (this value will not change within the time of the program execution). Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the hour (0,1,2,..23) of the last known server time by the moment of the program start (this value will not change within the time of the program execution). Note: At the testing, the last known server time is modelled.
     */
    public int hour(int sessionID);


    /**
     * Returns the current minute (0,1,2,..59) of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     *
     * @return Returns the current minute (0,1,2,..59) of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     */
    public int minute(int sessionID);


    /**
     * Returns the current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current month as number (1-January,2,3,4,5,6,7,8,9,10,11,12), i.e., the number of month of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int month(int sessionID);


    /**
     * Returns the amount of seconds elapsed from the beginning of the current minute of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     *
     * @return Returns the amount of seconds elapsed from the beginning of the current minute of the last known server time by the moment of the program start (this value will not change within the time of the program execution).
     */
    public int seconds(int sessionID);


    /**
     * Returns the last known server time (time of incoming of the latest quote) as number of seconds elapsed from 00:00 January 1, 1970. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the last known server time (time of incoming of the latest quote) as number of seconds elapsed from 00:00 January 1, 1970. Note: At the testing, the last known server time is modelled.
     */
    public java.util.Date timeCurrent(int sessionID);


    /**
     * Returns the current year, i.e., the year of the last known server time. Note: At the testing, the last known server time is modelled.
     *
     * @return Returns the current year, i.e., the year of the last known server time. Note: At the testing, the last known server time is modelled.
     */
    public int year(int sessionID);


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @param time2  Time part of the second point.
     * @param price2 Price part of the second point.
     * @param time3  Time part of the third point.
     * @param price3 Price part of the third point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(int sessionID,
                                String name,
                                ObjectType type,
                                int window,
                                java.util.Date time1,
                                double price1,
                                java.util.Date time2,
                                double price2,
                                java.util.Date time3,
                                double price3
    );


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(int sessionID,
                                String name,
                                ObjectType type,
                                int window,
                                java.util.Date time1,
                                double price1
    );


    /**
     * Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     *
     * @param name   Object unique name.
     * @param type   Object type. It can be any of the Object type enumeration values.
     * @param window Index of the window where the object will be added. Window index must exceed or equal to 0 and be less than WindowsTotal().
     * @param time1  Time part of the first point.
     * @param price1 Price part of the first point.
     * @param time2  Time part of the second point.
     * @param price2 Price part of the second point.
     * @return Creation of an object with the specified name, type and initial coordinates in the specified window. Count of coordinates related to the object can be from 1 to 3 depending on the object type. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. Objects of the OBJ_LABEL type ignore the coordinates. Use the function of ObjectSet() to set up the OBJPROP_XDISTANCE and OBJPROP_YDISTANCE properties. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. Coordinates must be passed in pairs: time and price. For example, the OBJ_VLINE object needs only time, but price (any value) must be passed, as well.
     */
    public boolean objectCreate(int sessionID,
                                String name,
                                ObjectType type,
                                int window,
                                java.util.Date time1,
                                double price1,
                                java.util.Date time2,
                                double price2
    );


    /**
     * Deletes object having the specified name. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name Object unique name.
     * @return Deletes object having the specified name. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function.
     */
    public boolean objectDelete(int sessionID, String name);


    /**
     * The function returns the value of the specified object property. To check errors, one has to call the GetLastError() function. See also ObjectSet() function.
     *
     * @param name  Object unique name.
     * @param index Object property index. It can be any of the Object properties enumeration values.
     * @return The function returns the value of the specified object property. To check errors, one has to call the GetLastError() function. See also ObjectSet() function.
     */
    public double objectGet(int sessionID,
                            String name,
                            ObjectProperty index
    );


    /**
     * Changes the value of the specified object property. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. See also ObjectGet() function.
     *
     * @param name  Object unique name.
     * @param index Object property index. It can be any of the Object properties enumeration values.
     * @param value New value of the given property.
     * @return Changes the value of the specified object property. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. See also ObjectGet() function.
     */
    public boolean objectSet(int sessionID,
                             String name,
                             ObjectProperty index,
                             double value
    );


    /**
     * The function returns the level description of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function. See also ObjectSetFiboDescription() function.
     *
     * @param name  Object unique name.
     * @param index Index of the Fibonacci level (0-31).
     * @return The function returns the level description of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function. See also ObjectSetFiboDescription() function.
     */
    public String objectGetFiboDescription(int sessionID,
                                           String name,
                                           int index
    );


    /**
     * The function assigns a new description to a level of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object unique name.
     * @param index Index of the Fibonacci level (0-31).
     * @param text  New description of the level.
     * @return The function assigns a new description to a level of a Fibonacci object. The amount of Fibonacci levels depends on the object type. The maximum amount of Fibonacci levels is 32. To get the detailed error information, one has to call the GetLastError() function.
     */
    public boolean objectSetFiboDescription(int sessionID,
                                            String name,
                                            int index,
                                            String text
    );


    /**
     * Changes the object description. For objects of OBJ_TEXT and OBJ_LABEL, this description is shown as a text line in the chart. If the function succeeds, the returned value will be TRUE. Otherwise, it is FALSE. To get the detailed error information, one has to call the GetLastError() function. Parameters of font_size, font_name and text_color are used for objects of OBJ_TEXT and OBJ_LABEL only. For objects of other types, these parameters are ignored. See also ObjectDescription() function.
     *
     * @param name       Object unique name.
     * @param text       A text describing the object.
     * @param font_size  Font size in points.
     * @param font       Font name (NULL).
     * @param text_color Text color (CLR_NONE).
     * @return Changes the object description. For objects of OBJ_TEXT and OBJ_LABEL, this description is shown as a text line in the chart. If the function succeeds, the returned value will be TRUE. Otherwise, it is FALSE. To get the detailed error information, one has to call the GetLastError() function. Parameters of font_size, font_name and text_color are used for objects of OBJ_TEXT and OBJ_LABEL only. For objects of other types, these parameters are ignored. See also ObjectDescription() function.
     */
    public boolean objectSetText(int sessionID,
                                 String name,
                                 String text,
                                 int font_size,
                                 String font,
                                 long text_color
    );


    /**
     * Returns total amount of objects of the specified type in the chart.
     *
     * @param type Optional parameter. An object type to be counted. It can be any of the Object type enumeration values or EMPTY constant to count all objects with any types.
     * @return Returns total amount of objects of the specified type in the chart.
     */
    public int objectsTotal(int sessionID,
                            ObjectType type
    );


    /**
     * The function returns the object type value. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name Object unique name.
     * @return The function returns the object type value. To get the detailed error information, one has to call the GetLastError() function.
     */
    public int objectType(int sessionID,
                          String name
    );


    /**
     * Calculates the Bill Williams' Accelerator/Decelerator oscillator.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Accelerator/Decelerator oscillator.
     */
    public double iAC(int sessionID,
                      String symbol,
                      Timeframe timeframe,
                      int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Accumulation/Distribution indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Accumulation/Distribution indicator and returns its value.
     */
    public double iAD(int sessionID,
                      String symbol,
                      Timeframe timeframe,
                      int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Bill Williams' Alligator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param jawPeriod    Blue line averaging period (Alligator's Jaw).
     * @param jawShift     Blue line shift relative to the chart.
     * @param teethPeriod  Red line averaging period (Alligator's Teeth).
     * @param teethShift   Red line shift relative to the chart.
     * @param lipsPeriod   Green line averaging period (Alligator's Lips).
     * @param lipsShift    Green line shift relative to the chart.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Data source, identifier of a line of the indicator. It can be any of the following values: MODE_GATORJAW - Gator Jaw (blue) balance line, MODE_GATORTEETH - Gator Teeth (red) balance line, MODE_GATORLIPS - Gator Lips (green) balance line.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Alligator and returns its value.
     */
    public double iAlligator(int sessionID,
                             String symbol,
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
                             int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Movement directional index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Movement directional index and returns its value.
     */
    public double iADX(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       AppliedPrice appliedPrice,
                       ADXIndicatorLines mode,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Indicator of the average true range and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Indicator of the average true range and returns its value.
     */
    public double iATR(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Bill Williams' Awesome oscillator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams' Awesome oscillator and returns its value.
     */
    public double iAO(int sessionID,
                      String symbol,
                      Timeframe timeframe,
                      int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Bears Power indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bears Power indicator and returns its value.
     */
    public double iBearsPower(int sessionID,
                              String symbol,
                              Timeframe timeframe,
                              int period,
                              AppliedPrice appliedPrice,
                              int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Movement directional index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param deviation    Deviation from the main line.
     * @param bandsShift   The indicator shift relative to the chart.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Movement directional index and returns its value.
     */
    public double iBands(int sessionID,
                         String symbol,
                         Timeframe timeframe,
                         int period,
                         int deviation,
                         int bandsShift,
                         AppliedPrice appliedPrice,
                         BandsIndicatorLines mode,
                         int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Bulls Power indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bulls Power indicator and returns its value.
     */
    public double iBullsPower(int sessionID,
                              String symbol,
                              Timeframe timeframe,
                              int period,
                              AppliedPrice appliedPrice,
                              int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Commodity channel index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Commodity channel index and returns its value.
     */
    public double iCCI(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       AppliedPrice appliedPrice,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param name      Custom indicator compiled program name.
     * @param mode      Line index. Can be from 0 to 7 and must correspond with the index used by one of SetIndexBuffer functions.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the specified custom indicator and returns its value. The custom indicator must be compiled (*.EX4 file) and be in the terminal_directory/experts/indicators directory.
     */
    public double iCustom(int sessionID,
                          String symbol,
                          Timeframe timeframe,
                          String name,
                          int mode,
                          int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the DeMarker indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the DeMarker indicator and returns its value.
     */
    public double iDeMarker(int sessionID,
                            String symbol,
                            Timeframe timeframe,
                            int period,
                            int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Envelopes indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param maPeriod     Averaging period for calculation of the main line.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param maShitf      MA shift. Indicator line offset relate to the chart by timeframe.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param deviation    Percent deviation from the main line.
     * @param mode         Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Envelopes indicator and returns its value.
     */
    public double iEnvelopes(int sessionID,
                             String symbol,
                             Timeframe timeframe,
                             int maPeriod,
                             MovingAverageMethod maMethod,
                             int maShitf,
                             AppliedPrice appliedPrice,
                             double deviation,
                             BandsIndicatorLines mode,
                             int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Force index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Force index and returns its value.
     */
    public double iForce(int sessionID,
                         String symbol,
                         Timeframe timeframe,
                         int period,
                         MovingAverageMethod maMethod,
                         AppliedPrice appliedPrice,
                         int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Fractals and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param mode      Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Fractals and returns its value.
     */
    public double iFractals(int sessionID,
                            String symbol,
                            Timeframe timeframe,
                            BandsIndicatorLines mode,
                            int shift
    ) throws ErrUnknownSymbol;


    /**
     * Gator oscillator calculation. The oscillator displays the difference between the Alligator red and blue lines (the upper histogram) and that between red and green lines (the lower histogram).
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param jawPeriod    Blue line averaging period (Alligator's Jaw).
     * @param jawShift     Blue line shift relative to the chart.
     * @param teethPeriod  Red line averaging period (Alligator's Teeth).
     * @param teethShift   Red line shift relative to the chart.
     * @param lipsPeriod   Green line averaging period (Alligator's Lips).
     * @param lipsShift    Green line shift relative to the chart.
     * @param maMethod     MA method. It can be any of Moving Average methods.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param mode         Data source, identifier of a line of the indicator. It can be any of the following values: MODE_GATORJAW - Gator Jaw (blue) balance line, MODE_GATORTEETH - Gator Teeth (red) balance line, MODE_GATORLIPS - Gator Lips (green) balance line.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Gator oscillator calculation. The oscillator displays the difference between the Alligator red and blue lines (the upper histogram) and that between red and green lines (the lower histogram).
     */
    public double iGator(int sessionID,
                         String symbol,
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
                         int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Bill Williams Market Facilitation index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Bill Williams Market Facilitation index and returns its value.
     */
    public double iBWMFI(int sessionID,
                         String symbol,
                         Timeframe timeframe,
                         int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Momentum indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Momentum indicator and returns its value.
     */
    public double iMomentum(int sessionID,
                            String symbol,
                            Timeframe timeframe,
                            int period,
                            AppliedPrice appliedPrice,
                            int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Money flow index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Money flow index and returns its value.
     */
    public double iMFI(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Moving average indicator and returns its value.
     *
     * @param symbol       Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Averaging period for calculation.
     * @param maShift      MA shift. Indicators line offset relate to the chart by timeframe.
     * @param maMethod     MA method. It can be any of the Moving Average method enumeration value.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving average indicator and returns its value.
     */
    public double iMA(int sessionID,
                      String symbol,
                      Timeframe timeframe,
                      int period,
                      int maShift,
                      MovingAverageMethod maMethod,
                      AppliedPrice appliedPrice,
                      int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Moving Average of Oscillator and returns its value. Sometimes called MACD Histogram in some systems.
     *
     * @param symbol        Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param fastEMAPeriod Number of periods for fast moving average calculation.
     * @param slowEMAPeriod Number of periods for slow moving average calculation.
     * @param signalPeriod  Number of periods for signal moving average calculation.
     * @param appliedPrice  Applied price. It can be any of Applied price enumeration values.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving Average of Oscillator and returns its value. Sometimes called MACD Histogram in some systems.
     */
    public double iOsMA(int sessionID,
                        String symbol,
                        Timeframe timeframe,
                        int fastEMAPeriod,
                        int slowEMAPeriod,
                        int signalPeriod,
                        AppliedPrice appliedPrice,
                        int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Moving averages convergence/divergence and returns its value. In the systems where OsMA is called MACD Histogram, this indicator is displayed as two lines. In the Client Terminal, the Moving Average Convergence/Divergence is drawn as a histogram.
     *
     * @param symbol        Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param fastEMAPeriod Number of periods for fast moving average calculation.
     * @param slowEMAPeriod Number of periods for slow moving average calculation.
     * @param signalPeriod  Number of periods for signal moving average calculation.
     * @param appliedPrice  Applied price. It can be any of Applied price enumeration values.
     * @param mode          Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Moving averages convergence/divergence and returns its value. In the systems where OsMA is called MACD Histogram, this indicator is displayed as two lines. In the Client Terminal, the Moving Average Convergence/Divergence is drawn as a histogram.
     */
    public double iMACD(int sessionID,
                        String symbol,
                        Timeframe timeframe,
                        int fastEMAPeriod,
                        int slowEMAPeriod,
                        int signalPeriod,
                        AppliedPrice appliedPrice,
                        MACDIndicatorLines mode,
                        int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the On Balance Volume indicator and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the On Balance Volume indicator and returns its value.
     */
    public double iOBV(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       AppliedPrice appliedPrice,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Parabolic Stop and Reverse system and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param step      Increment, usually 0.02.
     * @param maximum   Maximum value, usually 0.2.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Parabolic Stop and Reverse system and returns its value.
     */
    public double iSAR(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       double step,
                       double maximum,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Relative strength index and returns its value.
     *
     * @param symbol       Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period       Number of periods for calculation.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Relative strength index and returns its value.
     */
    public double iRSI(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       AppliedPrice appliedPrice,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Relative Vigor index and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Number of periods for calculation.
     * @param mode      Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Relative Vigor index and returns its value.
     */
    public double iRVI(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       MACDIndicatorLines mode,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Standard Deviation indicator and returns its value.
     *
     * @param symbol       Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe    Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param maPeriod     MA period
     * @param maShift      MA shift. Indicators line offset relate to the chart by timeframe.
     * @param maMethod     MA method. It can be any of the Moving Average method enumeration value.
     * @param appliedPrice Applied price. It can be any of Applied price enumeration values.
     * @param shift        Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Standard Deviation indicator and returns its value.
     */
    public double iStdDev(int sessionID,
                          String symbol,
                          Timeframe timeframe,
                          int maPeriod,
                          int maShift,
                          MovingAverageMethod maMethod,
                          AppliedPrice appliedPrice,
                          int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Stochastic oscillator and returns its value.
     *
     * @param symbol     Symbol the data of which should be used to calculate indicator; NULL means the current symbol.
     * @param timeframe  Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param kPeriod    %K line period.
     * @param dPeriod    %D line period.
     * @param slowing    Slowing value.
     * @param maMethod   MA method. It can be any of the Moving Average method enumeration value.
     * @param priceField Price field parameter. Can be one of this values: 0 - Low/High or 1 - Close/Close.
     * @param mode       Indicator line index. It can be any of the Indicators line identifiers enumeration value.
     * @param shift      Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Stochastic oscillator and returns its value.
     */
    public double iStochastic(int sessionID,
                              String symbol,
                              Timeframe timeframe,
                              int kPeriod,
                              int dPeriod,
                              int slowing,
                              MovingAverageMethod maMethod,
                              int priceField,
                              MACDIndicatorLines mode,
                              int shift
    ) throws ErrUnknownSymbol;


    /**
     * Calculates the Larry William's percent range indicator and returns its value.
     *
     * @param symbol    Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param period    Averaging period for calculation.
     * @param shift     Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Larry William's percent range indicator and returns its value.
     */
    public double iWPR(int sessionID,
                       String symbol,
                       Timeframe timeframe,
                       int period,
                       int shift
    ) throws ErrUnknownSymbol;


    /**
     * Closes opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param lots       Number of lots.
     * @param price      Preferred closing price.
     * @param slippage   Value of the maximum price slippage in points.
     * @param arrowColor Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Closes opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     */
    public boolean orderClose(int sessionID,
                              int ticket,
                              double lots,
                              double price,
                              int slippage,
                              long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidPriceParam, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders;


    /**
     * Closes an opened order by another opposite opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param opposite   Unique number of the opposite order ticket.
     * @param arrowColor Color of the closing arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Closes an opened order by another opposite opened order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     */
    public boolean orderCloseBy(int sessionID,
                                int ticket,
                                int opposite,
                                long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders;


    /**
     * Returns close price for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns close price for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderClosePrice(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns close time for the currently selected order. If order close time is not 0 then the order selected and has been closed and retrieved from the account history. Open and pending orders close time is equal to 0. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns close time for the currently selected order. If order close time is not 0 then the order selected and has been closed and retrieved from the account history. Open and pending orders close time is equal to 0. Note: The order must be previously selected by the OrderSelect() function.
     */
    public java.util.Date orderCloseTime(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns comment for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns comment for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public String orderComment(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns calculated commission for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns calculated commission for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderCommission(int sessionID) throws ErrNoOrderSelected;


    /**
     * Deletes previously opened pending order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     *
     * @param ticket     Unique number of the order ticket.
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Deletes previously opened pending order. If the function succeeds, the return value is true. If the function fails, the return value is false. To get the detailed error information, call GetLastError().
     */
    public boolean orderDelete(int sessionID,
                               int ticket,
                               long arrowColor
    ) throws ErrCustomIndicatorError, ErrInvalidFunctionParamvalue, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders;


    /**
     * Returns expiration date for the selected pending order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns expiration date for the selected pending order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderExpiration(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns amount of lots for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns amount of lots for the selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderLots(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns an identifying (magic) number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns an identifying (magic) number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public int orderMagicNumber(int sessionID) throws ErrNoOrderSelected;


    /**
     * Modification of characteristics for the previously opened position or pending orders. If the function succeeds, the returned value will be TRUE. If the function fails, the returned value will be FALSE. To get the detailed error information, call GetLastError() function. Notes: Open price and expiration time can be changed only for pending orders. If unchanged values are passed as the function parameters, the error 1 (ERR_NO_RESULT) will be generated. Pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated.
     *
     * @param ticket     Unique number of the order ticket.
     * @param price      New open price of the pending order.
     * @param stoploss   New StopLoss level.
     * @param takeprofit New TakeProfit level.
     * @param expiration Pending order expiration time.
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return Modification of characteristics for the previously opened position or pending orders. If the function succeeds, the returned value will be TRUE. If the function fails, the returned value will be FALSE. To get the detailed error information, call GetLastError() function. Notes: Open price and expiration time can be changed only for pending orders. If unchanged values are passed as the function parameters, the error 1 (ERR_NO_RESULT) will be generated. Pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated.
     */
    public boolean orderModify(int sessionID,
                               int ticket,
                               double price,
                               double stoploss,
                               double takeprofit,
                               java.util.Date expiration,
                               long arrowColor
    ) throws ErrCustomIndicatorError, ErrIntegerParameterExpected, ErrInvalidFunctionParamvalue, ErrInvalidPriceParam, ErrInvalidTicket, ErrUnknownSymbol, ErrTradeNotAllowed, ErrNoResult, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders;


    /**
     * Returns open price for the currently selected order. Order must be first selected by the OrderSelect() function.
     *
     * @return Returns open price for the currently selected order. Order must be first selected by the OrderSelect() function.
     */
    public double orderOpenPrice(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns open time for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns open time for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public java.util.Date orderOpenTime(int sessionID) throws ErrNoOrderSelected;


    /**
     * Prints information about the selected order in the log in the following format: ticket number; open time; trade operation; amount of lots; open price; Stop Loss; Take Profit; close time; close price; commission; swap; profit; comment; magic number; pending order expiration date. Order must be selected by the OrderSelect() function.
     *
     * @return Prints information about the selected order in the log in the following format: ticket number; open time; trade operation; amount of lots; open price; Stop Loss; Take Profit; close time; close price; commission; swap; profit; comment; magic number; pending order expiration date. Order must be selected by the OrderSelect() function.
     */
    public void orderPrint(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns the net profit value (without swaps or commissions) for the selected order. For open positions, it is the current unrealized profit. For closed orders, it is the fixed profit. Returns profit for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns the net profit value (without swaps or commissions) for the selected order. For open positions, it is the current unrealized profit. For closed orders, it is the fixed profit. Returns profit for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderProfit(int sessionID) throws ErrNoOrderSelected;


    /**
     * The function selects an order for further processing. It returns TRUE if the function succeeds. It returns FALSE if the function fails. To get the error information, one has to call the GetLastError() function. The pool parameter is ignored if the order is selected by the ticket number. The ticket number is a unique order identifier. To find out from what list the order has been selected, its close time must be analyzed. If the order close time equals to 0, the order is open or pending and taken from the terminal open positions list. One can distinguish an open position from a pending order by the order type. If the order close time does not equal to 0, the order is a closed order or a deleted pending order and was selected from the terminal history. They also differ from each other by their order types.
     *
     * @param index  Order index or order ticket depending on the second parameter.
     * @param select Selecting flags. It can be any of the following values: SELECT_BY_POS - index in the order pool, SELECT_BY_TICKET - index is order ticket.
     * @param pool   Optional order pool index. Used when the selected parameter is SELECT_BY_POS. It can be any of the following values: MODE_TRADES (default)- order selected from trading pool(opened and pending orders), MODE_HISTORY - order selected from history pool (closed and canceled order).
     * @return The function selects an order for further processing. It returns TRUE if the function succeeds. It returns FALSE if the function fails. To get the error information, one has to call the GetLastError() function. The pool parameter is ignored if the order is selected by the ticket number. The ticket number is a unique order identifier. To find out from what list the order has been selected, its close time must be analyzed. If the order close time equals to 0, the order is open or pending and taken from the terminal open positions list. One can distinguish an open position from a pending order by the order type. If the order close time does not equal to 0, the order is a closed order or a deleted pending order and was selected from the terminal history. They also differ from each other by their order types.
     */
    public boolean orderSelect(int sessionID,
                               int index,
                               SelectionType select,
                               SelectionPool pool
    );


    /**
     * The main function used to open a position or place a pending order. Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot be applied. If there has not been the requested open price in the price thread or it has not been normalized according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened at the current price and only if the current price lies within the range of price+-slippage. StopLoss and TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot be too close to the market. The minimal distance of the pending price from the current market one in points can be obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
     *
     * @param symbol     Symbol for trading.
     * @param cmd        Operation type. It can be any of the Trade operation enumeration.
     * @param volume     Number of lots.
     * @param price      Preferred price of the trade.
     * @param slippage   Maximum price slippage for buy or sell orders in points.
     * @param stoploss   StopLoss level.
     * @param takeprofit TakeProfit level.
     * @param comment    Order comment text. Last part of the comment may be changed by server.
     * @param magic      Order magic number. May be used as user defined identifier.
     * @param expiration Order expiration time (for pending orders only).
     * @param arrowColor Color of the arrow on the chart. If the parameter is missing or has CLR_NONE value closing arrow will not be drawn on the chart.
     * @return The main function used to open a position or place a pending order. Returns number of the ticket assigned to the order by the trade server or -1 if it fails. To get additional error information, one has to call the GetLastError() function. Notes: At opening of a market order (OP_SELL or OP_BUY), only the latest prices of Bid (for selling) or Ask (for buying) can be used as open price. If operation is performed with a security differing from the current one, the MarketInfo() function must be used with MODE_BID or MODE_ASK parameter for the latest quotes for this security to be obtained. Calculated or unnormalized price cannot be applied. If there has not been the requested open price in the price thread or it has not been normalized according to the amount of digits after decimal point, the error 129 (ERR_INVALID_PRICE) will be generated. If the requested open price is fully out of date, the error 138 (ERR_REQUOTE) will be generated independently on the slippage parameter. If the requested price is out of date, but present in the thread, the position will be opened at the current price and only if the current price lies within the range of price+-slippage. StopLoss and TakeProfit levels cannot be too close to the market. The minimal distance of stop levels in points can be obtained using the MarketInfo() function with MODE_STOPLEVEL parameter. In the case of erroneous or unnormalized stop levels, the error 130 (ERR_INVALID_STOPS) will be generated. At placing of a pending order, the open price cannot be too close to the market. The minimal distance of the pending price from the current market one in points can be obtained using the MarketInfo() function with the MODE_STOPLEVEL parameter. In case of false open price of a pending order, the error 130 (ERR_INVALID_STOPS) will be generated. Applying of pending order expiration time can be disabled in some trade servers. In this case, when a non-zero value is specified in the expiration parameter, the error 147 (ERR_TRADE_EXPIRATION_DENIED) will be generated. On some trade servers, the total amount of open and pending orders can be limited. If this limit has been exceeded, no new position will be opened (or no pending order will be placed) and trade server will return error 148 (ERR_TRADE_TOO_MANY_ORDERS).
     */
    public int orderSend(int sessionID,
                         String symbol,
                         TradeOperation cmd,
                         double volume,
                         double price,
                         int slippage,
                         double stoploss,
                         double takeprofit,
                         String comment,
                         int magic,
                         java.util.Date expiration,
                         long arrowColor
    ) throws ErrInvalidFunctionParamvalue, ErrCustomIndicatorError, ErrStringParameterExpected, ErrIntegerParameterExpected, ErrUnknownSymbol, ErrInvalidPriceParam, ErrTradeNotAllowed, ErrLongsNotAllowed, ErrShortsNotAllowed, ErrCommonError, ErrInvalidTradeParameters, ErrServerBusy, ErrOldVersion, ErrNoConnection, ErrTooFrequentRequests, ErrAccountDisabled, ErrInvalidAccount, ErrTradeTimeout, ErrInvalidPrice, ErrInvalidStops, ErrInvalidTradeVolume, ErrMarketClosed, ErrTradeDisabled, ErrNotEnoughMoney, ErrPriceChanged, ErrOffQuotes, ErrRequote, ErrOrderLocked, ErrLongPositionsOnlyAllowed, ErrTooManyRequests, ErrTradeTimeout2, ErrTradeTimeout3, ErrTradeTimeout4, ErrTradeModifyDenied, ErrTradeContextBusy, ErrTradeExpirationDenied, ErrTradeTooManyOrders;


    /**
     * Returns the number of closed orders in the account history loaded into the terminal. The history list size depends on the current settings of the Account history tab of the terminal.
     *
     * @return Returns the number of closed orders in the account history loaded into the terminal. The history list size depends on the current settings of the Account history tab of the terminal.
     */
    public int ordersHistoryTotal(int sessionID);


    /**
     * Returns stop loss value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns stop loss value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderStopLoss(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns market and pending orders count.
     *
     * @return Returns market and pending orders count.
     */
    public int ordersTotal(int sessionID);


    /**
     * Returns swap value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns swap value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderSwap(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns the order symbol value for selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns the order symbol value for selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public String orderSymbol(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns take profit value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns take profit value for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public double orderTakeProfit(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     *
     * @return Returns ticket number for the currently selected order. Note: The order must be previously selected by the OrderSelect() function.
     */
    public int orderTicket(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns order operation type for the currently selected order. It can be any of the following values: OP_BUY - buying position, OP_SELL - selling position, OP_BUYLIMIT - buy limit pending position, OP_BUYSTOP - buy stop pending position, OP_SELLLIMIT - sell limit pending position, OP_SELLSTOP - sell stop pending position. Note: order must be selected by OrderSelect() function.
     *
     * @return Returns order operation type for the currently selected order. It can be any of the following values: OP_BUY - buying position, OP_SELL - selling position, OP_BUYLIMIT - buy limit pending position, OP_BUYSTOP - buy stop pending position, OP_SELLLIMIT - sell limit pending position, OP_SELLSTOP - sell stop pending position. Note: order must be selected by OrderSelect() function.
     */
    public int orderType(int sessionID) throws ErrNoOrderSelected;


    /**
     * Returns TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE.
     *
     * @return Returns TRUE if a thread for trading is occupied by another expert advisor, otherwise returns FALSE.
     */
    public boolean isTradeContextBusy(int sessionID);


    /**
     * Refreshing of data in pre-defined variables and series arrays. This function is used when expert advisor has been calculating for a long time and needs data refreshing. Returns TRUE if data are refreshed, otherwise returns FALSE. The only reason for data cannot be refreshed is that they are the current data of the client terminal. Experts and scripts operate with their own copy of history data. Data of the current symbol are copied at the first launch of the expert or script. At each subsequent launch of the expert (remember that script is executed only once and does not depend on incoming ticks), the initial copy will be updated. One or more new ticks can income while the expert or script is operating, and data can become out of date.
     *
     * @return Refreshing of data in pre-defined variables and series arrays. This function is used when expert advisor has been calculating for a long time and needs data refreshing. Returns TRUE if data are refreshed, otherwise returns FALSE. The only reason for data cannot be refreshed is that they are the current data of the client terminal. Experts and scripts operate with their own copy of history data. Data of the current symbol are copied at the first launch of the expert or script. At each subsequent launch of the expert (remember that script is executed only once and does not depend on incoming ticks), the initial copy will be updated. One or more new ticks can income while the expert or script is operating, and data can become out of date.
     */
    public boolean refreshRates(int sessionID);


    /**
     * Returns the value of the Stop Out level.
     *
     * @return Returns the value of the Stop Out level.
     */
    public int accountStopoutLevel(int sessionID);


    /**
     * Returns the calculation mode for the Stop Out level. Calculation mode can take the following values: 0 - calculation of percentage ratio between margin and equity; 1 - comparison of the free margin level to the absolute value.
     *
     * @return Returns the calculation mode for the Stop Out level. Calculation mode can take the following values: 0 - calculation of percentage ratio between margin and equity; 1 - comparison of the free margin level to the absolute value.
     */
    public int accountStopoutMode(int sessionID);


    /**
     * The MessageBox function creates, displays, and operates message box. The message box contains an application-defined message and header, as well as a random combination of predefined icons and push buttons. If the function succeeds, the returned value is one of the MessageBox return code values.The function cannot be called from custom indicators since they are executed within interface thread and may not decelerate it.
     *
     * @param text    Text that contains the message to be displayed.
     * @param caption Text to be displayed in the header of the dialog box. If this parameter is NULL, the expert name will be displayed in the header.
     * @param flags   Flags that determine the type and behavior of the dialog box (see enumeration class MessageBoxFlag). They can represent a conbination of flags from the following groups.
     * @return The MessageBox function creates, displays, and operates message box. The message box contains an application-defined message and header, as well as a random combination of predefined icons and push buttons. If the function succeeds, the returned value is one of the MessageBox return code values.The function cannot be called from custom indicators since they are executed within interface thread and may not decelerate it.
     */
    public int messageBox(int sessionID,
                          String text,
                          String caption,
                          int flags
    );


    /**
     * Returns the code of the uninitialization reason for the experts, custom indicators, and scripts. The returned values can be ones of Uninitialize reason codes. This function can also be called in function init() to analyze the reasons for deinitialization of the previous launch.
     *
     * @return Returns the code of the uninitialization reason for the experts, custom indicators, and scripts. The returned values can be ones of Uninitialize reason codes. This function can also be called in function init() to analyze the reasons for deinitialization of the previous launch.
     */
    public int uninitializeReason(int sessionID);


    /**
     * Returns TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns FALSE.
     *
     * @return Returns TRUE if the expert is allowed to trade and a thread for trading is not occupied, otherwise returns FALSE.
     */
    public boolean isTradeAllowed(int sessionID);


    /**
     * Returns TRUE if the program (an expert or a script) has been commanded to stop its operation, otherwise returns FALSE. The program can continue operation for 2.5 seconds more before the client terminal stops its performing forcedly.
     *
     * @return Returns TRUE if the program (an expert or a script) has been commanded to stop its operation, otherwise returns FALSE. The program can continue operation for 2.5 seconds more before the client terminal stops its performing forcedly.
     */
    public boolean isStopped(int sessionID);


    /**
     * Returns TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert runs in the strategy tester optimization mode, otherwise returns FALSE.
     */
    public boolean isOptimization(int sessionID);


    /**
     * Returns TRUE if the expert can call library function, otherwise returns FALSE. See also IsDllsAllowed(), IsTradeAllowed().
     *
     * @return Returns TRUE if the expert can call library function, otherwise returns FALSE. See also IsDllsAllowed(), IsTradeAllowed().
     */
    public boolean isLibrariesAllowed(int sessionID);


    /**
     * Returns TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE.
     *
     * @return Returns TRUE if the function DLL call is allowed for the expert, otherwise returns FALSE.
     */
    public boolean isDllsAllowed(int sessionID);


    /**
     * Returns TRUE if expert advisors are enabled for running, otherwise returns FALSE.
     *
     * @return Returns TRUE if expert advisors are enabled for running, otherwise returns FALSE.
     */
    public boolean isExpertEnabled(int sessionID);


    /**
     * Returns free margin that remains after the specified position has been opened at the current price on the current account. If the free margin is insufficient, an error 134 (ERR_NOT_ENOUGH_MONEY) will be generated.
     *
     * @param symbol Symbol for trading operation.
     * @param cmd    Operation type. It can be either OP_BUY or OP_SELL.
     * @param volume Number of lots.
     * @return Returns free margin that remains after the specified position has been opened at the current price on the current account. If the free margin is insufficient, an error 134 (ERR_NOT_ENOUGH_MONEY) will be generated.
     */
    public double accountFreeMarginCheck(int sessionID,
                                         String symbol,
                                         TradeOperation cmd,
                                         double volume
    );


    /**
     * Calculation mode of free margin allowed to open positions on the current account. The calculation mode can take the following values: 0 - floating profit/loss is not used for calculation; 1 - both floating profit and loss on open positions on the current account are used for free margin calculation; 2 - only profit value is used for calculation, the current loss on open positions is not considered; 3 - only loss value is used for calculation, the current loss on open positions is not considered.
     *
     * @return Calculation mode of free margin allowed to open positions on the current account. The calculation mode can take the following values: 0 - floating profit/loss is not used for calculation; 1 - both floating profit and loss on open positions on the current account are used for free margin calculation; 2 - only profit value is used for calculation, the current loss on open positions is not considered; 3 - only loss value is used for calculation, the current loss on open positions is not considered.
     */
    public double accountFreeMarginMode(int sessionID);


    /**
     * Returns leverage of the current account.
     *
     * @return Returns leverage of the current account.
     */
    public int accountLeverage(int sessionID);


    /**
     * Returns the connected server name.
     *
     * @return Returns the connected server name.
     */
    public String accountServer(int sessionID);


    /**
     * Returns the name of company owning the client terminal.
     *
     * @return Returns the name of company owning the client terminal.
     */
    public String terminalCompany(int sessionID);


    /**
     * Returns client terminal name.
     *
     * @return Returns client terminal name.
     */
    public String terminalName(int sessionID);


    /**
     * Returns the directory, from which the client terminal was launched.
     *
     * @return Returns the directory, from which the client terminal was launched.
     */
    public String terminalPath(int sessionID);


    /**
     * Displays a dialog box containing the user-defined data.
     *
     * @param arg Any value.
     * @return Displays a dialog box containing the user-defined data.
     */
    public void alert(int sessionID,
                      String arg
    );


    /**
     * Function plays a sound file. The file must be located in the terminal_dir\sounds directory (see TerminalPath()) or in its subdirectory.
     *
     * @param filename Path to the sound file.
     * @return Function plays a sound file. The file must be located in the terminal_dir\sounds directory (see TerminalPath()) or in its subdirectory.
     */
    public void playSound(int sessionID,
                          String filename
    );


    /**
     * Return object description. For objects of OBJ_TEXT and OBJ_LABEL types, the text drawn by these objects will be returned.
     *
     * @param name Object name.
     * @return Return object description. For objects of OBJ_TEXT and OBJ_LABEL types, the text drawn by these objects will be returned.
     */
    public String objectDescription(int sessionID,
                                    String name
    );


    /**
     * Search for an object having the specified name. The function returns index of the windows that contains the object to be found. If it fails, the returned value will be -1. To get the detailed error information, one has to call the GetLastError() function. The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index.
     *
     * @param name Object name to search for.
     * @return Search for an object having the specified name. The function returns index of the windows that contains the object to be found. If it fails, the returned value will be -1. To get the detailed error information, one has to call the GetLastError() function. The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index.
     */
    public int objectFind(int sessionID,
                          String name
    );


    /**
     * The function calculates and returns bar index (shift related to the current bar) for the given price. The bar index is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object name.
     * @param value Price value.
     * @return The function calculates and returns bar index (shift related to the current bar) for the given price. The bar index is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     */
    public int objectGetShiftByValue(int sessionID,
                                     String name,
                                     double value
    );


    /**
     * The function calculates and returns the price value for the specified bar (shift related to the current bar). The price value is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param name  Object name.
     * @param shift Bar index.
     * @return The function calculates and returns the price value for the specified bar (shift related to the current bar). The price value is calculated by the first and second coordinates using a linear equation. Applied to trendlines and similar objects. To get the detailed error information, one has to call the GetLastError() function.
     */
    public double objectGetValueByShift(int sessionID,
                                        String name,
                                        int shift
    );


    /**
     * The function moves an object coordinate in the chart. Objects can have from one to three coordinates depending on their types. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. The object coordinates are numbered starting from 0.
     *
     * @param name   Object name.
     * @param point  Coordinate index (0-2).
     * @param time1  New time value.
     * @param price1 New price value.
     * @return The function moves an object coordinate in the chart. Objects can have from one to three coordinates depending on their types. If the function succeeds, the returned value will be TRUE. Otherwise, it will be FALSE. To get the detailed error information, one has to call the GetLastError() function. The object coordinates are numbered starting from 0.
     */
    public boolean objectMove(int sessionID,
                              String name,
                              int point,
                              java.util.Date time1,
                              double price1
    );


    /**
     * The function returns the object name by its index in the objects list. To get the detailed error information, one has to call the GetLastError() function.
     *
     * @param index Object index in the objects list. Object index must exceed or equal to 0 and be less than ObjectsTotal().
     * @return The function returns the object name by its index in the objects list. To get the detailed error information, one has to call the GetLastError() function.
     */
    public String objectName(int sessionID,
                             int index
    );


    /**
     * Removes all objects of the specified type and in the specified sub-window of the chart. The function returns the count of removed objects. To get the detailed error information, one has to call the GetLastError() function. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. If the window index is missing or it has the value of -1, the objects will be removed from the entire chart. If the type value equals to -1 or this parameter is missing, all objects will be removed from the specified sub-window.
     *
     * @param window Index of the window in which the objects will be deleted. Must exceed or equal to -1 (EMPTY, the default value) and be less than WindowsTotal().
     * @param type   An object type to be deleted. It can be any of the Object type enumeration values or EMPTY constant to delete all objects with any types.
     * @return Removes all objects of the specified type and in the specified sub-window of the chart. The function returns the count of removed objects. To get the detailed error information, one has to call the GetLastError() function. Notes: The chart sub-windows (if there are sub-windows with indicators in the chart) are numbered starting from 1. The chart main window always exists and has the 0 index. If the window index is missing or it has the value of -1, the objects will be removed from the entire chart. If the type value equals to -1 or this parameter is missing, all objects will be removed from the specified sub-window.
     */
    public int objectsDeleteAll(int sessionID,
                                int window,
                                int type
    );


    /**
     * Calculates the Ichimoku Kinko Hyo and returns its value.
     *
     * @param symbol        Symbol name of the security on the data of which the indicator will be calculated. NULL means the current symbol.
     * @param timeframe     Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @param tenkan_sen    Tenkan Sen averaging period.
     * @param kijun_sen     Kijun Sen averaging period.
     * @param senkou_span_b Senkou SpanB averaging period.
     * @param mode          Source of data. It can be one of the Ichimoku Kinko Hyo mode enumeration.
     * @param shift         Index of the value taken from the indicator buffer (shift relative to the current bar the given amount of periods ago).
     * @return Calculates the Ichimoku Kinko Hyo and returns its value.
     */
    public double iIchimoku(int sessionID,
                            String symbol,
                            Timeframe timeframe,
                            int tenkan_sen,
                            int kijun_sen,
                            int senkou_span_b,
                            IchimokuSource mode,
                            int shift
    ) throws ErrUnknownSymbol;


    /**
     * The function sets a flag hiding indicators called by the Expert Advisor. After the expert has been tested and the appropriate chart opened, the flagged indicators will not be drawn in the testing chart. Every indicator called will first be flagged with the current hiding flag. It must be noted that only those indicators can be drawn in the testing chart that are directly called from the expert under test.
     *
     * @param shift TRUE, if there is a need to hide indicators, or else FALSE.
     * @return The function sets a flag hiding indicators called by the Expert Advisor. After the expert has been tested and the appropriate chart opened, the flagged indicators will not be drawn in the testing chart. Every indicator called will first be flagged with the current hiding flag. It must be noted that only those indicators can be drawn in the testing chart that are directly called from the expert under test.
     */
    public void hideTestIndicators(int sessionID,
                                   boolean shift
    );


    /**
     * Returns the amount of minutes determining the used period (chart timeframe).
     *
     * @return Returns the amount of minutes determining the used period (chart timeframe).
     */
    public int period(int sessionID);


    /**
     * Returns a text string with the name of the current financial instrument.
     *
     * @return Returns a text string with the name of the current financial instrument.
     */
    public String symbol(int sessionID);


    /**
     * Function returns the amount of bars visible on the chart.
     *
     * @return Function returns the amount of bars visible on the chart.
     */
    public int windowBarsPerChart(int sessionID);


    /**
     * The function returns the first visible bar number in the current chart window. It must be taken into consideration that price bars are numbered in the reverse order, from the last to the first one. The current bar, the latest in the price array, is indexed as 0. The oldest bar is indexed as Bars-1. If the first visible bar number is 2 or more bars less than the amount of visible bars in the chart, it means that the chart window has not been fully filled out and there is a space to the left.
     *
     * @return The function returns the first visible bar number in the current chart window. It must be taken into consideration that price bars are numbered in the reverse order, from the last to the first one. The current bar, the latest in the price array, is indexed as 0. The oldest bar is indexed as Bars-1. If the first visible bar number is 2 or more bars less than the amount of visible bars in the chart, it means that the chart window has not been fully filled out and there is a space to the left.
     */
    public int windowFirstVisibleBar(int sessionID);


    /**
     * Returns name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from which this function has been called.
     *
     * @return Returns name of the executed expert, script, custom indicator, or library, depending on the MQL4 program, from which this function has been called.
     */
    public String windowExpertName(int sessionID);


    /**
     * If indicator with name was found, the function returns the window index containing this specified indicator, otherwise it returns -1. Note: WindowFind() returns -1 if custom indicator searches itself when init() function works.
     *
     * @param name Indicator short name.
     * @return If indicator with name was found, the function returns the window index containing this specified indicator, otherwise it returns -1. Note: WindowFind() returns -1 if custom indicator searches itself when init() function works.
     */
    public int windowFind(int sessionID,
                          String name
    );


    /**
     * Returns TRUE if the chart subwindow is visible, otherwise returns FALSE. The chart subwindow can be hidden due to the visibility properties of the indicator placed in it.
     *
     * @param index Chart subwindow index.
     * @return Returns TRUE if the chart subwindow is visible, otherwise returns FALSE. The chart subwindow can be hidden due to the visibility properties of the indicator placed in it.
     */
    public boolean windowIsVisible(int sessionID,
                                   int index
    );


    /**
     * Returns maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the maximal value of the price scale of the main chart window is returned.
     *
     * @param index Chart subwindow index (0 - main chart window).
     * @return Returns maximal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the maximal value of the price scale of the main chart window is returned.
     */
    public double windowPriceMax(int sessionID,
                                 int index
    );


    /**
     * Returns minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the minimal value of the price scale of the main chart window is returned.
     *
     * @param index Chart subwindow index (0 - main chart window).
     * @return Returns minimal value of the vertical scale of the specified subwindow of the current chart (0-main chart window, the indicators' subwindows are numbered starting from 1). If the subwindow index has not been specified, the minimal value of the price scale of the main chart window is returned.
     */
    public double windowPriceMin(int sessionID,
                                 int index
    );


    /**
     * Returns window index where expert, custom indicator or script was dropped. This value is valid if the expert, custom indicator or script was dropped by mouse. Note: For custom indicators being initialized (call from the init() function), this index is not defined. The returned index is the number of window (0-chart main menu, subwindows of indicators are numbered starting from 1) where the custom indicator is working. A custom indicator can create its own new subwindow during its work, and the number of this subwindow will differ from that of the window where the indicator was really dropped in.
     *
     * @return Returns window index where expert, custom indicator or script was dropped. This value is valid if the expert, custom indicator or script was dropped by mouse. Note: For custom indicators being initialized (call from the init() function), this index is not defined. The returned index is the number of window (0-chart main menu, subwindows of indicators are numbered starting from 1) where the custom indicator is working. A custom indicator can create its own new subwindow during its work, and the number of this subwindow will differ from that of the window where the indicator was really dropped in.
     */
    public int windowOnDropped(int sessionID);


    /**
     * Returns the value at X axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     *
     * @return Returns the value at X axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     */
    public int windowXOnDropped(int sessionID);


    /**
     * Returns the value at Y axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     *
     * @return Returns the value at Y axis in pixels for the chart window client area point at which the expert or script was dropped. The value will be true only if the expert or script were moved with the mouse (Drag_n_Drop) technique.
     */
    public int windowYOnDropped(int sessionID);


    /**
     * Returns the price part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     *
     * @return Returns the price part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     */
    public double windowPriceOnDropped(int sessionID);


    /**
     * Returns the time part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     *
     * @return Returns the time part of the chart point where expert or script was dropped. This value is only valid if the expert or script was dropped by mouse. Note: For custom indicators, this value is undefined.
     */
    public java.util.Date windowTimeOnDropped(int sessionID);


    /**
     * Returns count of indicator windows on the chart (including main chart).
     *
     * @return Returns count of indicator windows on the chart (including main chart).
     */
    public int windowsTotal(int sessionID);


    /**
     * Redraws the current chart forcedly. It is normally used after the objects properties have been changed.
     *
     * @return Redraws the current chart forcedly. It is normally used after the objects properties have been changed.
     */
    public void windowRedraw(int sessionID);


    /**
     * Saves current chart screen shot as a GIF file. Returns FALSE if it fails. To get the error code, one has to use the GetLastError() function. The screen shot is saved in the terminal_dir\experts\files (terminal_dir\tester\files in case of testing) directory or its subdirectories.
     *
     * @param filename   Screen shot file name.
     * @param sizeX      Screen shot width in pixels.
     * @param sizeY      Screen shot height in pixels.
     * @param startBar   Index of the first visible bar in the screen shot. If 0 value is set, the current first visible bar will be shot. If no value or negative value has been set, the end-of-chart screen shot will be produced, indent being taken into consideration.
     * @param chartScale Horizontal chart scale for screen shot. Can be in the range from 0 to 5. If no value or negative value has been set, the current chart scale will be used.
     * @param chartMode  Chart displaying mode. It can take the following values: CHART_BAR (0 is a sequence of bars), CHART_CANDLE (1 is a sequence of candlesticks), CHART_LINE (2 is a close prices line). If no value or negative value has been set, the chart will be shown in its current mode.
     * @return Saves current chart screen shot as a GIF file. Returns FALSE if it fails. To get the error code, one has to use the GetLastError() function. The screen shot is saved in the terminal_dir\experts\files (terminal_dir\tester\files in case of testing) directory or its subdirectories.
     */
    public boolean windowScreenShot(int sessionID,
                                    String filename,
                                    int sizeX,
                                    int sizeY,
                                    int startBar,
                                    int chartScale,
                                    int chartMode
    );


    /**
     * Returns the system window handler containing the given chart. If the chart of symbol and timeframe has not been opened by the moment of function calling, 0 will be returned.
     *
     * @param symbol    symbol name.
     * @param timeframe Timeframe. It can be any of Timeframe enumeration values. 0 means the current chart timeframe.
     * @return Returns the system window handler containing the given chart. If the chart of symbol and timeframe has not been opened by the moment of function calling, 0 will be returned.
     */
    public int windowHandle(int sessionID,
                            String symbol,
                            Timeframe timeframe
    );
}
