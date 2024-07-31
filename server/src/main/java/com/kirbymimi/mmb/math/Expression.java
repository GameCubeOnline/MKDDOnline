package com.kirbymimi.mmb.math;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ArrayUT;
import com.kirbymimi.mmb.ut.FieldUT;
import com.kirbymimi.mmb.ut.list.FastList;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.function.BiFunction;

public class Expression {
   String resultName;
   Expression.ExpressionWrap[] operations;
   Deque<Double> stack = new LinkedList();
   FastList<String> classLookUp;

   public String getResultName() {
      return this.resultName;
   }

   public Expression() {
   }

   public void addClassLookUp(String cls) {
      if (this.classLookUp == null) {
         this.classLookUp = new FastList();
      }

      this.classLookUp.add(cls);
   }

   public Expression(String expStr) {
      this.create(expStr);
   }

   public void create(String expStr) {
      int eq = -1;

      int i;
      for(i = 0; i != expStr.length(); ++i) {
         if (expStr.charAt(i) == '=') {
            eq = i;
            break;
         }
      }

      if (eq != -1) {
         this.resultName = expStr.substring(0, eq).replace(" ", "");
      }

      expStr = expStr.substring(eq + 1);
      expStr = expStr.replace("()", "!!");
      expStr = expStr.replace("(\"", "!\"");
      expStr = expStr.replace("\")", "\"!");
      i = 0;
      char[] chars = expStr.toCharArray();
      char[] var8 = chars;
      int p = chars.length;

      for(int var6 = 0; var6 < p; ++var6) {
         char c = var8[var6];
         if (c == '(') {
            ++i;
         } else if (c == ')') {
            --i;
         }
      }

      if (i != 0) {
         MMBSystem.fatalS((Exception)(new Expression.BadExpressionOperation("unequal amount of ( and )")));
      }

      Expression.ArithmeticFactory fac = new Expression.ArithmeticFactory(this);
      Vector<String> expDump = fac.expDump;
      boolean ArithSwitch = true;

      for(p = 0; p != chars.length; ++p) {
         char currChar = chars[p];
         boolean addMul;
         int p2;
         switch(currChar) {
         case ' ':
            continue;
         case '(':
         case ')':
         case '*':
         case '+':
         case '-':
         case '/':
         case '^':
            if (ArithSwitch && expDump.size() != 0 && ((String)expDump.elementAt(expDump.size() - 1)).compareTo(")") != 0 && currChar != '(') {
               MMBSystem.fatalS((Object)("Bad expression : " + expStr));
            }

            ArithSwitch = true;
            expDump.add(new String(new char[]{currChar}));
            continue;
         default:
            if (!ArithSwitch) {
               MMBSystem.fatalS((Object)("Bad expression : " + expStr));
            }

            ArithSwitch = false;
            if (p != 0 && chars[p - 1] == ')') {
               expDump.add("*");
            }

            p2 = p + 1;
            addMul = false;
         }

         label87:
         while(p2 < chars.length) {
            switch(chars[p2]) {
            case ' ':
            case ')':
            case '*':
            case '+':
            case '-':
            case '/':
            case '^':
               break label87;
            case '(':
               addMul = true;
               break label87;
            default:
               ++p2;
            }
         }

         char[] nc = new char[p2 - p];
         int p4 = 0;

         for(int p3 = p; p3 != p2; ++p3) {
            nc[p4++] = chars[p3];
         }

         String s = new String(nc);
         expDump.add(s);
         if (addMul) {
            expDump.add("*");
         }

         p = p2 - 1;
      }

      fac.shuntingYard();
      fac.finish(this);
   }

   public int resolvei() {
      return (int)this.resolve((Object)null, (BiFunction)null);
   }

   public long resolvel() {
      return (long)this.resolve((Object)null, (BiFunction)null);
   }

   public double resolve() {
      return this.resolve((Object)null, (BiFunction)null);
   }

   public <T> int resolvei(T arg, BiFunction<T, String[], Double> valueGet) {
      return (int)this.resolve(arg, valueGet);
   }

   public <T> long resolvel(T arg, BiFunction<T, String[], Double> valueGet) {
      return (long)this.resolve(arg, valueGet);
   }

