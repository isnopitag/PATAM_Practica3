package com.carmona.practica3;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements Callback<List<Numeros>>{

    private static final String BASE_URL = "http://192.168.0.100:8888/";
    //private static final String BASE_URL = "http://192.168.100.10:8888/";
    private ArrayList<Numeros> arrayNumeros;

    @BindView(R.id.lv)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        arrayNumeros = new ArrayList<Numeros>();
        CargarNumeros();


        Registro r = new Registro();
        r.registrar(getPhoneNumber());
        Toast.makeText(this,"Numero \n"+getPhoneNumber()+"\n Guardado!",Toast.LENGTH_LONG).show();

    }

    private String getPhoneNumber(){
        TelephonyManager mTelephonyManager;
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyManager.getLine1Number();
    }

    @OnClick(R.id.btnComprobar)
    public void onClick(View v){
        listView = (ListView) this.findViewById(R.id.lv);
        ObtenerContactos();
        Toast.makeText(this,"Contactos \n Cargados",Toast.LENGTH_LONG).show();
    }

    public boolean ComprobarServer(String numeros){
        boolean boleano=false;
        for (int i = 0; i < arrayNumeros.size() ; i++) {
            if(arrayNumeros.get(i).getNumero().equals(numeros)&&arrayNumeros.get(i).getNumero()!=null){
                boleano=true;
            }
        }
        return boleano;
    }

    public class Registro implements Callback<Numeros>{

        public void registrar(String linea1){
            Numeros numero = new Numeros();
            numero.setNumero(linea1);

            System.out.println(linea1+" "+linea1.length());
            Gson gson = new GsonBuilder().setLenient().create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            NumerosApi numerosApi = retrofit.create(NumerosApi.class);
            Call<Numeros> call = numerosApi.nuevoNumero(numero);
            call.enqueue(this);
        }

        @Override
        public void onResponse(Call<Numeros> call, Response<Numeros> response) {
            if(response.isSuccessful()) {
                System.out.println(response.body().toString());
            }
        }

        @Override
        public void onFailure(Call<Numeros> call, Throwable t) {
            t.printStackTrace();
        }
    }

    public void RegistrarServer() {

        Registro r = new Registro();

        for (int i = 0; i < arrayNumeros.size() ; i++) {
            if(arrayNumeros.get(i).getNumero().equals(getPhoneNumber())&&arrayNumeros.get(i).getNumero()!=null){
                Toast.makeText(this,"Numero \n"+getPhoneNumber()+"\n Ya existe!",Toast.LENGTH_LONG).show();
            }else{
                r.registrar(getPhoneNumber());
                Toast.makeText(this,"Numero \n"+getPhoneNumber()+"\n Guardado!",Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onResponse(Call<List<Numeros>> call, Response<List<Numeros>> response) {
        if(response.isSuccessful()){
            List<Numeros> numerosList = response.body();
            for(Numeros numero : numerosList){
             arrayNumeros.add(numero);
            }
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<List<Numeros>> call, Throwable t) {
        t.printStackTrace();
    }

    public class Android_Contact{
        public String nombre="";
        public String numero ="";
        public int id=0;
    }

    public void ObtenerContactos(){
        ArrayList<Android_Contact> arrayDeContactos = new ArrayList<Android_Contact>();
        String NUMERO="";
        Cursor cursor = null;
        ContentResolver contentResolver = getContentResolver();
        try {
            cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,null,null,null,null);
        }catch (Exception ex){
            Log.e("Error en Contacto", ex.getMessage());
        }

        if(cursor.getCount() > 0){
            while (cursor.moveToNext()){
                Android_Contact android_contact = new Android_Contact();
                String  id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String nombre = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                android_contact.nombre = nombre;

                int hayNumero = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

                if(hayNumero > 0){
                    Cursor cursorDeTelefono = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null
                    );
                    while (cursorDeTelefono.moveToNext()){
                        String numero = cursorDeTelefono.getString(cursorDeTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        android_contact.numero = numero;
                        NUMERO=numero;
                    }
                    cursorDeTelefono.close();
                }
                if(ComprobarServer(NUMERO)) {
                    arrayDeContactos.add(android_contact);
                }
            } //Fin todos los contactos

            AdapatadorContactos adaptador = new AdapatadorContactos(this, arrayDeContactos);

            listView.setAdapter(adaptador);
        }
    }

    public class AdapatadorContactos extends BaseAdapter{
        Context context;
        List<Android_Contact> listaDeContactos;

        public AdapatadorContactos(Context context,ArrayList<Android_Contact> android_contacts){
            this.context = context;
            listaDeContactos = android_contacts;
        }

        @Override
        public int getCount() {
            return listaDeContactos.size(); //returns total of items in the list
        }

        @Override
        public Object getItem(int position) {
            return listaDeContactos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = LayoutInflater.from(context).inflate(R.layout.lista_de_contactos,parent,false);
            }

            Android_Contact android_contact = (Android_Contact) getItem(position);

            TextView txtnombre  = (TextView) convertView.findViewById(R.id.txtNombre);
            TextView txttelefono  = (TextView) convertView.findViewById(R.id.txtTelefono);

            txtnombre.setText(android_contact.nombre);
            txttelefono.setText(android_contact.numero);

            return convertView;

        }


    }
    public void CargarNumeros() {
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        NumerosApi numerosApi = retrofit.create(NumerosApi.class);
        Call<List<Numeros>> call = numerosApi.AllNumeros();
        call.enqueue(this);

    }
}
