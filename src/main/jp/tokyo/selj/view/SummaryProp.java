package jp.tokyo.selj.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.tokyo.selj.common.DlgWait;
import jp.tokyo.selj.dao.Doc;
import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.SelJDaoContainer;
import jp.tokyo.selj.dao.WorkProp;
import jp.tokyo.selj.dao.WorkPropDao;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.DocModel;

import org.apache.log4j.Logger;

public class SummaryProp {
	Logger log = Logger.getLogger(this.getClass());
	Object[] workTypes_ = null;
	Object[] outputPropTypes_ = null;
	DocModel docModel_;
	WorkPropDao workPropDao_ = 
		(WorkPropDao)SelJDaoContainer.SEL_DAO_CONT.getComponent(WorkPropDao.class);
	Map<String, Map<String, BigDecimal>> sumDataJitu_ = new HashMap<String, Map<String, BigDecimal>>();
	Map<String, Map<String, BigDecimal>> sumDataYotei_ = new HashMap<String, Map<String, BigDecimal>>();

	public SummaryProp(
			DocModel docModel,
			Object[] workTypes,
			Object[] outputPropTypes){
		docModel_ = docModel;
		workTypes_ = workTypes;
		outputPropTypes_ = outputPropTypes;
		
		//集計オブジェクト（Map）生成
		for(Object obj:workTypes){
			Map<String, BigDecimal> propsJitu = new HashMap<String, BigDecimal>();
			Map<String, BigDecimal> propsYotei = new HashMap<String, BigDecimal>();
			for(Object obj2: outputPropTypes_){
				OutputPropType opt = (OutputPropType)obj2;
				propsJitu.put(""+opt.getOutputPropTypeId(), new BigDecimal(0));
				propsYotei.put(""+opt.getOutputPropTypeId(), new BigDecimal(0));
			}
			WorkType wt = (WorkType)obj;
			sumDataJitu_.put(""+wt.getWorkTypeId(), propsJitu);
			sumDataYotei_.put(""+wt.getWorkTypeId(), propsYotei);
		}
	}
	public void summary(Doc doc, int depth, DlgWait wd){
		class Summary implements DocModel.DocProcessor{
			int depth_;
			DlgWait wd_;
			long docCount_ = 0;
			Set<Long> calcDocIds = new HashSet<Long>();
//			SimpleDateFormat formatter_ = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			public Summary(int depth, DlgWait wd){
				depth_ = depth;
				wd_ = wd;
			}
			public boolean process(Doc doc, List<Doc> parents) {
				if(depth_ > 0 && parents.size() > depth_){
					return false;
				}
				//一度集計したデータは、再度修正しないこと！
				if(calcDocIds.contains(doc.getDocId())){
					return false;
				}
				if( wd_ != null){
					wd_.setMsg("process node count = "+docCount_++);
				}
				
				calcDocIds.add(doc.getDocId());
				for(Object obj:workTypes_){
					WorkType wt = (WorkType)obj;
					//docに関連する作業属性を抽出
					List<WorkProp> wkProps = 
						workPropDao_.findProps(doc.getDocId(), wt.getWorkTypeId());
					for(WorkProp wp:wkProps){
						for(Object obj2:outputPropTypes_){
							OutputPropType opt = (OutputPropType)obj2;
							if(opt.getOutputPropTypeId() == wp.getOutputPropTypeId()){
								
								
								//集計
								String wtId = "" + wp.getWorkTypeId();
								String optId = "" + opt.getOutputPropTypeId();
								Map<String, BigDecimal> opts = null;
								if(wp.isJissekiFlg()){
									opts = sumDataJitu_.get(wtId);
								}else{
									opts = sumDataYotei_.get(wtId);
								}
								BigDecimal sum = opts.get(optId).add(wp.getValue());
								opts.put(optId, sum);
							}
						}
					}
				}				
				return true;	//続行
			}
		}
		Summary proc = new Summary(depth, wd);
		docModel_.processAllDoc2(doc, proc, new ArrayList<Doc>());
	

	}
	public Map<String, Map<String, BigDecimal>> getSumDataJitu() {
		return sumDataJitu_;
	}
	public Map<String, Map<String, BigDecimal>> getSumDataYotei() {
		return sumDataYotei_;
	}
}