   public <T> double resolve(T arg, BiFunction<T, String[], Double> valueGet) {
      Expression.ExpressionWrap[] var12;
      int var11 = (var12 = this.operations).length;

      for(int var10 = 0; var10 < var11; ++var10) {
         Expression.ExpressionWrap expWrap = var12[var10];
         if (expWrap.id == 0) {
            double v2 = (Double)this.stack.pop();
            double v1 = (Double)this.stack.pop();
            Expression.ExpressionOperation expOp = (Expression.ExpressionOperation)expWrap;
            switch(expOp.opid) {
            case 0:
               v1 += v2;
               break;
            case 1:
               v1 -= v2;
               break;
            case 2:
               v1 *= v2;
               break;
            case 3:
               v1 /= v2;
               break;
            case 4:
               v1 = Math.pow(v1, v2);
            }

            this.stack.push(v1);
         } else {
            Expression.ExpressionValue expVal = (Expression.ExpressionValue)expWrap;
            double val = -1.0D;

            try {
               if (expVal.val.getClass() == String[].class) {
                  val = (Double)valueGet.apply(arg, (String[])expVal.val);
               } else if (expVal.val.getClass() == Method.class) {
                  val = (Double)((Method)expVal.val).invoke((Object)null, arg);
               } else {
                  val = ((Number)expVal.val).doubleValue();
               }
            } catch (Exception var19) {
               Exception e = var19;
               if (var19 instanceof Expression.ExpressionResolveException) {
                  throw (Expression.ExpressionResolveException)var19;
               }

               if (var19 instanceof RuntimeException) {
                  try {
                     throw e;
                  } catch (InvocationTargetException var17) {
                     var17.printStackTrace();
                  } catch (IllegalAccessException var18) {
                     var18.printStackTrace();
                  } catch (Exception ex) {
                      throw new RuntimeException(ex);
                  }
               }

               var19.printStackTrace();
               MMBSystem.fatalS((Object)("Can't resolve expression value : " + ArrayUT.arrToString((Object[])expVal.val)));
            }

            this.stack.push(val);
         }
      }

      return (Double)this.stack.pop();
   }

   public Number trash(Expression.ExpressionValue expVal, Object arg) throws Exception {
      if (expVal.val.getClass() != String[].class) {
         return null;
      } else {
         Object cObj = arg;
         String[] valPath = (String[])expVal.val;

         for(int i = 0; i != valPath.length; ++i) {
            if (valPath[i].endsWith("!")) {
               if (valPath[i].contains("!!")) {
                  cObj = cObj.getClass().getMethod(valPath[i].substring(0, valPath[i].length() - 2), (Class[])null).invoke(cObj, (Object[])null);
               } else {
                  String funcName = valPath[i].substring(0, valPath[i].indexOf(33));
                  int argcnt = 1;
                  char[] chars = valPath[i].toCharArray();
                  char[] var12 = chars;
                  int ccharpos = chars.length;

                  for(int var10 = 0; var10 < ccharpos; ++var10) {
                     char c = var12[var10];
                     if (c == ',') {
                        ++argcnt;
                     }
                  }

                  Object[] args = new Object[argcnt];
                  char[] builder = new char[chars.length];
                  ccharpos = -1;
                  int carg = 0;
                  char[] var16 = chars;
                  int var15 = chars.length;

                  int i2;
                  for(i2 = 0; i2 < var15; ++i2) {
                     char c = var16[i2];
                     if (c == '"') {
                        if (ccharpos != -1) {
                           args[carg++] = new String(builder, 0, ccharpos);
                           ccharpos = -1;
                        } else {
                           ccharpos = 0;
                        }
                     } else if (ccharpos != -1) {
                        builder[ccharpos++] = c;
                     }
                  }

                  Class[] argCls = new Class[argcnt];

                  for(i2 = 0; i2 != argcnt; ++i2) {
                     argCls[i2] = args[i2].getClass();
                  }

                  cObj = cObj.getClass().getMethod(funcName, argCls).invoke(cObj, args);
               }
            } else {
               cObj = fieldGet(cObj, valPath[i]);
            }

            if (cObj == null) {
               break;
            }
         }

         if (cObj == null) {
            throw new Expression.ExpressionResolveException();
         } else {
            return ((Number)cObj).doubleValue();
         }
      }
   }

   static final Object fieldGet(Object obj, String name) throws Exception {
      Field fld = FieldUT.getField(obj.getClass(), name);
      boolean acc = fld.isAccessible();
      fld.setAccessible(true);
      Object ret = fld.get(obj);
      fld.setAccessible(acc);
      return ret;
   }

   static class ArithmeticFactory {
      Expression parent;
      Vector<String> expDump = new Vector();
      Vector<String> outputs = new Vector();
      Deque<Character> opStack = new LinkedList();

      ArithmeticFactory(Expression parent) {
         this.parent = parent;
      }

