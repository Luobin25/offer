// 题目： 在一个长度为 n 的数组 nums 里的所有数字都在 0～n-1 的范围内。数组中某些数字是重复的，但不知道有几个数字重复了，
// 也不知道每个数字重复了几次。请找出数组中任意一个重复的数字。
public class duplicateNumberInArray {

    // 考察 对一维数组的掌握 和 分析问题的能力
    public static void main(String[] args) {
        // 测试1：
        // 输入： [2, 3, 1, 0, 2, 5, 3]
        // 输出： 2 或 3
        System.out.println(findRepetaNumber(new int[]{2, 3, 1, 0, 2, 5, 3}));
        // 测试2：
        // 输入： []
        // 输出： -1
        System.out.println(findRepetaNumber(new int[]{}));
        // 测试3：
        // 输入： [3,1,2,0]
        // 输出： -1
        System.out.println(findRepetaNumber(new int[]{3,1,2,0}));

        // 方法2，测试1：
        System.out.println(findRepetaNumber2(new int[]{2, 3, 1, 0, 2, 5, 3}));
    }

    // 时间复杂度 O(n), 空间 O（1）
    // 技巧： 因为我们知道该数组的长度为n，所有数字的范围为0～n-1
    // 那么是不是可以进行重排， 把每个数放回到自身的位置（0放到0的位置
    // 如果存在重复数， 那必然会导致 该位置和准备要放入的数是一样的
    public static int findRepetaNumber(int[] nums){
        if(nums == null || nums.length == 0)
            return -1;

        // 做法：从头开始，把nums[i]放到到原本属于的位置
        for(int i = 0; i < nums.length; i++){
            while(nums[i] != i){
                if(nums[i] != nums[nums[i]]){
                    int tmp = nums[nums[i]];
                    nums[nums[i]] = nums[i];
                    nums[i] = tmp;
                }else{
                    return nums[i];
                }
            }
        }

        return -1;
    }

    // 第二种解法：对于重复数一类的题来说， 实在不想出好办法时候就使用 hashmap
    // 这里对hashmap做了优化， 用int数组当作map
    public static int findRepetaNumber2(int[] nums){
        int[] map = new int[nums.length];

        for(int i = 0; i < nums.length; i++){
            if(map[nums[i]] > 0)
                return nums[i];
            map[nums[i]]++;
        }

        return -1;
    }
}
