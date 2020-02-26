// 题目：给定一个包含 n + 1 个整数的数组 nums，其数字都在 1 到 n 之间（包括 1 和 n），可知至少存在一个重复的整数。
// 假设只有一个重复的整数，找出这个重复的数。
// leetcode 287, 要求 不能更改原数组， 只能使用额外的O（1）空间
public class duplicateNumberInArray2 {
    // 考察：对一维数组的掌握 和 二分查找
    public static void main(String[] args) {
        // 测试1：
        // 输入： [1,3,4,2,2]
        // 输出： 2
        System.out.println(findDuplicate(new int[]{1,3,4,2,2}));
        // 测试2：
        // 输入： []
        // 输出： -1
        System.out.println(findDuplicate(new int[]{}));

        // 方法2，测试1：
        // 输入： [3,1,3,4,2]
        // 输出： 3
        System.out.println(findDuplicate2(new int[]{3,1,3,4,2}));
    }

    //  对于一个n+1长，数的范围在1到n之间。然后只有一个重复数， 我们是不是可以帮它当成
    // 一个循环的链表，对于循环链表 需要用快慢指针的技巧
    // O(n), O(1)
    public static int findDuplicate(int[] nums) {
        if(nums == null || nums.length == 0)
                return -1;

        // 判断是否为环， 参考leetcode 141
        int fast = 0, slow = 0;
        while(slow != fast || fast == 0){
            slow = nums[slow];
            fast = nums[nums[fast]];
        }

        // 找到循环入口位置， 参考leetcode 142
        slow = 0;
        while(slow != fast){
            slow = nums[slow];
            fast = nums[fast];
        }

        return slow;
    }

    // 二分查找 从题目已知 我们数的范围是在 1～n。 对于不重复的数，只能出现一次
    // 所以运用二分查找的思想， 如果对于一个中位数，该中位数等于或小于 比该中位小的所有值， 那么我们可以证明重复数不在这里面
    // O(nlogn), O(1)
    public static int findDuplicate2(int[] nums){
        int left = 0, right = nums.length -1;

        while(left < right){
            int mid = (left + right) >>> 1;
            int count = 0;
            for(int num : nums){
                if(num <= mid) count++;
            }
            if(count <= mid) left = mid + 1;
            else right = mid;
        }

        return left;
    }


}
