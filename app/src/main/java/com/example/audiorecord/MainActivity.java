package com.example.audiorecord;

import static android.graphics.Color.BLUE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.jtransforms.fft.DoubleFFT_1D;
import com.github.mikephil.charting.data.Entry;
import java.util.List;
import java.util.Arrays;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import java.util.concurrent.atomic.AtomicBoolean;


import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import android.os.Handler;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.ImageView;
import android.graphics.Paint;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.data.ScatterData;
import com.github.mikephil.charting.data.ScatterDataSet;

import java.util.ArrayList;





public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1; // ここで定義

    private LineChart mChart;
    private ScatterChart mSecondChart;

    private ImageView heatmapImageView;
    private Bitmap heatmapBitmap;
    private Canvas heatmapCanvas;

    private Handler handler;
    private Runnable heatmapUpdater;
    // 起動時の秒
    private double appStartTime;
    private final int samplingRate = 2756;
    private double columnStartTime = 0.0;
    private final double columnDuration = 0.1; // seconds
    private final int[] colors = new int[]{
            BLUE,
            Color.GRAY,
            Color.MAGENTA};


    private void setupChart() {
        mChart.setData(new LineData());
        mChart.getDescription().setEnabled(false);
        mChart.setDrawGridBackground(true);
        mChart.getAxisRight().setEnabled(false);

        //縦軸
        mChart.getAxisLeft().setAxisMinimum(0);
        mChart.getAxisLeft().setAxisMaximum(150f);

        // デシベル単位
        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setLabelCount(6);  // ラベルの数
        yAxis.setValueFormatter(new DecibelFormatter());  // デシベルの単位フォーマッタ

        //横軸
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X軸をグラフの下部に配置
        xAxis.setGranularity(1f); // グリッドラインの間隔
        xAxis.setValueFormatter(new HertzFormatter()); // ラベルのフォーマッターを設定
        xAxis.setAxisMinimum(0);  // 最小値の設定
        xAxis.setAxisMaximum(1300f);  // 最大値の設定
        xAxis.setEnabled(true);  // X軸を有効にする



        // 新しいデータセットを作成
        LineDataSet dataSet = new LineDataSet(null, "FFT Result");
        dataSet.setColor(BLUE);   // グラフの線の色を設定
        dataSet.setLineWidth(0.5f);       // 太さ
        dataSet.setDrawCircles(false);  // グラフ上の点を非表示にする（オプション）

        // データセットを LineData に追加
        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    private void setupSecondChart() {
        mSecondChart.setData(new ScatterData());

        ScatterDataSet scatterDataSet = new ScatterDataSet(new ArrayList<>(), "Second Scatter FFT Result");
        scatterDataSet.setColor(Color.BLUE);
        scatterDataSet.setScatterShape(ScatterChart.ScatterShape.CIRCLE); // サークルの形状に設定
        scatterDataSet.setScatterShapeSize(8f); // サークルのサイズを設定
        scatterDataSet.setDrawValues(false);

        ScatterData scatterData = new ScatterData(scatterDataSet);
        mSecondChart.setData(scatterData);

        mSecondChart.getDescription().setEnabled(false);
        mSecondChart.setDrawGridBackground(true);
        mSecondChart.getAxisRight().setEnabled(false);


        // 縦軸
        YAxis secondYAxis = mSecondChart.getAxisLeft();
        secondYAxis.setAxisMinimum(0);
        secondYAxis.setAxisMaximum(1000f);

        // 周波数単位
        secondYAxis.setLabelCount(6);  // ラベルの数
        secondYAxis.setValueFormatter(new HertzFormatter());  // ここで新しいグラフ用の縦軸のフォーマッタを指定

        // 横軸
        XAxis secondXAxis = mSecondChart.getXAxis();
        secondXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        secondXAxis.setGranularity(1f);
        secondXAxis.setValueFormatter(new TimeFormatter());  // ここで新しいグラフ用の横軸のフォーマッタを指定
        secondXAxis.setAxisMinimum(0);
        secondXAxis.setAxisMaximum(3f);
        secondXAxis.setEnabled(true);

        mSecondChart.notifyDataSetChanged();
        mSecondChart.invalidate();
    }
    /*private void drawAxisLabels() {
        // ビットマップのサイズを取得
        int width = heatmapBitmap.getWidth();
        int height = heatmapBitmap.getHeight();

        // 目盛りの数
        int numXTicks = 10; // x軸の目盛りの数
        int numYTicks = 10; // y軸の目盛りの数

        // x軸の目盛りの間隔
        float xTickInterval = (float) width / numXTicks;

        // y軸の目盛りの間隔
        float yTickInterval = (float) height / numYTicks;


    }*/


    // X軸単位
    public class HertzFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return (int) value + " Hz";
        }
    }
    // y軸単位
    public class DecibelFormatter extends ValueFormatter {

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return (int) value + " dB";
        }
    }

    // 時間のフォーマッター
    public class TimeFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format("%.2f s", value);  // 秒単位で表示
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 起動時の秒
        appStartTime = System.currentTimeMillis() / 1000.0;

        mChart = findViewById(R.id.chart);
        setupChart();



        mSecondChart = findViewById(R.id.scatterChart);  // レイアウトからグラフのViewを取得
        setupSecondChart();  // 新しいグラフの初期設定

        /*heatmapImageView = findViewById(R.id.heatmapImageView);  // レイアウトからImageViewを取得
        if (heatmapImageView.getWidth() > 0 && heatmapImageView.getHeight() > 0) {
            heatmapBitmap = Bitmap.createBitmap(heatmapImageView.getWidth(), heatmapImageView.getHeight(), Bitmap.Config.ARGB_8888);
            heatmapCanvas = new Canvas(heatmapBitmap);
        } else {
            Log.e("BitmapCreationError", "View size is not yet determined. Creating a temporary bitmap.");
            heatmapBitmap = Bitmap.createBitmap(900, 900, Bitmap.Config.ARGB_8888);
            heatmapCanvas = new Canvas(heatmapBitmap);

        }

        // ここで heatmapBitmap のサイズもログに出力
        Log.d("BitmapSize", "heatmapBitmap size: " + heatmapBitmap.getWidth() + " x " + heatmapBitmap.getHeight());*/

        AudioRecordSample audioRecordSample = new AudioRecordSample();
        audioRecordSample.startRecording();

        requestAudioPermission();
    }

    public class AudioRecordSample {


        private int audioBufferSizeInByte;
        private final int samplingRate = 11025; //22050でも動いた
        private final int frameRate = 10;

        //ウィンドウ幅 4410
        private final int oneFrameDataCount = samplingRate / frameRate;
        private final int oneFrameSizeInByte = oneFrameDataCount * 2;


        // FFT用の変数を追加
        private DoubleFFT_1D fft;

        // 音声データを格納するフィールド
        private double[] audioDataDouble;


        // ハミング窓関数
        private double hammingWindow(int n, int N) {
            return 0.54 - 0.46 * Math.cos(2 * Math.PI * n / (N - 1));
        }

        public AudioRecordSample() {
            // FFTオブジェクトの初期化
            fft = new DoubleFFT_1D(oneFrameDataCount);

            // audioDataDoubleを初期化
            audioDataDouble = new double[oneFrameDataCount];
        }

        public void startRecording() {
            audioBufferSizeInByte = Math.max(oneFrameSizeInByte * 10,
                    AudioRecord.getMinBufferSize(samplingRate,
                            AudioFormat.CHANNEL_IN_MONO,
                            AudioFormat.ENCODING_PCM_16BIT));


            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    samplingRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    audioBufferSizeInByte);


            audioRecord.setPositionNotificationPeriod(oneFrameDataCount);
            audioRecord.setNotificationMarkerPosition(40000);

            short[] audioDataArray = new short[oneFrameDataCount];

            audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
                @Override
                public void onPeriodicNotification(AudioRecord recorder) {
                    recorder.read(audioDataArray, 0, oneFrameDataCount);
                    //Log.v("AudioRecord", "onPeriodicNotification size=" + audioDataArray.length);
                    // 好きに処理する
                    // 音声データをdouble型の配列に変換
                    double[] audioDataDouble = new double[audioDataArray.length];
                    for (int i = 0; i < audioDataArray.length; i++) {
                        audioDataDouble[i] = audioDataArray[i];
                    }

                    // ハミング窓の適用
                    for (int i = 0; i < audioDataArray.length; i++) {
                        audioDataDouble[i] = audioDataArray[i] * hammingWindow(i, audioDataArray.length);
                    }

                    // FFT処理を実行
                    fft.realForward(audioDataDouble);


                    // FFT結果をグラフに描画
                    onFFTResultAvailable(audioDataDouble);


                }

                @Override
                public void onMarkerReached(AudioRecord recorder) {
                    recorder.read(audioDataArray, 0, oneFrameDataCount);
                    //Log.v("AudioRecord", "onMarkerReached size=" + audioDataArray.length);
                    // 好きに処理する

                    double[] audioDataDouble = new double[audioDataArray.length];
                    for (int i = 0; i < audioDataArray.length; i++) {
                        audioDataDouble[i] = audioDataArray[i];
                    }

                    fft.realForward(audioDataDouble);


                    // FFT結果をグラフに描画
                    onFFTResultAvailable(audioDataDouble);
                }
            });

            audioRecord.startRecording();
        }
    }



    // FFT 結果をグラフに描画するメソッド
    public void onFFTResultAvailable(double[] fftResult) {
        final double tolerance = 0.5; // 許容誤差の値を定義
        final double thresholdDB = 80; // デシベルの閾値を定義

        final AtomicBoolean detected770Hz = new AtomicBoolean(false);
        final AtomicBoolean detected960Hz = new AtomicBoolean(false);


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LineDataSet dataSet = (LineDataSet) mChart.getData().getDataSetByIndex(0);

                if (dataSet == null) {
                    setupChart();
                    dataSet = (LineDataSet) mChart.getData().getDataSetByIndex(0);
                }

                dataSet.clear(); // 既存のデータをクリア

                ScatterData scatterData = mSecondChart.getData();
                if (scatterData == null) {
                    scatterData = new ScatterData();
                    mSecondChart.setData(scatterData);
                }

                ScatterChart secondChart = findViewById(R.id.scatterChart);
                ScatterDataSet secondDataSet = (ScatterDataSet) mSecondChart.getData().getDataSetByIndex(0);
                if (secondDataSet == null) {
                    setupSecondChart();
                    secondDataSet = (ScatterDataSet) mSecondChart.getData().getDataSetByIndex(0);

                    // 初めてデータが追加されるときの処理
                    mSecondChart.getXAxis().setAxisMinimum((float) getElapsedTimeInSeconds());
                    mSecondChart.getXAxis().setAxisMaximum((float) getElapsedTimeInSeconds() + 3);  // 3秒
                }

                //secondDataSet.clear();  // 既存のデータをクリア*/

                // BarChartのデータセットをクリア
                //mSecondChart.getData().clearValues();

                /* ここでヒートマップデータを生成し、Bitmapに描画
                drawHeatmap(fftResult);

                // BitmapをImageViewにセット
                heatmapImageView.setImageBitmap(heatmapBitmap);
                //Log.d("Debug", "drawHeatmap called");

                /* BarChartを更新
                mSecondChart.notifyDataSetChanged();
                mSecondChart.invalidate();*/

                // FFT 結果から周波数成分を取得してデータセットに追加
                double[] frequencies = calculateFrequencies(fftResult);

                ScatterChart scatterChart = findViewById(R.id.scatterChart);
                scatterChart.clear(); // 既存のデータをクリア

                List<Entry> entries = new ArrayList<>();

                for (int i = 0; i < frequencies.length; i++) {
                    double squaredValue = fftResult[i] * fftResult[i];
                    double amplitudeDB = 20 * Math.log10(Math.sqrt(squaredValue)); // 2乗した値をデシベルに変換

                    // ここでFFT結果をデシベルに変換して追加
                    dataSet.addEntry(new Entry((float) frequencies[i], (float) amplitudeDB));

                    double amplitude = fftResult[i];  // 振幅スペクトルを取得

                    int color = getColorForAmplitude(amplitude);

                    double currentTime = getCurrentTimeInSeconds();  // 現在の時間を取得
                    double elapsedTime = getElapsedTimeInSeconds();  // 経過時間を取得

                    float x = (float) elapsedTime;  // 横軸に時間
                    float y = (float) frequencies[i];  // 縦軸に周波数

                    entries.add(new Entry(x, y));

                    /* 770Hzと960Hzの周波数が含まれているか確認
                    if (!detected770Hz.get() && Math.abs(frequencies[i] - 770) < tolerance && amplitudeDB > thresholdDB) {
                        detected770Hz.set(true);
                        System.out.println("Frequency 770Hz is detected with amplitude " + amplitudeDB + " dB!");
                    }

                    if (!detected960Hz.get() && Math.abs(frequencies[i] - 960) < tolerance && amplitudeDB > thresholdDB) {
                        detected960Hz.set(true);
                        System.out.println("Frequency 960Hz is detected with amplitude " + amplitudeDB + " dB!");
                    }*/

                }
                // 左に移動させるためにX軸の最小値と最大値を更新
                mSecondChart.getXAxis().setAxisMinimum((float) getElapsedTimeInSeconds() - 3);  // 3秒表示
                mSecondChart.getXAxis().setAxisMaximum((float) getElapsedTimeInSeconds());


                //Log.d("Time", "-------------------" );*/


                secondDataSet.setValues(entries);
                mSecondChart.notifyDataSetChanged();
                mSecondChart.invalidate();


                mChart.notifyDataSetChanged();
                mChart.invalidate();
            }
        });
    }


    // FFT結果から周波数成分を計算するメソッド
    private double[] calculateFrequencies(double[] fftResult) {
        int numSamples = fftResult.length;
        double[] frequencies = new double[numSamples / 2];

        // サンプリングレート
        int samplingRate = 5512;

        // 周波数成分の間隔
        double frequencyResolution = (double) samplingRate / (2 * numSamples);

        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = i * frequencyResolution;
            //Log.d("FrequencyDebug", "Frequency at index " + i + ": " + frequencies[i]);
        }

        return frequencies;
    }



    // 最大振幅を検索するヘルパーメソッド
    private double findMaxAmplitude(List<Entry> entries) {
        double maxAmplitude = Double.MIN_VALUE;
        for (Entry entry : entries) {
            maxAmplitude = Math.max(maxAmplitude, Math.abs(entry.getY()));
        }
        return maxAmplitude;
    }

    // 現在の時間を秒単位で取得するメソッド
    private double getCurrentTimeInSeconds() {
        // ここに現在の時間を秒単位で取得するコードを追加
        // 例えば、System.currentTimeMillis() / 1000.0 など
        return System.currentTimeMillis() / 1000.0;
    }

    // 現在の経過時間を秒単位で取得するメソッド
    private double getElapsedTimeInSeconds() {
        return (System.currentTimeMillis() / 1000.0) - appStartTime;
    }

    // 振幅に応じて色を返すメソッド
    // 振幅に応じて色を返すメソッドを変更
    private int getColorForAmplitude(double amplitudeDB) {
        double normalizedAmplitude = amplitudeDB;

        // 振幅が70デシベル以下の場合は色を付けない
        if (normalizedAmplitude <= 70) {
            return Color.TRANSPARENT; // 透明色
        }

        // 濃淡をつけるためにRGBを計算
        int red = (int) (255 * (1 - normalizedAmplitude));
        int green = (int) (255 * normalizedAmplitude);

        // Color.rgbでRGBを合成して色を作成
        return Color.rgb(red, green, 0); // 赤が薄くなり、緑が濃くなるように設定
    }

    private int offsetX = 0; // 左にシフトするオフセット
    // ヒートマップを描画するメソッド
    private void drawHeatmap(double[] fftResult) {
        // 周波数成分の間隔
        double frequencyResolution = (double) samplingRate / (2 * fftResult.length);

        /// Bitmapを右にシフト
        heatmapCanvas.save(); // Canvasの状態を保存
        heatmapCanvas.translate(-10, 0);

        // ヒートマップの描画ロジック
        for (int i = 0; i < fftResult.length; i++) {
            double frequency = i * frequencyResolution;
            double squaredValue = fftResult[i] * fftResult[i];
            double amplitudeDB = 20 * Math.log10(Math.sqrt(squaredValue)); // 2乗した値をデシベルに変換

            int color = getColorForAmplitude(amplitudeDB);

            double currentTime = getCurrentTimeInSeconds();  // 現在の時間を取得
            double elapsedTime = getElapsedTimeInSeconds();  // 経過時間を取得

            // xとyの値
            int y = (int) frequency;  // y軸には周波数を使用


            double minX = 0;  // 任意の最小値を設定
            int x = heatmapBitmap.getWidth() - 1; // 一番右に描画


            // yの値をコンソールに表示
            //System.out.println("Y value: " + y);

            // yの値がbitmap.height()を超えないように制約
            y = Math.min(y, heatmapBitmap.getHeight() - 1);
            // yの値が0以下にならないように制約
            y = Math.max(y, 0);

            // 点の大きさを設定
            int pointSize = 3; // 任意の大きさ
            // 円を描画
            Paint paint = new Paint();
            paint.setColor(color);
            heatmapCanvas.drawCircle(x, y, pointSize, paint);


        }

        // BitmapをImageViewにセット
        heatmapImageView.setImageBitmap(heatmapBitmap);

    }






    // RECORD_AUDIO アクセス許可をリクエストするメソッド
    private void requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // アクセス許可が付与されていない場合、リクエストする
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

}

