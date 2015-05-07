package yokohama.unit.ast;

public interface CellVisitor<T> {
    T visitExprCell(ExprCell exprCell);
    T visitPredCell(PredCell predCell);
}
