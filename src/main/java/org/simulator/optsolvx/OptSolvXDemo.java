package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;
import org.optsolvx.model.OptimizationDirection;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;
import org.optsolvx.solver.OptSolvXConfig;


import java.util.HashMap;
import java.util.Map;

public class OptSolvXDemo {
    public static void main(String[] args) {
        // Build a tiny LP: maximize x + y
        AbstractLPModel model = new AbstractLPModel();
        model.addVariable("x", 0.0d, Double.POSITIVE_INFINITY);
        model.addVariable("y", 0.0d, Double.POSITIVE_INFINITY);

        // Objective: max x + y  (Java 8 style map)
        Map<String, Double> objective = new HashMap<>();
        objective.put("x", 1.0d);
        objective.put("y", 1.0d);
        model.setObjective(objective, OptimizationDirection.MAXIMIZE);

        // Constraints (Java 8 style maps)
        Map<String, Double> c1 = new HashMap<>();
        c1.put("x", 1.0d);
        c1.put("y", 2.0d);
        model.addConstraint("c1", c1, Constraint.Relation.LEQ, 4.0d);

        Map<String, Double> c2 = new HashMap<>();
        c2.put("x", 1.0d);
        model.addConstraint("c2", c2, Constraint.Relation.LEQ, 3.0d);

        // Finalize model
        model.build();

        LPSolverAdapter backend = OptSolvXConfig.resolve(model, System.getProperty("optsolvx.solver"));

        LPSolution sol = backend.solve(model);

        // Print result (expected optimum: x=3.0, y=0.5, objective=3.5)
        System.out.println("Variables: " + sol.getVariableValues());
        System.out.println("Objective: " + sol.getObjectiveValue());
        System.out.println("Feasible:  " + sol.isFeasible());
    }
}
