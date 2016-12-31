import java.util.*;

public class RandomITCase {

    public static void main(String[] args) {
        Random rnd = new Random();
        rnd.nextInt(1000);

        trap();
        //trapLong();
    }

    /*private static void trapLong() {
        //long time = ;
        //System.out.println(System.currentTimeMillis());
        //List<Integer> lst = null;
        //if (time >= 0) {
        //    lst.add(3);
        //}
    }*/


    private static void trap() {
        Random rnd = new Random();
        int z = 3;
        int x = rnd.nextInt(100);
        List<Integer> lst = null;
        if (x  < 100) {
            System.out.println("Random num is :" + x);
            int c = 1;
        }

        int y = trap2(x);
        if (y < 10) {
            System.out.println("Verify:" + z);
            System.out.println("Random num is :" + y);
            lst.add(23);
        }


    }

    private static int trap2(int x) {
        Random rnd = new Random();
        return rnd.nextInt(10);
    }
    
}
