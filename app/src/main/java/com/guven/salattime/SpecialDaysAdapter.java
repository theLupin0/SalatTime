package com.guven.salattime;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SpecialDaysAdapter extends RecyclerView.Adapter<SpecialDaysAdapter.ViewHolder> {

    private List<SpecialDays> gunList;

    public SpecialDaysAdapter(List<SpecialDays> gunList) {
        this.gunList = gunList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.ozelgun_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SpecialDays gun = gunList.get(position);
        holder.ad.setText(gun.getName());
        holder.tarih.setText(gun.getDate());
    }

    @Override
    public int getItemCount() {
        return gunList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView ad, tarih;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ad = itemView.findViewById(R.id.gunAdi);
            tarih = itemView.findViewById(R.id.gunTarihi);
        }
    }
}
