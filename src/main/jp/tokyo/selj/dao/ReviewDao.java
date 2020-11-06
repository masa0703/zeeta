 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = Review.class)
public interface ReviewDao {

    @Query( "outputId = ? order by reviewDate DESC")
    public List<Review> findByOutputId(long outputId);

    public void remove(Review review);
    
    public void insert(Review review);

    public void update(Review review);
}
