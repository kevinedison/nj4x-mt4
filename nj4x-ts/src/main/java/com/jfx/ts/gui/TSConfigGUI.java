package com.jfx.ts.gui;

import com.google.api.services.drive.model.User;
import com.jfx.ts.io.BoxUtils;
import com.jfx.ts.net.*;
import org.apache.log4j.AsyncAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipOutputStream;

import static com.jfx.ts.net.GDriveAccess.*;

/**
 * TS console GUI.
 * Created by roman on 24-Mar-15.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class TSConfigGUI {
    public static final int MIN_LOG_EVENTS_TO_KEEP = 10000;
    public static final String DURATION_COLUMN = "Duration (s)";
    public static final String SESSION_COLUMN = "Session";
    public static final String PATH_COLUMN = "Path";
    public static final String BROKER_COLUMN = "Broker";
    public static final String ACCOUNT_COLUMN = "Account";
    public static final String PID_COLUMN = "PID";
    public static final int KEY_PRESSED_ACTION_DELAY = 500;
    final ArrayList<TerminalParams> connectionsInProgress;  //正在进行连接的终端
    //
    public JPanel tsConfigGUIRoot;
    public JTextField maxTerminalsField;
    public JTextField maxSessionsField;
    private TS ts;
    private JTabbedPane tabbedPane1;
    private JTable connectionsInProgressTable;
    private JTable activeTerminalsTable;
    private JTextField versionTextField;
    private JTextField tsHomeField;
    private JButton tsHomeDirOpenBtn;
    private JButton termsDirOpenBtn;
    private JTextField termsDirField;
    private JCheckBox enableDebugInfoCheckBox;//选择调试信息的多选框
    private JCheckBox hideTerminalsCheckBox;
    private JTextField minDiskSpaceField;
    private JPanel connectionsInProgressPanel;  //进入连接
    private JPanel activeTerminalsPanel;
    private JTable logTable;
    private JTextField filterField;
    private JTextField limitRowsField;//设置log事件数量的上限
    private JButton collectReportButton;
    private JButton clearButton;
    private JLabel maxSessionsLabel;
    private JTextField connTimeoutField;
    private JCheckBox deployEaWsCheckBox;  //是否部署专家系统的选择框
    private JCheckBox useGDrive;
    private JPanel GDrive;
    private JComboBox srvAtGDrive;
    private JLabel gDriveOwnerIcon;
    private JTextField gDriveOwner;
    private JComboBox zeroTermAtGDrive;
    private JLabel nextSyncClock;
    private JTextField updateInterval;
    private JComboBox zeroTermMt5AtGDrive;
    private JPanel zeroTermMt5AtGDrivePanel;
    private JLabel conectionTimeoutLabel;
    private JLabel keepMinDiskSpaceLabel;
    private CircularArrayList<LoggingEvent> logEvents;  //应该是事件列表

    public TSConfigGUI() {
        connectionsInProgress = new ArrayList<>();//实例化正在进行连接的终端
        enableDebugInfoCheckBox.setSelected(TS.LOGGER.isDebugEnabled());//启动的时候读取log4j的配置，是否支持调试信息
        hideTerminalsCheckBox.setSelected(TerminalParams.SW_HIDE.equals("true")); //设置隐藏终端的选择框
        if (true || TS.NJ4X.endsWith("P5")) {   //检测NJ4X的版本是不是以P5结束的
            deployEaWsCheckBox.setVisible(false); //不显示是否部署专家系统的选择框
        } else {
            deployEaWsCheckBox.setSelected(TerminalServer.IS_DEPLOY_EA_WS); //显示选择框，但根据终端的设置来确定是否选中
        }
        //
        if (TS.P_GUI_ONLY) {//只显示iGUI的时候，不显示以下控件
            connectionsInProgressPanel.setVisible(false);
            hideTerminalsCheckBox.setVisible(false);
            connTimeoutField.setVisible(false);
            minDiskSpaceField.setVisible(false);
            conectionTimeoutLabel.setVisible(false);
            keepMinDiskSpaceLabel.setVisible(false);
            deployEaWsCheckBox.setVisible(false);
        }
        //
        logEvents = new CircularArrayList<>(Math.max(Integer.parseInt(limitRowsField.getText()), MIN_LOG_EVENTS_TO_KEEP));//实例化日志事件
//        动态设置log4j的配置，主要是处理log在界面的显示，其实不用这样也可以很简单的实现
        Logger.getRootLogger().addAppender(new AsyncAppender() {
            long eventNo = 0;

            @Override
            public void append(final LoggingEvent event) {
                try {
//                    Thread.currentThread().setName("GUI");
                    //
                    event.setProperty("id", String.valueOf(this.eventNo++));
                    synchronized (logTable) {
                        logEvents.insert(event);//将日志加入到日志列表
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isFilterOkForEvent(event)) {
                                    CircularTableModel model = (CircularTableModel) logTable.getModel();
                                    model.addRow(toDataRow(event));//界面显示log
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Throwable e) {
                    TS.LOGGER.error("GUI log error", e);
                }
            }

            @Override
            public void close() {

            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
        });
        //
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
        int col;
        //设置等待连接table
        connectionsInProgressTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new Object[]{BROKER_COLUMN, ACCOUNT_COLUMN, "Start Time", DURATION_COLUMN, PATH_COLUMN}
        ));
        //以下设置字段的样式，长宽
        TableColumnModel connectionsInProgressColumnModel = connectionsInProgressTable.getColumnModel();
        col = 0;
        // Broker
        connectionsInProgressColumnModel.getColumn(col).setMinWidth(100);
        connectionsInProgressColumnModel.getColumn(col).setMaxWidth(240);
        connectionsInProgressColumnModel.getColumn(col++).setPreferredWidth(80);
        // Account
        connectionsInProgressColumnModel.getColumn(col).setMinWidth(100);
        connectionsInProgressColumnModel.getColumn(col).setMaxWidth(120);
        connectionsInProgressColumnModel.getColumn(col++).setPreferredWidth(80);
        // Start Time
        connectionsInProgressColumnModel.getColumn(col).setCellRenderer(rightRenderer);
        connectionsInProgressColumnModel.getColumn(col).setMinWidth(90);
        connectionsInProgressColumnModel.getColumn(col).setMaxWidth(120);
        connectionsInProgressColumnModel.getColumn(col++).setPreferredWidth(120);
        // Duration
        connectionsInProgressColumnModel.getColumn(col).setCellRenderer(rightRenderer);
        connectionsInProgressColumnModel.getColumn(col).setMinWidth(70);
        connectionsInProgressColumnModel.getColumn(col).setMaxWidth(90);
        connectionsInProgressColumnModel.getColumn(col++).setPreferredWidth(90);
        // Path
        connectionsInProgressColumnModel.getColumn(col).setMinWidth(200);
        connectionsInProgressColumnModel.getColumn(col++).setPreferredWidth(500);
        connectionsInProgressTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        int durationColumnIndex = ((DefaultTableModel) connectionsInProgressTable.getModel()).findColumn(DURATION_COLUMN);
        //设置排序
        TableRowSorter rowSorter = (TableRowSorter) connectionsInProgressTable.getRowSorter();
        Comparator<Integer> integerComparator = new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        };
        rowSorter.setComparator(durationColumnIndex, integerComparator);
        rowSorter.toggleSortOrder(durationColumnIndex);
        rowSorter.toggleSortOrder(durationColumnIndex);
        //每一秒执行一次
        TS.scheduledExecutorService.schedule(new Runnable() {
            public void run() {
//                将正在连接的终端列表下乳这个map
                final HashMap<String, TerminalParams> tp = new HashMap<>();
                synchronized (connectionsInProgress) {
                    for (TerminalParams p : connectionsInProgress) {
                        tp.put(p.getTerminalDirPathName(), p);
                    }
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (connectionsInProgressTable) {
                                DefaultTableModel model = (DefaultTableModel) connectionsInProgressTable.getModel();
                                Vector dataVector = model.getDataVector();
                                int sz = dataVector.size();
                                boolean allInPlace = sz > 0;
                                for (int row = 0; row < sz; row++) {
                                    String path = (String) model.getValueAt(row, model.findColumn(PATH_COLUMN));
                                    TerminalParams terminalParams = tp.get(path);
                                    if (terminalParams == null) {
                                        allInPlace = false;
                                        break;
                                    } else {
                                        model.setValueAt(
                                                //                                            "<html><b>" + ((System.currentTimeMillis() - terminalParams.start.getTime()) / 1000) + "</b></html>",
//                                                设置连接的时间
                                                new Integer((int) ((System.currentTimeMillis() - terminalParams.start.getTime()) / 1000)),
                                                row, model.findColumn(DURATION_COLUMN)
                                        );
                                    }
                                }
//                                设置好时间之后开始显示
                                if (!allInPlace) {
                                    dataVector.clear();
                                    // new Object[]{"Broker", "Account", "Path", "Start Time", "Duration (s)"}
                                    for (Map.Entry<String, TerminalParams> p : tp.entrySet()) {
                                        Vector row = new Vector();
                                        TerminalParams terminalParams = p.getValue();
                                        row.add(terminalParams.getSrv());
                                        row.add(terminalParams.getUser());
                                        row.add(new SimpleDateFormat("MMM d, HH:mm:ss").format(terminalParams.start));
                                        row.add(
                                                //"<html><b>" + ((System.currentTimeMillis() - terminalParams.start.getTime()) / 1000) + "</b></html>"
                                                new Integer((int) ((System.currentTimeMillis() - terminalParams.start.getTime()) / 1000))
                                        );
                                        row.add(terminalParams.getTerminalDirPathName());
                                        //
                                        dataVector.add(row);
                                    }
                                    model.fireTableDataChanged();
                                }
                                //这个才是真真的显示
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            TitledBorder border = (TitledBorder) connectionsInProgressPanel.getBorder();
                                            border.setTitle(tp.size() > 0
                                                    ? (tp.size() == 1 ? "1 Connection In Progress" : "" + tp.size() + " Connections In Progress")
                                                    : "Connections In Progress"
                                            );
                                            connectionsInProgressPanel.repaint();
                                        } catch (Exception e) {
                                            TS.LOGGER.error("At GUI", e);
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            TS.LOGGER.error("At GUI", e);
                        }
                    }
                });
                //不知道这句话是干什么用的，为什么要this，this指的是这个new runable类，不知道加不加有什么区别
                TS.scheduledExecutorService.schedule(this, 1, TimeUnit.SECONDS);
            }
        }, 1, TimeUnit.SECONDS);
        //接下来开始的是已经激活的连接
        activeTerminalsTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new Object[]{"Session", "PID", BROKER_COLUMN, ACCOUNT_COLUMN, PATH_COLUMN}
        ));
        //设置表格的样式
        TableColumnModel activeTerminalsTableColumnModel = activeTerminalsTable.getColumnModel();
        col = 0;
        // Session
        TableColumn session = activeTerminalsTableColumnModel.getColumn(col++);
        session.setMinWidth(60);
        session.setMaxWidth(120);
        session.setPreferredWidth(60);
        // PID
        activeTerminalsTableColumnModel.getColumn(col).setMinWidth(40);
        activeTerminalsTableColumnModel.getColumn(col).setMaxWidth(60);
        activeTerminalsTableColumnModel.getColumn(col++).setPreferredWidth(60);
        // Broker
        activeTerminalsTableColumnModel.getColumn(col).setMinWidth(100);
        activeTerminalsTableColumnModel.getColumn(col).setMaxWidth(240);
        activeTerminalsTableColumnModel.getColumn(col++).setPreferredWidth(80);
        // Account
        activeTerminalsTableColumnModel.getColumn(col).setMinWidth(100);
        activeTerminalsTableColumnModel.getColumn(col).setMaxWidth(120);
        activeTerminalsTableColumnModel.getColumn(col++).setPreferredWidth(80);
        // Path
        activeTerminalsTableColumnModel.getColumn(col).setPreferredWidth(500);

        if (!TS.P_USE_MSTSC)
            activeTerminalsTableColumnModel.removeColumn(session);//不使用远程连接的时候就不显示远程连接的session

        activeTerminalsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        //设置这个表格的鼠标事件
        activeTerminalsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                int r = activeTerminalsTable.rowAtPoint(e.getPoint());
                if (r >= 0 && r < activeTerminalsTable.getRowCount()) {
                    activeTerminalsTable.setRowSelectionInterval(r, r);
                } else {
                    activeTerminalsTable.clearSelection();
                }

                int activeTerminalsTableSelectedRow = activeTerminalsTable.getSelectedRow();
//                if (activeTerminalsTableSelectedRow < 0) {
//                    return;
//                }
                DefaultTableModel model = (DefaultTableModel) activeTerminalsTable.getModel();
                if (e.isPopupTrigger() && e.getComponent() instanceof JTable) {
                    createActiveTerminalsTablePopUp(
                            activeTerminalsTableSelectedRow < 0 ? null : (String) activeTerminalsTable.getValueAt(activeTerminalsTableSelectedRow, model.findColumn(PATH_COLUMN) - (TS.P_USE_MSTSC ? 0 : 1)),
                            activeTerminalsTableSelectedRow < 0 ? null : (String) activeTerminalsTable.getValueAt(activeTerminalsTableSelectedRow, model.findColumn(BROKER_COLUMN) - (TS.P_USE_MSTSC ? 0 : 1)),
                            activeTerminalsTableSelectedRow < 0 ? null : (String) activeTerminalsTable.getValueAt(activeTerminalsTableSelectedRow, model.findColumn(ACCOUNT_COLUMN) - (TS.P_USE_MSTSC ? 0 : 1)),
                            activeTerminalsTableSelectedRow < 0 ? null : (Integer) activeTerminalsTable.getValueAt(activeTerminalsTableSelectedRow, model.findColumn(PID_COLUMN) - (TS.P_USE_MSTSC ? 0 : 1)),
                            activeTerminalsTableSelectedRow < 0 || !TS.P_USE_MSTSC ? null : (String) activeTerminalsTable.getValueAt(activeTerminalsTableSelectedRow, model.findColumn(SESSION_COLUMN))
                    ).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        rowSorter = (TableRowSorter) activeTerminalsTable.getRowSorter();
//        rowSorter.setComparator(0, integerComparator); // session id changed to user name
        rowSorter.setComparator(1, integerComparator);
        rowSorter.toggleSortOrder(((DefaultTableModel) activeTerminalsTable.getModel()).findColumn(PATH_COLUMN));
        //

        //设置日志的表格
        logTable.setModel(new CircularTableModel(
                Integer.parseInt(limitRowsField.getText()),
                new Object[][]{},
                new Object[]{"Rec #", "Time", "Thread", "Severity", "Message"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }//设置不可编辑
        });
        TableColumnModel logTableColumnModel = logTable.getColumnModel();
        col = 0;
        // Rec #日志表格的样式
        logTableColumnModel.getColumn(col).setCellRenderer(rightRenderer);
        logTableColumnModel.getColumn(col).setMinWidth(30);
        logTableColumnModel.getColumn(col).setMaxWidth(70);
        logTableColumnModel.getColumn(col++).setPreferredWidth(70);
        // Time
        logTableColumnModel.getColumn(col).setCellRenderer(rightRenderer);
        logTableColumnModel.getColumn(col).setMinWidth(120);
        logTableColumnModel.getColumn(col).setMaxWidth(150);
        logTableColumnModel.getColumn(col++).setPreferredWidth(150);
        // Thread
        logTableColumnModel.getColumn(col).setCellRenderer(rightRenderer);
        logTableColumnModel.getColumn(col).setMinWidth(100);
        logTableColumnModel.getColumn(col).setMaxWidth(400);
        logTableColumnModel.getColumn(col++).setPreferredWidth(150);
        // Severity
        logTableColumnModel.getColumn(col).setMaxWidth(100);
        logTableColumnModel.getColumn(col).setPreferredWidth(50);
        // Message
        logTableColumnModel.getColumn(col).setPreferredWidth(500);
        logTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        rowSorter = (TableRowSorter) logTable.getRowSorter();
        rowSorter.setComparator(0, new Comparator<Long>() {
            @Override
            public int compare(Long o1, Long o2) {
                return o1.compareTo(o2);
            }
        });
        rowSorter.toggleSortOrder(0);
        rowSorter.toggleSortOrder(0);
        //
/*
        ButtonColumn buttonColumn = new ButtonColumn(logTable, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        }, 0);
        buttonColumn.setMnemonic(KeyEvent.VK_D);
*/
        limitRowsField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                final String text = limitRowsField.getText();
                //监听日志限制输入框的输入变化
                TS.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!text.equals(limitRowsField.getText())) {
                                        return;
                                    }
                                    if (text.replace("0", "").length() > 0) {
                                        try {
                                            if (Integer.parseInt(text) <= 0) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Error: Please enter number bigger than 0", "Error Message",
                                                        JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                applyNewRowsLimit();
                                            }
                                        } catch (NumberFormatException e) {
                                            JOptionPane.showMessageDialog(null,
                                                    "Error: Please enter valid number bigger than 0", "Error Message",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } catch (HeadlessException e) {
                                    TS.LOGGER.error("At GUI", e);
                                }
                            }
                        });
                    }
                }, KEY_PRESSED_ACTION_DELAY, TimeUnit.MILLISECONDS);
            }
        });
        //最小磁盘输入框的操作
        minDiskSpaceField.setText(TS.MIN_DISK_SPACE_GB);
        minDiskSpaceField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                final String text = minDiskSpaceField.getText();
                TS.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!text.equals(minDiskSpaceField.getText())) {
                                        return;
                                    }
                                    if (text.replace("0", "").length() > 0) {
                                        try {
                                            if (Integer.parseInt(text) <= 0) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Error: Please enter number bigger than 0", "Error Message",
                                                        JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                TS.MIN_DISK_SPACE_GB = text;//设置TS的最小磁盘空间
                                                TS.LOGGER.info("Minimum disk space set to " + text + "GB");
                                            }
                                        } catch (NumberFormatException e) {
                                            JOptionPane.showMessageDialog(null,
                                                    "Error: Please enter valid number bigger than 0", "Error Message",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } catch (HeadlessException e) {
                                    TS.LOGGER.error("At GUI", e);
                                }
                            }
                        });
                    }
                }, KEY_PRESSED_ACTION_DELAY, TimeUnit.MILLISECONDS);
            }
        });
        //连接超时输入框的处理
        connTimeoutField.setText(String.valueOf(TS.CONNECTION_TIMEOUT_MILLIS / 1000));
        connTimeoutField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                final String text = connTimeoutField.getText();
                TS.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!text.equals(connTimeoutField.getText())) {
                                        return;
                                    }
                                    if (text.replace("0", "").length() > 0) {
                                        try {
                                            int timeout = Integer.parseInt(text);
                                            if (timeout < 10 || timeout > 300) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Error: Please enter number 10 <= n <= 300", "Error Message",
                                                        JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                TS.CONNECTION_TIMEOUT_MILLIS = timeout * 1000;//设置TS的超时时间
                                                TS.NO_NETSTAT_DELAY_MILLIS = TS.CONNECTION_TIMEOUT_MILLIS / 2;
                                                TS.LOGGER.info("Connection Timeout set to " + timeout + " seconds");
                                            }
                                        } catch (NumberFormatException e) {
                                            JOptionPane.showMessageDialog(null,
                                                    "Error: Please enter valid number 10 <= n <= 300", "Error Message",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } catch (HeadlessException e) {
                                    TS.LOGGER.error("At GUI", e);
                                }
                            }
                        });
                    }
                }, KEY_PRESSED_ACTION_DELAY, TimeUnit.MILLISECONDS);
            }
        });
        //日志过滤输入框的操作，主要就是查找
        filterField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }
        });
        //日志过滤输入框清除的事件
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterField.setText("");
            }
        });
        //收集日志的事件
        collectReportButton.addActionListener(new ActionListener() {
            private File lastLogDir = null;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //Create a file chooser
                    String yyyyMmDdHhMm = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
                    String archiveFileName = String.format("nj4x_logs_%s.zip", yyyyMmDdHhMm);
                    File selectedFile = lastLogDir == null
                            ? new File(archiveFileName)
                            : new File(lastLogDir, archiveFileName);
                    final JFileChooser fc = new JFileChooser();
                    fc.setApproveButtonText("Save");
                    fc.setSelectedFile(selectedFile);
                    fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
                    fc.setFileFilter(new FileFilter() {
                        @Override
                        public boolean accept(File f) {
                            String name = f.getName();
                            return f.isDirectory() || name.startsWith("nj4x_logs_") && name.endsWith(".zip");
                        }

                        @Override
                        public String getDescription() {
                            return "NJ4X logs archives";
                        }
                    });
                    fc.setDialogTitle("Save NJ4X logs archive as ..");
                    //                                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    //
                    int returnVal = fc.showOpenDialog(null);
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        lastLogDir = file.getParentFile();
                        TS.LOGGER.info("Selected file: [" + file + "]");
                        generateLogsArchive(file);
                        if (Dropbox.getInstance().size() > file.length()) {
                            JEditorPane pane = new JEditorPane("text/html",
                                    "Ready: file = " + file + " size =~ " + (file.length() / 1024 / 1024) + "MB"
                                            + "<br><br><b>Would you like to upload this report to NJ4X support file store?</b>"
                                            + "<br><br>It contains no personally identifiable information."
                                            + "<br>Please refer file name in your support requests."
                            );
                            pane.setEditable(false);
                            pane.setBackground(Color.YELLOW);
                            if (JOptionPane.YES_NO_OPTION == JOptionPane.showConfirmDialog(null,
                                    pane,
                                    "Logs archive is ready",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE
                            )) {
                                try {
                                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                                    logTable.setCursor(c);
                                    //
//                                JOptionPane.showMessageDialog(null,
//                                        "Uploading report " + file.getName() + ", size=" + (file.length() / 1024)
//                                        + "KB, ",
//                                        "Uploading", JOptionPane.INFORMATION_MESSAGE);
                                    Dropbox.getInstance().upload(file);
                                } finally {
                                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                                    logTable.setCursor(c);
                                }
                                pane.setText("File <b>" + file + "</b> has been uploaded.");
                                JOptionPane.showMessageDialog(null, pane,
                                        "Logs archive is uploaded", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            JEditorPane pane = new JEditorPane("text/html",
                                    "<b>" + file + "</b>"
                            );
                            pane.setEditable(false);
                            pane.setBackground(Color.WHITE);
                            JOptionPane.showMessageDialog(null, pane,
                                    "Logs archive is ready", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                } catch (Exception e1) {
                    TS.LOGGER.error("At GUI", e1);
                    TerminalServer.displayUnexpectedError(e1);
                }
            }

            private void generateLogsArchive(File fArchive) {
                try {
                    TS.LOGGER.info("Creating archive: [" + fArchive + "] ...");
                    FileOutputStream fos = new FileOutputStream(fArchive);
                    final ZipOutputStream zos = new ZipOutputStream(fos);

                    //                                for (File termDir :  new File(TS.getTermDir()).listFiles(new java.io.FileFilter() {
                    //                                    @Override
                    //                                    public boolean accept(File pathname) {
                    //                                        return pathname.isDirectory();
                    //                                    }
                    //                                })){
                    //                                    Path startPath = termDir.toPath();
                    //                                    Files.walkFileTree(startPath, new LogsFileVisitor(startPath, zos));
                    //                                }
                    Path startPath = Paths.get(TS.getTermDir());
                    Files.walkFileTree(startPath, new LogsFileVisitor(startPath, zos));

                    /*Path */
                    startPath = Paths.get(TS.JFX_HOME);
                    Files.walkFileTree(startPath, new LogsFileVisitor(startPath, zos, true));

                    zos.close();
                    fos.close();

                    TS.LOGGER.info("NJ4X logs archive is ready");
                    TS.LOGGER.info(fArchive);
                } catch (Exception e) {
                    TS.LOGGER.error("Error generating NJ4X logs archive", e);
                }
            }
        });
        //允许debug级别的日志多选框监听事件
        enableDebugInfoCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Level level = enableDebugInfoCheckBox.isSelected() ? Level.DEBUG : Level.INFO;
                Logger.getRootLogger().setLevel(level);
                TS.LOGGER.setLevel(level);
            }
        });
        //选择隐藏后，不会显示mt4的程序界面
        hideTerminalsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TerminalParams.SW_HIDE = Boolean.toString(hideTerminalsCheckBox.isSelected());
            }
        });
        //部署专家系统的多选框事件，但是目前没有显示，也就不着急研究这个
        deployEaWsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (TerminalServer.IS_DEPLOY_EA_WS = deployEaWsCheckBox.isSelected()) {
                    if (!TerminalServer.deployEaWs(true)) {
                        deployEaWsCheckBox.setSelected(false);
                    }
                } else {
                    if (!TerminalServer.deployEaWs(false)) {
                        deployEaWsCheckBox.setSelected(true);
                    }
                }
            }
        });
        //打开Ts终端的按钮事件
        tsHomeDirOpenBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(tsHomeField.getText()));
                } catch (IOException e1) {
                    String message = "Open Terminal Home directory error: " + e1;
                    TS.LOGGER.error(message, e1);
                    JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //打开终端的路径，这两个是不一样的
        termsDirOpenBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().open(new File(TS.getTermDir()));
                } catch (IOException e1) {
                    String message = "Open Terminals directory error: " + e1;
                    TS.LOGGER.error(message, e1);
                    JOptionPane.showMessageDialog(null, message, "Error Message", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        //
        initCloudTab();
    }

    /**
     * 初始化云盘的tab，可以不看
     */
    private void initCloudTab() {
        if (!TS.NJ4X.endsWith("P5")) {
            zeroTermMt5AtGDrive.setVisible(false);
            zeroTermMt5AtGDrivePanel.setVisible(false);
        }

        updateInterval.setText(TS.getConfigurationValue("cloud_update_interval", "60"));
        updateInterval.setHorizontalAlignment(JTextField.RIGHT);
        updateInterval.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                warn();
            }

            public void removeUpdate(DocumentEvent e) {
                warn();
            }

            public void insertUpdate(DocumentEvent e) {
                warn();
            }

            public void warn() {
                final String text = updateInterval.getText();
                TS.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (!text.equals(updateInterval.getText())) {
                                        return;
                                    }
                                    if (text.replace("0", "").length() > 0) {
                                        try {
                                            if (Integer.parseInt(text) <= 0) {
                                                JOptionPane.showMessageDialog(null,
                                                        "Error: Please enter number bigger than 0", "Error Message",
                                                        JOptionPane.ERROR_MESSAGE);
                                            } else {
                                                TS.setConfigurationValue("cloud_update_interval", text);
                                                TS.LOGGER.info("Google Drive update interval set to " + text + " sec");
                                            }
                                        } catch (NumberFormatException e) {
                                            JOptionPane.showMessageDialog(null,
                                                    "Error: Please enter valid number >0", "Error Message",
                                                    JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                } catch (HeadlessException e) {
                                    TS.LOGGER.error("At GUI", e);
                                }
                            }
                        });
                    }
                }, KEY_PRESSED_ACTION_DELAY, TimeUnit.MILLISECONDS);
            }
        });
        //
        if (getInstance().isValid()) {
            initGDrive();
        } else {
            disableGDriveGUI();
        }
        useGDrive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (useGDrive.isSelected()) {
                    initGDrive();
                } else {
                    disableGDrive();
                }
            }
        });
        srvAtGDrive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GDriveAccess.GDriveFile f = (GDriveAccess.GDriveFile) srvAtGDrive.getSelectedItem();
                if (f != null && f.getDriveFile() != null) {
                    newConfig(P_SRV, f.getDriveFile().getId());
                } else {
                    removeConfig(P_SRV);
                }
            }
        });
        zeroTermAtGDrive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GDriveAccess.GDriveFile f = (GDriveAccess.GDriveFile) zeroTermAtGDrive.getSelectedItem();
                if (f != null && f.getDriveFile() != null) {
                    newConfig(P_ZERO_TERM, f.getDriveFile().getId());
                } else {
                    removeConfig(P_ZERO_TERM);
                }
            }
        });
        zeroTermMt5AtGDrive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GDriveAccess.GDriveFile f = (GDriveAccess.GDriveFile) zeroTermMt5AtGDrive.getSelectedItem();
                if (f != null && f.getDriveFile() != null) {
                    newConfig(P_ZERO_TERM_MT5, f.getDriveFile().getId());
                } else {
                    removeConfig(P_ZERO_TERM_MT5);
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                int seconds = getInstance().nextSyncInSeconds();
                if (seconds <= 0) {
                    nextSyncClock.setText("(Downloading files now, see Journal tab)");
                } else {
                    nextSyncClock.setText("(Next download is in " + seconds + " seconds)");
                }
/*
                if (seconds >= 0) {
                } else {
                    nextSyncClock.setText("");
                }
*/
                final Runnable x = this;
                TS.scheduledExecutorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(x);
                    }
                }, 1, TimeUnit.SECONDS);
            }
        });
    }

    /**
     * 初始化云盘
     */
    private void initGDrive() {
        TS.scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                getInstance().initDrive();
                final boolean enable = getInstance().isValid();
                //
                SwingUtilities.invokeLater(new Runnable() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public void run() {
                        srvAtGDrive.removeAllItems();
                        zeroTermAtGDrive.removeAllItems();
                        zeroTermMt5AtGDrive.removeAllItems();
                        if (enable) {
                            useGDrive.setSelected(true);
                            updateInterval.setEnabled(true);
                            srvAtGDrive.setEnabled(true);
                            zeroTermAtGDrive.setEnabled(true);
                            zeroTermMt5AtGDrive.setEnabled(true);
                            User owner = getInstance().getOwner();
                            if (owner != null) {
                                try {
                                    gDriveOwnerIcon.setIcon(new ImageIcon(ImageIO.read(new URL(
                                            owner.getPhotoLink()))));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                gDriveOwner.setText(owner.getDisplayName() + " (" + owner.getEmailAddress() + ")");
                            } else {
                                gDriveOwnerIcon.setIcon(null);
                                gDriveOwner.setText("-");
                            }
                            //
                            List<GDriveAccess.GDriveFile> ls = getInstance().listFolders();
                            if (ls != null) {
                                GDriveAccess.GDriveFile srv = null;
                                GDriveAccess.GDriveFile zt = null;
                                GDriveAccess.GDriveFile zt5 = null;
                                for (GDriveAccess.GDriveFile p : ls) {
                                    com.google.api.services.drive.model.File driveFile = p.getDriveFile();
                                    if (driveFile != null) {
                                        if (driveFile.getId().equals(TS.getConfigurationValue(P_SRV, ""))) {
                                            srv = p;
                                        }
                                        if (driveFile.getId().equals(TS.getConfigurationValue(P_ZERO_TERM, ""))) {
                                            zt = p;
                                        }
                                        if (driveFile.getId().equals(TS.getConfigurationValue(P_ZERO_TERM_MT5, ""))) {
                                            zt5 = p;
                                        }
                                    }
                                }
                                for (GDriveAccess.GDriveFile p : ls) {
                                    srvAtGDrive.addItem(p);
                                    zeroTermAtGDrive.addItem(p);
                                    zeroTermMt5AtGDrive.addItem(p);
                                }
                                if (srv != null) {
                                    srvAtGDrive.setSelectedItem(srv);
                                }
                                if (zt != null) {
                                    zeroTermAtGDrive.setSelectedItem(zt);
                                }
                                if (zt5 != null) {
                                    zeroTermMt5AtGDrive.setSelectedItem(zt5);
                                }
                            }
                        } else {
                            disableGDrive();
                        }
                    }
                });
            }
        }, 1, TimeUnit.MILLISECONDS);
    }

    private void SetSelection(JComboBox jComboBox, GDriveAccess.GDriveFile p, String configProperty) {
        //noinspection unchecked
        jComboBox.addItem(p);
        com.google.api.services.drive.model.File driveFile = p.getDriveFile();
        if (driveFile == null || driveFile.getId().equals(TS.getConfigurationValue(configProperty, ""))) {
            jComboBox.setSelectedItem(p);
        }
    }

    private void disableGDrive() {
        getInstance().disconnectAndClearCredentials();
        disableGDriveGUI();
        TS.removeConfigurationValues(P_SRV, P_ZERO_TERM);
    }

    private void disableGDriveGUI() {
        useGDrive.setSelected(false);
        updateInterval.setEnabled(false);
        srvAtGDrive.setEnabled(false);
        zeroTermAtGDrive.setEnabled(false);
        zeroTermMt5AtGDrive.setEnabled(false);
        gDriveOwnerIcon.setIcon(null);
        gDriveOwner.setText("");
    }

    /**
     * 设置右击菜单的
     * @param path
     * @param broker
     * @param account
     * @param PID
     * @param sessionUser
     * @return
     */
    private JPopupMenu createActiveTerminalsTablePopUp(final String path, final String broker, final String account, final Integer PID, final String sessionUser) {
        // Create some menu items for the popup
        final JMenuItem menuRefresh = new JMenuItem("Refresh");
        final JMenuItem menuRdpThere = new JMenuItem("RDP there");
        final JMenuItem menuShowTerminalUsageStatistics = new JMenuItem("Get statistics for " + account + "(" + broker + ") terminal, PID=" + PID);
        final JMenuItem menuToggleTerminalVisibility = new JMenuItem("Show/hide " + account + "(" + broker + ") terminal, PID=" + PID);
        final JMenuItem menuHideAllTerminals = new JMenuItem("Hide All Terminals");
        final JMenuItem menuShowAllTerminals = new JMenuItem("Show All Terminals");
        final JMenuItem menuStopTerminal = new JMenuItem("Stop " + account + "(" + broker + ") terminal, PID=" + PID);
        final JMenuItem menuKillTerminal = new JMenuItem("Kill " + account + "(" + broker + ") terminal, PID=" + PID);
        final JMenuItem menuKillAllTerminals = new JMenuItem("Kill All Terminals");

        // Create a popup menu
        JPopupMenu popupMenu = new JPopupMenu("Manage Terminals");
        popupMenu.add(menuRefresh);
        if (TS.P_USE_MSTSC && sessionUser != null) {
            popupMenu.add(menuRdpThere);
            // Action and mouse listener support
            menuRdpThere.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        for (Session s : ts.getSessionManager().getSessions()) {
                            for (Map.Entry<String, Integer> p : s.getTermProcesses().entrySet()) {
                                TsSystemUser user = s.getUser();
                                if (user != null) {
                                    String name = user.getName();
                                    if (sessionUser.equals(name) || ("NJ4X_" + sessionUser).equals(name)) {
                                        SessionManager.StartRDPClient(user);
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (Exception e1) {
                        TS.LOGGER.error("RDP error: " + e1, e1);
                    }
                }
            });
        }
        if (PID > 0) {
            popupMenu.addSeparator();
//            popupMenu.add(menuShowTerminalUsageStatistics);
//            popupMenu.addSeparator();
            popupMenu.add(menuToggleTerminalVisibility);
            popupMenu.add(menuHideAllTerminals);
            popupMenu.add(menuShowAllTerminals);
            popupMenu.addSeparator();
            popupMenu.add(menuStopTerminal);
            popupMenu.add(menuKillTerminal);
            popupMenu.addSeparator();
            popupMenu.add(menuKillAllTerminals);
        }
        // Action and mouse listener support
        menuRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    ts.updateTerminals();
                } catch (Exception e1) {
                    TS.LOGGER.error("Refresh terminals error: " + e1, e1);
                }
            }
        });
        // Action and mouse listener support
        menuShowTerminalUsageStatistics.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String action = "Getting statistics for";
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info(action + " terminal: " + path);
                    boolean b = ts.getStatisticsByPID(PID, path);
                    TS.LOGGER.info(action + " terminal, res=" + b);
                } catch (Exception e1) {
                    TS.LOGGER.error(action + " terminal error: " + e1, e1);
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
            }
        });
        // Action and mouse listener support
        menuToggleTerminalVisibility.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String action = "Toggle visibility for";
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info(action + " terminal: " + path);
                    boolean b = ts.toggleWindowVisibilityByPID(PID);
                    TS.LOGGER.info(action + " terminal, res=" + b);
                } catch (Exception e1) {
                    TS.LOGGER.error(action + " terminal error: " + e1, e1);
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
            }
        });
        // Action and mouse listener support
        menuStopTerminal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info("Stopping terminal: " + path);
                    ts.killTerminal(path);
                    TS.LOGGER.info("Terminal stopped: " + path);
                } catch (Exception e1) {
                    TS.LOGGER.error("Stop terminal error: " + e1, e1);
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
                ts.updateTerminals();
            }
        });
        menuHideAllTerminals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info("Hiding ALL terminals");
                    DefaultTableModel model = (DefaultTableModel) activeTerminalsTable.getModel();
                    for (int row = 0, max = model.getRowCount(); row < max; ++row) {
                        Integer pid = (Integer) model.getValueAt(row, model.findColumn(PID_COLUMN));
                        try {
                            ts.swShowHideByPID(false, pid);
                            TS.LOGGER.info("Terminal hidden: " + path);
                        } catch (Exception e1) {
                            TS.LOGGER.error("Hide terminal error: " + e1, e1);
                        }
                    }
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
            }
        });
        menuShowAllTerminals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info("Showing ALL terminals");
                    DefaultTableModel model = (DefaultTableModel) activeTerminalsTable.getModel();
                    for (int row = 0, max = model.getRowCount(); row < max; ++row) {
                        Integer pid = (Integer) model.getValueAt(row, model.findColumn(PID_COLUMN));
                        try {
                            ts.swShowHideByPID(true, pid);
                            TS.LOGGER.info("Terminal shown: " + path);
                        } catch (Exception e1) {
                            TS.LOGGER.error("Show terminal error: " + e1, e1);
                        }
                    }
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
            }
        });
        menuKillTerminal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info("Killing terminal: " + path);
                    ts.killProcessUngracefully(path);
                    TS.LOGGER.info("Terminal killed: " + path);
                } catch (Exception e1) {
                    TS.LOGGER.error("Stop terminal error: " + e1, e1);
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
                ts.updateTerminals();
            }
        });
        menuKillAllTerminals.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Cursor c = new Cursor(Cursor.WAIT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                    //
                    TS.LOGGER.info("Killing ALL terminals");
                    DefaultTableModel model = (DefaultTableModel) activeTerminalsTable.getModel();
                    for (int row = 0, max = model.getRowCount(); row < max; ++row) {
                        String path = (String) model.getValueAt(row, model.findColumn(PATH_COLUMN));
                        try {
                            ts.killProcessUngracefully(path);
                            TS.LOGGER.info("Terminal killed: " + path);
                        } catch (Exception e1) {
                            TS.LOGGER.error("Kill terminal error: " + e1, e1);
                        }
                    }
                    try {
                        Thread.sleep(1000 * (1 + model.getRowCount() / 100));
                    } catch (InterruptedException ignore) {
                    }
                    ts.updateTerminals();
                } finally {
                    Cursor c = new Cursor(Cursor.DEFAULT_CURSOR);
                    activeTerminalsTable.setCursor(c);
                }
            }
        });
        //
        return popupMenu;
    }

    private void applyNewRowsLimit() {
        int capacity = Integer.parseInt(limitRowsField.getText());
        synchronized (logTable) {
            logEvents.setCapacity(Math.max(capacity, MIN_LOG_EVENTS_TO_KEEP));
            CircularTableModel model = (CircularTableModel) logTable.getModel();
            model.setCapacity(capacity);
        }
        applyFilters();
    }

    private void applyFilters() {
        try {
            CircularTableModel model = (CircularTableModel) logTable.getModel();
            CircularArrayList<ArrayList<Object>> dataVector = model.getDataVector();
            dataVector.clear();
            synchronized (logTable) {
                for (LoggingEvent event : logEvents) {
                    if (isFilterOkForEvent(event)) {
                        dataVector.add(toDataRow(event));
                    }
                }
            }
            model.fireTableDataChanged();
        } catch (Exception e) {
            TS.LOGGER.error("Apply filters error", e);
        }
    }

    private ArrayList<Object> toDataRow(LoggingEvent event) {
        ArrayList<Object> row = new ArrayList<>();
        row.add(new Long(event.getProperty("id")));
        row.add(new SimpleDateFormat("MMM d, HH:mm:ss.SSS").format(new Date(event.getTimeStamp())));
        row.add(event.getThreadName());
        row.add(event.getLevel());
        row.add(event.getRenderedMessage());
        return row;
    }
