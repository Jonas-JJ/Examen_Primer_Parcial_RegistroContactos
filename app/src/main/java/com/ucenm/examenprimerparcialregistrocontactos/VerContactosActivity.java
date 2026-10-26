package com.ucenm.examenprimerparcialregistrocontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class VerContactosActivity extends AppCompatActivity implements ContactoAdapter.OnContactoActionListener {

    private RecyclerView recyclerContactos;
    private TextView txtSinContactos;
    private SQLiteConexion conexion;
    private ContactoAdapter adapter;
    private List<Contacto> listaContactos = new ArrayList<>();
    private String numeroTelefonoALlamar = "";

    // Launcher para permiso CALL_PHONE en tiempo de ejecución
    private final ActivityResultLauncher<String> requestCallPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    ejecutarLlamada(numeroTelefonoALlamar);
                } else {
                    Toast.makeText(this, "Se requiere permiso para realizar la llamada.", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_contactos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainVerContactos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        recyclerContactos.setLayoutManager(new LinearLayoutManager(this));

        cargarContactos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        listaContactos = conexion.obtenerContactos();

        if (listaContactos.isEmpty()) {
            txtSinContactos.setVisibility(View.VISIBLE);
            recyclerContactos.setVisibility(View.GONE);
        } else {
            txtSinContactos.setVisibility(View.GONE);
            recyclerContactos.setVisibility(View.VISIBLE);

            adapter = new ContactoAdapter(this, listaContactos, this);
            recyclerContactos.setAdapter(adapter);
        }
    }

    // =========================================================================
    // PANTALLA 3 - LLAMADAS CON CONFIRMACIÓN (AlertDialog) Y PERMISO RUNTIME
    // =========================================================================
    @Override
    public void onLlamarContacto(Contacto contacto) {
        String nombre = contacto.getNombre();
        String numeroCompleto = contacto.getPais() + " " + contacto.getTelefono();

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Llamada")
                .setMessage("¿Desea llamar a " + nombre + "?\n\nTeléfono: " + numeroCompleto)
                .setPositiveButton("Sí", (dialog, which) -> {
                    numeroTelefonoALlamar = numeroCompleto;
                    solicitarPermisoYRealizarLlamada(numeroCompleto);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void solicitarPermisoYRealizarLlamada(String numero) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            ejecutarLlamada(numero);
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE);
        }
    }

    private void ejecutarLlamada(String numero) {
        if (numero == null || numero.trim().isEmpty()) return;

        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numero.replace(" ", "")));
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permiso denegado para realizar llamadas.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo realizar la llamada.", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================================
    // COMPARTIR CONTACTO CON INTENT.ACTION_SEND
    // =========================================================================
    @Override
    public void onCompartirContacto(Contacto contacto) {
        String textoACompartir = "Contacto: " + contacto.getNombre() +
                "\nTeléfono: " + contacto.getPais() + " " + contacto.getTelefono() +
                (contacto.getNota() != null && !contacto.getNota().isEmpty() ? "\nNota: " + contacto.getNota() : "");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textoACompartir);
        startActivity(Intent.createChooser(shareIntent, "Compartir contacto vía"));
    }

    // =========================================================================
    // VER IMAGEN GUARDADA EN GRANDE EN UN DIÁLOGO
    // =========================================================================
    @Override
    public void onVerImagenContacto(Contacto contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(contacto.getNombre());

        ImageView imageView = new ImageView(this);
        imageView.setPadding(32, 32, 32, 32);
        imageView.setAdjustViewBounds(true);

        byte[] imagenBytes = contacto.getImagen();
        if (imagenBytes != null && imagenBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(R.drawable.ic_contact_placeholder);
        }

        builder.setView(imageView);
        builder.setPositiveButton("Cerrar", null);
        builder.show();
    }

    // =========================================================================
    // EDITAR / ACTUALIZAR CONTACTO
    // =========================================================================
    @Override
    public void onEditarContacto(Contacto contacto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Contacto");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_editar_contacto, null);
        builder.setView(dialogView);

        EditText editNombre = dialogView.findViewById(R.id.editNombreDialog);
        EditText editTelefono = dialogView.findViewById(R.id.editTelefonoDialog);
        EditText editNota = dialogView.findViewById(R.id.editNotaDialog);

        editNombre.setText(contacto.getNombre());
        editTelefono.setText(contacto.getTelefono());
        editNota.setText(contacto.getNota());

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = editNombre.getText().toString().trim();
            String nuevoTelefono = editTelefono.getText().toString().trim();
            String nuevaNota = editNota.getText().toString().trim();

            if (nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
                Toast.makeText(this, "El nombre y teléfono son obligatorios", Toast.LENGTH_SHORT).show();
                return;
            }

            contacto.setNombre(nuevoNombre);
            contacto.setTelefono(nuevoTelefono);
            contacto.setNota(nuevaNota);

            int filas = conexion.actualizarContacto(contacto);
            if (filas > 0) {
                Toast.makeText(this, "Contacto actualizado correctamente", Toast.LENGTH_SHORT).show();
                cargarContactos();
            } else {
                Toast.makeText(this, "Error al actualizar contacto", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // =========================================================================
    // ELIMINAR CONTACTO CON ACTUALIZACIÓN EN TIEMPO REAL
    // =========================================================================
    @Override
    public void onEliminarContacto(Contacto contacto, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Contacto")
                .setMessage("¿Está seguro de eliminar a " + contacto.getNombre() + " de la base de datos?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int resultado = conexion.eliminarContacto(contacto.getId());
                    if (resultado > 0) {
                        Toast.makeText(this, "Contacto eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarContactos(); // Actualiza la lista en tiempo real
                    } else {
                        Toast.makeText(this, "No se pudo eliminar el contacto", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
