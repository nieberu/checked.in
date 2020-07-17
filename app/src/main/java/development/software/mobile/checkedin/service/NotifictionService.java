package development.software.mobile.checkedin.service;

import development.software.mobile.checkedin.notification.NotificationResponse;
import development.software.mobile.checkedin.notification.NotificationSender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotifictionService {

     @Headers({
             "Content-Type:application/json",
             "Authorization:key=AAAAnl51Jas:APA91bGJl5SJDd3CvMc5MRWYuslxlDTJ3pJGaYzxMcgGCaFcuD3wxVsTc8-BnFfCuKcoQfNyhzV5aa8ggqY8Wp7IR46rmlI09VETJbcGb9pAFVDZ0kHkwPDJp2MUbfFNerZCRT6T5uKA"
     })
     @POST("fcm/send")
    Call<NotificationResponse> sendNotification(@Body NotificationSender sender);
}
