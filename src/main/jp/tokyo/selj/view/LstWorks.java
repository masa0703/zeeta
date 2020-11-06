package jp.tokyo.selj.view;

import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.model.DocNode;
import jp.tokyo.selj.model.OutputOfWorkListModel;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class LstWorks extends JList implements TreeSelectionListener{
	Logger log = Logger.getLogger(this.getClass());
	public static final String DOUBLE_CLICK_ACTION_KEY = "double click action"; 

	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	private JPopupMenu mnuWorkListPopup = null;  //  @jve:decl-index=0:visual-constraint="110,13"
	
	private class OutputListCellRenderer extends PnlOutputOfWork implements ListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Work work = (Work)value;
			Output output = work.getOutput();
			dspWorkType.setText(work.getWorkType().getWorkTypeName());
			dspOutputId.setText(""+output.getOutputId());
			dspName.setText(output.getName());
			dspPath.setText(output.getPath());
			dspPointer.setText(work.getPointer());
			dspType.setText(output.getOutputType().getOutputTypeName());

			this.setOpaque(isSelected);		//不透明にすることで選択状態を表す。（透明だと白くなる）
			return this;
		}

	}
	class MyMouseListener extends MouseInputAdapter {
		public void mouseClicked(java.awt.event.MouseEvent e) {
    		if(e.getClickCount() == 2){
    			Action action = getActionMap().get(DOUBLE_CLICK_ACTION_KEY);
    			if(action != null){
    				action.actionPerformed(null);
    			}
    		}
    	}
    	public void mousePressed(java.awt.event.MouseEvent e) {
			if(e.getButton() == MouseEvent.BUTTON3){	//右ボタン
				getMnuWorkListPopup().show(LstWorks.this, e.getX(), e.getY());
			}
    	}
	}
	
	/**
	 * This method initializes this
	 * 
	 */
	private void initialize() {
		this.setCellRenderer(new OutputListCellRenderer());
        this.addMouseListener(new MyMouseListener());
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	//-----------------------------------------------------------
	public void valueChanged(TreeSelectionEvent e) {
		DocNode node = (DocNode)e.getPath().getLastPathComponent();
		long id = node.getDoc().getDocId();
		
		if( getModel() instanceof OutputOfWorkListModel){
			OutputOfWorkListModel model = (OutputOfWorkListModel)getModel();
			model.reloadWorkList(id);
			if( model.size() > 0){
				setSelectedIndex(0);
			}else{
				fireSelectionValueChanged(-1, -1, false);
			}
		}
	}

	public LstWorks(){
		super();
		initialize();
	}
	public JPopupMenu getPopupMenu(){
		return getMnuWorkListPopup();
	}

	public void setup() {
		//何もすることがなくなったけど、一応残しておく
	}

	/**
	 * This method initializes mnuWorkList	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getMnuWorkListPopup() {
		if (mnuWorkListPopup == null) {
			mnuWorkListPopup = new JPopupMenu();
		}
		return mnuWorkListPopup;
	}
}
