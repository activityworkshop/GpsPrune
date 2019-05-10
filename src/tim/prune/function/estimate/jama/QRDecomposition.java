package tim.prune.function.estimate.jama;

/**
 * QR Decomposition.
 *
 * For an m-by-n matrix A with m >= n, the QR decomposition is an m-by-n
 * orthogonal matrix Q and an n-by-n upper triangular matrix R so that A = Q*R.
 *
 * The QR decomposition always exists, even if the matrix does not have full
 * rank, so the constructor will never fail. The primary use of the QR
 * decomposition is in the least squares solution of nonsquare systems of
 * simultaneous linear equations. This will fail if isFullRank() returns false.
 *
 * Original authors The MathWorks, Inc. and the National Institute of Standards and Technology
 * The original public domain code has now been modified and reduced,
 * and is placed under GPL2 with the rest of the GpsPrune code.
 */
public class QRDecomposition
{

	/** Array for internal storage of decomposition */
	private double[][] _QR;

	/** Row and column dimensions */
	private int _m, _n;

	/** Array for internal storage of diagonal of R */
	private double[] _Rdiag;


	/**
	 * QR Decomposition, computed by Householder reflections.
	 *
	 * @param inA   Rectangular matrix
	 * @return Structure to access R and the Householder vectors and compute Q.
	 */
	public QRDecomposition(Matrix inA)
	{
		// Initialize.
		_QR = inA.getArrayCopy();
		_m = inA.getNumRows();
		_n = inA.getNumColumns();
		_Rdiag = new double[_n];

		// Main loop.
		for (int k = 0; k < _n; k++)
		{
			// Compute 2-norm of k-th column without under/overflow.
			double nrm = 0;
			for (int i = k; i < _m; i++) {
				nrm = Maths.pythag(nrm, _QR[i][k]);
			}

			if (nrm != 0.0)
			{
				// Form k-th Householder vector.
				if (_QR[k][k] < 0) {
					nrm = -nrm;
				}
				for (int i = k; i < _m; i++) {
					_QR[i][k] /= nrm;
				}
				_QR[k][k] += 1.0;

				// Apply transformation to remaining columns.
				for (int j = k + 1; j < _n; j++)
				{
					double s = 0.0;
					for (int i = k; i < _m; i++) {
						s += _QR[i][k] * _QR[i][j];
					}
					s = -s / _QR[k][k];
					for (int i = k; i < _m; i++) {
						_QR[i][j] += s * _QR[i][k];
					}
				}
			}
			_Rdiag[k] = -nrm;
		}
	}

	/*
	 * ------------------------ Public Methods ------------------------
	 */

	/**
	 * Is the matrix full rank?
	 * @return true if R, and hence A, has full rank.
	 */
	public boolean isFullRank()
	{
		for (int j = 0; j < _n; j++) {
			if (_Rdiag[j] == 0)
				return false;
		}
		return true;
	}


	/**
	 * Least squares solution of A*X = B
	 * @param B   A Matrix with as many rows as A and any number of columns
	 * @return X that minimizes the two norm of Q*R*X-B
	 * @exception IllegalArgumentException if matrix dimensions don't agree
	 * @exception RuntimeException         if Matrix is rank deficient.
	 */
	public Matrix solve(Matrix B)
	{
		if (B.getNumRows() != _m) {
			throw new IllegalArgumentException("Matrix row dimensions must agree.");
		}
		if (!isFullRank()) {
			throw new RuntimeException("Matrix is rank deficient.");
		}

		// Copy right hand side
		int nx = B.getNumColumns();
		double[][] X = B.getArrayCopy();

		// Compute Y = transpose(Q)*B
		for (int k = 0; k < _n; k++) {
			for (int j = 0; j < nx; j++) {
				double s = 0.0;
				for (int i = k; i < _m; i++) {
					s += _QR[i][k] * X[i][j];
				}
				s = -s / _QR[k][k];
				for (int i = k; i < _m; i++) {
					X[i][j] += s * _QR[i][k];
				}
			}
		}
		// Solve R*X = Y;
		for (int k = _n - 1; k >= 0; k--) {
			for (int j = 0; j < nx; j++) {
				X[k][j] /= _Rdiag[k];
			}
			for (int i = 0; i < k; i++) {
				for (int j = 0; j < nx; j++) {
					X[i][j] -= X[k][j] * _QR[i][k];
				}
			}
		}
		return (new Matrix(X, _n, nx).getMatrix(0, _n - 1, 0, nx - 1));
	}
}
