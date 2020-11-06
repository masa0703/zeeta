package jp.tokyo.selj.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.XADataSource;
import javax.swing.table.AbstractTableModel;

import jp.tokyo.selj.common.DateTextFormatter;
import jp.tokyo.selj.dao.SelJDaoContainer;

import org.apache.log4j.Logger;
import org.seasar.framework.container.S2Container;


/**
 * 
 * マスタメンテのJTable用のModel
 *
 * @author 
 */
public abstract class DBTableModel extends AbstractTableModel {
	Logger log = Logger.getLogger(this.getClass()); 

	// names of colums --- will get them from DB table
	String[] columnNames_ = new String[0];
	int[] columnTypes_ = new int[0];

	// this Vector will keep rows of table
	List<List<Object>> rows_ = new ArrayList<List<Object>>();
	int versionNo_index_ = -1;
	S2Container daoCont_ = SelJDaoContainer.SEL_DAO_CONT;

	public String getWhereOfUpdateSql(int row){
		throw new RuntimeException("I'm not using update function.");
	}
	public abstract String getQuerySql();
	public abstract String getTableName();
	
	static final String VERSION_NO_NAME = "versionNo";
	DateTextFormatter dateTextFormatter_ = new DateTextFormatter();

	Connection getConnection() throws SQLException{
		XADataSource ds = (XADataSource)daoCont_.getComponent("xaDataSource");
		return ds.getXAConnection().getConnection();
	}
	
	public Object getValueAt(int row, String columnName){
		int index = getColumnIndex(columnName);
		if(index == -1){
			throw new RuntimeException(columnName + "は、読み込んでいません");
		}
		return getValueAt(row, index);
	}
	public int getColumnIndex(String columnName){
		int index = -1;
		for(int i=0; i<columnNames_.length; i++){
			if(columnName.equalsIgnoreCase(columnNames_[i])){
				index = i;
				break;
			}
		}
		return index;
	}
	
	public int getVersionNoColumnIndex(){
		return versionNo_index_;
	}
	public void clear(){
		rows_.clear();
		fireTableChanged(null);
	}
	
	/** method sends SELECT query and parses result of this query */
	public void executeQuery() {
		executeQuery((XADataSource)SelJDaoContainer.SEL_DAO_CONT.getComponent("xaDataSource"));
	}
	public void executeQuery(XADataSource ds) {
		String query = getQuerySql();
		// let’s parse result of query
		versionNo_index_ = -1;
		Connection con = null;
		try {
			con = ds.getXAConnection().getConnection();
			Statement statement = con.createStatement();
			
			ResultSet resultSet = statement.executeQuery(query);
			ResultSetMetaData metaData = resultSet.getMetaData();
			int numberOfColumns = metaData.getColumnCount();
			columnNames_ = new String[numberOfColumns];
			columnTypes_ = new int[numberOfColumns];
			for (int i = 0; i < numberOfColumns; i++) {
				columnNames_[i] = metaData.getColumnLabel(i + 1);
				columnTypes_[i] = metaData.getColumnType(i + 1);
				log.debug(columnNames_[i]+":"+columnTypes_[i]);
				if(VERSION_NO_NAME.equalsIgnoreCase(columnNames_[i])){
					versionNo_index_ = i;
				}
			}
			
			Object testnulledobj;
			rows_ = new ArrayList<List<Object>>();
			// from the beginning of resultSet up to the END
			while (resultSet.next()) {
				// each row of DB table is Vector
				List<Object> newRow = new ArrayList<Object>();
				for (int i = 1; i <= getColumnCount(); i++) {
					testnulledobj = tbRepresentation(i-1,resultSet.getObject(i));
					newRow.add(testnulledobj);
				}
				rows_.add(newRow);
			}
			fireTableChanged(null);
			
		}catch (SQLException ex) {
			throw new RuntimeException(ex);
		}catch (Exception ex) {
			throw new RuntimeException(ex);
		}finally{
			if(con != null){
				try{
					con.close();
				}catch(Exception e){
					//このエラーは無視
				}
			}
		}
	}

	/** set column name in JTable */
	public String getColumnName(int column) {
		return this.columnNames_[column];
	}

	/** get quantity of rows in table */
	public int getRowCount() {
		return this.rows_.size();
	}

	/** get quantity of columns in table */
	public int getColumnCount() {
		return this.columnNames_.length;
	}

	/** get value from JTable cell */
	public Object getValueAt(int aRow, int aColumn) {
		List row = rows_.get(aRow);
		return row.get(aColumn);
	}

	/**
	 * convert SQL (MySQL in this case) datatypes into datatypes which java
	 * accepts
	 * @throws ParseException 
	 */
	Object tbRepresentation(int column, Object value)
			throws NumberFormatException, ParseException {
		if (value == null) {
			return "null";
		}
		switch (columnTypes_[column]) {
		case Types.TIMESTAMP:
		case Types.DATE:
			return dateTextFormatter_.valueToString(value);
		case Types.BIGINT:
			return new Long(value.toString());
		case Types.TINYINT:
		case Types.SMALLINT:
		case Types.INTEGER:
			return new Integer(value.toString());
		case Types.REAL:
		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.DECIMAL:
			return new Double(value.toString());
		case Types.CHAR:
		case Types.BLOB:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return new String(value.toString());
		default:
			return new String(value.toString());
		}
	}

	@Override
	public final boolean isCellEditable(int row,int column){
		return false;
//		if(	column ==getVersionNoColumnIndex() ){
//			return false;
//		}else{
//			return true;
//		}
	}

}

