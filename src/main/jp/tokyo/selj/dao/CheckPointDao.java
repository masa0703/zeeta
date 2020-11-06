 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = CheckPoint.class)
public interface CheckPointDao {

    @Query( "checkPoint.outputTypeId=? and deleteFlg=false order by SEQ")
    public List<CheckPoint> findByOutputTypeId(long outputTypeId);
    
    @Arguments({"checkPointId"})
    public CheckPoint findById(long id);
    
    public void insert(CheckPoint checkPoint);
    public void update(CheckPoint checkPoint);
    
    @Sql("delete from checkPoint where checkPointId=?")
    public void deleteById(long id);
}
