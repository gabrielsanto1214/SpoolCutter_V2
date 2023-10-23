package com.PI.spoolcutter.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.PI.spoolcutter.R;
import com.PI.spoolcutter.ui.dashboard.DashboardItem;

import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.DashboardViewHolder> {

    private List<DashboardItem> items;

    public DashboardAdapter(List<DashboardItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dashboard_item, parent, false);
        return new DashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        DashboardItem item = items.get(position);

        // Configure as visualizações do item com os dados do DashboardItem
        holder.dataProduzidaTextView.setText(item.getDataProduzida());
        holder.producaoDoDiaTextView.setText(item.getProducaoDoDia());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DashboardViewHolder extends RecyclerView.ViewHolder {
        public TextView dataProduzidaTextView;
        public TextView producaoDoDiaTextView;

        public DashboardViewHolder(@NonNull View itemView) {
            super(itemView);
            dataProduzidaTextView = itemView.findViewById(R.id.dataProduzidaTextView);
            producaoDoDiaTextView = itemView.findViewById(R.id.producaoDoDiaTextView);
        }
    }

    public void setData(List<DashboardItem> newData) {
        items.clear();
        items.addAll(newData);
        notifyDataSetChanged(); // Notifica o RecyclerView de que os dados foram atualizados
    }
}
