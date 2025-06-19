import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.math.ode.DerivativeException;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.DESystem;
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
    LSODAIntegrator integrator = new LSODAIntegrator();

    /**
     * The following tests are for the function .ddot() within LSODAIntegrator.java
     * The function calculates the dot product of two vectors.
    **/

    /** Basic dot product test, incx = incy = 1 */
    @Test
    void ddotBasicDotProduct() {
        int n = 3;
        double[] dx = {0d, 1.0, 2.0, 3.0};
        double[] dy = {0d, 4.0, 5.0, 6.0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((1d*4d + 2d*5d + 3d*6d), result, 1e-6, "Dot product should be "+ 1d*4d + 2d*5d + 3d*6d);
    }

    /** Test vector with length = 0 */
    @Test
    void ddotZeroLengthVector() {
        int n = 0;
        double[] dx = {0};
        double[] dy = {0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0, result, 1e-6, "Dot product for zero-length vectors should be 0.");
    }

    /** Test where n < 0 */
    @Test
    void ddotNegativeN() {
        int n = -5;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 2d, 4d, 6d, 8d, 10d};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0, result, 1e-6, "Dot product for negative n should be 0.");
    }

    /** Test different values for incx and incy */
    @Test
    void ddotDifferentStrides() {
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
        int n = 3;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1;
        int incy = -1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        double expected = 3d * 6d + 2d * 5d + 1d * 4d;
        assertEquals(expected, result, 1e-6, "Dot product with negative strides should be correct");
    }

    /** Test with all values = 0 */
    @Test
    void ddotAllZero() {
        int n = 3;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 0d, 0d, 0d};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0d, result, 1e-6, "Dot product of zero vectors should be 0");
    }

    /** Test with a single-element vector */
    @Test
    void ddotSingleElement() {
        int n = 1;
        double[] dx = {0d, 7.5};
        double[] dy = {0d, 2.0};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((7.5 * 2d), result, 1e-6, "Dot product of single-element vectors should be correct");
    }


    /** Test with NaN values */
    @Test
    void ddotNaN() {
        int n = 2;
        double[] dx = {0d, Double.NaN, 2.0};
        double[] dy = {0d, 5.0, Double.NaN};
        int incx = 1;
        int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertTrue(Double.isNaN(result));
    }

    /**
     * The following tests are for the function .vmnorm within LSODAIntegrator.java
     * The function calculates the weighted max-norm of a vector.
    **/

    /** Basic max-norm test */
    @Test
    void vmnormBasicMaxNorm() {
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
        int n = 3;
        double[] v = {0d, 0d, 0d, 0d};
        double[] w = {0d, 0d, 0d, 0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6, "Max-norm of all-zero vectors should be 0");
    }

    /** Test single element */
    @Test
    void vmnormSingleElement() {
        int n = 1;
        double[] v = {0d, 7.5};
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(Math.abs(7.5) * 2d, result, 1e-6, "Max-norm of single-element vector should be abs(v[1]) * w[1]");
    }

    /** Test negative values in v */
    @Test
    void vmnormNegativeValues() {
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
        int n = 3;
        double[] v = {0d, 1d, 2d, 3d};
        double[] w = {0d, -1d, -2d, -3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        double expected = 0d;
        assertEquals(expected, result, 1e-6, "Max-norm with negative weights should be 0");
    }

    /** Test zero-length vector */
    @Test
    void vmnormZeroLengthVector() {
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
        int n = 1;
        double[][] a = {
            {0d, 0d},
            {0d, -7.5}
        };
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(Math.abs(-7.5), result, 1e-6, "Single element matrix norm should be absolute value of the element");
    }

    /** Matrix has negative values */
    @Test
    void fnormNegativeValues() {
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
        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
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
        int n = 3;
        double[] dx = {0d, 1d, 0d, -4d, -5d, 3d, 3d};
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
        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
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
        int n = 4;
        double[] dx = {0d, 2d, 0d, 0d, -5d, 0d, 0d, 3d, 0d, 0d, -5d, 0d, 0d};
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
    void prjaBasic() throws DerivativeException {
        ctx.setNeq(1);

        // dy/dt = y, one dimensional
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        ctx.setOdeSystem(system);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 1d}; // f(t, y) = y and y = 1 stated below
        common.setSavf(newSavf);
        double[][] newWm = new double[2][2];
        common.setWm(newWm);
        double[] newAcor = new double[2];
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[2];
        common.setIpvt(newIpvt);

        double[] y = {0d, 1d};
        int result = LSODAIntegrator.prja(ctx, y);

        // manually calculated 
        double[][] expectedWm = {
            {0, 0},
            {0, 0.9}
        };

        assertArrayEquals(expectedWm, common.getWm());

        assertEquals(1, common.getNje());
        assertEquals(1, common.getNfe());
        assertEquals(1, result);
    }

    @Test
    void prjaMiterNotTwo() throws DerivativeException {
        ctx.setNeq(3);
        common.setMiter(5);

        // No need to set ODE system as miter is not equal to 2.

        double[] y = {0d, 1d, 2d, 3d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, common.getNje());
        assertEquals(0, common.getNfe());
        assertEquals(0, result);
    }


    @Test
    void prjaZeroLengthSystem() throws DerivativeException {
        ctx.setNeq(0);
        common.setMiter(2);

        // No need to set ODE system as neq is equal to 0.

        double[] y = {0d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, common.getNje());
        assertEquals(0, common.getNfe());
        assertEquals(1, result);
    }

    @Test
    void prjaTwoLengthSystem() throws DerivativeException {
        ctx.setNeq(2);
        common.setMiter(2);

        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 2;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 1;
                yDot[1] = y[0] + y[1];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        ctx.setOdeSystem(system);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 1d, 5d}; // f(t, y0) = 1, f(t, y1) = y0 + y1;
        common.setSavf(newSavf);
        double[][] newWm = new double[3][3];
        common.setWm(newWm);
        double[] newAcor = new double[3];
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[3];
        common.setIpvt(newIpvt);

        double[] y = {0d, 3d, 2d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, common.getNje());
        assertEquals(2, common.getNfe());
        assertEquals(1, result);
    }

    @Test
    void prjaAllZero() throws DerivativeException {
        ctx.setNeq(3);

        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 3;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 0;
                yDot[1] = 0;
                yDot[2] = 0;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        ctx.setOdeSystem(system);
        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 0d, 0d, 0d}; // f(t, y) = 0
        common.setSavf(newSavf);
        double[][] newWm = new double[4][4];
        common.setWm(newWm);
        double[] newAcor = new double[4];
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d, 0d, 0d, 0d};
        int result = LSODAIntegrator.prja(ctx, y);

        double[][] expectedWm = {
            {0, 0, 0, 0},
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1}
        };

        double delta = 1e-12;
        for(int i=0; i<=ctx.getNeq(); i++){
            for(int j=0; j<=ctx.getNeq(); j++){
                assertEquals(expectedWm[i][j], common.getWm()[i][j], delta);
            }
        }

        assertEquals(1, common.getNje());
        assertEquals(3, common.getNfe());
        assertEquals(1, result);
    }

    @Test
    void prjaFunctionEvaluationsIncrement() throws DerivativeException {
        ctx.setNeq(3);

        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 3;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0];
                yDot[1] = y[1];
                yDot[2] = y[2];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        ctx.setOdeSystem(system);

        common.setMiter(2);
        common.setH(0.1);
        double[] newEl = new double[]{0d, 1d};
        common.setEl(newEl);
        double[] newEwt = new double[]{0d, 1d, 1d, 1d};
        common.setEwt(newEwt);
        double[] newSavf = new double[]{0d, 1d, 2d, 3d}; // f(y, t) = y, y defined below
        common.setSavf(newSavf);
        double[][] newWm = new double[4][4];
        common.setWm(newWm);
        double[] newAcor = new double[4];
        common.setAcor(newAcor);
        common.setTn(0d);
        common.setNje(0);
        common.setNfe(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d, 1d, 2d, 3d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, common.getNje());
        assertEquals(3, common.getNfe());
        assertEquals(1, result);
    }

    /**
     * The following test cases are for the function daxpy() within LSODAIntegrator.java.
     * Daxpy computes <pre>dy = da * dx + dy</pre> for vectors <pre>dx</pre> and <pre>dy</pre> and scalar <pre>da</pre>.
     */
    @Test
    void daxpyBasic() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 6d, 9d, 12d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyZeroScalarMultiplier() {
        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyNegativeN() {
        int n = -5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyNonUnitaryIncrements() {
        int n = 2;
        double da = 3d;
        double[] dx = {0d, 1d, 2d, 3d, 4d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 8d, 15d, 7d, 8d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyNegativeStrides() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1, incy = -1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 12d, 9d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyAllZeroDx() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void ddotLargeNumbers() {
        int n = 2;
        double[] dx = {0d, 1e100, -1e100};
        double[] dy = {0d, 1e-100, 1e-100};
        int incX = 1, incY = 1;

        double sumOfProducts = (dx[1] * dy[1]) + (dx[2] * dy[2]);
        double result = LSODAIntegrator.ddot(n, dx, dy, incX, incY);

        logger.info("Expected: " + sumOfProducts + ", Actual: " + result);

        assertEquals(sumOfProducts, result, 1e-6);
    }

    @Test
    void daxpyLargeValues() {
        int n = 3;
        double da = 1e9d;
        double[] dx = {0d, 1e9d, -2e9d, 3e9d};
        double[] dy = {0d, 1e9d, 2e9d, -3e9d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 1.000000001e18, -1.999999998e18, 2.999999997e18};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpyInPlaceModification() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = dx.clone();
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, dy, incy, 1, 1);

        double[] expectedDy = {0d, 3d, 6d, 9d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    /**
     * The following tests are for the functions dgefa() within LSODAIntegrator
     */
    @Test
    void dgefaBasic() {
        int n = 3;
        int[] ipvt = new int[4];
        int[] info = new int[1];
        double[][] a = {
            {0, 0, 0, 0},
            {0, 4, 3, 2},
            {0, 2, 3, 1},
            {0, 1, 1, 2}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        // based on manual calculation
        double[][] expectedA = {
            {0, 0, 0, 0},
            {0, 4, -0.75, -0.5},
            {0, 2, 1.5, 0.0},
            {0, 1, 0.25, 1.5}
        };

        int[] expectedIpvt = {0, 1, 2, 3};

        double delta = 1e-12;
        for(int i=0; i<=n; i++){
            for(int j=0; j<=n; j++){
                assertEquals(expectedA[i][j], a[i][j], delta);
            }
        }

        assertArrayEquals(expectedIpvt, ipvt);

        assertEquals(0, info[0]);
    }

    @Test
    void dgefaUpperTriangular() {
        int n = 3;
        int[] ipvt = new int[4];
        int[] info = new int[1];
        double[][] a = {
            {0d, 0d, 0d, 0d},
            {0d, 3d, 1d, 2d},
            {0d, 0d, 4d, 5d},
            {0d, 0d, 0d, 6d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        // System.out.println("a = ");
        // print2DArray(a);

        assertEquals(0, info[0]);
    }

    @Test
    void dgefaSingularMatrix() {
        int n = 2;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 6d, 12d},
            {0d, 2d, 4d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(n, info[0]);
    }

    @Test
    void dgefaIdentityMatrix() {
        int n = 3;
        int[] ipvt = new int[4];
        int[] info = new int[1];
        double[][] a = {
            {0d, 0d, 0d, 0d},
            {0d, 1d, 0d, 0d},
            {0d, 0d, 1d, 0d},
            {0d, 0d, 0d, 1d}
        };

        LSODAIntegrator.dgefa(a, n, ipvt, info);

        assertEquals(0, info[0]);
        // assertEquals(1, ipvt[1]);
        // assertEquals(2, ipvt[2]);
        // assertEquals(3, ipvt[3]);
    }

    @Test
    void dgefaRowSwapping() {
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

        assertEquals(3, info[0]);
        // assertEquals(1, ipvt[1]);
        // assertEquals(2, ipvt[2]);
        // assertEquals(3, ipvt[3]);
    }


    /**
     * The following test cases are for the function dscal() within LSODAIntegrator
     */
    @Test
    void dscalBasic() {
        int n = 5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 2d, 4d, 6d, 8d, 10d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalZeroLengthVector() {
        int n = 0;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalNegativeN() {
        int n = -3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalUnitaryScalar() {
        int n = 3;
        double da = 1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalZeroScalar() {
        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 0d, 0d, 0d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalNegativeScalar() {
        int n = 3;
        double da = -1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, -1d, -2d, -3d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscalFractionalScalar() {
        int n = 3;
        double da = 0.5;
        double[] dx = {0d, 2d, 4d, 6d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    /** The following tests are for the function solsy() within LSODAIntegrator */

    /* Test that solsy() calls dgesl() when miter == 2 */
    @Test
    void solsyBasicLUDecomposition() {
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
    }

    @Test
    void solsyMiterNotTwo() {
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
    }

    @Test
    void solsyZeroNeq() {
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

        LSODAIntegrator.dgesl(coefficientMatrix, numberOfEquations, pivotArray, rightHandSide, 0);

        assertArrayEquals(new double[]{0d, 3d, 4d}, rightHandSide, 1e-6);
    }

    @Test
    void dgeslSolveLinearSystem1() {
        double[][] arr = {
            {0, 0, 0},
            {0, 1, 2},
            {0, 2, 1},
        };
        int n = 2;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];

        LSODAIntegrator.dgefa(arr, n, ipvt, info);

        double[] b = {0, 11, 10};

        LSODAIntegrator.dgesl(arr, n, ipvt, b, 0); 

        double[] analyticalRes = {0, 3, 4};

        for (int i=1; i<b.length; i++) {
            assertTrue(Math.abs(b[i]-analyticalRes[i])<1e-14);
        }
    }

    @Test
    void dgeslSolveLinearSystem2() {
        double[][] arr = {
            {0, 0, 0, 0},
            {0, 1, 2, 3},
            {0, 5, -1, 0},
            {0, 19, 2, 2}
        };
        int n = 3;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];

        LSODAIntegrator.dgefa(arr, n, ipvt, info);

        double[] b = {0, 72, 54, 21};

        LSODAIntegrator.dgesl(arr, n, ipvt, b, 0); 

        double[] analyticalRes = {0, 0.4153846153846154, -51.92307692307692, 58.47692307692308};

        for (int i=1; i<b.length; i++) {
            assertTrue(Math.abs(b[i]-analyticalRes[i])<1e-14);
        }
    }

    @Test
    void dgeslSolveLinearSystem3() {
        double[][] arr = {
            {0, 0, 0, 0, 0},
            {0, 1, -1, 3, 7},
            {0, 10, -1, 0, -2},
            {0, 100, 2, 2, 4},
            {0, 5, 99, 2, 9}
        };
        int n = 4;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];

        LSODAIntegrator.dgefa(arr, n, ipvt, info);

        double[] b = {0, 727.4, -175, 740.4, 1471.2};

        LSODAIntegrator.dgesl(arr, n, ipvt, b, 0); 

        double[] analyticalRes = {0, 3.1, 5.4, 9.2, 100.3};

        for (int i=1; i<b.length; i++) {
            assertTrue(Math.abs(b[i]-analyticalRes[i])<1e-14);
        }
    }

    @Test
    void dgeslSolveLinearSystemWithSingularMatrix() {
        double[][] arr = {
            {0, 0, 0},
            {0, 1, 2},
            {0, 2, 4},
        };
        int n = 2;
        int[] ipvt = new int[n + 1];
        int[] info = new int[1];

        LSODAIntegrator.dgefa(arr, n, ipvt, info);
        // System.out.println("info = " + info[0]);
        double[] b = {0, 11, 22};
        
        assertThrows(IllegalArgumentException.class, () -> {
            LSODAIntegrator.dgesl(arr, n, ipvt, b, 0);
        } );
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
    }

    @Test
    void checkOptBasicCaseState3() {

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
    }

    @Test
    void checkOptNeqZero() {
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
    }

    @Test
    void checkOptNegativeRtol() {
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
    }

    @Test
    void checkOptNegativeAtol() {
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
    }

    @Test
    void checkOptIllegalItaskHigh() {
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
    }

    @Test
    void checkOptIllegalItaskLow() {
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
    }

    @Test
    void checkOptIllegalIxprLow() {
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
    }

    @Test
    void checkOptIllegalIxprHigh() {
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
    }

    @Test
    void checkOptNegativeMxstep() {
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
    }

    @Test
    void checkOptMxstepZero() {
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
    }

    @Test
    void checkOptNegativeMxhnil() {
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
    }

    @Test
    void checkOptNegativeMxordn() {
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
    }

    @Test
    void checkOptMxordnZero() {
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
    }

    @Test
    void checkOptNegativeMxords() {
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
    }

    @Test
    void checkOptMxordsZero() {
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
    }

    @Test
    void checkOptNegativeHmax() {
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
    }

    @Test
    void checkOptHmxiCalculation() {
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
    }

    @Test
    void checkOptNegativeHmin() {
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
    }

    @Test
    void correctionBasic() throws DerivativeException {
        ctx.setNeq(3);

        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 3;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0]=1;
                yDot[1]=1;
                yDot[2]=1;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        ctx.setOdeSystem(system);
        common.setMiter(0);
        common.setIpup(0);
        common.setH(1.0);
        common.setNq(1);

        double[][] yh = new double[3][ctx.getNeq() + 1];
        yh[1] = new double[] {0, 1.0, 2.0, 3.0};
        yh[2] = new double[] {0, 0.0, 0.0, 0.0};
        common.setYh(yh);
        common.setSavf(new double[ctx.getNeq() + 1]);
        common.setAcor(new double[ctx.getNeq() + 1]);

        double[] ewt = new double[] {0, 1, 1, 1};
        common.setEwt(ewt);

        double pnorm = 1e-3;
        double told = 10.0;
        double[] del = new double[1];
        int[] m = new int[1];

        int result = LSODAIntegrator.correction(ctx, new double[ctx.getNeq() + 1], pnorm, del, new double[1], told, m);

        logger.info("Correction test result: " + result);
        logger.info("m[0]: " + m[0]);
        logger.info("del[0]: " + del[0]);

        assertEquals(0, result);
        assertEquals(1, m[0]);
        assertEquals(0.0, del[0], 1e-9);
    }

    /* The following tests are for the function scaleh() within LSODAIntegrator.java */
    @Test
    void scalehNoStability() {
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
    }

    // Uses setSM1 method, inapplicable test

    // @Test
    // void scalehStabilityNoAdjustment() {
    //     ctx.setNeq(2);
    //     common.setH(2d);
    //     common.setRc(3d);
    //     common.setMeth(1);
    //     common.setPdlast(0.3);
    //     common.setNq(3);
    //     common.setRmax(1.5);

    //     double[] SM1 = common.getSM1();
    //     SM1[3] = 0.7;
    //     common.setSM1(SM1);

    //     double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
    //     yh[2][1] = 10;  yh[2][2] = 20;
    //     yh[3][1] = 30;  yh[3][2] = 40;
    //     yh[4][1] = 50;  yh[4][2] = 60;
    //     common.setYh(yh);

    //     double rhInput = 1.0;
    //     LSODAIntegrator.scaleh(ctx, rhInput);

    //     logger.info("Updated h: " + common.getH());
    //     logger.info("Updated rc: " + common.getRc());
    //     logger.info("Updated ialth: " + common.getIalth());

    //     assertEquals(2.0, common.getH(), 1e-8);
    //     assertEquals(3.0, common.getRc(), 1e-8);
    //     assertEquals(4, common.getIalth());

    //     double[][] resultYh = common.getYh();
    //     assertEquals(10, resultYh[2][1], 1e-8);
    //     assertEquals(20, resultYh[2][2], 1e-8);
    //     assertEquals(30, resultYh[3][1], 1e-8);
    //     assertEquals(40, resultYh[3][2], 1e-8);
    //     assertEquals(50, resultYh[4][1], 1e-8);
    //     assertEquals(60, resultYh[4][2], 1e-8);

    //     assertEquals(0, common.getIrflag());
    // }

    // @Test
    // void scalehStabilityAdjustment() {
    //     ctx.setNeq(2);
    //     common.setH(2.0);
    //     common.setRc(4.0);
    //     common.setMeth(1);
    //     common.setPdlast(1.0);
    //     common.setNq(2);
    //     opt.setHmxi(0.2);
    //     common.setRmax(1.5);

    //     // For nq = 2, set sm1[2] low enough so that the condition triggers.
    //     // pdh = fmax(2.0*1.0, 1e-6) = 2.0, and initial (1.0*2.0*1.00001) ≈ 2.00002 >= sm1[2]
    //     double[] SM1 = common.getSM1();
    //     SM1[2] = 1.5;
    //     common.setSM1(SM1);

    //     double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
    //     yh[2][1] = 7;  yh[2][2] = 8;
    //     yh[3][1] = 9;  yh[3][2] = 10;
    //     common.setYh(yh);

    //     double rhInput = 1.0;
    //     LSODAIntegrator.scaleh(ctx, rhInput);

    //     logger.info("Updated h: " + common.getH());
    //     logger.info("Updated rc: " + common.getRc());
    //     logger.info("Updated ialth: " + common.getIalth());

    //     // Expected effective rh becomes: new rh = sm1[2] / pdh = 1.5/2.0 = 0.75, and irflag = 1.
    //     // Then, h becomes 2.0*0.75 = 1.5, rc becomes 4.0*0.75 = 3.0, ialth becomes 3.
    //     // The yh rows are scaled: row2 by 0.75 and row3 by 0.75^2 = 0.5625.
    //     assertEquals(1.5, common.getH(), 1e-8);
    //     assertEquals(3.0, common.getRc(), 1e-8);
    //     assertEquals(3, common.getIalth());

    //     double[][] resultYh = common.getYh();
    //     // For row2: 7*0.75 = 5.25, 8*0.75 = 6.0
    //     assertEquals(5.25, resultYh[2][1], 1e-8);
    //     assertEquals(6.0, resultYh[2][2], 1e-8);
    //     // For row3: 9*0.5625 = 5.0625, 10*0.5625 = 5.625
    //     assertEquals(5.0625, resultYh[3][1], 1e-8);
    //     assertEquals(5.625, resultYh[3][2], 1e-8);

    //     // Verify that the stability branch set irflag to 1.
    //     assertEquals(1, common.getIrflag());
    // }

    @Test
    void scalehRhLimitedByRmax() {
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
    }

   @Test
    void scalehNegativeH() {
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
    }

    @Test
    void cfodeCoefficentConsistency() {
        LSODAIntegrator.cfode(ctx, 1); // Generate elco coefficients

        // For each order from 2 to 12, ensure elco[nq][1] is not zero
        for (int nq = 2; nq <= 12; nq++) {
            double coeff = common.getElco()[nq][1];
            logger.fine("Checking elco[" + nq + "][1] = " + coeff);
            assertNotEquals(0.0, coeff, 1e-10, "Coefficient elco[" + nq + "][1] must be nonzero");
        }
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

    }

        /**
     * Error test when k is greater than _C(nq).
     * For example, if _C(nq)=1 then k=2 should be illegal.
     */
    @Test
    void testIntdyKGreaterThanNq() {
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
    }


    /**
     * Error test when t is out of bounds.
     * Here we choose a t value that is greater than _C(tn)
     * (i.e. outside the interval [tcur - _C(hu) - 100*ETA*(tcur+_C(hu)), tcur]).
     */
    @Test
    void testIntdyTOutOfBounds() {
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
    }

    /* The following tests are for .intdyReturn() within LSODAIntegrator.java */
    /**
     *  iflag == 0
    */
    @Test
    void testIntdyReturnBasic() {
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

    /*
     * test for methodSwitch() helper function
     */
    @Test
    void methodSwitchNonstiffOrderMoreThan5() {
        ctx.setNeq(1);
        opt.setMxordn(12);
        opt.setMxords(5);
        common.setMeth(1);
        common.setNq(7);
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 0, 0, rh);

        assertEquals(1, common.getMeth());  // no change in method
    } 

    @Test
    void methodSwitchNonstiffPollutedErrorCase1() {
        ctx.setNeq(1);
        opt.setMxordn(12);
        opt.setMxords(5);
        common.setMeth(1);
        common.setNq(3);
        common.setPdest(0);                     // last lipschitz estimate, pdest set to 0
        common.setIrflag(0);                   // No stepsize stability, irflag set to 0
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 0, 0, rh);

        assertEquals(1, common.getMeth());  // no change in method
    } 

    @Test
    void methodSwitchNonstiffPollutedErrorCase2() {
        ctx.setNeq(1);
        opt.setMxordn(12);
        opt.setMxords(5);
        common.setMeth(1);
        common.setNq(3);           
        common.setPdest(1);    
        common.setIrflag(0);                   // No stepsize stability, irflag set to 0
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-12, 1000, rh);

        assertEquals(1, common.getMeth());  // no change in method
    } 

    @Test
    void methodSwitchNonstiffPollutedErrorCase3() {
        ctx.setNeq(1);
        opt.setMxordn(12);
        opt.setMxords(3);
        common.setMeth(1);
        common.setNq(4);           
        common.setPdest(0);    
        common.setIrflag(1);                  // irflag set to 1
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1, 1, rh);

        assertEquals(2, common.getMeth());  // method switched to BDF
        assertEquals(2, rh[0]);             
        assertEquals(3, common.getNq());    // order is minimum of mxords and current order
    } 

    @Test
    void methodSwitchNonstiffLessStepsizeFactorGain() {
        ctx.setNeq(1);
        opt.setMxordn(10);
        opt.setMxords(5);
        common.setMeth(1);
        common.setNq(5);           
        common.setPdest(1);    
        common.setIrflag(1); 
        common.setH(0.1);
        common.setPdlast(5);  
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-2, 1, rh);

        assertEquals(1, common.getMeth());  // No method switch, rh2 < rh1 * 5
    } 

    @Test
    void methodSwitchNonstiffToStiff1() {
        ctx.setNeq(1);
        opt.setMxordn(10);
        opt.setMxords(5);
        common.setMeth(1);
        common.setNq(5);           
        common.setPdest(1);    
        common.setIrflag(1); 
        common.setH(0.5);
        common.setPdlast(5);  
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-2, 1, rh);

        assertEquals(2, common.getMeth());  // Method switched to BDF, rh2 > rh1 * 5
    } 

    @Test
    void methodSwitchNonstiffToStiff2() {
        ctx.setNeq(1);
        opt.setMxordn(10);
        opt.setMxords(3);
        common.setMeth(1);
        common.setNq(5);                        // order > mxords
        common.setPdest(1);    
        common.setIrflag(1); 
        common.setH(0.5);
        common.setPdlast(5);  
        double[][] yh = new double[11][2];
        yh[5] = new double[] {0d, 1d};
        common.setYh(yh);
        double[] ewt = new double[] {0d, 1d};
        common.setEwt(ewt);
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-2, 1, rh);

        assertEquals(2, common.getMeth());  // Method switched to BDF, rh2 > rh1 * 5
    } 

    @Test
    void methodSwitchStiffLessStepsizeFactorGain() {
        ctx.setNeq(1);
        opt.setMxordn(10);
        opt.setMxords(5);
        common.setMeth(2);
        common.setNq(5);           
        common.setPdest(1);    
        common.setIrflag(1); 
        common.setH(0.5);
        common.setPdnorm(1);  
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-2, 1, rh);

        assertEquals(2, common.getMeth());  // No method switch, rh1 < rh2
    } 

    @Test
    void methodSwitchStiffToNonstiff() {
        ctx.setNeq(1);
        opt.setMxordn(10);
        opt.setMxords(5);
        common.setMeth(2);
        common.setNq(5);           
        common.setPdest(1);    
        common.setIrflag(1); 
        common.setH(0.1);
        common.setPdnorm(1);  
        double[] rh = new double[1];

        LSODAIntegrator.methodSwitch(ctx, 1e-1, 1, rh);

        assertEquals(1, common.getMeth());  // Method switched to Adams, rh1 > rh2
    } 

    /*
     * Tests for orderSwitch() helper function
     */

    @Test
    void orderSwitchOrderSame() {
        ctx.setNeq(1);
        common.setNq(1);                                        // order = 1, which implies rhdn = 0.
        common.setMeth(1);
        common.setH(0.5);
        common.setPdlast(1);
        double[] rh = new double[1];

        int orderflag = LSODAIntegrator.orderSwitch(ctx, 0.7, 1, rh, 1, 10);

        assertEquals(1, orderflag);                       // rhsm > rhdn & rhsm > rhup, therefore change in stepsize but order remains same
        assertEquals(0.8333325d, rh[0], 1e-12);     // calculate manually
        assertEquals(1, common.getNq());                  // same order
    }

    @Test
    void orderSwitchOrderUp() {
        ctx.setNeq(2);
        common.setNq(2);                                        
        common.setMeth(2);
        common.setH(0.5);
        common.setPdlast(1);
        double[][] yh = new double[5][ctx.getNeq() + 1];
        yh[3] = new double[] {0d, 1d, 1d};
        common.setYh(yh);
        double[] ewt = new double[] {0d, 1d, 1d};
        common.setEwt(ewt);
        double[][] tesco = new double[13][4];
        tesco[2][1] = 1d;
        common.setTesco(tesco);
        double[] el = new double[14];
        el[3] = 1d;
        common.setEl(el);
        double[] acor = new double[] {0d, 1d, 1d};
        common.setAcor(acor);
        double[] rh = new double[1];

        int orderflag = LSODAIntegrator.orderSwitch(ctx, 1.1d, 1, rh, 1, 10);

        assertEquals(2, orderflag);                       // rhup > rhsm & rhup > rhdn
        assertEquals(1.1d, rh[0]);
        assertEquals(3, common.getNq());                  // order up by one
    }

    @Test
    void orderSwitchOrderUpThresholdNotAchieved() {
        ctx.setNeq(2);
        common.setNq(2);                                        
        common.setMeth(2);
        common.setH(0.5);
        common.setPdlast(1);
        double[][] yh = new double[5][ctx.getNeq() + 1];
        yh[3] = new double[] {0d, 1d, 1d};
        common.setYh(yh);
        double[] ewt = new double[] {0d, 1d, 1d};
        common.setEwt(ewt);
        double[][] tesco = new double[12][4];
        tesco[2][1] = 1d;
        common.setTesco(tesco);
        double[] rh = new double[1];

        int orderflag = LSODAIntegrator.orderSwitch(ctx, 1d, 1, rh, 1, 10);

        assertEquals(0, orderflag);                       // rhup > rhsm & rhup > rhdn, but rhup = 1 < 1.1, threshold of 10% increment not achieved   
        assertEquals(2, common.getNq());                  // same order
        assertEquals(3, common.getIalth());
    }

    @Test
    void orderSwitchOrderDown() {
        ctx.setNeq(2);
        common.setNq(2);                                        
        common.setMeth(2);
        common.setH(0.5);
        common.setPdlast(1);
        double[][] yh = new double[5][ctx.getNeq() + 1];
        yh[3] = new double[] {0d, 1d, 1d};
        common.setYh(yh);
        double[] ewt = new double[] {0d, 1d, 1d};
        common.setEwt(ewt);
        double[][] tesco = new double[12][4];
        tesco[2][1] = 1d;
        common.setTesco(tesco);
        double[] rh = new double[1];

        int orderflag = LSODAIntegrator.orderSwitch(ctx, 0.7d, 1.5d, rh, 1, 10);

        assertEquals(2, orderflag);                       // rhdn > rhup & rhdn > rhsm
        assertEquals(0.76923, rh[0], 1e-12);        // calculated manually
        assertEquals(1, common.getNq());                  // order down by one
    }

    @Test
    void orderSwitchNegativeKflag() {
        ctx.setNeq(2);
        common.setNq(2);                                        
        common.setMeth(2);
        common.setH(0.5);
        common.setPdlast(1);
        double[][] yh = new double[5][ctx.getNeq() + 1];
        yh[3] = new double[] {0d, 1d, 1d};
        common.setYh(yh);
        double[] ewt = new double[] {0d, 1d, 1d};
        common.setEwt(ewt);
        double[][] tesco = new double[12][4];
        tesco[2][1] = 10d;
        common.setTesco(tesco);
        double[] rh = new double[1];

        int orderflag = LSODAIntegrator.orderSwitch(ctx, 0.7d, 1.5d, rh, -1, 10);

        assertEquals(2, orderflag);                       // rhdn > rhup & rhdn > rhsm, but rhdn < 1d and kflag < 0
        assertEquals(1, common.getNq());                  // order down by 1
        assertEquals(1, rh[0]);                           // rh[0] reset to 1
    }

    @Test
    void orderSwitchMultipleFailures() {
        ctx.setNeq(1);
        common.setNq(1);                                        
        common.setMeth(1);
        common.setH(0.5);
        common.setPdlast(1);
        double[] rh = new double[1];

        int kflag = -2;
        int orderflag = LSODAIntegrator.orderSwitch(ctx, 0.7, 1, rh, kflag, 10);

        assertEquals(1, orderflag);                       // rhsm > rhdn & rhsm > rhup, 
        assertEquals(0.2d, rh[0]);                        // tiny step after several failures
        assertEquals(1, common.getNq());                  // same order
    }

    @Test
    void linearSystem() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2;
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {15d};
        double[] t = {5d};
        double tout = 7d;
        double result = 2*tout + 5;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getY();
        System.out.println("output = " + res[1] + ", expected = " + result);

        assertTrue(Math.abs(result-res[1])<1e-8);
    }

    @Test
    void exponentialSystem() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0];
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {5d};
        double[] t = {0d};
        double tout = 4d;
        double result = 5*(Math.exp(tout));

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getY();
        System.out.println("output = " + res[1] + ", expected = " + result + ", diff = " + Math.abs(result-res[1]));

        assertTrue(Math.abs(result-res[1])<1e-8);
    }

    @Test
    void exponentialSystem2() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2 * y[0];
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {5d};
        double[] t = {0};
        double tout = 4;
        double result = 5 * Math.exp(2 * tout);

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getY();
        System.out.println("output = " + res[1] + ", expected = " + result + ", diff = " + Math.abs(result-res[1]));
        assertTrue(Math.abs(result-res[1])<1e-6);
    }

    @Test
    void stiffSystem() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 3;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 1.0E4 * y[1] * y[2] - .04E0 * y[0];
                yDot[2] = 3.0E7 * y[1] * y[1];
                yDot[1] = -1.0 * (yDot[0] + yDot[2]);
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = new double[3];
        double[] rtol = new double[3];
        double[] y = new double[3];
        y[0] = 1.0E0;
        y[1] = 0.0E0;
        y[2] = 0.0E0;
        double[] t = {0d};
        double tout = 0.4;
        // double result = 5 * Math.exp(2 * tout);

        rtol[0] = 1.0E-4;
        rtol[2] = 1.0E-4;
        rtol[1] = 1.0E-4;
        atol[0] = 1.0E-6;
        atol[1] = 1.0E-10;
        atol[2] = 1.0E-6;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);

        for(int i=1; i<=12; i++) {

            solver.lsoda(ctx, y, t, tout);
            double[] result = solver.getY();
            System.out.println("t = " + tout + " -> " + result[1] + ",  " + result[2] + ",  " + result[3]);

            if(ctx.getState()==0) break;
            tout*=10;
        }
        //assertTrue(Math.abs(result-y[0])<1e-6);
    }

    void print2DArray(double[][]a){
        for(int i=0; i<a.length; i++){
            for(int j=0; j<a[0].length; j++){
                System.out.print(a[i][j]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    void print1DArray(int[] a){
        for(int j=0; j<a.length; j++){
            System.out.print(a[j] + " ");
        }
        System.out.println();
    }

}





