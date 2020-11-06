package jp.tokyo.selj.dao;

import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

public class SelJDaoContainer {
    public static S2Container SEL_DAO_CONT = 
    	S2ContainerFactory.create("jp/tokyo/selj/dao/selDao.dicon");
    static{
		SEL_DAO_CONT.init();
    }
}
