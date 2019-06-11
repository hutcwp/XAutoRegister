package me.hutcwp.api;

import java.util.ArrayList;
import java.util.List;

/**
 * ��ע����Ļ�����
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
