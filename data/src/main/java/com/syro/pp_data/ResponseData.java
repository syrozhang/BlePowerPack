package com.syro.pp_data;

/**
 * Created by Syro on 2016-01-30.
 */
public class ResponseData<T> {
    private T mObj;
    private T mObjList;

    public T getObject() {
        return mObj;
    }

    public void setObject(T object) {
        this.mObj = object;
    }

    public T getObjList() {
        return mObjList;
    }

    public void setObjList(T objList) {
        this.mObjList = objList;
    }
}
