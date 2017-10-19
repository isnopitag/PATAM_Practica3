package com.carmona.practica3;

import android.content.ClipData;
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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.onClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.lv)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toast.makeText(this,"El Número: \n"+getPhoneNumber()+"\n Se agrego a la BD.",Toast.LENGTH_LONG).show();
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
        Toast.makeText(this,"Pressed",Toast.LENGTH_LONG).show();
    }

    public class Android_Contact{
        public String nombre="";
        public String telefono="";
        public int id=0;
    }

    public void ObtenerContactos(){
        ArrayList<Android_Contact> arrayDeContactos = new ArrayList<Android_Contact>();

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
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " - ?",
                            new String[]{id},
                            null
                    );
                    while (cursorDeTelefono.moveToNext()){
                        String numero = cursorDeTelefono.getString(cursorDeTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        android_contact.telefono = numero;
                        System.out.println("This shiet"+numero);
                    }
                    cursorDeTelefono.close();
                }

                arrayDeContactos.add(android_contact);
            } //Fin todos los contactos

            //cursor.close();
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
            txttelefono.setText(android_contact.telefono);

            return convertView;

        }


    }
}
