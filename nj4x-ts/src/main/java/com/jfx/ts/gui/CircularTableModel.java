package com.jfx.ts.gui;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Limited size ring (FIFO) table model.  回环数据类型
 * Created by roman on 26-Mar-15.
 */
public class CircularTableModel extends AbstractTableModel {
    /**
     * The <code>CircularArrayList</code> of <code>ArrayLists</code> of
     * <code>Object</code> values.
     */
    protected CircularArrayList<ArrayList<Object>> dataVector;

    /**
     * The <code>ArrayList<Object></code> of column identifiers.
     */
    protected ArrayList<Object> columnIdentifiers;

    protected int capacity;

    public int getCapacity() {
        return capacity;
    }

    /**
     * Constructs a default <code>CircularTableModel</code>
     * which is a table of zero columns and zero rows.
     */
    public CircularTableModel(int capacity) {
        this(capacity, 0, 0);
    }

    private static ArrayList<Object> newVector(int size) {
        ArrayList<Object> v = new ArrayList<Object>(size);
        while (v.size() < size) v.add(null);
        return v;
    }

    private static CircularArrayList<ArrayList<Object>> newVector(int capacity, int size) {
        return new CircularArrayList<>(Math.max(capacity, size));
    }

    /**
     * Constructs a <code>CircularTableModel</code> with
     * <code>rowCount</code> and <code>columnCount</code> of
     * <code>null</code> object values.
     *
     * @param rowCount    the number of rows the table holds
     * @param columnCount the number of columns the table holds
     * @see #setValueAt
     */
    public CircularTableModel(int capacity, int rowCount, int columnCount) {
        this(capacity, newVector(columnCount), rowCount);
    }

    /**
     * Constructs a <code>CircularTableModel</code> with as many columns
     * as there are elements in <code>columnNames</code>
     * and <code>rowCount</code> of <code>null</code>
     * object values.  Each column's name will be taken from
     * the <code>columnNames</code> ArrayList<Object>.
     *
     * @param columnNames <code>ArrayList<Object></code> containing the names
     *                    of the new columns; if this is
     *                    <code>null</code> then the model has no columns
     * @param rowCount    the number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    public CircularTableModel(int capacity, ArrayList<Object> columnNames, int rowCount) {
        setDataVector(capacity, newVector(capacity, rowCount), columnNames);
    }

    /**
     * Constructs a <code>CircularTableModel</code> with as many
     * columns as there are elements in <code>columnNames</code>
     * and <code>rowCount</code> of <code>null</code>
     * object values.  Each column's name will be taken from
     * the <code>columnNames</code> array.
     *
     * @param columnNames <code>array</code> containing the names
     *                    of the new columns; if this is
     *                    <code>null</code> then the model has no columns
     * @param rowCount    the number of rows the table holds
     * @see #setDataVector
     * @see #setValueAt
     */
    public CircularTableModel(int capacity, Object[] columnNames, int rowCount) {
        this(capacity, convertToVector(columnNames), rowCount);
    }

    /**
     * Constructs a <code>CircularTableModel</code> and initializes the table
     * by passing <code>data</code> and <code>columnNames</code>
     * to the <code>setDataVector</code> method.
     *
     * @param data        the data of the table, a <code>ArrayList<Object></code>
     *                    of <code>ArrayList<Object></code>s of <code>Object</code>
     *                    values
     * @param columnNames <code>ArrayList<Object></code> containing the names
     *                    of the new columns
     * @see #getDataVector
     * @see #setDataVector
     */
    public CircularTableModel(CircularArrayList<ArrayList<Object>> data, ArrayList<Object> columnNames) {
        setDataVector(data.capacity(), data, columnNames);
    }

    /**
     * Constructs a <code>CircularTableModel</code> and initializes the table
     * by passing <code>data</code> and <code>columnNames</code>
     * to the <code>setDataVector</code>
     * method. The first index in the <code>Object[][]</code> array is
     * the row index and the second is the column index.
     *
     * @param data        the data of the table
     * @param columnNames the names of the columns
     * @see #getDataVector
     * @see #setDataVector
     */
    public CircularTableModel(int capacity, Object[][] data, Object[] columnNames) {
        setDataVector(capacity, data, columnNames);
    }

    /**
     * Returns the <code>ArrayList<Object></code> of <code>Vectors</code>
     * that contains the table's
     * data values.  The vectors contained in the outer ArrayList<Object> are
     * each a single row of values.  In other words, to get to the cell
     * at row 1, column 5: <p>
     * <p/>
     * <code>((ArrayList<Object>)getDataVector().elementAt(1)).elementAt(5);</code>
     *
     * @return the ArrayList<Object> of vectors containing the tables data values
     * @see #newDataAvailable
     * @see #newRowsAdded
     * @see #setDataVector
     */
    public CircularArrayList<ArrayList<Object>> getDataVector() {
        return dataVector;
    }

