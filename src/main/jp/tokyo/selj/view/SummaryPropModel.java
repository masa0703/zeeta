package jp.tokyo.selj.view;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import jp.tokyo.selj.dao.OutputPropType;
import jp.tokyo.selj.dao.WorkType;
import jp.tokyo.selj.model.DocModel;

import org.apache.log4j.Logger;

public class SummaryPropModel extends SummaryProp implements TableModel{
	Logger log = Logger.getLogger(this.getClass());
	List<TableModelListener> listeners_ = new ArrayList<TableModelListener>();
	boolean sumPlan_ = false;
	
	public SummaryPropModel(
			DocModel docModel,
			Object[] workTypes,
			Object[] outputPropTypes){
		super(docModel, workTypes, outputPropTypes);
	}
	public void addTableModelListener(TableModelListener arg0) {
		listeners_.add(arg0);
	}
	public void setSumPlan(boolean val){
		if( sumPlan_ != val){
			sumPlan_ = val;
			refleshListeners();
		}
	}

	private void refleshListeners() {
		TableModelEvent e = new TableModelEvent(this);
		for(TableModelListener l:listeners_){
			l.tableChanged(e);
		}
	}

	public Class<?> getColumnClass(int arg0) {

		return String.class;
	}

	public int getColumnCount() {
		return 2 + workTypes_.length;		// 2 + 作業種類
	}
	public int getRowCount() {
		return outputPropTypes_.length + 1;
	}


	public String getColumnName(int col) {
		String ret = null;
		switch(col){
		case 0:		//作業属性
			ret = "propType";
			break;
		case 1:		//合計
			ret = "summary";
			break;
		default:
			int workTypeIndex = col - 2;
			WorkType wt = (WorkType)workTypes_[workTypeIndex];
			ret = wt.getWorkTypeName();
			if(sumPlan_){
				ret = ret + "(plan)"; 
			}
			break;
		}
		return ret;
	}

	public Object getValueAt(int row, int col) {
//		log.debug("row = " + row + ", col = "+col);
		BigDecimal sum;
		Object ret = null;
		switch(col){
		case 0:		//作業属性
			//縦合計
			if(outputPropTypes_.length <= row){
				ret = "summary";
			}else{
				ret = ((OutputPropType)outputPropTypes_[row]).getOutputPropTypeName();
			}
			break;
		case 1:		//横合計
			if(outputPropTypes_.length <= row){
				return null;
			}
			sum = new BigDecimal(0); 
			for(int i=0; i < workTypes_.length; i++){
				sum = sum.add(getWorkPropValue(sumDataJitu_, row, i + 2));
			}
			ret = sum;
			if(sumPlan_){
				sum = new BigDecimal(0); 
				for(int i=0; i < workTypes_.length; i++){
					sum = sum.add(getWorkPropValue(sumDataYotei_, row, i + 2));
				}
				ret = ret + "(" +sum + ")";
			}
			break;
		default:
			//縦合計
			if(outputPropTypes_.length <= row){
				sum = new BigDecimal(0); 
				for(int i=0; i < row; i++){
					sum = sum.add(getWorkPropValue(sumDataJitu_, i, col));
				}
				ret = sum;
				if(sumPlan_){
					sum = new BigDecimal(0); 
					for(int i=0; i < row; i++){
						sum = sum.add(getWorkPropValue(sumDataYotei_, i, col));
					}
					ret = ret + "(" +sum + ")";
				}
			}else{
				ret = getWorkPropValue(sumDataJitu_, row, col); 
				if(sumPlan_){
					ret = ret + "(" +
								getWorkPropValue(sumDataYotei_, row, col)
							+ ")"; 
				}
			}
			break;
		}
		return ret;
	}
	BigDecimal getWorkPropValue(Map<String, Map<String, BigDecimal>> values, 
			int row, int col){
		int workTypeIndex = col - 2;
		WorkType wt = (WorkType)workTypes_[workTypeIndex];
		OutputPropType opt = (OutputPropType)outputPropTypes_[row];
		Map<String, BigDecimal> opts = values.get(""+wt.getWorkTypeId());
		return opts.get(""+opt.getOutputPropTypeId()); 
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {
		listeners_.remove(arg0);
	}

	public void setValueAt(Object arg0, int arg1, int arg2) {
		throw new RuntimeException("setValueAt() is unsupported."); 
	}
}
