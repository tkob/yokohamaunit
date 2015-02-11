/**
 * Abstract Syntax Tree.
 * 
 * <pre>
 * Group ::= Definition*
 * 
 * Definition ::= Test | FourPhaseTest | Table
 * 
 * Test ::= Assertion*
 * 
 * Assertion ::= Proposition* Fixture
 * 
 * Proposition ::= QuotedExpr Predicate
 * 
 * Predicate ::= IsPredicate | IsNotPredicate | ThrowsPredicate
 * 
 * IsPredicate ::= Matcher
 * IsNotPredicate ::= Matcher
 * ThrowsPredicate ::= Matcher
 * 
 * Matcher ::= EqualToMatcher
 *           | InstanceOfMatcher
 *           | InstanceSuchThatMatcher
 *           | NullValueMatcher
 * 
 * EqualToMatcher ::= QuotedExpr
 * InstanceOfMatcher ::= ClassType
 * InstanceSuchThatMatcher ::= ClassType Proposition+
 * 
 * Expr ::= QuotedExpr | StubExpr
 * 
 * StubExpr ::= QuotedExpr StubBehavior*
 * 
 * StubBehavior ::= MethodPattern Expr
 * 
 * MethodPattern ::= Type*
 * 
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * Fixture ::= | TableRef | Bindings
 * 
 * TableRef ::= TableType
 * 
 * TableType :: INLINE | CSV | TSV | EXCEL
 * 
 * Bindings ::= Binding*
 * 
 * FourPhaseTest ::= Phase? Phase? VerifyPhase Phase?
 * 
 * Phase ::= LetBindings? Execution?
 * VerifyPhase ::= Assertion+
 * 
 * Execution ::= QuotedExpr*
 * 
 * Table ::= Row*
 * 
 * Row ::= Expr*
 * </pre>
 * 
 */
package yokohama.unit.ast;