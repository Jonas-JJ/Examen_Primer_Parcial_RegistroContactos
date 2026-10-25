package com.ucenm.examenprimerparcialregistrocontactos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private ImageView imgFotoContacto;
    private Spinner spnPais;
    private TextInputEditText txtNombre;
    private TextInputEditText txtTelefono;
    private TextInputEditText txtNota;

    private byte[] imagenSeleccionada = null;
    private SQLiteConexion conexion;

    // RegEx patterns
    private static final String REGEX_NOMBRE = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
    private static final String REGEX_TELEFONO = "^[0-9]+$";

    // Launchers for Camera & Gallery
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Object dataObj = extras.get("data");
                        if (dataObj instanceof Bitmap) {
                            procesarYMostrarImagen((Bitmap) dataObj);
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            procesarYMostrarImagen(bitmap);
                        } catch (Exception e) {
                            Log.e(TAG, "Error al cargar la imagen de la galería", e);
                            mostrarAlerta("Error de Imagen", "No se pudo cargar la imagen seleccionada.");
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "Se requiere permiso de cámara para capturar fotos", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize SQLite helper
        conexion = new SQLiteConexion(this);

        // Bind Views
        imgFotoContacto = findViewById(R.id.imgFotoContacto);
        MaterialButton btnTomarFoto = findViewById(R.id.btnTomarFoto);
        spnPais = findViewById(R.id.spnPais);
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);
        txtNota = findViewById(R.id.txtNota);
        MaterialButton btnSalvarContacto = findViewById(R.id.btnSalvarContacto);
        MaterialButton btnVerContactos = findViewById(R.id.btnVerContactos);

        // Listeners
        btnTomarFoto.setOnClickListener(v -> mostrarOpcionesCaptura());
        btnSalvarContacto.setOnClickListener(v -> guardarContacto());
        btnVerContactos.setOnClickListener(v -> navegarAContactos());
    }

    private void mostrarOpcionesCaptura() {
        String[] opciones = {"Tomar Foto (Cámara)", "Seleccionar de Galería"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Capturar Foto del Contacto");
        builder.setItems(opciones, (dialog, which) -> {
            if (which == 0) {
                verificarYAbrirCamara();
            } else {
                abrirGaleria();
            }
        });
        builder.show();
    }

    private void verificarYAbrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(cameraIntent);
    }

    private void abrirGaleria() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(galleryIntent);
    }

    private void procesarYMostrarImagen(Bitmap originalBitmap) {
        if (originalBitmap == null) return;

        Bitmap bitmapReducido = comprimirBitmap(originalBitmap, 600);
        imagenSeleccionada = bitmapToByteArray(bitmapReducido);
        imgFotoContacto.setImageBitmap(bitmapReducido);
    }

    private Bitmap comprimirBitmap(Bitmap image, int maxSize) {
        int origWidth = image.getWidth();
        int origHeight = image.getHeight();

        int targetWidth;
        int targetHeight;

        float bitmapRatio = (float) origWidth / (float) origHeight;
        if (bitmapRatio > 1) {
            targetWidth = maxSize;
            targetHeight = (int) (targetWidth / bitmapRatio);
        } else {
            targetHeight = maxSize;
            targetWidth = (int) (targetHeight * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true);
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    private void guardarContacto() {
        String pais = spnPais.getSelectedItem() != null ? spnPais.getSelectedItem().toString() : "";
        int posicionPais = spnPais.getSelectedItemPosition();
        String nombre = txtNombre.getText() != null ? txtNombre.getText().toString().trim() : "";
        String telefono = txtTelefono.getText() != null ? txtTelefono.getText().toString().trim() : "";
        String nota = txtNota.getText() != null ? txtNota.getText().toString().trim() : "";

        // 1. Validaciones de campos vacíos
        if (posicionPais == 0) {
            mostrarAlerta("Campo Requerido", "Debe seleccionar un país de la lista.");
            spnPais.requestFocus();
            return;
        }

        if (nombre.isEmpty()) {
            mostrarAlerta("Campo Requerido", "Debe escribir un nombre.");
            txtNombre.requestFocus();
            return;
        }

        if (telefono.isEmpty()) {
            mostrarAlerta("Campo Requerido", "Debe escribir un teléfono.");
            txtTelefono.requestFocus();
            return;
        }

        if (nota.isEmpty()) {
            mostrarAlerta("Campo Requerido", "Debe escribir una nota.");
            txtNota.requestFocus();
            return;
        }

        if (imagenSeleccionada == null || imagenSeleccionada.length == 0) {
            mostrarAlerta("Campo Requerido", "Debe capturar o seleccionar una foto para el contacto.");
            return;
        }

        // 2. Validaciones con Expresiones Regulares
        if (!nombre.matches(REGEX_NOMBRE)) {
            mostrarAlerta("Formato Inválido", "El nombre solo debe permitir letras y espacios.");
            txtNombre.requestFocus();
            return;
        }

        if (!telefono.matches(REGEX_TELEFONO)) {
            mostrarAlerta("Formato Inválido", "El teléfono solo debe contener dígitos numéricos.");
            txtTelefono.requestFocus();
            return;
        }

        // 3. Crear objeto Contacto e Insertar en SQLite
        Contacto nuevoContacto = new Contacto(pais, nombre, telefono, nota, imagenSeleccionada);
        long resultado = conexion.insertarContacto(nuevoContacto);

        if (resultado > 0) {
            mostrarAlerta("¡Éxito!", "Contacto guardado correctamente con el ID: " + resultado);
            limpiarFormulario();
        } else {
            mostrarAlerta("Error al Guardar", "Ocurrió un error al intentar guardar el contacto en la base de datos.");
        }
    }

    private void limpiarFormulario() {
        spnPais.setSelection(0);
        txtNombre.setText("");
        txtTelefono.setText("");
        txtNota.setText("");
        imgFotoContacto.setImageResource(R.drawable.ic_contact_placeholder);
        imagenSeleccionada = null;
        txtNombre.clearFocus();
        txtTelefono.clearFocus();
        txtNota.clearFocus();
    }

    private void navegarAContactos() {
        Intent intent = new Intent(MainActivity.this, VerContactosActivity.class);
        startActivity(intent);
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}
