package com.fondova.finance.ui.chart.detail;


import com.shinobicontrols.charts.CandlestickSeriesStyle;
import com.shinobicontrols.charts.Data;
import com.shinobicontrols.charts.MultiValueDataPoint;
import com.shinobicontrols.charts.Series;
import com.shinobicontrols.charts.SeriesStyleProvider;

class CandelstickStyleProvider implements SeriesStyleProvider<CandlestickSeriesStyle> {

    private int colorUp, colorDown;

    CandelstickStyleProvider(int colorUp, int colorDown) {
        this.colorUp = colorUp;
        this.colorDown = colorDown;
    }

    @Override
    public <S extends Series<CandlestickSeriesStyle>> CandlestickSeriesStyle provide(Data<?, ?> data, int i, S series) {
        SeriesStyleProvider<CandlestickSeriesStyle> defaultStyle = series.createDefaultSeriesStyleProvider();
        CandlestickSeriesStyle providedStyle = defaultStyle.provide(data, i, series);

        if (data == null) return providedStyle;

        if (series.getDataAdapter() != null && series.getDataAdapter().size() > 0 && i < series.getDataAdapter().size()) {
            Double currentClose = (Double) ((MultiValueDataPoint) data).getClose();
            Double currentOpen = (Double) ((MultiValueDataPoint) data).getOpen();
            Double priorClose = 0d;
            if (i > 0 && i < series.getDataAdapter().size()) {
                MultiValueDataPoint priorPoint = (MultiValueDataPoint) series.getDataAdapter().get(i - 1);
                if (priorPoint != null) priorClose = (Double) priorPoint.getClose();
            }

            int color;
            if (currentClose > priorClose && currentClose > currentOpen) {
                color = colorUp;
            } else {
                color = colorDown;
            }
            providedStyle.setFallingColor(color);
            providedStyle.setRisingColor(color);
            providedStyle.setRisingColorGradient(color);
            providedStyle.setFallingColorGradient(color);
            providedStyle.setOutlineColor(color);
            providedStyle.setStickColor(color);
        }
        return providedStyle;
    }
}