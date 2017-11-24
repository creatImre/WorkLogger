package com.hw.szoftarch.worklogger.networking;

import com.hw.szoftarch.worklogger.entities.Project;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.WorkingHour;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface WorkLoggerService {

    @GET("auth/")
    Call<User> login();

    @PUT("auth/{googleId}")
    Call<String> setUserLevel(@Path("googleId") String googleId, @Body String level);

    @GET("report/{projectName}")
    Call<Project> getReportByProjectName(@Path("projectName") String projectName);

    @POST("hour/")
    Call<WorkingHour> addWorkingHour(@Body WorkingHour workingHour);

    @PUT("hour/")
    Call<String> updateWorkingHour(@Body WorkingHour workingHour);

    @HTTP(method = "DELETE", path = "hour/", hasBody = true)
    Call<String> removeWorkingHour(@Body WorkingHour workingHour);

    @GET("hour/")
    Call<List<WorkingHour>> getWorkingHoursByUser();

}
