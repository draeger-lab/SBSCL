import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.LSODA.LSODACommon;
import org.simulator.math.odes.LSODA.LSODAContext;
import org.simulator.math.odes.LSODA.LSODAIntegrator;
import org.simulator.math.odes.LSODA.LSODAOptions;

public class LSODAIntegratorTest {

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


    // /** 
    //  * The following tests are for the function .fnorm() within LSODAIntegrator.java
    //  * The function computes the weighted matrix norm 
    // **/

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
    * The following tests are for the function <code>.prja()</code> within LSODAIntegrator.java
    * The function computes and processes the matrix using finite differencing, calls <code>vmonrm()</code> to calculate the norm of the Jacobian, calls <code>fnorm()</code> to compute the norm of, and performs LU decomposition using <code>dgefa()</code>.
    * 
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

    @Test
    void prja_MiterNotTwo() {
        ctx.setNeq(3);
        common.setMiter(5);

        double[] y = {0d, 1d, 2d, 3d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(0, result);

    }


    @Test
    void prja_ZeroLengthSystem() {
        common.setMiter(2);
        ctx.setNeq(0);
        int[] newIpvt = new int[4];
        common.setIpvt(newIpvt);

        double[] y = {0d};
        int result = LSODAIntegrator.prja(ctx, y);

        assertEquals(1, result);

    }

    @Test
    void prja_AllZero() {
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

    }

    @Test
    void prja_FunctionEvaluationsIncrement() {
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
    }

    /**
     * The following test cases are for the function daxpy() within LSODAIntegrator.java.
     * Daxpy computes <pre>dy = da * dx + dy</pre> for vectors <pre>dx</pre> and <pre>dy</pre> and scalar <pre>da</pre>.
     */
    @Test
    void daxpy_Basic() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 6d, 9d, 12d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_ZeroScalarMultiplier() {
        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_NegativeN() {
        int n = -5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_NonUnitaryIncrements() {
        int n = 2;
        double da = 3d;
        double[] dx = {0d, 1d, 2d, 3d, 4d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 8d, 15d, 7d, 8d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_NegativeStrides() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1, incy = -1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 12d, 9d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_AllZeroDx() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 4d, 5d, 6d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 4d, 5d, 6d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_LargeValues() {
        int n = 3;
        double da = 1e9d;
        double[] dx = {0d, 1e9d, -2e9d, 3e9d};
        double[] dy = {0d, 1e9d, 2e9d, -3e9d};
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 1.000000001e18, -1.999999998e18, 2.999999997e18};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    @Test
    void daxpy_InPlaceModification() {
        int n = 3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        double[] dy = dx;
        int incx = 1, incy = 1;

        LSODAIntegrator.daxpy(n, da, dx, incx, incy, dy);

        double[] expectedDy = {0d, 3d, 6d, 9d};
        assertArrayEquals(expectedDy, dy, 1e-6);
    }

    /**
     * The following tests are for the functions dgefa() within LSODAIntegrator
     */
    @Test
    void dgefa_Basic() {
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
    }

    @Test
    void dgefa_UpperTriangular() {
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
    }

    @Test
    void dgefa_SingularMatrix() {
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
    }

    @Test
    void dgefa_IdentityMatrix() {
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
    }

    @Test
    void dgefa_RowSwapping() {
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
    }


    /**
     * The following test cases are for the function dscal() within LSODAIntegrator
     */
    @Test
    void dscal_Basic() {
        int n = 5;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 2d, 4d, 6d, 8d, 10d};
        assertArrayEquals(expected, dx, 1e-6);
    }

    @Test
    void dscal_ZeroLengthVector() {
        int n = 0;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);   
    }

    @Test
    void dscal_NegativeN() {
        int n = -3;
        double da = 2d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);   
    }

    @Test
    void dscal_UnitaryScalar() {
        int n = 3;
        double da = 1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 1d, 2d, 3d};
        assertArrayEquals(expected, dx, 1e-6);   
    }

    @Test
    void dscal_ZeroScalar() {
        int n = 3;
        double da = 0d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, 0d, 0d, 0d};
        assertArrayEquals(expected, dx, 1e-6);   
    }

    @Test
    void dscal_NegativeScalar() {
        int n = 3;
        double da = -1d;
        double[] dx = {0d, 1d, 2d, 3d};
        int incx = 1;

        LSODAIntegrator.dscal(n, da, incx, dx);

        double[] expected = {0d, -1d, -2d, -3d};
        assertArrayEquals(expected, dx, 1e-6);   
    }

