
package com.thomas.mcmissileguidance;

/**********************************************************
 * 
 * Class CubicSpline
 * 
 * Class for performing an interpolation using a cubic spline setTabulatedArrays
 * and interpolate adapted from Numerical Recipes in C
 * 
 * WRITTEN BY: Mick Flanagan
 * 
 * DATE: May 2002
 * 
 * DOCUMENTATION: See Michael T Flanagan's JAVA library on-line web page:
 * CubicSpline.html
 * 
 **********************************************************/

public class CubicSpline {

	private int npoints = 0; // no. of tabulated points
	private double[] y = null; // y=f(x) tabulated function
	private double[] x = null; // x in tabulated function f(x)
	private double[] y2 = null; // returned second derivatives of y
	private double yp1 = 0.0D; // first derivative at point one
	// default value = zero (natural spline)
	private double ypn = 0.0D; // first derivative at point n

	// default value = zero (natural spline)

	// Constructors
	// Constructor with data arrays initialised to arrays x and y
	public CubicSpline(double[] x, double[] y) {
		this.npoints = x.length;
		if (this.npoints != y.length)
			throw new IllegalArgumentException(
					"Arrays x and y are of different length");
		this.x = new double[npoints];
		this.y = new double[npoints];
		this.y2 = new double[npoints];
		for (int i = 0; i < this.npoints; i++) {
			this.x[i] = x[i];
			this.y[i] = y[i];
		}
		this.yp1 = 1e40;
		this.ypn = 1e40;
	}

	// METHODS
	// Resets the x y data arrays - primarily for use in BiCubicSpline
	public void resetData(double[] x, double[] y) {
		if (x.length != y.length)
			throw new IllegalArgumentException(
					"Arrays x and y are of different length");
		if (this.npoints != x.length)
			throw new IllegalArgumentException(
					"Original array length not matched by new array length");
		for (int i = 0; i < this.npoints; i++) {
			this.x[i] = x[i];
			this.y[i] = y[i];
		}
	}

    // Constructor with data arrays initialised to zero
	// Primarily for use by BiCubicSpline
	CubicSpline(int npoints) {
		this.npoints = npoints;
		this.x = new double[npoints];
		this.y = new double[npoints];
		this.y2 = new double[npoints];
		this.yp1 = 1e40;
		this.ypn = 1e40;
	}

	// Returns a new CubicSpline setting array lengths to n and all array values
	// to zero with natural spline default
	// Primarily for use in BiCubicSpline
	public static CubicSpline zero(int n) {
		CubicSpline aa = new CubicSpline(n);
		return aa;
	}

	// Create a one dimensional array of cubic spline objects of length n each
	// of array length m
	// Primarily for use in BiCubicSpline
	public static CubicSpline[] oneDarray(int n, int m) {
		CubicSpline[] a = new CubicSpline[n];
		for (int i = 0; i < n; i++) {
			a[i] = CubicSpline.zero(m);
		}
		return a;
	}

	// Enters the first derivatives of the cubic spline at
	// the first and last point of the tabulated data
	// Overrides a natural spline
	public void setDerivLimits(double yp1, double ypn) {
		this.yp1 = yp1;
		this.ypn = ypn;
	}

	// Returns the internal array of second derivatives
	public double[] getDeriv() {
		return this.y2;
	}

