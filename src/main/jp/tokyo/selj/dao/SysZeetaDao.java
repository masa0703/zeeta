 package jp.tokyo.selj.dao;

import org.seasar.dao.annotation.tiger.Query;
import org.seasar.dao.annotation.tiger.S2Dao;


@S2Dao(bean = SysZeeta.class)
public interface SysZeetaDao {

    @Query ("id = 1")
    public SysZeeta find();
    public void update(SysZeeta sysZeeta);

}
