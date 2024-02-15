package iss.AD.myhealthapp;

import com.github.mikephil.charting.data.Entry;

import java.util.Comparator;

class EntryXComparator implements Comparator<Entry> {
    @Override
    public int compare(Entry e1, Entry e2) {
        return Float.compare(e1.getX(), e2.getX());
    }
}