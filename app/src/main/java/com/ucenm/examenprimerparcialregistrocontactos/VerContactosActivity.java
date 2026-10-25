package com.ucenm.examenprimerparcialregistrocontactos;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VerContactosActivity extends AppCompatActivity {

    private RecyclerView recyclerContactos;
    private TextView txtSinContactos;
    private SQLiteConexion conexion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_contactos);

        Toolbar toolbar = findViewById(R.id.toolbarVerContactos);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerContactos = findViewById(R.id.recyclerContactos);
        txtSinContactos = findViewById(R.id.txtSinContactos);

        conexion = new SQLiteConexion(this);

        cargarContactos();
    }

    private void cargarContactos() {
        List<Contacto> lista = conexion.obtenerContactos();

        if (lista.isEmpty()) {
            txtSinContactos.setVisibility(View.VISIBLE);
            recyclerContactos.setVisibility(View.GONE);
        } else {
            txtSinContactos.setVisibility(View.GONE);
            recyclerContactos.setVisibility(View.VISIBLE);

            ContactoAdapter adapter = new ContactoAdapter(this, lista);
            recyclerContactos.setLayoutManager(new LinearLayoutManager(this));
            recyclerContactos.setAdapter(adapter);
        }
    }
}
