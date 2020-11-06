package jp.tokyo.selj.view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import jp.tokyo.selj.common.TextUndoHandler;
import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.Review;
import jp.tokyo.selj.dao.ReviewDao;
import jp.tokyo.selj.dao.ReviewDetailDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.model.MasterComboModel;

import org.seasar.framework.container.S2Container;
import java.awt.BorderLayout;

public class DlgNewReview extends JDialog {

	private static final long serialVersionUID = 1L;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;  //  @jve:decl-index=0:
    protected TextUndoHandler memoUndoHandler_ = new TextUndoHandler();

	private JPanel jContentPane = null;

	private JComboBox inpAuthor = null;

	private JButton cmdNewReview = null;
	
	Output output_;  //  @jve:decl-index=0:
	ReviewDao headerDao_= null;  //  @jve:decl-index=0:
	ReviewDetailDao detailDao_= null;
	DlgReviewList dlgReviewList_  = null;
	private JComboBox inpReviewer1 = null;
	private JComboBox inpReviewer2 = null;
	private JComboBox inpReviewer3 = null;
	private JTextArea inpMemo = null;
	Review header_ = null;  //  @jve:decl-index=0:
	boolean isCreate_ = false;
	private JPanel cntUsers = null;
	
	class ActCreateReviewList extends ActTransactionBase {
		protected Component getOwnerComponent(){
			return DlgNewReview.this;
		}
		public ActCreateReviewList(){
			super();
			putValue(Action.NAME, "commit");
			putValue(Action.SHORT_DESCRIPTION, "レビューを作成・更新する");
		}
		public void actionPerformed2(ActionEvent e) {
			header_ = getReview();
			header_.check();
			
			if(isCreate_){
				headerDao_.insert(header_);
			}else{
				headerDao_.update(header_);
			}

			dlgReviewList_.refreshReviewList();
	    	memoUndoHandler_.cleanup();
			setVisible(false);
		}
	}

