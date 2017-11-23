package com.example.valera.ldc_schedule;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by valera on 09.11.2017.
 */

final class Schedule implements Iterable<Schedule.ScheduleRow> {

    enum rowPosition {
        EVEN,
        ODD
    }

    final static String[] columnHeaders = {
            "cab",
            "snp",
            "post",
            "mon",
            "mon_end",
            "tue",
            "tue_end",
            "wed",
            "wed_end",
            "thu",
            "thu_end",
            "fri",
            "fri_end"};
    final int minRowsCount = 9;
    private ArrayList<ScheduleRow> rows = new ArrayList<>();
    private HashMap<rowPosition, Integer> colors;

    public Schedule () {}

    public Schedule (ResultSet sqlData, HashMap<rowPosition, Integer> colors) throws SQLException {
        int rowIndex = 1;
        while (sqlData.next()) {
            HashMap<String, String> map = new HashMap<>();
            for (String header : columnHeaders) {
                map.put(header, sqlData.getString(header));
            }
            rows.add(new ScheduleRow(
                    map,
                    colors.get(getRowPosition(rowIndex))));
            rowIndex++;
        }
        if (minRowsCount > rowIndex) {
            for (int i = rowIndex; i <= minRowsCount; i++) {
                HashMap<String, String> map = new HashMap<>();
                for (String header : columnHeaders) {
                    map.put(header, "");
                }
                rows.add(new ScheduleRow(
                        map,
                        colors.get(getRowPosition(i))));
            }
        }
    }

    private rowPosition getRowPosition (int position) {
        return checkForEven(position) == true ? rowPosition.EVEN : rowPosition.ODD;
    }

    private boolean checkForEven (int number) {
        return (number & 1) == 0 ? true : false;
    }

    public void add (ScheduleRow row) {
        rows.add(row);
    }

    public void clear () {
        rows.clear();
    }

    public int size () {
        return rows.size();
    }

    public ScheduleRow get(int position) {
        return rows.get(position);
    }

    public ArrayList<ScheduleRow> getRows() {
        return rows;
    }

    /**
     * Returns an {@link Iterator} for the elements in this object.
     *
     * @return An {@code Iterator} instance.
     */
    @NonNull
    @Override
    public Iterator<ScheduleRow> iterator() {
        return rows.iterator();
    }

    final class ScheduleRow {
        private HashMap<String, String> data;
        int color;

        public ScheduleRow(HashMap<String, String> data, @ColorInt int color) {
            this.data = new HashMap<>(data);
            this.color = color;
        }

        public HashMap<String, String> getData() {
            return data;
        }

        public @ColorInt int getColor() {
            return color;
        }

        public String get(String key) {
            return data.get(key);
        }
    }
}