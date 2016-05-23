package com.sam_chordas.android.stockhawk.data;

/**
 * Created by vaibhav on 11/5/16.
 */
public class Stock {

    private String Low;
    private String Open;
    private String Adj_Close;
    private String Close;
    private String Date;
    private String Volume;
    private String Symbol;
    private String High;

    public String getLow() {
        return Low;
    }

    public void setLow(String low) {
        Low = low;
    }

    public String getOpen() {
        return Open;
    }

    public void setOpen(String open) {
        Open = open;
    }

    public String getAdj_Close() {
        return Adj_Close;
    }

    public void setAdj_Close(String adj_Close) {
        Adj_Close = adj_Close;
    }

    public String getClose() {
        return Close;
    }

    public void setClose(String close) {
        Close = close;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getVolume() {
        return Volume;
    }

    public void setVolume(String volume) {
        Volume = volume;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }

    public String getHigh() {
        return High;
    }

    public void setHigh(String high) {
        High = high;
    }
}
