package com.ucenm.examenprimerparcialregistrocontactos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteConexion extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "DBContactos.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CONTACTOS = "contactos";
    public static final String COL_ID = "id";
    public static final String COL_PAIS = "pais";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_TELEFONO = "telefono";
    public static final String COL_NOTA = "nota";
    public static final String COL_IMAGEN = "imagen";

    private static final String CREATE_TABLE_CONTACTOS = "CREATE TABLE " + TABLE_CONTACTOS + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_PAIS + " TEXT, " +
            COL_NOMBRE + " TEXT, " +
            COL_TELEFONO + " TEXT, " +
            COL_NOTA + " TEXT, " +
            COL_IMAGEN + " BLOB);";

    public SQLiteConexion(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CONTACTOS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTOS);
        onCreate(db);
    }

    public long insertarContacto(Contacto contacto) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PAIS, contacto.getPais());
        values.put(COL_NOMBRE, contacto.getNombre());
        values.put(COL_TELEFONO, contacto.getTelefono());
        values.put(COL_NOTA, contacto.getNota());
        values.put(COL_IMAGEN, contacto.getImagen());

        long id = db.insert(TABLE_CONTACTOS, null, values);
        db.close();
        return id;
    }

    public List<Contacto> obtenerContactos() {
        List<Contacto> listaContactos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CONTACTOS + " ORDER BY " + COL_NOMBRE + " ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COL_ID);
                int paisIndex = cursor.getColumnIndex(COL_PAIS);
                int nombreIndex = cursor.getColumnIndex(COL_NOMBRE);
                int telefonoIndex = cursor.getColumnIndex(COL_TELEFONO);
                int notaIndex = cursor.getColumnIndex(COL_NOTA);
                int imagenIndex = cursor.getColumnIndex(COL_IMAGEN);

                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;
                String pais = paisIndex != -1 ? cursor.getString(paisIndex) : "";
                String nombre = nombreIndex != -1 ? cursor.getString(nombreIndex) : "";
                String telefono = telefonoIndex != -1 ? cursor.getString(telefonoIndex) : "";
                String nota = notaIndex != -1 ? cursor.getString(notaIndex) : "";
                byte[] imagen = imagenIndex != -1 ? cursor.getBlob(imagenIndex) : null;

                Contacto contacto = new Contacto(id, pais, nombre, telefono, nota, imagen);
                listaContactos.add(contacto);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return listaContactos;
    }
}
