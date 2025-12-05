package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression> {
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i = 0; i < ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null)
                stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, null);
    }

    @Override
    public Expression visitInt(FeatherweightJavaScriptParser.IntContext ctx) {
        int val = Integer.valueOf(ctx.INT().getText());
        return new ValueExpr(new IntVal(val));
    }

    @Override
    public Expression visitParens(FeatherweightJavaScriptParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty())
            return null;
        Expression exp = stmts.get(0);
        for (int i = 1; i < stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }

    // THE CODE BELOW CAN BE MODIFIED

    @Override
    public Expression visitWhile(FeatherweightJavaScriptParser.WhileContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression body = visit(ctx.stat());
        return new WhileExpr(cond, body);
    }

    @Override
    public Expression visitPrint(FeatherweightJavaScriptParser.PrintContext ctx) {
        Expression expr = visit(ctx.expr());
        return new PrintExpr(expr);
    }

    @Override
    public Expression visitBlockExpr(FeatherweightJavaScriptParser.BlockExprContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitAssign(FeatherweightJavaScriptParser.AssignContext ctx) {
        String varName = ctx.ID().getText();
        Expression expr = visit(ctx.expr());
        return new AssignExpr(varName, expr);
    }

    @Override
    public Expression visitVarDecl(FeatherweightJavaScriptParser.VarDeclContext ctx) {
        String varName = ctx.ID().getText();
        Expression expr = null;
        if (ctx.expr() != null) {
            expr = visit(ctx.expr());
        }
        return new VarDeclExpr(varName, expr);
    }

    @Override
    public Expression visitCompare(FeatherweightJavaScriptParser.CompareContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        return new BinOpExpr(mapOperator(op), left, right);
    }

    @Override
    public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        return new BinOpExpr(mapOperator(op), left, right);
    }

    @Override
    public Expression visitMulDivMod(FeatherweightJavaScriptParser.MulDivModContext ctx) {
        Expression left = visit(ctx.expr(0));
        Expression right = visit(ctx.expr(1));
        String op = ctx.op.getText();
        return new BinOpExpr(mapOperator(op), left, right);
    }

    @Override
    public Expression visitCall(FeatherweightJavaScriptParser.CallContext ctx) {
        Expression function = visit(ctx.expr());
        List<Expression> args = new ArrayList<>();
        if (ctx.argList() != null) {
            for (int i = 0; i < ctx.argList().expr().size(); i++) {
                args.add(visit(ctx.argList().expr(i)));
            }
        }
        return new FunctionAppExpr(function, args);
    }

    @Override
    public Expression visitFunc(FeatherweightJavaScriptParser.FuncContext ctx) {
        List<String> params = new ArrayList<>();
        if (ctx.paramList() != null) {
            for (int i = 0; i < ctx.paramList().ID().size(); i++) {
                params.add(ctx.paramList().ID(i).getText());
            }
        }
        Expression body = visit(ctx.block());
        return new FunctionDeclExpr(params, body);
    }

    @Override
    public Expression visitBool(FeatherweightJavaScriptParser.BoolContext ctx) {
        boolean val = Boolean.valueOf(ctx.BOOL().getText());
        return new ValueExpr(new BoolVal(val));
    }

    @Override
    public Expression visitNull(FeatherweightJavaScriptParser.NullContext ctx) {
        return new ValueExpr(new NullVal());
    }

    @Override
    public Expression visitId(FeatherweightJavaScriptParser.IdContext ctx) {
        String varName = ctx.ID().getText();
        return new VarExpr(varName);
    }

    @Override
    public Expression visitBlockVal(FeatherweightJavaScriptParser.BlockValContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        // Iterate over children, skipping the first and last (the braces)
        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            Expression exp = visit(ctx.getChild(i));
            if (exp != null) {
                stmts.add(exp);
            }
        }
        return listToSeqExp(stmts);
    }

    private Op mapOperator(String opStr) {
        switch (opStr) {
            case "+":
                return Op.ADD;
            case "-":
                return Op.SUBTRACT;
            case "*":
                return Op.MULTIPLY;
            case "/":
                return Op.DIVIDE;
            case "%":
                return Op.MOD;
            case ">":
                return Op.GT;
            case ">=":
                return Op.GE;
            case "<":
                return Op.LT;
            case "<=":
                return Op.LE;
            case "==":
                return Op.EQ;
            default:
                throw new RuntimeException("Unknown operator: " + opStr);
        }
    }
}
