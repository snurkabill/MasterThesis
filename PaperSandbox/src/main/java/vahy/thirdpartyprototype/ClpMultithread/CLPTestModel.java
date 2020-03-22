package vahy.thirdpartyprototype.ClpMultithread;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;

/**
 * CLPmodel is a CLP model for the assignment problem.
 * @author Paul A. Rubin (rubin@msu.edu)
 */
public final class CLPTestModel {
  private final int dim;               // problem dimension
  private final CLP lp;                // lp model
  private final CLPVariable[][] vars;  // matrix of variables
  private CLP.STATUS status;           // solution status

  /**
   * Constructor.
   * @param cost the assignment problem cost matrix (must be square)
   */
  public CLPTestModel(final double[][] cost) {
    dim = cost.length;
    lp = new CLP();
    // create the variables (and set up the objective coefficients)
    vars = new CLPVariable[dim][dim];
    for (int i = 0; i < dim; i++) {
      for (int j = 0; j < dim; j++) {
        vars[i][j] = lp.addVariable()
                       .lb(0.0)
                       .ub(1.0)
                       .name("x_" + i + "_" + j)
                       .obj(cost[i][j]);
      }
    }
    // constraint: every subject must be assigned exactly once
    for (int j = 0; j < dim; j++) {
      CLPExpression expr = lp.createExpression();
      for (int i = 0; i < dim; i++) {
        expr.add(vars[i][j]);
      }
      expr.eq(1.0).name("col_" + j);
    }
    // constraint: every slot must be filled exactly once
    for (int i = 0; i < dim; i++) {
      CLPExpression expr = lp.createExpression();
      for (int j = 0; j < dim; j++) {
        expr.add(vars[i][j]);
      }
      expr.eq(1.0).name("row_" + i);
    }
  }

  /**
   * Solve the model.
   * @return the optimal objective value (NaN if not solved)
   */
  public double solve() {
    status = lp.minimize();
    if (status == CLP.STATUS.OPTIMAL) {
      return lp.getObjectiveValue();
    } else {
      return Double.NaN;
    }
  }

  /**
   * Get the solver status.
   * @return the solver status
   */
  public CLP.STATUS getStatus() {
    return status;
  }

  /**
   * Get the optimal assignments.
   * @return the array of assignment decisions
   */
  public double[][] getAssignments() {
    double[][] x = new double[dim][dim];
    for (int i = 0; i < dim; i++) {
      for (int j = 0; j < dim; j++) {
        x[i][j] = lp.getSolution(vars[i][j]);
      }
    }
    return x;
  }
}
