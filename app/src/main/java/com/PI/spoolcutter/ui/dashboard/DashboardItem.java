package com.PI.spoolcutter.ui.dashboard;

public class DashboardItem {
    private String dataProduzida;
    private String producaoDoDia;

    public DashboardItem(String dataProduzida, String producaoDoDia) {
        this.dataProduzida = dataProduzida;
        this.producaoDoDia = producaoDoDia;
    }

    public String getDataProduzida() {
        return dataProduzida;
    }

    public String getProducaoDoDia() {
        return producaoDoDia;
    }
}
