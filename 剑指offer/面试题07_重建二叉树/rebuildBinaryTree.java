import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

// 输入某二叉树的前序遍历和中序遍历的结果，请重建该二叉树。假设输入的前序遍历和中序遍历的结果中都不含重复的数字。
// 延伸题目： 给定某二叉树的中序遍历和后序遍历的结果，请重建该二叉树。
public class rebuildBinaryTree {
    // 考点：1. 对二叉树进行递归和分治 2. 理解二叉树三种遍历之间的联系
    public static void main(String[] args) {
        Solution solution = new Solution();
        // 输入某二叉树的前序遍历和中序遍历
        // 测试1：任意二叉树
        // 输入： preorder = [3,9,20,15,7], inorder = [9,3,15,20,7]
        // 输出： [3,9,20,null,null,15,7]
        TreeNode ans = solution.buildTree(new int[]{3,9,20,15,7}, new int[]{9,3,15,20,7});
        System.out.println(treeNodeToString(ans));

        // 测试2：全为左节点
        // 输入： preorder = [], inorder = []
        // 输出： [2, 4, null, 7, null, 9, null, null, null]
        ans = solution.buildTree(new int[]{2,4,7,9}, new int[]{9,7,4,2});
        System.out.println(treeNodeToString(ans));

        // 测试3：
        // 输入： preorder = [], inorder = []
        // 输出： []
        ans = solution.buildTree(new int[]{}, new int[]{});
        System.out.println(treeNodeToString(ans));

    }

    public static String treeNodeToString(TreeNode root){
        if(root == null)
            return "[]";

        String output = "";
        Queue<TreeNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(root);

        while(!nodeQueue.isEmpty()){
            TreeNode node = nodeQueue.remove();

            if(node == null){
                output += "null, ";
                continue;
            }

            output += String.valueOf(node.val) + ", ";
            nodeQueue.add(node.left);
            nodeQueue.add(node.right);
        }

        return "[" + output.substring(0, output.length() - 2) + "]";
    }
}


class TreeNode{
    TreeNode left;
    TreeNode right;
    int val;

    TreeNode(int x) { val = x; }
}

class Solution {
    // 假设 二叉树如下：    3
    //                  /  \
    //                 9   20
    //                    / \
    //                   15  7
    // 分析： 前序遍历： 先根 再左右节点(3,9,20,15,7)。         那么从根节点对数组进行分割为 3 【左子树】 【右子树】
    //       中序遍历： 先左子树 再根 再到右子树（9,3,15,20,7） 那么从根节点对数据进行分割为 【左子树】 3 【右子树】
    // 两者分割不同， 但左右子树的长度是一致的， 所以我们从中序遍历得知了 左子树为【9】，右子树为【15，20，7】。 进行递归
    // 总结： 通过前序遍历 确定根节点， 再通过根节点在中序遍历中位置，确认左右子树的长度
    private Map<Integer, Integer> reverses;
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        int len = inorder.length;
        reverses = new HashMap<>(len);

        // 以空间换时间，否则，找根结点在中序遍历中的位置需要遍历
        for(int i = 0; i < len; i++){
            reverses.put(inorder[i], i);
        }

        return build(preorder, 0, len - 1, 0, len - 1);
    }

    public TreeNode build(int[] preorder, int preL, int preR, int inL, int inR){
        if(inL > inR)
            return null;

        TreeNode root = new TreeNode(preorder[preL]);

        int pivot = reverses.get(preorder[preL]);
        root.left = build(preorder, preL + 1, preL + pivot - inL, inL, pivot - 1);
        root.right = build(preorder, preL + 1 + pivot - inL, preR, pivot + 1, inR);

        return root;
    }

    // 分析： 后序遍历： 先左右子树 再根节点(3,9,20,15,7)。         那么从根节点对数组进行分割为 【左子树】 【右子树】3
    //       中序遍历： 先左子树 再根 再到右子树（9,3,15,20,7）     那么从根节点对数据进行分割为 【左子树】 3 【右子树】
    // 两者分割不同， 但左右子树的长度是一致的， 所以我们从中序遍历得知了 左子树为【9】，右子树为【15，20，7】。 进行递归
    // 总结： 通过后序遍历 确定根节点， 再通过根节点在中序遍历中位置，确认左右子树的长度
    // 答案基本如上， 稍微改造即可
}