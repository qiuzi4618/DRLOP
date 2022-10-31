import java.util.ArrayList;
import java.util.List;

public class PMPoint implements Comparable<PMPoint> {
    //相点，x<=y
    private int x, y;
    private Rectangle rectangle;
    private List<Rectangle> rects = new ArrayList<>();

    PMPoint() {
    }

    PMPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    PMPoint(Rectangle rectangle) {
        int x1 = rectangle.getData()[0], x2 = rectangle.getData()[1], y1 = rectangle.getData()[2], y2 = rectangle.getData()[3];
        x = Math.min(x1, x2) + Math.min(y1, y2);
        y = Math.max(x1, x2) + Math.max(y1, y2);
        if (x > y) {
            y ^= x;
            x ^= y;
            y ^= x;
        }
        this.rectangle = rectangle;
        rects.add(rectangle);
        // x = rectangle.getData()[0] + rectangle.getData()[2];
        // y = rectangle.getData()[1] + rectangle.getData()[3];
        // this.rectangle = rectangle;
        // rects.add(rectangle);
    }

    /**
     * 矩形放大一倍
     * @return
     */
    public PMPoint GetExtend()
    {
        Rectangle ran = new Rectangle(rectangle.getData()[0] - GetHalfLength(), rectangle.getData()[1] + GetHalfLength(),
                                        rectangle.getData()[2] - GetHalfHeight(), rectangle.getData()[3] + GetHalfHeight());
        return new PMPoint(ran);
    }

    public int GetHalfLength()
    {
        return (rectangle.getData()[1] - rectangle.getData()[0]) /2;
    }

    public int GetHalfHeight()
    {
        return (rectangle.getData()[3] - rectangle.getData()[2]) / 2;
    }

    @Override
    public int hashCode() {
        return 1;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getMidX() {
        return (rectangle.getData()[0] + rectangle.getData()[1]) / 2;
    }

    public int getMidY() {
        return (rectangle.getData()[2] + rectangle.getData()[3]) / 2;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public List<Rectangle> getRectangles() {
        return rects;
    }

    public int[] getPoint() {
        int[] arr = {x, y};
        return arr;
    }

    public boolean isOverlap(PMPoint r) {
        int x2 = r.getX(), y2 = r.getY();
        //相点不同的直接判断相点相交的情况
//        if (!this.equals(r)) {
            return x2 <= x && x <= y2 || x2 <= y && y <= y2 ||
                    x <= x2 && x2 <= y || x <= y2 && y2 <= y;
//        }
        //相点一样看矩形的点
//        else {
//            return this.rectangle.isRectOverlap(r.rectangle);
//        }
    }

    /**
     * 判断是否包含对象
     * @param r
     * @return
     */
    public boolean isContain(PMPoint r) {
        int x2 = r.getX(), y2 = r.getY();
        return (x <= x2) && (y2 <= y);
    }

    public boolean isContain2(PMPoint r) 
    {
        // 不包含
        if (r.getRectangle().getData()[1] < rectangle.getData()[0] || r.getRectangle().getData()[0] > rectangle.getData()[1]
        || r.getRectangle().getData()[3] < rectangle.getData()[2] || r.getRectangle().getData()[2] > rectangle.getData()[3])
            return false;
            // 相交
            else
            return true;
        }

    public boolean isContain2(Rectangle r) 
    {
        // 不包含
        if (r.getData()[1] < rectangle.getData()[0] || r.getData()[0] > rectangle.getData()[1]
                || r.getData()[3] < rectangle.getData()[2] || r.getData()[2] > rectangle.getData()[3])
            return false;
        // 相交
        else
            return true;
    }

    /**
     * 包含类型(0 : 不包含, 1 : 完全包含, 2 : 相交)
     * @param r
     * @return
     */
    public int ContainType(PMPoint r) 
    {
        // 不包含
        if (r.getRectangle().getData()[1] < rectangle.getData()[0] || r.getRectangle().getData()[0] > rectangle.getData()[1]
                || r.getRectangle().getData()[3] < rectangle.getData()[2] || r.getRectangle().getData()[2] > rectangle.getData()[3])
            return 0;
        // 完全包含
        else if (r.getRectangle().getData()[0] < rectangle.getData()[0] || r.getRectangle().getData()[1] > rectangle.getData()[1]
                || r.getRectangle().getData()[2] < rectangle.getData()[2] || r.getRectangle().getData()[3] > rectangle.getData()[3])
            return 1;
        // 相交
        else
            return 2;
    }

    /**
     * 判断是否被包含
     * @param r
     * @return
     */
    public boolean isIncluding(PMPoint r) {
        return r.isContain(this);
    }

    @Override
    public int compareTo(PMPoint point) {
        int x = point.getX();
        int y = point.getY();
        if (this.x - x > 0)
            return 1;
        else if (this.x - x == 0) {
            if (this.y - y > 0)
                return -1;
            else if (this.y - y == 0)
                return 0;
            else
                return 1;
        } else
            return -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PMPoint) {
            if (((PMPoint) obj).x == this.x && ((PMPoint) obj).y == this.y) {
                return true;
            } else
                return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return "<" + x + ", " + y + "> " + rectangle;
    }
}
