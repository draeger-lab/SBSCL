import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.LSODA.LSODACommon;
import org.simulator.math.odes.LSODA.LSODAContext;
import org.simulator.math.odes.LSODA.LSODAIntegrator;

public class LSODAIntegratorTest {

    LSODACommon common = new LSODACommon();
    LSODAContext ctx = new LSODAContext(common);
    LSODAIntegrator integrator = new LSODAIntegrator(ctx);
    boolean prepared = integrator.lsoda_prepare(ctx, ctx.getOpt());

    /** 
     * The following tests are for the function .ddot() within LSODAIntegrator.java 
     * The function calculates the dot product of two vectors.
    **/

    /** Basic dot product test, incx = incy = 1 */
    @Test
    void Ddot_BasicDotProduct() {
        int n = 3;
        double[] dx = {0d, 1.0, 2.0, 3.0};
        double[] dy = {0d, 4.0, 5.0, 6.0};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((1d*4d + 2d*5d + 3d*6d), result, 1e-6);
    }

    /** Test vector with length = 0 */
    @Test
    void Ddot_ZeroLengthVector() {
        int n = 0;
        double[] dx = {0};
        double[] dy = {0};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0, result, 1e-6);
    }

    /** Test where n < 0 */
    @Test
    void Ddot_NegativeN() {
        int n = -5;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 2d, 4d, 6d, 8d, 10d};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0d, result, 1e-6);
    }

    /** Test different values for incx and incy */
    @Test
    void Ddot_DifferentStrides() {
        int n = 3;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((1d * 5d + 3d * 6d + 5d * 7d), result, 1e-6);
    }

    /** Test with incx = incy = -1 */
    @Test
    void Ddot_NegativeStrides() {
        int n = 3;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1; int incy = -1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((3d * 6d + 2d * 5d + 1d * 4d), result, 1e-6);
    }

    /** Test with all values = 0 */
    @Test
    void Ddot_AllZero() {
        int n = 3;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 0d, 0d, 0d};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0d, result, 1e-6);
    }

    /** Test with a single-element vector */
    @Test
    void Ddot_SingleElement() {
        int n = 1;
        double[] dx = {0d, 7.5};
        double[] dy = {0d, 2d};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((7.5 * 2d), result, 1e-6);
    }

    @Test
    void Ddot_NaN() {
        int n = 2;
        double[] dx = {0d, Double.NaN, 2d};
        double[] dy = {0d, 5d, Double.NaN};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(Double.NaN, result, 1e-6);
    }

    
    /** 
     * The following tests are for the function .vmnorm within LSODAIntegrator.java 
     * The function calculates the weighted max-norm of a vector.
    **/

    /** Basic max-norm test */
    @Test
    void Vmnorm_BasicMaxNorm() {
        int n = 3;
        double[] v = {0d, 1d, -2d, 3d};
        double[] w = {0d, 4d, 1.5, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(Math.max(Math.abs(1d) * 4d, Math.max(Math.abs(-2d) * 1.5, Math.abs(3d) * 2.0)), result, 1e-6);
    }

    /** Test vector with all elements zero */
    @Test
    void Vmnorm_AllZero() {
        int n = 3;
        double[] v = {0d, 0d, 0d, 0d};
        double[] w = {0d, 0d, 0d, 0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6);
    }


    /** Test single element */
    @Test
    void Vmnorm_SingleElement() {
        int n = 1;
        double[] v = {0d, 7.5};
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(Math.abs(7.5) * 2d, result, 1e-6);
    }

    /** Test negative values in v */
    @Test
    void Vmnorm_NegativeValues() {
        int n = 3;
        double[] v = {0d, -1d, -2d, -3d};
        double[] w = {0d, 1d, 2d, 3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(3d * 3d, result, 1e-6);
    }

    /** Test negative values in w */
    @Test
    void Vmnorm_NegativeWeights() {
        int n = 3;
        double[] v = {0d, 1d, 2d, 3d};
        double[] w = {0d, -1d, -2d, -3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(3d * 3d, result, 1e-6);
    }

    /** Test zero length vector */
    @Test
    void Vmnorm_ZeroLengthVector() {
        int n = 0;
        double[] v = {0d};
        double[] w = {0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6);
    }


    /** 
     * The following tests are for the function .fnorm() within LSODAIntegrator.java
     * The function computes the weighted matrix norm 
    **/

    /** Basic matrix norm */
    @Test
    void Fnorm_BasicMatrixNorm() {
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

        assertEquals(expected, result, 1e-6);
    }

    /** Matrix has all zero elements */
    @Test
    void Fnorm_AllZero() {
        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 0d, 0d},
            {0d, 0d, 0d}
        };
        double[] w = {0d, 2d, 1d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(0d, result, 1e-6);
    }

    /** Matrix has a single element (n = 1) */
    @Test
    void Fnorm_SingleElement() {
        int n = 1;
        double[][] a = {
            {0d, 0d},
            {0d, -7.5}
        };
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.fnorm(n, a, w);

        assertEquals(Math.abs(-7.5), result, 1e-6);
    }

    /** Matrix has negative values */
    @Test
    void Fnorm_NegativeValues() {
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

        assertEquals(expected, result, 1e-6);
    }

    /** Negative weights in w */
    @Test
    void Fnorm_NegativeWeights() {
        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 1d, -2d},
            {0d, 3d, 4d}
        };
        double[] w = {0d, -1d, -2d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertTrue(result > 0);
    }

    /** Zero length matrix (n = 0) */
    @Test
    void Fnorm_ZeroLengthMatrix() {
        int n = 0;
        double[][] a = {{0d}};
        double[] w = {0d};

        double result = LSODAIntegrator.fnorm(n, a, w);
        assertEquals(0d, result, 1e-6);
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
    void Idamax_Basic() {
        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
        int incx = 1;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
    
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
    void Idamax_EmptyVector() {
        int n = 0;
        double[] dx = {0d, 1d, 2d};
        int incx = 1;

        int expected = 0;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
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
    void Idamax_SingleElement() {
        int n = 1;
        double[] dx = {0d, 3.14};
        int incx = 1;

        int expected = 1;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
    
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
    void Idamax_MultipleElementsIncxOne() {
        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
        int incx = 1;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
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
    void Idamax_MultipleElementsIncxTwo() {
        int n = 3;
        double[] dx = {0d, 1d, 0d, -4d, -5d, 3d, 3d};
        int incx = 2;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
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
    void Idamax_NegativeIncrement() {
        int n = 3;
        double[] dx = {0d, 1d, -3d, 2d};
        int incx = -1;

        int expected = 1;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
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
    void Idamax_MultipleElementsIncxThree() {
        int n = 4;
        double[] dx = {0d, 2d, 0d, 0d, -5d, 0d, 0d, 3d, 0d, 0d, -5d, 0d, 0d};
        int incx = 3;

        int expected = 2;
        int result = LSODAIntegrator.idamax(n, dx, incx);
        assertEquals(expected, result);
    }

    /**
    * The following tests are for the function .prja() within LSODAIntegrator.java
    * The function scales, computes the norm of, modifies and performs LU decomposition on the Jacobian matrix.
    */
    @Test
    void prja_Basic() {
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

    }
    
}


