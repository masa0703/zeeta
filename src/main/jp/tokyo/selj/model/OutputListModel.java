package jp.tokyo.selj.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.OutputDao;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkType;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;

public class OutputListModel extends CommonListModel{
	Logger log = Logger.getLogger(this.getClass());
	
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;
	OutputDao outputDao_ = null;
	
	public enum SortOrder {ASC, DESC};
	
	static public abstract class OutputComparator implements Comparator<Output>{
		String _name;
		public OutputComparator(String name){_name = name;}
		public void setOrder(SortOrder order){_LAST_SORT_ORDER = order;}
		public String toString(){return _name;}
		public int compareString(String s1, String s2){
			s1 = (s1 == null)? "":s1;
			s2 = (s2 == null)? "":s2;
			int ret = -1;
			ret = s1.compareToIgnoreCase( s2 );
			if(_LAST_SORT_ORDER == SortOrder.DESC){
				ret = ret * -1;
			}
			return ret;
		}
		
	}
	
	static public OutputComparator ID_COMPARATOR = new  OutputComparator("ID"){
		public int compare(Output o1, Output o2) {
			int ret = -1;
			if(o1.getOutputId() == o2.getOutputId()){
				ret = 0;
			}else if(o1.getOutputId() > o2.getOutputId()){
				ret = 1;
			}
			if(_LAST_SORT_ORDER == SortOrder.DESC){
				ret = ret * -1;
			}
			return ret;
		}
	};
	static public OutputComparator TYPE_COMPARATOR = new  OutputComparator("type"){
		public int compare(Output o1, Output o2) {
			int ret = -1;
			if(o1.getOutputType().getSeq() == o2.getOutputType().getSeq()){
				ret = 0;
			}else if(o1.getOutputType().getSeq() > o2.getOutputType().getSeq()){
				ret = 1;
			}
			if(_LAST_SORT_ORDER == SortOrder.DESC){
				ret = ret * -1;
			}
			return ret;
		}
	};
	static public OutputComparator NAME_COMPARATOR = new  OutputComparator("name"){
		public int compare(Output o1, Output o2) {
			return compareString(o1.getName(), o2.getName());
		}
	};
	static public OutputComparator CREATOR_COMPARATOR = new  OutputComparator("creator"){
		public int compare(Output o1, Output o2) {
			return compareString(o1.getUserName(), o2.getUserName());
		}
	};
	static public OutputComparator PATH_COMPARATOR = new  OutputComparator("path"){
		public int compare(Output o1, Output o2) {
			return compareString(o1.getPath(), o2.getPath());
		}
	};
	static public OutputComparator DATE_COMPARATOR = new  OutputComparator("date"){
		public int compare(Output o1, Output o2) {
			int ret = -1;
			ret = o1.getNewDate().compareTo( o2.getNewDate() );
			if(_LAST_SORT_ORDER == SortOrder.DESC){
				ret = ret * -1;
			}
			return ret;
		}
	};

	static SortOrder _LAST_SORT_ORDER = SortOrder.ASC;
	static OutputComparator _Last_COMPARATOR = ID_COMPARATOR;
	
	
	
	
	public void sort(OutputComparator cmp, SortOrder order){
		_Last_COMPARATOR = cmp;
		_LAST_SORT_ORDER = order;
		Output[] outputArray = new Output[getSize()];
		for(int i=0; i<getSize(); i++){
			outputArray[i] = (Output)get(i);
		}
		clear();
		Arrays.sort(outputArray, cmp);
		setArray(outputArray);
		fireContentsChanged(this, 0, getSize()-1);
	}

	
	public OutputListModel(){
		super();
		outputDao_ = (OutputDao) daoCont_.getComponent(OutputDao.class);
	}
//	public void reloadOutputList(){
//		clear();
//		List<Output> outputs = outputDao_.findAll();
//		setList(outputs);
//	}
//	public void reloadOutputListByOutputType(int outputType){
//		clear();
//		List<Output> outputs = outputDao_.findByType(outputType);
//		setList(outputs);
//	}
	public void reloadOutputListByWorkType(int workTypeId){
		clear();
		List<Output> outputs = outputDao_.findByWorkTypeId(workTypeId);
		Output[] outputArray = new Output[outputs.size()];
		Arrays.sort(outputs.toArray(outputArray), _Last_COMPARATOR);
		setArray(outputArray);
	}
	public void reloadOutputListByNameOrPathOrCreator(String word){
		clear();
		List<Output> outputs = outputDao_.findByNameOrPathOrCreator(
				"%"+Util.cnvLikeWord(word)+"%");
		Output[] outputArray = new Output[outputs.size()];
		Arrays.sort(outputs.toArray(outputArray), _Last_COMPARATOR);
		setArray(outputArray);
	}
	public void reloadOutputListByWorkTypeAndNameOrPathOrCreator(WorkType wt, String word){
		clear();
		OutputDao.Param01 p = new OutputDao.Param01();
		p.setWorkTypeId(wt.getWorkTypeId());
		p.setWord("%"+Util.cnvLikeWord(word)+"%");
		List<Output> outputs = 
			outputDao_.findByWorkTypeAndNameOrPathOrCreator(p);
		Output[] outputArray = new Output[outputs.size()];
		Arrays.sort(outputs.toArray(outputArray), _Last_COMPARATOR);
		setArray(outputArray);
	}
	public void deleteOutput(Output output){
		outputDao_.delete(output);
		int index = searchIndex(output.getOutputId());
		remove(index);
	}
	public void updateOutput(Output output){
		outputDao_.update(output);
		outputChanged(output);
	}
	public void insertOutput(Output output){
		outputDao_.insert(output);
		addElement(output);
	}
	public void outputChanged(Output output) {
		int index = searchIndex(output.getOutputId());
		set(index, output);		//“ü‚ê‘Ö‚¦‚é
		fireContentsChanged(this, index, index);
	}
	protected int searchIndex(long outputId){
		int ret = -1;
		for(int i=0; i<size(); i++){
			Output output = (Output)get(i);
			if(output.getOutputId() == outputId){
				ret = i;
				break;
			}
		}
		return ret;
	}
}
