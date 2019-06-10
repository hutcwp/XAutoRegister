package me.hutcwp.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 被注入类的基础类
 */
public abstract class BaseManager {

    public BaseManager() {
        Injector.autoRegister(this);
    }

    private List<IRegister> mDATA = new ArrayList<>();

    public void autoRegister(IRegister register) {
        mDATA.add(register);
    }

    public List<IRegister> getData() {
        return mDATA;
    }

}
