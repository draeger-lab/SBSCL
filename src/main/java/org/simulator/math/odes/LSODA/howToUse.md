## How to Use LSODAIntegrator in SBSCL

The `LSODAIntegrator` can be used similarly to other solvers in SBSCL. Below is a step-by-step guide to using it with both SBML models and directly defined ODE systems.

### 1. Usage with SBML Models

**Example Code**:
```
// Parse SBML model
Model model = (new SBMLReader()).readSBML(sbmlFile).getModel();
SBMLInterpreter interpreter = new SBMLInterpreter(model);

// Initialize solver with tolerances
double relTol = 1e-8;
double absTol = 1e-10;
AbstractDESolver solver = new LSODAIntegrator(relTol, absTol);

// Define time points for simulation
double[] timePoints = {0.1, 0.2, 0.3, 0.5};

// Solve the model
Multitable solution = solver.solve(interpreter, interpreter.getInitialValues(), timePoints);
```
**Steps:**
1. Parse the SBML file using `SBMLReader.readSBML()` It returns a `SBMLDocument` object.

2. Retrieve the `Model` from the `SBMLDocument` using `.getModel()`.

3. Initialize an `SBMLInterpreter` with the `Model`.

4. Create a `LSODAIntegrator` instance with desired tolerance values. There are a couple of options to do so.

    - **Single tolerance for all variables:**

    ```
    double relTol = 1e-8;
    double absTol = 1e-10;
    AbstractDESolver solver = new LSODAIntegrator(relTol, absTol);
    ```
    
    - **Variable-specific tolerance:**

    ```
    double[] relTol = {1e-8, 1e-8, 1e-10};
    double[] absTol = {1e-10, 1e-10, 1e-12};
    AbstractDESolver solver = new LSODAIntegrator(relTol, absTol);
    ```

    - **Using setters after construction:**

    ```
    AbstractDESolver solver = new LSODAIntegrator();
    solver.setRelTol(1e-8);
    solver.setAbsTol(1e-10);
    // Only single values allowed via setters.
    ```

5. Use `solve()` method to get the solution in `Multitable` format.

The `solve()` method can throw exceptions, so wrap it in a `try-catch` block if needed. Also, `solve()` is overloaded, check `AbstractDESolver` documentation for more variants.

### 2. Usage with Direct ODE Systems
You can also use `LSODAIntegrator` directly with a custom `DESystem`.

**Example Code:**
```
// Define a custom ODE system
DESystem system = new DESystem() {
    public int getDimension() {
        return 1;
    }

    public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
        yDot[0] = 2;
    }

    // Other methods can throw UnsupportedOperationException if unused
    public String[] getIdentifiers() { throw new UnsupportedOperationException(); }
    public boolean containsEventsOrRules() { throw new UnsupportedOperationException(); }
    public int getPositiveValueCount() { throw new UnsupportedOperationException(); }
    public void setDelaysIncluded(boolean delaysIncluded) { throw new UnsupportedOperationException(); }
};

// Initialize solver
LSODAIntegrator solver = new LSODAIntegrator(1e-8, 1e-10);

// Prepare solver
solver.prepare(system, 1, 1, 1); // The arguments are DESystem, ixpr, itask and state in order.

// Solve using lsoda()
int flag = solver.lsoda(y, tInitial, tOut);
double[] result;
if (flag > 0) {
    result = solver.getResult();
}
```

**Steps:**

1. Implement a `DESystem` with your desired ODE.

2. Initialize `LSODAIntegrator` with required tolerances. You can follow any of the way listed above.

3. Call `prepare()`. This method is overloaded, here are the different variants.

    - `prepare(DESystem, ixpr, itask, istate)`. Default value of maxStep is 500.

    - `prepare(DESystem, ixpr, itask, istate, maxStep)`

    - Here is more detail about these variables.
        - `ixpr` - integer flag that controls whether to print messages. 1 means Yes and 0 means No.
        - `itask` - integer flag that controls the behavior of the solver, specifically when to return the solution. It determines whether the solver should proceed with integration, return the solution at a specific `tout` value, or handle other specific tasks. 

            itask = 1: This is the normal mode. The solver proceeds with integration until it reaches the specified `tout` value. 

            itask = 2: The solver returns the solution at the current time `t` but does not advance the time. 

            itask = 3: The solver returns the solution at `tout` and then stops. 

            itask = 4: The solver attempts to reach `tout` but will stop if a critical point `tcrit` is encountered before `tout`. 
            
            itask = 5: Similar to itask=4, the solver aims to reach `tout`, but it also returns control to the user if a critical point is encountered. 

        - `istate` - integer flag that controls the state of the solver and provides information about the outcome of the integration.

            istate = 1: Indicates a normal start of the integration. LSODA will initialize itself and begin solving the ODEs from the initial conditions.

            istate = 2: Indicates a continuation of a previous integration. LSODA will continue from the last successful step, using internal information saved from the previous call.

        - `mxstep` - integer value specifying the maximum number of steps allowed while solving the integration. The default value is 500. 

4. Call `lsoda()`, with the initial values and time at which integration is required, to compute the solution.

5. Retrieve results using `getResult()`.

**Other Ways**

If you need more control over the solver's internals, you can define `LSODAContext` and `LSODAOptions` directly and then use those with `lsoda()`.

```
// Define the ODE system
DESystem system = new DESystem() {
    // Define computeDerivatives, getDimension, etc.
};

// Tolerance Values
double[] atol = {1e-12};
double[] rtol = {1e-12};

// LSODAOptions object
LSODAOptions opt = new LSODAOptions();
opt.setIxpr(0);
opt.setItask(1);
opt.setAtol(atol);
opt.setRtol(rtol);

// LSODAContext object
LSODAContext ctx = new LSODAContext();
ctx.setNeq(system.getDimension());
ctx.setOdeSystem(system);
ctx.setState(1);

// Initialize solver
LSODAIntegrator solver = new LSODAIntegrator();

// Prepare solver
solver.lsodaPrepare(ctx, opt);

// Solve 
int flag = solver.lsoda(ctx, y, tInitial, tOut);
double[] result;
if(flag > 0) {
    result = solver.getResult();
}
```

### Others
You can further access and modify internals anytime using these methods:
```
LSODAContext context = solver.getContext();
LSODAOptions options = context.getOpt();
LSODACommon common = context.getCommon();
```
This is especially useful for debugging or research purposes where inspecting or adjusting the internals is required.

### Notes
- `LSODAIntegrator` extends `AbstractDESolver`, so it fits seamlessly into the SBSCL framework.

- Supports both SBML-based biological systems and custom ODEs.

- Offers flexible tolerance and configuration options.