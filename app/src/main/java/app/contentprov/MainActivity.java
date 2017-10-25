package app.contentprov;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener, ListView.OnItemLongClickListener{
    EditText etBuscar;
    EditText etMensaje;
    ContentResolver cr;
    ListView lvContactos;
    Button b;
    private final String tag = "SMS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etBuscar = (EditText) findViewById(R.id.etBuscar);
        etMensaje = (EditText) findViewById(R.id.etMensaje);

        b=(Button)findViewById(R.id.btnBuscar);
        b.setOnClickListener(this);

        lvContactos = (ListView) findViewById(R.id.lvContactos);
        lvContactos.setOnItemLongClickListener(this);

        cr = getContentResolver();

    }

    private void buscar() {
        String proyeccion[] = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.PHOTO_ID};

        String filtro = ContactsContract.Contacts.DISPLAY_NAME + " like ?";
        String args_filtro[] = {"%" + etBuscar.getText().toString() + "%"};

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, proyeccion, filtro, args_filtro, null);

        List<String> lista_contactos = new ArrayList<String>();

        if(cur.getCount()>0){
            while(cur.moveToNext()) {
                // Obtener id de contacto
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                // Obtener nombre de contacto
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                // si tiene telefono, lo agregamos a la lista de contactos
                if(Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))>0){
                    lista_contactos.add(name);
                }
            }
        }


        lvContactos.setAdapter(new ArrayAdapter<String>(this, R.layout.fila_contactos, lista_contactos));
        cur.close();// cerrar el cursor
    }

    @Override
    public void onClick(View view) {
        buscar();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        TextView t = (TextView) view;
        String nombreContacto = t.getText().toString();

        String proyeccion[]={ContactsContract.Contacts._ID};
        String filtro=ContactsContract.Contacts.DISPLAY_NAME + " = ?";
        String args_filtro[]={nombreContacto};

        List<String> lcon = new ArrayList<String>();

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,proyeccion,filtro, args_filtro,null);

        if(cur.getCount()>0){
            while(cur.moveToNext()) {
                String identificador = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                sendSms(identificador);
            }
        }
        cur.close();
        return true;
    }

    private void sendSms(String identificador){
        SmsManager smsManager = SmsManager.getDefault();
        String mensaje = etMensaje.getText().toString();

        Cursor curTelefono = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{identificador},null);

        while(curTelefono.moveToNext()) {
            String telefono = curTelefono.getString(curTelefono.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

            try{
                System.out.println(telefono +"," +mensaje);
                smsManager.sendTextMessage(telefono,null,mensaje,null,null);
                Log.d(tag, "SMS enviado");
            }catch(Exception e){
                Log.d(tag, "No se pudo enviar el SMS");
                //e.printStackTrace();
            }

        }
        curTelefono.close();
    }
}
