public class deletLinkNode {
    public static void main(String[] args) {
        ListNode head = new ListNode(4);
        head.next = new ListNode(5);
        head.next.next = new ListNode(1);
        head.next.next.next = null;

        // 测试1：删除中间的元素
        // 输入： 4->5->1
        // 输出： 4->1
        head = deleteNode(head, head.next);
        printLink(head);

        // 测试2：尾指针
        // 输入： 4->1
        // 输出： 4
        head = deleteNode(head, head.next);
        printLink(head);

        // 测试2：仅一个指针
        // 输入： 4
        // 输出： null
        head = deleteNode(head, head);
        printLink(head);
    }

    // 给定单向链表的头指针和一个节点指针， 定义一个函数在O（1）时间内删除该节点
    public static ListNode deleteNode(ListNode head, ListNode delNode){
        if(delNode.next != null){
            ListNode next = delNode.next;
            delNode.val = next.val;
            delNode.next = next.next;
        }else if(head == delNode)
            return null;
        else{
            ListNode tmp = head;
            while(tmp.next != delNode){
                tmp = tmp.next;
            }

            tmp.next = null;
        }

        return head;
    }

    public static void printLink(ListNode head){
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


