import java.security.cert.TrustAnchor;

public class Rectangle {
    private int[] data;//矩形数据，左下角
    private float x1;
    private float y1;
    private float x2;
    private float y2;

    public Rectangle() {
    }

    public Rectangle(int[] data) {
        this(data[0], data[1], data[2], data[3]);
    }

    public Rectangle(int x1, int x2, int y1, int y2) {
        int[] arr = {x1, x2, y1, y2};
        this.data = arr;
        System.arraycopy(data, 0, this.data, 0, data.length);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public PMPoint convert2PMPoint(Rectangle rectangle) {
        PMPoint pmPoint = new PMPoint(rectangle);
        return pmPoint;
    }

    //判断两个矩形是否相交(相交+包围)
    public boolean isRectOverlap(Rectangle r2) {
        Rectangle r1 = this;
        if (Math.abs((r1.x1 + r1.x2) / 2 - (r2.x1 + r2.x2) / 2) <= ((r1.x2 + r2.x2 - r1.x1 - r2.x1) / 2)
                && Math.abs((r1.y1 + r1.y2) / 2 - (r2.y1 + r2.y2) / 2) <= ((r1.y2 + r2.y2 - r1.y1 - r2.y1) / 2)) {
            return true;
        }
        return false;
    }

    //测试相交
    public static void main(String[] args) {
        // 测试相点相同的情况
        Rectangle r1 = new Rectangle(2, 5, 5, 8);
        Rectangle r2 = new Rectangle(6, 9, 1, 4);
        Rectangle r3 = new Rectangle(5, 8, 2, 5);
        System.out.println(r1.isRectOverlap(r2));
        System.out.println(r2.isRectOverlap(r1));
        System.out.println(r1.isRectOverlap(r3));
        System.out.println(r3.isRectOverlap(r1));
        System.out.println(r2.isRectOverlap(r3));
        System.out.println(r3.isRectOverlap(r2));

        //测试包围的情况
        Rectangle r4 = new Rectangle(3, 4, 6, 7);
        Rectangle r5 = new Rectangle(1, 6, 4, 9);
        System.out.println(r4.isRectOverlap(r1));
        System.out.println(r1.isRectOverlap(r4));
        System.out.println(r5.isRectOverlap(r1));
        System.out.println(r1.isRectOverlap(r5));

        //测试相点不相同的情况
        Rectangle r6 = new Rectangle(0, 2, 0, 2);
        Rectangle r7 = new Rectangle(1, 3, 1, 3);
        Rectangle r8 = new Rectangle(4, 5, 4, 5);
        System.out.println(r6.isRectOverlap(r7));
        System.out.println(r7.isRectOverlap(r6));
        System.out.println(r6.isRectOverlap(r8));
        System.out.println(r8.isRectOverlap(r6));
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        if (obj instanceof Rectangle) {
            Rectangle r = (Rectangle) obj;
            if (r.x1 == x1 && r.x2 == x2 && r.y1 == y1 && r.y2 == y2) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "(" + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3] + ")";
    }

    public int[] getData() {
        return data;
    }

}
