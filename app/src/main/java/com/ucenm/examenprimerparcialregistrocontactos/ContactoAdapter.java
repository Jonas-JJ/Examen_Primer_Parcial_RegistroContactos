package com.ucenm.examenprimerparcialregistrocontactos;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    private final Context context;
    private final List<Contacto> listaContactos;

    public ContactoAdapter(Context context, List<Contacto> listaContactos) {
        this.context = context;
        this.listaContactos = listaContactos;
    }

    @NonNull
    @Override
    public ContactoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contacto, parent, false);
        return new ContactoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactoViewHolder holder, int position) {
        Contacto contacto = listaContactos.get(position);

        holder.txtNombre.setText(contacto.getNombre());
        String telefonoConPais = contacto.getTelefono() + " (" + contacto.getPais() + ")";
        holder.txtTelefonoPais.setText(telefonoConPais);
        holder.txtNota.setText(contacto.getNota());

        byte[] imagenBytes = contacto.getImagen();
        if (imagenBytes != null && imagenBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            holder.imgFoto.setImageBitmap(bitmap);
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_contact_placeholder);
        }

        // Abrir SecondActivity con los detalles al hacer clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SecondActivity.class);
            intent.putExtra("id", contacto.getId());
            intent.putExtra("nombre", contacto.getNombre());
            intent.putExtra("telefono", contacto.getTelefono());
            intent.putExtra("pais", contacto.getPais());
            intent.putExtra("nota", contacto.getNota());
            intent.putExtra("imagen", contacto.getImagen());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView txtNombre, txtTelefonoPais, txtNota;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
            txtNombre = itemView.findViewById(R.id.txtItemNombre);
            txtTelefonoPais = itemView.findViewById(R.id.txtItemTelefonoPais);
            txtNota = itemView.findViewById(R.id.txtItemNota);
        }
    }
}