	/**
	 * @param owner
	 */
	public DlgNewReview(Dialog owner) {
		super(owner, true);
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(654, 278);
		this.setResizable(false);
		this.setTitle("new review");
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
			jContentPane.add(getCmdNewReview(), BorderLayout.SOUTH);
			jContentPane.add(getInpMemo(), BorderLayout.CENTER);
			jContentPane.add(getCntUsers(), BorderLayout.NORTH);
		}
		return jContentPane;
	}

	/**
	 * This method initializes inpAuthor	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpAuthor() {
		if (inpAuthor == null) {
			inpAuthor = new JComboBox();
			inpAuthor.setPreferredSize(new Dimension(200, 25));
			inpAuthor.setEditable(true);
			inpAuthor.setBorder(new TitledBorder("author"));
		}
		return inpAuthor;
	}
	public void setup(DlgReviewList dlgReviewList){
		dlgReviewList_ = dlgReviewList;
		getInpAuthor().setModel(MasterComboModel.newUserComboBoxModel());
		getInpReviewer1().setModel(MasterComboModel.newUserComboBoxModel());
		getInpReviewer2().setModel(MasterComboModel.newUserComboBoxModel());
		getInpReviewer3().setModel(MasterComboModel.newUserComboBoxModel());
		
		headerDao_ = (ReviewDao)daoCont_.getComponent(ReviewDao.class);
		detailDao_ = (ReviewDetailDao)daoCont_.getComponent(ReviewDetailDao.class);
		getCmdNewReview().setAction(new ActCreateReviewList());
		memoUndoHandler_.setup(getInpMemo());
	}
	@Override
	public void setVisible(boolean b) {
		if(b){
			throw new RuntimeException("setVisible(boolean b, Output output)をつかわなあかん");
		}else{
			super.setVisible(b);
		}
	}
	public void setVisible(boolean b, Output output) {
		isCreate_ = true;
		Review review = new Review();
		review.setReviewi(output.getUserName());	//初期値
		review.setOutputId(output.getOutputId());
		this.setVisible(b, output, review);

	}
	public void setVisible(boolean b, Output output, Review review) {
		output_ = output;
		header_ = review;
		setReview(header_);
		super.setVisible(b);
	}
	Review getReview(){
		header_.setReviewi(
				(getInpAuthor().getModel().getSelectedItem()==null)? null:
					""+getInpAuthor().getModel().getSelectedItem()
		);
		header_.setReviewer1(
				(getInpReviewer1().getModel().getSelectedItem()==null)? null:
					""+getInpReviewer1().getModel().getSelectedItem()
		);
		header_.setReviewer2(
				(getInpReviewer2().getModel().getSelectedItem()==null)? null:
					""+getInpReviewer2().getModel().getSelectedItem()
		);
		header_.setReviewer3(
				(getInpReviewer3().getModel().getSelectedItem()==null)? null:
					""+getInpReviewer3().getModel().getSelectedItem()
		);
		header_.setRemark(getInpMemo().getText());
		return header_;
	}
	void setReview(Review review){
		getInpAuthor().setSelectedItem(review.getReviewi());
		getInpReviewer1().setSelectedItem(review.getReviewer1());
		getInpReviewer2().setSelectedItem(review.getReviewer2());
		getInpReviewer3().setSelectedItem(review.getReviewer3());
		getInpMemo().setText(review.getRemark());
	}

	
	/**
	 * This method initializes cmdNewReview	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getCmdNewReview() {
		if (cmdNewReview == null) {
			cmdNewReview = new JButton();
			cmdNewReview.setMaximumSize(new Dimension(200,25));
			cmdNewReview.setText("new review");
		}
		return cmdNewReview;
	}

	/**
	 * This method initializes inpReviewer1	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpReviewer1() {
		if (inpReviewer1 == null) {
			inpReviewer1 = new JComboBox();
			inpReviewer1.setPreferredSize(new Dimension(200, 25));
			inpReviewer1.setEditable(true);
			inpReviewer1.setBorder(new TitledBorder("reviewer1"));
		}
		return inpReviewer1;
	}

	/**
	 * This method initializes inpReviewer2	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpReviewer2() {
		if (inpReviewer2 == null) {
			inpReviewer2 = new JComboBox();
			inpReviewer2.setPreferredSize(new Dimension(200, 25));
			inpReviewer2.setEditable(true);
			inpReviewer2.setBorder(new TitledBorder("reviewer2"));
		}
		return inpReviewer2;
	}

	/**
	 * This method initializes inpReviewer3	
	 * 	
	 * @return javax.swing.JComboBox	
	 */
	private JComboBox getInpReviewer3() {
		if (inpReviewer3 == null) {
			inpReviewer3 = new JComboBox();
			inpReviewer3.setPreferredSize(new Dimension(200, 25));
			inpReviewer3.setEditable(true);
			inpReviewer3.setBorder(new TitledBorder("reviewer3"));
		}
		return inpReviewer3;
	}

	/**
	 * This method initializes inpMemo	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getInpMemo() {
		if (inpMemo == null) {
			inpMemo = new JTextArea();
			inpMemo.setBorder(new TitledBorder("memo"));
		}
		return inpMemo;
	}

	/**
	 * This method initializes cntUsers	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getCntUsers() {
		if (cntUsers == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.NONE;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.weightx = 1.0;
			gridBagConstraints1.ipady = 25;
			gridBagConstraints1.gridx = 2;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = GridBagConstraints.NONE;
			gridBagConstraints4.gridx = -1;
			gridBagConstraints4.gridy = -1;
			gridBagConstraints4.ipady = 25;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.fill = GridBagConstraints.NONE;
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 1;
			gridBagConstraints3.ipady = 25;
			gridBagConstraints3.weightx = 1.0;
			gridBagConstraints3.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.fill = GridBagConstraints.NONE;
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.gridy = 1;
			gridBagConstraints2.ipady = 25;
			gridBagConstraints2.weightx = 1.0;
			gridBagConstraints2.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.NONE;
			gridBagConstraints.gridx = -1;
			gridBagConstraints.gridy = -1;
			gridBagConstraints.ipady = 25;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new Insets(0, 5, 0, 5);
			cntUsers = new JPanel();
			cntUsers.setLayout(new GridBagLayout());
			cntUsers.add(getInpAuthor(), gridBagConstraints);
			cntUsers.add(getInpReviewer1(), gridBagConstraints2);
			cntUsers.add(getInpReviewer2(), gridBagConstraints3);
			cntUsers.add(getInpReviewer3(), gridBagConstraints1);
		}
		return cntUsers;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