    private static ArrayList<Object> nonNullVector(ArrayList<Object> v) {
        return (v != null) ? v : new ArrayList<>();
    }

    private static CircularArrayList<ArrayList<Object>> nonNullVector(int capacity, CircularArrayList<ArrayList<Object>> v) {
        return (v != null) ? v : new CircularArrayList<ArrayList<Object>>(capacity);
    }

    /**
     * Replaces the current <code>dataVector</code> instance variable
     * with the new <code>ArrayList<Object></code> of rows, <code>dataVector</code>.
     * Each row is represented in <code>dataVector</code> as a
     * <code>ArrayList<Object></code> of <code>Object</code> values.
     * <code>columnIdentifiers</code> are the names of the new
     * columns.  The first name in <code>columnIdentifiers</code> is
     * mapped to column 0 in <code>dataVector</code>. Each row in
     * <code>dataVector</code> is adjusted to match the number of
     * columns in <code>columnIdentifiers</code>
     * either by truncating the <code>ArrayList<Object></code> if it is too long,
     * or adding <code>null</code> values if it is too short.
     * <p>Note that passing in a <code>null</code> value for
     * <code>dataVector</code> results in unspecified behavior,
     * an possibly an exception.
     *
     * @param dataVector        the new data ArrayList<Object>
     * @param columnIdentifiers the names of the columns
     * @see #getDataVector
     */
    public void setDataVector(int capacity, CircularArrayList<ArrayList<Object>> dataVector, ArrayList<Object> columnIdentifiers) {
        this.columnIdentifiers = nonNullVector(columnIdentifiers);
        this.dataVector = nonNullVector(capacity, dataVector);
        this.capacity = this.dataVector.capacity();
        fireTableStructureChanged();
    }

    public void setCapacity(int capacity) {
        this.dataVector.setCapacity(capacity);
        this.capacity = capacity;
        fireTableDataChanged();
    }

    /**
     * Replaces the value in the <code>dataVector</code> instance
     * variable with the values in the array <code>dataVector</code>.
     * The first index in the <code>Object[][]</code>
     * array is the row index and the second is the column index.
     * <code>columnIdentifiers</code> are the names of the new columns.
     *
     * @param dataVector        the new data ArrayList<Object>
     * @param columnIdentifiers the names of the columns
     */
    public void setDataVector(int capacity, Object[][] dataVector, Object[] columnIdentifiers) {
        setDataVector(capacity, convertToVector(capacity, dataVector), convertToVector(columnIdentifiers));
    }

    /**
     * Equivalent to <code>fireTableChanged</code>.
     *
     * @param event the change event
     */
    public void newDataAvailable(TableModelEvent event) {
        fireTableChanged(event);
    }

    /**
     * Ensures that the new rows have the correct number of columns.
     * This is accomplished by  using the <code>setSize</code> method in
     * <code>ArrayList<Object></code> which truncates vectors
     * which are too long, and appends <code>null</code>s if they
     * are too short.
     * This method also sends out a <code>tableChanged</code>
     * notification message to all the listeners.
     *
     * @param e this <code>TableModelEvent</code> describes
     *          where the rows were added.
     *          If <code>null</code> it assumes
     *          all the rows were newly added
     * @see #getDataVector
     */
    public void newRowsAdded(TableModelEvent e) {
        fireTableChanged(e);
    }

    /**
     * Equivalent to <code>fireTableChanged</code>.
     *
     * @param event the change event
     */
    public void rowsRemoved(TableModelEvent event) {
        fireTableChanged(event);
    }

    /**
     * Sets the number of rows in the model.  If the new size is greater
     * than the current size, new rows are added to the end of the model
     * If the new size is less than the current size, all
     * rows at index <code>rowCount</code> and greater are discarded.
     *
     * @param rowCount the new number of rows
     * @see #setRowCount
     */
    public void setNumRows(int rowCount) {
        int old = getRowCount();
        if (old == rowCount/* || rowCount > dataVector.capacity()*/) {
            return;
        }
        if (rowCount > dataVector.capacity()) {
            throw new IndexOutOfBoundsException();
        }
        //
        while (rowCount < dataVector.size()) {
            dataVector.removeOldest();
        }
        while (rowCount > dataVector.size()) {
            ArrayList<Object> row = new ArrayList<>();
            setSize(row, getColumnCount());
            dataVector.insert(row);
        }
        fireTableDataChanged();
    }

    private void setSize(ArrayList<Object> list, int size) {
        while (list.size() < size) list.add(null);
    }

    /**
     * Sets the number of rows in the model.  If the new size is greater
     * than the current size, new rows are added to the end of the model
     * If the new size is less than the current size, all
     * rows at index <code>rowCount</code> and greater are discarded.
     */
    public void setRowCount(int rowCount) {
        setNumRows(rowCount);
    }

