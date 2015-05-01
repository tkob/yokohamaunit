/**
 * Abstract Syntax Tree.
 * 
 * <pre>
 * Group ::= Abbreviation* Definition*
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
 * Expr ::= QuotedExpr | StubExpr | InvocationExpr
 * 
 * StubExpr ::= ClassType StubBehavior*
 * 
 * StubBehavior ::= MethodPattern Expr
 * 
 * MethodPattern ::= Type*
 * 
 * Type ::= NonArrayType
 * NonArrayType ::= PrimitiveType | ClassType
 * PrimitiveType ::= Kind
 * 
 * InvocationExpr ::= MethodPattern Expr*
 * 
 * Fixture ::= | TableRef | Bindings
 * 
 * TableRef ::= Ident+ TableType
 * 
 * TableType :: INLINE | CSV | TSV | EXCEL
 * 
 * Bindings ::= Binding*
 * Binding ::= Ident Expr
 * 
 * FourPhaseTest ::= Phase? Phase? VerifyPhase Phase?
 * 
 * Phase ::= LetBindings? Execution?
 * VerifyPhase ::= Assertion+
 * 
 * LetBindings ::= LetBinding+
 * LetBinding ::= Expr
 * 
 * Execution ::= QuotedExpr*
 * 
 * Table ::= Ident* Row*
 * 
 * Row ::= Expr*
 * </pre>
 * 
 */
package yokohama.unit.ast;