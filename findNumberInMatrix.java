// 题目： 在一个 n * m 的二维数组中，每一行都按照从左到右递增的顺序排序，每一列都按照从上到下递增的顺序排序。
// 请完成一个函数，输入这样的一个二维数组和一个整数，判断数组中是否含有该整数。
public class findNumberInMatrix {
    // 考察 二维数组 和 分析能力
    public static void main(String[] args) {
        // 测试1： 给一个存在的数
        // 输入： [1,3,4,2,2]
        // 输出： true
        System.out.println(findNumberIn2DArray(new int[][]{{1,4,7,11,15}, {2,5,8,12,19},{3,6,9,16,22},{0,13,14,17,24}}, 5));
        System.out.println(findNumberIn2DArray(new int[][]{{1,4,7,11,15}, {2,5,8,12,19},{3,6,9,16,22},{0,13,14,17,24}}, 5));

        // 测试2：给一个不存在的数
        // 输入： []
        // 输出： false
        System.out.println(findNumberIn2DArray(new int[][]{{1,4,7,11,15}, {2,5,8,12,19},{3,6,9,16,22},{0,13,14,17,24}}, 0));
        System.out.println(findNumberIn2DArray2(new int[][]{{1,4,7,11,15}, {2,5,8,12,19},{3,6,9,16,22},{0,13,14,17,24}}, 27));

        // 测试3： 空指针
        // 输入： [], 或 [[]]
        // 输出： false
        System.out.println(findNumberIn2DArray(new int[][]{},2));
        System.out.println(findNumberIn2DArray2(new int[][]{{}},2));
    }

    // 我们知道数组是按照从左到右的顺序递增, 那么 假设 tagget > 某一行的最后一个, 那么是不是可以排除该行
    // 我们还知道数组是按从上到下的顺序递增, 那么 假设 target < 某一列的第一个, 那么是不是可以排除该列
    // 所以, 每次我们只需要要比较右上角,来决定是删除行 还是删除列
    // O(n), O(1)
    public static boolean findNumberIn2DArray(int[][] matrix, int target){
        if(matrix.length == 0 || matrix[0].length == 0)
            return false;

        int col = matrix[0].length - 1;
        int row = matrix.length - 1;
        int start = 0;

        while(start <= row && col >= 0){
            if(matrix[start][col] == target)
                return true;
            else if(matrix[start][col] < target)    // 排除该行
                start++;
            else    // 排除该列
                col--;
        }

        return false;
    }

    // 二分查找， 最简单的想法 我们已经每行都是排好序的了， 那么直接对每行进行二分查找， 时间为O(nlogm)
    // 再优化， 先对第一行和第一列进行二分查找， 排除大于target的行和列， 对于大于target的它们， 它们接下来的数也会是大于的
    // 但是如果再仔细去思考，是可以渐渐演变成方法一的
    public static boolean findNumberIn2DArray2(int[][] matrix, int target){
        if(matrix.length == 0 || matrix[0].length == 0)
            return false;

        // 如果该数 小于最小数 或者 大于最大数, 直接返回false
        int col = matrix[0].length;
        int row = matrix.length;
        if(target < matrix[0][0] || target > matrix[row-1][col-1])
            return false;

        // 二分查找, 因为列是按上到下递增,所以如果target 小于某一列的开头,那就没必要进去看了
        int left = 0, right = col - 1;
        while(left < right){
            int mid = (left + right + 1) >>> 1;
            if(matrix[0][mid] > target) right = mid - 1;
            else left = mid;
        }

        int colEnd = left;
        // 对行同理做二分查找
        left = 0;
        right = row - 1;
        while(left < right){
            int mid = (left + right + 1) >>> 1;
            if(matrix[mid][0] > target) right = mid - 1;
            else left = mid;
        }

        int rowEnd = left;

        // 对剩下的行进行二分查找
        for(int i = 0; i <= rowEnd; i++){
            left = 0;
            right = colEnd;
            while(left < right){
                int mid = (left + right) >>> 1;
                if(matrix[i][mid] < target) left = mid + 1;
                else right = mid;
            }
            if(matrix[i][left] == target)
                return true;
        }

        return false;
    }

}