      void shuntingYard() {
         for(int i = 0; i != this.expDump.size(); ++i) {
            String s;
            char c = (s = (String)this.expDump.elementAt(i)).charAt(0);
            switch(c) {
            case '(':
               this.opStack.push(c);
               break;
            case ')':
               while((Character)this.opStack.peek() != '(') {
                  this.shuntingYardStkToOut();
               }

               this.opStack.pop();
               break;
            case '*':
            case '+':
            case '-':
            case '/':
               while(!this.opStack.isEmpty() && this.symbolIsBigger(c, (Character)this.opStack.peek())) {
                  this.shuntingYardStkToOut();
               }

               this.opStack.push(c);
               break;
            case ',':
            case '.':
            default:
               this.outputs.add(s);
            }
         }

         while(!this.opStack.isEmpty()) {
            this.shuntingYardStkToOut();
         }

      }

      void shuntingYardStkToOut() {
         this.outputs.add(new String(new char[]{(Character)this.opStack.pop()}));
      }

      boolean symbolIsBigger(char curr, char prev) {
         if (prev == '(') {
            return false;
         } else {
            switch(prev) {
            case '*':
            case '/':
               return true;
            default:
               switch(curr) {
               case '+':
               case '-':
                  return true;
               case ',':
               default:
                  return false;
               }
            }
         }
      }

      public Expression finish(Expression exp) {
         exp.operations = new Expression.ExpressionWrap[this.outputs.size()];
         int cp = -1;
         Object value = null;
         Iterator var8 = Collections.list(this.outputs.elements()).iterator();

         while(true) {
            byte i;
            label100:
            while(true) {
               if (!var8.hasNext()) {
                  return exp;
               }

               String S = (String)var8.next();
               ++cp;
               switch(S.charAt(0)) {
               case '*':
                  i = 2;
                  break label100;
               case '+':
                  i = 0;
                  break label100;
               case '-':
                  i = 1;
                  break label100;
               case '/':
                  i = 3;
                  break label100;
               case '^':
                  i = 4;
                  break label100;
               default:
                  value = null;

                  try {
                     value = Integer.decode(S);
                  } catch (NumberFormatException var17) {
                  }

                  if (value == null) {
                     try {
                        value = Float.parseFloat(S);
                     } catch (NumberFormatException var16) {
                     }
                  }

                  if (value == null) {
                     try {
                        System.out.println(S);
                        int lastDot = S.lastIndexOf(46);
                        Class cls = null;

                        try {
                           cls = Class.forName(S.substring(0, lastDot));
                        } catch (Exception var15) {
                        }

                        if (S.charAt(S.length() - 1) == '!' && cls == null && this.parent.classLookUp != null) {
                           String base = null;
                           if (lastDot != -1) {
                              base = S.substring(0, lastDot);
                           }

                           Iterator var13 = this.parent.classLookUp.iterator();

                           while(var13.hasNext()) {
                              String str = (String)var13.next();

                              try {
                                 if (base != null) {
                                    if (str.charAt(str.length() - 1) != '.') {
                                       continue;
                                    }

                                    cls = Class.forName(base + S.substring(0, lastDot));
                                 } else {
                                    if (str.charAt(str.length() - 1) == '.') {
                                       continue;
                                    }

                                    cls = Class.forName(str);
                                 }

                                 try {
                                    value = cls.getMethod(S.substring(lastDot + 1, S.length() - 2), Object.class);
                                    break;
                                 } catch (Exception var18) {
                                 }
                              } catch (Exception var19) {
                              }
                           }
                        }
                     } catch (Exception var20) {
                     }
                  }

                  if (value == null) {
                     value = S.split("\\.");
                  }

                  Expression.ExpressionValue expVal = new Expression.ExpressionValue(value);
                  exp.operations[cp] = expVal;
               }
            }

            Expression.ExpressionOperation expOp = new Expression.ExpressionOperation(i);
            exp.operations[cp] = expOp;
         }
      }
   }

   public static class BadExpressionOperation extends Exception {
      private static final long serialVersionUID = 1L;
      public String err;

      BadExpressionOperation(String e) {
         this.err = e;
      }
   }

   public static class ExpressionOperation extends Expression.ExpressionWrap {
      int opid;

      ExpressionOperation(int i) {
         super(0);
         this.opid = i;
      }
   }

   public static class ExpressionResolveException extends RuntimeException {
      private static final long serialVersionUID = 1L;
   }

   public static class ExpressionValue extends Expression.ExpressionWrap {
      Object val;

      ExpressionValue(Object v) {
         super(1);
         this.val = v;
      }
   }

   public static class ExpressionWrap {
      int id;

      ExpressionWrap(int I) {
         this.id = I;
      }
   }
}