    @Test
    void dscal_FractionalScalar() {
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
    void solsy_BasicLUDecomposition() {
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
    void solsy_MiterNotTwo() {
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
    void solsy_ZeroNeq() {
        ctx.setNeq(0);
        common.setMiter(2);
        double[] y = new double[0];

        double[][] wm = new double[1][1];
        int[] ipvt = new int[1];
        common.setWm(wm);
        common.setIpvt(ipvt);

        int result = LSODAIntegrator.solsy(ctx, y);
        assertEquals(1, result);
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
    void dgesl_Basic() {
        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 2d, 1d},
            {0d, 5d, 7d}
        };
        int[] ipvt = {0, 1, 2};
        double[] b = {0d, 11d, 13d};

        LSODAIntegrator.dgesl(a, n, ipvt, b, 0);

        assertArrayEquals(new double[]{0, 3.4285714286, -2.0714285714}, b, 1e-10);
    }

        /**
     * Test solving A^T * x = b for a simple 2x2 system with row swaps and transposition.
     * A = [2 1]
     *     [5 7]
     * b = [11, 13]
     * Solution should be x ≈ [-3.07, 0.343]
     */
    @Test
    void dgesl_TransposeSolve() {
        int n = 2;
        double[][] a = {
            {0d, 0d, 0d},  // Unused row (1-based index)
            {0d, 2d, 1d},  // Row 1
            {0d, 5d, 7d}   // Row 2
        };
        int[] ipvt = {0, 1, 2}; // No row swaps
        double[] b = {0d, 11d, 13d};

        LSODAIntegrator.dgesl(a, n, ipvt, b, 1);

        assertArrayEquals(new double[]{0, -3.0714285714285716, 3.4285714285714284}, b, 1e-6, "Incorrect solution for A^T x = b");
    }

    /** solving for a larger 4x4 system */
    @Test
    void dgesl_SingularMatrix() {
    int n = 2;
    double[][] a = {
        {0d, 0d, 0d},    
        {0d, 1d, 2d},    
        {0d, 0d, 0d}     
    };
    int[] ipvt = {0, 1, 2};
    double[] b = {0d, 3d, 6d};

    assertThrows(IllegalArgumentException.class, () -> {
        LSODAIntegrator.dgesl(a, n, ipvt, b, 0);
    }, "Matrix is singular or nearly singular at index 2");
    }

    /* Solving for an identity matrix */
    @Test
    void dgesl_IdentityMatrix() {
        double[][] a = {
            {0d, 0d, 0d},
            {0d, 1d, 0d},
            {0d, 0d, 1d}
        };
        int n = 2;
        int[] ipvt = {0, 1, 2};
        double[] b = {0d, 3d, 4d};

        LSODAIntegrator.dgesl(a, n, ipvt, b, 0);

        assertArrayEquals(new double[] {0d, 3d, 4d}, b, 1e-6);
    }

    /** The following tests are for the function corfailure() within LSODAIntegrator */
    @Test
    void corfailure_Basic() {
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
    void corfailure_HTooSmall() {
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
        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(2, result);
        assertEquals(1, common.getNcf());
    }

    @Test
    void corfailure_NcfLimit() {
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
        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(2, result);
        assertEquals(common.MXNCF, common.getNcf());
    }

    @Test
    void corfailure_ZeroNq() {
        ctx.setNeq(2);
        opt.setHmin(0.1);
        common.setH(1.0);
        common.setNq(0);
        common.setNcf(0);
        common.setMiter(6);

        double[][] yh = new double[1][3];
        common.setYh(yh);

        double told = 10.0;
        int result = LSODAIntegrator.corfailure(ctx, told);

        assertEquals(1, result);
        assertEquals(common.getMiter(), common.getIpup());
    }

    /** The following tests are for checkOpt() within LSODAIntegrator */
    @Test
    void checkOpt_BasicCase_State0() {
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
    void checkOpt_BasicCase_State3() {
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
       void checkOpt_NeqZero() {
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
    void checkOpt_NegativeRtol() {
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
    void checkOpt_NegativeAtol() {
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
    void checkOpt_IllegalItaskHigh() {
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
    void checkOpt_IllegalItaskLow() {
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
    void checkOpt_IllegalIxprLow() {
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
    void checkOpt_IllegalIxprHigh() {
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
    void checkOpt_NegativeMxstep() {
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
    void checkOpt_MxstepZero() {
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
    void checkOpt_NegativeMxhnil() {
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
    void checkOpt_NegativeMxordn() {
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
    void checkOpt_MxordnZero() {
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
    void checkOpt_NegativeMxords() {
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
    void checkOpt_MxordsZero() {
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
    void checkOpt_NegativeHmax() {
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
    void checkOpt_HmxiCalculation() {
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
    void checkOpt_NegativeHmin() {
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

}