    /**
     * Adds a row to the end of the model.  The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     *
     * @param rowData optional data of the row being added
     */
    public void addRow(ArrayList<Object> rowData) {
        insertRow(getRowCount(), rowData);
    }

    /**
     * Adds a row to the end of the model.  The new row will contain
     * <code>null</code> values unless <code>rowData</code> is specified.
     * Notification of the row being added will be generated.
     *
     * @param rowData optional data of the row being added
     */
    public void addRow(Object[] rowData) {
        addRow(convertToVector(rowData));
    }

    /**
     * Inserts a row at <code>row</code> in the model.  The new row
     * will contain <code>null</code> values unless <code>rowData</code>
     * is specified.  Notification of the row being added will be generated.
     *
     * @param row     the row index of the row to be inserted
     * @param rowData optional data of the row being added
     * @throws ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void insertRow(int row, ArrayList<Object> rowData) {
        dataVector.insertElementAt(row, rowData);
//        fireTableRowsInserted(row, row);
        fireTableDataChanged();
    }

    /**
     * Inserts a row at <code>row</code> in the model.  The new row
     * will contain <code>null</code> values unless <code>rowData</code>
     * is specified.  Notification of the row being added will be generated.
     *
     * @param row     the row index of the row to be inserted
     * @param rowData optional data of the row being added
     * @throws ArrayIndexOutOfBoundsException if the row was invalid
     */
    public void insertRow(int row, Object[] rowData) {
        insertRow(row, convertToVector(rowData));
    }

    /**
     * Returns a ArrayList<Object> that contains the same objects as the array.
     *
     * @param anArray the array to be converted
     * @return the new ArrayList<Object>; if <code>anArray</code> is <code>null</code>,
     * returns <code>null</code>
     */
    protected static ArrayList<Object> convertToVector(Object[] anArray) {
        if (anArray == null) {
            return null;
        }
        ArrayList<Object> v = new ArrayList<>(anArray.length);
        Collections.addAll(v, anArray);
        return v;
    }

    /**
     * Returns a ArrayList<Object> of vectors that contains the same objects as the array.
     *
     * @param anArray the double array to be converted
     * @return the new ArrayList<Object> of vectors; if <code>anArray</code> is
     * <code>null</code>, returns <code>null</code>
     */
    protected static CircularArrayList<ArrayList<Object>> convertToVector(int capacity, Object[][] anArray) {
        if (anArray == null) {
            return null;
        }
        CircularArrayList<ArrayList<Object>> v = new CircularArrayList<>(Math.max(capacity, anArray.length));
        for (Object[] o : anArray) {
            v.insert(convertToVector(o));
        }
        return v;
    }

    /**
     * Returns the number of rows in this data table.
     *
     * @return the number of rows in the model
     */
    @Override
    public int getRowCount() {
        return dataVector.size();
    }

    /**
     * Returns the number of columns in this data table.
     *
     * @return the number of columns in the model
     */
    @Override
    public int getColumnCount() {
        return columnIdentifiers.size();
    }

    /**
     * Returns the column name.
     *
     * @return a name for this column using the string value of the
     * appropriate member in <code>columnIdentifiers</code>.
     * If <code>columnIdentifiers</code> does not have an entry
     * for this index, returns the default
     * name provided by the superclass.
     */
    @Override
    public String getColumnName(int column) {
        Object id = null;
        // This test is to cover the case when
        // getColumnCount has been subclassed by mistake ...
        if (column < columnIdentifiers.size() && (column >= 0)) {
            id = columnIdentifiers.get(column);
        }
        return (id == null) ? super.getColumnName(column)
                : id.toString();
    }

    /**
     * Returns true regardless of parameter values.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return true
     * @see #setValueAt
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    /**
     * Returns an attribute value for the cell at <code>row</code>
     * and <code>column</code>.
     *
     * @param row    the row whose value is to be queried
     * @param column the column whose value is to be queried
     * @return the value Object at the specified cell
     * @throws ArrayIndexOutOfBoundsException if an invalid row or
     *                                        column was given
     */
    @Override
    public Object getValueAt(int row, int column) {
        ArrayList<Object> rowVector = dataVector.get(row);
        return rowVector.get(column);
    }

    /**
     * Sets the object value for the cell at <code>column</code> and
     * <code>row</code>.  <code>aValue</code> is the new value.  This method
     * will generate a <code>tableChanged</code> notification.
     *
     * @param aValue the new value; this can be null
     * @param row    the row whose value is to be changed
     * @param column the column whose value is to be changed
     * @throws ArrayIndexOutOfBoundsException if an invalid row or
     *                                        column was given
     */
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        ArrayList<Object> rowVector = dataVector.get(row);
        rowVector.set(column, aValue);
        fireTableCellUpdated(row, column);
    }
}
