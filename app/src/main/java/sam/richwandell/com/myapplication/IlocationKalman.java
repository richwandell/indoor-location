package sam.richwandell.com.myapplication;

import android.util.Log;

import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.KalmanFilter;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


public class ILocationKalman {
    private double[] readings = new double[11];
    private int currentReading = 0;

    private KalmanFilter filter;
    private RealVector pNoise;
    private RealVector mNoise;
    private RealMatrix H;
    private RealMatrix R;
    private RealMatrix A;
    private RealVector x;

    private double processNoise = 1e-5d;
    private double measurementNoise = .1d;



    public ILocationKalman(double sample) {

        readings[currentReading] = sample;
        currentReading++;

        //state transition matrix
        A = new Array2DRowRealMatrix(new double[]{1d});

        //control matrix
        RealMatrix B = null;

        //process noise
        RealMatrix Q = new Array2DRowRealMatrix(new double[]{processNoise});

        //initial state estimate
        x = new ArrayRealVector(new double[]{0d});

        //initial error
        RealMatrix P0 = new Array2DRowRealMatrix(new double[]{1d});

        //measurement matrix
        H = new Array2DRowRealMatrix(new double[]{sample});

        //measurement noise
        R = new Array2DRowRealMatrix(new double[]{measurementNoise});

        ProcessModel pm = new DefaultProcessModel(A, B, Q, x, P0);
        MeasurementModel mm = new DefaultMeasurementModel(H, R);

        filter = new KalmanFilter(pm, mm);

        pNoise = new ArrayRealVector(1);
        mNoise = new ArrayRealVector(1);
    }

    public void addSample(double sample){
        readings[currentReading] = sample;
        currentReading++;
        filter.predict();

        // simulate the process
        pNoise.setEntry(0, processNoise * sample);

        // x = A * x + p_noise
        x = A.operate(new ArrayRealVector(new double[]{sample})).add(pNoise);

        // simulate the measurement
        mNoise.setEntry(0, measurementNoise * sample);

        // z = H * x + m_noise
        RealVector z = H.operate(x).add(mNoise);

        filter.correct(z);
    }

    public double getStateEstimation(){
        double estimate[] = filter.getStateEstimation();
        return estimate[0];
    }

    public double getFirstMeasurement(){
        return readings[0];
    }


    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(double x : readings){
            sb.append(x);
            sb.append(", ");
        }
        sb.replace(sb.lastIndexOf(", "), sb.lastIndexOf(", ") + 2, "");
        return sb.toString();
    }
}
