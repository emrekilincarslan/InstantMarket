package com.fondova.finance.ui.chart.detail;

import com.shinobicontrols.charts.Data;
import com.shinobicontrols.charts.DataAdapter;

import java.util.Collection;

class DataAdapterWithoutNotifying<Tx, Ty> extends DataAdapter<Tx, Ty> {

    private int pendingUpdates = 0;

    DataAdapterWithoutNotifying() {
    }

    public boolean add(Data<Tx, Ty> dataPoint) {
        boolean b = super.add(dataPoint);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public void add(int location, Data<Tx, Ty> dataPoint) {
        super.add(location, dataPoint);
    }

    public boolean addAll(Collection<? extends Data<Tx, Ty>> dataPoints) {
        boolean b = super.addAll(dataPoints);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public boolean addAll(int location, Collection<? extends Data<Tx, Ty>> dataPoints) {
        boolean b = super.addAll(location, dataPoints);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public void clear() {
        int size = this.size();
        super.clear();
        if (size > 0) {
            this.notifyDataChanged();
        }
    }

    public Data<Tx, Ty> remove(int location) {
        Data data = super.remove(location);
        this.notifyDataChanged();
        return data;
    }

    public boolean remove(Object object) {
        boolean b = super.remove(object);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public boolean removeAll(Collection<?> collection) {
        boolean b = super.removeAll(collection);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public boolean retainAll(Collection<?> collection) {
        boolean b = super.retainAll(collection);
        if (b) {
            this.notifyDataChanged();
        }
        return b;
    }

    public boolean update(int location, Data<Tx, Ty> dataPoint) {
        super.set(location, dataPoint);
        boolean updateCalled = false;
        pendingUpdates++;
        if (pendingUpdates >= 5) {
            // If we have 10 updates pending, we notify listeners that the data has changed.
            // Note that until this method is called, the chart will NOT be updated!
            notifyDataChanged();
            pendingUpdates = 0;
            updateCalled = true;
        }

        return updateCalled;
    }
}
