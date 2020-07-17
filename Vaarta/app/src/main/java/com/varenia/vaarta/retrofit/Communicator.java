package com.varenia.vaarta.retrofit;

import com.squareup.otto.Produce;
import com.varenia.vaarta.util.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// Here we specify our main URL.
// This is the class which will get responses from the server.
//

public class Communicator {
    private static final String SERVER_URL = Constants.MAIN_URL;

    RetrofitInterface service;

    @Produce
    public ServerEvent produceServerEvent(StringArraySR stringArraySR) {
        return new ServerEvent(stringArraySR);
    }

    @Produce
    public ErrorEvent produceErrorEvent(int errorCode, String errorMsg) {
        return new ErrorEvent(errorCode, errorMsg);
    }

    public RetrofitInterface initialization()
    {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();


        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
/*
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(10, TimeUnit.SECONDS);
        httpClient.readTimeout(5, TimeUnit.SECONDS);
        httpClient.writeTimeout(5, TimeUnit.SECONDS);*/

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(SERVER_URL)
                .build();

        service = retrofit.create(RetrofitInterface.class);

        return service;
    }

}
