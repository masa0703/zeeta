package jp.tokyo.selj.model;

import java.util.List;

import jp.tokyo.selj.dao.Output;
import jp.tokyo.selj.dao.Work;
import jp.tokyo.selj.dao.WorkDao;
import jp.tokyo.selj.dao.WorkType;

import org.apache.log4j.Logger;

public class OutputOfWorkListModel extends OutputListModel{
	Logger log = Logger.getLogger(this.getClass());
	
	WorkDao workDao_ = null;

	public OutputOfWorkListModel(){
		super();
		workDao_ = (WorkDao) daoCont_.getComponent(WorkDao.class);
	}
	public void reloadWorkList(long docId){
		clear();
		List<Work> works = workDao_.findWorks(docId);
		for(int i=0; i<works.size(); i++){
			Work work =works.get(i); 
			Output output = outputDao_.findById(work.getOutputId());
			if(output == null){
				throw new RuntimeException("outputId="
						+works.get(i).getOutputId()+"‚ª‘¶Ý‚µ‚Ü‚¹‚ñB"
						+"ŽŸ‚ÌworkƒŒƒR[ƒh‚ðíœ‚µ‚Ä‚­‚¾‚³‚¢>"+works.get(i));
			}
			work.setOutput(output);
			addElement(work);
		}
	}
	public void updateWork(Work work){
		workDao_.update(work);
	}
	public void outputChanged(Output output) {
		int index = searchIndex(output.getOutputId());
		
		Work work = (Work)get(index);
		work.setOutput(output);
		fireContentsChanged(this, index, index);
	}
	public boolean isWorkExist(long docId, int workTypeId){
		return workDao_.findWork(docId, workTypeId) != null;
	}
	public void addWork(long docId, Output output, String worker, WorkType workType){
		Work work = new Work();
		work.setDocId(docId);
		work.setOutput(output);
		work.setUserName(worker);
		work.setWorkType(workType);
		//DB‚É“o˜^
		workDao_.insert(work);
		
		addElementSeq(work);
	}
	protected void addElementSeq(Work work){
		for(int i=0; i<getSize(); i++){
			Work w = (Work)get(i);
			if(work.getWorkType().getSeq() < w.getWorkType().getSeq()){
				add(i, work);
				work = null;
				break;
			}
		}
		if( work != null ){
			addElement(work);
		}
	}
	
	
	protected int searchIndex(long outputId){
		int ret = -1;
		for(int i=0; i<size(); i++){
			Work work = (Work)get(i);
			if(work.getOutput().getOutputId() == outputId){
				ret = i;
				break;
			}
		}
		return ret;
	}
	public void removeWork(Work work) {
		removeElement(work);
		workDao_.removeWork(work);
	}
}
