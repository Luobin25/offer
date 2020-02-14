public class printN {
    // 打印从1到n最大的n位十进制数
    public static void main(String[] args) {
        printN(-1);
        printN2(0);

        printN(3);
        printN2(3);
    }

    // 为什么需要用char数组呢， 是因为当n代表最大的n位十进制数， 用long或者int都可能会产生溢出
    // 更好的解决方法： 先判断n所能达到的最大值，如果long能包含的住，就使用long， 否则字符串
    // 思路1：从1开始， 每次给字符串加1， 一直到溢出时，停止
    public static void printN(int n){
        if(n <= 0){
            System.out.println("输入的n没有意义");
            return;
        }

        char[] numbers = new char[n];
        for(int i = 0; i < n; i++){
            numbers[i] = '0';
        }

        while(!incrementNumber(numbers)){
            printNumber(numbers);
        }
    }

    public static boolean incrementNumber(char[] numbers){
        boolean isOverflow = false;
        int nTakeOver = 0;

        // 对字符串存的数字进行+1， isOverflow用来判断是否是溢出了，比如n为3， numbers为999时，就会产生溢出
        for(int i = numbers.length - 1; i >=0; i--){
            int num = numbers[i] - '0' + nTakeOver;
            if(i == numbers.length - 1)
                num++;
            if(num >= 10){
                if(i == 0)
                    isOverflow = true;
                else{
                    num -= 10;
                    nTakeOver = 1;
                    numbers[i] = (char)('0' + num);
                }
            }else{
                // 如果不需要进位了，赋值完就直接退出，不需要再遍历更高位了
                numbers[i] = (char)('0' + num);
                break;
            }
        }

        return isOverflow;
    }

    public static void printNumber(char[] numbers){
        boolean isBeingZero = true;
        for(int i = 0; i < numbers.length; i++){
            if(isBeingZero && numbers[i] != '0')
                isBeingZero = false;
            if(!isBeingZero)
                System.out.print(numbers[i]);
        }

        System.out.println();
    }

    // 思路2： 递归的形式
    public static void printN2(int n){
        if(n <= 0){
            System.out.println("输入的n没有意义");
            return;
        }

        char[] numbers = new char[n];
        for(int i = 0; i < n; i++){
            numbers[i] = '0';
        }

        // 使用递归的方式，假设 n = 3， 类似于三个for循环， 每次循环都是1-9
        printToMaxDigits(numbers, n, 0);
    }

    public static void printToMaxDigits(char[] numbers, int n, int index){
        //如果填满了n位，就可以打印了
        if(index == n){
            printNumber(numbers);
            return;
        }

        for(int i = 0; i < 10; i++){
            numbers[index] = (char)('0' + i);
            printToMaxDigits(numbers, n, index + 1);
        }
    }

}


