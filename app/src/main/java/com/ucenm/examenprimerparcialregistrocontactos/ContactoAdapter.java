package com.ucenm.examenprimerparcialregistrocontactos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.ContactoViewHolder> {

    public interface OnContactoActionListener {
        void onLlamarContacto(Contacto contacto);
        void onCompartirContacto(Contacto contacto);
        void onEditarContacto(Contacto contacto);
        void onEliminarContacto(Contacto contacto, int position);
        void onVerImagenContacto(Contacto contacto);
    }

    private final Context context;
    private final List<Contacto> listaContactos;
    private final OnContactoActionListener listener;

    public ContactoAdapter(Context context, List<Contacto> listaContactos, OnContactoActionListener listener) {
        this.context = context;
        this.listaContactos = listaContactos;
        this.listener = listener;
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
        String telefonoConPais = contacto.getPais() + " " + contacto.getTelefono();
        holder.txtTelefonoPais.setText(telefonoConPais);
        holder.txtNota.setText(contacto.getNota() != null && !contacto.getNota().isEmpty() ? contacto.getNota() : "Sin nota");

        byte[] imagenBytes = contacto.getImagen();
        if (imagenBytes != null && imagenBytes.length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            holder.imgFoto.setImageBitmap(bitmap);
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_contact_placeholder);
        }

        // Listeners para cada acción
        holder.imgFoto.setOnClickListener(v -> listener.onVerImagenContacto(contacto));
        holder.btnLlamar.setOnClickListener(v -> listener.onLlamarContacto(contacto));
        holder.btnCompartir.setOnClickListener(v -> listener.onCompartirContacto(contacto));
        holder.btnEditar.setOnClickListener(v -> listener.onEditarContacto(contacto));
        holder.btnEliminar.setOnClickListener(v -> listener.onEliminarContacto(contacto, holder.getBindingAdapterPosition()));
        holder.itemView.setOnClickListener(v -> listener.onVerImagenContacto(contacto));
    }

    @Override
    public int getItemCount() {
        return listaContactos.size();
    }

    public static class ContactoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFoto;
        TextView txtNombre, txtTelefonoPais, txtNota;
        ImageButton btnLlamar, btnCompartir, btnEditar, btnEliminar;

        public ContactoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgItemFoto);
            txtNombre = itemView.findViewById(R.id.txtItemNombre);
            txtTelefonoPais = itemView.findViewById(R.id.txtItemTelefonoPais);
            txtNota = itemView.findViewById(R.id.txtItemNota);
            btnLlamar = itemView.findViewById(R.id.btnItemLlamar);
            btnCompartir = itemView.findViewById(R.id.btnItemCompartir);
            btnEditar = itemView.findViewById(R.id.btnItemEditar);
            btnEliminar = itemView.findViewById(R.id.btnItemEliminar);
        }
    }
}
