package com.impervious.chatapp.Fragments;

import com.impervious.chatapp.notification.MyResponse;
import com.impervious.chatapp.notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAj6QBJqg:APA91bFLNDuMoHALhUDGCMOLC75lXANk3PuhPxw09tQpKOgy3E2ET1H9GJ5B7vAuI131W-FJb3vFzKYzKqJxzP5LEZxeCjtAjH3-f5C3PmqEd5Qyacvjdf0gSHykT7bNZztkDLle5Edf"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
