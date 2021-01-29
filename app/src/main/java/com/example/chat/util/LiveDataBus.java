package com.example.chat.util;

import androidx.lifecycle.MutableLiveData;
import java.util.HashMap;
import java.util.Map;

public class LiveDataBus {

    private static volatile LiveDataBus liveDataBus;

    private Map<String, MutableLiveData<?>> liveDataMap;

    private LiveDataBus(){
        liveDataMap = new HashMap<>();
    }
    //DCL单例
    public static LiveDataBus getInstance() {
        if (liveDataBus == null) {
            synchronized (LiveDataBus.class){
                if (liveDataBus == null){
                    liveDataBus = new LiveDataBus();
                }
            }
        }
        return liveDataBus;
    }

    //存取一体
    public<T> MutableLiveData<T> with(String key,Class<T> clazz){
        if (!liveDataMap.containsKey(key)){
            liveDataMap.put(key,new MutableLiveData<>());
        }
        return (MutableLiveData<T>) liveDataMap.get(key);
    }
}
