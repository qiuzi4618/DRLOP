import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
// import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
// import java.util.Queue;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

// import java.util.concurrent.ArrayBlockingQueue;

public class PMIndex {

    public synchronized void insert(List<PMPoint> points) {
        serials = new HashMap<>();
        Map<Integer, Map<Integer, List<PMPoint>>> datas = new HashMap<>();
        for (PMPoint point : points) {
            int x = point.getX();
            int y = point.getY();
            Map<Integer, List<PMPoint>> map = datas.getOrDefault(x, new HashMap<>());
            List<PMPoint> data = map.getOrDefault(y, new ArrayList<>());
            data.add(point);
            map.put(y, data);
            datas.put(x, map);
        }
        List<Entry<Integer, List<PMPoint>>> array = Collections.synchronizedList(new ArrayList<>());
        datas.forEach((x, map) -> {
            List<PMPoint> list = Collections.synchronizedList(new ArrayList<>());
            map.forEach((y, set) -> {
                set.forEach(point -> {
                    list.add(point);
                });
            });
            list.sort((a, b) -> a.compareTo(b));
            array.add(new SimpleEntry<>(x, list));
        });
        array.sort((a, b) -> a.getKey() - b.getKey());
        for (int i = 1; i < array.size(); i++) {
            int x = i;
            while (x < array.size()) {
                Entry<Integer, List<PMPoint>> e1 = array.get(x - 1);
                Entry<Integer, List<PMPoint>> e2 = array.get(x);
                if (e2.getKey() - e1.getKey() == 1) {
                    List<PMPoint> l1 = e1.getValue();
                    List<PMPoint> l2 = e2.getValue();
                    if (l1.size() > 0) {
                        PMPoint tail = l1.get(l1.size() - 1);
                        int l = 0, r = l2.size(), m;
                        while (l < r) {
                            m = (l + r) / 2;
                            PMPoint point = l2.get(m);
                            if (point.getY() == tail.getY()) {
                                l = m;
                                break;
                            } else if (point.getY() > tail.getY()) {
                                l = m + 1;
                            } else {
                                r = m;
                            }
                        }
                        ((Integer) l).toString();
                        if (l < l2.size()) {
                            List<PMPoint> sub = l2.subList(l, l2.size());
                            l1.addAll(sub);
                            l2.removeAll(sub);
                        }
                        x++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        array.forEach(e -> {
            List<PMPoint> list = e.getValue();
            if (list != null && list.size() > 0) {
                serials.put(list.get(0), new ArrayList<>(list));
            }
        });
    }

    public synchronized void insert2(List<PMPoint> data) {
        if (data == null || data.size() <= 0) {
            return;
        }
        serials = new HashMap<>();
        List<PMPoint> points = new ArrayList<>(data);
        Collections.sort(points);
        List<List<PMPoint>> array = new ArrayList<>();
        int x = -1;
        do {
            PMPoint point = points.get(0);
            List<PMPoint> list = null;
            if (point.getX() == x) {
                list = array.get(array.size() - 1);
            } else {
                list = new ArrayList<>();
                array.add(list);
                x = point.getX();
            }
            int index = points.size();
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i).getX() != x) {
                    index = i;
                    break;
                }
            }
            if (index > 0) {
                List<PMPoint> sub = points.subList(0, index);
                list.addAll(sub);
                points = new ArrayList<>(points.subList(index, points.size()));
            }
        } while (points.size() > 0);
        for (int i = 1; i < array.size(); i++) {
            x = i;
            while (x < array.size()) {
                List<PMPoint> l1 = array.get(x - 1);
                List<PMPoint> l2 = array.get(x);
                boolean _continue = false;
                if (l2 == null || l2.size() <= 0) {
                    array.remove(x);
                    _continue = true;
                }
                if (l1 == null || l1.size() <= 0) {
                    array.remove(x - 1);
                    _continue = true;
                }
                if (_continue) {
                    continue;
                }
                if (l2.get(0).getX() - l1.get(0).getX() == 1) {
                    PMPoint tail = l1.get(l1.size() - 1);
                    int l = 0, r = l2.size(), m;
                    while (l < r) {
                        m = (l + r) / 2;
                        PMPoint point = l2.get(m);
                        if (point.getY() > tail.getY()) {
                            l = m + 1;
                        } else {
                            r = m;
                        }
                    }
                    if (l < l2.size()) {
                        l1.addAll(l2.subList(l, l2.size()));
                        array.set(x, new ArrayList<>(l2.subList(0, l)));
                    }
                    x++;
                } else {
                    break;
                }
            }
        }
        for (int i = 0; i < array.size(); i++) {
            List<PMPoint> list = array.get(i);
            serials.put(list.get(0), list);
        }
    }

    public synchronized void add(PMPoint point) {
        List<PMPoint> addList = new ArrayList<>();
        addList.add(point);
        while (!addList.isEmpty()) {
            boolean addFlag = false;
            PMPoint addPoint = addList.get(0);
            for (Entry<PMPoint, List<PMPoint>> entry : serials.entrySet()) { // 查询所有列
                PMPoint large = entry.getKey();
                List<PMPoint> link = entry.getValue();
                if (large.isContain(addPoint)) { // 列最大值包含添加最大点
                    PMPoint tailPoint = link.get(link.size() - 1);
                    if (tailPoint.isContain(addPoint)) { // 列最小值包含添加最大点
                        if (addPoint.getX() == tailPoint.getX() || addPoint.getX() == tailPoint.getX() + 1) { // 列最小值与添加点x差1
                            link.addAll(addList);
                            addList.clear();
                            addFlag = true;
                            break;
                        }
                    } else { // 在列中插入添加列表
                        PMPoint[] pl = link.toArray(new PMPoint[0]);
                        int l, r;
                        l = 0; r = pl.length;
                        while (l < r) {
                            int m = (l + r) >>> 1;
                            PMPoint pm = pl[m];
                            if (pm.isContain(addPoint)) {
                                l = m + 1;
                            } else {
                                r = m;
                            }
                        }
                        int index = l - 1;
                        if (0 <= index && index <= link.size()) {
                            PMPoint indexPoint = link.get(index);
                            int sub = addPoint.getX() - indexPoint.getX();
                            if (0 <= sub && sub < 2) {
                                if (index + 1 >= link.size()) {
                                    link.addAll(addList);
                                    addList.clear();
                                    addFlag = true;
                                    break;
                                } else {
                                    PMPoint nextPoint = link.get(index + 1);
                                    if (addPoint.getX() <= nextPoint.getX()) {
                                        PMPoint atPoint = addList.get(addList.size() - 1);
                                        List<PMPoint> subList = new ArrayList<>(link.subList(index + 1, link.size()));
                                        pl = subList.toArray(new PMPoint[0]);
                                        l = 0; r = pl.length;
                                        while (l < r) {
                                            int m = (l + r) >>> 1;
                                            PMPoint pm = pl[m];
                                            if (atPoint.isContain(pm)) {
                                                r = m;
                                            } else {
                                                l = m + 1;
                                            }
                                        }
                                        int subIndex = l;
                                        if (0 <= subIndex && subIndex <= subList.size()) {
                                            List<PMPoint> nextAddList = null;
                                            List<PMPoint> lastLink = null;
                                            List<PMPoint> headLink = new ArrayList<>(link.subList(0, index + 1));
                                            if (subIndex < subList.size() && subList.get(subIndex).getX() - atPoint.getX() > 1) {
                                                nextAddList = new ArrayList<>(subList);
                                                lastLink = new ArrayList<>();
                                            } else {
                                                nextAddList = new ArrayList<>(subList.subList(0, subIndex));
                                                lastLink = new ArrayList<>(subList.subList(subIndex, subList.size()));
                                            }
                                            link.clear();
                                            link.addAll(headLink);
                                            link.addAll(addList);
                                            link.addAll(lastLink);
                                            addList.clear();
                                            addList.addAll(nextAddList);
                                            addFlag = true;
                                            break;
                                        // } else if (0 <= subIndex && subIndex <= subList.size()) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (addList.size() == 1 && addPoint.getX() - large.getX() < 2) {
                    link.add(0, addPoint);
                    addList.clear();
                    addFlag = true;
                    serials.remove(large);
                    serials.put(addPoint, link);
                    break;
                }
            }
            if (!addFlag) {
                serials.put(addPoint, new ArrayList<>(addList));
                addList.clear();
            }
        }
    }

    public synchronized List<Rectangle> query(Rectangle rect) {
        ArrayList<Rectangle> result = new ArrayList<>();
        Set<PMPoint> key = serials.keySet();
        PMPoint rectPoint = new PMPoint(rect);
        for (PMPoint k : key) {
            //在key的范围里，就有可能相交
            if (k.isOverlap(rectPoint)) {
                List<PMPoint> list = serials.get(k);
                //遍历该key的列表的所有点
                for (PMPoint p : list) {
                    if (p.getRectangle().isRectOverlap(rectPoint.getRectangle())) {
                        result.add(p.getRectangle());
                    }
                }
            }
        }
        return result;
    }

    public synchronized List<Rectangle> query2(Rectangle rect) {
        List<Rectangle> result = new ArrayList<>();
        Set<PMPoint> keys = serials.keySet();
        PMPoint rp = new PMPoint(rect);
        PMPoint rectPoint = new PMPoint(rp.getY() + 1, rp.getX() - 1);
        //omp parallel for
        for (PMPoint k : keys) {
            if (k.isContain(rectPoint)) {
                List<PMPoint> list = serials.get(k);
                int l = 0, r = list.size();
                while (l < r) {
                    int m = (l + r) >>> 1;
                    PMPoint pm = list.get(m);
                    if (pm.isContain(rectPoint)) {
                        l = m + 1;
                    } else {
                        r = m;
                    }
                }
                for (PMPoint p : list.subList(0, l)) {
                    if (p.getRectangle().isRectOverlap(rect)) {
                        result.add(p.getRectangle());
                    }
                }
            }
        }
        return result;
    }

    public synchronized List<Rectangle> query2_fork(Rectangle rect) {
        // System.out.println("line 331: " + Runtime.getRuntime().availableProcessors());
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        Future<List<Rectangle>> result = pool.submit(new QueryTaskRect(rect, serials));
        List<Rectangle> list = new ArrayList<>();
        // int val = -1;
        try {
            list = result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // val = pool.getActiveThreadCount();
            pool.shutdown();
        }
        return list;
    }

    class QueryTaskRect extends RecursiveTask<List<Rectangle>> {

        // public int flag = 2000;

        private boolean deep;

        private Rectangle rect;

        private List<PMPoint> input;
        private int start;
        private int end;

        private Iterator<Entry<PMPoint, List<PMPoint>>> iter;
        private PMPoint point;

        public QueryTaskRect(Rectangle rect, List<PMPoint> list, int start, int end) {
            this.rect = rect;
            input = list;
            this.start = start;
            this.end = end;
            deep = true;
        }
        public QueryTaskRect(Rectangle rect, Map<PMPoint, List<PMPoint>> map) {
            Iterator<Entry<PMPoint, List<PMPoint>>> entries = serials.entrySet().iterator();
            PMPoint rp = new PMPoint(rect);
            this.rect = rect;
            iter = entries;
            point = rp;
            deep = false;
        }
        protected List<Rectangle> compute() {
            List<Rectangle> list = new ArrayList<>();
            if (rect == null) {
                return list;
            }
            if (deep) {
                if (end - start < arraySize) {
                    for (int i = start; i <= end; i++) {
                        PMPoint p = input.get(i);
                        Rectangle r = p.getRectangle();
                        if (/*r != null && */r.isRectOverlap(rect)) {
                            list.add(p.getRectangle());
                        }
                    }
                } else {
                    int middle = (start + end) / 2;
                    QueryTaskRect task1 = new QueryTaskRect(rect, input, start, middle);
                    QueryTaskRect task2 = new QueryTaskRect(rect, input, middle + 1, end);
                    invokeAll(task1, task2);
                    list.addAll(task1.join());
                    list.addAll(task2.join());
                }
            } else {
                PMPoint rectPoint = new PMPoint(point.getY() + 1, point.getX() - 1);
                List<QueryTaskRect> tasks = new ArrayList<>();
                while (iter.hasNext()) {
                    Entry<PMPoint, List<PMPoint>> entry = iter.next();
                    PMPoint k = entry.getKey();
                    if (k.isContain(rectPoint)) {
                        List<PMPoint> ll = entry.getValue();
                        int l = 0, r = ll.size();
                        while (l < r) {
                            int m = (l + r) >>> 1;
                            PMPoint pm = ll.get(m);
                            if (pm.isContain(rectPoint)) {
                                l = m + 1;
                            } else {
                                r = m;
                            }
                        }
                        List<PMPoint> sub = ll.subList(0, l);
                        if (sub.size() > 0) {
                            tasks.add(new QueryTaskRect(rect, sub, 0, sub.size() - 1));
                        }
                    }
                }
                // for (QueryTaskRect task : tasks) {
                //     task.fork();
                // }
                invokeAll(tasks);
                for (QueryTaskRect task : tasks) {
                    List<Rectangle> ret = task.join();
                    list.addAll(ret);
                }
            }
            return list;
        }
    }

    public synchronized List<Rectangle> query2_while(Rectangle rect) {
        List<Rectangle> result = new ArrayList<>();
        PMPoint rp = new PMPoint(rect);
        PMPoint rectPoint = new PMPoint(rp.getY() + 1, rp.getX() - 1);
        Iterator<Entry<PMPoint, List<PMPoint>>> entries = serials.entrySet().iterator();
        while (entries.hasNext()) {
            Entry<PMPoint, List<PMPoint>> entry = entries.next();
            PMPoint k = entry.getKey();
            if (k.isContain(rectPoint)) {
                List<PMPoint> list = entry.getValue();
                int l = 0, r = list.size();
                while (l < r) {
                    int m = (l + r) >>> 1;
                    PMPoint pm = list.get(m);
                    if (pm.isContain(rectPoint)) {
                        l = m + 1;
                    } else {
                        r = m;
                    }
                }
                for (PMPoint p : list.subList(0, l)) {
                    if (p.getRectangle().isRectOverlap(rect)) {
                        result.add(p.getRectangle());
                    }
                }
            }
        }
        return result;
    }

    public synchronized List<Rectangle> query2_foreach(Rectangle rect) {
        // List<Rectangle> result = Collections.synchronizedList(new ArrayList<>());
        List<Rectangle> result = new ArrayList<>();
        Set<PMPoint> keys = serials.keySet();
        PMPoint rp = new PMPoint(rect);
        PMPoint rectPoint = new PMPoint(rp.getY() + 1, rp.getX() - 1);
        keys.parallelStream().forEach(k -> {
            if (k.isContain(rectPoint)) {
                List<PMPoint> list = serials.get(k);
                int l = 0, r = list.size();
                while (l < r) {
                    int m = (l + r) >>> 1;
                    PMPoint pm = list.get(m);
                    if (pm.isContain(rectPoint)) {
                        l = m + 1;
                    } else {
                        r = m;
                    }
                }
                list.subList(0, l).forEach(p -> {
                    if (p.getRectangle().isRectOverlap(rect)) {
                        result.add(p.getRectangle());
                    }
                });
            }
        });
        return result;
    }

    public synchronized int size() {
        return serials.size();
    }

    public synchronized List<Integer> linkSize() {
        List<Integer> list = new ArrayList<>();
        Iterator<Entry<PMPoint, List<PMPoint>>> entries = serials.entrySet().iterator();
        while (entries.hasNext()) {
            list.add(entries.next().getValue().size());
        }
        return list;
    }

    public synchronized int count() {
        Set<PMPoint> keys = serials.keySet();
        int i = 0;
        for (PMPoint k : keys) {
            i += serials.get(k).size();
        }
        return i;
    }

    PMIndex() {}

    PMIndex(PMPoint key, int level, PMIndex parent)
    {
        m_level = level;
        m_rectNum = 0;
        m_parent = parent;
        m_children = new PMIndex[4];
        m_PointList = new ArrayList<>();
        InitPMPoint(key);
    }

    private synchronized void InitPMPoint(PMPoint point)
    {
        m_key = point;
        m_keyExtend = point.GetExtend();
        m_center = new Point2D(point);
        m_halfLength = point.GetHalfLength();
        m_halfHeight = point.GetHalfLength();
    }

    public synchronized void insert3(PMPoint pt)
    {
        if (pt == null) {
            return;
        }

        boolean insertHere = false;
        if(m_level == 0)
        {
            insertHere = (!m_keyExtend.isContain2(pt) || CheckInsertPos(pt));
        }
        else
        {
            insertHere = CheckInsertPos(pt);
        }

        if(insertHere)
        {
            AddRectangle(pt.getRectangle());
        }
        else
        {
            int x = pt.getMidX() < m_center.getX() ? 0 : 1;
            int y = pt.getMidY() < m_center.getY() ? 0 : 2;

            GetOrCreateChild(x + y).insert3(pt);
        }
    }

    public synchronized List<Rectangle> query3_fork(Rectangle rect, List<Rectangle> totalList) {
        // System.out.println("line 331: " + Runtime.getRuntime().availableProcessors());
        ForkJoinPool pool = new ForkJoinPool(parallelism);
        Future<List<Rectangle>> result = pool.submit(new QueryTaskPMIPt(rect, this));
        List<Rectangle> list = new ArrayList<>();
        // int val = -1;
        try {
            list = result.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            // val = pool.getActiveThreadCount();
            pool.shutdown();
        }
        totalList.addAll(list);
        intersectionNum += list.size();

        return list;
    }

    private synchronized PMIndex GetOrCreateChild(int index)
    {
        if (m_children[index] != null)
			return m_children[index];

        Point2D newMinPt = new Point2D(m_key.getRectangle().getData()[0],m_key.getRectangle().getData()[2]);
        Point2D newMaxPt = new Point2D(m_key.getRectangle().getData()[1],m_key.getRectangle().getData()[3]);
        Point2D oldMidPt = new Point2D((newMinPt.getX() + newMaxPt.getX()) / 2, (newMinPt.getY() + newMaxPt.getY()) / 2);

        if ((index & 1) != 0)
        {
            newMinPt.setX(oldMidPt.getX());
        }
        else
        {
            newMaxPt.setX(oldMidPt.getX());
        }

        if ((index & 2) != 0)
        {
            newMinPt.setY(oldMidPt.getY());
        }
        else
        {
            newMaxPt.setY(oldMidPt.getY());
        }

        PMPoint pt = new PMPoint(new Rectangle(newMinPt.getX(),newMaxPt.getX(),newMinPt.getY(),newMaxPt.getY()));
        m_children[index] = new PMIndex(pt, m_level + 1, this);

        return m_children[index];
    }


    private synchronized boolean CheckInsertPos(PMPoint pt)
    {
        int length = pt.getRectangle().getData()[1] - pt.getRectangle().getData()[0];
        int height = pt.getRectangle().getData()[3] - pt.getRectangle().getData()[2];

        if(m_level >= 16 || length >= m_halfLength || height >= m_halfHeight)
        {
            return true;
        }
        else
        {
            if(pt.getRectangle().getData()[0] <= m_key.getRectangle().getData()[0] - 0.5f * m_halfLength ||
               pt.getRectangle().getData()[1] >= m_key.getRectangle().getData()[1] + 0.5f * m_halfLength ||
               pt.getRectangle().getData()[2] <= m_key.getRectangle().getData()[2] - 0.5f * m_halfHeight ||
               pt.getRectangle().getData()[3] >= m_key.getRectangle().getData()[3] + 0.5f * m_halfHeight )
            {
                return true;
            }
        }
        return false;
    }

    private synchronized void AddRectangle(Rectangle pt)
    {
        m_PointList.add(pt);
        IncRectangleCount();
    }

    private synchronized void IncRectangleCount()
    {
        ++m_rectNum;
        if(m_parent != null)
        {
            m_parent.IncRectangleCount();
        }
    }

    public void FindItems(PMPoint rect, List<Rectangle> items, int start, int end)
    {
        //RectBox box = new RectBox(new Point2D(rect.getData()[0], rect.getData()[2]),new Point2D(rect.getData()[1], rect.getData()[3]));

        if (!m_PointList.isEmpty())
        {
        /* 	for (Rectangle elem : m_PointList)
            {
                if (rect.isContain2(elem.convert2PMPoint(elem)))
                {
                    items.add(elem);
                }
            } */
            for(int i = start; i < end; i++)
            {
                if (rect.isContain2(m_PointList.get(i)))
                {
                    items.add(m_PointList.get(i));
                }
            }
        }
    }

    public void GetTaskNode(PMPoint rect,List<QueryTaskPMIPt> tasks, List<Rectangle> rectList)
    {

    /*     if(m_PointList.size() > listSize)
        {
            List<Integer> array = new ArrayList<>();
            DivideList(m_PointList, array);
            for(int i = 0;i<array.size() - 1;i++)
            {
                tasks.add(new QueryTaskPMIPt(rect, m_PointList ,this, array.get(i),array.get(i + 1), false));
            }
        }
        else */
        int res = rect.ContainType(this.m_keyExtend);

        if(res == 0)
        {
            return;
        }
        else if(res == 1)
        {
            tasks.add(new QueryTaskPMIPt(rect.getRectangle(),this,0,m_PointList.size()));
        }
        else
        {
            getAllRectangle(rectList);
            return;
        }

        for (PMIndex child : m_children)
        {
            if (child != null)
            {
                //tasks.add(new QueryTaskPMIPt(rect, child.m_PointList, child, true));
                child.GetTaskNode(rect,tasks, rectList);
            }
        }
    }

    void getAllRectangle(List<Rectangle> rectList)
    {
        rectList.addAll(m_PointList);
        for (PMIndex child : m_children)
        {
            if (child != null)
            {
                child.getAllRectangle(rectList);
            }
        }
    }

    /**
     * @param points
     */
    public void DivideList(List<Rectangle> points, List<Integer> index)
    {
        int num = points.size() / listSize + 1;
        int step = points.size() / num;

        int i = 1;
        index.add(0);
        while(step * i < points.size())
        {
            index.add(step * i);
            i++;
        }
        index.add(points.size());
    }

    class QueryTaskPMIPt extends RecursiveTask<List<Rectangle>>
    {
        private boolean deep;
        private boolean first;
        private Rectangle rect;
        private PMPoint point;
        private PMIndex m_node;
        private int start;
        private int end;

        public QueryTaskPMIPt(Rectangle rect, PMIndex node, int start, int end)
        {
            PMPoint rp = new PMPoint(rect);
            this.rect = rect;
            point = rp;
            deep = true;
            this.start = start;
            this.end = end;
            m_node = node;
        }
        public QueryTaskPMIPt(Rectangle rect, PMIndex node)
        {
            PMPoint rp = new PMPoint(rect);
            this.rect = rect;
            point = rp;
            deep = false;
            m_node = node;
        }

        protected List<Rectangle> compute() {
            List<Rectangle> list = new ArrayList<>();
            if (rect == null) {
                return list;
            }
            if (deep)
            {
                if (end - start < arraySize)
                {
                    m_node.FindItems(point, list, start, end);
                }
                else
                {
                    int middle = (start + end) / 2;
                    QueryTaskPMIPt task1 = new QueryTaskPMIPt(rect, m_node, start, middle);
                    QueryTaskPMIPt task2 = new QueryTaskPMIPt(rect, m_node, middle ,end);
                    invokeAll(task1, task2);
                    list.addAll(task1.join());
                    list.addAll(task2.join());
                }
            }
            else
            {
                List<QueryTaskPMIPt> tasks = new ArrayList<>();
                m_node.GetTaskNode(point, tasks, list);
                invokeAll(tasks);
                for (QueryTaskPMIPt task : tasks) {
                    List<Rectangle> ret = task.join();
                    list.addAll(ret);
                }
            }
            return list;
        }
    }

    private PMIndex m_parent;
    private PMIndex m_children[];

    private int m_level;
    public  int m_rectNum;
    private PMPoint m_key;
    private PMPoint m_keyExtend;

    private Point2D m_center;
    public  int m_halfLength;
    public  int m_halfHeight;

    public static int parallelism = Runtime.getRuntime().availableProcessors();
    public static int arraySize = 2000;
    public static int listSize = 100000;

    public static int x = 0;
    public static int y = 0;
    public static int z = 0;

    public static int intersectionNum = 0;
    private Map<PMPoint, List<PMPoint>> serials = new HashMap<>();
    private List<Rectangle> m_PointList;
}
