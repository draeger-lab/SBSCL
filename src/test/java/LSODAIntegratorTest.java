import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.LSODA.LSODAIntegrator;

public class LSODAIntegratorTest {

    /** 
     * The following tests are for the function .ddot() within LSODAIntegrator.java 
     * The function calculates the dot product of two vectors.
    **/

    /* Basic dot product test, incx = incy = 1 */
    @Test
    void Ddot_BasicDotProduct() {
        int n = 3;
        double[] dx = {0d, 1.0, 2.0, 3.0};
        double[] dy = {0d, 4.0, 5.0, 6.0};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((1d*4d + 2d*5d + 3d*6d), result, 1e-6);
    }

    /* Test vector with length = 0 */
    @Test
    void Ddot_ZeroLengthVector() {
        int n = 0;
        double[] dx = {0};
        double[] dy = {0};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0, result, 1e-6);
    }

    /* Test where n < 0 */
    @Test
    void Ddot_NegativeN() {
        int n = -5;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 2d, 4d, 6d, 8d, 10d};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0d, result, 1e-6);
    }

    /* Test different values for incx and incy */
    @Test
    void Ddot_DifferentStrides() {
        int n = 3;
        double[] dx = {0d, 1d, 2d, 3d, 4d, 5d};
        double[] dy = {0d, 5d, 6d, 7d, 8d};
        int incx = 2; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((1d * 5d + 3d * 6d + 5d * 7d), result, 1e-6);
    }

    /* Test with incx = incy = -1 */
    @Test
    void Ddot_NegativeStrides() {
        int n = 3;
        double[] dx = {0d, 3d, 2d, 1d};
        double[] dy = {0d, 6d, 5d, 4d};
        int incx = -1; int incy = -1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals((3d * 6d + 2d * 5d + 1d * 4d), result, 1e-6);
    }

    /* Test with all values = 0 */
    @Test
    void Ddot_AllZero() {
        int n = 3;
        double[] dx = {0d, 0d, 0d, 0d};
        double[] dy = {0d, 0d, 0d, 0d};
        int incx = 1; int incy = 1;

        double result = LSODAIntegrator.ddot(n, dx, dy, incx, incy);
        assertEquals(0d, result, 1e-6);
    }

    /* Test with a single-element vector */
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

    /* Basic max-norm test */
    @Test
    void Vmnorm_BasicMaxNorm() {
        int n = 3;
        double[] v = {0d, 1d, -2d, 3d};
        double[] w = {0d, 4d, 1.5, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(Math.max(Math.abs(1d) * 4d, Math.max(Math.abs(-2d) * 1.5, Math.abs(3d) * 2.0)), result, 1e-6);
    }

    /* Test vector with all elements zero */
    @Test
    void Vmnorm_AllZero() {
        int n = 3;
        double[] v = {0d, 0d, 0d, 0d};
        double[] w = {0d, 0d, 0d, 0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6);
    }


    /* Test single element */
    @Test
    void Vmnorm_SingleElement() {
        int n = 1;
        double[] v = {0d, 7.5};
        double[] w = {0d, 2d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(Math.abs(7.5) * 2d, result, 1e-6);
    }

    /* Test negative values in v */
    @Test
    void Vmnorm_NegativeValues() {
        int n = 3;
        double[] v = {0d, -1d, -2d, -3d};
        double[] w = {0d, 1d, 2d, 3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(3d * 3d, result, 1e-6);
    }

    /* Test negative values in w */
    @Test
    void Vmnorm_NegativeWeights() {
        int n = 3;
        double[] v = {0d, 1d, 2d, 3d};
        double[] w = {0d, -1d, -2d, -3d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(3d * 3d, result, 1e-6);
    }

    /* Test zero length vector */
    @Test
    void Vmnorm_ZeroLengthVector() {
        int n = 0;
        double[] v = {0d};
        double[] w = {0d};

        double result = LSODAIntegrator.vmnorm(n, v, w);
        assertEquals(0d, result, 1e-6);
    }
}
