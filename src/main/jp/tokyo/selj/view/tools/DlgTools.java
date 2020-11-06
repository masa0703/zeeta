package jp.tokyo.selj.view.tools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.SysZeetaManager;
import jp.tokyo.selj.ZeetaDBManager;
import jp.tokyo.selj.common.SysPreference;
import jp.tokyo.selj.dao.SysZeeta;
import jp.tokyo.selj.model.MasterComboModel;
import jp.tokyo.selj.view.ActBase;
import jp.tokyo.selj.view.ActTransactionBase;

import org.apache.log4j.Logger;

public class DlgTools extends JDialog {
	static Logger log_ = Logger.getLogger(DlgTools.class);

	private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;

	private JTabbedPane jTabbedPane = null;

	private JPanel cntMasterMaint = null;

	private JButton cmdShowUserMaint = null; 
	private JButton cmdShowWorkTypeOutputType = null;

	private JPanel cntPreference = null;

	private JPanel pnlDefUserName = null;

	private JCheckBox inpUseDefUser = null;

	private JComboBox inpUserName = null;

	private JButton cmdApply = null;

	private JPanel cntInfo = null;

	private JScrollPane dspInfoScr = null;

	private JTable dspInfo = null;
	
	private static String[][]  ZEETA_INFO;

	private JButton cmdShowCheckPoint = null;

	private JButton cmdShowReviewStateType = null;

	private JPanel jPanel1 = null;

	private JPanel pnlLinkNode = null;

	private JLabel dspLinkNode = null;

	private JTextField inpLinkNodePref = null;

	private JButton cmdApplyLinkNode = null;

	private JCheckBox inpUseLinkNodePref = null;

	private JLabel dspLinkNode2 = null;

	static {
		ZEETA_INFO = new String[][]{
				{"author", "タンジ"},
				{"home page", "http://sites.google.com/site/zeetahp/"},
				{"mail address", "zeeta0703@gmail.com"},
				{"database schema version", ZeetaDBManager.DB_VERSION},
		};
	}
	
