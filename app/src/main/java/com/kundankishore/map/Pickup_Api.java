package com.kundankishore.map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Caliber on 02-09-2018.
 */

public interface Pickup_Api {
    String Base_Url = "http://hmkcode.appspot.com/";

    @POST("jsonservlet/")
    Call<Example> insertdata(@Body Example example);

    @GET("recommendations")
    Call<Example> getBooknames();
}
