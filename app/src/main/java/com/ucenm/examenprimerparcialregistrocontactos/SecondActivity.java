package com.ucenm.examenprimerparcialregistrocontactos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class SecondActivity extends AppCompatActivity {

    private ImageView imgDetalleFoto;
    private TextView txtDetalleNombre;
    private TextView txtDetallePais;
    private TextView txtDetalleTelefono;
    private TextView txtDetalleNota;
    private MaterialButton btnRegresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainSecond), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Vincular elementos gráficos por su ID
        imgDetalleFoto = findViewById(R.id.imgDetalleFoto);
        txtDetalleNombre = findViewById(R.id.txtDetalleNombre);
        txtDetallePais = findViewById(R.id.txtDetallePais);
        txtDetalleTelefono = findViewById(R.id.txtDetalleTelefono);
        txtDetalleNota = findViewById(R.id.txtDetalleNota);
        btnRegresar = findViewById(R.id.btnRegresar);

        // Recibir datos desde el Intent
        obtenerYMostrarDatos();

        // Listener para el botón regresar
        btnRegresar.setOnClickListener(v -> finish());
    }

    private void obtenerYMostrarDatos() {
        Intent intent = getIntent();

        if (intent != null) {
            String nombre = intent.getStringExtra("nombre");
            String telefono = intent.getStringExtra("telefono");
            String pais = intent.getStringExtra("pais");
            String nota = intent.getStringExtra("nota");
            byte[] imagenBytes = intent.getByteArrayExtra("imagen");

            // Nombre
            if (nombre != null && !nombre.isEmpty()) {
                txtDetalleNombre.setText(nombre);
            } else {
                txtDetalleNombre.setText("Contacto sin nombre");
            }

            // País
            if (pais != null && !pais.isEmpty()) {
                txtDetallePais.setText("País: " + pais);
            } else {
                txtDetallePais.setText("País: No especificado");
            }

            // Teléfono
            if (telefono != null && !telefono.isEmpty()) {
                txtDetalleTelefono.setText(telefono);
            } else {
                txtDetalleTelefono.setText("Sin número de teléfono");
            }

            // Nota
            if (nota != null && !nota.trim().isEmpty()) {
                txtDetalleNota.setText(nota);
            } else {
                txtDetalleNota.setText("Sin nota adicional.");
            }

            // Foto de Perfil (Opcional)
            if (imagenBytes != null && imagenBytes.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
                imgDetalleFoto.setImageBitmap(bitmap);
            } else {
                imgDetalleFoto.setImageResource(R.drawable.ic_contact_placeholder);
            }
        }
    }
}
