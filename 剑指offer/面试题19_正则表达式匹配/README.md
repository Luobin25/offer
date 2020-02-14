# 正则表达式匹配
给你一个字符串 s 和一个字符规律 p，请你来实现一个支持 '.' 和 '*' 的正则表达式匹配。

```
'.' 匹配任意单个字符
'*' 匹配零个或多个前面的那一个元素
```

注意: 
1. 题目考的是**匹配**, 是要涵盖整个text文本,而不是部分字符串
2. `*` 匹配前面的零个或多个前面的那一个元素

# 思路1: 回溯法
因为有`*`的存在,我们可以利用回溯递归的思想,枚举匹配零个或多个的思想

但是一上来就写出来,是很困难的. 我们一步步来分析(使用伪代码)

## 思路1.1
不算特殊字符, 按照正常的思路写一个匹配函数

```
boolean isMatch(String Text, String Pattern){
  if(text.length != pattern.length )  return false;
  
  for(i in 0 ~ length){
    if(text[i] != pattern[i]
      return false;
  }
  
  return true;
}
```

改造成基于递归的模式: 两点转变
1. 每次递归只比较第一个字符, 然后传递剩下的子字符串
2. 当pattern已经空了, text 若空 为 true 否则 为 false

```
boolean isMatch(String Text, String Pattern){
  if( pattern is Empty) return text is Empty or not
  
  //text not empty 是为了防止 pattern的长度 大于 text
  boolean first_match = (text not empty) && text[0] == pattern[0]
  
  return first_match && isMatch(text[1:], pattern[1:]);
}
```

## 思路1.2
加上 `.` 匹配任意单个字符


```
boolean isMatch(String Text, String Pattern){
  if( pattern is Empty) return text is Empty or not
  boolean first_match = (text not empty) && (text[0] in { pattern[0] or '.'}
  
  return first_match && isMatch(text[1:], pattern[1:]);
}
```

## 思路1.3
加上 `*` 匹配零个或多个. 因为是递归, 我们每次只比较一个字符, 所以当我们遇上 `*` 时, 只需要考虑两种情况: 1个 或 0

对于0个的例子: text: aab, pattern: aac\*b => 当我们把 c\*看成0时, 就相当于 aab, aa(c\*)b.  
一般化式子: 如果把\*看成0, 就相当于 下一次的递归 text字串不移动, pattern向后移动两位(当前字符和\*字符)

对于1个的例子: 先检查 `*` 前面的字符串是否匹配, 若不匹配, 直接返回false  
若匹配: 来看例子 ab, a* => a匹配, 所以text字符串移动一位, 而pattern不变(反复递归,直到text第一个字符不等于a时)

```
boolean isMatch(String Text, String Pattern){
  if( pattern is Empty) return text is Empty or not
  boolean first_match = (text not empty) && (text[0] in { pattern[0] or '.'}
  
  if(len(pattern) >= 2 && pattern[1] == '*')
    // 情况为0时
    return isMatch(text, pattern[2:]) or 
          (first_match && isMatch(text[1:], pattern)
    
  return first_match && isMatch(text[1:], pattern[1:]);
}
```

但是这道题中 使用递归的做法是不够快速的, 因为这里存在**大量的重复计算没有被利用** 以及 **产生过多的字串**

# 思路2: 动态规划
动态规划的出现是为了避免**重复计算**, 通过将结果保存在map或者数组中, 形成直接引用

对于思想1,求时间复杂度是很困难的, 因为存在某些特殊case,导致大量重复计算.  
而对于思想2, 因为我们创建了一个数组, 将每次结果都保存起来, 所以不可能会出现重复计算的情况  

那么最多的计算次数:  T的长度为T, P的长度为P. 时间复杂度为O(TP), 空间复杂度为O(TP)
