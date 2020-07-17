package development.software.mobile.checkedin.util;

import android.util.Log;
import android.widget.Toast;

import development.software.mobile.checkedin.notification.Data;
import development.software.mobile.checkedin.notification.NotificationResponse;
import development.software.mobile.checkedin.notification.NotificationSender;
import development.software.mobile.checkedin.service.NotifictionService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PushNotification {

    private static PushNotification pushNotification;

    private NotifictionService notificationService;

    private PushNotification(NotifictionService notificationService){
        this.notificationService = notificationService;
    }

    public static PushNotification builder(){
        if(pushNotification == null){
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://fcm.googleapis.com/").addConverterFactory(GsonConverterFactory.create()).build();
            pushNotification = new PushNotification(retrofit.create(NotifictionService.class));
        }
        return pushNotification;
    }

    public void sendNotification(String token, Data data){
        NotificationSender sender = new NotificationSender(data,token);
        notificationService.sendNotification(sender).enqueue(new Callback<NotificationResponse>() {
            @Override
            public void onResponse(Call<NotificationResponse> call, Response<NotificationResponse> response) {
                if(response.code() == 200){
                    if (response.body().response!= 1) {
                        Log.e("PushNotification", "onResponse: failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<NotificationResponse> call, Throwable t) {
               t.printStackTrace();
            }
        });
    }
}
