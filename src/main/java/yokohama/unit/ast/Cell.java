package yokohama.unit.ast;

import java.util.function.Function;

public interface Cell {
    <T> T accept(CellVisitor<T> visitor);

    default <T> T accept(
            Function<ExprCell, T> visitExprCell_,
            Function<PredCell, T> visitPredCell_
    ) {
        return accept(new CellVisitor<T>() {
            @Override
            public T visitExprCell(ExprCell exprCell) {
                return visitExprCell_.apply(exprCell);
            }
            @Override
            public T visitPredCell(PredCell predCell) {
                return visitPredCell_.apply(predCell);
            }
        });
    }
}
