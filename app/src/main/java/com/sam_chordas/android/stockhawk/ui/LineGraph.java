package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.util.Log;
import android.view.Gravity;
import android.widget.RadioButton;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.Stock;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LineGraph extends Activity {

    private static final int CURSOR_LOADER_ID = 0;

    @BindView(R.id.rbTime5d)
    RadioButton rbTime5d;
    @BindView(R.id.rbTime3m)
    RadioButton rbTime3m;
    @BindView(R.id.rbTime6m)
    RadioButton rbTime6m;
    @BindView(R.id.rbTime1y)
    RadioButton rbTime1y;
    @BindView(R.id.rbTime3y)
    RadioButton rbTime3y;
    @BindView(R.id.chart)
    LineChart lineChartView;
    String symbol;
    List<Stock> stocks = new ArrayList<>();
    @BindView(R.id.progressBar)
    ContentLoadingProgressBar contentLoadingProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        ButterKnife.bind(this);


        symbol = getIntent().getStringExtra(QuoteColumns.SYMBOL);
        rbTime5d.setOnClickListener(v -> getStocksDataFromYahooApi(DateTime.now().minusDays(5), DateTime.now()));
        rbTime3m.setOnClickListener(v -> getStocksDataFromYahooApi(DateTime.now().minusMonths(3), DateTime.now()));
        rbTime6m.setOnClickListener(v -> getStocksDataFromYahooApi(DateTime.now().minusMonths(6), DateTime.now()));
        rbTime1y.setOnClickListener(v -> getStocksDataFromYahooApi(DateTime.now().minusYears(1), DateTime.now()));
        rbTime3y.setOnClickListener(v -> getStocksDataFromYahooApiForThreeYears());
        lineChartView.setDragEnabled(true);
        lineChartView.setScaleEnabled(true);
        lineChartView.setPinchZoom(true);
        lineChartView.setDescription("");

    }

    private void getStocksDataFromYahooApiForThreeYears() {

        showDialog();

        String formattedStartDate1 = DateTime.now().minusYears(1).toString(Utils.YAHOO_DATE_FORMAT);
        String formattedEndDate1 = DateTime.now().toString(Utils.YAHOO_DATE_FORMAT);

        String formattedStartDate2 = DateTime.now().minusYears(2).toString(Utils.YAHOO_DATE_FORMAT);
        String formattedEndDate2 = DateTime.now().minusYears(1).toString(Utils.YAHOO_DATE_FORMAT);

        String formattedStartDate3 = DateTime.now().minusYears(3).toString(Utils.YAHOO_DATE_FORMAT);
        String formattedEndDate3 = DateTime.now().minusYears(2).toString(Utils.YAHOO_DATE_FORMAT);


        String url1 = buildYahooQuery(formattedStartDate1, formattedEndDate1);
        String url2 = buildYahooQuery(formattedStartDate2, formattedEndDate2);
        String url3 = buildYahooQuery(formattedStartDate3, formattedEndDate3);

        ExecutorService executorService = Executors.newCachedThreadPool();

        List<Callable<String>> callables = new ArrayList<>();
        callables.add(() -> Utils.fetchData(url1));
        callables.add(() -> Utils.fetchData(url2));
        callables.add(() -> Utils.fetchData(url3));

        try {
            List<Future<String>> futures = executorService.invokeAll(callables);
            for (int i = 0; i < futures.size(); i++) {
                Future<String> stringFuture = futures.get(i);
                List<Stock> stocks = Utils.quoteJsonToContentVals(stringFuture.get());
                this.stocks.addAll(stocks);
            }

            ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < stocks.size(); i++) {
                Stock stock = stocks.get(i);
                Entry c1e1 = new Entry(Float.parseFloat(stock.getClose()), i);
                xVals.add(stock.getDate());
                valsComp1.add(c1e1);
            }
            updateUI(valsComp1, xVals);
            dismissDialog();


        } catch (InterruptedException e) {


        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private void showDialog() {
        contentLoadingProgressBar.show();
    }

    private void dismissDialog() {
        contentLoadingProgressBar.hide();
    }

    private void toast(Stock stock) {
        String message = "open " + stock.getOpen() + "\nhigh +" + stock.getHigh() + "\nlow " + stock.getLow();
        Toast toast = Toast.makeText(this,
                message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP | Gravity.END, 0, 0);
        toast.show();
    }

    private void getStocksDataFromYahooApi(DateTime startDate, DateTime endDate) {

        showDialog();
        String formattedStartDate = startDate.toString(Utils.YAHOO_DATE_FORMAT);
        String formattedEndDate = endDate.toString(Utils.YAHOO_DATE_FORMAT);

        try {
            String url = buildYahooQuery(formattedStartDate, formattedEndDate);
            Log.e("url", url);
            ExecutorService executorService = Executors.newCachedThreadPool();
            Callable<String> stringCallable = () -> Utils.fetchData(url);
            Future<String> submit = executorService.submit(stringCallable);
            String s = submit.get();
            dismissDialog();
            stocks = Utils.quoteJsonToContentVals(s);

            ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
            ArrayList<String> xVals = new ArrayList<String>();
            for (int i = 0; i < stocks.size(); i++) {
                Stock stock = stocks.get(i);
                Entry c1e1 = new Entry(Float.parseFloat(stock.getClose()), i);
                xVals.add(stock.getDate());
                valsComp1.add(c1e1);
            }
            updateUI(valsComp1, xVals);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


    }

    private String buildYahooQuery(String formattedStartDate, String formattedEndDate) {
        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
        try {
            urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol "
                    + "= ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\"" + symbol + "\" and startDate = \"" + formattedStartDate + "\" and endDate = \"" + formattedEndDate + "\"", "UTF-8"));
            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
                    + "org%2Falltableswithkeys&callback=");
            String url = urlStringBuilder.toString();
            return url;
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return null;
    }


    private void updateUI(ArrayList<Entry> valsComp1, ArrayList<String> xVals) {
        LineDataSet setComp1 = new LineDataSet(valsComp1, symbol);
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        LineData data = new LineData(xVals, dataSets);
        lineChartView.setData(data);
        lineChartView.animate();
        lineChartView.invalidate();

    }

}
