package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.Writer;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.util.ClipboardStringWriter;

public class DlgSummaryResult extends JDialog {

	PreferenceWindowHelper pref_;
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JScrollPane scrSummaryProp = null;

	private JTable dspSummaryProp = null;
	private JPanel cntHead = null;
	private JCheckBox inpSumPlan = null;
	private JButton cmdCopyToClipboard = null;

	class ActCopyToClipboard extends ActBase {
		public ActCopyToClipboard() {
			putValue(Action.NAME, "copy to clipboard");
			putValue(Action.SHORT_DESCRIPTION, "結果をクリップボードへコピーします");
		}
		@Override
		public void actionPerformed2(ActionEvent e) {
			Writer writer = new ClipboardStringWriter();
			try{
				String line = "";
				String sep = "";
				for(int col =0; col < dspSummaryProp.getColumnCount(); col++){
					line = line + sep + dspSummaryProp.getColumnName(col);
					sep = "\t";
				}
				writer.write(line + "\n");
				for(int row =0; row < dspSummaryProp.getRowCount(); row++){
					line = "";
					sep = "";
					for(int col =0; col < dspSummaryProp.getColumnCount(); col++){
						line = line + sep + 
							((dspSummaryProp.getValueAt(row, col) == null)? 
									"":dspSummaryProp.getValueAt(row, col));
						sep = "\t";
					}
					writer.write(line + "\n");
				}
				writer.flush();
			}catch(IOException ex){
				throw new RuntimeException(ex);
			}finally{
				try {
					writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		
		}
		protected Component getOwnerComponent(){
			return DlgSummaryResult.this;
		}
	}
	
	/**
	 * @param owner
	 */
	public DlgSummaryResult(Frame owner) {
		super(owner, true);
		initialize();
	}
	public DlgSummaryResult(JDialog owner){
		super(owner, true);
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
			jContentPane.add(getCntHead(), BorderLayout.NORTH);
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

	public void setModel(SummaryPropModel model) {
		model.setSumPlan(getInpSumPlan().isSelected());
		getDspSummaryProp().setModel(model);
	}

	public void setup(){
		getCmdCopyToClipboard().setAction(new ActCopyToClipboard());
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
	}
	/**
	 * This method initializes cntHead	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntHead() {
		if (cntHead == null) {
			cntHead = new JPanel();
			cntHead.setLayout(new BoxLayout(cntHead, BoxLayout.X_AXIS));
			cntHead.add(Box.createHorizontalGlue());
			cntHead.add(getInpSumPlan());
			cntHead.add(Box.createHorizontalStrut(30));
			cntHead.add(getCmdCopyToClipboard());
			cntHead.add(Box.createHorizontalGlue());
		}
		return cntHead;
	}
	/**
	 * This method initializes inpSumPlan	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpSumPlan() {
		if (inpSumPlan == null) {
			inpSumPlan = new JCheckBox();
			inpSumPlan.setText("show plan");
			inpSumPlan.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					SummaryPropModel model = (SummaryPropModel)getDspSummaryProp().getModel();
					model.setSumPlan(((JCheckBox)e.getSource()).isSelected());
				}
			});
		}
		return inpSumPlan;
	}
	/**
	 * This method initializes cmdCopyToClipboard	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdCopyToClipboard() {
		if (cmdCopyToClipboard == null) {
			cmdCopyToClipboard = new JButton();
		}
		return cmdCopyToClipboard;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"
