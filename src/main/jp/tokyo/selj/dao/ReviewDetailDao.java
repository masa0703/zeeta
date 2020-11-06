 package jp.tokyo.selj.dao;

import java.sql.Timestamp;
import java.util.List;

import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = ReviewDetail.class)
public interface ReviewDetailDao {

    @Query( "outputId = ? and reviewDate = ? order by SEQ")
    public List<ReviewDetail> findByOutputIdDate(long outputId, Timestamp date);
    
    public void update(ReviewDetail detail);

    public void insert(ReviewDetail detail);

    public void remove(ReviewDetail detail);
}
