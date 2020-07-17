package com.varenia.vaarta.retrofit;

import com.varenia.vaarta.retrofit.response.MeetingInvitationResp;
import com.varenia.vaarta.retrofit.response.MeetingValidationResp;
import com.varenia.vaarta.retrofit.response.UserLoginSR;
import com.varenia.vaarta.util.Constants;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by VCIMS-PC2 on 26-09-2017.
 */

public interface RetrofitInterface {

    //Here we can declare different calls for different web services and each call will have some parameters that
    //we can send to the web services for getting data.

    @FormUrlEncoded
    @POST(Constants.LOGIN_URL)
    Call<UserLoginSR> loginUser(
            @Field("data") String data
    );
    @FormUrlEncoded
    @POST(Constants.MEETING_VALIDATION_URL)
    Call<MeetingValidationResp> MEETING_VALIDATION_RESP_CALL(
            @Field("data") String data
    );
    @FormUrlEncoded
    @POST(Constants.MEETING_INVITATATION_URL)
    Call<MeetingInvitationResp> MEETING_INVITATATION_RESP_CALL(
            @Field("contacts") String data,
            @Field("msg") String msg
    );
}
