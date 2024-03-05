package com.example.projekt_mobilne;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RestCountriesAPI {
    @GET("v2/all")
    Call<List<Country>> getName();
    @GET("v2/all")
    Call<List<Country>> getCountries();
}
