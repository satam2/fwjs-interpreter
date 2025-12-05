package edu.sjsu.fwjs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ExpressionTest {

    @Test
    public void testValueExpr() {
        Environment env = new Environment();
        ValueExpr ve = new ValueExpr(new IntVal(3));
        IntVal i = (IntVal) ve.evaluate(env);
        assertEquals(i.toInt(), 3);
    }
    
    @Test
    public void testVarExpr() {
        Environment env = new Environment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("x");
        assertEquals(e.evaluate(env), v);
    }
    
    @Test
    public void testVarNotFoundExpr() {
        Environment env = new Environment();
        Value v = new IntVal(3);
        env.updateVar("x", v);
        Expression e = new VarExpr("y");
        assertEquals(e.evaluate(env), new NullVal());
    }
    
    @Test
    public void testIfTrueExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new BoolVal(true)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(iv.toInt(), 1);
    }
    
    @Test
    public void testIfFalseExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new BoolVal(false)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) ife.evaluate(env);
        assertEquals(iv.toInt(), 2);
    }
    
    @Test
    public void testBadIfExpr() {
        Environment env = new Environment();
        IfExpr ife = new IfExpr(new ValueExpr(new IntVal(0)),
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        try {
            ife.evaluate(env);
            fail();
        } catch (Exception e) {}
    }
    
    @Test
    public void testAssignExpr() {
        Environment env = new Environment();
        IntVal inVal = new IntVal(42);
        AssignExpr ae = new AssignExpr("x", new ValueExpr(inVal));
        IntVal iv = (IntVal) ae.evaluate(env);
        assertEquals(iv, inVal);
        assertEquals(env.resolveVar("x"), inVal);
    }
    
    @Test
    public void testBinOpExpr() {
        Environment env = new Environment();
        BinOpExpr boe = new BinOpExpr(Op.ADD,
                new ValueExpr(new IntVal(1)),
                new ValueExpr(new IntVal(2)));
        IntVal iv = (IntVal) boe.evaluate(env);
        assertEquals(iv, new IntVal(3));
    }
    
    @Test
    public void testSeqExpr() {
        Environment env = new Environment();
        SeqExpr se = new SeqExpr(new AssignExpr("x", new ValueExpr(new IntVal(2))),
                new BinOpExpr(Op.MULTIPLY,
                        new VarExpr("x"),
                        new ValueExpr(new IntVal(3))));
        assertEquals(se.evaluate(env), new IntVal(6));
    }
    
    @Test
    public void testWhileExpr() {
        Environment env = new Environment();
        env.updateVar("x", new IntVal(10));
        WhileExpr we = new WhileExpr(new BinOpExpr(Op.GT,
                    new VarExpr("x"),
                    new ValueExpr(new IntVal(0))),
                new AssignExpr("x",
                        new BinOpExpr(Op.SUBTRACT,
                                new VarExpr("x"),
                                new ValueExpr(new IntVal(1)))));
        we.evaluate(env);
        assertEquals(new IntVal(0), env.resolveVar("x"));
    }
    
    @Test
    // (function(x) { x; })(321);
    public void testIdFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<String>();
        params.add("x");
        FunctionDeclExpr f = new FunctionDeclExpr(params, new VarExpr("x"));
        List<Expression> args = new ArrayList<Expression>();
        args.add(new ValueExpr(new IntVal(321)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(321), app.evaluate(env));
    }
    
    @Test
    // (function(x,y) { x / y; })(8,2);
    public void testDivFunction() {
        Environment env = new Environment();
        List<String> params = new ArrayList<String>();
        params.add("x");
        params.add("y");
        FunctionDeclExpr f = new FunctionDeclExpr(params,
                new BinOpExpr(Op.DIVIDE,
                        new VarExpr("x"),
                        new VarExpr("y")));
        List<Expression> args = new ArrayList<Expression>();
        args.add(new ValueExpr(new IntVal(8)));
        args.add(new ValueExpr(new IntVal(2)));
        FunctionAppExpr app = new FunctionAppExpr(f,args);
        assertEquals(new IntVal(4), app.evaluate(env));
    }
    
    @Test
    // x=112358; (function() { x; })();
    public void testOuterScope() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new VarExpr("x"));
        SeqExpr seq = new SeqExpr(newVar, new FunctionAppExpr(f, new ArrayList<Expression>()));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(112358), v);
    }
    
    @Test
    // x=112358; (function() { var x=42; x; })();
    public void testScope() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new VarDeclExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(newVar, new FunctionAppExpr(f, new ArrayList<Expression>()));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(42), v);
    }
    
    @Test
    // x=112358; (function() { var x=42; x; })(); x;
    public void testScope2() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new VarDeclExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(new SeqExpr(newVar,
                new FunctionAppExpr(f, new ArrayList<Expression>())),
                new VarExpr("x"));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(112358), v);
    }
    
    @Test
    // x=112358; (function() { x=42; x; })(); x;
    public void testScope3() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(112358)));
        FunctionDeclExpr f = new FunctionDeclExpr(new ArrayList<String>(),
                new SeqExpr(new AssignExpr("x", new ValueExpr(new IntVal(42))),
                        new VarExpr("x")));
        SeqExpr seq = new SeqExpr(new SeqExpr(newVar,
                new FunctionAppExpr(f, new ArrayList<Expression>())),
                new VarExpr("x"));
        Value v = seq.evaluate(env);
        assertEquals(new IntVal(42), v);
    }
    
    @Test
    // var x=99; var x=99;  /* should throw an error */
    public void testVarDecl() {
        Environment env = new Environment();
        VarDeclExpr newVar = new VarDeclExpr("x", new ValueExpr(new IntVal(99)));
        try {
            (new SeqExpr(newVar, newVar)).evaluate(env);
            fail();
        } catch (Exception e) {}
    }

    // ------------------------------------ SELF TESTS ----------------------------------------
    @Test
    public void testResolveUndefinedReturnsNull() {
        Environment env = new Environment();
        assertEquals(new NullVal(), new VarExpr("nope").evaluate(env));
    }

    @Test
    public void testUpdateCreatesGlobalWhenMissing() {
        Environment g = new Environment();
        Environment l = new Environment(g);
        new AssignExpr("x", new ValueExpr(new IntVal(42))).evaluate(l); // not declared anywhere
        assertEquals(new IntVal(42), new VarExpr("x").evaluate(g));      // must be created in global
    }

    @Test
    public void testShadowingReadAndWrite() {
        Environment g = new Environment();
        g.createVar("x", new IntVal(5));
        Environment l = new Environment(g);
        l.createVar("x", new IntVal(1));                // shadow
        new AssignExpr("x", new ValueExpr(new IntVal(2))).evaluate(l);  // update nearest (local)
        assertEquals(new IntVal(2), new VarExpr("x").evaluate(l));
        assertEquals(new IntVal(5), new VarExpr("x").evaluate(g));      // global untouched
    }

    @Test
    public void testUpdateOuterWhenNotLocal() {
        Environment g = new Environment();
        g.createVar("y", new IntVal(10));
        Environment l = new Environment(g);
        new AssignExpr("y", new ValueExpr(new IntVal(11))).evaluate(l); // no local y -> update outer
        assertEquals(new IntVal(11), new VarExpr("y").evaluate(g));
    }

    @Test
    public void testArithmeticAndComparisons() {
        Environment env = new Environment();
        assertEquals(new IntVal(7),  new BinOpExpr(Op.ADD,       new ValueExpr(new IntVal(3)), new ValueExpr(new IntVal(4))).evaluate(env));
        assertEquals(new IntVal(6),  new BinOpExpr(Op.MULTIPLY,  new ValueExpr(new IntVal(2)), new ValueExpr(new IntVal(3))).evaluate(env));
        assertEquals(new IntVal(1),  new BinOpExpr(Op.MOD,       new ValueExpr(new IntVal(7)), new ValueExpr(new IntVal(3))).evaluate(env));
        assertEquals(new BoolVal(true),  new BinOpExpr(Op.GT,    new ValueExpr(new IntVal(5)), new ValueExpr(new IntVal(2))).evaluate(env));
        assertEquals(new BoolVal(false), new BinOpExpr(Op.LE,    new ValueExpr(new IntVal(5)), new ValueExpr(new IntVal(2))).evaluate(env));
        assertEquals(new BoolVal(true),  new BinOpExpr(Op.EQ,    new ValueExpr(new IntVal(9)), new ValueExpr(new IntVal(9))).evaluate(env));
    }

    @Test
    public void testSimpleFunctionCall() {
        Environment env = new Environment();
        // function(a,b){ a + b } (a closure)
        FunctionDeclExpr addDecl = new FunctionDeclExpr(
            java.util.Arrays.asList("a","b"),
            new BinOpExpr(Op.ADD, new VarExpr("a"), new VarExpr("b"))
        );
        Value add = addDecl.evaluate(env);

        // add(2,3)
        FunctionAppExpr call = new FunctionAppExpr(
            new ValueExpr(add),
            java.util.Arrays.asList(new ValueExpr(new IntVal(2)), new ValueExpr(new IntVal(3)))
        );
        assertEquals(new IntVal(5), call.evaluate(env));
    }

    @Test
    public void testClosureCapturesDefiningEnv() {
        Environment env = new Environment();
        new VarDeclExpr("x", new ValueExpr(new IntVal(10))).evaluate(env);

        // f = function(y){ x + y }
        Value f = new FunctionDeclExpr(
            java.util.Arrays.asList("y"),
            new BinOpExpr(Op.ADD, new VarExpr("x"), new VarExpr("y"))
        ).evaluate(env);

        // change x in *current* env; closure should still see the defining env’s x if your spec captures by env reference at eval time.
        new AssignExpr("x", new ValueExpr(new IntVal(3))).evaluate(env);

        // f(5) -> depending on capture semantics:
        // If your closure captures by reference (typical), result is 3 + 5 = 8.
        // If by value/snapshot (less common), result would be 10 + 5 = 15.
        // Most FWJS homeworks capture by reference; adjust expected accordingly:
        FunctionAppExpr call = new FunctionAppExpr(
            new ValueExpr(f),
            java.util.Arrays.asList(new ValueExpr(new IntVal(5)))
        );
        assertEquals(new IntVal(8), call.evaluate(env));
    }

    @Test
    public void testArityShortAndLong() {
        Environment env = new Environment();
        // g = function(a,b){ (a == null ? 0 : a) + (b == null ? 0 : b) }
        // since your apply() binds missing params to NullVal, check behavior
        Expression sum =
            new BinOpExpr(Op.ADD,
                new BinOpExpr(Op.ADD,
                    new BinOpExpr(Op.EQ, new VarExpr("a"), new ValueExpr(new NullVal())),
                    new BinOpExpr(Op.EQ, new VarExpr("b"), new ValueExpr(new NullVal()))
                ),
                new ValueExpr(new IntVal(0)) // dummy to keep it simple if EQ returns BoolVal; you can adapt as your Op set allows
            );

        // Easier: just return 'a' to test missing/extra args:
        Value g = new FunctionDeclExpr(
            java.util.Arrays.asList("a","b"),
            new VarExpr("a")
        ).evaluate(env);

        // g(7) -> a=7, b=null
        assertEquals(new IntVal(7), new FunctionAppExpr(
            new ValueExpr(g),
            java.util.Arrays.asList(new ValueExpr(new IntVal(7)))
        ).evaluate(env));

        // g(7,8,9) -> extra arg ignored by your apply(); still returns a (=7)
        assertEquals(new IntVal(7), new FunctionAppExpr(
            new ValueExpr(g),
            java.util.Arrays.asList(new ValueExpr(new IntVal(7)),
                                    new ValueExpr(new IntVal(8)),
                                    new ValueExpr(new IntVal(9)))
        ).evaluate(env));
    }

    @Test
    public void testCallNonFunctionReturnsNull() {
        Environment env = new Environment();
        FunctionAppExpr call = new FunctionAppExpr(
            new ValueExpr(new IntVal(123)), // not a closure
            java.util.Collections.emptyList()
        );
        assertEquals(new NullVal(), call.evaluate(env));
    }
}

