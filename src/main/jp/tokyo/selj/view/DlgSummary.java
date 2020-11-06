package jp.tokyo.selj.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import jp.tokyo.selj.common.DlgWait;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.OutputPropTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkPropDao;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.DocModel;
import jp.tokyo.selj.model.MasterComboModel;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;
import javax.swing.JScrollPane;

public class DlgSummary extends JDialog {

	Logger log = Logger.getLogger(this.getClass());  //  @jve:decl-index=0:
	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JTabbedPane jTabbedPane = null;

	Doc doc_;  //  @jve:decl-index=0:
	DocModel docModel_;  //  @jve:decl-index=0:
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
	WorkPropDao workPropDao_ = null;
	OutputPropTypeDao outputPropTypeDao_ = null;
	OutputPropTypeListModel outputPropTypeListModel_ = null;
	
	class OutputPropTypeListModel extends DefaultListModel{
		public void setWorkType(Object[] workTypes){
			clear();
			if(workTypes.length <= 0){
				return;
			}
			
			//workTypeIdの配列を作成する
			long[] ids = new long[workTypes.length];
			
			for(int i=0; i<workTypes.length; i++){
				WorkType wt = (WorkType)workTypes[i];
				ids[i] = wt.getWorkTypeId();
			}
			
			//OutputTypeを取得
			List<OutputPropType> outputPropTypes = 
				outputPropTypeDao_.findByWorkTypeId(ids);
			for(OutputPropType opt:outputPropTypes){
				addElement(opt);
			}
		}
	}
	
	//選択の反転
	class ActReverseSelection extends AbstractAction {
		JFileChooser chooser_=null;
		public ActReverseSelection(){
			putValue(Action.NAME, "reverse selection");
			putValue(Action.SHORT_DESCRIPTION, "選択状態を反転します");
		}
		public void actionPerformed(ActionEvent e) {
			JList list = null;
			if( e.getSource() == cmdReverseSelWork){
				list = inpSelWork;
			}else{
				list = inpSelProp;
			}
			for(int i=0; i < list.getModel().getSize(); i++){
				if(!list.isSelectedIndex(i)){
					list.addSelectionInterval(i,i);
				}else{
					list.removeSelectionInterval(i,i);
				}
			}
		}
	}
	ActReverseSelection actReverseSelection_ = new ActReverseSelection();  //  @jve:decl-index=0:

	//サマリ
	class ActSummary extends AbstractAction {
		JFileChooser chooser_=null;
		DlgSummaryResult dlg_ = null;
		public ActSummary(){
			putValue(Action.NAME, "summary");
			putValue(Action.SHORT_DESCRIPTION, "選択ノード配下の作業属性を集計します");
		}
		public void actionPerformed(ActionEvent e) {
			//選択作業種類
//			List<WorkType> workTypes = new ArrayList<WorkType>();
//			for(int i=0; i < inpSelWork.getSelectedValues().length; i++){
//				workTypes.add((WorkType)inpSelWork.getSelectedValues()[i]);
//			}
			

			setCursor(Util.WAIT_CURSOR);
			try{
				new Thread(){
					public void run(){					
						DlgWait wd = new DlgWait(DlgSummary.this);
						wd.setVisible(true);
						SummaryPropModel summModel;
						DlgSummary.this.setEnabled(false);
						try{
							summModel = sum(wd);
						}finally{
							wd.setVisible(false);
							DlgSummary.this.setEnabled(true);
						}
						//表示
						DlgSummaryResult dlg = getDlg();
						dlg.setModel(summModel);
						dlg.setVisible(true);
					}
				}.start();
			}finally{
				setCursor(Cursor.getDefaultCursor());
			}

		}
		SummaryPropModel sum(DlgWait wd){
			SummaryPropModel summ = 
				new SummaryPropModel(
						docModel_, 
						inpSelWork.getSelectedValues(),
						inpSelProp.getSelectedValues()
				);
			summ.summary(doc_, inpDepth.getValue(), wd);
			return summ;
		}
		
