package com.example.ggxiaozhi.store.the_basket.api;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import com.zhxu.library.api.BaseApi;
import com.zhxu.library.listener.HttpOnNextListener;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import rx.Observable;

public class DownLoadApkApi extends BaseApi<String> {
    public DownLoadApkApi(HttpOnNextListener listener, RxAppCompatActivity rxAppCompatActivity) {
        super(listener, rxAppCompatActivity);
        setCache(true);
        setMothed("categorydata/subject");
    }

    @Override
    public Observable getObservable(Retrofit retrofit) {
        HttpGetService httpGetService = retrofit.create(HttpGetService.class);
        return httpGetService.getDownloadApk();
    }

    @Override
    public String call(ResponseBody responseBody) {
        String result = "";
        try {
            result = responseBody.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