//    设置Ts
    public void setTs(TS ts) {
        this.ts = ts;
        boolean isNotPersonal = BoxUtils.BOXID == 0;
        maxSessionsLabel.setVisible(isNotPersonal);
        maxSessionsField.setVisible(isNotPersonal);
        String version = (isNotPersonal ? "" : " Personal") + " Terminal Server"
                + (TS.P_GUI_ONLY ? " Viewer" : "")
                + ", v" + TS.NJ4X
                + (isNotPersonal ? "" : ", BOXID=" + BoxUtils.BOXID)
                + ", port=" + ts.getPortAsString();  // Terminal Server, v2.6.2, port=7788
        versionTextField.setText(version);
        tsHomeField.setText(TS.JFX_HOME);
        termsDirField.setText(TS.TERM_DIR == null ? TS.getTermDir() : TS.getTermDir() + " -> " + TS.getTargetTermDir());
        minDiskSpaceField.setText(TS.MIN_DISK_SPACE_GB);
    }

    private boolean isFilterOkForEvent(LoggingEvent event) {
        String content = filterField.getText();
        return content.length() == 0
                || event.getThreadName().contains(content)
                || event.getRenderedMessage().contains(content)
                ;
    }
//    更新终端MT4，这个类没有用到它，其他地方用到了
    public void updateTerminals(final SessionManager sessionManager) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (activeTerminalsTable) {
                        int totalTerminals = 0;
                        DefaultTableModel model = (DefaultTableModel) activeTerminalsTable.getModel();
                        Vector dataVector = model.getDataVector();
                        dataVector.clear();
                        // new Object[]{"Session", "PID", "Broker", "Account", "Path"}
                        for (Session s : sessionManager.getSessions()) {
                            totalTerminals += s.getTermsCount();
                            for (Map.Entry<String, Integer> p : s.getTermProcesses().entrySet()) {
                                try {
                                    Vector row = new Vector();
                                    TsSystemUser user = s.getUser();
                                    String name = user == null ? System.getProperty("user.name") : user.getName();
                                    row.add(name.startsWith("NJ4X_HOST_") ? name.substring("NJ4X_".length()) : name);
                                    row.add(p.getValue());
                                    String pPath = p.getKey();
                                    String dir = Paths.get(ts.getTermDir()).toString();
                                    int t = pPath.indexOf(dir) + dir.length() + 1;
                                    int sp1 = pPath.indexOf(' ', t);
                                    if (sp1 > 0) {
                                        int sp2 = pPath.lastIndexOf(' ');
                                        String srvPrefix = pPath.substring(t, sp2);
                                        boolean srvFound = false;
                                        while (!(srvFound = Paths.get(pPath).getParent().resolve("config").resolve(srvPrefix + ".srv").toFile().exists())) {
                                            sp2 = pPath.lastIndexOf(' ', sp2 - 1);
                                            if (sp2 > t) {
                                                srvPrefix = pPath.substring(t, sp2);
                                            } else {
                                                sp2 = sp1;
                                                break;
                                            }
                                        }
                                        //                                    sp2 = sp2 > 0 ? sp2 : pPath.indexOf(' ', sp1 + 1);
                                        sp2 = sp2 >= 0 ? sp2 : sp1;
                                        row.add(srvPrefix);
                                        String acct = pPath.substring(sp2 + 1, pPath.lastIndexOf('\\')).trim();
                                        sp1 = acct.indexOf(' ');
                                        acct = sp1 > 0 ? acct.substring(0, sp1) : acct;
                                        row.add(acct);
                                    } else {
                                        sp1 = pPath.indexOf('\\', t);
                                        row.add(pPath.substring(t, sp1));
                                        row.add("");
                                    }
                                    row.add(pPath);
                                    //
                                    dataVector.add(row);
                                } catch (Exception e) {
                                    TS.LOGGER.error("Error parsing [" + p.getKey() + "]", e);
                                }
                            }
                        }
                        if (totalTerminals == 0) {
                            Vector row = new Vector();
                            row.add(new Integer(-1));
                            row.add(new Integer(-1));
                            row.add("-");
                            row.add("-");
                            row.add("No active terminals, <Right-Click> to refresh");
                            //
                            dataVector.add(row);
                        }
                        model.fireTableDataChanged();
                        //
                        TitledBorder border = (TitledBorder) activeTerminalsPanel.getBorder();
                        border.setTitle(totalTerminals > 0
                                ? (totalTerminals == 1 ? "1 Active Terminal" : "" + totalTerminals + " Active Terminals")
                                : "Active Terminals"
                        );
                        activeTerminalsPanel.repaint();
                    }
                } catch (Exception e) {
                    TS.LOGGER.error("At GUI", e);
                }
            }
        });
    }

    public void startConnection(TerminalParams tp) {
        synchronized (connectionsInProgress) {
            connectionsInProgress.add(tp);
        }
    }

    public void endConnection(TerminalParams tp) {
        synchronized (connectionsInProgress) {
            connectionsInProgress.remove(tp);
        }
        ts.updateTerminals();
    }
}
