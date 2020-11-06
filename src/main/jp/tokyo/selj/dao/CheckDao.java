 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = Check.class)
public interface CheckDao {

    @Query( "outputId = ? order by checkDate DESC")
    public List<Check> findByOutputId(long outputId);

    public void remove(Check check);
    
    public void insert(Check check);

    public void update(Check check);
}
