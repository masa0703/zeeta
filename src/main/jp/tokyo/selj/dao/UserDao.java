 package jp.tokyo.selj.dao;

import java.util.List;

import org.seasar.dao.annotation.tiger.Arguments;
import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;
import org.seasar.dao.annotation.tiger.Sql;


@S2Dao(bean = User.class)
public interface UserDao {

    @Query ("order by userName")
    public List findAll();
    
    @Arguments({"userName"})
    public User findByName(String name);

    public void insert(User user);
    public void update(User user);
    public void remove(User user);

    @Sql("delete from usertbl")
    public void deleteAll();

    @Sql("delete from usertbl where userName=?")
    public void deleteByName(String name);
}
