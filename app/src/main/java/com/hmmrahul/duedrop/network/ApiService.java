package com.hmmrahul.duedrop.network;

import com.hmmrahul.duedrop.models.MainModelClass;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

//    https://api.openweathermap.org/data/2.5/weather?q=Rajkot&units=metric&appid=337b87cfcbee5247eff3ce28e9c3d918
    @GET("weather")
    Call<MainModelClass> getDailyInfo(@Query("q") String cityName, @Query("units") String units,@Query("appid") String appId);


}
