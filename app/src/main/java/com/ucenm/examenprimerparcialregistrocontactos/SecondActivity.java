package com.ucenm.examenprimerparcialregistrocontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

    private String numeroTelefonoParaLlamar = "";

    private final ActivityResultLauncher<String> requestCallPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    realizarLlamada();
                } else {
                    Toast.makeText(this, "Se requiere permiso de llamadas para realizar la llamada.", Toast.LENGTH_SHORT).show();
                }
            }
    );

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
        MaterialButton btnLlamar = findViewById(R.id.btnLlamar);
        MaterialButton btnRegresar = findViewById(R.id.btnRegresar);

        // Recibir datos desde el Intent
        obtenerYMostrarDatos();

        // Listeners
        btnLlamar.setOnClickListener(v -> solicitarPermisoYLlamar());
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

            this.numeroTelefonoParaLlamar = telefono != null ? telefono : "";

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

    private void solicitarPermisoYLlamar() {
        if (numeroTelefonoParaLlamar == null || numeroTelefonoParaLlamar.trim().isEmpty()) {
            Toast.makeText(this, "El contacto no tiene un número telefónico registrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            realizarLlamada();
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void realizarLlamada() {
        if (numeroTelefonoParaLlamar == null || numeroTelefonoParaLlamar.trim().isEmpty()) return;

        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numeroTelefonoParaLlamar));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permiso denegado para realizar la llamada.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al intentar realizar la llamada.", Toast.LENGTH_SHORT).show();
        }
    }
}
