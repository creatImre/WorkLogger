package com.hw.szoftarch.worklogger.networking;

import com.hw.szoftarch.worklogger.entities.Project;
import com.hw.szoftarch.worklogger.entities.User;
import com.hw.szoftarch.worklogger.entities.UserLevel;
import com.hw.szoftarch.worklogger.entities.WorkingHour;

import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface WorkLoggerService {

    @GET("auth/")
    Call<User> login();

    @GET("auth/{googleId}")
    Call<String> setUserLevel(@Path("googleId") String googleId, UserLevel level);

    @GET("report/{projectName}")
    Call<Project> getReportByProjectName(@Path("projectName") String projectName);

    @POST("hour/")
    Call<String> addWorkingHour(WorkingHour workingHour);

    @PUT("hour/")
    Call<String> updateWorkingHour(WorkingHour workingHour);

    @DELETE("hour/")
    Call<String> removeWorkingHour(WorkingHour workingHour);

    @GET("hour/")
    Call<RealmList<WorkingHour>> getWorkingHoursByUser();

}