	private class ActRefreshAllLinkNode extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgTools.this;
		}

		@Override
		protected void actionPerformed2(ActionEvent e) {
			// s2daoではcase文の?はうまく置換してくれないので仕方ないのでこうするのだ
	    	final String sql =
	    		"update doc set doctypeid = " +
	    		"case   when doctypeid = 1 then doctypeid" +
	    		"	when doctitle like "+ 
	    			"'" + SysZeetaManager.getSysZeeta().getLinkNodePref() + "%'" + " then 2" +
	    		" 	else  0" +
	    		"	end";
			Connection con = ZeetaDBManager.getConnection();
			
			try {
				con.createStatement().execute(sql);
			} catch (SQLException e1) {
				throw new RuntimeException(e1);
			}
			
		}
		@Override
		protected void postProc(){
			super.postProc();
			JOptionPane.showMessageDialog(
					DlgTools.this
					,"" +
					 "全てのノードを変更しました。\n" +
					 "表示をリフレッシュするためにルートノードを選択しF5キーを押してください。"
					,""
					,JOptionPane.INFORMATION_MESSAGE);
		}
	}
	ActRefreshAllLinkNode actRefreshAllLinkNode_ = new ActRefreshAllLinkNode();

	private class ActShowDlgWorkTypeOutputType extends ActBase {
		DlgWorkTypeOutputType view_=null;
		ActShowDlgWorkTypeOutputType(){
			putValue(Action.NAME, "workType & outputType");
			putValue(Action.SHORT_DESCRIPTION, "作業種類と成果物種類tableのメンテナンス");
//			putValue(Action.SMALL_ICON, 
//					new ImageIcon(getClass().getResource("/image/moveDown.gif")));
		}
		
		@Override
		protected Component getOwnerComponent() {
			return DlgTools.this;
		}

		@Override
		protected void actionPerformed2(ActionEvent e) {
			if(view_ == null){
				view_ = new DlgWorkTypeOutputType(DlgTools.this);
				view_.setup();
				view_.setLocationRelativeTo(DlgTools.this);
			}
			view_.loadWorkTypeTable(); 
			view_.setVisible(true);
		}
	}
	class ActShowDlgUserMaint extends ActBase {
		DlgUserMaint view_=null;
		ActShowDlgUserMaint(){
			putValue(Action.NAME, "user table");
			putValue(Action.SHORT_DESCRIPTION, "ユーザtableのメンテナンス");
//			putValue(Action.SMALL_ICON, 
//					new ImageIcon(getClass().getResource("/image/moveDown.gif")));
		}
		
		@Override
		protected Component getOwnerComponent() {
			return DlgTools.this;
		}

		@Override
		protected void actionPerformed2(ActionEvent e) {
			if(view_ == null){
				view_ = new DlgUserMaint(DlgTools.this);
				view_.setup();
				view_.setLocationRelativeTo(DlgTools.this);
			}
			view_.loadUsertblTable(); 
			view_.setVisible(true);
		}
	}
	private class ActShowCheckPointMaint extends ActBase {
		DlgCheckPointAndPropTypeMaint view_=null;
		ActShowCheckPointMaint(){
			putValue(Action.NAME, "checkPoint & output property type");
			putValue(Action.SHORT_DESCRIPTION, "チェック項目と成果物属性tableのメンテナンス");
//			putValue(Action.SMALL_ICON, 
//					new ImageIcon(getClass().getResource("/image/moveDown.gif")));
		}
		
		@Override
		protected Component getOwnerComponent() {
			return DlgTools.this;
		}

		@Override
		protected void actionPerformed2(ActionEvent e) {
			if(view_ == null){
				view_ = new DlgCheckPointAndPropTypeMaint(DlgTools.this);
				view_.setup();
				view_.setLocationRelativeTo(DlgTools.this);
			}
			view_.loadOutputTypeTable(); 
			view_.refreshOutputPropTypeTable();
			view_.setVisible(true);
		}
	}
	class ActShowReviewStateTypeMaint extends ActBase {
		DlgReviewStateTypeMaint view_=null;
		ActShowReviewStateTypeMaint(){
			putValue(Action.NAME, "reviewStateType");
			putValue(Action.SHORT_DESCRIPTION, "レビュー状態種類tableのメンテナンス");
//			putValue(Action.SMALL_ICON, 
//					new ImageIcon(getClass().getResource("/image/moveDown.gif")));
		}
		
		@Override
		protected Component getOwnerComponent() {
			return DlgTools.this;
		}

		@Override
		protected void actionPerformed2(ActionEvent e) {
			if(view_ == null){
				view_ = new DlgReviewStateTypeMaint(DlgTools.this);
				view_.setup();
				view_.setLocationRelativeTo(DlgTools.this);
			}
			view_.loadReviewStateTypeTable(); 
			view_.setVisible(true);
		}
	}

	/**
	 * @param owner
	 */
	public DlgTools(Frame owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(455, 274);
		this.setTitle("tools");
		this.setContentPane(getJContentPane());
		
		getCmdShowWorkTypeOutputType().setAction(new ActShowDlgWorkTypeOutputType());
		getCmdShowUserMaint().setAction(new ActShowDlgUserMaint());
		getCmdShowCheckPoint().setAction(new ActShowCheckPointMaint());
		getCmdShowReviewStateType().setAction(new ActShowReviewStateTypeMaint());

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
			jTabbedPane.addTab("Master Maint.", null, getCntMasterMaint(), null);
			jTabbedPane.addTab("Preference", null, getCntPreference(), null);
			jTabbedPane.addTab("Info", null, getCntInfo(), null);
		}
		return jTabbedPane;
	}

	/**
	 * This method initializes cntMasterMaint	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntMasterMaint() {
		if (cntMasterMaint == null) {
			cntMasterMaint = new JPanel();
			cntMasterMaint.setLayout(new FlowLayout());
			cntMasterMaint.add(getCmdShowUserMaint(), null);
			cntMasterMaint.add(getCmdShowWorkTypeOutputType(), null);
			cntMasterMaint.add(getCmdShowCheckPoint(), null);
			cntMasterMaint.add(getCmdShowReviewStateType(), null);
			cntMasterMaint.add(getJPanel1(), null);
		}
		return cntMasterMaint;
	}

	/**
	 * This method initializes cmdShowWorkTypeOutputType	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowWorkTypeOutputType() {
		if (cmdShowWorkTypeOutputType == null) {
			cmdShowWorkTypeOutputType = new JButton();
			cmdShowWorkTypeOutputType.setText("workType & outputType");
		}
		return cmdShowWorkTypeOutputType;
	}

	/**
	 * This method initializes cmdShowUserMaint	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowUserMaint() {
		if (cmdShowUserMaint == null) {
			cmdShowUserMaint = new JButton();
			cmdShowUserMaint.setText("user");
		}
		return cmdShowUserMaint;
	}

	/**
	 * This method initializes cntPreference	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntPreference() {
		if (cntPreference == null) {
			cntPreference = new JPanel();
			cntPreference.setLayout(new BoxLayout(getCntPreference(), BoxLayout.Y_AXIS));
			cntPreference.add(getPnlDefUserName(), null);
			cntPreference.add(getPnlLinkNode(), null);
		}
		return cntPreference;
	}

	/**
	 * This method initializes pnlDefUserName	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlDefUserName() {
		if (pnlDefUserName == null) {
			pnlDefUserName = new JPanel();
			pnlDefUserName.setLayout(new FlowLayout());
			pnlDefUserName.add(getInpUseDefUser(), null);
			pnlDefUserName.setBorder(new TitledBorder("default user name"));
			pnlDefUserName.add(getInpUserName(), null);
			pnlDefUserName.add(getCmdApply(), null);
		}
		return pnlDefUserName;
	}

	/**
	 * This method initializes inpUseDefUser	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpUseDefUser() {
		if (inpUseDefUser == null) {
			inpUseDefUser = new JCheckBox();
			inpUseDefUser.setText("use");
			inpUseDefUser.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					getInpUserName().setEnabled(getInpUseDefUser().isSelected());
				}
			});
		}
		return inpUseDefUser;
	}

	/**
	 * This method initializes inpUserName	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpUserName() {
		if (inpUserName == null) {
			inpUserName = new JComboBox();
			inpUserName.setEnabled(false);
			inpUserName.setEditable(true);
			inpUserName.setPreferredSize(new Dimension(200, 25));
		}
		return inpUserName;
	}
	public void setVisible(boolean b){
		if(b){
			// default user name
			String userName = SysPreference.getDefaultUserName();
			if( userName != null){
				getInpUseDefUser().setSelected(true);
				getInpUserName().getModel().setSelectedItem(userName);
			}else{
				getInpUseDefUser().setSelected(false);
			}
			
			// link node pref
			SysZeeta sysZeeta = SysZeetaManager.getSysZeeta();
			String pref = sysZeeta.getLinkNodePref();
			if(pref != null && pref.trim().length() > 0){
				getInpUseLinkNodePref().setSelected(true);
				getInpLinkNodePref().setText(pref);
			}else{
				getInpLinkNodePref().setText("");
				getInpUseLinkNodePref().setSelected(false);
			}
		}
		super.setVisible(b);
	}
	public void setup(){
		getInpUserName().setModel(MasterComboModel.newUserComboBoxModel());
	}

	/**
	 * This method initializes cmdApply	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdApply() {
		if (cmdApply == null) {
			cmdApply = new JButton();
			cmdApply.setText("apply");
			cmdApply.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
	            	if(getInpUseDefUser().isSelected() 
	            			&& getInpUserName().getModel().getSelectedItem() != null){
	            		SysPreference.putDefaultUserName(
//	            				((User)getInpUserName().getSelectedItem()).getUserName());
	            				"" + getInpUserName().getModel().getSelectedItem());
	            	}else{
	            		SysPreference.putDefaultUserName(null);
	            	}
					JOptionPane.showMessageDialog(
							DlgTools.this
							,"設定しました。",""
							,JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}
		return cmdApply;
	}

	/**
	 * This method initializes cntInfo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntInfo() {
		if (cntInfo == null) {
			cntInfo = new JPanel();
			cntInfo.setLayout(new BorderLayout());
			cntInfo.add(getDspInfoScr(), BorderLayout.CENTER);
		}
		return cntInfo;
	}

	/**
	 * This method initializes dspInfoScr	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getDspInfoScr() {
		if (dspInfoScr == null) {
			dspInfoScr = new JScrollPane();
			dspInfoScr.setViewportView(getDspInfo());
		}
		return dspInfoScr;
	}

	/**
	 * This method initializes dspInfo	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getDspInfo() {
		if (dspInfo == null) {
			dspInfo = new JTable(ZEETA_INFO, new String[]{"name", "value"});
			dspInfo.setEnabled(false);
		}
		return dspInfo;
	}

	/**
	 * This method initializes cmdShowCheckPoint	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowCheckPoint() {
		if (cmdShowCheckPoint == null) {
			cmdShowCheckPoint = new JButton();
			cmdShowCheckPoint.setText("checkPoint");
		}
		return cmdShowCheckPoint;
	}

	/**
	 * This method initializes cmdShowReviewStateType	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdShowReviewStateType() {
		if (cmdShowReviewStateType == null) {
			cmdShowReviewStateType = new JButton();
			cmdShowReviewStateType.setText("reviewStateType");
		}
		return cmdShowReviewStateType;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			jPanel1 = new JPanel();
			jPanel1.setLayout(new GridBagLayout());
		}
		return jPanel1;
	}

	/**
	 * This method initializes pnlLinkNode	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPnlLinkNode() {
		if (pnlLinkNode == null) {
			dspLinkNode2 = new JLabel();
			dspLinkNode2.setText("この設定は、マスタDBに保存されます。");
			dspLinkNode2.setFont(new Font("Dialog", Font.PLAIN, 12));
			pnlLinkNode = new JPanel();
			pnlLinkNode.setBorder(new TitledBorder("title prefix for \"link node\""));
			pnlLinkNode.setLayout(new FlowLayout());
			pnlLinkNode.setPreferredSize(new Dimension(516, 100));
			dspLinkNode = new JLabel();
			dspLinkNode.setText("Titleの先頭をこの文字列にするとlink nodeとみなします。");
			dspLinkNode.setFont(new Font("Dialog", Font.PLAIN, 12));
			pnlLinkNode.add(getInpUseLinkNodePref(), null);
			pnlLinkNode.add(getInpLinkNodePref(), null);
			pnlLinkNode.add(getCmdApplyLinkNode(), null);
			pnlLinkNode.add(dspLinkNode);
			pnlLinkNode.add(dspLinkNode2, null);
		}
		return pnlLinkNode;
	}

	/**
	 * This method initializes inpLinkNodePref	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getInpLinkNodePref() {
		if (inpLinkNodePref == null) {
			inpLinkNodePref = new JTextField();
			inpLinkNodePref.setPreferredSize(new Dimension(100, 20));
			inpLinkNodePref.setEditable(false);
		}
		return inpLinkNodePref;
	}

	/**
	 * This method initializes cmdApplyLinkNode	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdApplyLinkNode() {
		if (cmdApplyLinkNode == null) {
			cmdApplyLinkNode = new JButton();
			cmdApplyLinkNode.setText("apply");
			cmdApplyLinkNode.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					String inp_pref = getInpLinkNodePref().getText();
					SysZeeta sysZeeta = SysZeetaManager.getSysZeeta();
	            	if(getInpUseLinkNodePref().isSelected()){
	            		if(inp_pref == null || inp_pref.trim().length() <= 0){
	    					JOptionPane.showMessageDialog(
	    							DlgTools.this
	    							,"スペース以外の文字列を入力してください。",""
	    							,JOptionPane.INFORMATION_MESSAGE);
	    					return;
	            		}
	            	}else{
	            		inp_pref = null;
	            	}
            		sysZeeta.setLinkNodePref(inp_pref);
            		SysZeetaManager.save();

					JOptionPane.showMessageDialog(
							DlgTools.this
							,"設定しました。",""
							,JOptionPane.INFORMATION_MESSAGE);
					
					
					if(inp_pref != null){
	    				if( JOptionPane.showConfirmDialog(
	    						DlgTools.this
	    						,"この設定で全てのノードの「link node」を再設定しますか？\n" +
	    						 "（この処理を行う前にDBをバックアップすることをお勧めします。\n" +
	    						 "　H2を使用している場合は、dbフォルダをどこかへコピーします）"
	    						,""
	    						,JOptionPane.YES_NO_OPTION
	    						,JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
	    					
	    					actRefreshAllLinkNode_.actionPerformed(null);
	    				}
					}
				}
			});
		}
		return cmdApplyLinkNode;
	}

	/**
	 * This method initializes inpUseLinkNodePref	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getInpUseLinkNodePref() {
		if (inpUseLinkNodePref == null) {
			inpUseLinkNodePref = new JCheckBox();
			inpUseLinkNodePref.setText("use");
			inpUseLinkNodePref.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(java.awt.event.ItemEvent e) {
					getInpLinkNodePref().setEditable(inpUseLinkNodePref.isSelected());
				}
			});
		}
		return inpUseLinkNodePref;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
