package linked_list_set;

public class Main {
    public static void main(String[] args) {
        Set s = new SetImpl();
//        s.add(1);
//        System.out.println(s.contains(1));
//        s.remove(1);
//        System.out.println(s.contains(1));
//        s.add(2);
//        System.out.println(s.contains(1));
//        System.out.println(s.contains(2));
//        s.add(1);
//        System.out.println(s.contains(1));
//        s.remove(1);
//        System.out.println(s.contains(1));
//        s.remove(1);
//        s.remove(1);
//        System.out.println(s.contains(1));


        System.out.println(s.add(2));
        System.out.println(s.add(3));
        System.out.println(s.remove(3));
        System.out.println(s.add(3));
        System.out.println(s.add(3));
        System.out.println(s.remove(3));
        System.out.println(s.contains(2));
        System.out.println(s.remove(2));
        System.out.println(s.contains(2));
        System.out.println(s.contains(1));
        System.out.println(s.add(1));
        System.out.println(s.remove(1));
        System.out.println(s.add(1));
        s.contains(1);
    }
}
