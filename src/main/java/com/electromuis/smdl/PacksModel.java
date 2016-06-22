package com.electromuis.smdl;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Created by electromuis on 12.05.16.
 */
public class PacksModel extends AbstractTableModel {
    private Pack[] packs;


    public PacksModel(Pack[] packs) {
        this.packs = packs;
    }

    public Pack[] getPacks() {
        return packs;
    }

    public void setPacks(Pack[] packs) {
        this.packs = packs;
        fireTableDataChanged();

    }

    public int getRowCount() {
        return getPacks().length;
    }

    public int getColumnCount() {
        return 4;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if(columnIndex==3){
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex==3);
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex){
            case 0: return "Name";
            case 1: return "Size";
            case 2: return "Type";
            case 3: return "Download";
            default: return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if(columnIndex == 3){
            getPack(rowIndex).setDownload((Boolean)aValue);
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Pack p = getPack(rowIndex);
        switch (columnIndex){
            case 0: return p.getName();
            case 1: return p.getSize();
            case 2: return p.getType();
            case 3: return p.isDownload();
            default: return null;
        }
    }

    public Pack getPack(int i){
        return packs[i];
    }


}
