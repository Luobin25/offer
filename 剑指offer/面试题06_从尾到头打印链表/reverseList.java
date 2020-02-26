import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
// 题目：输入一个链表的头节点，从尾到头反过来返回每个节点的值（用数组返回）。
public class reverseList {

    // 对于链表的题目，如果没有明确的说明，是不建议修改链表的
    // 从尾到头反过来，是不是可以理解成 先进的后输出， 后进的先输出 =》栈的思想就出来了
    // 再或者， 我们用递归的思想，递归到最后，在回溯依次输出值
    public static void main(String[] args) {
        Solution solution = new Solution();
        // 测试1：
        // 输入： [1，3，2]
        // 输出： [2，3，1]
        ListNode head = new ListNode(1);
        head.next = new ListNode(3);
        head.next.next = new ListNode(2);
        head.next.next.next = null;
        System.out.println(Arrays.toString(solution.reversePrint(head)));
        System.out.println(Arrays.toString(solution.reversePrint2(head)));

        // 测试2：空指针
        System.out.println(Arrays.toString(solution.reversePrint(null)));
        System.out.println(Arrays.toString(solution.reversePrint2(null)));
    }


}
class Solution{

    public int[] reversePrint(ListNode head) {
        // 遍历一遍获取长度
        int len = 0;
        ListNode tmp = head;
        while (tmp != null){
            tmp = tmp.next;
            len++;
        }

        int[] ans = new int[len];
        while(head != null){
            ans[len-1] = head.val;
            head = head.next;
            len--;
        }

        return ans;
    }

    // 用递归的思想，这里因为int【】初始化时是要固定大小的， 我们只能在退出条件时创建
    // 因为只是简单的反转，没有用上arraylist
    public int[] reversePrint2(ListNode head) {
        recurse(head, 0);
        return ans;
    }

    public int[] ans;
    public int len;
    public void recurse(ListNode head, int size){
        if(head == null){
            ans = new int[size];
            len = size - 1;
            return;
        }

        recurse(head.next, size+1);
        ans[len - size] = head.val;
    }
}
class ListNode{
    int val;
    ListNode next;
    ListNode(int x) {val = x;};
}
