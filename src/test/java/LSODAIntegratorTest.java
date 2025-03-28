import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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

    /** The following tests are for checkOpt() within LSODAIntegrator.java */
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

    /** The following test cases are for the function correction() within LSODAIntegrator.java */

    @Test
    void correction_Basic() {
        ctx.setNeq(3);
        
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
        
        double[] el = new double[2];
        el[1] = 1.0;
        common.setEl(el);
        double[] ewt = new double[ctx.getNeq() + 1];
        for (int i = 1; i <= ctx.getNeq(); i++) {
            ewt[i] = 1.0;
        }
        common.setEwt(ewt);
        
        double pnorm = 1e-3;
        double told = 10.0;
        double[] y = new double[ctx.getNeq() + 1];
        double[] del = new double[1];
        double[] delp = new double[1];
        int[] m = new int[1];
        common.setNfe(0);
        
        int result = LSODAIntegrator.correction(ctx, y, pnorm, del, delp, told, m);
        
        assertEquals(0, result);
        assertEquals(0, m[0]);
        assertEquals(0.0, del[0], 1e-9);
    }

    /* The following tests are for the function scaleh() within LSODAIntegrator.java */
    @Test
    void scaleh_NoStability() {
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
        
        // Expected effective rh:
        //   rh = fmin(0.4, 0.5) = 0.4; denominator = fmax(1, 1.0*0.1*0.4)=1, so rh remains 0.4.
        // Then:
        //   h becomes 1.0 * 0.4 = 0.4
        //   rc becomes 2.0 * 0.4 = 0.8
        //   ialth becomes (nq+1) = 3.
        // And the yh array is updated in the loop:
        //   For j=2: multiplier = 0.4  → row2: {1*0.4, 2*0.4, 3*0.4} = {0.4, 0.8, 1.2}
        //   For j=3: multiplier = 0.4^2 = 0.16 → row3: {4*0.16, 5*0.16, 6*0.16} = {0.64, 0.80, 0.96}
        assertEquals(0.4, common.getH(), 1e-8);
        assertEquals(0.8, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());
        
        double[][] resultYh = common.getYh();
        // Check row 2 scaling
        assertEquals(0.4, resultYh[2][1], 1e-8);
        assertEquals(0.8, resultYh[2][2], 1e-8);
        assertEquals(1.2, resultYh[2][3], 1e-8);
        // Check row 3 scaling
        assertEquals(0.64, resultYh[3][1], 1e-8);
        assertEquals(0.80, resultYh[3][2], 1e-8);
        assertEquals(0.96, resultYh[3][3], 1e-8);
        
        // Since meth != 1, irflag is not changed (assumed default 0)
        assertEquals(0, common.getIrflag());
    }

    @Test
    void scaleh_StabilityNoAdjustment() {
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
        
        // Expected: effective rh remains 1.0.
        // h remains 2.0, rc remains 3.0, ialth becomes 4.
        // No change in yh rows (all multiplications by 1).
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
        
        // irflag should be 0 because the condition did not trigger adjustment.
        assertEquals(0, common.getIrflag());
    }

    @Test
    void scaleh_StabilityAdjustment() {
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
    }

    @Test
    void scaleh_RhLimitedByRmax() {
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
    void scaleh_NegativeH() {
        ctx.setNeq(2);
        common.setH(-1.0);
        common.setRc(-3.0);
        common.setMeth(2);
        common.setPdlast(0.0);
        common.setNq(2);
        
        opt.setHmxi(0.3);
        common.setRmax(2.0);
        
        double[][] yh = new double[common.getNq() + 2][ctx.getNeq() + 1];
        yh[2][1] = -2;  yh[2][2] = -4;
        yh[3][1] = -6;  yh[3][2] = -8;
        common.setYh(yh);
        
        double rhInput = 1.0;
        LSODAIntegrator.scaleh(ctx, rhInput);
        
        // Expected effective rh:
        //   rh = fmin(1.0,2.0) = 1.0; denominator = fmax(1, | -1.0 |*0.3*1.0 = 0.3)=1.
        // So rh remains 1.0 and no scaling occurs.
        // h remains -1.0, rc remains -3.0, and ialth becomes 3.
        double[][] resultYh = common.getYh();
        assertEquals(-1.0, common.getH(), 1e-8);
        assertEquals(-3.0, common.getRc(), 1e-8);
        assertEquals(3, common.getIalth());
        assertEquals(-2, resultYh[2][1], 1e-8);
        assertEquals(-4, resultYh[2][2], 1e-8);
        assertEquals(-6, resultYh[3][1], 1e-8);
        assertEquals(-8, resultYh[3][2], 1e-8);
    }

    /* The following tests are for the function .cfode() within LSODAIntegrator.java */
    @Test
    void cfode_Method1() {
        LSODAIntegrator.cfode(ctx, 1);

        // For order 1 the algorithm sets:
        //   _C(elco)[1][1] = 1.0, _C(elco)[1][2] = 1.0,
        //   _C(tesco)[1][1] = 0.0, _C(tesco)[1][2] = 2.0.
        double elco11 = common.getElco()[1][1];
        double elco12 = common.getElco()[1][2];
        double tesco11 = common.getTesco()[1][1];
        double tesco12 = common.getTesco()[1][2];
        assertEquals(1.0, elco11, 1e-10, "Order 1 elco[1][1] should be 1.0");
        assertEquals(1.0, elco12, 1e-10, "Order 1 elco[1][2] should be 1.0");
        assertEquals(0.0, tesco11, 1e-10, "Order 1 tesco[1][1] should be 0.0");
        assertEquals(2.0, tesco12, 1e-10, "Order 1 tesco[1][2] should be 2.0");

        // In addition, the algorithm explicitly sets:
        //   _C(tesco)[2][1] = 1.0 and _C(tesco)[12][3] = 0.0.
        double tesco21 = common.getTesco()[2][1];
        double tesco123 = common.getTesco()[12][3];
        assertEquals(1.0, tesco21, 1e-10, "Order 2 tesco[2][1] should be 1.0");
        assertEquals(0.0, tesco123, 1e-10, "Order 12 tesco[12][3] should be 0.0");
    }

    @Test
    void cfode_CoefficentConsistency() {
        LSODAIntegrator.cfode(ctx, 1);
        
        // For each order from 2 to 12, check that the first coefficient (elco[nq][1]) is not zero.
        for (int nq = 2; nq <= 12; nq++) {
            double coeff = common.getElco()[nq][1];
            assertNotEquals(0.0, coeff, 1e-10, "Coefficient elco[" + nq + "][1] must be nonzero");
        }
    }

    @Test
    void cfode_InitalCoefficients() {
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

        // For order 2, we can check one computed value.
        // For example, from a manual simulation one finds:
        //   _C(elco)[2][1] should equal 2/3.
        double elco21 = common.getElco()[2][1];
        assertEquals(2.0 / 3.0, elco21, 1e-10, "For meth==2, order 2: elco[2][1] should be 2/3");

        // And for order 2, the test constants:
        //   _C(tesco)[2][1] = previous rq1fac = 1, 
        //   _C(tesco)[2][2] = (nqp1)/elco[2][1] = 3/(2/3) = 4.5,
        //   _C(tesco)[2][3] = (nq+2)/elco[2][1] = 4/(2/3) = 6.
        double tesco21 = common.getTesco()[2][1];
        double tesco22 = common.getTesco()[2][2];
        double tesco23 = common.getTesco()[2][3];
        assertEquals(1.0, tesco21, 1e-10, "For meth==2, order 2: tesco[2][1] should be 1.0");
        assertEquals(4.5, tesco22, 1e-10, "For meth==2, order 2: tesco[2][2] should be 4.5");
        assertEquals(6.0, tesco23, 1e-10, "For meth==2, order 2: tesco[2][3] should be 6.0");
    }

    @Test
    void cfode_AlternateMeth() {
        LSODAIntegrator.cfode(ctx, 3);

        // The same initial values as for meth==2 should appear.
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
    void cfode_RepeatedCalls() {
        // First call with meth==1.
        LSODAIntegrator.cfode(ctx, 1);
        double elco11Meth1 = common.getElco()[1][1];
        double tesco11Meth1 = common.getTesco()[1][1];
        
        // Next, call with meth==2.
        LSODAIntegrator.cfode(ctx, 2);
        double elco11Meth2 = common.getElco()[1][1];
        double tesco11Meth2 = common.getTesco()[1][1];
        
        // For order 1, elco[1][1] remains 1.0 in both cases.
        assertEquals(1.0, elco11Meth1, 1e-10, "Meth==1: elco[1][1] must be 1.0");
        assertEquals(1.0, elco11Meth2, 1e-10, "Meth==2: elco[1][1] must be 1.0");
        // However, tesco[1][1] is 0.0 for meth==1 and 1.0 for meth==2.
        assertNotEquals(tesco11Meth1, tesco11Meth2, "tesco[1][1] should change between methods");
    }

    /* The following tests are for the function .ewset() within LSODAIntegrator.java */
    @Test
    void ewset_Basic() {
        ctx.setNeq(3);
        opt.setRtol(new double[] {0d, 0.1, 0.1, 0.1});
        opt.setAtol(new double[] {0d, 0.001, 0.001, 0.001});
        double[] y = {0d, 1d, 2d, 3d};
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        assertEquals(1/(0.1*1d + 0.001), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0 + 0.001), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0 + 0.001), ewt[3], 1e-6);
    }

    @Test
    void ewset_ZeroYValues() {
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, 0.0, 0.0, 0.0 };

        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();

        for (int i = 1; i <= 3; i++) {
            assertEquals(1000d, ewt[i], 1e-6);
        }
    }

    @Test
    void ewset_NegativeYValues() {
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, -1.0, -2.0, -3.0 };
        
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();
        
        assertEquals(1/(0.1*1.0 + 0.001), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0 + 0.001), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0 + 0.001), ewt[3], 1e-6);
    }

    @Test
    void ewset_ZeroRelativeTolerance() {
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.0, 0.0, 0.0 });
        opt.setAtol(new double[] { 0, 0.001, 0.001, 0.001 });
        double[] y = { 0, 10.0, 20.0, 30.0 };
        
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();
        
        for (int i = 1; i <= 3; i++) {
            assertEquals(1000.0, ewt[i], 1e-6);
        }
    }

    @Test
    void ewset_ZeroAbsoluteTolerance_NonZeroY() {
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.0, 0.0, 0.0 });
        double[] y = { 0, 1.0, 2.0, 3.0 };
        
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();
        
        assertEquals(1/(0.1*1.0), ewt[1], 1e-6);
        assertEquals(1/(0.1*2.0), ewt[2], 1e-6);
        assertEquals(1/(0.1*3.0), ewt[3], 1e-6);
    }
    
    @Test
    void ewset_ZeroAbsoluteTolerance_ZeroY() {
        ctx.setNeq(3);
        opt.setRtol(new double[] { 0, 0.1, 0.1, 0.1 });
        opt.setAtol(new double[] { 0, 0.0, 0.0, 0.0 });
        double[] y = { 0, 0.0, 0.0, 0.0 };
        
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();
        
        for (int i = 1; i <= 3; i++) {
            assertTrue(Double.isInfinite(ewt[i]));
        }
    }
    
    @Test
    void ewset_NoEquations() {
        ctx.setNeq(0);
        opt.setRtol(new double[] { 0 });
        opt.setAtol(new double[] { 0 });
        double[] y = { 0 };
        
        LSODAIntegrator.ewset(y, opt.getRtol(), opt.getAtol(), ctx.getNeq(), common);
        double[] ewt = common.getEwt();
        
        assertEquals(1, ewt.length);
    }


    /* The following tests are for the function .intdy() within LSODAIntegrator.java */
    /**
     * Valid test with k = 0.
     * The function should compute the 0th-derivative by returning yh[1].
     */
    @Test
    void testIntdy_k0_valid() {
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0; yh[1][2] = 2.0;
        yh[2][1] = 3.0; yh[2][2] = 4.0;
        yh[3][1] = 5.0; yh[3][2] = 6.0;
        common.setYh(yh);

        double t = 10d;
        int k = 0;
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k , dky);
        assertEquals(0, res);
        assertEquals(1d, dky[1], 1e-6);
        assertEquals(2d, dky[2], 1e-6);
    }

    /**
     * Valid test with k = 1.
     * Using the same context as k0_valid, but with t set to (tcur - h),
     * the computed first derivative is manually checked.
     */
    @Test
    void testIntdy_k1_valid() {
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0; yh[1][2] = 2.0;
        yh[2][1] = 3.0; yh[2][2] = 4.0;
        yh[3][1] = 5.0; yh[3][2] = 6.0;
        common.setYh(yh);

        double t = 9.5;
        int k = 1;
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k, dky);
        assertEquals(0, res);
        assertEquals(-14.0, dky[1], 1e-6);
        assertEquals(-16.0, dky[2], 1e-6);
    }

    /**
     * Valid test with k equal to _C(nq).
     */
    @Test
    void testIntdy_kEqualsNq() {
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10.0);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0; yh[1][2] = 2.0;
        yh[2][1] = 3.0; yh[2][2] = 4.0;
        yh[3][1] = 5.0; yh[3][2] = 6.0;
        common.setYh(yh);

        double t = 10.0;
        int k = 2;
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k, dky);
        assertEquals(0, res);
        assertEquals(40.0, dky[1], 1e-6);
        assertEquals(48.0, dky[2], 1e-6);
    }

        /**
     * Error test when k is negative.
     * The function should return -1 if k < 0.
     */
    @Test
    void testIntdy_negativeK() {
        ctx.setNeq(1);
        common.setNq(1);
        common.setTn(5.0);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[3][2];
        yh[1][1] = 10.0;
        yh[2][1] = 20.0;
        common.setYh(yh);

        double t = 5.0;
        int k = -1;  // Illegal value
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k, dky);
        assertEquals(-1, res);
    }

        /**
     * Error test when k is greater than _C(nq).
     * For example, if _C(nq)=1 then k=2 should be illegal.
     */
    @Test
    void testIntdy_kGreaterThanNq() {
        ctx.setNeq(1);
        common.setNq(1);
        common.setTn(5.0);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[3][2];
        yh[1][1] = 10.0;
        yh[2][1] = 20.0;
        common.setYh(yh);

        double t = 5.0;
        int k = 2;
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k, dky);
        assertEquals(-1, res);
    }


    /**
     * Error test when t is out of bounds.
     * Here we choose a t value that is greater than _C(tn)
     * (i.e. outside the interval [tcur - _C(hu) - 100*ETA*(tcur+_C(hu)), tcur]).
     */
    @Test
    void testIntdy_tOutOfBounds() {
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10.0);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[4][3];
        yh[1][1] = 1.0; yh[1][2] = 2.0;
        yh[2][1] = 3.0; yh[2][2] = 4.0;
        yh[3][1] = 5.0; yh[3][2] = 6.0;
        common.setYh(yh);

        double t = 10.1;
        int k = 0;
        double[] dky = new double[ctx.getNeq() + 1];

        int res = LSODAIntegrator.intdy(ctx, t, k, dky);
        assertEquals(-2, res);
    }

    /* The following tests are for .intdyReturn() within LSODAIntegrator.java */
    /**
     *  iflag == 0
    */
    @Test
    void intdyReturn_Basic() {
        ctx.setNeq(2);
        common.setNq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);

        double[][] yh = new double[4][3];
        yh[1][1] = 5.0;  yh[1][2] = 6.0;
        common.setYh(yh);
        
        double tout = 10.0;
        double[] t = new double[1]; 
    
        double[] y = new double[ctx.getNeq() + 1];
        y[1] = 1d;
        y[2] = 2d;
        
        int state = LSODAIntegrator.intdyReturn(ctx, y, t, tout, opt.getItask());
        
        assertEquals(2, state);
        assertEquals(tout, t[0], 1e-10);
    }

    @Test
    void intdyReturn_errorCase() {
        ctx.setNeq(2);
        ctx.setNeq(2);
        common.setTn(10d);
        common.setH(0.5);
        common.setHu(0.5);
        double[][] yh = new double[4][3];
        yh[1][1] = 5d; yh[1][2] = 6;
        common.setYh(yh);

        double tout = 10d;
        double[] t = new double[1];

        double[] y = new double[ctx.getNeq() + 1];
        y[1] = 1d; y[2] = 2d;

        int state = LSODAIntegrator.intdyReturn(ctx, y, t, tout, opt.getItask());

        assertEquals(2, state);
        assertEquals(tout, t[0], 1e-10);

        assertEquals(5d, y[1], 1e-10);
        assertEquals(6d, y[2], 1e-10);
    }



}