		DlgSummaryResult getDlg(){
			if(dlg_ == null){
				dlg_ = new DlgSummaryResult(DlgSummary.this);
				dlg_.setLocationRelativeTo(DlgSummary.this);
				dlg_.setup();
			}
			return dlg_;
		}
	}
	ActSummary actSummary_ = new ActSummary();  //  @jve:decl-index=0:

	
	
	private JPanel jPanel3 = null;
	private JSlider inpDepth = null;
	private JPanel cntSummary = null;
	private JSplitPane cntSplit = null;
	private JPanel cntSummaryBtn = null;
	private JButton cmdSummary = null;
	private JButton cmdReverseSelWork = null;
	private JButton cmdReverseSelProp = null;
	private JPanel cntLeftSum = null;
	private JPanel cntRightSum = null;
	private JList inpSelWork = null;
	private JList inpSelProp = null;
	private JScrollPane cntScrWork = null;
	private JScrollPane cntScrProp = null;
	/**
	 * @param owner
	 */
	public DlgSummary(Frame owner) {
		super(owner, true);
		initialize();
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(595, 328);
		this.setTitle("summary");
		this.setMinimumSize(new Dimension(480, 320));
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
			jContentPane.add(getJTabbedPane(), BorderLayout.CENTER);
			jContentPane.add(getJPanel3(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
			jTabbedPane.addTab("summary", null, getCntSummary(), null);
		}
		return jTabbedPane;
	}

	public void setup(Doc doc, DocModel docModel) {
		doc_ = doc;
		docModel_ = docModel;
		workPropDao_ = (WorkPropDao)daoCont_.getComponent(WorkPropDao.class);
		outputPropTypeDao_= (OutputPropTypeDao)daoCont_.getComponent(OutputPropTypeDao.class);
		inpSelWork.setModel(MasterComboModel.newWorkTypeComboBoxModel());
		
		outputPropTypeListModel_ = new OutputPropTypeListModel();
		inpSelProp.setModel(outputPropTypeListModel_);
	}

	/**
	 * This method initializes jPanel3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel3() {
		if (jPanel3 == null) {
			jPanel3 = new JPanel();
			jPanel3.setLayout(new BorderLayout());
			jPanel3.add(getInpDepth(), BorderLayout.CENTER);
		}
		return jPanel3;
	}

	/**
	 * This method initializes inpDepth	
	 * 	
	 * @return javax.swing.JSlider	
	 */
	private JSlider getInpDepth() {
		if (inpDepth == null) {
			inpDepth = new JSlider();
			inpDepth.setBorder( 
				new TitledBorder(null, "summary depth", 
						TitledBorder.LEADING, TitledBorder.TOP, 
						new Font("Dialog", Font.PLAIN, 12), new Color(51, 51, 51))
			);
			inpDepth.setMinimum(0);
			inpDepth.setMaximum(20);
			inpDepth.setMinorTickSpacing(1);
			inpDepth.setMajorTickSpacing(5);
			inpDepth.setPaintTicks(true);
			inpDepth.setPaintLabels(false);
			inpDepth.setToolTipText("0の場合は末端の階層まで集計します");
			inpDepth.setSnapToTicks(true);
			inpDepth.setValue(0);
		}
		return inpDepth;
	}

	/**
	 * This method initializes cntSummary	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntSummary() {
		if (cntSummary == null) {
			cntSummary = new JPanel();
			cntSummary.setLayout(new BorderLayout());
			cntSummary.setName("");
			cntSummary.add(getCntSplit(), BorderLayout.CENTER);
			cntSummary.add(getCntSummaryBtn(), BorderLayout.SOUTH);
		}
		return cntSummary;
	}

	/**
	 * This method initializes cntSplit	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getCntSplit() {
		if (cntSplit == null) {
			cntSplit = new JSplitPane();
			cntSplit.setResizeWeight(0.5D);
			cntSplit.setRightComponent(getCntRightSum());
			cntSplit.setLeftComponent(getCntLeftSum());
		}
		return cntSplit;
	}

	/**
	 * This method initializes cntSummaryBtn	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntSummaryBtn() {
		if (cntSummaryBtn == null) {
			cntSummaryBtn = new JPanel();
			cntSummaryBtn.setLayout(new FlowLayout());
			cntSummaryBtn.add(getCmdSummary(), null);
		}
		return cntSummaryBtn;
	}

	/**
	 * This method initializes cmdSummary	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdSummary() {
		if (cmdSummary == null) {
			cmdSummary = new JButton();
			cmdSummary.setAction(actSummary_);
		}
		return cmdSummary;
	}

	/**
	 * This method initializes cmdReverseSelWork	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdReverseSelWork() {
		if (cmdReverseSelWork == null) {
			cmdReverseSelWork = new JButton();
			cmdReverseSelWork.setAction(actReverseSelection_);
		}
		return cmdReverseSelWork;
	}

	/**
	 * This method initializes cmdReverseSelProp	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdReverseSelProp() {
		if (cmdReverseSelProp == null) {
			cmdReverseSelProp = new JButton();
			cmdReverseSelProp.setAction(actReverseSelection_);
		}
		return cmdReverseSelProp;
	}

	/**
	 * This method initializes cntLeftSum	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntLeftSum() {
		if (cntLeftSum == null) {
			cntLeftSum = new JPanel();
			cntLeftSum.setLayout(new BorderLayout());
			cntLeftSum.add(getCmdReverseSelWork(), BorderLayout.NORTH);
			cntLeftSum.add(getCntScrWork(), BorderLayout.CENTER);
		}
		return cntLeftSum;
	}

	/**
	 * This method initializes cntRightSum	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntRightSum() {
		if (cntRightSum == null) {
			cntRightSum = new JPanel();
			cntRightSum.setLayout(new BorderLayout());
			cntRightSum.add(getCmdReverseSelProp(), BorderLayout.NORTH);
			cntRightSum.add(getCntScrProp(), BorderLayout.CENTER);
		}
		return cntRightSum;
	}

	/**
	 * This method initializes inpSelWork	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getInpSelWork() {
		if (inpSelWork == null) {
			inpSelWork = new JList();
			inpSelWork.setToolTipText("ctrl+clickで複数選択が可能です");
			inpSelWork.addListSelectionListener(
					new ListSelectionListener(){
						public void valueChanged(ListSelectionEvent arg0) {
							
							outputPropTypeListModel_.setWorkType(
									inpSelWork.getSelectedValues());
						}
					}
			);
		}
		return inpSelWork;
	}

	/**
	 * This method initializes inpSelProp	
	 * 	
	 * @return javax.swing.JList	
	 */
	private JList getInpSelProp() {
		if (inpSelProp == null) {
			inpSelProp = new JList();
			inpSelProp.setCellRenderer(
					new DefaultListCellRenderer(){
						@Override
						public Component getListCellRendererComponent(JList arg0, Object prop, int arg2, boolean arg3, boolean arg4) {
							Component ret = super.getListCellRendererComponent(arg0, prop, arg2, arg3, arg4);
							((JLabel)ret).setText(((OutputPropType)prop).getOutputPropTypeName());
							return ret;
						}
					}
			);
			inpSelProp.setToolTipText("ctrl+clickで複数選択が可能です");
			
		}
		return inpSelProp;
	}

	/**
	 * This method initializes cntScrWork	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrWork() {
		if (cntScrWork == null) {
			cntScrWork = new JScrollPane();
			cntScrWork.setViewportView(getInpSelWork());
		}
		return cntScrWork;
	}

	/**
	 * This method initializes cntScrProp	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getCntScrProp() {
		if (cntScrProp == null) {
			cntScrProp = new JScrollPane();
			cntScrProp.setViewportView(getInpSelProp());
		}
		return cntScrProp;
	}
	

}  //  @jve:decl-index=0:visual-constraint="10,10"
