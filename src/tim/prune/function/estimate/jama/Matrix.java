package tim.prune.function.estimate.jama;

/**
 * The Java Matrix Class provides the fundamental operations of numerical linear algebra.
 * Original authors The MathWorks, Inc. and the National Institute of Standards and Technology
 * The original public domain code has now been modified and reduced to only contain
 * the use of QR Decomposition of rectangular matrices, to solve least squares regression,
 * and is placed under GPL2 with the rest of the GpsPrune code.
 */
public class Matrix
{

	/** Array for internal storage of elements */
	private double[][] _matrix;

	/** Row and column dimensions */
	private int _m, _n;


	/**
	 * Construct an m-by-n matrix of zeros
	 * @param inM  Number of rows
	 * @param inN  Number of colums
	 */
	public Matrix(int inM, int inN)
	{
		_m = inM;
		_n = inN;
		_matrix = new double[inM][inN];
	}

	/**
	 * Construct a matrix from a 2-D array
	 * @param A   Two-dimensional array of doubles.
	 * @exception IllegalArgumentException All rows must have the same length
	 */
	public Matrix(double[][] A)
	{
		_m = A.length;
		_n = A[0].length;
		for (int i = 0; i < _m; i++) {
			if (A[i].length != _n) {
				throw new IllegalArgumentException("All rows must have the same length.");
			}
		}
		_matrix = A;
	}

	/**
	 * Construct a matrix quickly without checking arguments.
	 * @param inA   Two-dimensional array of doubles.
	 * @param inM   Number of rows
	 * @param inN   Number of columns
	 */
	public Matrix(double[][] inA, int inM, int inN)
	{
		_matrix = inA;
		_m = inM;
		_n = inN;
	}

	/*
	 * ------------------------ Public Methods ------------------------
	 */


	/**
	 * Set a value in a cell of the matrix
	 * @param inRow row index
	 * @param inCol column index
	 * @param inValue value to set
	 */
	public void setValue(int inRow, int inCol, double inValue)
	{
		_matrix[inRow][inCol] = inValue;
	}

	/**
	 * Access the internal two-dimensional array.
	 * @return Pointer to the two-dimensional array of matrix elements.
	 */
	public double[][] getArray() {
		return _matrix;
	}

	/**
	 * Copy the internal two-dimensional array.
	 * @return Two-dimensional array copy of matrix elements.
	 */
	public double[][] getArrayCopy()
	{
		double[][] C = new double[_m][_n];
		for (int i = 0; i < _m; i++) {
			for (int j = 0; j < _n; j++) {
				C[i][j] = _matrix[i][j];
			}
		}
		return C;
	}

	/**
	 * Get a single element.
	 * @param inRow   Row index
	 * @param inCol   Column index
	 * @return A(inRow,inCol)
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public double get(int inRow, int inCol) {
		return _matrix[inRow][inCol];
	}

	/** @return number of rows _m */
	public int getNumRows() {
		return _m;
	}

	/** @return number of columns _n */
	public int getNumColumns() {
		return _n;
	}

	/**
	 * Get a submatrix
	 * @param i0  Initial row index
	 * @param i1  Final row index
	 * @param j0  Initial column index
	 * @param j1  Final column index
	 * @return A(i0:i1,j0:j1)
	 * @exception ArrayIndexOutOfBoundsException
	 */
	public Matrix getMatrix(int i0, int i1, int j0, int j1)
	{
		Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
		double[][] B = X.getArray();
		try {
			for (int i = i0; i <= i1; i++) {
				for (int j = j0; j <= j1; j++) {
					B[i - i0][j - j0] = _matrix[i][j];
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new ArrayIndexOutOfBoundsException("Submatrix indices");
		}
		return X;
	}


	/**
	 * Linear algebraic matrix multiplication, A * B
	 * @param B   another matrix
	 * @return Matrix product, A * B
	 * @exception IllegalArgumentException if matrix dimensions don't agree
	 */
	public Matrix times(Matrix B)
	{
		if (B._m != _n) {
			throw new IllegalArgumentException("Matrix inner dimensions must agree.");
		}
		Matrix X = new Matrix(_m, B._n);
		double[][] C = X.getArray();
		double[] Bcolj = new double[_n];
		for (int j = 0; j < B._n; j++) {
			for (int k = 0; k < _n; k++) {
				Bcolj[k] = B._matrix[k][j];
			}
			for (int i = 0; i < _m; i++) {
				double[] Arowi = _matrix[i];
				double s = 0;
				for (int k = 0; k < _n; k++) {
					s += Arowi[k] * Bcolj[k];
				}
				C[i][j] = s;
			}
		}
		return X;
	}

	/**
	 * Subtract the other matrix from this one
	 * @param B   another matrix
	 * @return difference this - B
	 * @exception IllegalArgumentException if matrix dimensions don't agree
	 */
	public Matrix minus(Matrix B)
	{
		if (B._m != _m || B._n != _n) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
		Matrix result = new Matrix(_m, _n);
		for (int i = 0; i < _m; i++) {
			for (int j = 0; j < _n; j++) {
				result.setValue(i, j, get(i, j) - B.get(i, j));
			}
		}
		return result;
	}

	/**
	 * Divide each element of this matrix by the corresponding element in the other one
	 * @param B   another matrix
	 * @return this[i,j]/other[i,j]
	 * @exception IllegalArgumentException if matrix dimensions don't agree
	 */
	public Matrix divideEach(Matrix B)
	{
		if (B._m != _m || B._n != _n) {
			throw new IllegalArgumentException("Matrix dimensions must agree.");
		}
		Matrix result = new Matrix(_m, _n);
		for (int i = 0; i < _m; i++) {
			for (int j = 0; j < _n; j++) {
				result.setValue(i, j, get(i, j) / B.get(i, j));
			}
		}
		return result;
	}

	/**
	 * Solve A*X = B
	 * @param B   right hand side
	 * @return least squares solution
	 */
	public Matrix solve(Matrix B) {
		return new QRDecomposition(this).solve(B);
	}

	/**
	 * @return the average absolute value of all the elements in the matrix
	 */
	public double getAverageAbsValue()
	{
		double total = 0.0;
		for (int i = 0; i < _m; i++) {
			for (int j = 0; j < _n; j++) {
				total += Math.abs(_matrix[i][j]);
			}
		}
		return total / _m / _n;
	}

	/**
	 * Primitive output for debugging
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append('(');
		for (int i = 0; i < _m; i++) {
			builder.append('(');
			for (int j = 0; j < _n; j++) {
				builder.append((_matrix[i][j]));
				builder.append(", ");
			}
			builder.append(") ");
		}
		builder.append(')');
		return builder.toString();
	}
}
