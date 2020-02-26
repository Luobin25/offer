// 题目： 请实现一个函数，把字符串 s 中的每个空格替换成"%20"。
public class replaceBlank {

    // 在剑指offer中， 它想考察的思想是 假设我们只能在原字符上修改
    // 如果从头往后遍历， 会导致遇到空格时，就要移动后面的数，导致O（n^2)
    // 为什么会这样呢？ 是因为头是固定的，而如果从后往前移动
    // 我们先统计一下字符的长度 以及空格的次数， 那么我们就可以在字符串的尾巴补上替换空格所需要的长度，
    // 这样当我们从后往前移动的时候，就不需要考虑移动的问题

    // 再比如 对于两个排好序的数组合并， 假设A1足够大， 大到能容纳A2。 那么我们分别比较A1和A2最大数，然后在放入A1尾巴
    // 是不是要比从头开始遍历快(查看 leetcode 面试题 10.01. 合并排序的数组)
    public static void main(String[] args) {
        // 测试1： 包含空格
        // 输入： "We are happy."
        // 输出： "We%20are%20happy."
        System.out.println(replaceSpace(new String("We are happy.")));
        System.out.println(replaceSpace2(new String("We are happy.")));
        // 测试1： 不包含空格
        // 输入： "WeAreHappy"
        // 输出： "WeAreHappy"
        System.out.println(replaceSpace(new String("WeAreHappy")));
        System.out.println(replaceSpace2(new String("WeAreHappy")));
        // 测试1： 空指针
        // 输入： null
        // 输出： null
        System.out.println(replaceSpace(null));
        System.out.println(replaceSpace2(null));
    }

    public static String replaceSpace(String s) {
        if(s == null)
            return null;

        char[] ans = new char[s.length() * 3];

        int size = 0;
        for(char c : s.toCharArray()){
            if(c == ' '){
                ans[size++] = '%';
                ans[size++] = '2';
                ans[size++] = '0';
            }else
                ans[size++] = c;
        }

        return new String(ans, 0, size);
    }

    public static String replaceSpace2(String s) {
        if(s == null)
            return null;

        StringBuilder ans = new StringBuilder();

        String replace = "%20";
        for(int i = 0; i < s.length(); i++){
            if(s.charAt(i) == ' ')
                ans.append(replace);
            else
                ans.append(s.charAt(i));
        }

        return ans.toString();
    }


}
