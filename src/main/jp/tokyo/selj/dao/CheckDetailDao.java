 package jp.tokyo.selj.dao;

import java.sql.Timestamp;
import java.util.List;

import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = CheckDetail.class)
public interface CheckDetailDao {

    @Query( "outputId = ? and checkDate = ? order by checkPoint.SEQ")
    public List<CheckDetail> findByOutputIdDate(long outputId, Timestamp date);
    
    public void update(CheckDetail checkDetail);

    public void insert(CheckDetail checkDetail);

}
