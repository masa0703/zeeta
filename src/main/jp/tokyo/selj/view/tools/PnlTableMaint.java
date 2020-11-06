package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class PnlTableMaint extends JPanel {
	public static final String ACTION_KEY_NEW = "new action";  //  @jve:decl-index=0:
	public static final String ACTION_KEY_UPDATE = "update action";  //  @jve:decl-index=0:
	public static final String ACTION_KEY_DELETE = "delete action";
	public static final String ACTION_KEY_SELECTED = "selected action";
	
	
	private JScrollPane cntScrBar = null;
	private JTable dspTable = null;
	private JPanel cntHeader = null;
	private JLabel dspTitle = null;
	private JButton cmdNew = null;
	private JButton cmdUpdate = null;
	private JButton cmdDelete = null;

	/**
	 * This method initializes 
	 * 
	 */
	public PnlTableMaint() {
		super();
		initialize();
	}
	
	
	public void setTitle(String title){
		dspTitle.setText("  " +title + "  ");
	}
	public JTable getJTable(){
		return getDspTable();
	}
	public void setTableSelectionListener(ListSelectionListener l){
		ListSelectionModel selModel = getDspTable().getSelectionModel();
		selModel.addListSelectionListener(l);
	}
	public void setup(){
		getCmdNew().setAction(this.getActionMap().get(ACTION_KEY_NEW));
		getCmdUpdate().setAction(this.getActionMap().get(ACTION_KEY_UPDATE));
		getCmdDelete().setAction(this.getActionMap().get(ACTION_KEY_DELETE));
		//popup menuにも追加する
	}
	public void hideButtons(){
		getCmdDelete().setVisible(false);
		getCmdNew().setVisible(false);
		getCmdUpdate().setVisible(false);
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setSize(640, 100);
		this.setLayout(new BorderLayout());
		this.add(getCntScrBar(), java.awt.BorderLayout.CENTER);
		this.add(getCntHeader(), java.awt.BorderLayout.NORTH);
	}

	/**
	 * This method initializes cntScrBar	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrBar() {
		if (cntScrBar == null) {
			cntScrBar = new JScrollPane();
			cntScrBar.setViewportView(getDspTable());
		}
		return cntScrBar;
	}

	/**
	 * This method initializes dspTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspTable() {
		if (dspTable == null) {
			dspTable = new JTable();
			dspTable.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if(e.getClickCount() >= 2){
						Action act = PnlTableMaint.this.getActionMap().get(ACTION_KEY_UPDATE);
						act.actionPerformed(new ActionEvent(this, 0, ACTION_KEY_UPDATE));
					}
				}
			});
			dspTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		return dspTable;
	}

	/**
	 * This method initializes cntHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHeader() {
		if (cntHeader == null) {
			dspTitle = new JLabel();
			dspTitle.setText("(undefined)");
			FlowLayout flowLayout = new FlowLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			cntHeader = new JPanel();
			cntHeader.setLayout(flowLayout);
			cntHeader.add(dspTitle, null);
			cntHeader.add(getCmdNew(), null);
			cntHeader.add(getCmdUpdate(), null);
			cntHeader.add(getCmdDelete(), null);
		}
		return cntHeader;
	}

	/**
	 * This method initializes cmdNew	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNew() {
		if (cmdNew == null) {
			cmdNew = new JButton();
			cmdNew.setText("new");
		}
		return cmdNew;
	}

	/**
	 * This method initializes cmdUpdate	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdUpdate() {
		if (cmdUpdate == null) {
			cmdUpdate = new JButton();
			cmdUpdate.setText("update");
		}
		return cmdUpdate;
	}

	/**
	 * This method initializes cmdDelete	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdDelete() {
		if (cmdDelete == null) {
			cmdDelete = new JButton();
			cmdDelete.setText("delete");
		}
		return cmdDelete;
	}

}
