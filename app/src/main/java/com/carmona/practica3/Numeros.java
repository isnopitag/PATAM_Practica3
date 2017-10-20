package com.carmona.practica3;

import com.google.gson.annotations.SerializedName;

/**
 * Created by carmona on 19/10/17.
 */

public class Numeros {
    @SerializedName("id")
    private int id;
    @SerializedName("numero")
    private String numero;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
