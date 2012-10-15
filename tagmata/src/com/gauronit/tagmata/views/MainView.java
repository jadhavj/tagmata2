/*******************************************************************************
 * Copyright (c) 2012 Gauronit Technologies.
 * All rights reserved. Tagmata and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Jayesh Jadhav - initial API and implementation
 ******************************************************************************/
package com.gauronit.tagmata.views;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.Timer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskMonitor;

import com.gauronit.tagmata.Main;
import com.gauronit.tagmata.core.CardSnapshot;
import com.gauronit.tagmata.core.Indexer;
import com.gauronit.tagmata.util.StringUtil;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;

public class MainView extends JFrame {

    ResourceMap resourceMap;
    public static MainView mainView;
    final PopupMenu popup;
    final TrayIcon trayIcon;
    final SystemTray tray;

    public MainView() {
    	setTitle("Gauronit Tagmata");
        mainView = this;
        initComponents();
        this.setSize(900, 567);

        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Determine the new location of the window
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        
        // Move the window
        setLocation(x, y);        
        
        Main.getApplication().getMainFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // status bar initialization - message timeout, idle icon and busy animation, etc
        
        resourceMap = Main.getApplication().getContext().getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(Main.getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        // -- FIXME
        allChkBox.setSelected(true);
        tagsChkBox.setSelected(true);
        titleChkBox.setSelected(true);
        textChkBox.setSelected(true);
        // --

        loadIndexes();

        results.setModel(new DefaultListModel());
        quickCards.setModel(new DefaultListModel());
        GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
        gl_mainPanel.setHorizontalGroup(
        	gl_mainPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_mainPanel.createSequentialGroup()
        			.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
        				.addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
        				.addGroup(gl_mainPanel.createSequentialGroup()
        					.addGap(10)
        					.addComponent(jLabel4)))
        			.addGap(874))
        		.addGroup(gl_mainPanel.createSequentialGroup()
        			.addGap(10)
        			.addComponent(outerSplitPane, GroupLayout.DEFAULT_SIZE, 864, Short.MAX_VALUE)
        			.addContainerGap())
        );
        gl_mainPanel.setVerticalGroup(
        	gl_mainPanel.createParallelGroup(Alignment.LEADING)
        		.addGroup(gl_mainPanel.createSequentialGroup()
        			.addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, 0, GroupLayout.PREFERRED_SIZE)
        			.addGap(11)
        			.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
        				.addComponent(outerSplitPane, GroupLayout.DEFAULT_SIZE, 648, Short.MAX_VALUE)
        				.addComponent(jLabel4))
        			.addContainerGap())
        );
        mainPanel.setLayout(gl_mainPanel);
        this.setIconImage(resourceMap.getImageIcon("tagmataIcon").getImage());

        popup = new PopupMenu();
        tray = SystemTray.getSystemTray();


        MenuItem searchTagmata = new MenuItem("Search Tagmata");
        searchTagmata.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                mainView.setVisible(true);
                mainView.toFront();
                mainView.setFocusableWindowState(true);
                mainView.setState(JFrame.NORMAL);
            }
        });
        popup.add(searchTagmata);
        MenuItem exitTagmata = new MenuItem("Exit");
        exitTagmata.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        popup.add(exitTagmata);
        trayIcon = new TrayIcon(resourceMap.getImageIcon("tagmataIcon").getImage(), "Tray Demo", popup);
        trayIcon.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    mainView.setVisible(true);
                    mainView.toFront();
                    mainView.setFocusableWindowState(true);
                    mainView.setState(JFrame.NORMAL);
                }
                if (e.getButton() == MouseEvent.BUTTON2) {
                    popup.show(Main.getApplication().getMainFrame(), e.getX(), e.getY());
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println(e);
        }
        Main.getApplication().getMainFrame().setVisible(false);
        refreshBookmarks();

    }

    private void loadIndexes() {
        try {
            if (Indexer.currentInstance().getIndexNames().isEmpty()) {
                Indexer.currentInstance().createIndex("General");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        indexesTable = new IndexesTable();
        DefaultTableModel tm = new IndexesTableModel(
                new Object[][]{},
                new Object[]{"Active", "Index Name", "displayName"});

        Map<String, Indexes.Index> indexes = Indexes.getIndexes();

        int i = 0;
        for (Map.Entry<String, Indexes.Index> indexEntry : indexes.entrySet()) {
            Indexes.Index index = indexEntry.getValue();
            tm.insertRow(i, new Object[]{true, index.getDisplayName(), index.getIndexName()});
            i++;
        }

        indexesTable.setModel(tm);
        indexesTable.getColumnModel().removeColumn(indexesTable.getColumnModel().getColumn(2));
        indexesTable.getColumnModel().getColumn(0).setResizable(false);
        indexesTable.getColumnModel().getColumn(0).setMaxWidth(50);
        indexesTable.setAutoCreateRowSorter(true);
        indexesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        indexesTable.repaint();
        indexesScrollPane.setViewportView(indexesTable);
    }

    private static class Indexes {

        private static class Index {

            public Index(String indexName, String displayName, boolean searchable) {
                this.displayName = displayName;
                this.indexName = indexName;
                this.searchable = searchable;
            }

            public String getDisplayName() {
                return displayName;
            }

            public void setDisplayName(String displayName) {
                this.displayName = displayName;
            }

            public String getIndexName() {
                return indexName;
            }

            public void setIndexName(String indexName) {
                this.indexName = indexName;
            }

            public boolean isSearchable() {
                return searchable;
            }

            public void setSearchable(boolean searchable) {
                this.searchable = searchable;
            }
            private String indexName;
            private boolean searchable;
            private String displayName;
        }
        private static Map<String, Index> indexes = new LinkedHashMap();

        static {
            loadIndexes();
        }

        public static void loadIndexes() {
            try {
                ArrayList<String[]> indexNames = Indexer.currentInstance().getIndexNames();
                for (String[] indexName : indexNames) {
                    Index index = new Index(indexName[0], indexName[1], true);
                    indexes.put(indexName[0], index);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static boolean isSearchable(String indexName) {
            return indexes.get(indexName).isSearchable();
        }

        public static void enableSearch(String indexName) {
            indexes.get(indexName).setSearchable(true);
        }

        public static void disableSearch(String indexName) {
            indexes.get(indexName).setSearchable(false);
        }

        public static void removeIndex(String indexName) {
            try {
                indexes.remove(indexName);
                Indexer.currentInstance().deleteIndex(indexName);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static void addIndex(String displayName) {
            UUID indexName = UUID.randomUUID();
            indexes.put(indexName.toString(), new Index(indexName.toString(), displayName, true));
        }

        public static Map<String, Index> getIndexes() {
            return indexes;
        }
    }

    private class IndexesTable extends JTable {
    }

    private class IndexesTableModel extends DefaultTableModel {

        private IndexesTableModel(Object[][] data, Object[] columns) {
            super(data, columns);
            this.addTableModelListener(new TableModelListener() {

                public void tableChanged(TableModelEvent e) {
                    int row = e.getFirstRow();
                    if (row < getRowCount()) {
                        if (getValueAt(row, 1) != null && getValueAt(row, 2) != null && e.getColumn() != 0) {
                            try {
                                String indexDisplayName = getValueAt(row, 1).toString();
                                String indexName = getValueAt(row, 2).toString();
                                Indexer.currentInstance().renameIndex(indexName, indexDisplayName);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            super.setValueAt(value, row, col);
            if (col == 0) {
                if (((Boolean) value).booleanValue() == false) {
                    searchAllIndices.setSelected(false);
                    return;
                } else if (((Boolean) value).booleanValue() == true) {
                    for (int i = 0; i < getRowCount(); i++) {
                        if (((Boolean) getValueAt(i, 0)).booleanValue() == false) {
                            searchAllIndices.setSelected(false);
                            return;
                        }
                        searchAllIndices.setSelected(true);
                    }
                }
            }
        }

        @Override
        public Class getColumnClass(int col) {
            if (col == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        public boolean isCellEditable(int row, int col) {
        	if (getValueAt(row, col).equals("General")) {
        		return false;
        	}
        	return true;
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = Main.getApplication().getMainFrame();
            aboutBox = new About(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        Main.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        outerSplitPane = new javax.swing.JSplitPane();
        innerSplitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        quickCardsPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        quickCards = new javax.swing.JList();
        jLabel2 = new javax.swing.JLabel();
        deleteQuickCard = new javax.swing.JLabel();
        resultsPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        results = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        searchText = new javax.swing.JTextField();
        allChkBox = new javax.swing.JCheckBox();
        tagsChkBox = new javax.swing.JCheckBox();
        titleChkBox = new javax.swing.JCheckBox();
        textChkBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        newCardLbl = new javax.swing.JLabel();
        deleteCardLbl = new javax.swing.JLabel();
        superFuzzy = new javax.swing.JCheckBox();
        addBookmarkLbl = new javax.swing.JLabel();
        listAllCardsLbl = new javax.swing.JLabel();
        clearResultsLbl = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        indexesScrollPane = new javax.swing.JScrollPane();
        indexesTable = new javax.swing.JTable();
        searchAllIndices = new javax.swing.JCheckBox();
        addIndexLbl = new javax.swing.JLabel();
        deleteIndex = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        indicesPopup = new javax.swing.JPopupMenu();

        mainPanel.setMinimumSize(new Dimension(0, 0));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(1024, 768));
        mainPanel.addHierarchyBoundsListener(new java.awt.event.HierarchyBoundsListener() {
            public void ancestorMoved(java.awt.event.HierarchyEvent evt) {
            }
            public void ancestorResized(java.awt.event.HierarchyEvent evt) {
                mainPanelAncestorResized(evt);
            }
        });
        mainPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                mainPanelPropertyChange(evt);
            }
        });

        jLabel4.setName("bookmarksLbl"); // NOI18N

        outerSplitPane.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        outerSplitPane.setDividerLocation(270);
        outerSplitPane.setMinimumSize(new java.awt.Dimension(0, 0));
        outerSplitPane.setName("outerSplitPane"); // NOI18N

        innerSplitPane.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        innerSplitPane.setDividerLocation(300);
        innerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        innerSplitPane.setName("innerSplitPane"); // NOI18N
        innerSplitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                innerSplitPaneComponentResized(evt);
            }
        });
        innerSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                innerSplitPanePropertyChange(evt);
            }
        });

        jPanel1.setName("jPanel1"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(com.gauronit.tagmata.Main.class).getContext().getResourceMap(MainView.class);
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        innerSplitPane.setBottomComponent(jPanel1);

        quickCardsPanel.setName("quickCardsPanel"); // NOI18N

        jScrollPane4.setName("jScrollPane4"); // NOI18N

        quickCards.setModel(new javax.swing.AbstractListModel() {
            String[] strings = {};
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        quickCards.setName("quickCards"); // NOI18N
        quickCards.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                quickCardsMouseClicked(evt);
            }
        });
        jScrollPane4.setViewportView(quickCards);

        jLabel2.setIcon(resourceMap.getIcon("jLabel2.icon")); // NOI18N
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        deleteQuickCard.setIcon(resourceMap.getIcon("deleteQuickCard.icon")); // NOI18N
        deleteQuickCard.setText(resourceMap.getString("deleteQuickCard.text")); // NOI18N
        deleteQuickCard.setName("deleteQuickCard"); // NOI18N
        deleteQuickCard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteQuickCardMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout quickCardsPanelLayout = new javax.swing.GroupLayout(quickCardsPanel);
        quickCardsPanelLayout.setHorizontalGroup(
        	quickCardsPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(quickCardsPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(quickCardsPanelLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
        				.addGroup(quickCardsPanelLayout.createSequentialGroup()
        					.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 99, GroupLayout.PREFERRED_SIZE)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(deleteQuickCard)))
        			.addContainerGap())
        );
        quickCardsPanelLayout.setVerticalGroup(
        	quickCardsPanelLayout.createParallelGroup(Alignment.LEADING)
        		.addGroup(quickCardsPanelLayout.createSequentialGroup()
        			.addContainerGap()
        			.addGroup(quickCardsPanelLayout.createParallelGroup(Alignment.LEADING)
        				.addComponent(jLabel2, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
        				.addComponent(deleteQuickCard))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(jScrollPane4, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
        			.addContainerGap())
        );
        quickCardsPanel.setLayout(quickCardsPanelLayout);

        innerSplitPane.setBottomComponent(quickCardsPanel);

        resultsPanel.setName("resultsPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        results.setModel(new javax.swing.AbstractListModel() {
            String[] strings = {};
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        results.setName("results"); // NOI18N
        results.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                resultsMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(results);

        jLabel3.setName("jLabel3"); // NOI18N

        searchText.setName("searchText"); // NOI18N
        searchText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchTextKeyTyped(evt);
            }
        });

        allChkBox.setText(resourceMap.getString("allChkBox.text")); // NOI18N
        allChkBox.setName("allChkBox"); // NOI18N
        allChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allChkBoxActionPerformed(evt);
            }
        });

        tagsChkBox.setText(resourceMap.getString("tagsChkBox.text")); // NOI18N
        tagsChkBox.setName("tagsChkBox"); // NOI18N
        tagsChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagsChkBoxActionPerformed(evt);
            }
        });

        titleChkBox.setText(resourceMap.getString("titleChkBox.text")); // NOI18N
        titleChkBox.setName("titleChkBox"); // NOI18N
        titleChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleChkBoxActionPerformed(evt);
            }
        });

        textChkBox.setText(resourceMap.getString("textChkBox.text")); // NOI18N
        textChkBox.setName("textChkBox"); // NOI18N
        textChkBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textChkBoxActionPerformed(evt);
            }
        });

        jLabel5.setIcon(resourceMap.getIcon("jLabel5.icon")); // NOI18N
        jLabel5.setText(resourceMap.getString("jLabel5.text")); // NOI18N
        jLabel5.setName("jLabel5"); // NOI18N

        newCardLbl.setIcon(resourceMap.getIcon("newCardLbl.icon")); // NOI18N
        newCardLbl.setText(resourceMap.getString("newCardLbl.text")); // NOI18N
        newCardLbl.setName("newCardLbl"); // NOI18N
        newCardLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                newCardLblMouseClicked(evt);
            }
        });

        deleteCardLbl.setIcon(resourceMap.getIcon("deleteCardLbl.icon")); // NOI18N
        deleteCardLbl.setText(resourceMap.getString("deleteCardLbl.text")); // NOI18N
        deleteCardLbl.setName("deleteCardLbl"); // NOI18N
        deleteCardLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteCardLblMouseClicked(evt);
            }
        });

        superFuzzy.setText(resourceMap.getString("superFuzzy.text")); // NOI18N
        superFuzzy.setName("superFuzzy"); // NOI18N

        addBookmarkLbl.setIcon(resourceMap.getIcon("addBookmarkLbl.icon")); // NOI18N
        addBookmarkLbl.setText(resourceMap.getString("addBookmarkLbl.text")); // NOI18N
        addBookmarkLbl.setName("addBookmarkLbl"); // NOI18N
        addBookmarkLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addBookmarkLblMouseClicked(evt);
            }
        });

        listAllCardsLbl.setIcon(resourceMap.getIcon("listAllCardsLbl.icon")); // NOI18N
        listAllCardsLbl.setText(resourceMap.getString("listAllCardsLbl.text")); // NOI18N
        listAllCardsLbl.setName("listAllCardsLbl"); // NOI18N
        listAllCardsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listAllCardsLblMouseClicked(evt);
            }
        });

        clearResultsLbl.setIcon(resourceMap.getIcon("clearResultsLbl.icon")); // NOI18N
        clearResultsLbl.setText(resourceMap.getString("clearResultsLbl.text")); // NOI18N
        clearResultsLbl.setName("clearResultsLbl"); // NOI18N
        clearResultsLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                clearResultsLblMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout resultsPanelLayout = new javax.swing.GroupLayout(resultsPanel);
        resultsPanel.setLayout(resultsPanelLayout);
        resultsPanelLayout.setHorizontalGroup(
            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 740, Short.MAX_VALUE)
                    .addComponent(jLabel3)
                    .addGroup(resultsPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(searchText, javax.swing.GroupLayout.DEFAULT_SIZE, 712, Short.MAX_VALUE))
                    .addGroup(resultsPanelLayout.createSequentialGroup()
                        .addComponent(allChkBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tagsChkBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(titleChkBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textChkBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newCardLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(deleteCardLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addBookmarkLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listAllCardsLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearResultsLbl)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 299, Short.MAX_VALUE)
                        .addComponent(superFuzzy)))
                .addContainerGap())
        );

        resultsPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {tagsChkBox, textChkBox, titleChkBox});

        resultsPanelLayout.setVerticalGroup(
            resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resultsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel5)
                    .addComponent(searchText, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(allChkBox)
                    .addGroup(resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tagsChkBox)
                        .addComponent(titleChkBox, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(textChkBox, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(superFuzzy)
                        .addComponent(listAllCardsLbl)
                        .addComponent(clearResultsLbl))
                    .addComponent(addBookmarkLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newCardLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                    .addComponent(deleteCardLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE)
                .addContainerGap())
        );

        resultsPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {allChkBox, deleteCardLbl, newCardLbl, tagsChkBox, textChkBox, titleChkBox});

        innerSplitPane.setLeftComponent(resultsPanel);

        outerSplitPane.setRightComponent(innerSplitPane);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel3.setName("jPanel3"); // NOI18N

        indexesScrollPane.setName("indexesScrollPane"); // NOI18N

        indexesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        indexesTable.setName("indexesTable"); // NOI18N
        indexesTable.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                indexesTableFocusLost(evt);
            }
        });
        indexesScrollPane.setViewportView(indexesTable);

        searchAllIndices.setSelected(true);
        searchAllIndices.setText(resourceMap.getString("searchAllIndices.text")); // NOI18N
        searchAllIndices.setName("searchAllIndices"); // NOI18N
        searchAllIndices.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchAllIndicesActionPerformed(evt);
            }
        });

        addIndexLbl.setIcon(resourceMap.getIcon("addIndexLbl.icon")); // NOI18N
        addIndexLbl.setText(resourceMap.getString("addIndexLbl.text")); // NOI18N
        addIndexLbl.setName("addIndexLbl"); // NOI18N
        addIndexLbl.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                addIndexLblMouseClicked(evt);
            }
        });

        deleteIndex.setIcon(resourceMap.getIcon("deleteIndex.icon")); // NOI18N
        deleteIndex.setText(resourceMap.getString("deleteIndex.text")); // NOI18N
        deleteIndex.setName("deleteIndex"); // NOI18N
        deleteIndex.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                deleteIndexMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3Layout.setHorizontalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.TRAILING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
        				.addGroup(jPanel3Layout.createSequentialGroup()
        					.addComponent(searchAllIndices, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        					.addGap(75)
        					.addComponent(addIndexLbl)
        					.addPreferredGap(ComponentPlacement.RELATED)
        					.addComponent(deleteIndex, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
        				.addGroup(jPanel3Layout.createSequentialGroup()
        					.addGap(10)
        					.addComponent(indexesScrollPane, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)))
        			.addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
        	jPanel3Layout.createParallelGroup(Alignment.LEADING)
        		.addGroup(jPanel3Layout.createSequentialGroup()
        			.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
        				.addComponent(deleteIndex, 0, 0, Short.MAX_VALUE)
        				.addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING, false)
        					.addComponent(addIndexLbl, 0, 0, Short.MAX_VALUE)
        					.addComponent(searchAllIndices, GroupLayout.PREFERRED_SIZE, 20, Short.MAX_VALUE)))
        			.addPreferredGap(ComponentPlacement.RELATED)
        			.addComponent(indexesScrollPane, GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
        			.addContainerGap())
        );
        jPanel3.setLayout(jPanel3Layout);

        outerSplitPane.setLeftComponent(jPanel3);

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(com.gauronit.tagmata.Main.class).getContext().getActionMap(MainView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        helpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.ALT_MASK));
        helpMenuItem.setText(resourceMap.getString("helpMenuItem.text")); // NOI18N
        helpMenuItem.setName("helpMenuItem"); // NOI18N
        helpMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                helpMenuItemMouseClicked(evt);
            }
        });
        helpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuItem);

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK));
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 1061, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 891, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        indicesPopup.setName("indicesPopup"); // NOI18N

        
        
        setContentPane(mainPanel);
        setJMenuBar(menuBar);
    }// </editor-fold>//GEN-END:initComponents
    private boolean titleChecked = true;
    private boolean tagsChecked = true;
    private boolean textChecked = true;

    private void innerSplitPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_innerSplitPaneComponentResized
    }//GEN-LAST:event_innerSplitPaneComponentResized

    private void innerSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_innerSplitPanePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_innerSplitPanePropertyChange

    private void newCardLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_newCardLblMouseClicked
        // TODO add your handling code here:
        CardView card = new CardView(Main.getApplication().getMainFrame(), true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - card.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - card.getHeight()) / 2);
        card.setLocation(x, y);
        card.setVisible(true);
    }//GEN-LAST:event_newCardLblMouseClicked

    private void searchAllIndicesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchAllIndicesActionPerformed
        // TODO add your handling code here:
        boolean selected = searchAllIndices.isSelected();
        int indexCount = indexesTable.getRowCount();
        for (int i = 0; i < indexCount; i++) {
            indexesTable.setValueAt(selected, i, 0);
        }
    }//GEN-LAST:event_searchAllIndicesActionPerformed

    private void mainPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_mainPanelPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_mainPanelPropertyChange

    private void mainPanelAncestorResized(java.awt.event.HierarchyEvent evt) {//GEN-FIRST:event_mainPanelAncestorResized
        if (innerSplitPane.getHeight() != 0) {
            innerSplitPane.setDividerLocation(0.7);
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_mainPanelAncestorResized

    private void addIndexLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addIndexLblMouseClicked
        // TODO add your handling code here:
        Object indexDisplayName = JOptionPane.showInputDialog(Main.getApplication().getMainFrame(), "Name for the new index", "Index Creation", JOptionPane.PLAIN_MESSAGE, resourceMap.getIcon("newIndexIcon2"), null, null);
        if (indexDisplayName != null && indexDisplayName.toString().trim().length() != 0) {
            if (indexDisplayName.toString().length() >= 100) {
                JOptionPane.showMessageDialog(Main.getApplication().getMainFrame(), "The index name you enter is too long. A maximum of 100 letters is permissible.", "Invalid Index Name", JOptionPane.ERROR_MESSAGE);
            } else {
                for (Map.Entry<String, Indexes.Index> entry : Indexes.getIndexes().entrySet()) {
                    Indexes.Index index = entry.getValue();
                    if (index.getDisplayName().equals(indexDisplayName)) {
                        JOptionPane.showMessageDialog(Main.getApplication().getMainFrame(), "Index with the same name already exists. Please choose a different name.", "Index Creation", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                try {
                    String indexName = Indexer.currentInstance().createIndex(indexDisplayName.toString());
                    ((DefaultTableModel) indexesTable.getModel()).addRow(new Object[]{true, indexDisplayName.toString(), indexName});
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                DefaultRowSorter sorter = ((DefaultRowSorter) indexesTable.getRowSorter());
                ArrayList list = new ArrayList();
                list.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
                sorter.setSortKeys(list);
                sorter.sort();
            }
        }
    }//GEN-LAST:event_addIndexLblMouseClicked

    private void deleteIndexMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteIndexMouseClicked
        // TODO add your handling code here:
        int row = indexesTable.getSelectedRow();
        if (row != -1) {
            String indexDisplayName = indexesTable.getValueAt(row, 1).toString();
            for (int i = 0; i < indexesTable.getModel().getRowCount(); i++) {
                if (indexesTable.getModel().getValueAt(i, 1).toString().equals(indexDisplayName)) {
                    String indexName = indexesTable.getModel().getValueAt(i, 2).toString();
                    if (indexDisplayName.equals("General")) {
                        JOptionPane.showMessageDialog(Main.getApplication().getMainFrame(), "Can't delete the main index. Use it to save cards that are miscellaneous.", "Index Deletion", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    int choice = JOptionPane.showConfirmDialog(Main.getApplication().getMainFrame(), "Delete Index '" + indexDisplayName + "'? All the cards saved in it will be deleted and will not be recovered.", "Index Deletion", JOptionPane.YES_NO_OPTION);
                    if (choice == 0) {
                        ((DefaultTableModel) indexesTable.getModel()).removeRow(i);
                        Indexes.removeIndex(indexName);
                    }
                }
            }
        }
        if (!queryText.trim().equals("") || results.getModel().getSize() != 0) {
            fillResults();
            refreshBookmarks();
        }
    }//GEN-LAST:event_deleteIndexMouseClicked
    private ArrayList<CardSnapshot> cardSnaps;

    public void fillResults() {
        try {
            ((DefaultListModel) results.getModel()).clear();
            String text = queryText;
            int indexesCount = indexesTable.getRowCount();
            ArrayList<String> indexNames = new ArrayList<String>();
            for (int i = 0; i < indexesCount; i++) {
                boolean selected = (Boolean) indexesTable.getModel().getValueAt(i, 0);
                if (selected) {
                    indexNames.add(indexesTable.getModel().getValueAt(i, 2).toString());
                }
            }
            boolean searchTitle = titleChkBox.isSelected();
            boolean searchTags = tagsChkBox.isSelected();
            boolean searchText = textChkBox.isSelected();

            cardSnaps = Indexer.currentInstance().search(text, indexNames, searchTitle, searchTags, searchText, text.trim().equals("") ? true : superFuzzy.isSelected());
            for (CardSnapshot cardSnap : cardSnaps) {
                String item = "<html><b>Title </b>" + StringUtil.getHTMLEntities(cardSnap.getTitle());
                item += "<br/><b>Tags</b> " + StringUtil.getHTMLEntities(cardSnap.getTags());
                item += "<br/>" + StringUtil.getHTMLEntities(cardSnap.getHighlights());
                item += "<br/>";
                item += "<table width='600px'><tr><td><hr/></td></tr></table></html>";
                ((DefaultListModel) results.getModel()).add(results.getModel().getSize(), item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void searchTextKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchTextKeyTyped
        // TODO add your handling code here:
        try {
            if (evt.getKeyChar() == '\n') {
                queryText = searchText.getText();
                fillResults();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_searchTextKeyTyped

    private void indexesTableFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_indexesTableFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_indexesTableFocusLost
    private String queryText = "";
    private void resultsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_resultsMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2 && !evt.isConsumed()) {
            if (results.getModel().getSize() != 0) {
                CardSnapshot cardSnap = cardSnaps.get(results.getSelectedIndex());

                CardView card = new CardView(cardSnap, Main.getApplication().getMainFrame(), true);

                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
                int x = (int) ((dimension.getWidth() - card.getWidth()) / 2);
                int y = (int) ((dimension.getHeight() - card.getHeight()) / 2);
                card.setLocation(x, y);
                card.setVisible(true);
            }
        }
    }//GEN-LAST:event_resultsMouseClicked

    private void deleteCardLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteCardLblMouseClicked
        // TODO add your handling code here:
        if (results.getModel().getSize() != 0) {
            int result = JOptionPane.showConfirmDialog(Main.getApplication().getMainFrame(), "Delete selected cards?", "Cards Deletion", JOptionPane.OK_CANCEL_OPTION, 0, resourceMap.getIcon("deleteCardLbl.icon"));
            if (result == 2) {
                return;
            }
            int[] rows = results.getSelectedIndices();
            ArrayList cardSnapsDelete = new ArrayList();
            for (int row : rows) {
                cardSnapsDelete.add(cardSnaps.get(row));
            }
            try {
                Indexer.currentInstance().deleteCards(cardSnapsDelete);
                fillResults();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_deleteCardLblMouseClicked

private void addBookmarkLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_addBookmarkLblMouseClicked
// TODO add your handling code here:
    if (results.getModel().getSize() != 0 && results.getSelectedIndex() != -1) {
        try {
            CardSnapshot cardSnap = cardSnaps.get(results.getSelectedIndex());
            Indexer.currentInstance().saveBookmark(cardSnap.getId(), cardSnap.getTitle(), cardSnap.getTags(), cardSnap.getText(), cardSnap.getIndexName());
        } catch (Exception ex) {
            Logger.getLogger(MainView.class.getName()).log(Level.SEVERE, null, ex);
        }
        refreshBookmarks();
    }
}//GEN-LAST:event_addBookmarkLblMouseClicked

private void quickCardsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_quickCardsMouseClicked
// TODO add your handling code here:
    if (evt.getClickCount() == 2 && !evt.isConsumed()) {
        if (quickCards.getModel().getSize() != 0) {
            CardSnapshot cardSnap = bookmarks.get(quickCards.getSelectedIndex());

            CardView card = new CardView(cardSnap, Main.getApplication().getMainFrame(), true);

            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (int) ((dimension.getWidth() - card.getWidth()) / 2);
            int y = (int) ((dimension.getHeight() - card.getHeight()) / 2);
            card.setLocation(x, y);
            card.setVisible(true);
        }
    }
}//GEN-LAST:event_quickCardsMouseClicked

private void deleteQuickCardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_deleteQuickCardMouseClicked
// TODO add your handling code here:
    if (quickCards.getModel().getSize() != 0 && quickCards.getSelectedIndex() != -1) {
        try {
            CardSnapshot cardSnap = bookmarks.get(quickCards.getSelectedIndex());
            Indexer.currentInstance().deleteBookmark(cardSnap.getId(), cardSnap.getIndexName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        refreshBookmarks();
    }
}//GEN-LAST:event_deleteQuickCardMouseClicked

private void listAllCardsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listAllCardsLblMouseClicked
// TODO add your handling code here:
    String temp = queryText;
    queryText = "";
    fillResults();
    queryText = temp;
}//GEN-LAST:event_listAllCardsLblMouseClicked

private void clearResultsLblMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_clearResultsLblMouseClicked
// TODO add your handling code here:
    cardSnaps = new ArrayList();
    ((DefaultListModel) results.getModel()).removeAllElements();
}//GEN-LAST:event_clearResultsLblMouseClicked

private void helpMenuItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_helpMenuItemMouseClicked
// TODO add your handling code here:

}//GEN-LAST:event_helpMenuItemMouseClicked

private void helpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpMenuItemActionPerformed
// TODO add your handling code here:
    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
    try {
        java.net.URI uri = new java.net.URI("http://www.gauronit.com/tagmata.html");
        desktop.browse(uri);
    } catch (Exception e) {
        e.printStackTrace();
    }
}//GEN-LAST:event_helpMenuItemActionPerformed
    private ArrayList<CardSnapshot> bookmarks = new ArrayList();

    public void refreshBookmarks() {
        try {
            bookmarks = Indexer.currentInstance().getBookmarks();
            DefaultListModel model = (DefaultListModel) quickCards.getModel();
            model.removeAllElements();
            for (CardSnapshot cardSnap : bookmarks) {
                String item = "<html><b>Title </b>" + StringUtil.getHTMLEntities(cardSnap.getTitle());
                item += "<br/><b>Tags</b> " + StringUtil.getHTMLEntities(cardSnap.getTags());
                if (cardSnap.getText().length() > 100) {
                    item += "<br/>" + StringUtil.getHTMLEntities(cardSnap.getText().substring(0, 100));
                } else {
                    item += "<br/>" + StringUtil.getHTMLEntities(cardSnap.getText());
                }
                item += "<br/>";
                item += "<table width='600px'><tr><td><hr/></td></tr></table></html>";
                model.add(model.getSize(), item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void allChkBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (allChkBox.isSelected()) {
            titleChkBox.setSelected(true);
            tagsChkBox.setSelected(true);
            textChkBox.setSelected(true);
        } else {
            titleChkBox.setSelected(titleChecked);
            tagsChkBox.setSelected(tagsChecked);
            textChkBox.setSelected(textChecked);
            if (titleChkBox.isSelected()) {
                titleChecked = true;
            }
            if (tagsChkBox.isSelected()) {
                tagsChecked = true;
            }
            if (textChkBox.isSelected()) {
                textChecked = true;
            }
            if (titleChkBox.isSelected() && tagsChkBox.isSelected() && textChkBox.isSelected()) {
                allChkBox.setSelected(true);
            }
        }
    }

    private void titleChkBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!titleChkBox.isSelected() && !tagsChkBox.isSelected() && !textChkBox.isSelected()) {
            if (titleChecked == true) {
                titleChkBox.setSelected(true);
            }
        }
        if (titleChkBox.isSelected() && tagsChkBox.isSelected() && textChkBox.isSelected()) {
            allChkBox.setSelected(true);
        }
        if (!titleChkBox.isSelected() || !tagsChkBox.isSelected() || !textChkBox.isSelected()) {
            allChkBox.setSelected(false);
        }
        if (titleChkBox.isSelected()) {
            titleChecked = true;
        } else {
            titleChecked = false;
        }
        if (tagsChkBox.isSelected()) {
            tagsChecked = true;
        } else {
            tagsChecked = false;
        }
        if (textChkBox.isSelected()) {
            textChecked = true;
        } else {
            textChecked = false;
        }
    }

    private void tagsChkBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!titleChkBox.isSelected() && !tagsChkBox.isSelected() && !textChkBox.isSelected()) {
            if (tagsChecked == true) {
                tagsChkBox.setSelected(true);
            }
        }
        if (titleChkBox.isSelected() && tagsChkBox.isSelected() && textChkBox.isSelected()) {
            allChkBox.setSelected(true);
        }
        if (!titleChkBox.isSelected() || !tagsChkBox.isSelected() || !textChkBox.isSelected()) {
            allChkBox.setSelected(false);
        }
        if (titleChkBox.isSelected()) {
            titleChecked = true;
        } else {
            titleChecked = false;
        }
        if (tagsChkBox.isSelected()) {
            tagsChecked = true;
        } else {
            tagsChecked = false;
        }
        if (textChkBox.isSelected()) {
            textChecked = true;
        } else {
            textChecked = false;
        }
    }

    private void textChkBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
        if (!titleChkBox.isSelected() && !tagsChkBox.isSelected() && !textChkBox.isSelected()) {
            if (textChecked == true) {
                textChkBox.setSelected(true);
            }
        }
        if (titleChkBox.isSelected() && tagsChkBox.isSelected() && textChkBox.isSelected()) {
            allChkBox.setSelected(true);
        }
        if (!titleChkBox.isSelected() || !tagsChkBox.isSelected() || !textChkBox.isSelected()) {
            allChkBox.setSelected(false);
        }
        if (titleChkBox.isSelected()) {
            titleChecked = true;
        } else {
            titleChecked = false;
        }
        if (tagsChkBox.isSelected()) {
            tagsChecked = true;
        } else {
            tagsChecked = false;
        }
        if (textChkBox.isSelected()) {
            textChecked = true;
        } else {
            textChecked = false;
        }

    }

    public String getSelectedIndexDisplayName() {
        int row = indexesTable.getSelectedRow();

        if (row == -1) {
            return indexesTable.getValueAt(0, 1).toString();
        }
        return indexesTable.getValueAt(row, 1).toString();
    }

    public String getSelectedIndexName() {
        int row = indexesTable.getSelectedRow();
        String indexDisplayName = "General";
        if (row != -1) {
            indexDisplayName = indexesTable.getValueAt(row, 1).toString();
        }
        for (int i = 0; i < indexesTable.getModel().getRowCount(); i++) {
            if (((String) indexesTable.getModel().getValueAt(i, 1)).equals(indexDisplayName)) {
                return (String) indexesTable.getModel().getValueAt(i, 2);
            }
        }

        return indexesTable.getModel().getValueAt(row, 2).toString();
    }

    public String getIndexDisplayName(String indexName) {
        for (int i = 0; i < indexesTable.getModel().getRowCount(); i++) {
            if (((String) indexesTable.getModel().getValueAt(i, 2)).equals(indexName)) {
                return (String) indexesTable.getModel().getValueAt(i, 1);
            }
        }
        return null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel addBookmarkLbl;
    private javax.swing.JLabel addIndexLbl;
    private javax.swing.JCheckBox allChkBox;
    private javax.swing.JLabel clearResultsLbl;
    private javax.swing.JLabel deleteCardLbl;
    private javax.swing.JLabel deleteIndex;
    private javax.swing.JLabel deleteQuickCard;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JScrollPane indexesScrollPane;
    private javax.swing.JTable indexesTable;
    private javax.swing.JPopupMenu indicesPopup;
    private javax.swing.JSplitPane innerSplitPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel listAllCardsLbl;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel newCardLbl;
    private javax.swing.JSplitPane outerSplitPane;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JList quickCards;
    private javax.swing.JPanel quickCardsPanel;
    private javax.swing.JList results;
    private javax.swing.JPanel resultsPanel;
    private javax.swing.JCheckBox searchAllIndices;
    private javax.swing.JTextField searchText;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JCheckBox superFuzzy;
    private javax.swing.JCheckBox tagsChkBox;
    private javax.swing.JCheckBox textChkBox;
    private javax.swing.JCheckBox titleChkBox;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
}
