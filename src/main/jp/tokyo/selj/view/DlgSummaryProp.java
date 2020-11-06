package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import jp.tokyo.selj.common.PreferenceWindowHelper;

public class DlgSummaryProp extends JDialog {

	PreferenceWindowHelper pref_;
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane scrSummaryProp = null;

	private JTable dspSummaryProp = null;

	/**
	 * @param owner
	 */
	public DlgSummaryProp(Frame owner) {
		super(owner);
		initialize();
	}
	public DlgSummaryProp(JDialog owner){
		super(owner);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(571, 222);
		this.setContentPane(getJContentPane());
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
			jContentPane.add(getScrSummaryProp(), BorderLayout.CENTER);
		}
		return jContentPane;
	}

	/**
	 * This method initializes scrSummaryProp	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrSummaryProp() {
		if (scrSummaryProp == null) {
			scrSummaryProp = new JScrollPane();
			scrSummaryProp.setViewportView(getDspSummaryProp());
		}
		return scrSummaryProp;
	}

	/**
	 * This method initializes dspSummaryProp	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspSummaryProp() {
		if (dspSummaryProp == null) {
			dspSummaryProp = new JTable();
		}
		return dspSummaryProp;
	}

	public void setModel(TableModel model) {
		getDspSummaryProp().setModel(model);
	}

	public void setup(){
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
