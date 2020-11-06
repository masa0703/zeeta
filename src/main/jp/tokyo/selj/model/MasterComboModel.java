package jp.tokyo.selj.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import jp.tokyo.selj.dao.CheckState;
import jp.tokyo.selj.dao.CheckStateDao;
import jp.tokyo.selj.dao.OutputTypeDao;
import jp.tokyo.selj.dao.ReviewStateType;
import jp.tokyo.selj.dao.ReviewStateTypeDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.SortType;
import jp.tokyo.selj.dao.SortTypeDao;
import jp.tokyo.selj.dao.UserDao;
import jp.tokyo.selj.dao.WorkTypeDao;

public class MasterComboModel {
	/*
	 *  user
	 */
	static List users_ = null;
	static Set<DefaultComboBoxModel> userModels_ = new HashSet<DefaultComboBoxModel>();
	public static void refreshUser(){
		if(users_ == null){
			users_ = new ArrayList();
		}
		UserDao userDao = (UserDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(UserDao.class);
		users_.clear();
		users_.addAll(userDao.findAll());
		refreshComboBoxModels(userModels_, users_);
	}
	public static ComboBoxModel newUserComboBoxModel(){
		if(users_ == null){
			refreshUser();
		}
		DefaultComboBoxModel ret = new DefaultComboBoxModel(users_.toArray());
		userModels_.add(ret);
		return ret;
	}

	
	/*
	 *  sortType
	 */
	static List sortTypes_ = null;
	public static ComboBoxModel newSortTypeComboBoxModel(){
		if(sortTypes_ == null){
			SortTypeDao sortTypeDao = (SortTypeDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(SortTypeDao.class);
			sortTypes_ = sortTypeDao.findAll();
			DEFAULT_SORT_TYPE = sortTypeDao.findById(0);	//デフォルト
		}
		return new DefaultComboBoxModel(sortTypes_.toArray());
	}
    public static SortType DEFAULT_SORT_TYPE = null;

    
	/*
	 *  outputType
	 */
	static List outputTypes_ = null;
	static Set<DefaultComboBoxModel> outpoutTypeModels_ = new HashSet<DefaultComboBoxModel>();
	public static void refreshOutputType(){
		if(outputTypes_ == null){
			outputTypes_ = new ArrayList();
		}
		OutputTypeDao outputTypeDao = (OutputTypeDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(OutputTypeDao.class);
		outputTypes_.clear();
		outputTypes_.addAll(outputTypeDao.findAll());
		refreshComboBoxModels(outpoutTypeModels_, outputTypes_);
	}
	public static ComboBoxModel newOutputTypeComboBoxModel(){
		if(outputTypes_ == null){
			refreshOutputType();
		}
		DefaultComboBoxModel ret = new DefaultComboBoxModel(outputTypes_.toArray());
		outpoutTypeModels_.add(ret);
		return ret;
	}
    
	/*
	 *  workType
	 */
    static List workTypes_ = null;
	static Set<DefaultComboBoxModel> workTypeModels_ = new HashSet<DefaultComboBoxModel>();
	public static void refreshWorkType(){
		if(workTypes_ == null){
			workTypes_ = new ArrayList();
		}
		WorkTypeDao workTypeDao = (WorkTypeDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(WorkTypeDao.class);
		workTypes_.clear();
		workTypes_.addAll(workTypeDao.findAll());
		refreshComboBoxModels(workTypeModels_, workTypes_);
	}
	public static ComboBoxModel newWorkTypeComboBoxModel(){
		if(workTypes_ == null){
			refreshWorkType();
		}
		DefaultComboBoxModel ret = new DefaultComboBoxModel(workTypes_.toArray());
		workTypeModels_.add(ret);
		return ret;
	}
	
	/*
	 *  checkState
	 */
	static List checkStates_ = null;
	static Set<DefaultComboBoxModel> checkStateModels_ = new HashSet<DefaultComboBoxModel>();
	public static void refreshCheckState(){
		CheckStateDao dao = (CheckStateDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(CheckStateDao.class);
		if(checkStates_ == null){
			checkStates_ = new ArrayList();
			DEFAULT_CHECK_STATE = dao.findById(0);	//デフォルト
		}
		checkStates_.clear();
		checkStates_.addAll(dao.findAll());
		refreshComboBoxModels(checkStateModels_, checkStates_);
	}
	public static ComboBoxModel newCheckStateComboBoxModel(){
		if(checkStates_ == null){
			refreshCheckState();
		}
		DefaultComboBoxModel ret = new DefaultComboBoxModel(checkStates_.toArray());
		checkStateModels_.add(ret);
		return ret;
	}
	public static CheckState DEFAULT_CHECK_STATE = null;

	/*
	 *  revieStateType
	 */
	static List revieStateTypes_ = null;
	static Set<DefaultComboBoxModel> revieStateTypeModels_ = new HashSet<DefaultComboBoxModel>();
	public static void refreshrRvieStateType(){
		ReviewStateTypeDao dao = (ReviewStateTypeDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(ReviewStateTypeDao.class);
		if(revieStateTypes_ == null){
			revieStateTypes_ = new ArrayList();
			DEFAULT_REVIEW_STATE = dao.findById(0);	//デフォルト
		}
		revieStateTypes_.clear();
		revieStateTypes_.addAll(dao.findAll());
		refreshComboBoxModels(revieStateTypeModels_, revieStateTypes_);
	}
	public static ComboBoxModel newRevieStateTypeComboBoxModel(){
		if(revieStateTypes_ == null){
			refreshrRvieStateType();
		}
		DefaultComboBoxModel ret = new DefaultComboBoxModel(revieStateTypes_.toArray());
		revieStateTypeModels_.add(ret);
		return ret;
	}
	public static ReviewStateType DEFAULT_REVIEW_STATE = null;

	
	/*
	 *  DefaultComboBoxModelの内容を入れ替える
	 */
	static void refreshComboBoxModels(Set<DefaultComboBoxModel> cbModels, List items) {
		for(Iterator<DefaultComboBoxModel> it=cbModels.iterator(); it.hasNext();){
			DefaultComboBoxModel cbModel = it.next();
			cbModel.removeAllElements();
			for(Iterator items_it=items.iterator(); items_it.hasNext();){
				cbModel.addElement(items_it.next());
			}
		}
	}
	
}
