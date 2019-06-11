package me.hutcwp.api;

import java.util.ArrayList;
import java.util.List;

/**
 * 被注入类的基础类
 */
@RegisterTarget()
public abstract class BaseManager<T extends IRegister> {

    public BaseManager() {
        Injector.autoRegister(this);
    }

    private List<T> mDATA = new ArrayList<>();

    public void autoRegister(T register) {
        mDATA.add(register);
    }

    public List<T> getData() {
        return mDATA;
    }
}
