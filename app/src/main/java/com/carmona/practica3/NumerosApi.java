package com.carmona.practica3;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

/**
 * Created by carmona on 19/10/17.
 */

public interface NumerosApi {
    @GET("numeros")
    Call<List<Numeros>> AllNumeros();

    @POST("/numeros/new_numero")
    Call<Numeros> nuevoNumero(@Body Numeros numeros);
}
