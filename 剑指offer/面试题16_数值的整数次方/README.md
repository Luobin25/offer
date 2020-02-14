# 题目: 数值的整数次方

实现函数double Power(double base, int exponent)，求base的exponent次方。不得使用库函数，同时不需要考虑大数问题。

## 思路: 二分查找
假设 我们想求 2的8次方, 那么
```
2 x 2 x 2 x 2 x 2 x 2 x 2 x 2

=> 4 x 4 x 4 x 4

=> 8 x 8

递归公式:

      { x^n/2 * x^n/2     (n为偶数)
x^n = {
      { x^n/2 * x^n/2 * x (n为奇数)
```

通过递归进行二分, 思想类似于 `num = mypwer(x, n/2); num *= num;`

时间复杂度: O(logbase), 空间复杂度: O(1)

但是这道题重点考察的是: 对边界值的判断, 详细见代码

