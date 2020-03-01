public class deleteDuplicatesNode {
    public static void main(String[] args) {
        Solution4 s = new Solution4();

        // 测试1： 空指针
        // 输入： null
        // 输出： null
        s.deleteDuplicates(null);
        s.printLink(null);

        ListNode head1 = new ListNode(1);
        head1.next = new ListNode(1);
        head1.next.next = new ListNode(1);
        head1.next.next.next = new ListNode(2);
        head1.next.next.next.next = null;
        // 测试1：删除中间的元素
        // 输入： 1->1->1->2
        // 输出： 2
        head1 = s.deleteDuplicates(head1);
        s.printLink(head1);

        // 测试2： 全部都重复
        // 输入： 1->1->1
        // 输出： null
        ListNode head2 = new ListNode(1);
        head2.next = new ListNode(1);
        head2.next.next = new ListNode(1);
        head2.next.next.next = null;
        head2 = s.deleteDuplicates(head2);
        s.printLink(head2);
    }
}

class Solution4{
    boolean deleteFirst;
    public ListNode deleteDuplicates(ListNode head) {
        deleteFirst = false;
        head = deleteRecursion(head);
        return deleteFirst ? head.next : head;
    }

    public ListNode deleteRecursion(ListNode head){
        if(head == null || head.next == null)
            return head;

        head.next = deleteRecursion(head.next);
        if(head.val == head.next.val){
            deleteFirst = true;
            head = head.next;
        }else if(deleteFirst){
            head.next = head.next.next;
            deleteFirst = false;
        }

        return head;
    }

    public void printLink(ListNode head){
        while(head != null){
            System.out.print(head.val + ", ");
            head = head.next;
        }

        System.out.println("null");
    }
}
//class ListNode{
//    int val;
//    ListNode next;
//    ListNode(int x) {val = x;};
//}

