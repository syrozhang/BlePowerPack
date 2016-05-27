package com.syro.pp_core;

/**
 * Created by Syro on 2016-01-30.
 */
public interface AppActionCallbackListener<T> {
    /**
     * 成功就返回数据
     * @param data
     */
    public void onSuccess(T data);

    /**
     * 失败就返回失败信息
     * @param errMsg
     */
    public void onFailure(String errMsg);
}
