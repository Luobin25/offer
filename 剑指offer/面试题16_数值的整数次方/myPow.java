public class myPow {
    // 题目： 实现函数double Power(double base, int exponent)，求base的exponent次方。不得使用库函数，同时不需要考虑大数问题。
    public static void main(String[] args) {
        //边界性检测：
        // base 为 0
        // 输出值 0.0
        System.out.println(mypow(0,4));
        // exponent为 0
        // 输出值： 1
        System.out.println(mypow(4,0));
        // exponent 为 负数最小值
        System.out.println(mypow(1,Integer.MIN_VALUE));
        
        // 正常性检测
        // base, exponent 为 正数
        System.out.println(mypow(4,4));
        System.out.println(mypow(4,5));
        // base, exponent 为 负数
        System.out.println(mypow(-3,-4));
        // base 为正数 exponent 为 负数
        System.out.println(mypow(4,-4));
        // base 为负数, exponent 为 正数
        System.out.println(mypow(-4,4));
        // 异常性
    }

    public static double mypow(double base, int exponent){
        if(base == 0) return 0.0;
        if(exponent == 0) return 1;

        // 对于exponent为负数时， 我们的做法时 1/base^-exponent， 但是exponent为负数最小值时，取正数会溢出
        if(exponent < 0)   return mypow(1/base, -(exponent + 1)) / base;

        if(exponent % 2 == 0)  return mypow(base * base, exponent>> 1);
        else return mypow(base * base, exponent >> 1) * base;
    }
}
