package jp.tokyo.selj.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.Date;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.common.PreferenceWindowHelper;
import jp.tokyo.selj.common.TextUndoHandler;
import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.model.OutputOfWorkListModel;

public class DlgWorkDetail extends DlgOutputDetail {
	JTextField inpPointer;
    protected TextUndoHandler pointerUndoHandler_ = new TextUndoHandler();	
	private JFormattedTextField inpCompDate;

	private JPanel jPanel = null;  //  @jve:decl-index=0:visual-constraint="547,15"
	private JCheckBox inpCompFlag = null;
	
	class ActUpdateWork extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgWorkDetail.this;
		}
		public ActUpdateWork(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "作業の更新を行います");
		}
		public void actionPerformed2(ActionEvent e) {
			DlgWorkDetail.super.commitOutput(e);
			work_.setPointer(inpPointer.getText());
			work_.setCompFlg(inpCompFlag.isSelected());
			if(inpCompDate.getValue() != null){
				work_.setCompDate(new Timestamp(
						((Date)inpCompDate.getValue()).getTime() ));
			}else{
				work_.setCompDate(null);
			}
			work_.check();
			//更新
			((OutputOfWorkListModel)model_).updateWork(work_);
			DlgWorkDetail.this.setVisible(false);
		}
	}

	
	/**
	 * This method initializes 
	 * 
	 */
	public DlgWorkDetail() {
		super();
		initialize();
	}
	public DlgWorkDetail(JFrame owner) {
		super(owner, true);
		initialize();
	}
	public void setup(OutputOfWorkListModel model){
		super.setup(model);
		getInpPointer().getDocument().addDocumentListener(docListener_);
		getCmdOk().setAction(new ActUpdateWork());	//actionを入れ替える
		
		pointerUndoHandler_.setup(getInpPointer());
		getJTree().setToolTipText(getJTree().getToolTipText()
				+"緑の下線は、選択中の作業にLinkしているノード。");
	}

	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		Dimension size = getSize();
		size.height += 50;	//pointerなどの入力エリアを追加するので少し高くする
        this.setSize(size);
		//「作業」属性分を
		GridBagConstraints gbcPointer = new GridBagConstraints();
		gbcPointer.fill = GridBagConstraints.HORIZONTAL;
		gbcPointer.weightx = 1.0;
		gbcPointer.weighty = 1.0;
		gbcPointer.gridwidth = 5;
		gbcPointer.gridx = 0;
		gbcPointer.gridy = 4;
		getInputPanel().add(getJPanel(), gbcPointer);
		this.pack();
	}
	private JTextField getInpPointer() {
		if (inpPointer == null) {
			inpPointer = new JTextField();
			inpPointer.setBorder(new TitledBorder("pointer"));
			inpPointer.setToolTipText("成果物内の位置を記述します");
			inpPointer.setOpaque(false);
		}
		return inpPointer;
	}
	Work work_ = null;  //  @jve:decl-index=0:
	PreferenceWindowHelper pref_;
	public void setWork(Work work) {
		isUpdate_ = true;
		work_ = work;
		
		resetOutputTypeList(work_.getWorkTypeId());		//成果物種類のコンボボックスリストをリセット
		refreshDocTree(work);
		
		pointerUndoHandler_.cleanup();
		pref_ = new PreferenceWindowHelper(this);
		pref_.restoreForm();
		docListener_.reset();
	}
	void refreshDocTree(Work work){
		getPnlOutput().setOutput(work.getOutput());
		inpPointer.setText(work.getPointer());
		inpCompDate.setValue(work.getCompDate());
		inpCompFlag.setSelected(work.getCompFlg());
		// TreeCellRendererの設定
		((DocTreeCellRenderer4Work)getJTree().getCellRenderer()).setWork(work);
		
		refreshDocTree(work.getOutput());
	}
	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "work property", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.TOP, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51));
			titledBorder.setTitlePosition(2);
			GridBagConstraints gbcPointer = new GridBagConstraints();
			gbcPointer.fill = GridBagConstraints.HORIZONTAL;
			gbcPointer.gridx = 0;
			gbcPointer.gridy = 0;
			gbcPointer.weightx = 5.0;
			GridBagConstraints gbcComplFlag = new GridBagConstraints();
			gbcComplFlag.gridx = 1;
			gbcComplFlag.anchor = GridBagConstraints.EAST;
			gbcComplFlag.gridy = 0;
			GridBagConstraints gbcComplDate = new GridBagConstraints();
			gbcComplDate.fill = GridBagConstraints.NONE;
			gbcComplDate.weightx = 0.0;
			gbcComplDate.gridx = 2;
			gbcComplDate.gridy = 0;
			jPanel = new JPanel();
			jPanel.setLayout(new GridBagLayout());
			jPanel.setSize(new Dimension(317, 64));
			jPanel.setBackground(new Color(204, 255, 204));
			jPanel.setBorder(titledBorder);
			jPanel.add(getInpCompFlag(), gbcComplFlag);
			jPanel.add(getInpPointer(), gbcPointer);
			jPanel.add(getInpDate(), gbcComplDate);
		}
		return jPanel;
	}
	/**
	 * This method initializes inpCompFlag	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpCompFlag() {
		if (inpCompFlag == null) {
			inpCompFlag = new JCheckBox();
			inpCompFlag.setText("complete");
			inpCompFlag.setToolTipText("作業が完了した場合on");
			inpCompFlag.setOpaque(false);
			inpCompFlag.addChangeListener(new javax.swing.event.ChangeListener() {
				public void stateChanged(javax.swing.event.ChangeEvent e) {
					if(inpCompFlag.isSelected()){
						getInpDate().setValue(new Date());
					}else{
						getInpDate().setValue(null);
					}
				}
			});
		}
		return inpCompFlag;
	}
	private JFormattedTextField getInpDate() {
		if (inpCompDate == null) {
			inpCompDate = new JFormattedTextField(new DateTextFormatter());
			inpCompDate.setBorder(new TitledBorder("complete date"));
			inpCompDate.setOpaque(false);
		}
		return inpCompDate;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