	// Calculates the second derivatives of the tabulated function
	// for use by the cubic spline interpolation method (.interpolate)
	public void calcDeriv() {
		int i = 0, k = 0;
		double p = 0.0D, qn = 0.0D, sig = 0.0D, un = 0.0D;
		double[] u = new double[npoints];

		if (yp1 > 0.99e30) {
			y2[0] = u[0] = 0.0;
		} else {
			this.y2[0] = -0.5;
			u[0] = (3.0 / (this.x[1] - this.x[0]))
					* ((this.y[1] - this.y[0]) / (this.x[1] - this.x[0]) - this.yp1);
		}

		for (i = 1; i <= this.npoints - 2; i++) {
			sig = (this.x[i] - this.x[i - 1]) / (this.x[i + 1] - this.x[i - 1]);
			p = sig * this.y2[i - 1] + 2.0;
			this.y2[i] = (sig - 1.0) / p;
			u[i] = (this.y[i + 1] - this.y[i]) / (this.x[i + 1] - this.x[i])
					- (this.y[i] - this.y[i - 1]) / (this.x[i] - this.x[i - 1]);
			u[i] = (6.0 * u[i] / (this.x[i + 1] - this.x[i - 1]) - sig
					* u[i - 1])
					/ p;
		}

		if (this.ypn > 0.99e30) {
			qn = un = 0.0;
		} else {
			qn = 0.5;
			un = (3.0 / (this.x[npoints - 1] - this.x[this.npoints - 2]))
					* (this.ypn - (this.y[this.npoints - 1] - this.y[this.npoints - 2])
							/ (this.x[this.npoints - 1] - x[this.npoints - 2]));
		}

		this.y2[this.npoints - 1] = (un - qn * u[this.npoints - 2])
				/ (qn * this.y2[this.npoints - 2] + 1.0);
		for (k = this.npoints - 2; k >= 0; k--) {
			this.y2[k] = this.y2[k] * this.y2[k + 1] + u[k];
		}
	}

	// INTERPOLATE
	// Returns an interpolated value of y for a value of xfrom a tabulated
	// function y=f(x)
	// after the data has been entered via a constructor and the derivatives
	// calculated and
	// stored by calcDeriv().
	public double interpolate(double xx) {
		int klo = 0, khi = 0, k = 0;
		double h = 0.0D, b = 0.0D, a = 0.0D, yy = 0.0D;

		if (xx < this.x[0] || xx > this.x[this.npoints - 1]) {
			// System.out.println(xx);
			xx = xx - 0.00001;
			// throw new
			// IllegalArgumentException("x is outside the range of data points");
		}

		klo = 0;
		khi = this.npoints - 1;
		while (khi - klo > 1) {
			k = (khi + klo) >> 1;
			if (this.x[k] > xx) {
				khi = k;
			} else {
				klo = k;
			}
		}
		h = this.x[khi] - this.x[klo];

		if (h == 0.0) {
			throw new IllegalArgumentException("Two values of x are identical");
		} else {
			a = (this.x[khi] - xx) / h;
			b = (xx - this.x[klo]) / h;
			yy = a
					* this.y[klo]
					+ b
					* this.y[khi]
					+ ((a * a * a - a) * this.y2[klo] + (b * b * b - b)
							* this.y2[khi]) * (h * h) / 6.0;
		}
		return yy;
	}

	// Returns an interpolated value of y for a value of x (xx) from a tabulated
	// function y=f(x)
	// after the derivatives (deriv) have been calculated independently of and
	// calcDeriv().
	public static double interpolate(double xx, double[] x, double[] y,
			double[] deriv) {
		if (((x.length != y.length) || (x.length != deriv.length))
				|| (y.length != deriv.length)) {
			throw new IllegalArgumentException(
					"array lengths are not all equal");
		}
		int n = x.length;
		int klo = 0, khi = 0, k = 0;
		double h = 0.0D, b = 0.0D, a = 0.0D, yy = 0.0D;

		klo = 0;
		khi = n - 1;
		while (khi - klo > 1) {
			k = (khi + klo) >> 1;
			if (x[k] > xx) {
				khi = k;
			} else {
				klo = k;
			}
		}
		h = x[khi] - x[klo];

		if (h == 0.0) {
			throw new IllegalArgumentException("Two values of x are identical");
		} else {
			a = (x[khi] - xx) / h;
			b = (xx - x[klo]) / h;
			yy = a
					* y[klo]
					+ b
					* y[khi]
					+ ((a * a * a - a) * deriv[klo] + (b * b * b - b)
							* deriv[khi]) * (h * h) / 6.0;
		}
		return yy;
	}

}