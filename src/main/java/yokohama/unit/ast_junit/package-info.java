/**
 * Abstract Syntax Tree for JUnit.
 * 
 * <pre>
 * CompilationUnit ::= ClassDecl
 * 
 * ClassDecl ::= TestMethod*
 * 
 * TestMethod ::= Binding* ActionStatement* TestStateMent* ActionStatement*
 * 
 * TestStatement ::= IsStatement
 *                 | IsNotStatement
 *                 | ThrowsStatement
 * </pre>
 * 
 */
package yokohama.unit.ast_junit;