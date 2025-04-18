import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.LSODA.LSODACommon;
import org.simulator.math.odes.LSODA.LSODAContext;
import org.simulator.math.odes.LSODA.LSODAIntegrator;
import org.simulator.math.odes.LSODA.LSODAOptions;
import java.util.logging.Logger;

public class LSODAIntegratorTest {

    private static final Logger logger = Logger.getLogger(LSODAIntegratorTest.class.getName());

    LSODACommon common = new LSODACommon();
    LSODAOptions opt = new LSODAOptions();
    LSODAContext ctx = new LSODAContext(common, opt);
    LSODAIntegrator integrator = new LSODAIntegrator(ctx);

    /**
     * The following tests are for the function .ddot() within LSODAIntegrator.java
     * The function calculates the dot product of two vectors.
    **/

    /** Basic dot product test, incx = incy = 1 */
    @Test
    void ddotBasicDotProduct() {
        logger.info("Running: ddotBasicDotProduct");

        int n = 3;
        double[] dx = {0d, 1.0, 2.0, 3.0}; // skip index 0
        double[] dy = {0d, 4.0, 5.0, 6.0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(32.0, result, 1e-6, "Dot product should be 32.0");
    }

    /** Test vector with length = 0 */
    @Test
    void ddotZeroLengthVector() {
        logger.info("Running: ddotZeroLengthVector");

        int n = 0;
        double[] dx = {0};
        double[] dy = {0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0.0, result, 1e-6, "Dot product for zero-length vectors should be 0.");
    }

    /** Test where n < 0, the result should be 0 */
    @Test
    void ddotNegativeN() {
        logger.info("Running: ddotNegativeN");

        int n = -5;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 2d, 4d, 6d, 8d, 10d};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0.0, result, 1e-6, "Dot product for negative n should be 0.");
    }

    /** Test different values for incx and incy */
    @Test
    void ddotDifferentStrides() {
        logger.info("Running: ddotDifferentStrides");

        int n = 3;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        double expected = 1d * 5d + 3d * 6d + 5d * 7d;
        assertEquals(expected, result, 1e-6, "Dot product with different strides should be correct");
    }

    /** Test with incx = incy = -1 */
    @Test
    void ddotNegativeStrides() {
        logger.info("Running: ddotNegativeStrides");

        int n = 3;
        double[] dx = {0d, 3d, 2d, 1d};  // index 1 to 3 used
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1;
        int incy = -1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        double expected = 3d * 6d + 2d * 5d + 1d * 4d;
        assertEquals(expected, result, 1e-6, "Dot product with negative strides should be correct");
    }

    /** Test with all vector values = 0 */
    @Test
    void ddotAllZero() {
        logger.info("Running: ddotAllZero");

        int n = 3;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 0d, 0d, 0d};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0.0, result, 1e-6, "Dot product of zero vectors should be 0");
    }

    /** Test with a single-element vector */
    @Test
    void ddotSingleElement() {
        logger.info("Running: ddotSingleElement");

        int n = 1;
        double[] dx = {0d, 7.5};
        double[] dy = {0d, 2.0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(15.0, result, 1e-6, "Dot product of single-element vectors should be correct");
    }

    /** Test with NaN values */
    @Test
    void ddotNaN() {
        logger.info("Running: ddotNaN");

        int n = 2;
        double[] dx = {0d, Double.NaN, 2.0};
        double[] dy = {0d, 5.0, Double.NaN};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertTrue(Double.isNaN(result), "Dot product with NaN should return NaN");
    }


    /** 
     * The following tests are for the function .vmnorm within LSODAIntegrator.java 
     * The function calculates the weighted max-norm of a vector.
    **/

    /** Basic max-norm test */
    @Test
    void vmnormBasicMaxNorm() {
        logger.info("Running: vmnormBasicMaxNorm");

        int n = 3;
        double[] v = {0d, 1d, -2d, 3d};
        double[] w = {0d, 4d, 1.5, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        double expected = Math.max(Math.abs(1d) * 4d, Math.max(Math.abs(-2d) * 1.5, Math.abs(3d) * 2.0));
        assertEquals(expected, result, 1e-6, "Max-norm result should match expected value");
    }

    /** Test vector with all elements zero */
    @Test
    void vmnormAllZero() {
        logger.info("Running: vmnormAllZero");

        int n = 3;
        double[] v = {0d, 0d, 0d, 0d};
        double[] w = {0d, 0d, 0d, 0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6, "Max-norm of all-zero vectors should be 0");
    }

    /** Test single element */
    @Test
    void vmnormSingleElement() {
        logger.info("Running: vmnormSingleElement");

        int n = 1;
        double[] v = {0d, 7.5};
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(15d, result, 1e-6, "Max-norm of single-element vector should be abs(v[1]) * w[1]");
    }

    /** Test negative values in v */
    @Test
    void vmnormNegativeValues() {
        logger.info("Running: vmnormNegativeValues");

        int n = 3;
        double[] v = {0d, -1d, -2d, -3d};
        double[] w = {0d, 1d, 2d, 3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        double expected = 3d * 3d;
        assertEquals(expected, result, 1e-6, "Max-norm with negative v values should still be positive");
    }

    /** Test negative values in w */
    @Test
    void vmnormNegativeWeights() {
        logger.info("Running: vmnormNegativeWeights");

        int n = 3;
        double[] v = {0d, 1d, 2d, 3d};
        double[] w = {0d, -1d, -2d, -3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        double expected = 3d * 3d;
        assertEquals(expected, result, 1e-6, "Max-norm with negative weights should use abs values");
    }

    /** Test zero-length vector */
    @Test
    void vmnormZeroLengthVector() {
        logger.info("Running: vmnormZeroLengthVector");

        int n = 0;
        double[] v = {0d};
        double[] w = {0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6, "Max-norm of zero-length vector should be 0");
    }


    // /** 
    //  * The following tests are for the function .fnorm() within LSODAIntegrator.java
    //  * The function computes the weighted matrix norm 
    // **/

    /** Basic matrix norm */
    @Test
    void fnormBasicMatrixNorm() {
        logger.info("Running: fnormBasicMatrixNorm");

        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 1d, -2d},
            {0d, 3d, 4d}
        };
        double[] w = {0d, 2d, 1d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        double expected = Math.max(
            2d * (Math.abs(1d) / 2d + Math.abs(-2d) / 1d),
            1d * (Math.abs(3d) / 2d + Math.abs(4d) / 1d)
        );

        assertEquals(expected, result, 1e-6, "Weighted matrix norm should match expected value");
    }

    /** Matrix has all zero elements */
    @Test
    void fnormAllZeroMatrix() {
        logger.info("Running: fnormAllZeroMatrix");

        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 0d, 0d},
            {0d, 0d, 0d}
        };
        double[] w = {0d, 2d, 1d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(0d, result, 1e-6, "Matrix norm should be 0 for all-zero matrix");
    }

    /** Matrix has a single element (n = 1) */
    @Test
    void fnormSingleElementMatrix() {
        logger.info("Running: fnormSingleElementMatrix");

        int n = 1;
        double[][] a = {
            {0d, 0d},
            {0d, -7.5}
        };
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(7.5, result, 1e-6, "Single element matrix norm should be absolute value of the element");
    }

    /** Matrix has negative values */
    @Test
    void fnormNegativeValues() {
        logger.info("Running: fnormNegativeValues");

        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, -1d, -2d},
            {0d, -3d, -4d}
        };
        double[] w = {0d, 1d, 2d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        double expected = Math.max(
            1d * (Math.abs(-1d) / 1d + Math.abs(-2d) / 2d),
            2d * (Math.abs(-3d) / 1d + Math.abs(-4d) / 2d)
        );

        assertEquals(expected, result, 1e-6, "Matrix norm with negative values should be correctly calculated");
    }

    /** Negative weights in w */
    @Test
    void fnormNegativeWeights() {
        logger.info("Running: fnormNegativeWeights");

        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 1d, -2d},
            {0d, 3d, 4d}
        };
        double[] w = {0d, -1d, -2d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertTrue(result > 0, "Matrix norm should be positive even with negative weights");
    }

    /** Zero length matrix (n = 0) */
    @Test
    void fnormZeroLengthMatrix() {
        logger.info("Running: fnormZeroLengthMatrix");

        int n = 0;
        double[][] a = {{0d}};
        double[] w = {0d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(0d, result, 1e-6, "Zero-length matrix should yield 0 as norm");
    }

    /**
     * The following tests are for the function .idamax() within LSODAIntegrator.java
     * The function finds the index of the element with the maximum absolute value.
     * 
     * <p>
     * This test uses a simple case where the vector (assumed to be 1-indexed, with the element at index 0 unused) 
     * contains three values. The test verifies that the function returns the smalles index where the maximum absolute 
     * value occurs.
     * </p>
     * 
     * <p>
     * Preconditions:
     * <ul>
     *   <li>The input array must have a dummy element at index 0.</li>
     *   <li>The number of elements <code>n</code> corresponds to the number of valid elements</li>
     *   <li><code>incx</code> is positive and typically equal to 1.</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Expected Output:
     *  <ul>
     *      <li>If the vector is {unused, 1.0, -3.0, 2.0} with <code>n = 3</code> and the largest absolute value element at index 2 (i.e. -3.0), this index has the maximum absolute value of 3.0.</li>
     *  </ul>
     * </p>
     * 
     * @see LSODAIntegrator#idamax(int, double[], int)
     * 
     */
    @Test
    void idamaxBasic() {
        logger.info("Running: idamaxBasic");

        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
        int incx = 1;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return the index of the element with the maximum absolute value");
    }

    /**
     * Tests the idamax function for an empty vector scenario.
     * 
     * <p>
     * This test case verifies that when the number of elements <code>n</code> is 0, the function returns 0, indicating that no valid index exists.
     * The vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused).
     * </p>
     * 
     * <p>
     * Preconditions:
     * <ul>
     *  <li><code>n</code> is set to 0, meaning there are no valid elements to examine.</li>
     * <li>The array <code>dx</code> is provided with a dummy value at index 0.</li>
     * <li><code>incx</code> is positive (here <code>incx = 1</code>).</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Expected Output:
     * <ul>
     *  <li>The function should return 0, indicating that no index is valid since <code>n = 0</code>.</li>
     * </ul>
     * </p>
     * 
     * @see LSODAIntegrator#idamax(int, double[], int)
     */
    @Test
    void idamaxEmptyVector() {
        logger.info("Running: idamaxEmptyVector");

        int n = 0;
        double[] dx = {0d, 1d, 2d};
        int incx = 1;

        int expected = 0;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return 0 for an empty vector (n = 0)");
    }

    /**
     * Tests the idamax function for a single-element vector.
     * 
     * <p>
     * This test case verifies that when the number of elements <code>n</code> is 1, the function correctly returns 1, as the only valid element (at index 1) is trivially the one with the maximum absolute value.
     * </p>
     * 
     * <p>
     * Preconditions:
     * <ul>
     *  <li><code>n</code> is set to 1, indicating that there is esactly one element to examine</li>
     *  <li>The vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused).</li>
     *  <li><code>incx</code> is positive (in this test, <code>incx = 1</code>).</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Expected Output:
     * <ul>
     *  <li>The function should return 1, confirming that the first and only element is the maximum</li>
     * </ul>
     * </p>
     * 
     * @see LSODAIntegrator#idamax(int, double[], int)#
     * 
     */
    @Test
    void idamaxSingleElement() {
        logger.info("Running: idamaxSingleElement");

        int n = 1;
        double[] dx = {0d, 3.14};
        int incx = 1;

        int expected = 1;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return 1 for a single-element vector");
    }

    /**
     * Tests the idamax function for a vector with multiple elements and a unitary increment.
     * 
     * <p>
     * This test case verifies that when the number of elem,ents <code>n</code> is greater than 1 and the increment (<code>incx</code>) is 1, the function correctly identifies the index of the element with the maximum absolute value.
     * </p>
     * 
     * <p>
     * In this test, the vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused) and contains three valid elements: 1.0, -3.0 and 2.0. Thus, the element with the maximum absolute value is -3.0 at index 2.
     * </p>
     * 
     * <p>
     * Preconditions:
     * <ul>
     *  <li><code>n</code> is 3, indicating three valid elements are present in the vector.</li>
     *  <li>The array <code>dx</code> is 1-indexed, meaning the element at index 0 is not used.</li>
     *  <li><code>incx</code> is 1, so the elements are accessed consecutively without any jumps.</li>
     * </ul>
     * </p>
     * 
     * <p>
     * Expected Output:
     * <ul>
     *  <li>The function shuold return 2, confirming that the second element has the maximum absolute value.</li>
     * </ul>
     * <p>
     * 
     * @see LSODAIntegrator#idamax(int, double[], int)
     * 
     */
    @Test
    void idamaxMultipleElementsIncxOne() {
        logger.info("Running: idamaxMultipleElementsIncxOne");

        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};  // 1-indexed array
        int incx = 1;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return 2 for max(abs) at index 2");
    }

    /**
     * Tests the idamax function with multiple elements using a non-unitary increment (incx = 2).
     *
     * <p>
     * This test case verifies that when the vector is traversed with an increment other than 1, 
     * the function correctly identifies the index (1-indexed) of the element with the maximum absolute value.
     * </p>
     *
     * <p>
     * In this test, the vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused).
     * The valid elements, accessed using an increment of 2 (every second element is skipped), are:
     * <ul>
     *   <li><code>dx[1] = 1.0</code></li>
     *   <li><code>dx[3] = -4.0</code></li>
     *   <li><code>dx[5] = 3.0</code></li>
     * </ul>
     * Their absolute values are 1.0, 4.0, and 3.0 respectively, so the element with the maximum absolute
     * value is -4.0, which is the second valid element.
     * </p>
     *
     * <p>
     * Preconditions:
     * <ul>
     *   <li><code>n</code> is set to 3, meaning three valid elements are expected.</li>
     *   <li>The array <code>dx</code> is properly sized for the given increment (<code>incx = 2</code>).</li>
     *   <li>The element at index 0 of <code>dx</code> is unused.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Expected Outcome:
     * <ul>
     *   <li>The function should return 2, indicating that the second valid element has the maximum absolute value.</li>
     * </ul>
     * </p>
     *
     * @see LSODAIntegrator#idamax(int, double[], int)
     */
    @Test
    void idamaxMultipleElementsIncxTwo() {
        logger.info("Running: idamaxMultipleElementsIncxTwo");

        int n = 3;
        double[] dx = {0d, 1d, 0d, -4d, -5d, 3d, 3d};  // 1-indexed with incx = 2
        int incx = 2;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return 2 for max(abs) at dx[3] = -4.0");
    }

    /**
     * Tests the idamax function with a negative increment value.
     *
     * <p>
     * This test case verifies that when the increment <code>incx</code> is negative, the function correctly handles this scenario by returning 1. According to the specification, if <code>n &lt;= 1</code> or <code>incx</code> is non-positive, the function should default to 1.
     * </p>
     *
     * <p>
     * Preconditions:
     * <ul>
     *   <li><code>n</code> is set to 3, indicating three elements are intended to be examined.</li>
     *   <li>The vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused).</li>
     *   <li><code>incx</code> is negative (<code>incx = -1</code>)</li>
     * </ul>
     * </p>
     *
     * <p>
     * Expected Outcome:
     * <ul>
     *   <li>The function should return 1, as the negative increment triggers the default behavior.</li>
     * </ul>
     * </p>
     *
     * @see LSODAIntegrator#idamax(int, double[], int)
     */
    @Test
    void idamaxNegativeIncrement() {
        logger.info("Running: idamaxNegativeIncrement");

        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d}; // 1-indexed
        int incx = -1;

        int expected = 1;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return 1 for negative increment (fallback behavior)");
    }

    /**
    * Tests the idamax function with multiple elements using an increment of 3.
    *
    * <p>
    * This test case verifies that when the vector is traversed with an increment of 3, the function correctly identifies the index (1-indexed) of the element with the maximum absolute value.
    * The vector <code>dx</code> is assumed to be 1-indexed (i.e., <code>dx[0]</code> is unused).
    * </p>
    *
    * <p>
    * In this test, <code>n</code> is 4 and the valid elements are located at indices determined by:
    * <code>index = 1, 1+3, 1+6, 1+9</code> which correspond to:
    * <ul>
    *   <li><code>dx[1] = 2.0</code></li>
    *   <li><code>dx[4] = -5.0</code></li>
    *   <li><code>dx[7] = 3.0</code></li>
    *   <li><code>dx[10] = -5.0</code></li>
    * </ul>
    * Although the maximum absolute value (5.0) occurs at two positions, the function is expected to return the index of the first occurrence, which is 2.
    * </p>
    *
    * <p>
    * Preconditions:
    * <ul>
    *   <li><code>n</code> is 4, indicating that there are four valid elements.</li>
    *   <li>The array <code>dx</code> is sized appropriately for the given increment (<code>incx = 3</code>),
    *       with a dummy element at index 0.</li>
    *   <li>The elements are accessed with a step size of 3.</li>
    * </ul>
    * </p>
    *
    * <p>
    * Expected Outcome:
    * <ul>
    *   <li>The function should return 2, indicating that the second valid element has the first maximum absolute value.</li>
    * </ul>
    * </p>
    *
    * @see LSODAIntegrator#idamax(int, double[], int)
    */
    @Test
    void idamaxMultipleElementsIncxThree() {
        logger.info("Running: idamaxMultipleElementsIncxThree");

        int n = 4;
        double[] dx = {0d, 2d, 0d, 0d, -5d, 0d, 0d, 3d, 0d, 0d, -5d, 0d, 0d}; // 1-indexed
        int incx = 3;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);

        assertEquals(expected, result, "idamax should return index of element with max absolute value when increment is 3");
    }

    /**
    * The following tests are for the function <code>.prja()</code> within LSODAIntegrator.java
    * The function computes and processes the matrix using finite differencing, calls <code>vmonrm()</code> to calculate the norm of the Jacobian, calls <code>fnorm()</code> to compute the norm of, and performs LU decomposition using <code>dgefa()</code>.
    * 
    */
    @Test
    void prjaBasic() {
        logger.info("Running: prjaBasic");

        ctx.setNeq(3);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 1d, 2d, 3d};
        common.setSavf(newSavf);
        double[][] newWm = new double[4][4];
        common.setWm(newWm);
        double[] newAcor = new double[]{0d, 0.2, 0.2, 0.3};
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d, 1d, 2d, 3d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, result);
        logger.info("prjaBasic completed successfully.");
    }

    @Test
    void prjaMiterNotTwo() {
        logger.info("Running: prjaMiterNotTwo");

        ctx.setNeq(3);
        common.setMiter(5);

        double[] y = {0d, 1d, 2d, 3d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(0, result);
        logger.info("prjaMiterNotTwo completed with result 0 (miter not equal to 2).");
    }


    @Test
    void prjaZeroLengthSystem() {
        logger.info("Running: prjaZeroLengthSystem");

        common.setMiter(2);
        ctx.setNeq(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, result);
        logger.info("prjaZeroLengthSystem completed successfully (zero-length system).");
    }

    @Test
    void prjaAllZero() {
        logger.info("Running: prjaAllZero");

        ctx.setNeq(3);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 0d, 0d, 0d};
        common.setSavf(newSavf);
        double[][] newWm = new double[4][4];
        common.setWm(newWm);
        double[] newAcor = new double[]{0d, 0d, 0d, 0d};
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d, 0d, 0d, 0d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, result);
        logger.info("prjaAllZero completed successfully (all zero values).");
    }

    @Test
    void prjaFunctionEvaluationsIncrement() {
        logger.info("Running: prjaFunctionEvaluationsIncrement");

        ctx.setNeq(3);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 1d, 2d, 3d};
        common.setSavf(newSavf);
        double[][] newWm = new double[4][4];
        common.setWm(newWm);
        double[] newAcor = new double[]{0d, 0.1, 0.2, 0.3};
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d, 1d, 2d, 3d};
        LSODAIntegrator.prja(ctx, y);

        assertEquals(3, common.getNfe());
        logger.info("prjaFunctionEvaluationsIncrement completed with 3 function evaluations.");
    }

    /**
     * The following test cases are for the function daxpy() within LSODAIntegrator.java.
     * Daxpy computes <pre>dy = da * dx + dy</pre> for vectors <pre>dx</pre> and <pre>dy</pre> and scalar <pre>da</pre>.
     */
    @Test
    void daxpyBasic() {
        logger.info("Running: daxpyBasic");

        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 6d, 9d, 12d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyBasic completed successfully.");
    }

    @Test
    void daxpyZeroScalarMultiplier() {
        logger.info("Running: daxpyZeroScalarMultiplier");

        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyZeroScalarMultiplier completed successfully (no modification).");
    }

    @Test
    void daxpyNegativeN() {
        logger.info("Running: daxpyNegativeN");

        int n = -5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyNegativeN completed successfully (no modification with negative n).");
    }

    @Test
    void daxpyNonUnitaryIncrements() {
        logger.info("Running: daxpyNonUnitaryIncrements");

        int n = 2;
        double da = 3d;
        double[] dx = {0d, 1d, 2d, 3d, 4d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 8d, 15d, 7d, 8d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyNonUnitaryIncrements completed successfully.");
    }

    @Test
    void daxpyNegativeStrides() {
        logger.info("Running: daxpyNegativeStrides");

        int n = 3;
        double da = 2d;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1, incy = -1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 12d, 9d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyNegativeStrides completed successfully.");
    }

    @Test
    void daxpyAllZeroDx() {
        logger.info("Running: daxpyAllZeroDx");

        int n = 3;
        double da = 2d;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyAllZeroDx completed successfully (dx is zero).");
    }

    @Test
    void daxpyLargeValues() {
        logger.info("Running: daxpyLargeValues");

        int n = 3;
        double da = 1e9d;
        double[] dx = {0d, 1e9d, -2e9d, 3e9d};
        double[] dy = {0d, 1e9d, 2e9d, -3e9d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 1.000000001e18, -1.999999998e18, 2.999999997e18};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyLargeValues completed successfully (large values).");
    }

    @Test
    void daxpyInPlaceModification() {
        logger.info("Running: daxpyInPlaceModification");

        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = dx.clone();
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 3d, 6d, 9d};
        assertArrayEquals(expectedDy, dy, 1e-6);
        logger.info("daxpyInPlaceModification completed successfully (in-place modification).");
    }

    /**
     * The following tests are for the functions dgefa() within LSODAIntegrator
     */
    @Test
    void dgefaBasic() {
        logger.info("Running: dgefaBasic");

        int n = 3;
        int[] ipvt = new int[4];
        int[] info = {0};
        double[][] a = {
            {0, 0, 0, 0},
            {0, 4, 3, 2},
            {0, 2, 3, 1},
            {0, 1, 1, 2}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(0, info[0]);
        logger.info("dgefaBasic completed successfully.");
    }

    @Test
    void dgefaUpperTriangular() {
        logger.info("Running: dgefaUpperTriangular");

        int n = 3;
        int[] ipvt = new int[4];
        int[] info = {0};
        double[][] a = {
            {0d, 0d, 0d, 0d},
            {0d, 3d, 1d, 2d},
            {0d, 0d, 4d, 5d},
            {0d, 0d, 0d, 6d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(0, info[0]);
        logger.info("dgefaUpperTriangular completed successfully.");
    }

    @Test
    void dgefaSingularMatrix() {
        logger.info("Running: dgefaSingularMatrix");

        int n = 3;
        int[] ipvt = new int[4];
        int[] info = {0};
        double[][] a = {
            {0d, 0d, 0d, 0d},
            {0d, 1d, 2d, 3d},
            {0d, 4d, 5d, 6d},
            {0d, 0d, 0d, 0d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(3, info[0]);
        logger.info("dgefaSingularMatrix completed successfully.");
    }

    @Test
    void dgefaIdentityMatrix() {
        logger.info("Running: dgefaIdentityMatrix");

        int n = 3;
        int[] ipvt = new int[4];
        int[] info = {0};
        double[][] a = {
            {0d, 0d, 0d, 0d},
            {0d, 1d, 0d, 0d},
            {0d, 0d, 1d, 0d},
            {0d, 0d, 0d, 1d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(0, info[0]);
        assertEquals(1, ipvt[1]);
        assertEquals(2, ipvt[2]);
        assertEquals(3, ipvt[3]);
        logger.info("dgefaIdentityMatrix completed successfully.");
    }

    @Test
    void dgefaRowSwapping() {
        logger.info("Running: dgefaRowSwapping");

        int n = 3;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];
        double[][] a = {
            {0, 0, 0, 0},
            {0, 2, 1, 1},
            {0, 4, 3, 3},
            {0, 6, 5, 5}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(0, info[0]);
        assertEquals(3, ipvt[1]);
        assertEquals(2, ipvt[2]);
        assertEquals(3, ipvt[3]);
        logger.info("dgefaRowSwapping completed successfully.");
    }


    /**
     * The following test cases are for the function dscal() within LSODAIntegrator
     */
    @Test
    void dscalBasic() {
        logger.info("Running: dscalBasic");

        int n = 5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 2d, 4d, 6d, 8d, 10d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalBasic completed successfully.");
    }

    @Test
    void dscalZeroLengthVector() {
        logger.info("Running: dscalZeroLengthVector");

        int n = 0;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalZeroLengthVector completed successfully.");
    }

    @Test
    void dscalNegativeN() {
        logger.info("Running: dscalNegativeN");

        int n = -3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalNegativeN completed successfully.");
    }

    @Test
    void dscalUnitaryScalar() {
        logger.info("Running: dscalUnitaryScalar");

        int n = 3;
        double da = 1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalUnitaryScalar completed successfully.");
    }

    @Test
    void dscalZeroScalar() {
        logger.info("Running: dscalZeroScalar");

        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 0d, 0d, 0d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalZeroScalar completed successfully.");
    }

    @Test
    void dscalNegativeScalar() {
        logger.info("Running: dscalNegativeScalar");

        int n = 3;
        double da = -1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, -1d, -2d, -3d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalNegativeScalar completed successfully.");
    }

    @Test
    void dscalFractionalScalar() {
        logger.info("Running: dscalFractionalScalar");

        int n = 3;
        double da = 0.5;
        double[] dx = {0d, 2d, 4d, 6d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
        logger.info("dscalFractionalScalar completed successfully.");
    }

    /** The following tests are for the function solsy() within LSODAIntegrator */
    
    /* Test that solsy() calls dgesl() when miter == 2 */
    @Test
    void solsyBasicLUDecomposition() {
        logger.info("Running: solsyBasicLUDecomposition");

        ctx.setNeq(3);
        common.setMiter(2);
        double[] y = {0d, 1d, 2d, 3d};
        double[][] wm = {
            {0, 0, 0, 0},
            {0, 1, 2, 3},
            {0, 4, 5, 6},
            {0, 7, 8, 9}
        };
        common.setWm(wm);
        int[] ipvt = {0, 1, 2, 3};
        common.setIpvt(ipvt);

        int result = LSODAIntegrator.solsy(ctx, y);
        assertEquals(1, result);
        logger.info("solsyBasicLUDecomposition completed successfully.");
    }

    @Test
    void solsyMiterNotTwo() {
        logger.info("Running: solsyMiterNotTwo");

        ctx.setNeq(3);
        common.setMiter(5);
        double[] y = {0d, 1d, 2d, 3d};
        double[][] wm = {
            {0, 0, 0, 0},
            {0, 1, 2, 3},
            {0, 4, 5, 6},
            {0, 7, 8, 9}
        };
        common.setWm(wm);
        int[] ipvt = {0, 1, 2, 3};
        common.setIpvt(ipvt);

        assertThrows(IllegalStateException.class, () -> {
            LSODAIntegrator.solsy(ctx, y);
        });
        logger.info("solsyMiterNotTwo completed successfully.");
    }

    @Test
    void solsyZeroNeq() {
        logger.info("Running: solsyZeroNeq");

        ctx.setNeq(0);
        common.setMiter(2);
        double[] y = new double[0];

        double[][] wm = new double[1][1];
        int[] ipvt = new int[1];
        common.setWm(wm);
        common.setIpvt(ipvt);

        int result = LSODAIntegrator.solsy(ctx, y);
        assertEquals(1, result);
        logger.info("solsyZeroNeq completed successfully.");
    }


    /** The following tests are for the function dgesl() within LSODAIntegrator */

    /**
     * Test solving a simple 2x2 system Ax = b
     * A = [2 1]
     *     [5 7]
     * b = [11, 13]
     * Solution should be x = [3.4285..., -2.0714...]
     */
    @Test
    void dgeslBasic() {
        int numberOfEquations = 2;
        double[][] coefficientMatrix = {
            {0d, 0d, 0d},
            {0d, 2d, 1d},
            {0d, 5d, 7d}
        };
        int[] pivotArray = {0, 1, 2};
        double[] rightHandSide = {0d, 11d, 13d};

        logger.info("Solving system: A = [[2, 1], [5, 7]], b = [11, 13]. Expected solution: x = [3.4285714286, -2.0714285714].");

        LSODAIntegrator.dgesl(coefficientMatrix, numberOfEquations, pivotArray, rightHandSide, 0);

        assertArrayEquals(new double[]{0d, 3.4285714286, -2.0714285714}, rightHandSide, 1e-10);
    }

    /**
     * Test solving A^T * x = b for a simple 2x2 system with row swaps and transposition.
     * A = [2 1]
     *     [5 7]
     * b = [11, 13]
     * Solution should be x ≈ [-3.07, 0.343]
     */
    @Test
    void dgeslTransposeSolve() {
        int numberOfEquations = 2;
        double[][] coefficientMatrix = {
            {0d, 0d, 0d},
            {0d, 2d, 1d},
            {0d, 5d, 7d}
        };
        int[] pivotArray = {0, 1, 2};
        double[] rightHandSide = {0d, 11d, 13d};

        logger.info("Solving for A^T * x = b: A = [[2, 1], [5, 7]], b = [11, 13]. Expected solution: x ≈ [-3.07, 0.343].");

        LSODAIntegrator.dgesl(coefficientMatrix, numberOfEquations, pivotArray, rightHandSide, 1);

        assertArrayEquals(new double[]{0d, -3.0714285714285716, 3.4285714285714284}, rightHandSide, 1e-6, "Incorrect solution for A^T * x = b");
    }

    /** solving for a larger 4x4 system */
    @Test
    void dgeslSingularMatrix() {
        int numberOfEquations = 2;
        double[][] coefficientMatrix = {
            {0d, 0d, 0d},
            {0d, 1d, 2d},
            {0d, 0d, 0d}
        };
        int[] pivotArray = {0, 1, 2};
        double[] rightHandSide = {0d, 3d, 6d};

        assertThrows(IllegalArgumentException.class, () -> {
            LSODAIntegrator.dgesl(coefficientMatrix, numberOfEquations, pivotArray, rightHandSide, 0);
        }, "Matrix is singular or nearly singular at index 2");
    }

    /* Solving for an identity matrix */
    @Test
    void dgeslIdentityMatrix() {
        double[][] coefficientMatrix = {
            {0d, 0d, 0d},
            {0d, 1d, 0d},
            {0d, 0d, 1d}
        };
        int numberOfEquations = 2;
        int[] pivotArray = {0, 1, 2};
        double[] rightHandSide = {0d, 3d, 4d};

        logger.info("Solving for identity matrix: A = [[1, 0], [0, 1]], b = [3, 4]. Expected solution: x = [3, 4].");

        LSODAIntegrator.dgesl(coefficientMatrix, numberOfEquations, pivotArray, rightHandSide, 0);

        assertArrayEquals(new double[]{0d, 3d, 4d}, rightHandSide, 1e-6);
    }

    /** The following tests are for the function corfailure() within LSODAIntegrator */
    @Test
    void corfailureBasic() {
        ctx.setNeq(3);
        opt.setHmin(0.1);
        common.setH(1d);
        common.setNq(2);
        common.setNcf(0);
        common.setMiter(3);

        double[][] yh = {
            {0d, 0d, 0d, 0d},
            {0d, 10d, 20d, 30d},
            {0d, 1d, 2d, 3d},
            {0d, 0.5, 1d, 1.5}
        };
        common.setYh(yh);
        double told = 5d;

        logger.info("Testing corfailure with neq = 3, h = 1.0, nq = 2, told = 5.0");

        int result = LSODAIntegrator.corfailure(ctx, told);
        double[][] expected = {
            {0d, 0d, 0d, 0d},
            {0d, 9.5, 19d, 28.5},
            {0d, 0d, 0d, 0d},
            {0d, 0.5, 1d, 1.5}
        };

        assertEquals(1, result);
        assertEquals(1, common.getNcf());
        assertEquals(2d, common.getRmax());
        assertEquals(told, common.getTn());
        assertEquals(common.getMiter(), common.getIpup());

        for (int row = 1; row <= 3; row++) {
            for (int col = 1; col <= 3; col++) {
                assertEquals(expected[row][col], common.getYh()[row][col], 1e-9);
            }
        }
    }

    @Test
    void corfailureHTooSmall() {
        ctx.setNeq(2);
        opt.setHmin(0.1);
        common.setH(0.1 * 1.00001 - 1e-9);
        common.setNq(1);
        common.setNcf(0);
        common.setMiter(4);

        double[][] yh = {
            {0d, 0d, 0d},
            {0d, 5d, 6d},
            {0d, 2d, 3d}
        };
        common.setYh(yh);

        double told = 2.5;
        logger.info("Testing corfailure with h too small: h = " + common.getH() + ", told = " + told);

        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(2, result);
        assertEquals(1, common.getNcf());
    }

    @Test
    void corfailureNcfLimit() {
        ctx.setNeq(2);
        opt.setHmin(0.1);
        common.setH(1.0);
        common.setNq(1);
        common.setNcf(common.MXNCF - 1);
        common.setMiter(5);

        double[][] yh = {
            {0d, 0d, 0d},
            {0d, 3d, 4d},
            {0d, 1d, 2d}
        };
        common.setYh(yh);

        double told = 7.0;
        logger.info("Testing corfailure with Ncf limit: Ncf = " + common.getNcf() + ", told = " + told);

        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(2, result);
        assertEquals(common.MXNCF, common.getNcf());
    }

    @Test
    void corfailureZeroNq() {
        ctx.setNeq(2);
        opt.setHmin(0.1);
        common.setH(1.0);
        common.setNq(0);
        common.setNcf(0);
        common.setMiter(6);

        double[][] yh = new double[1][3];
        common.setYh(yh);

        double told = 10.0;
        logger.info("Testing corfailure with zero Nq: Nq = 0, told = " + told);

        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(1, result);
        assertEquals(common.getMiter(), common.getIpup());
    }

    /** The following tests are for checkOpt() within LSODAIntegrator.java */
    @Test
    void checkOptBasicCaseState0() {
        logger.info("Starting test: checkOptBasicCaseState0");

        ctx.setState(0);
        ctx.setNeq(3);

        opt.setRtol(new double[]{1e-4, 1e-4, 1e-4});
        opt.setAtol(new double[]{1e-6, 1e-6, 1e-6});
        opt.setItask(0);
        opt.setIxpr(1);
        opt.setMxstep(0);
        opt.setMxhnil(0);
        opt.setMxordn(99);
        opt.setMxords(99);
        opt.setHmax(2.0);
        opt.setHmin(0.01);

        boolean result = LSODAIntegrator.checkOpt(ctx, opt);

        assertTrue(result);
        assertEquals(1, ctx.getState());
        assertEquals(0d, opt.getH0());
        assertEquals(1, opt.getItask());
        assertEquals(500, opt.getMxstep());
        assertEquals(12, opt.getMxordn());
        assertEquals(5, opt.getMxords());
        assertEquals(0.5, opt.getHmxi(), 1e-9);
        logger.info("Test checkOptBasicCaseState0 passed");
    }

    @Test
    void checkOptBasicCaseState3() {
        logger.info("Starting test: checkOptBasicCaseState3");

        ctx.setState(3);
        ctx.setNeq(2);

        opt.setRtol(new double[]{1e-4, 1e-4});
        opt.setAtol(new double[]{1e-6, 1e-6});
        opt.setItask(1);
        opt.setIxpr(0);
        opt.setMxstep(10);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        boolean result = LSODAIntegrator.checkOpt(ctx, opt);

        assertTrue(result);
        assertEquals(1.0, opt.getHmxi(), 1e-9);
        logger.info("Test checkOptBasicCaseState3 passed");
    }

    @Test
    void checkOptNeqZero() {
        logger.info("Starting test: checkOptNeqZero");

        ctx.setState(1);
        ctx.setNeq(0);

        opt.setRtol(new double[]{});
        opt.setAtol(new double[]{});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNeqZero passed");
    }

    @Test
    void checkOptNegativeRtol() {
        logger.info("Starting test: checkOptNegativeRtol");

        ctx.setState(1);
        ctx.setNeq(2);

        opt.setRtol(new double[]{-1e-4, 1e-4});
        opt.setAtol(new double[]{1e-6, 1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeRtol passed");
    }

    @Test
    void checkOptNegativeAtol() {
        logger.info("Starting test: checkOptNegativeAtol");

        ctx.setState(1);
        ctx.setNeq(2);

        opt.setRtol(new double[]{1e-4, 1e-4});
        opt.setAtol(new double[]{1e-6, -1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeAtol passed");
    }

    @Test
    void checkOptIllegalItaskHigh() {
        logger.info("Starting test: checkOptIllegalItaskHigh");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(6);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptIllegalItaskHigh passed");
    }

    @Test
    void checkOptIllegalItaskLow() {
        logger.info("Starting test: checkOptIllegalItaskLow");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(-1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptIllegalItaskLow passed");
    }

    @Test
    void checkOptIllegalIxprLow() {
        logger.info("Starting test: checkOptIllegalIxprLow");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(-1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptIllegalIxprLow passed");
    }

    @Test
    void checkOptIllegalIxprHigh() {
        logger.info("Starting test: checkOptIllegalIxprHigh");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(2);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptIllegalIxprHigh passed");
    }

    @Test
    void checkOptNegativeMxstep() {
        logger.info("Starting test: checkOptNegativeMxstep");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(-1);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeMxstep passed");
    }

    @Test
    void checkOptMxstepZero() {
        logger.info("Starting test: checkOptMxstepZero");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(0);
        opt.setMxhnil(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        assertEquals(500, opt.getMxstep());
        logger.info("Test checkOptMxstepZero passed");
    }

    @Test
    void checkOptNegativeMxhnil() {
        logger.info("Starting test: checkOptNegativeMxhnil");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(-1);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeMxhnil passed");
    }

    @Test
    void checkOptNegativeMxordn() {
        logger.info("Starting test: checkOptNegativeMxordn");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(-1);
        opt.setMxords(1);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeMxordn passed");
    }

    @Test
    void checkOptMxordnZero() {
        logger.info("Starting test: checkOptMxordnZero");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(0);
        opt.setMxords(1);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        assertEquals(12, opt.getMxordn());
        logger.info("Test checkOptMxordnZero passed");
    }

    @Test
    void checkOptNegativeMxords() {
        logger.info("Starting test: checkOptNegativeMxords");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(5);
        opt.setMxords(-1);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeMxords passed");
    }

    @Test
    void checkOptMxordsZero() {
        logger.info("Starting test: checkOptMxordsZero");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(5);
        opt.setMxords(0);
        opt.setHmax(1.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        assertEquals(5, opt.getMxords());
        logger.info("Test checkOptMxordsZero passed");
    }

    @Test
    void checkOptNegativeHmax() {
        logger.info("Starting test: checkOptNegativeHmax");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(5);
        opt.setMxords(3);
        opt.setHmax(-1.0);
        opt.setHmin(0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));
        logger.info("Test checkOptNegativeHmax passed");
    }

    @Test
    void checkOptHmxiCalculation() {
        logger.info("Starting test: checkOptHmxiCalculation");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(5);
        opt.setMxords(3);
        opt.setHmax(4.0);
        opt.setHmin(0.01);

        assertTrue(LSODAIntegrator.checkOpt(ctx, opt));
        assertEquals(0.25, opt.getHmxi(), 1e-9);
        logger.info("Test checkOptHmxiCalculation passed");
    }

    @Test
    void checkOptNegativeHmin() {
        logger.info("Starting test: checkOptNegativeHmin");

        ctx.setState(1);
        ctx.setNeq(1);

        opt.setRtol(new double[]{1e-4});
        opt.setAtol(new double[]{1e-6});
        opt.setItask(1);
        opt.setIxpr(1);
        opt.setMxstep(1);
        opt.setMxhnil(0);
        opt.setMxordn(5);
        opt.setMxords(3);
        opt.setHmax(1.0);
        opt.setHmin(-0.01);

        assertFalse(LSODAIntegrator.checkOpt(ctx, opt));

        logger.info("Test checkOptNegativeHmin passed");
    }

    @Test
    void correctionBasic() {
        ctx.setNeq(3);

        common.setMiter(0);
        common.setIpup(0);
        common.setH(1.0);
        common.setNq(1);

        double[][] yh = new double[4][ctx.getNeq() + 1];
        yh[1] = new double[] {0, 1.0, 2.0, 3.0};
        yh[2] = new double[] {0, 0.0, 0.0, 0.0};
        common.setYh(yh);
        common.setSavf(new double[ctx.getNeq() + 1]);
        common.setAcor(new double[ctx.getNeq() + 1]);

        double[] ewt = new double[ctx.getNeq() + 1];
        for (int i = 1; i <= ctx.getNeq(); i++) {
            ewt[i] = 1.0;
        }
        common.setEwt(ewt);

        double pnorm = 1e-3;
        double told = 10.0;
        double[] del = new double[1];
        int[] m = new int[1];

        logger.info("Starting correction test with initial conditions.");

        int result = LSODAIntegrator.correction(ctx, new double[ctx.getNeq() + 1], pnorm, del, new double[1], told, m);

        logger.info("Correction test result: " + result);
        logger.info("m[0]: " + m[0]);
        logger.info("del[0]: " + del[0]);

        assertEquals(0, result);
        assertEquals(0, m[0]);
        assertEquals(0.0, del[0], 1e-9);
    }

    /* The following tests are for the function scaleh() within LSODAIntegrator.java */
    @Test
    void scalehNoStability() {
        logger.info("Starting test scalehNoStability");

        ctx.setNeq(3);
        common.setH(1d);
        common.setRc(2d);
        common.setMeth(2);
        common.setPdlast(0d);
        common.setNq(2);

        opt.setHmxi(0.1);
        common.setRmax(0.5);

        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = 1;  yh[2][2] = 2;  yh[2][3] = 3;
        yh[3][1] = 4;  yh[3][2] = 5;  yh[3][3] = 6;
        common.setYh(yh);

        double rhInput = 0.4;
        LSODAIntegrator.scaleh(ctx, rhInput);

        logger.info("Test scalehNoStability executed");
        logger.info("Updated h: " + common.getH());
        logger.info("Updated rc: " + common.getRc());
        logger.info("Updated ialth: " + common.getIalth());

        assertEquals(0.4, common.getH(), 1e-8);
        assertEquals(0.8, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());

        double[][] resultYh = common.getYh();
        assertEquals(0.4, resultYh[2][1], 1e-8);
        assertEquals(0.8, resultYh[2][2], 1e-8);
        assertEquals(1.2, resultYh[2][3], 1e-8);
        assertEquals(0.64, resultYh[3][1], 1e-8);
        assertEquals(0.80, resultYh[3][2], 1e-8);
        assertEquals(0.96, resultYh[3][3], 1e-8);

        assertEquals(0, common.getIrflag());

        logger.info("Completed test scalehNoStability");
    }

    @Test
    void scalehStabilityNoAdjustment() {
        logger.info("Starting test scalehStabilityNoAdjustment");

        ctx.setNeq(2);
        common.setH(2d);
        common.setRc(3d);
        common.setMeth(1);
        common.setPdlast(0.3);
        common.setNq(3);
        common.setRmax(1.5);

        double[] SM1 = common.getSM1();
        SM1[3] = 0.7;
        common.setSM1(SM1);

        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = 10;  yh[2][2] = 20;
        yh[3][1] = 30;  yh[3][2] = 40;
        yh[4][1] = 50;  yh[4][2] = 60;
        common.setYh(yh);

        double rhInput = 1.0;
        LSODAIntegrator.scaleh(ctx, rhInput);

        logger.info("Test scalehStabilityNoAdjustment executed");
        logger.info("Updated h: " + common.getH());
        logger.info("Updated rc: " + common.getRc());
        logger.info("Updated ialth: " + common.getIalth());

        assertEquals(2.0, common.getH(), 1e-8);
        assertEquals(3.0, common.getRc(), 1e-8);
        assertEquals(4, common.getIalth());

        double[][] resultYh = common.getYh();
        assertEquals(10, resultYh[2][1], 1e-8);
        assertEquals(20, resultYh[2][2], 1e-8);
        assertEquals(30, resultYh[3][1], 1e-8);
        assertEquals(40, resultYh[3][2], 1e-8);
        assertEquals(50, resultYh[4][1], 1e-8);
        assertEquals(60, resultYh[4][2], 1e-8);

        assertEquals(0, common.getIrflag());

        logger.info("Completed test scalehStabilityNoAdjustment");
    }

    @Test
    void scalehStabilityAdjustment() {
        logger.info("Starting test scalehStabilityAdjustment");

        ctx.setNeq(2);
        common.setH(2.0);
        common.setRc(4.0);
        common.setMeth(1);
        common.setPdlast(1.0);
        common.setNq(2);
        opt.setHmxi(0.2);
        common.setRmax(1.5);

        // For nq = 2, set sm1[2] low enough so that the condition triggers.
        // pdh = fmax(2.0*1.0, 1e-6) = 2.0, and initial (1.0*2.0*1.00001) ≈ 2.00002 >= sm1[2]
        double[] SM1 = common.getSM1();
        SM1[2] = 1.5;
        common.setSM1(SM1);

        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = 7;  yh[2][2] = 8;
        yh[3][1] = 9;  yh[3][2] = 10;
        common.setYh(yh);

        double rhInput = 1.0;
        LSODAIntegrator.scaleh(ctx, rhInput);

        logger.info("Test scalehStabilityAdjustment executed");
        logger.info("Updated h: " + common.getH());
        logger.info("Updated rc: " + common.getRc());
        logger.info("Updated ialth: " + common.getIalth());

        // Expected effective rh becomes: new rh = sm1[2] / pdh = 1.5/2.0 = 0.75, and irflag = 1.
        // Then, h becomes 2.0*0.75 = 1.5, rc becomes 4.0*0.75 = 3.0, ialth becomes 3.
        // The yh rows are scaled: row2 by 0.75 and row3 by 0.75^2 = 0.5625.
        assertEquals(1.5, common.getH(), 1e-8);
        assertEquals(3.0, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());

        double[][] resultYh = common.getYh();
        // For row2: 7*0.75 = 5.25, 8*0.75 = 6.0
        assertEquals(5.25, resultYh[2][1], 1e-8);
        assertEquals(6.0, resultYh[2][2], 1e-8);
        // For row3: 9*0.5625 = 5.0625, 10*0.5625 = 5.625
        assertEquals(5.0625, resultYh[3][1], 1e-8);
        assertEquals(5.625, resultYh[3][2], 1e-8);

        // Verify that the stability branch set irflag to 1.
        assertEquals(1, common.getIrflag());

        logger.info("Completed test scalehStabilityAdjustment");
    }

    @Test
    void scalehRhLimitedByRmax() {
        logger.info("Starting test scalehRhLimitedByRmax");

        ctx.setNeq(2);
        common.setH(1.0);
        common.setRc(5.0);
        common.setMeth(2);
        common.setPdlast(0.0);
        common.setNq(2);
        opt.setHmxi(0.5);
        common.setRmax(0.8);

        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = 2;  yh[2][2] = 3;
        yh[3][1] = 4;  yh[3][2] = 5;
        common.setYh(yh);

        double rhInput = 1.0;
        LSODAIntegrator.scaleh(ctx, rhInput);

        logger.info("Test scalehRhLimitedByRmax executed");
        logger.info("Updated h: " + common.getH());
        logger.info("Updated rc: " + common.getRc());
        logger.info("Updated ialth: " + common.getIalth());

        // Expected effective rh:
        //   rh = fmin(1.0, 0.8) = 0.8; denominator = fmax(1, 1.0*0.5*0.8 = 0.4) = 1,
        // so rh remains 0.8.
        // Then, h becomes 1.0*0.8 = 0.8, rc becomes 5.0*0.8 = 4.0, and ialth becomes 3.
        // yh row scaling:
        //   Row2: multiplied by 0.8 → {2*0.8, 3*0.8} = {1.6, 2.4}
        //   Row3: multiplied by 0.8^2 = 0.64 → {4*0.64, 5*0.64} = {2.56, 3.2}
        assertEquals(0.8, common.getH(), 1e-8);
        assertEquals(4.0, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());

        double[][] resultYh = common.getYh();
        assertEquals(1.6, resultYh[2][1], 1e-8);
        assertEquals(2.4, resultYh[2][2], 1e-8);
        assertEquals(2.56, resultYh[3][1], 1e-8);
        assertEquals(3.2, resultYh[3][2], 1e-8);

        logger.info("Completed test scalehRhLimitedByRmax");
    }

   @Test
    void scalehNegativeH() {
        logger.info("Running test: scaleh_NegativeH");

        // Setup basic parameters
        ctx.setNeq(2);
        common.setH(-1.0);
        common.setRc(-3.0);
        common.setMeth(2);
        common.setPdlast(0.0);
        common.setNq(2);

        // Set max step size and max rh
        opt.setHmxi(0.3);
        common.setRmax(2.0);

        // Initialize Yh values
        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = -2;  yh[2][2] = -4;
        yh[3][1] = -6;  yh[3][2] = -8;
        common.setYh(yh);

        // Call scaleh with rh = 1.0
        double rhInput = 1.0;
        LSODAIntegrator.scaleh(ctx, rhInput);

        // Verify post-conditions: no change to h, rc, or yh
        double[][] resultYh = common.getYh();
        assertEquals(-1.0, common.getH(), 1e-8);
        assertEquals(-3.0, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());
        assertEquals(-2, resultYh[2][1], 1e-8);
        assertEquals(-4, resultYh[2][2], 1e-8);
        assertEquals(-6, resultYh[3][1], 1e-8);
        assertEquals(-8, resultYh[3][2], 1e-8);

        logger.fine("scaleh_NegativeH passed: h=" + common.getH() + ", rc=" + common.getRc());
    }

    /* The following tests are for the function .cfode() within LSODAIntegrator.java */
    @Test
    void cfodeMethod1() {
        logger.info("Running test: cfode_Method1");

        LSODAIntegrator.cfode(ctx, 1); // Set coefficients for method 1

        // Retrieve elco and tesco values for order 1
        double elco11 = common.getElco()[1][1];
        double elco12 = common.getElco()[1][2];
        double tesco11 = common.getTesco()[1][1];
        double tesco12 = common.getTesco()[1][2];

        // Assertions for order 1
        assertEquals(1.0, elco11, 1e-10, "Order 1 elco[1][1] should be 1.0");
        assertEquals(1.0, elco12, 1e-10, "Order 1 elco[1][2] should be 1.0");
        assertEquals(0.0, tesco11, 1e-10, "Order 1 tesco[1][1] should be 0.0");
        assertEquals(2.0, tesco12, 1e-10, "Order 1 tesco[1][2] should be 2.0");

        // Check other pre-set values
        double tesco21 = common.getTesco()[2][1];
        double tesco123 = common.getTesco()[12][3];

        assertEquals(1.0, tesco21, 1e-10, "Order 2 tesco[2][1] should be 1.0");
        assertEquals(0.0, tesco123, 1e-10, "Order 12 tesco[12][3] should be 0.0");

        logger.fine("cfode_Method1 passed: elco and tesco values are correct");
    }

    @Test
    void cfodeCoefficentConsistency() {
        logger.info("Running test: cfode_CoefficentConsistency");

        LSODAIntegrator.cfode(ctx, 1); // Generate elco coefficients

        // For each order from 2 to 12, ensure elco[nq][1] is not zero
        for (int nq = 2; nq <= 12; nq++) {
            double coeff = common.getElco()[nq][1];
            logger.fine("Checking elco[" + nq + "][1] = " + coeff);
            assertNotEquals(0.0, coeff, 1e-10, "Coefficient elco[" + nq + "][1] must be nonzero");
        }

        logger.fine("cfode_CoefficentConsistency passed: all elco[nq][1] ≠ 0 for nq=2..12");
    }

    @Test
    void cfodeInitialCoefficients() {
        LSODAIntegrator.cfode(ctx, 2);

        // Order 1 values (nq == 1)
        double elco11 = common.getElco()[1][1];
        double elco12 = common.getElco()[1][2];
        assertEquals(1.0, elco11, 1e-10, "For meth==2, order 1: elco[1][1] should be 1.0");
        assertEquals(1.0, elco12, 1e-10, "For meth==2, order 1: elco[1][2] should be 1.0");

        double tesco11 = common.getTesco()[1][1];
        double tesco12 = common.getTesco()[1][2];
        double tesco13 = common.getTesco()[1][3];
        assertEquals(1.0, tesco11, 1e-10, "For meth==2, order 1: tesco[1][1] should be 1.0");
        assertEquals(2.0, tesco12, 1e-10, "For meth==2, order 1: tesco[1][2] should be 2.0");
        assertEquals(3.0, tesco13, 1e-10, "For meth==2, order 1: tesco[1][3] should be 3.0");

        // Order 2 values (nq == 2)
        double elco21 = common.getElco()[2][1];
        assertEquals(2.0 / 3.0, elco21, 1e-10, "For meth==2, order 2: elco[2][1] should be 2/3");

        double tesco21 = common.getTesco()[2][1];
        double tesco22 = common.getTesco()[2][2];
        double tesco23 = common.getTesco()[2][3];
        assertEquals(1.0, tesco21, 1e-10, "For meth==2, order 2: tesco[2][1] should be 1.0");
        assertEquals(4.5, tesco22, 1e-10, "For meth==2, order 2: tesco[2][2] should be 4.5");
        assertEquals(6.0, tesco23, 1e-10, "For meth==2, order 2: tesco[2][3] should be 6.0");
    }


    @Test
    void cfodeAlternateMeth() {
        LSODAIntegrator.cfode(ctx, 3);

        double elco11 = common.getElco()[1][1];
        double elco12 = common.getElco()[1][2];
        double tesco11 = common.getTesco()[1][1];
        double tesco12 = common.getTesco()[1][2];
        double tesco13 = common.getTesco()[1][3];

        assertEquals(1.0, elco11, 1e-10, "Nonstandard meth should yield elco[1][1]==1.0");
        assertEquals(1.0, elco12, 1e-10, "Nonstandard meth should yield elco[1][2]==1.0");
        assertEquals(1.0, tesco11, 1e-10, "Nonstandard meth should yield tesco[1][1]==1.0");
        assertEquals(2.0, tesco12, 1e-10, "Nonstandard meth should yield tesco[1][2]==2.0");
        assertEquals(3.0, tesco13, 1e-10, "Nonstandard meth should yield tesco[1][3]==3.0");
    }

    @Test
    void cfodeRepeatedCalls() {
        // First call with meth==1
        LSODAIntegrator.cfode(ctx, 1);
        double elco11Meth1 = common.getElco()[1][1];
        double tesco11Meth1 = common.getTesco()[1][1];

        // Second call with meth==2
        LSODAIntegrator.cfode(ctx, 2);
        double elco11Meth2 = common.getElco()[1][1];
        double tesco11Meth2 = common.getTesco()[1][1];

        assertEquals(1.0, elco11Meth1, 1e-10, "Meth==1: elco[1][1] must be 1.0");
        assertEquals(1.0, elco11Meth2, 1e-10, "Meth==2: elco[1][1] must be 1.0");
        assertNotEquals(tesco11Meth1, tesco11Meth2, "tesco[1][1] should change between methods");
    }

    /* The following tests are for the function .ewset() within LSODAIntegrator.java */
    @Test
    void ewsetBasic() {
        logger.info("Running ewsetBasic test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] {0d, 0.1, 0.1, 0.1});
        opt.setAtol(new double[] {0d, 0.001, 0.001, 0.001});
        double[] y = {0d, 1d, 2d, 3d};

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        assertEquals(1/(0.1*1d + 0.001), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0 + 0.001), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0 + 0.001), ewt[3], 1e-6);
    }

    @Test
    void ewsetZeroYValues() {
        logger.info("Running ewsetZeroYValues test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, 0.0, 0.0, 0.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        for (int i = 1; i <= 3; i++) {
            assertEquals(1000d, ewt[i], 1e-6);
        }
    }

    @Test
    void ewsetNegativeYValues() {
        logger.info("Running ewsetNegativeYValues test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, -1.0, -2.0, -3.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        assertEquals(1/(0.1*1.0 + 0.001), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0 + 0.001), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0 + 0.001), ewt[3], 1e-6);
    }

    @Test
    void ewsetZeroRelativeTolerance() {
        logger.info("Running ewsetZeroRelativeTolerance test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.0, 0.0, 0.0 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, 10.0, 20.0, 30.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        for (int i = 1; i <= 3; i++) {
            assertEquals(1000.0, ewt[i], 1e-6);
        }
    }

    @Test
    void ewsetZeroAbsoluteToleranceNonZeroY() {
        logger.info("Running ewsetZeroAbsoluteToleranceNonZeroY test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.0, 0.0, 0.0 });
        double[] y = { 0, 1.0, 2.0, 3.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        assertEquals(1/(0.1*1.0), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0), ewt[3], 1e-6);
    }

    @Test
    void ewsetZeroAbsoluteToleranceZeroY() {
        logger.info("Running ewsetZeroAbsoluteToleranceZeroY test...");
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.0, 0.0, 0.0 });
        double[] y = { 0, 0.0, 0.0, 0.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        for (int i = 1; i <= 3; i++) {
            assertTrue(Double.isInfinite(ewt[i]), "EWT should be infinite for zero Y values.");
        }
    }

    @Test
    void ewsetNoEquations() {
        logger.info("Running ewsetNoEquations test...");
        ctx.setNeq(0);
        opt.setRtol(new double[] { 0 });
        opt.setAtol(new double[] { 0 });
        double[] y = { 0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertNotNull(ewt, "EWT should not be null.");
        assertEquals(1, ewt.length, "EWT array length should be 1 when there are no equations.");
    }


    /* The following tests are for the function .intdy() within LSODAIntegrator.java */
    /**
     * Valid test with k = 0.
     * The function should compute the 0th-derivative by returning yh[1].
     */
    @Test
    void testIntdyK0Valid() {
        logger.info("Running testIntdyK0Valid test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0;
        yh[1][2] = 2.0;
        yh[2][1] = 3.0;
        yh[2][2] = 4.0;
        yh[3][1] = 5.0;
        yh[3][2] = 6.0;
        common.setYh(yh);

        // Test parameters
        double t = 10d;
        int k = 0;
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(0, res, "intdy should return 0 when k = 0");
        assertNotNull(dky, "dky array should not be null");
        assertEquals(1d, dky[1], 1e-6, "Expected 0th-derivative value for y[1]");
        assertEquals(2d, dky[2], 1e-6, "Expected 0th-derivative value for y[2]");

        // Log the results for verification
        logger.info("Test Results for k = 0: dky[1] = " + dky[1] + ", dky[2] = " + dky[2]);
    }

    /**
     * Valid test with k = 1.
     * Using the same context as k0_valid, but with t set to (tcur - h),
     * the computed first derivative is manually checked.
     */
    @Test
    void testIntdyK1Valid() {
        logger.info("Running testIntdyK1Valid test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0;
        yh[1][2] = 2.0;
        yh[2][1] = 3.0;
        yh[2][2] = 4.0;
        yh[3][1] = 5.0;
        yh[3][2] = 6.0;
        common.setYh(yh);

        // Test parameters
        double t = 9.5;
        int k = 1;
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(0, res, "intdy should return 0 when k = 1");
        assertNotNull(dky, "dky array should not be null");
        assertEquals(-14.0, dky[1], 1e-6, "Expected first derivative value for y[1]");
        assertEquals(-16.0, dky[2], 1e-6, "Expected first derivative value for y[2]");

        // Log the results for verification
        logger.info("Test Results for k = 1: dky[1] = " + dky[1] + ", dky[2] = " + dky[2]);
    }

    /**
     * Valid test with k equal to _C(nq).
     */
    @Test
    void testIntdyKEqualsNq() {
        logger.info("Running testIntdyKEqualsNq test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10.0);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0;
        yh[1][2] = 2.0;
        yh[2][1] = 3.0;
        yh[2][2] = 4.0;
        yh[3][1] = 5.0;
        yh[3][2] = 6.0;
        common.setYh(yh);

        // Test parameters
        double t = 10.0;
        int k = 2; // k equal to nq
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(0, res, "intdy should return 0 when k equals nq");
        assertNotNull(dky, "dky array should not be null");
        assertEquals(40.0, dky[1], 1e-6, "Expected derivative value for y[1]");
        assertEquals(48.0, dky[2], 1e-6, "Expected derivative value for y[2]");

        // Log the results for verification
        logger.info("Test Results for k = nq: dky[1] = " + dky[1] + ", dky[2] = " + dky[2]);
    }

        /**
     * Error test when k is negative.
     * The function should return -1 if k < 0.
     */
    @Test
    void testIntdyNegativeK() {
        logger.info("Running testIntdyNegativeK test...");

        // Set up the test context
        ctx.setNeq(1);
        common.setNq(1);
        common.setTn(5.0);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[3][2];
        yh[1][1] = 10.0;
        yh[2][1] = 20.0;
        common.setYh(yh);

        // Test parameters
        double t = 5.0;
        int k = -1;  // Illegal value
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(-1, res, "intdy should return -1 when k is negative");

        // Log the result for verification
        logger.info("Test Results for k = -1: res = " + res);
    }

        /**
     * Error test when k is greater than _C(nq).
     * For example, if _C(nq)=1 then k=2 should be illegal.
     */
    @Test
    void testIntdyKGreaterThanNq() {
        logger.info("Running testIntdyKGreaterThanNq test...");

        // Set up the test context
        ctx.setNeq(1);
        common.setNq(1);
        common.setTn(5.0);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[3][2];
        yh[1][1] = 10.0;
        yh[2][1] = 20.0;
        common.setYh(yh);

        // Test parameters
        double t = 5.0;
        int k = 2;  // Illegal value
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(-1, res, "intdy should return -1 when k is greater than nq");

        // Log the result for verification
        logger.info("Test Results for k > nq: res = " + res);
    }


    /**
     * Error test when t is out of bounds.
     * Here we choose a t value that is greater than _C(tn)
     * (i.e. outside the interval [tcur - _C(hu) - 100*ETA*(tcur+_C(hu)), tcur]).
     */
    @Test
    void testIntdyTOutOfBounds() {
        logger.info("Running testIntdyTOutOfBounds test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10.0);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0; yh[1][2] = 2.0;
        yh[2][1] = 3.0; yh[2][2] = 4.0;
        yh[3][1] = 5.0; yh[3][2] = 6.0;
        common.setYh(yh);

        // Test parameters
        double t = 10.1;  // t value outside of bounds
        int k = 0;
        double[] dky = new double[ctx.getNeq() + 1]; // to store derivatives

        // Call intdy function
        int res = LSODAIntegrator.intdy(ctx, t, k, dky);

        // Assertions
        assertEquals(-2, res, "intdy should return -2 when t is out of bounds");

        // Log the result for verification
        logger.info("Test Results for t out of bounds: res = " + res);
    }

    /* The following tests are for .intdyReturn() within LSODAIntegrator.java */
    /**
     *  iflag == 0
    */
    @Test
    void testIntdyReturnBasic() {
        logger.info("Running testIntdyReturnBasic test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 5.0;  yh[1][2] = 6.0;
        common.setYh(yh);

        // Test parameters
        double tout = 10.0;
        double[] t = new double[1];  // store time
        double[] y = new double[ctx.getNeq() + 1];  // store results
        y[1] = 1d;
        y[2] = 2d;

        // Call intdyReturn function
        int state = LSODAIntegrator.intdyReturn(ctx, y, t, tout, opt.getItask());

        // Assertions
        assertEquals(2, state, "intdyReturn should return 2 when iflag == 0");
        assertEquals(tout, t[0], 1e-10, "The time value should match tout");
    }

    @Test
    void testIntdyReturnErrorCase() {
        logger.info("Running testIntdyReturnErrorCase test...");

        // Set up the test context
        ctx.setNeq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);

        // Initialize yh (solution history)
        double[][] yh = new double[4][3];
        yh[1][1] = 5d; yh[1][2] = 6d;
        common.setYh(yh);

        // Test parameters
        double tout = 10d;
        double[] t = new double[1];  // store time
        double[] y = new double[ctx.getNeq() + 1];  // store results
        y[1] = 1d;
        y[2] = 2d;

        // Call intdyReturn function
        int state = LSODAIntegrator.intdyReturn(ctx, y, t, tout, opt.getItask());

        // Assertions
        assertEquals(2, state, "intdyReturn should return 2 for successful execution");
        assertEquals(tout, t[0], 1e-10, "The time value should match the target output time");

        assertEquals(5d, y[1], 1e-10, "y[1] should be 5 after the call");
        assertEquals(6d, y[2], 1e-10, "y[2] should be 6 after the call");
    }



}






